package uk.ac.ed.inf;

import java.sql.SQLException;

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

        DatabaseClient databaseClient = new DatabaseClient(MACHINE_NAME, databasePort);


    }

}