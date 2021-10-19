package uk.ac.ed.inf;

enum MoveTypes {
    FLY,
    HOVER;
}

public class Drone {

    private final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private final int MAXIMUM_MOVES = 1500;

    private LongLat currentLocation;
    private int movesRemaining;

    public Drone() {
        currentLocation = APPLETON_TOWER;
        movesRemaining = MAXIMUM_MOVES;
    }
}
