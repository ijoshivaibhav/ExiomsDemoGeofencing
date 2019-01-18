package example.vaibhav.com.exiomsvaibhavjoshi;

import java.util.ArrayList;

public class Place {
    public String name;
    public String vicinity;
    public float rating;
    public String place_id;


    public Geometry geometry;


    public String toString() {
        return name + "\n" + vicinity +
                "\n" +  geometry.location.lat +  ", " + geometry.location.lng +
                "\n" + place_id +
                "\n" + rating +
                "\n\n\n";
    }
}
