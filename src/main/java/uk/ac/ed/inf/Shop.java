package uk.ac.ed.inf;

/**
 * Represents a shop participating in the drone lunch delivery service.
 * Contains the name, location of the shop as well as the items that it sells.
 */
public class Shop {
    String name;
    String location;

    Item[] menu;

    /**
     * Represents an item sold by the shop.
     * Contains the name of the item as well as the price in pence.
     */
    public static class Item {
        String item;
        int pence;
    }
}
