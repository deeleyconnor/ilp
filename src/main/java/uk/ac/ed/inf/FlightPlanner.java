package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanner {

    public  final String RETURN_TO_APPLETON_ORDER_NO = "0GOHOME0";
    private final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private final int MAXIMUM_DRONE_MOVES = 1500;
    private final String LANDMARKS_FILE_LOCATION = "buildings/landmarks.geojson";
    private final String NO_FLY_ZONES_FILE_LOCATION = "buildings/no-fly-zones.geojson";
    private final int SINGLE_PICKUP_POINT = 1;

    private ArrayList<LongLat> landmarks = new ArrayList<>();
    private FeatureCollection noFlyZones;

    public FlightPlanner() {
        FeatureCollection landmarksFeature = getGeoJsonData(LANDMARKS_FILE_LOCATION);

        for (Feature landmark: landmarksFeature.features()) {
            Point landmarkPoint = (Point)landmark.geometry();

            landmarks.add(new LongLat(landmarkPoint.longitude(), landmarkPoint.latitude()));
        }

        noFlyZones = getGeoJsonData(NO_FLY_ZONES_FILE_LOCATION);
    }

    private FeatureCollection getGeoJsonData(String fileLocation) {
        String responseBody = WebServerClient.request(fileLocation);

        FeatureCollection featureCollection = FeatureCollection.fromJson(responseBody);

        return featureCollection;
    }

    public FlightPlan dayFlightPlanner(ArrayList<Order> orders) {
        orders.forEach( (order) -> this.orderFlightPlanner(order));

        FlightPlan flightPlan = new FlightPlan();

        FlightPlan returnFlightPlan = new FlightPlan();

        LongLat currentPos = APPLETON_TOWER;
        int movesAvailable = MAXIMUM_DRONE_MOVES;

        boolean flightPlanComplete = false;

        while (!flightPlanComplete) {
            FlightPlan opitimalOrderFlightPlanToStart = new FlightPlan();
            double optimalOrderValue = Double.MIN_VALUE;
            Order optimalOrder = null;
            for (Order order : orders) {
                if (!order.completed())
                {
                    ArrayList<Point> currentOrderStartFlightPath = twoPointsFlightPlanner(currentPos, order.getStartLocation());
                    currentOrderStartFlightPath.add(order.getStartLocation().toPoint());
                    FlightPlan currentOrderStartFlightPlan = new FlightPlan(currentOrderStartFlightPath, order.getOrderNo());

                    int orderAndReturnMoveCount = currentOrderStartFlightPlan.size() + order.getOrderAndReturnFlightPlanMoveCount();

                    if (orderAndReturnMoveCount <= movesAvailable) {
                        double currentOrderValue = order.getOrderValue(currentOrderStartFlightPlan.size());

                        if (currentOrderValue > optimalOrderValue) {
                            optimalOrderValue = currentOrderValue;
                            optimalOrder = order;
                            opitimalOrderFlightPlanToStart = currentOrderStartFlightPlan;
                        }
                    }
                }
            }

            if (optimalOrder != null) {
                flightPlan.getPlan().addAll(opitimalOrderFlightPlanToStart.getPlan());
                flightPlan.getPlan().addAll(optimalOrder.getOrderFlightPlan().getPlan());
                optimalOrder.complete();
                movesAvailable -= opitimalOrderFlightPlanToStart.size() + optimalOrder.getOrderFlightPlanMoveCount();
                returnFlightPlan = optimalOrder.getReturnFlightPlan();
            }
            else {
                flightPlanComplete = true;
                flightPlan.getPlan().addAll(returnFlightPlan.getPlan());
            }
        }

        return flightPlan;
    }

    private void orderFlightPlanner(Order order) {
        ArrayList<Point> orderFlightPlan;

        LongLat deliveryLocation = order.getDeliveryLocation();
        ArrayList<LongLat> pickupLocations = order.getPickupLocations();


        if (pickupLocations.size() == SINGLE_PICKUP_POINT) {
            orderFlightPlan = twoPointsFlightPlanner(pickupLocations.get(0),deliveryLocation);
            orderFlightPlan.add(deliveryLocation.toPoint());
        }
        else {
            orderFlightPlan = doublePickupOrderFlightPlanner(pickupLocations, deliveryLocation);
        }

        LongLat lastPoint = new LongLat(orderFlightPlan.get(orderFlightPlan.size()-1));


        ArrayList<Point> orderReturnFlightPlan = twoPointsFlightPlanner(lastPoint,APPLETON_TOWER);
        orderReturnFlightPlan.add(APPLETON_TOWER.toPoint());

        order.setOrderFlightPlan(new FlightPlan(orderFlightPlan, order.getOrderNo()));

        order.setReturnFlightPlan(new FlightPlan(orderReturnFlightPlan, RETURN_TO_APPLETON_ORDER_NO));
    }

    private ArrayList<Point> twoPointsFlightPlanner(LongLat startLocation, LongLat finishLocation) {
        ArrayList<Point> flightPlan = new ArrayList<>();
        flightPlan.add(startLocation.toPoint());

        Line2D path = makeLineFromLongLat(startLocation, finishLocation);

        if (!avoidsNoFlyZones(path)) {
            double bestPathLength = Double.MAX_VALUE;
            Point bestPoint = null;

            for (LongLat landmark: landmarks) {
                Line2D pickupToLandmark = makeLineFromLongLat(startLocation, landmark);
                Line2D landmarkToDelivery = makeLineFromLongLat(landmark, finishLocation);

                if (avoidsNoFlyZones(pickupToLandmark) && avoidsNoFlyZones(landmarkToDelivery)) {
                    double pathLength = landmark.distanceTo(startLocation) + landmark.distanceTo(finishLocation);

                    if (pathLength < bestPathLength) {
                        bestPoint = landmark.toPoint();
                        bestPathLength = pathLength;
                    }
                }
            }

            flightPlan.add(bestPoint);
        }

        flightPlan.add(finishLocation.toPoint());

        return flightPlan;
    }

    private ArrayList<Point> doublePickupOrderFlightPlanner(ArrayList<LongLat> pickupLocation, LongLat deliveryLocation) {
        ArrayList<Point> orderFlightPlan1 = new ArrayList<>();
        ArrayList<Point> orderFlightPlan2 = new ArrayList<>();

        orderFlightPlan1.addAll(twoPointsFlightPlanner(pickupLocation.get(0),pickupLocation.get(1)));
        orderFlightPlan1.addAll(twoPointsFlightPlanner(pickupLocation.get(1),deliveryLocation));
        orderFlightPlan1.add(deliveryLocation.toPoint());

        orderFlightPlan2.addAll(twoPointsFlightPlanner(pickupLocation.get(1),pickupLocation.get(0)));
        orderFlightPlan2.addAll(twoPointsFlightPlanner(pickupLocation.get(0),deliveryLocation));
        orderFlightPlan2.add(deliveryLocation.toPoint());

        if (getFlightPlanEstimateDistance(orderFlightPlan1) < getFlightPlanEstimateDistance(orderFlightPlan2)) {
            return orderFlightPlan1;
        }
        else {
            return orderFlightPlan2;
        }
    }

    /**
     * This method converts a list of points that make a estimate flight plath into distance. Distance is measured as
     * the euclidean distance between from each point to the next point totaled together.
     *
     * @param path The estimated flight path.
     * @return The distance of the estimated flight path.
     */
    private double getFlightPlanEstimateDistance(ArrayList<Point> path) {
        double distance = 0.0;

        LongLat currentPosition;
        LongLat nextPostion;

        for (int i = 1; i < path.size(); i++) {
            currentPosition =  new LongLat(path.get(i-1));
            nextPostion = new LongLat(path.get(i));

            distance += currentPosition.distanceTo(nextPostion);
        }

        return distance;
    }

    /**
     * This method converts to two LongLat objects into a Line2D object.
     *
     * @param start The LongLat coordinates of where the line will start.
     * @param finish The LongLat coordinates of where the line will end.
     * @return A Line2D object originating at start and finishing at finish.
     */
    private Line2D makeLineFromLongLat(LongLat start, LongLat finish) {
        return new Line2D.Double(start.longitude, start.latitude, finish.longitude, finish.latitude);
    }

    /**
     * This method checks that a straight line between a position and a target position avoids the no fly
     * zones.
     *
     * @param path A straight line between a position and a target position.
     * @return True if path avoids the no fly zones otherwise returns False.
     */
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