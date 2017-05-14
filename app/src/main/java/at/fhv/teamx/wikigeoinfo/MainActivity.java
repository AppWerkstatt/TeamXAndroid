package at.fhv.teamx.wikigeoinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.thefinestartist.finestwebview.FinestWebView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private static final int PERMISSION_ACCESS_COURSE_LOCATION = 527;
    private boolean followUserLocation = true;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setupDeepLinking();
        startLocationUpdates();
    }

    private void setupDeepLinking() {
        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);

                                    Log.d("Wikify", deepLink);
                                } else {
                                    Log.d("Wikify", "getInvitation: no deep link found.");
                                }
                            }
                        });
    }

    private void startLocationUpdates() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_ACCESS_COURSE_LOCATION );
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COURSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.gpsoffsnack), Snackbar.LENGTH_LONG)
                            .setAction(getResources().getString(R.string.settingsgps), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (followUserLocation) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, (float)10.0));
            followUserLocation = false;
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.gpsoffsnack), Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.settingsgps), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.gpsison), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void loadNearbyWikidata() {
        WikiAPIProvider.loadPOIS(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude, new WikiAPIProvider.WikiAPICompletionHandler() {
            @Override
            public void handle(ArrayList<POI> pois) {
                mMap.clear();
                for (int i = 0; i < pois.size(); i++) {
                    POI currentPOI = pois.get(i);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(currentPOI.latitude, currentPOI.longitude))
                            .title(currentPOI.title));
                    int source = getResources().getIdentifier(currentPOI.type + "_pin", "drawable", getPackageName());
                    if (source != 0) {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(source));
                    } else {
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.city_pin));
                    }
                    marker.setTag(currentPOI.pageId);
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = (int)marker.getTag();
        if (tag > 0) {
            // Track the article
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "article_" + tag);
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, marker.getTitle());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "article");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            // Open the activity
            new FinestWebView.Builder(MainActivity.this).show("https://en.m.wikipedia.org/w/index.php?title=Translation&curid=" + tag);
        }
        return true;
    }

    private LatLng oldPos = null;
    private float oldZoom = 0;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (oldPos == null) {
                    return;
                }
                double latOff = Math.abs(oldPos.latitude - mMap.getCameraPosition().target.latitude);
                double lonOff = Math.abs(oldPos.longitude - mMap.getCameraPosition().target.longitude);
                System.out.print(latOff);
                System.out.print(lonOff);
                float newZoom = mMap.getCameraPosition().zoom;
                if (latOff > 0.05 || lonOff > 0 || oldZoom != newZoom) {
                    loadNearbyWikidata();
                }
            }
        });
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                oldPos = mMap.getCameraPosition().target;
                oldZoom = mMap.getCameraPosition().zoom;
            }
        });
        loadNearbyWikidata();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(getWindow().getDecorView().getRootView(), getResources().getString(R.string.checkinternetconnection), Snackbar.LENGTH_LONG).show();
    }
}
