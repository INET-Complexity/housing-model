package housing;

import org.apache.commons.math3.random.MersenneTwister;

/**************************************************************************************************
 * Class to represent the rental market
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseRentalMarket extends HousingMarket {

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public HouseRentalMarket(MersenneTwister prng) { super(prng); }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Deal with everything necessary whenever a house gets rented out, such as deleting the rentalRecord, signaling
     * both the renter and the landlord to complete everything on their respective sides, adding relevant data to
     * statistics and (possibly) writing it to a file. Note that any recording of information takes place after the
     * transaction has occurred, and thus after the exchange of contracts and payment obligations.
     *
     * @param purchase HouseBidderRecord with information on the renter
     * @param sale HouseOfferRecord with information on the landlord and the offered property
     */
    @Override
    public void completeTransaction(HouseBidderRecord purchase, HouseOfferRecord sale) {
        sale.getHouse().rentalRecord = null;
        // This affects: house's resident, renter's home, renter's housePayments, renter, seller's rental income
        sale.getHouse().owner.completeHouseLet(sale, purchase.getBidder().completeHouseRental(sale));
        // This uses: house's id and quality; mortgage's type, downpayment and principal; sale's initial listing price,
        // initial listing time and final price; buyer's id, age, BTL gene, income, rental income, bank balance, and
        // capital gains coefficient; and seller's id, age, BTL gene, income, rental income, bank balance and capital
        // gains coefficient
        Model.rentalMarketStats.recordTransaction(purchase, sale);
    }

    @Override
    public HouseOfferRecord offer(House house, double price, boolean BTLOffer) {
        HouseOfferRecord hsr = super.offer(house, price, false);
        house.putForRent(hsr);
        return(hsr);
    }

    @Override
    public void removeOffer(HouseOfferRecord hsr) {
        super.removeOffer(hsr);
        hsr.getHouse().resetRentalRecord();
    }
}
