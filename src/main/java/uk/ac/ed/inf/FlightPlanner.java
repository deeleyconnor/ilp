package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;


public class FlightPlanner {

    private final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private final int MAXIMUM_MOVES = 1500;
    private final static String LANDMARKS_FILE_LOCATION = "buildings/landmarks.geojson";
    private final static String NO_FLY_ZONES_FILE_LOCATION = "buildings/no-fly-zones.geojson";
    private final static int SINGLE_PICKUP_POINT = 1;

    private FeatureCollection landmarks;
    private FeatureCollection noFlyZones;

    public FlightPlanner(String machineName, String port) {
        landmarks = getGeoJsonData(machineName, port, LANDMARKS_FILE_LOCATION);
        noFlyZones = getGeoJsonData(machineName, port, NO_FLY_ZONES_FILE_LOCATION);
    }

    private FeatureCollection getGeoJsonData(String machineName, String port, String fileLocation) {
        String urlString = String.format("http://%s:%s/%s", machineName, port, fileLocation);
        String responseBody = WebServerClient.request(urlString);

        FeatureCollection featureCollection = FeatureCollection.fromJson(responseBody);

        return featureCollection;
    }

    public void orderFlightPlanner(Order order) {
        ArrayList<Point> orderFlightPlan;

        LongLat deliveryLocation = order.getDeliveryLocation();
        ArrayList<LongLat> pickupLocations = order.getPickupLocations();


        if (pickupLocations.size() == SINGLE_PICKUP_POINT) {
            orderFlightPlan = twoPointsFlightPlanner(pickupLocations.get(0),deliveryLocation);
        }
        else {
            orderFlightPlan = doublePickupOrderFlightPlanner();
        }

        orderFlightPlan.add(Point.fromLngLat(deliveryLocation.longitude, deliveryLocation.latitude));


        order.setOrderFlightPlan(orderFlightPlan);
    }

    private ArrayList<Point> twoPointsFlightPlanner(LongLat pickupLocation, LongLat deliveryLocation) {
        ArrayList<Point> flightPlan = new ArrayList<>();
        flightPlan.add(Point.fromLngLat(pickupLocation.longitude, pickupLocation.latitude));

        Line2D path = new Line2D.Double(pickupLocation.longitude, pickupLocation.latitude, deliveryLocation.longitude, deliveryLocation.latitude);

        if (!avoidsNoFlyZones(path)) {
            System.out.println("Fuck");
        }

        return flightPlan;
    }

    private ArrayList<Point> doublePickupOrderFlightPlanner() {
        ArrayList<Point> orderFlightPlan = new ArrayList<>();



        return orderFlightPlan;
    }



    private boolean avoidsNoFlyZones(Line2D path) {
        boolean intersect;

        for (Feature building: noFlyZones.features()) {
            List<Point> buildingPoints = ((Polygon) building.geometry()).coordinates().get(0);

            for (int i = 0; i < buildingPoints.size() - 1; i++) {
                intersect = path.intersectsLine(buildingPoints.get(i).longitude(), buildingPoints.get(i).latitude(), buildingPoints.get(i+1).longitude(), buildingPoints.get(i+1).latitude());
                if (intersect) {
                    return false;
                }
            }

            intersect = path.intersectsLine(buildingPoints.get(buildingPoints.size() - 1).longitude(), buildingPoints.get(buildingPoints.size() - 1).latitude(), buildingPoints.get(0).longitude(), buildingPoints.get(0).latitude());
            if (intersect) {
                return false;
            }
        }

        return true;
    }
}
