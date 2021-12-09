package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a lunch order.
 */
public class Order {
    private final String orderNo;
    private final ArrayList<String> items;
    private final String deliverTo;

    private LongLat deliveryLocation;
    private ArrayList<LongLat> pickupLocations = new ArrayList<>();
    private int orderPrice;

    private FlightPlan orderFlightPlan;
    private FlightPlan returnFlightPlan;

    private boolean completed = false;

    /**
     * Creates an instance of Order. Note these fields are from the database.
     *
     * @param orderNo The order number of the number.
     * @param items The items that are in the order.
     * @param deliverTo The WhatThreeWords address of the delivery location.
     */
    public Order(String orderNo, ArrayList<String> items, String deliverTo) {
        this.orderNo = orderNo;
        this.items = items;
        this.deliverTo = deliverTo;
    }

    /**
     * This method populates the field where the data was retrieved from the webserver.
     *
     * @param menus the menus that are available to the delivery service.
     */
    public void setOrderObjectives(Menus menus) {
        setDeliveryLocation();
        setPickupLocations(menus);
        setOrderPrice(menus);
    }

    /**
     * This method sets the flight plan required to complete the order.
     *
     * @param orderFlightPlan The flight plan required to complete the order.
     */
    public void setOrderFlightPlan(FlightPlan orderFlightPlan) {
        this.orderFlightPlan = orderFlightPlan;
    }

    /**
     * This method sets the flight plan to return to Appleton tower once the order is complete.
     *
     * @param returnFlightPlan The flight plan required to return to Appleton tower after completion of the flight plan.
     */
    public void setReturnFlightPlan(FlightPlan returnFlightPlan) {
        this.returnFlightPlan = returnFlightPlan;
    }

    /**
     * This method gets the LongLat coordinates of WhatThreeWords address of the delivery location and stores it.
     */
    private void setDeliveryLocation() {
        this.deliveryLocation = LocationFinder.findLocation(deliverTo);
    }

    /**
     * This method gets the LongLat coordinates of WhatThreeWords addresses of the pickup locations and stores them.
     */
    private void setPickupLocations(Menus menus) {
        HashSet<String> pickupLocationsWords = menus.getPickupLocations(items);

        for (String words : pickupLocationsWords) {
            pickupLocations.add(LocationFinder.findLocation(words));
        }
    }

    /**
     * This method is used to convert the list of item names in the order into a total price for the order.
     *
     * @param menus the menus that are available to the delivery service.
     */
    private void setOrderPrice(Menus menus) {
        this.orderPrice = menus.getDeliveryCost(items);
    }

    /**
     * This method is used to get the number of drone moves in the flight plan.
     *
     * @return The number of drone moves to complete the order.
     */
    public int getOrderFlightPlanMoveCount() {
        return  this.orderFlightPlan.size();
    }

    /**
     * This method is used to get the order flight plan.
     *
     * @return The flight plan to complete the order.
     */
    public FlightPlan getOrderFlightPlan() {
       return this.orderFlightPlan;
    }

    /**
     * This method is used to get the number of drone moves in the flight plan to complete the order as well as the
     * number of moves to return to Appleton tower.
     *
     * @return The number of drone moves to complete the order and return to Appleton tower.
     */
    public int getOrderAndReturnFlightPlanMoveCount() {
        return  this.orderFlightPlan.size() + this.returnFlightPlan.size();
    }

    /**
     * This method returns the flight plan from the delivery location of this order back to the Appleton Tower.
     *
     * @return The flightPlan from the delivery location back to Appleton Tower.
     */
    public FlightPlan getReturnFlightPlan() {
        return this.returnFlightPlan;
    }

    /**
     * This method returns the WhatThreeWords address of the delivery location of this order.
     *
     * @return The WhatThreeWords address of the delivery location.
     */
    public String getDeliverTo() {
        return this.deliverTo;
    }

    /**
     * This method is used to get the starting position of flight plan used to complete this order.
     *
     * @return The LongLat coordinates of the first position in the order flight plan.
     */
    public LongLat getStartLocation() {
        return orderFlightPlan.getPlan().get(0).fromLongLat;
    }

    /**
     * This method is used to assign an estimated added value for completing the order against the number of drone
     * moves that are required to complete it. This value is used for picking the best order to do next in the
     * flight planner.
     *
     * @param moveCountToStartLocation The moves required to get to the first pick up location.
     * @return Estimated added value of completing the order against cost in moves.
     */
    public double getOrderValue(int moveCountToStartLocation) {
        double totalMoves = this.getOrderFlightPlanMoveCount() + moveCountToStartLocation;

        return this.orderPrice / totalMoves;
    }

    /**
     * This method marks this order as completed.
     */
    public void complete() {
        this.completed = true;
    }

    /**
     * This method is used to find out if the order is completed by the daily flight plan.
     *
     * @return True if the order has been completed
     */
    public boolean completed() {
        return this.completed;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public String getOrderPrice() {
        return String.valueOf(this.orderPrice);
    }

    public LongLat getDeliveryLocation() {
        return this.deliveryLocation;
    }

    public ArrayList<LongLat> getPickupLocations() {
        return this.pickupLocations;
    }
}