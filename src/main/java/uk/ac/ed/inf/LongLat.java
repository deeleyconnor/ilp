package uk.ac.ed.inf;

import com.mapbox.geojson.Point;

import java.lang.Math;

/**
 * The values of the drone confinement area.
 */
enum DroneConfinementArea {
    MIN_LONGITUDE(-3.184319f),
    MAX_LONGITUDE(-3.192473f),
    MIN_LATITUDE(55.942617f),
    MAX_LATITUDE(55.946233f);

    public final double value;

    /**
     * Assign a value to each part of drone confinement area.
     *
     * @param value The value of the part of the drone confinement area.
     */
    DroneConfinementArea(double value) {
        this.value = value;
    }
}

/**
 * Represents a location with a longitude and latitude coordinate.
 */
public class LongLat {

    private final static double CLOSE_DISTANCE =  0.00015;
    public final static double MOVE_DISTANCE = 0.00015;
    private final static int HOVER_ANGLE = -999;

    public final double longitude;
    public final double latitude;

    /**
     * Creates an instance of the LongLat Class using longitude and latitude.
     *
     * @param longitude The longitude coordinate of the location. It is the measurement east or west of the prime
     *                  meridian.
     * @param latitude The latitude coordinate of the location. It is the measurement north or south of the equator.
     */
    public LongLat (double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Creates an instance of the LongLat Class using a point.
     *
     * @param point A point that has a longitude and latitude coordinate of the location.
     * @see Point
     */
    public LongLat (Point point) {
        this.longitude = point.longitude();
        this.latitude = point.latitude();
    }

    /**
     * This method checks whether this LongLat instance is within the drone confinement area.
     *
     * @return True if the location is within the drone confinement area. Otherwise returns false.
     * @see DroneConfinementArea
     */
    public boolean isConfined() {
        return (DroneConfinementArea.MIN_LONGITUDE.value > this.longitude)
                        && (DroneConfinementArea.MAX_LONGITUDE.value < this.longitude)
                        && (DroneConfinementArea.MIN_LATITUDE.value < this.latitude)
                        && (DroneConfinementArea.MAX_LATITUDE.value > this.latitude);
    }

    /**
     * This method calculates the distance between this LongLat instance and another given target LongLat instance. The
     * distance is calculated using Pythagorean distance.
     *
     * @param target The target LongLat instance that we want to calculate the distance from this LongLat instance.
     * @return The distance between this LongLat instance and the target LongLat instance
     */
    public double distanceTo(LongLat target) {
        double longitudeDiff = this.longitude - target.longitude;
        double latitudeDiff = this.latitude - target.latitude;

        return Math.sqrt(Math.pow(longitudeDiff, 2) + Math.pow(latitudeDiff, 2));
    }

    /**
     * This method calculates if two points are considered close (Within the CLOSE_DISTANCE). This is required due to
     * in general it not being possible to manoeuvre the drone to a specified location exactly.
     *
     * @param target The target location we want to know if this LongLat instance is close to.
     * @return True if our location is close to the target location. Otherwise returns false.
     */
    public boolean closeTo(LongLat target) {
        double distance = distanceTo(target);

        return distance < CLOSE_DISTANCE;
    }

    /**
     * This method calculates the angle from this location to the target to the nearest 10.
     *
     * @param target The target location that we want to know the angle to.
     * @return An angle in degrees between 0 and 350 which is a multiple of 10. If target position is the same as this
     *         location then the hover angle will be returned.
     */
    public int angleTo(LongLat target) {
        double longitudeDiff = target.longitude - this.longitude;
        double latitudeDiff = target.latitude - this.latitude;

        if (longitudeDiff == 0 && latitudeDiff == 0) {
            return HOVER_ANGLE;
        }

        int angle = (int) (Math.round(Math.toDegrees(Math.atan2(latitudeDiff,longitudeDiff)) / 10) * 10);

        if (angle < 0) {
            angle += 360;
        }
        else if (angle == 360) {
            angle = 0;
        }

        return angle;
    }

    /**
     * This method calculates the location reached by the drone in a move from the current location given an angle.
     *
     * @param angle The angle the drone is facing.
     * @return The new location reached after a move. If the drone angle is the HOVER_ANGLE then the LongLat will just
     *         be the current LongLat.
     */
    public LongLat nextPosition(int angle) {
        if (HOVER_ANGLE == angle) { return this; }

        double longitudeMovement = Math.cos(Math.toRadians(angle)) * MOVE_DISTANCE;
        double latitudeMovement = Math.sin(Math.toRadians(angle)) * MOVE_DISTANCE;

        double newLongitude = this.longitude + longitudeMovement;
        double newLatitude = this.latitude + latitudeMovement;

        return new LongLat(newLongitude,newLatitude);
    }

    /**
     * This method gives the point equivalent of this LongLat object.
     *
     * @return A new Point with the same longitude and latitude as this LongLat object.
     * @see Point
     */
    public Point toPoint() {
        return Point.fromLngLat(this.longitude, this.latitude);
    }
}