package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;


public class FlightPlanner {

    public static final String RETURN_TO_APPLETON_ORDER_NO = "0GOHOME0";
    private final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private final int MAXIMUM_DRONE_MOVES = 1500;
    private final static String LANDMARKS_FILE_LOCATION = "buildings/landmarks.geojson";
    private final static String NO_FLY_ZONES_FILE_LOCATION = "buildings/no-fly-zones.geojson";
    private final static int SINGLE_PICKUP_POINT = 1;

    private ArrayList<LongLat> landmarks = new ArrayList<>();
    private FeatureCollection noFlyZones;

    public FlightPlanner(String machineName, String port) {
        FeatureCollection landmarksFeature = getGeoJsonData(machineName, port, LANDMARKS_FILE_LOCATION);

        for (Feature landmark: landmarksFeature.features()) {
            Point landmarkPoint = (Point)landmark.geometry();

            landmarks.add(new LongLat(landmarkPoint.longitude(), landmarkPoint.latitude()));
        }

        noFlyZones = getGeoJsonData(machineName, port, NO_FLY_ZONES_FILE_LOCATION);
    }

    private FeatureCollection getGeoJsonData(String machineName, String port, String fileLocation) {
        String urlString = WebServerClient.getUrlString(machineName, port, fileLocation);
        String responseBody = WebServerClient.request(urlString);

        FeatureCollection featureCollection = FeatureCollection.fromJson(responseBody);

        return featureCollection;
    }

    public ArrayList<DroneMove> dayFlightPlanner(ArrayList<Order> orders) {
        orders.forEach( (order) -> this.orderFlightPlanner(order));

        ArrayList<DroneMove> flightPlan = new ArrayList<>();

        ArrayList<DroneMove> returnFlightPlan = new ArrayList<>();

        LongLat currentPos = APPLETON_TOWER;
        int movesAvailable = MAXIMUM_DRONE_MOVES;

        boolean flightPlanComplete = false;

        while (!flightPlanComplete) {
            ArrayList<DroneMove> opitimalOrderFlightPlanToStart = new ArrayList<>();
            double optimalOrderValue = Double.MIN_VALUE;
            Order optimalOrder = null;
            for (Order order : orders) {
                if (!order.completed())
                {
                    ArrayList<Point> currentOrderStartFlightPath = twoPointsFlightPlanner(currentPos, order.getStartLocation());
                    currentOrderStartFlightPath.add(order.getStartLocation().toPoint());
                    ArrayList<DroneMove> currentOrderStartFlightPlan = (convertPointsToFlightPlan(currentOrderStartFlightPath, order.getOrderNo()));

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
                flightPlan.addAll(opitimalOrderFlightPlanToStart);
                flightPlan.addAll(optimalOrder.getOrderFlightPlan());
                optimalOrder.complete();
                movesAvailable -= opitimalOrderFlightPlanToStart.size() + optimalOrder.getOrderFlightPlanMoveCount();
                returnFlightPlan = optimalOrder.getReturnFlightPlan();
            }
            else {
                flightPlanComplete = true;
                flightPlan.addAll(returnFlightPlan);
            }
        }

        System.out.println();

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

        order.setOrderFlightPlan(convertPointsToFlightPlan(orderFlightPlan, order.getOrderNo()));

        order.setReturnFlightPlan(convertPointsToFlightPlan(orderReturnFlightPlan, RETURN_TO_APPLETON_ORDER_NO));
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

        if (getFlightPlanDistance(orderFlightPlan1) < getFlightPlanDistance(orderFlightPlan2)) {
            return orderFlightPlan1;
        }
        else {
            return orderFlightPlan2;
        }
    }

    private double getFlightPlanDistance(ArrayList<Point> path) {
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

    private Line2D makeLineFromLongLat(LongLat start, LongLat finish) {
        return new Line2D.Double(start.longitude, start.latitude, finish.longitude, finish.latitude);
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

    private ArrayList<DroneMove> convertPointsToFlightPlan(ArrayList<Point> flightPlanPoints, String orderNo) {
        ArrayList<DroneMove> flightPlan = new ArrayList<>();

        LongLat currentPosition = new LongLat(flightPlanPoints.get(0));
        LongLat targetPosition;
        LongLat nextPostion;
        int targetPositionNumber = 1;

        while (targetPositionNumber < (flightPlanPoints.size())) {
            targetPosition = new LongLat(flightPlanPoints.get(targetPositionNumber));

            int angleToTarget = currentPosition.angleTo(targetPosition);
            nextPostion = currentPosition.nextPosition(angleToTarget);

            flightPlan.add(new DroneMove(orderNo, currentPosition, nextPostion, angleToTarget));

            currentPosition = currentPosition.nextPosition(angleToTarget);

            if (currentPosition.closeTo(targetPosition)) {
                targetPositionNumber++;
            }
        }

        return flightPlan;
    }
}
