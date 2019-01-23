package example.vaibhav.com.exiomsvaibhavjoshi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    protected static final String TAG = "GeofenceTransitionsIS";

    public GeofenceTransitionsIntentService() {
        super(TAG);  // use TAG to name the IntentService worker thread
    }

    private static String
    getGeofenceTransitionDetails(GeofencingEvent event) {
        String transitionString =
                GeofenceStatusCodes.getStatusCodeString(event.getGeofenceTransition());
        List triggeringIDs = new ArrayList();
        for (Geofence geofence : event.getTriggeringGeofences()) {
            triggeringIDs.add(geofence.getRequestId());
        }
        return String.format("%s: %s", transitionString, TextUtils.join(", ", triggeringIDs));
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
//        if (event.hasError()) {
//            Log.e(TAG, "GeofencingEvent Error: " + event.getErrorCode());
//            return;
//        }
//
//        String description = getGeofenceTransitionDetails(event);
////        sendNotification(description);
//    }

//    private void sendNotification(String notificationDetails) {
//        // Create an explicit content Intent that starts MainActivity.
//        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//
//        // Get a PendingIntent containing the entire back stack.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class).addNextIntent(notificationIntent);
//        PendingIntent notificationPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Get a notification builder that's compatible with platform versions >= 4
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        // Define the notification settings.
//        builder.setColor(Color.RED)
//                .setContentTitle(notificationDetails)
//                .setContentText("Click notification to return to App")
//                .setContentIntent(notificationPendingIntent)
//                .setAutoCancel(true)
//                .setSmallIcon(R.drawable.ic_launcher_background);
//
//
//        // Fire and notify the built Notification.
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0, builder.build());
//
//    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }

        // Retrieve GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            // Create a detail message with Geofences received
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences);
            // Send notification details as a String
            sendNotification(geofenceTransitionDetails);
        }
    }

    private void sendNotification(String msg) {

        Log.i(TAG, "sendNotification: " + msg);

        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(
                getApplicationContext(), msg
        );

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificatioMng.notify(
                0,
                createNotification(msg, notificationPendingIntent, notificatioMng));


    }

    // Create a notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent, NotificationManager notificatioMng) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "80001";
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "NoTUII", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificatioMng.createNotificationChannel(notificationChannel);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder
                    .setSmallIcon(R.drawable.splash)
                    .setColor(Color.RED)
                    .setContentTitle("Geofence Notification!")
                    .setContentText(msg)
                    .setContentIntent(notificationPendingIntent)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setChannelId(channelId);
            return notificationBuilder.build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder
                    .setSmallIcon(R.drawable.splash)
                    .setColor(Color.RED)
                    .setContentTitle(msg)
                    .setContentText("Geofence Notification!")
                    .setContentIntent(notificationPendingIntent)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                    .setAutoCancel(true);
            return notificationBuilder.build();
        }


    }

    // Create a detail message with Geofences received
    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering ";
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting ";
        return status + TextUtils.join(", ", triggeringGeofencesList);
    }
}