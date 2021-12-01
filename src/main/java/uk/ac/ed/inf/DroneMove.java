package uk.ac.ed.inf;

public class DroneMove {
    public final String orderNo;
    public final LongLat fromLongLat;
    public final LongLat toLongLat;
    public final int angle;

    public DroneMove(String orderNo, LongLat fromLongLat, LongLat toLongLat, int angle) {
        this.orderNo = orderNo;
        this.fromLongLat = fromLongLat;
        this.toLongLat = toLongLat;
        this.angle = angle;
    }

}
