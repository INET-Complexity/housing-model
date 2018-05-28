package markets;


import housing.Household;


/** Represents an offer by a specific household to purchase a yet to be determined house at some price. */
public class Bid extends Order {

    Bid(Household household, double price) {
        this.household = household;
        this.price = price;
    }

    public Household getHousehold() {
        return household;
    }

    public double getPrice() {
        return price;
    }

}
