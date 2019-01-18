package example.vaibhav.com.exiomsvaibhavjoshi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private final Context mContext;
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location = null; // location
    double latitude; // latitude
    double longitude; // longitude
    int flag = 0;// notifier var
    float distance;
    private Location mylocation = new Location("");
    private Location dest_location = new Location("");
    private Notification notification;
    private NotificationManager notifier;

    public GPSTracker(Context context) {
        mContext = context;
        getLocation(context);
    }

    @SuppressLint("MissingPermission")
    public Location getLocation(Context context) {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = getLocation(mContext);
        Log.i("Tag", "location changed");
        distance = mylocation.distanceTo(dest_location);
        Log.i("Tag", "" + distance);
        if (flag == 0) {
            if ((distance / 1000) < 1) {
                Log.i("Distance", "dist. b/w < 1km");
                Log.d("location", "" + distance);
                NotificationManager notificationManager = (NotificationManager) mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        mContext, 1, new Intent(mContext, MainActivity.class), 0);
                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                builder.setAutoCancel(false);
                builder.setTicker("Current Location");
                builder.setContentTitle("Near To Destination");
                builder.setContentText("You are within 1 km from ur destination!!");
                builder.setSmallIcon(R.drawable.ic_launcher_foreground);
                builder.setContentIntent(pendingIntent);
                builder.setOngoing(true);
                builder.setSubText("You are "
                        + distance + " meters "
                        + " away from your point of interest ");
                builder.setNumber(100);
                builder.build();

                notification = builder.getNotification();
                notificationManager.notify(11, notification);


                flag = 1;
                onDestroy();
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}

