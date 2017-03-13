package at.fhv.teamx.wikigeoinfo;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                loadNearbyWikidata();
            }
        });
        loadNearbyWikidata();
    }

    private void loadNearbyWikidata() {
        try {
            String wikipediaAPISearchPlaces = "https://en.wikipedia.org/w/api.php?action=query&list=geosearch&gscoord=" + mMap.getCameraPosition().target.latitude + "-" + mMap.getCameraPosition().target.longitude + "&gsradius=10000&gslimit=50&format=json";
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

                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(title));
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
