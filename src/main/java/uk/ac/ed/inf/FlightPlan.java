package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import com.mapbox.geojson.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FlightPlan {

    private ArrayList<DroneMove> flightPlan;

    public FlightPlan(ArrayList<DroneMove> flightPlan) {
        this.flightPlan = flightPlan;
    }

    public void toGeoJson(){
        ArrayList<Point> flightPlanPoints = new ArrayList<>();
        flightPlanPoints.add(flightPlan.get(0).fromLongLat.toPoint());

        for (DroneMove move : flightPlan) {
            flightPlanPoints.add(move.toLongLat.toPoint());
        }

        LineString flightPlanLineString = LineString.fromLngLats(flightPlanPoints);

        Feature flightPlanFeature = Feature.fromGeometry((Geometry) flightPlanLineString);

        FeatureCollection flightPlanFeatureCollection = FeatureCollection.fromFeature(flightPlanFeature);

        try {
            FileWriter myWriter = new FileWriter("test.geojson");
            myWriter.write(flightPlanFeatureCollection.toJson());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}
