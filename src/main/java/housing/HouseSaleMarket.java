package housing;

import java.util.Iterator;
import org.apache.commons.math3.random.MersenneTwister;
import utilities.PriorityQueue2D;

/**************************************************************************************************
 * Class to represent the sales market
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseSaleMarket extends HousingMarket {

    //------------------//
    //----- Fields -----//
    //------------------//

    private PriorityQueue2D<HousingMarketRecord>    offersPY;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    HouseSaleMarket(MersenneTwister prng) {
        super(prng);
        offersPY = new PriorityQueue2D<>(new HousingMarketRecord.PYComparator());
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    @Override
    public void init() {
        super.init();
        offersPY.clear();
    }

    /**
     * Deal with everything necessary whenever a house gets sold, such as deleting the saleRecord, signaling both the
     * seller and the buyer to complete everything on their respective sides, adding relevant data to statistics and
     * (possibly) writing it to a file and, finally, changing the house ownership. Note that any recording of
     * information takes place after the transaction has occurred, and thus after the exchange of contracts and money,
     * though before the official change of ownership.
     *
     * @param purchase HouseBidderRecord with information on the bidder
     * @param sale HouseOfferRecord with information on the seller and the offered property
     */
    public void completeTransaction(HouseBidderRecord purchase, HouseOfferRecord sale) {
        sale.getHouse().saleRecord = null;
        // This affects: seller's bank balance, seller's housePayments, HouseRentalMarket offers, seller's home, house's
        // renter, seller's rental income
        sale.getHouse().owner.completeHouseSale(sale);
        // This affects: buyer's home resident, buyer's home owner rental income, buyer's bank balance, mortgage,
        // buyer's housePayments, buyer's home, house's resident, HouseRentalMarket offers, and buyer's isFirstTimeBuyer
        purchase.getBidder().completeHousePurchase(sale, purchase.getDesiredDownPayment());
        // This uses: house's id and quality; mortgage's type, downpayment and principal; sale's initial listing price,
        // initial listing time and final price; buyer's id, age, BTL gene, income, rental income, bank balance, and
        // capital gains coefficient; and seller's id, age, BTL gene, income, rental income, bank balance and capital
        // gains coefficient
        Model.housingMarketStats.recordTransaction(purchase, sale);
        sale.getHouse().owner = purchase.getBidder();
    }

    @Override
    public HouseOfferRecord offer(House house, double price, boolean BTLOffer) {
        HouseOfferRecord hsr = super.offer(house, price, BTLOffer);
        offersPY.add(hsr);
        house.putForSale(hsr);
        return(hsr);
    }

    @Override
    public void removeOffer(HouseOfferRecord hsr) {
        super.removeOffer(hsr);
        offersPY.remove(hsr);
        hsr.getHouse().resetSaleRecord();
    }

    @Override
    public void updateOffer(HouseOfferRecord hsr, double newPrice) {
        offersPY.remove(hsr);
        super.updateOffer(hsr, newPrice);
        offersPY.add(hsr);
    }

    /**
     * This method overrides the main simulation step in order to sort the price-yield priorities.
     */
    @Override
    void clearMarket() {
        // Before any use, priorities must be sorted by filling in the uncoveredElements TreeSet at the corresponding
        // PriorityQueue2D. In particular, we sort here the price-yield priorities
        offersPY.sortPriorities();
        // Then continue with the normal HousingMarket clearMarket mechanism
        super.clearMarket();
    }

    /**
     * This method overrides the main getBestOffer method so as to consider buy-to-let households bidding for the
     * highest yield house being offered for a price up to that of their bid (offerPrice <= bidPrice)
     *
     * @param bid HouseBidderRecord with the highest possible price the buyer is ready to pay
     * @return HouseOfferRecord of the best offer available, null if the household cannot afford any offer
     */
    @Override
    protected HouseOfferRecord getBestOffer(HouseBidderRecord bid) {
        // BTL bids are yield-driven, thus use the offersPY priority queue
        if (bid.isBTLBid()) {
            return (HouseOfferRecord)offersPY.peek(bid);
        // Non-BTL bids are quality-driven, thus use the main (offersPQ) priority queue
        } else {
            return super.getBestOffer(bid);
        }
    }

    /**
     * Overrides corresponding method at HousingMarket in order to remove successfully matched and cleared offers from
     * the offersPY queue
     *
     * @param record Iterator over the HousingMarketRecord objects contained in offersPQ
     * @param offer Offer to remove from queues
     */
    @Override
    void removeOfferFromQueues(Iterator<HousingMarketRecord> record, HouseOfferRecord offer) {
        record.remove();
        offersPY.remove(offer);
    }
}
