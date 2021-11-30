package uk.ac.ed.inf;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class is the main app.
 */
public class App 
{

    private static final String MACHINE_NAME = "localhost";

    /**
     *
     * @param day
     * @param month
     * @param year
     * @param webserverPort
     * @param databasePort
     * @throws SQLException
     */
    public static void main(String day, String month, String year,String webserverPort, String databasePort) throws SQLException {
        System.out.println( "System Started!" );

        LocationFinder locationFinder = new LocationFinder(MACHINE_NAME, webserverPort);

        DatabaseClient databaseClient = new DatabaseClient(MACHINE_NAME, databasePort);

        Menus menus = new Menus(MACHINE_NAME, webserverPort, locationFinder);
        ArrayList<Order> orders = databaseClient.getOrders(day,month,year, locationFinder);

    }

}