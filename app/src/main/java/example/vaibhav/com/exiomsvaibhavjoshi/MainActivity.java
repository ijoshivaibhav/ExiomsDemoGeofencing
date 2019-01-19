package example.vaibhav.com.exiomsvaibhavjoshi;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, AdapterView.OnItemSelectedListener {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 12 * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1000;
    public Results results;
    public HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    ArrayList<Geofence> mGeofenceList, selectedGeofence;
    int selectedItem = 0;
    private EditText medtCurrentLocation;
    private Spinner mSpinnerPlaces;
    private Button mbtnStart, mbtnStop, mbtnSearch;
    private GPSTracker mGPSTracker;
    private GeofencingClient mGeofencingClient;
    private GoogleApiClient mGoogleApiClient;

    static Intent makeNotificationIntent(Context geofenceService, String msg) {
        Log.d("tag", msg);
        return new Intent(geofenceService, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        init();
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?type=" + "restaurent" + "&radius=1000&location=" + mGPSTracker.getLatitude() + "," + mGPSTracker.getLongitude() + "&&key=AIzaSyCbyFEyzpx5GQC37MBHMG6EPgMaMZcWwA0",
                    null, new MyResponseListener(), new MyErrorListener());
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mGPSTracker.canGetLocation) {
            String currentAddress = getCurrentAddress(mGPSTracker.getLatitude(), mGPSTracker.getLongitude());
            medtCurrentLocation.setText(currentAddress + "");
        } else {
            Toast.makeText(this, "Enable GPS", Toast.LENGTH_LONG).show();
        }

        mGeofenceList = new ArrayList<>();
        selectedGeofence = new ArrayList<>();

        buildGoogleApiClient();


        mbtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addGeofencesButtonHandler(v);
            }
        });


        mbtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGeofence();
            }
        });
    }


    public void stopGeofence() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : LANDMARKS.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
    }


    private void init() {
        medtCurrentLocation = findViewById(R.id.edtCurrentLocation);
        mSpinnerPlaces = findViewById(R.id.edtDestLocation);
        mbtnStart = findViewById(R.id.btnStart);
        mbtnStop = findViewById(R.id.btnStop);
        mbtnSearch = findViewById(R.id.btnsearch);

        mGPSTracker = new GPSTracker(this);
        mSpinnerPlaces.setOnItemSelectedListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void addGeofencesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(selectedGeofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            String address = addresses.get(0).getAddressLine(0);
            return address;
        }
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Toast.makeText(
                    this,
                    "Success, You will be notified once reached to destination !",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        selectedItem = parent.getSelectedItemPosition();
        selectedGeofence.add(mGeofenceList.get(selectedItem));


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    class MyResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {

            Gson gson = new Gson();
            results = gson.fromJson(response.toString(), Results.class);

            switch (results.status) {
                case "OK":
                    for (Place p : results.results) {
                        LANDMARKS.put(p.name, new LatLng(p.geometry.location.lat, p.geometry.location.lng));
                    }
                    populateGeofenceList();
                    ArrayAdapter<Place> adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_dropdown_item_1line, mGeofenceList);
                    mSpinnerPlaces.setAdapter(adapter);
                    System.out.print(LANDMARKS.size() + "\tsize");
                    break;

                case "ZERO_RESULTS":
                    Toast.makeText(MainActivity.this, "Sorry no places found. Try to change the types of places", Toast.LENGTH_LONG).show();// "OK", null, R.drawable.ic_my_location_black_24dp);
                    break;

                case "OVER_QUERY_LIMIT":
                    Toast.makeText(MainActivity.this, "Sorry query limit to google places is reached", Toast.LENGTH_LONG).show();// "OK", null, R.drawable.ic_my_location_black_24dp);
                    break;

                case "REQUEST_DENIED":
                    Toast.makeText(MainActivity.this, "Sorry error occured. Request is denied", Toast.LENGTH_LONG).show();// "OK", null, R.drawable.ic_my_location_black_24dp);
                    break;

                case "UNKNOWN_ERROR":
                    Toast.makeText(MainActivity.this, "Sorry error occured. Unknown Error", Toast.LENGTH_LONG).show();
                    break;

                case "INVALID_REQUEST":
                    Toast.makeText(MainActivity.this, "Sorry error occured. Invalid Request", Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }
    }

    class MyErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }


}