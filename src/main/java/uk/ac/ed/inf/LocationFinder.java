package uk.ac.ed.inf;

import com.google.gson.Gson;

import javax.xml.stream.Location;
import java.util.Hashtable;
import java.util.Map;

public class LocationFinder {
    private final static String WORDS_FILE_LOCATION = "words/%s/details.json";

    private String machineName;
    private String port;

    Hashtable<String, LongLat> locations;

    public LocationFinder(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;

        locations = new Hashtable<String, LongLat>();
    }

    public LongLat getLocation(String locationWords) {

        LongLat location = locations.get(locationWords);

        if (location == null) {
            String urlString = String.format("http://%s:%s/%s", machineName, port, locationWords.replaceAll(",","/"));
            String responseBody = WebServerClient.request(urlString);

            Words words = new Gson().fromJson(responseBody, Words.class);
            location = new LongLat(words.coordinates.lng, words.coordinates.lat);
            locations.put(locationWords, location);
        }

        return location;
    }
}
