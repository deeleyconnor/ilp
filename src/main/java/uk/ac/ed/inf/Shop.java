package uk.ac.ed.inf;

/**
 * Represents a Shop. This class is used for parsing a JSON file menus.json in the menus folder. Contains the location
 * of the shop as well as the items that it sells.
 */
public class Shop {
    public String location;
    public Item[] menu;

    /**
     * Represents an item sold by the shop. Contains the name of the item as well as the price in pence.
     */
    public static class Item {
        public String item;
        public int pence;
    }
}