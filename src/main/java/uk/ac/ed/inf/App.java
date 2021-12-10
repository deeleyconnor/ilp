package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * This class is the main app.
 */
public class App 
{
    public static final String MACHINE_NAME = "localhost";

    /**
     * This method takes a date to create a flight plan for the drone on a certain day as well as a webserver port and
     * a database port and writes this plan to a geojson file as well as the flightpath and deliveries tables in the
     * database.
     */
    public static void main(String[] args) {
        String day  = args[0];
        String month = args[1];
        String year = args[2];
        String webserverPort = args[3];
        String databasePort = args[4];

        System.out.println( "System Started!" );

        System.out.println("Setting up Web Server client and Database client");
        WebServerClient.setupWebServerClient(MACHINE_NAME, webserverPort);
        DatabaseClient databaseClient = new DatabaseClient(MACHINE_NAME, databasePort);

        System.out.println("Getting Order Data");
        ArrayList<Order> orders = getOrderData(day, month, year, databaseClient);

        System.out.println("Planning Flight");
        FlightPlan flightPlan = getFlightPlan(orders);
        System.out.println("Flight Planned");

        int completedOrderCount = (int) orders.stream().filter(Order::completed).count();

        System.out.printf("Orders delivered %s/%s%n", completedOrderCount, orders.size());
        System.out.printf("Total Drone Moves %s%n",flightPlan.size());

        saveFlightPlan(day, month, year, databaseClient, orders, flightPlan);
    }

    /**
     * This method gets the order data so that a flight plan can be created.
     *
     * @param day The day of the lunch orders the delivery flight plan is to be created.
     * @param month The month of the lunch orders the delivery flight plan to be created.
     * @param year The year of the lunch orders the delivery flight plan to be created.
     * @param databaseClient The database client used for connecting to the database to request data.
     * @return The orders that are to be delivered on that date.
     */
    private static ArrayList<Order> getOrderData(String day, String month, String year, DatabaseClient databaseClient) {
        Menus menus = new Menus();
        ArrayList<Order> orders = databaseClient.getOrders(day, month, year);
        orders.forEach( (order) -> order.setOrderObjectives(menus));
        return orders;
    }

    /**
     * This method is used to get the flight plan from a Flight Planner given a list of orders.
     *
     * @param orders The orders that a flight plan is to be created for.
     * @return The best flight plan found for the list of orders.
     */
    private static FlightPlan getFlightPlan(ArrayList<Order> orders) {
        FlightPlanner flightPlanner = new FlightPlanner();
        return flightPlanner.dayFlightPlanner(orders);
    }

    /**
     * This method is used to write the plan to a geojson file as well as the flightpath and deliveries tables in the
     * database.
     *
     * @param day The day of the lunch orders delivery flight plan was created for.
     * @param month The month of the lunch orders delivery flight plan was created for.
     * @param year The year of the lunch orders delivery flight plan was created for.
     * @param databaseClient The database client used for connecting to the database to request data.
     * @param orders The orders that we want to add to the deliveries table in the database if they are completed.
     * @param flightPlan The flight plan that we want to create a geojson file for and add to the flightpath table in the
     *                   database.
     */
    private static void saveFlightPlan(String day, String month, String year, DatabaseClient databaseClient, ArrayList<Order> orders, FlightPlan flightPlan) {
        flightPlan.toGeoJson(day, month, year);
        databaseClient.createDeliveriesTable(orders);
        databaseClient.createFlightPathTable(flightPlan);
    }
}