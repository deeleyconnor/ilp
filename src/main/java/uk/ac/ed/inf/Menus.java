package uk.ac.ed.inf;

import com.google.gson.Gson;

public class Menus {

    private final static int STANDARD_DELIVERY_CHARGE = 50;
    private final static String MENUS_FILE_LOCATION = "menus/menus.json";

    private Shop[] shops;

    public Menus(String machineName, String port) {
        String urlString = String.format("http://%s:%s/%s", machineName, port, MENUS_FILE_LOCATION); ;
        String responseBody = App.websiteRequest(urlString);

        shops = new Gson().fromJson(responseBody, Shop[].class);
    }

    public int getDeliveryCost(String ... order) {
        int orderCost = 0;

        for (String orderItem : order) {
            itemSearch:
            for (Shop shop : shops) {
                for (Shop.Item menuItem : shop.menu) {
                    if (menuItem.item.equals(orderItem)) {
                        orderCost = orderCost + menuItem.pence;
                        break itemSearch;
                    }
                }
            }
        }

        return orderCost + STANDARD_DELIVERY_CHARGE;
    }
}
