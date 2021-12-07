package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a lunch order.
 */
public class Order {
    private String orderNo;
    private String customer;
    private ArrayList<String> items;
    private String deliverTo;

    private LongLat deliveryLocation;
    private ArrayList<LongLat> pickupLocations = new ArrayList<>();
    private int orderPrice;

    private FlightPlan orderFlightPlan;
    private FlightPlan returnFlightPlan;

    private boolean completed = false;

    /**
     *
     *
     * @param orderNo
     * @param customer
     * @param items
     * @param deliverTo
     */
    public Order(String orderNo, String customer, ArrayList<String> items, String deliverTo) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.items = items;
        this.deliverTo = deliverTo;
    }

    public void setOrderObjectives(LocationFinder locationFinder, Menus menus) {
        setDeliveryLocation(locationFinder);
        setPickupLocations(locationFinder, menus);
        setOrderPrice(menus);
    }

    public void setOrderFlightPlan(FlightPlan orderFlightPlan) {
        this.orderFlightPlan = orderFlightPlan;
    }

    public void setReturnFlightPlan(FlightPlan returnFlightPlan) {
        this.returnFlightPlan = returnFlightPlan;
    }

    private void setDeliveryLocation(LocationFinder locationFinder) {
        this.deliveryLocation = locationFinder.findLocation(deliverTo);
    }

    private void setPickupLocations(LocationFinder locationFinder, Menus menus) {
        HashSet<String> pickupLocationsWords = menus.getPickupLocations(items);

        for (String words : pickupLocationsWords) {
            pickupLocations.add(locationFinder.findLocation(words));
        }
    }

    private void setOrderPrice(Menus menus) {
        this.orderPrice = menus.getDeliveryCost(items);
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public LongLat getDeliveryLocation() {
        return this.deliveryLocation;
    }

    public ArrayList<LongLat> getPickupLocations() {
        return this.pickupLocations;
    }

    public int getOrderFlightPlanMoveCount() {
        return  this.orderFlightPlan.size();
    }

    public FlightPlan getOrderFlightPlan() {
       return this.orderFlightPlan;
    }

    public int getOrderAndReturnFlightPlanMoveCount() {
        return  this.orderFlightPlan.size() + this.returnFlightPlan.size();
    }

    public FlightPlan getReturnFlightPlan() {
        return this.returnFlightPlan;
    }

    public int getOrderPrice() {
        return this.orderPrice;
    }

    public LongLat getStartLocation() {
        return orderFlightPlan.getPlan().get(0).fromLongLat;
    }

    public double getOrderValue(int moveCountToStartLocation) {
        double totalMoves = this.getOrderFlightPlanMoveCount() + moveCountToStartLocation;

        return this.orderPrice / totalMoves;
    }

    public void complete() {
        this.completed = true;
    }

    public boolean completed() {
        return this.completed;
    }
}
