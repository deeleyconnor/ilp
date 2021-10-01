package uk.ac.ed.inf;

public class Menus {

    public String machineName;
    public String port;

    public Menus(String machineName, String port){
        this.machineName = machineName;
        this.port = port;
    }

    public int getDeliveryCost(String ... order) {
        return 50;
    }
}
