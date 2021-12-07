package uk.ac.ed.inf;

import com.google.gson.Gson;

import java.util.Hashtable;

/**
 *
 */
public class LocationFinder {
    private final static String WHAT_3_WORDS_FILE_LOCATION = "words/%s/details.json";

    private final String machineName;
    private final String port;

    private Hashtable<String, LongLat> locations;

    public LocationFinder(String machineName, String port) {
        this.machineName = machineName;
        this.port = port;

        locations = new Hashtable<>();
    }

    public LongLat findLocation(String words) {
        LongLat location = locations.get(words);

        if (location == null) {
            location = findNewLocation(words);
            locations.put(words, location);
        }

        return location;
    }

    private LongLat findNewLocation(String words) {
        String fileLocation = getWordsFileLocation(words);
        String responseBody = WebServerClient.request(machineName, port, fileLocation);
        WhatThreeWords whatThreeWords = new Gson().fromJson(responseBody, WhatThreeWords.class);
        LongLat location = whatThreeWords.toLongLat();

        return location;
    }

    /**
     * This method turns the What3Words address into a file location in webserver which can be used to retrieve the
     * location data.
     *
     * @param words The What3Words address.
     * @return The file location
     */
    private String getWordsFileLocation(String words) {
        return String.format(WHAT_3_WORDS_FILE_LOCATION, words.replace(".","/"));
    }
}