package at.fhv.teamx.wikigeoinfo;

import org.json.JSONObject;

/**
 * Created by lukasboehler on 13.03.17.
 */
public interface OnTaskCompleted{
    void onTaskCompleted(JSONObject result);
}