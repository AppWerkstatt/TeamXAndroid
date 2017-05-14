package at.fhv.teamx.wikigeoinfo;

import java.io.Serializable;

/**
 * Created by lukasboehler on 14.05.17.
 */

public class FirPOI implements Serializable {
    private String articleId;
    private String title;

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
