package at.fhv.teamx.wikigeoinfo;

import java.io.Serializable;

/**
 * Created by lukasboehler on 14.05.17.
 */

public class POI implements Serializable {
    double longitude;
    double latitude;
    int pageId;
    String title;
    String type;
}
