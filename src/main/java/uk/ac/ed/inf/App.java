package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * This class is the main app.
 */
public class App 
{
    public static final String MACHINE_NAME = "localhost";

    /**
     * This method takes a date to create a flight plan for the drone on a certain day.
     *
     * @param day The day of the month
     * @param month The month
     * @param year
     * @param webserverPort The port that the webserver is running on.
     * @param databasePort The port that the database is running on.
     */
    public static void main(String day, String month, String year,String webserverPort, String databasePort) {
        System.out.println( "System Started!" );

        System.out.println("Setting up Web Server client and Database client");
        WebServerClient.setupWebServerClient(MACHINE_NAME, webserverPort);
        DatabaseClient databaseClient = new DatabaseClient(MACHINE_NAME, databasePort);

        System.out.println("Getting Order Data");
        Menus menus = new Menus();
        ArrayList<Order> orders = databaseClient.getOrders(day,month,year);
        orders.forEach( (order) -> order.setOrderObjectives(menus));

        System.out.println("Planning Flight");
        FlightPlanner flightPlanner = new FlightPlanner();
        FlightPlan flightPlan = flightPlanner.dayFlightPlanner(orders);
        System.out.println("Flight Planned");

        flightPlan.toGeoJson(day, month, year);
        databaseClient.createDeliveriesTable(orders);
        databaseClient.createFlightPathTable(flightPlan);
    }
}