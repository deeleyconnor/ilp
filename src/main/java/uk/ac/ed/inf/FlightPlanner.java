package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create flight plans.
 */
public class FlightPlanner {

    public  final String RETURN_TO_APPLETON_ORDER_NO = "0GOHOME0";
    private final LongLat APPLETON_TOWER = new LongLat(-3.186874, 55.944494);
    private final int MAXIMUM_DRONE_MOVES = 1500;
    private final String LANDMARKS_FILE_LOCATION = "buildings/landmarks.geojson";
    private final String NO_FLY_ZONES_FILE_LOCATION = "buildings/no-fly-zones.geojson";
    private final int SINGLE_PICKUP_POINT = 1;

    private final ArrayList<LongLat> landmarks = new ArrayList<>();
    private final FeatureCollection noFlyZones;

    /**
     * This creates an instance of flight planner. It retrieves the landmarks and the no fly zones from the webserver
     * and saves them. The landmarks are converted to LongLat positions for easier use later.
     */
    public FlightPlanner() {
        FeatureCollection landmarksFeature = getGeoJsonData(LANDMARKS_FILE_LOCATION);

        for (Feature landmark: landmarksFeature.features()) {
            Point landmarkPoint = (Point)landmark.geometry();

            landmarks.add(new LongLat(landmarkPoint.longitude(), landmarkPoint.latitude()));
        }

        noFlyZones = getGeoJsonData(NO_FLY_ZONES_FILE_LOCATION);
    }

    /**
     * This method gets a GeoJson file from the web server and stores it as a feature collection.
     *
     * @param fileLocation The file location of the geojson file.
     * @return A GeoJson feature collection.
     */
    private FeatureCollection getGeoJsonData(String fileLocation) {
        String responseBody = WebServerClient.request(fileLocation);

        return FeatureCollection.fromJson(responseBody);
    }

    /**
     * This method takes a list of orders and gets a flight plan for each then converts them into a single flight plan
     * including as many of the orders as possible while being less drone moves than MAXIMUM_DRONE_MOVES.
     *
     * @param orders The list of orders for the day.
     * @return A combined flight plan of the orders.
     */
    public FlightPlan dayFlightPlanner(ArrayList<Order> orders) {
        orders.forEach(this::orderFlightPlanner);

        FlightPlan flightPlan = new FlightPlan();

        FlightPlan returnFlightPlan = new FlightPlan();

        LongLat currentPos = APPLETON_TOWER;
        int movesAvailable = MAXIMUM_DRONE_MOVES;

        boolean flightPlanComplete = false;

        while (!flightPlanComplete) {
            FlightPlan optimalOrderFlightPlanToStart = new FlightPlan();
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
                            optimalOrderFlightPlanToStart = currentOrderStartFlightPlan;
                        }
                    }
                }
            }

            if (optimalOrder != null) {
                ArrayList<DroneMove> optimalFlightPlan = optimalOrder.getOrderFlightPlan().getPlan();
                flightPlan.getPlan().addAll(optimalOrderFlightPlanToStart.getPlan());
                flightPlan.getPlan().addAll(optimalFlightPlan);
                optimalOrder.complete();
                movesAvailable -= optimalOrderFlightPlanToStart.size() + optimalOrder.getOrderFlightPlanMoveCount();
                currentPos = optimalFlightPlan.get(optimalFlightPlan.size() - 1).toLongLat;
                returnFlightPlan = optimalOrder.getReturnFlightPlan();
            }
            else {
                flightPlan.getPlan().addAll(returnFlightPlan.getPlan());
                flightPlanComplete = true;
            }
        }

        return flightPlan;
    }

    /**
     * This method creates a flight path and a return flight plan for a single order.
     *
     * @param order The order that the flight plan is being created for.
     */
    private void orderFlightPlanner(Order order) {
        ArrayList<Point> orderFlightPlan;

        LongLat deliveryLocation = order.getDeliveryLocation();
        ArrayList<LongLat> pickupLocations = order.getPickupLocations();


        if (pickupLocations.size() == SINGLE_PICKUP_POINT) {
            orderFlightPlan = singleOrderFlightPlan(deliveryLocation, pickupLocations);
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

    /**
     * This method creates a estimated flight plan for a single pickup order.
     *
     * @param pickupLocations The pickup location required for the order.
     * @param deliveryLocation The delivery location of the order.
     * @return A estimated flight plan for the single pickup order.
     */
    private ArrayList<Point> singleOrderFlightPlan(LongLat deliveryLocation, ArrayList<LongLat> pickupLocations) {
        ArrayList<Point> orderFlightPlan;
        orderFlightPlan = twoPointsFlightPlanner(pickupLocations.get(0), deliveryLocation);
        orderFlightPlan.add(deliveryLocation.toPoint());
        return orderFlightPlan;
    }

    /**
     * This method creates an estimated flight plan between two points. If a direct path is not possible then it
     * attempts to use one of the landmarks to avoid the no fly zones.
     *
     * @param startLocation The start location of the estimated flight plan.
     * @param finishLocation The finish location of the estimated flight plan.
     * @return An estimated flight plan between the two points.
     */
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

    /**
     * This method is used for getting the best flight plan for a double pickup order. It creates two flight plans with
     * the pickup locations swapped around.
     *
     * @param pickupLocations The pickup locations required for the order.
     * @param deliveryLocation The delivery location of the order.
     * @return A estimated flight plan for the double pickup order
     */
    private ArrayList<Point> doublePickupOrderFlightPlanner(ArrayList<LongLat> pickupLocations, LongLat deliveryLocation) {


        ArrayList<Point> orderFlightPlan1 = getDoublePickupOrderPlan(pickupLocations, deliveryLocation, 0, 1);
        ArrayList<Point> orderFlightPlan2 = getDoublePickupOrderPlan(pickupLocations, deliveryLocation, 1, 0);

        if (getFlightPlanEstimateDistance(orderFlightPlan1) < getFlightPlanEstimateDistance(orderFlightPlan2)) {
            return orderFlightPlan1;
        }
        else {
            return orderFlightPlan2;
        }
    }

    /**
     * This method creates a estimated flight plan for a double pickup order.
     *
     * @param pickupLocations The pickup locations required for the order.
     * @param deliveryLocation The delivery location of the order.
     * @param firstPickupIndex The index of the first pickup location.
     * @param secondPickupIndex The index of the second pickup location.
     * @return A estimated flight plan for the double pickup order.
     */
    private ArrayList<Point> getDoublePickupOrderPlan(ArrayList<LongLat> pickupLocations, LongLat deliveryLocation, int firstPickupIndex, int secondPickupIndex) {
        ArrayList<Point> orderFlightPlan = new ArrayList<>();
        orderFlightPlan.addAll(twoPointsFlightPlanner(pickupLocations.get(firstPickupIndex), pickupLocations.get(secondPickupIndex)));
        orderFlightPlan.addAll(twoPointsFlightPlanner(pickupLocations.get(secondPickupIndex), deliveryLocation));
        orderFlightPlan.add(deliveryLocation.toPoint());
        return orderFlightPlan;
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
        LongLat nextPosition;

        for (int i = 1; i < path.size(); i++) {
            currentPosition =  new LongLat(path.get(i-1));
            nextPosition = new LongLat(path.get(i));

            distance += currentPosition.distanceTo(nextPosition);
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