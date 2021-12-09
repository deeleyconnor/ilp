package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Represents a lunch order.
 */
public class Order {
    private final String orderNo;
    private final String customer;
    private final ArrayList<String> items;
    private final String deliverTo;

    private LongLat deliveryLocation;
    private ArrayList<LongLat> pickupLocations = new ArrayList<>();
    private int orderPrice;

    private FlightPlan orderFlightPlan;
    private FlightPlan returnFlightPlan;

    private boolean completed = false;

    /**
     * Creates an instance of Order
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

    public void setOrderObjectives(Menus menus) {
        setDeliveryLocation();
        setPickupLocations(menus);
        setOrderPrice(menus);
    }

    public void setOrderFlightPlan(FlightPlan orderFlightPlan) {
        this.orderFlightPlan = orderFlightPlan;
    }

    public void setReturnFlightPlan(FlightPlan returnFlightPlan) {
        this.returnFlightPlan = returnFlightPlan;
    }

    private void setDeliveryLocation() {
        this.deliveryLocation = LocationFinder.findLocation(deliverTo);
    }

    private void setPickupLocations(Menus menus) {
        HashSet<String> pickupLocationsWords = menus.getPickupLocations(items);

        for (String words : pickupLocationsWords) {
            pickupLocations.add(LocationFinder.findLocation(words));
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

    public String getOrderPrice() {
        return String.valueOf(this.orderPrice);
    }

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
     * This method is used to
     *
     * @return
     */
    public boolean completed() {
        return this.completed;
    }
}
