package housing;

import java.util.Comparator;

/**************************************************************************************************
 * This class encapsulates information on a household that has placed a bid for a house on the
 * rental or the ownership housing market. One can think of it as the file an estate agent would
 * have on a customer who wants to buy or rent a house.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseBidderRecord extends HousingMarketRecord {

    //------------------//
    //----- Fields -----//
    //------------------//

    private Household bidder; // Household who is bidding to buy or rent a house
    private boolean BTLBid; // True if the bid is for a buy-to-let property, false for a home bid (Note that rental bids are all set to false)
    private double desiredDownPayment;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    HouseBidderRecord(Household h, double price, boolean BTLBid, double desiredDownPayment) {
        super(price);
        this.bidder = h;
        this.BTLBid = BTLBid;
        this.desiredDownPayment = desiredDownPayment;
    }

    //----------------------//
    //----- Subclasses -----//
    //----------------------//

    /**
     * Class that implements a price comparator which solves the case of equal price by using the arguments' IDs.
     */
    public static class PComparator implements Comparator<HouseBidderRecord> {
        @Override
        public int compare(HouseBidderRecord arg0, HouseBidderRecord arg1) {
            double diff = arg0.getPrice() - arg1.getPrice();
            if (diff == 0.0) {
                diff = arg0.getId() - arg1.getId();
            }
            return (int) Math.signum(diff);
        }
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- Getter/setter methods -----//

    public Household getBidder() { return bidder; }

    boolean isBTLBid() { return BTLBid; }

    // TODO: Check if the abstract method in HousingMarketRecord class is actually needed, otherwise this could be removed
    @Override
    public int getQuality() { return 0; }

    double getDesiredDownPayment() { return desiredDownPayment; }
}
