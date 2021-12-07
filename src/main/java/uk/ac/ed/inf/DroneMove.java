package uk.ac.ed.inf;

/**
 * Represents a single made by the drone.
 */
public class DroneMove {
    public final String orderNo;
    public final LongLat fromLongLat;
    public final LongLat toLongLat;
    public final int angle;

    /**
     * Creates an instance of the drone move class.
     *
     * @param orderNo the order number for the lunch order which the drone is currently collecting and delivering.
     * @param fromLongLat the coordinates of the drone at the start of the move.
     * @param toLongLat the coordinates of the drone at the end of the move.
     * @param angle the angle of travel of the drone at the end of the move.
     */
    public DroneMove(String orderNo, LongLat fromLongLat, LongLat toLongLat, int angle) {
        this.orderNo = orderNo;
        this.fromLongLat = fromLongLat;
        this.toLongLat = toLongLat;
        this.angle = angle;
    }
}