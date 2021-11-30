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

    public LongLat getDeliveryLocation() {
        return this.deliveryLocation;
    }

    public ArrayList<LongLat> getPickupLocations() {
        return this.pickupLocations;
    }

    public int getOrderPrice() {
        return this.orderPrice;
    }
}
