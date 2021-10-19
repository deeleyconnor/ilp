package uk.ac.ed.inf;

import java.util.ArrayList;

public class Order {

    private String orderNo;
    private String customer;
    private LongLat deliverTo;
    private ArrayList<String> items;

    /**
     *
     * @param orderNo
     * @param customer
     * @param items
     * @param words
     */
    public Order(String orderNo, String customer, ArrayList<String> items, String words) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.items = items;

        //Figure way to convery words to LongLat
    }
}
