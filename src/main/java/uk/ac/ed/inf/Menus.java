package uk.ac.ed.inf;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Represents the menus that are available to the delivery service.
 */
public class Menus {

    private final static int STANDARD_DELIVERY_CHARGE = 50;
    private final static String MENUS_FILE_LOCATION = "menus/menus.json";

    private final HashMap<String, MenusItem> items = new HashMap<>();

    /**
     * Represents an item in a shops menu.
     */
    private class MenusItem {
        private final int price;
        private final String location;

        /**
         * Creates an instance of the Item class.
         *
         * @param price    The price of the item.
         * @param location The location of the shop where the item is sold.
         */
        private MenusItem(int price, String location) {
            this.price = price;
            this.location = location;
        }
    }

    /**
     * Creates an instance of the Menus class. Data for the menus is requested using the WebServerClient request method
     * from http://machineName:port/MENUS_FILE_LOCATION and are stored as a collection of shops.
     *
     * @param machineName The name of the machine which the server is running on.
     * @param port The port which the server is running on
     * @see WebServerClient
     * @see Shop
     * @see MenusItem
     */
    public Menus(String machineName, String port) {
        String responseBody = WebServerClient.request(machineName, port, MENUS_FILE_LOCATION);

        Shop[] shops = new Gson().fromJson(responseBody, Shop[].class);

        shopItemsToMenuItems(shops);
    }

    /**
     * This methods converts a collection of shops into a collection of HashMap
     *
     * @param shops
     */
    private void shopItemsToMenuItems(Shop[] shops) {
        for (Shop shop : shops) {
            for (Shop.Item item :shop.menu) {
                items.put(item.item, new MenusItem(item.pence, shop.location));
            }
        }
    }

    /**
     * Gets
     *
     * @param itemNames
     * @return
     */
    public int getDeliveryCost(ArrayList<String> itemNames) {
        int orderCost = STANDARD_DELIVERY_CHARGE;

        for (String item : itemNames) {
            orderCost += items.get(item).price;
        }

        return orderCost;
    }

    /**
     *
     * @param itemNames
     * @return
     */
    public HashSet<String> getPickupLocations(ArrayList<String> itemNames) {
        HashSet<String> locations = new HashSet<>();

        for (String item : itemNames) {
            locations.add(items.get(item).location);
        }

        return locations;
    }
}