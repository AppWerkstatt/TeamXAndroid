package at.fhv.teamx.wikigeoinfo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by lukasboehler on 13.03.17.
 */

public class APIProvider extends AsyncTask<URL, Void, JSONObject> {
    private OnTaskCompleted listener;
    private String result;

    public APIProvider(OnTaskCompleted listener){
        this.listener=listener;
    }

    @Override
    protected JSONObject doInBackground(URL... params) {
        if (params.length > 0) {
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try {
                URL url = params[0];
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONObject(stringBuffer.toString());
            } catch(Exception ex) {
                return null;
            } finally {
                if(bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    protected void onPostExecute(JSONObject result) {
        listener.onTaskCompleted(result);
    }
}