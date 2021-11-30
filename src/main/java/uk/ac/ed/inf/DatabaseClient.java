package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseClient {

    private static final String DATABASE = "derbyDB";
    private static final String ORDERS_QUERY = "select * from orders where deliveryData=(?)";
    private static final String ORDER_DETAILS_QUERY = "select * from orderDetails where orderNo=(?)";

    private final Connection conn;
    private final PreparedStatement psOrdersQuery;
    private final PreparedStatement psOrderDetailsQuery;

    //PLease Handle SQL Exception
    public DatabaseClient(String machineName, String port) throws SQLException {
        String jdbcString = String.format("jdbc:derby://%s:%s/%s", machineName, port, DATABASE);

        conn = DriverManager.getConnection(jdbcString);

        psOrdersQuery = conn.prepareStatement(ORDERS_QUERY);
        psOrderDetailsQuery = conn.prepareStatement(ORDER_DETAILS_QUERY);
    }

    public ArrayList<Order> getOrders(String day, String month, String year, LocationFinder locationFinder) throws SQLException {
        String date = String.format("%s-%s-%s", year,month,day);

        psOrdersQuery.setString(2,date);

        ArrayList<Order> orders = new ArrayList<>();
        ResultSet rs = psOrdersQuery.executeQuery();
        while (rs.next()) {
            String orderNo = rs.getString("orderNo");
            String customer = rs.getString("customer");
            String words = rs.getString("deliverTo");

            ArrayList<String> items = getOrderDetails(orderNo);
            LongLat deliveryLocation = locationFinder.findLocation(words);

            orders.add(new Order(orderNo, customer, items, deliveryLocation));
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
}
