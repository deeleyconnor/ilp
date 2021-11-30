package uk.ac.ed.inf;

import uk.ac.ed.inf.JsonTemplates.Shop;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.HashSet;


/**
 * Represents the menus that are available to the delivery service.
 */
public class Menus {

    private final static int STANDARD_DELIVERY_CHARGE = 50;
    private final static String MENUS_FILE_LOCATION = "menus/menus.json";

    public HashMap<String, Item> items;

    public class Item {
        public int price;
        public String location;

        public Item(int price, String location) {
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
     * @param locationFinder
     * @see WebServerClient
     * @see Shop
     */
    public Menus(String machineName, String port) {
        String urlString = String.format("http://%s:%s/%s", machineName, port, MENUS_FILE_LOCATION);
        String responseBody = WebServerClient.request(urlString);

        Shop[] shops = new Gson().fromJson(responseBody, Shop[].class);
        items = new HashMap<String, Item>();

        for (Shop shop : shops) {
            for (Shop.Item item :shop.menu) {
                items.put(item.item, new Item(item.pence, shop.location));
            }
        }
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
            orderCost += items.get(item).price;
        }

        return orderCost;
    }

    public HashSet<String> getDeliveryLocations(String ... order) {
        HashSet<String> locations = new HashSet<String>();

        for (String item : order) {
            locations.add(items.get(item).location);
        }

        return locations;
    }
}