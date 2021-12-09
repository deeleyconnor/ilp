package uk.ac.ed.inf;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class is the main app.
 */
public class App 
{
    public static final String MACHINE_NAME = "localhost";

    /**
     * This method takes a date to create a fligh
     *
     * @param day
     * @param month
     * @param year
     * @param webserverPort The port that the webserver is running on.
     * @param databasePort The port that the database is running on.
     * @throws SQLException
     */
    public static void main(String day, String month, String year,String webserverPort, String databasePort) throws SQLException {
        System.out.println( "System Started!" );

        WebServerClient.setupWebServerClient(MACHINE_NAME, webserverPort);
        DatabaseClient databaseClient = new DatabaseClient(MACHINE_NAME, databasePort);

        //LocationFinder locationFinder = new LocationFinder();

        System.out.println("Getting Order Data");

        Menus menus = new Menus();

        ArrayList<Order> orders = databaseClient.getOrders(day,month,year);
        orders.forEach( (order) -> order.setOrderObjectives(menus));

        System.out.println();
        FlightPlanner flightPlanner = new FlightPlanner();
        FlightPlan flightPlan = flightPlanner.dayFlightPlanner(orders);

        System.out.println("Creating Json Files");
        flightPlan.toGeoJson();
    }
}