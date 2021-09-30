package uk.ac.ed.inf;

import java.lang.Math;

enum DroneConfinementArea {
    MIN_LONGITUDE(-3.184319f),
    MAX_LONGITUDE(-3.192473f),
    MIN_LATITUDE(55.942617f),
    MAX_LATITUDE(55.946233f);

    public final double value;

    private DroneConfinementArea(double value) {
        this.value = value;
    }
}

public class LongLat {

    public double longitude;
    public double latitude;

    public LongLat (double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isConfined() {
        boolean confined = (DroneConfinementArea.MIN_LONGITUDE.value < this.longitude)
                        && (DroneConfinementArea.MAX_LONGITUDE.value > this.longitude)
                        && (DroneConfinementArea.MIN_LATITUDE.value < this.latitude)
                        && (DroneConfinementArea.MAX_LATITUDE.value > this.latitude);

        return confined;
    }

    public double distanceTo(LongLat target) {
        double longitudeDiff = this.longitude - target.longitude;
        double latitudeDiff = this.latitude - target.latitude;

        double distance = Math.sqrt(Math.pow(longitudeDiff, 2) + Math.pow(latitudeDiff, 2));

        return distance;
    }
}
