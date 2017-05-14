package at.fhv.teamx.wikigeoinfo;

/**
 * Created by lukasboehler on 14.05.17.
 */

public class FlyTo {
    private double lng = 0.0;
    private double lat = 0.0;
    private String name = "";
    private String url = "";

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
