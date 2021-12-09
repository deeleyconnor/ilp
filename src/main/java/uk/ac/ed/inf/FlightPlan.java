package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import com.mapbox.geojson.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 */
public class FlightPlan {

    private ArrayList<DroneMove> plan;

    public FlightPlan() {
        this.plan = new ArrayList<>();
    }

    public FlightPlan(ArrayList<DroneMove> flightPlan) {
        this.plan = flightPlan;
    }

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
            }
        }
    }

    public int size(){
        return this.plan.size();
    }

    public ArrayList<DroneMove> getPlan() {
        return this.plan;
    }

    public double getPlanDistance() {
        double distance = 0.0;

        LongLat currentPosition;
        LongLat nextPostion;

        for (int i = 1; i < plan.size(); i++) {
            currentPosition =  plan.get(i - 1).fromLongLat;
            nextPostion = plan.get(i).fromLongLat;

            distance += currentPosition.distanceTo(nextPostion);
        }

        distance += plan.get(plan.size()).fromLongLat.distanceTo(plan.get(plan.size()).toLongLat);

        return distance;
    }

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
