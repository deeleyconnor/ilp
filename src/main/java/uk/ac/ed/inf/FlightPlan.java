package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import com.mapbox.geojson.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents a flight plan which is a collection of drone moves.
 */
public class FlightPlan {

    private ArrayList<DroneMove> plan;

    /**
     * This creates an instance of a flight plan with a blank plan.
     */
    public FlightPlan() {
        this.plan = new ArrayList<>();
    }

    public FlightPlan(ArrayList<DroneMove> flightPlan) {
        this.plan = flightPlan;
    }

    /**
     * This creates an instance of a flight plan for an order by converting a a list of points into drone moves.
     *
     * @param flightPlanPoints The list of points in the flight plan.
     * @param orderNo The order no of the flight plan.
     */
    public FlightPlan(ArrayList<Point> flightPlanPoints, String orderNo) {
        this.plan = new ArrayList<>();

        LongLat currentPosition = new LongLat(flightPlanPoints.get(0));
        LongLat targetPosition;
        LongLat nextPostion;
        int targetPositionNumber = 1;

        while (targetPositionNumber < (flightPlanPoints.size())) {
            targetPosition = new LongLat(flightPlanPoints.get(targetPositionNumber));

            int angleToTarget = currentPosition.angleTo(targetPosition);
            nextPostion = currentPosition.nextPosition(angleToTarget);

            plan.add(new DroneMove(orderNo, currentPosition, nextPostion, angleToTarget));

            currentPosition = currentPosition.nextPosition(angleToTarget);

            if (currentPosition.closeTo(targetPosition)) {
                targetPositionNumber++;

                // Adds hover move if required.
                if (targetPositionNumber < (flightPlanPoints.size())) {
                    if (isHovering(targetPosition, new LongLat(flightPlanPoints.get(targetPositionNumber)))) {
                        plan.add(new DroneMove(orderNo, nextPostion, nextPostion, LongLat.HOVER_ANGLE));
                    }
                }
            }
        }
    }

    /**
     * This method returns whether a hover move is required, meaning that the current target position is the same as
     * the next target position.
     *
     * @param targetPosition The target position that the drone has reached in the flight plan.
     * @param nextTargetPosition The next target position that the drone has reached in the flight plan.
     * @return True if hover is required otherwise false.
     */
    private boolean isHovering(LongLat targetPosition, LongLat nextTargetPosition) {
        if (targetPosition.closeTo(nextTargetPosition)) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This method is used to get this flight plans total number of drone moves.
     *
     * @return The number of drone moves that the flight plan contains
     */
    public int size(){
        return this.plan.size();
    }

    /**
     * This method used tp get this flight plans list of drone moves.
     *
     * @return the list of drone moves in the flight plan.
     */
    public ArrayList<DroneMove> getPlan() {
        return this.plan;
    }

    /**
     * This method converts the flight plan into a geojson then saves it in the current directory to a  file called
     * "drone-day-month-year.geojson".
     *
     * @param day The day of the flight.
     * @param month The month of the flight.
     * @param year The year of the flight.
     */
    public void toGeoJson(String day, String month, String year){
        ArrayList<Point> flightPlanPoints = new ArrayList<>();
        flightPlanPoints.add(plan.get(0).fromLongLat.toPoint());

        for (DroneMove move : plan) {
            flightPlanPoints.add(move.toLongLat.toPoint());
        }

        LineString flightPlanLineString = LineString.fromLngLats(flightPlanPoints);

        Feature flightPlanFeature = Feature.fromGeometry((Geometry) flightPlanLineString);

        FeatureCollection flightPlanFeatureCollection = FeatureCollection.fromFeature(flightPlanFeature);

        try {
            String fileName = String.format("drone-%s-%s-%s.geojson", day,month,year);
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(flightPlanFeatureCollection.toJson());
            myWriter.close();
            System.out.println(String.format("Successfully wrote to the file \"%s\".", fileName));
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
