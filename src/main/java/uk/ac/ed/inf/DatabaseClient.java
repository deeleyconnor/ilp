package uk.ac.ed.inf;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

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
    private PreparedStatement psInsertDeliveryQuery;
    private PreparedStatement psInsertDroneMoveQuery;
    private Statement statement;

    //PLease Handle SQL Exception
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

    public ArrayList<Order> getOrders(String day, String month, String year) throws SQLException {
        String date = String.format("%s-%s-%s", year,month,day);

        psOrdersQuery.setString(1,date);

        ArrayList<Order> orders = new ArrayList<>();
        ResultSet rs = psOrdersQuery.executeQuery();
        while (rs.next()) {
            String orderNo = rs.getString("orderNo");
            String customer = rs.getString("customer");
            String deliverTo = rs.getString("deliverTo");

            ArrayList<String> items = getOrderDetails(orderNo);

            orders.add(new Order(orderNo, customer, items, deliverTo));
        }

        return orders;
    }

    private ArrayList<String> getOrderDetails(String orderNo) throws SQLException {
        psOrderDetailsQuery.setString(1,orderNo);

        ArrayList<String> items = new ArrayList<>();
        ResultSet rs = psOrderDetailsQuery.executeQuery();
        while (rs.next()) {
            String item = rs.getString("item");
            items.add(item);
        }

        return items;
    }

    public void createDeliveriesTable(ArrayList<Order> orders) {
        newTable("deliveries",DROP_DELIVERY_TABLE_STATEMENT,CREATE_DELIVERY_TABLE_STATEMENT);

        try {
            psInsertDeliveryQuery = conn.prepareStatement(INSERT_DELIVERY_QUERY);

            for (Order order: orders) {
                psInsertDeliveryQuery.setString(1, order.getOrderNo());
                psInsertDeliveryQuery.setString(2, order.getDeliverTo());
                psInsertDeliveryQuery.setString(3, order.getOrderPrice());
                psInsertDeliveryQuery.execute();
            }
        }
        catch (SQLException e) {
            sqlExceptionHandler(e);
        }

        System.out.println("Successfully created table \"deliveries\" in database.");
    }

    public void createFlightPathTable(FlightPlan flightPlan) {
        newTable("flightpath", DROP_FLIGHT_PATH_TABLE_STATEMENT, CREATE_FLIGHT_PATH_TABLE_STATEMENT);
        ArrayList<DroneMove> droneMoves= flightPlan.getPlan();

        try {
            psInsertDroneMoveQuery = conn.prepareStatement(INSERT_DRONE_MOVE_QUERY);

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

    private void newTable(String tableName, String tableDropStatement, String tableCreateStatement) {
        try {
            DatabaseMetaData databaseMetaData = conn.getMetaData();

            ResultSet resultSet = databaseMetaData.getTables(null, null, tableName,null);

            if (resultSet.next()) {
                statement.execute(tableDropStatement);
                statement.execute(tableCreateStatement);
            }
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
        System.out.println("SQL Exception");
        System.out.println(message);
        System.exit(1);
    }
}
