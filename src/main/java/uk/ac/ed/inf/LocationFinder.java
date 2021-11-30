package uk.ac.ed.inf;

import com.google.gson.Gson;
import uk.ac.ed.inf.JsonTemplates.Words;

import java.util.Hashtable;

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

    public LongLat findLocation(String words) {

        LongLat location = locations.get(words);

        if (location == null) {
            String urlString = String.format("http://%s:%s/%s", machineName, port, words.replaceAll(",","/"));
            String responseBody = WebServerClient.request(urlString);

            Words jsonWords = new Gson().fromJson(responseBody, Words.class);
            location = new LongLat(jsonWords.coordinates.lng, jsonWords.coordinates.lat);
            locations.put(words, location);
        }

        return location;
    }
}