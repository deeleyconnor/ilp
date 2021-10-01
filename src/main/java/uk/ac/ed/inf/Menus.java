package uk.ac.ed.inf;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;


public class Menus {

    private final static int STANDARD_DELIVERY_CHARGE = 50;

    public static final HttpClient client = HttpClient.newHttpClient();

    private Shop[] shops;

    public Menus(String machineName, String port) {
        String urlString = String.format("http://%s:%s/menus/menus.json", machineName, port); ;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)) .build();

        try {
            HttpResponse <String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                shops = new Gson().fromJson(response.body(), Shop[].class);
            }
            else {
                System.out.println("Menu Request Failed");
                System.out.println(response.statusCode());
                System.exit(1);
            }

        }
        catch (Exception e) {
            System.out.println("Menus Request Failed");
            System.out.println(e);
            System.exit(1);
        }
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
