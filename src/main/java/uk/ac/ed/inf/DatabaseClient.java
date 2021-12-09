package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This class is used for connecting to the database to request data and create new tables.
 */
public class DatabaseClient {

    private static final String DATABASE = "derbyDB";
    private static final String ORDERS_QUERY = "select * from orders where deliveryDate=(?)";
    private static final String ORDER_DETAILS_QUERY = "select * from orderDetails where orderNo=(?)";
    private static final String CREATE_DELIVERY_TABLE_STATEMENT = "create table deliveries (" +
                                                                    "orderNo char(8), " +
                                                                    "deliveredTo varchar(19), " +
                                                                    "costInPence int)";
    private static final String CREATE_FLIGHT_PATH_TABLE_STATEMENT = "create table flightpath (" +
                                                                        "orderNo char(8), " +
                                                                        "fromLongitude double, " +
                                                                        "fromLatitude double, " +
                                                                        "angle int, " +
                                                                        "toLongitude double, " +
                                                                        "toLatitude double)";
    private static final String DROP_DELIVERY_TABLE_STATEMENT = "drop table deliveries";
    private static final String DROP_FLIGHT_PATH_TABLE_STATEMENT = "drop table flightpath";
    private static final String INSERT_DELIVERY_QUERY = "insert into deliveries values (?, ?, ?)";
    private static final String INSERT_DRONE_MOVE_QUERY = "insert into flightpath values(?, ?, ?, ?, ?, ?)";

    private Connection conn;
    private PreparedStatement psOrdersQuery;
    private PreparedStatement psOrderDetailsQuery;
    private Statement statement;

    /**
     * This method creates an instance of the database client. It establishes a connection to the database and also
     * prepares a couple of statements which will be used to interact with the database.
     *
     * @param machineName The name of the machine which the database is running on.
     * @param port The port which the database is running on.
     */
    public DatabaseClient(String machineName, String port) {
        String jdbcString = String.format("jdbc:derby://%s:%s/%s", machineName, port, DATABASE);

        try {
            conn = DriverManager.getConnection(jdbcString);

            psOrdersQuery = conn.prepareStatement(ORDERS_QUERY);
            psOrderDetailsQuery = conn.prepareStatement(ORDER_DETAILS_QUERY);
            statement = conn.createStatement();
        }
        catch (SQLException e) {
            sqlExceptionHandler(e);
        }
    }

    /**
     * This method returns a list of orders objects from a given date.
     *
     * @param day The day of the orders being retrieved.
     * @param month The month of the orders being retrieved.
     * @param year The year of the orders being retrieved.
     * @return A list of all the orders from a given date.
     */
    public ArrayList<Order> getOrders(String day, String month, String year) {
        String date = String.format("%s-%s-%s", year,month,day);
        ArrayList<Order> orders = new ArrayList<>();

        try {
            psOrdersQuery.setString(1,date);
            ResultSet rs = psOrdersQuery.executeQuery();

            while (rs.next()) {
                String orderNo = rs.getString("orderNo");
                String deliverTo = rs.getString("deliverTo");

                ArrayList<String> items = getOrderDetails(orderNo);

                orders.add(new Order(orderNo, items, deliverTo));
            }
        }
        catch (SQLException e) {
           sqlExceptionHandler(e);
        }

        return orders;
    }

    /**
     * This method gets the names of items in an order given the order number from the orderDetails table.
     *
     * @param orderNo The order number.
     * @return A list of all the item names that are in the order.
     */
    private ArrayList<String> getOrderDetails(String orderNo) {
        ArrayList<String> items = new ArrayList<>();

        try {
            psOrderDetailsQuery.setString(1,orderNo);
            ResultSet rs = psOrderDetailsQuery.executeQuery();

            while (rs.next()) {
                String item = rs.getString("item");
                items.add(item);
            }
        }
        catch (SQLException e) {
            sqlExceptionHandler(e);
        }

        return items;
    }

    /**
     * This method creates and populates the deliveries table which contains information about every delivery that was
     * made by the drone makes on the day.
     *
     * @param orders The orders for
     */
    public void createDeliveriesTable(ArrayList<Order> orders) {
        newTable("deliveries",DROP_DELIVERY_TABLE_STATEMENT,CREATE_DELIVERY_TABLE_STATEMENT);

        try {
            PreparedStatement psInsertDeliveryQuery = conn.prepareStatement(INSERT_DELIVERY_QUERY);

            for (Order order: orders) {
                if (order.completed()) {
                    psInsertDeliveryQuery.setString(1, order.getOrderNo());
                    psInsertDeliveryQuery.setString(2, order.getDeliverTo());
                    psInsertDeliveryQuery.setString(3, order.getOrderPrice());
                    psInsertDeliveryQuery.execute();
                }
            }
        }
        catch (SQLException e) {
            sqlExceptionHandler(e);
        }

        System.out.println("Successfully created table \"deliveries\" in database.");
    }

    /**
     * This method create and populates the flightpath table which contains every drone move made by the drone while
     * making the day's lunch deliveries.
     *
     * @param flightPlan The flight plan with the list of drone moves that the flightpath table will contain.
     */
    public void createFlightPathTable(FlightPlan flightPlan) {
        newTable("flightpath", DROP_FLIGHT_PATH_TABLE_STATEMENT, CREATE_FLIGHT_PATH_TABLE_STATEMENT);
        ArrayList<DroneMove> droneMoves= flightPlan.getPlan();

        try {
            PreparedStatement psInsertDroneMoveQuery = conn.prepareStatement(INSERT_DRONE_MOVE_QUERY);

            for (DroneMove droneMove: droneMoves) {
                psInsertDroneMoveQuery.setString(1, droneMove.orderNo);
                psInsertDroneMoveQuery.setString(2, String.valueOf(droneMove.fromLongLat.longitude));
                psInsertDroneMoveQuery.setString(3, String.valueOf(droneMove.fromLongLat.latitude));
                psInsertDroneMoveQuery.setString(4, String.valueOf(droneMove.angle));
                psInsertDroneMoveQuery.setString(5, String.valueOf(droneMove.toLongLat.longitude));
                psInsertDroneMoveQuery.setString(6, String.valueOf(droneMove.toLongLat.latitude));
                psInsertDroneMoveQuery.execute();
            }
        }
        catch (SQLException e) {
            sqlExceptionHandler(e);
        }

        System.out.println("Successfully created table \"flightpath\" in database.");
    }

    /**
     * This method creates a new table. If a table of this name already exists in the derbyDB database then it is
     * deleted.
     *
     * @param tableName The name of the table that is being created.
     * @param tableDropStatement SQL statement to drop the table.
     * @param tableCreateStatement SQL statement to create the table.
     */
    private void newTable(String tableName, String tableDropStatement, String tableCreateStatement) {
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();

            ResultSet resultSet = databaseMetaData.getTables(null, null, tableName.toUpperCase(Locale.ROOT),null);

            if (resultSet.next()) {
                statement.execute(tableDropStatement);
            }
            statement.execute(tableCreateStatement);
        }
        catch (SQLException e) {
            sqlExceptionHandler(e);
        }
    }

    /**
     * If an SQL Exception is given it is a fatal error and thus we cannot recover from so the system will exit.
     *
     * @param message The reason for the SQL exception.
     */
    private static void sqlExceptionHandler(SQLException message) {
        System.err.println("SQL Exception");
        System.err.println(message);
        System.exit(1);
    }
}
