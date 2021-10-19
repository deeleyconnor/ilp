package uk.ac.ed.inf;

import java.util.ArrayList;

public class Order {

    private String orderNo;
    private String customer;
    private LongLat deliverTo;
    private ArrayList<String> order;

    public Order(String orderNo, String customer,ArrayList<String> order,  String words) {
        this.orderNo = orderNo;
        this.customer = customer;
        this.order = order;

        //Figure way to convery words to LongLat
    }
}
