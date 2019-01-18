package example.vaibhav.com.exiomsvaibhavjoshi;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class Constants {

    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 12 * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 2000;


    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();

//    public static void populateLandMarks(ArrayList<Place> placesList) {
//        for (Place p : placesList) {
//            LANDMARKS.put(p.name, new LatLng(p.geometry.location.lat, p.geometry.location.lng));
//        }
//    }
    static {
        // San Francisco International Airport.
            LANDMARKS.put("Moscone South", new LatLng(18.498406, 73.839033));

        // Googleplex.
//        LANDMARKS.put("Japantown", new LatLng(18.497388, 73.839560));

        // Test
//        LANDMARKS.put("SFO", new LatLng(37.621313,-122.378955));
    }
}
