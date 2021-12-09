package uk.ac.ed.inf;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * This class is used to retrieve and store the LongLat positions of WhatThreeWord Addresses.
 */
public class LocationFinder {
    private final static String WHAT_3_WORDS_FILE_LOCATION = "words/%s/details.json";

    private final static HashMap<String, LongLat> locations = new HashMap<>();

    /**
     * This method is used to get the LongLat coordinates of a WhatThreeWords address. It first searches a HashMap of
     * already retrieved locations if it is not contained there then it will search the webserver and put it in the
     * HashMap to be retrieved again later if required.
     *
     * @param words The WhatThreeWords address that we want to know the LongLat coordinates.
     * @return The LongLat coordinates of the WhatThreeWords address.
     */
    public static LongLat findLocation(String words) {
        LongLat location = locations.get(words);

        if (location == null) {
            location = findNewLocation(words);
            locations.put(words, location);
        }

        return location;
    }

    /**
     * This method is used to search the webserver for a WhatThreeWords address that is not currently contained in the
     * HashMap of locations known.
     *
     * @param words The WhatThreeWords address that we want to know the LongLat coordinates.
     * @return The LongLat coordinates of the WhatThreeWords address.
     * @see WebServerClient
     */
    private static LongLat findNewLocation(String words) {
        String fileLocation = getWordsFileLocation(words);
        String responseBody = WebServerClient.request(fileLocation);
        WhatThreeWords whatThreeWords = new Gson().fromJson(responseBody, WhatThreeWords.class);

        return whatThreeWords.toLongLat();
    }

    /**
     * This method turns the What3Words address into a file location in webserver which can be used to retrieve the
     * location data.
     *
     * @param words The What3Words address.
     * @return The file location
     */
    private static String getWordsFileLocation(String words) {
        return String.format(WHAT_3_WORDS_FILE_LOCATION, words.replace(".","/"));
    }
}