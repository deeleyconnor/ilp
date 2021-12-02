package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashSet;

public class Order {
    private String orderNo;
    private String customer;
    private ArrayList<String> items;
    private String deliverTo;

    private LongLat deliveryLocation;
    private ArrayList<LongLat> pickupLocations = new ArrayList<>();
    private int orderPrice;

    private ArrayList<DroneMove> orderFlightPlan;
    private ArrayList<DroneMove> returnFlightPlan;

    private boolean completed = false;

    /**
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

    public void setOrderFlightPlan(ArrayList<DroneMove> orderFlightPlan) {
        this.orderFlightPlan = orderFlightPlan;
    }

    public void setReturnFlightPlan(ArrayList<DroneMove> returnFlightPlan) {
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

    public ArrayList<DroneMove> getOrderFlightPlan() {
       return this.orderFlightPlan;
    }

    public int getOrderAndReturnFlightPlanMoveCount() {
        return  this.orderFlightPlan.size() + this.returnFlightPlan.size();
    }

    public ArrayList<DroneMove> getReturnFlightPlan() {
        return this.returnFlightPlan;
    }

    public int getOrderPrice() {
        return this.orderPrice;
    }

    public LongLat getStartLocation() {
        return orderFlightPlan.get(0).fromLongLat;
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
