package uk.ac.ed.inf.JsonTemplates;

/**
 * Represents a shop participating in the drone lunch delivery service.
 * Contains the name, location of the shop as well as the items that it sells.
 */
public class Shop {
    public String name;
    public String location;

    public Item[] menu;

    /**
     * Represents an item sold by the shop.
     * Contains the name of the item as well as the price in pence.
     */
    public static class Item {
        public String item;
        public int pence;
    }
}