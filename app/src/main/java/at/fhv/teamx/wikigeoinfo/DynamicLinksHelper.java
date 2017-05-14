package at.fhv.teamx.wikigeoinfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by lukasboehler on 14.05.17.
 */

public class DynamicLinksHelper {
    public static String createDynamicLink(int pageId) {
        String link = "";
        try {
            link = URLEncoder.encode("https://en.m.wikipedia.org/w/index.php?title=Translation&curid=" + pageId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String baseLink = "https://a57za.app.goo.gl/?link=" + link + "&apn=at.fhv.teamx.wikigeoinfo&al=" + pageId;
        return baseLink;
    }
}
