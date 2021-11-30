package uk.ac.ed.inf;

import java.util.ArrayList;

public class Order {
    private String orderNo;
    private String customer;
    private ArrayList<String> items;
    private LongLat deliveryLocation;

    /**
     *
     * @param orderNo
     * @param customer
     * @param items
     * @param deliveryLocation
     */
    public Order(String orderNo, String customer, ArrayList<String> items, LongLat deliveryLocation) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.items = items;
        this.deliveryLocation = deliveryLocation;
    }
}
