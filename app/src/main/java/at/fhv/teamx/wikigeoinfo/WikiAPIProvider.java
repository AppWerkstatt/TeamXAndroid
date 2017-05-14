package at.fhv.teamx.wikigeoinfo;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by lukasboehler on 14.05.17.
 */

public class WikiAPIProvider {
    interface WikiAPICompletionHandler {
        public void handle(ArrayList<POI> pois);
    }

    public static void loadPOIS(double latitude, double longitude, final WikiAPICompletionHandler completionHandler) {
        try {
            String wikipediaAPISearchPlaces = "https://en.wikipedia.org/w/api.php?action=query&list=geosearch&gscoord=" + latitude + "|" + longitude + "&gsradius=10000&gslimit=200&format=json&gsprop=type";
            new APIProvider(new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(JSONObject result) {
                    try {
                        ArrayList<POI> poiArrayList = new ArrayList<POI>();
                        JSONArray places = result.getJSONObject("query").getJSONArray("geosearch");
                        for (int i = 0; i < places.length(); i++) {
                            JSONObject currentPlace = places.getJSONObject(i);
                            POI poi = new POI();
                            poi.longitude = currentPlace.getDouble("lon");
                            poi.latitude = currentPlace.getDouble("lat");
                            poi.title = currentPlace.getString("title");
                            poi.pageId = currentPlace.getInt("pageid");
                            String poiType = currentPlace.getString("type");
                            if (poiType == null) {
                                poiType = "landmark";
                            }
                            poi.type = poiType;
                            poiArrayList.add(poi);
                        }
                        completionHandler.handle(poiArrayList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        completionHandler.handle(null);
                    }
                }
            }).execute(new URL(wikipediaAPISearchPlaces));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            completionHandler.handle(null);
        }
    }
}
