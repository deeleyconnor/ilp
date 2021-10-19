package uk.ac.ed.inf;

import com.google.gson.Gson;

/**
 * Represents the menus that are available to the delivery service.
 */
public class Menus {

    private final static int STANDARD_DELIVERY_CHARGE = 50;
    private final static String MENUS_FILE_LOCATION = "menus/menus.json";

    private Shop[] shops;

    /**
     * Creates an instance of the Menus class. Data for the menus is requested using the WebServerClient request method
     * from http://machineName:port/MENUS_FILE_LOCATION and are stored as a collection of shops.
     *
     * @param machineName The name of the machine which the server is running on.
     * @param port The port which the server is running on.
     * @see WebServerClient
     * @see Shop
     */
    public Menus(String machineName, String port) {
        String urlString = String.format("http://%s:%s/%s", machineName, port, MENUS_FILE_LOCATION);
        String responseBody = WebServerClient.request(urlString);

        shops = new Gson().fromJson(responseBody, Shop[].class);
    }

    /**
     * This method calculates the cost of a order being delivered by drone including the standard delivery charge.
     *
     * @param order A variable number of String of names of items for an order.
     * @return The cost in pence to deliver the given list of items.
     */
    public int getDeliveryCost(String ... order) {
        int orderCost = STANDARD_DELIVERY_CHARGE;

        for (String item : order) {
            orderCost += getItemCost(item);
        }

        return orderCost;
    }

    /**
     * This method gets the cost of an individual item by searching through the individual menus of each of the shops.
     *
     * @param item The item of which we are looking for the cost.
     * @return The cost in pence of the given orderItem. Returns 0 if item was not found.
     * @see Shop
     */
    private int getItemCost(String item) {
        for (Shop shop : shops) {
            for (Shop.Item menuItem : shop.menu) {
                if (menuItem.item.equals(item)) {
                    return menuItem.pence;
                }
            }
        }

        return 0;
    }
}