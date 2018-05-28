package markets;


import housing.House;
import housing.Household;


/** Represents an offer by a specific household to sell a particular house at some price. */
public class Offer extends Order {

    private House house;

    Offer(Household household, House house, double price) {
        this.household = household;
        this.house = house;
        this.price = price;
    }

    public Household getHousehold() {
        return household;
    }

    public House getHouse() {
        return house;
    }

    public double getPrice() {
        return price;
    }

}
