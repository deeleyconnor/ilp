package uk.ac.ed.inf;

/**
 * This class represents a WhatThreeWords address. This class is used for parsing any of the JSON files details.json in
 * the words folder. Contains the coordinates that the WhatThreeWords address represents.
 */
public class WhatThreeWords {
    private Location coordinates;

    /**
     * Represents a location with a longitude (lng) and latitude (lat) coordinate. Required for parsing a JSON file of
     * the words folder.
     */
    public static class Location {
        private double lng;
        private double lat;
    }

    /**
     * This method converts the WhatThreeWords address coordinates into a LongLat object.
     *
     * @return A LongLat object of the location of the WhatThreeWords address.
     */
    public LongLat toLongLat() {
        return new LongLat(this.coordinates.lng, this.coordinates.lat);
    }
}