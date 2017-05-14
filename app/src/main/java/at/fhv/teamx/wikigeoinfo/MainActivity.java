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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thefinestartist.finestwebview.FinestWebView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private static final int PERMISSION_ACCESS_COURSE_LOCATION = 527;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startLocationUpdates();
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
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, (float)10.0));
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
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.city_pin));
                    marker.setTag(currentPOI.pageId);
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = (int)marker.getTag();
        if (tag > 0) {
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
}
