package at.fhv.teamx.wikigeoinfo;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thefinestartist.finestwebview.FinestWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void loadNearbyWikidata() {
        try {
            String wikipediaAPISearchPlaces = "https://en.wikipedia.org/w/api.php?action=query&list=geosearch&gscoord=" + mMap.getCameraPosition().target.latitude + "|" + mMap.getCameraPosition().target.longitude + "&gsradius=10000&gslimit=200&format=json";
            new APIProvider(new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(JSONObject result) {
                    mMap.clear();
                    try {
                        JSONArray places = result.getJSONObject("query").getJSONArray("geosearch");
                        for (int i = 0; i < places.length(); i++) {
                            JSONObject currentPlace = places.getJSONObject(i);
                            double lon = currentPlace.getDouble("lon");
                            double lat = currentPlace.getDouble("lat");
                            String title = currentPlace.getString("title");
                            int pageid = currentPlace.getInt("pageid");

                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(title));
                            marker.setTag(pageid);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(new URL(wikipediaAPISearchPlaces));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int tag = (int)marker.getTag();
        if (tag > 0) {
            new FinestWebView.Builder(MapsActivity.this).show("http://en.wikipedia.org/?curid=" + tag);
        }
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private LatLng oldPos = null;
    private float oldZoom = 0;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng dornbirn = new LatLng(47.413070, 9.744314);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dornbirn, (float)14.0));
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
