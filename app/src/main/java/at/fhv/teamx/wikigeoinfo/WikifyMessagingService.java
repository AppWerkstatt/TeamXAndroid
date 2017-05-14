package at.fhv.teamx.wikigeoinfo;

import android.util.Log;

import com.google.firebase.messaging.*;

public class WikifyMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "Wikify";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
