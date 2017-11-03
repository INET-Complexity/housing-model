package housing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import utilities.PriorityQueue2D;

/**************************************************************************************************
 * Class that implements the market mechanism behind both the sale and the rental markets
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public abstract class HousingMarket implements Serializable {
    private static final long serialVersionUID = -7249221876467520088L;

    //------------------//
    //----- Fields -----//
    //------------------//

    private static Authority                        authority = new Authority();

    private Config                                  config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister                         rand = Model.rand; // Passes the Model's random number generator to a private field
    private PriorityQueue2D<HousingMarketRecord>    offersPQ;

    ArrayList<HouseBuyerRecord>                     bids;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    HousingMarket() {
        offersPQ = new PriorityQueue2D<>(new HousingMarketRecord.PQComparator()); //Priority Queue of (Price, Quality)
        // The integer passed to the ArrayList constructor is an initially declared capacity (for initial memory
        // allocation purposes), it will actually have size zero and only grow by adding elements
        // TODO: Check if this integer is too large or small, check speed penalty for using ArrayList as opposed to
        // TODO: normal arrays
        bids = new ArrayList<>(config.TARGET_POPULATION/16);
    }

    //----------------------//
    //----- Subclasses -----//
    //----------------------//

    // TODO: Make sure this authority class is actually needed
    static class Authority {
        private Authority() {}
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- Initialisation methods -----//
    
    public void init() { offersPQ.clear(); }

    //----- Methods to add, update, remove offers and bids -----//
    
    /**
     * Put a new offer on the market
     *
     * @param house House to put on the market
     * @param price List price for the house
     * @return HouseSaleRecord for the house
     */
    public HouseSaleRecord offer(House house, double price) {
        HouseSaleRecord hsr = new HouseSaleRecord(house, price);
        offersPQ.add(hsr);
        return hsr;
    }
    
    /**
     * Change the list-price on a house that is already on the market
     * 
     * @param hsr The HouseSaleRecord of the house to change the price for
     * @param newPrice The new price of the house
     */
    public void updateOffer(HouseSaleRecord hsr, double newPrice) {
        offersPQ.remove(hsr);
        hsr.setPrice(newPrice, authority);
        offersPQ.add(hsr);
    }
    
    /**
     * Take a house off the market
     * 
     * @param hsr The HouseSaleRecord of the house to take off the market
     */
    public void removeOffer(HouseSaleRecord hsr) { offersPQ.remove(hsr); }

    /**
     * Make a bid on the market (i.e. make an offer on a (yet to be decided) house
     * 
     * @param buyer The household that is making the bid
     * @param price The price that the household is willing to pay
     */
    public void bid(Household buyer, double price) {
        bids.add(new HouseBuyerRecord(buyer, price)); }

    //----- Market clearing methods -----//

    /**
     * Main simulation step. For a number of rounds, matches bids with offers and clears the matches.
     */
    void clearMarket() {
        // offersPQ contains Price-Quality 2D-priority queue of offers
        // offersPY contains Price-Yield 2D-priority queue of offers
        // bids contains bids (HouseBuyerRecords) in an array
        // TODO: This number of rounds needs more thinking... this is just a cheap fix for the moment
        // TODO: This needs to be correctly described in the paper!!!
        int rounds = Math.max(10, Model.households.size()/50); // Previously, int rounds = Math.min(config.TARGET_POPULATION/1000, 1 + (offersPQ.size() + bids.size())/500);
        int i = 0;
        while (i < rounds && bids.size() > 0 && offersPQ.size() > 0) { // Previously, for(int i=0, i<rounds, i++) {
            matchBidsWithOffers(); // Step 1: iterate through bids
            clearMatches(); // Step 2: iterate through offers
            i++; // Previously absent
        }
        bids.clear();
    }

    /**
     * First step to clear the market. Iterate through all bids and, for each bid, find the best quality house being
     * offered for that price or lower (if it exists) and record the match. Note that offers could be matched with
     * multiple bids.
     */
    private void matchBidsWithOffers() {
        HouseSaleRecord offer;
        for(HouseBuyerRecord bid : bids) {
            offer = getBestOffer(bid);
            // If buyer and seller is the same household, then the bid falls through and the household will need to
            // reissue it next month. Also, if the bid price is not enough to buy anything in this market and at this
            // time, the bid also falls through
            if(offer != null && (offer.house.owner != bid.buyer)) {
                offer.matchWith(bid);
            }
        }
        // To keep only matched bids, we clear the bids ArrayList, it will be refilled with unsuccessful bids when
        // matches are cleared at clearMatches
        bids.clear();
    }

    /**
     * Second step to clear the market. Iterate through all offers and, for each offer, loop through its matched bids.
     * If BIDUP is activated, the offer price is bid up according to a geometric distribution with mean dependent on the
     * number of matched bids.
     */
    private void clearMatches() {
        // Clear and resolve oversubscribed offers
        HouseSaleRecord offer;
        GeometricDistribution geomDist;
        int nBids;
        double pSuccessfulBid;
        double salePrice;
        int winningBid;
        int enoughBids; // Upper bounded number of bids on one house
        Iterator<HousingMarketRecord> record = getOffersIterator();
        while(record.hasNext()) {
            offer = (HouseSaleRecord)record.next();
            nBids = offer.matchedBids.size();
            // If matches for this offer are multiple...
            if(nBids > 1) {
                // ...first bid up the price
                if(config.BIDUP > 1.0) {
                    // TODO: All this enough bids mechanism is not explained! The 10000/N factor, the 0.5 added, and the
                    // TODO: topping of the function at 4 are not declared in the paper. Remove or explain!
                    enoughBids = Math.min(4, (int)(0.5 + nBids*10000.0/config.TARGET_POPULATION));
                    // TODO: Also, the role of MONTHS_UNDER_OFFER is not explained or declared!
                    pSuccessfulBid = Math.exp(-enoughBids*config.derivedParams.MONTHS_UNDER_OFFER);
                    geomDist = new GeometricDistribution(rand, pSuccessfulBid);
                    salePrice = offer.getPrice()*Math.pow(config.BIDUP, geomDist.sample());
                } else {
                    salePrice = offer.getPrice();                    
                }
                // ...then choose a bid above the new price
                offer.matchedBids.sort(new HouseBuyerRecord.PComparator()); // This orders the list with the highest price last
                while(nBids > 0 && offer.matchedBids.get(nBids - 1).getPrice() >= salePrice) {
                    --nBids; // This counts the number of bids above the new price
                }
                if (offer.matchedBids.size() - nBids > 1) {
                    winningBid = nBids + rand.nextInt(offer.matchedBids.size()- nBids); // This chooses a random one if they are multiple
                } else if (offer.matchedBids.size() - nBids == 1) {
                    winningBid = nBids; // This chooses the only one if there is only one
                } else {
                    winningBid = nBids - 1;
                    salePrice = offer.matchedBids.get(winningBid).getPrice(); // This chooses the highest bid if all of them are below the new price
                }
                // ...update price for the offer
                offer.setPrice(salePrice, authority);
                // ...complete successful transaction and record it into the corresponding housingMarketStats
                completeTransaction(offer.matchedBids.get(winningBid), offer);
                // Put the rest of the bids for this property (failed bids) back on bids array
                bids.addAll(offer.matchedBids.subList(0, winningBid));
                bids.addAll(offer.matchedBids.subList(winningBid + 1, offer.matchedBids.size()));
                // Remove this offer from the offers priority queue, offersPQ, underlying the record iterator
                removeOfferFromQueues(record, offer);
            // If there is only one match...
            } else if (nBids == 1) {
                // ...complete successful transaction and record it into the corresponding housingMarketStats
                completeTransaction(offer.matchedBids.get(0), offer);
                // ...remove this offer from the offers priority queue, offersPQ, underlying the record iterator (and, for HouseSaleMarket, also from the PY queue)
                removeOfferFromQueues(record, offer);
            }
            // Note that we skip the whole process if there are no matches
        }
    }

    /**
     * Extracts the removal of successfully matched and cleared offers from the priority queues from the clearMatches
     * method, so that only this part can be overridden at HouseSaleMarket
     *
     * @param record Iterator over the HousingMarketRecord objects contained in offersPQ
     * @param offer Offer to remove from queues
     */
    void removeOfferFromQueues(Iterator<HousingMarketRecord> record, HouseSaleRecord offer) {
        record.remove();
    }

    /**
     * This abstract method allows for the different implementations at HouseSaleMarket and HouseRentalMarket to be
     * called as appropriate
     *
     * @param purchase HouseBuyerRecord with information on the offer
     * @param sale HouseSaleRecord with information on the bid
     */
    public abstract void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale);

    //----- Getter/setter methods -----//

    public ArrayList<HouseBuyerRecord> getBids() { return bids; }

    public PriorityQueue2D<HousingMarketRecord> getOffersPQ() { return offersPQ; }

    Iterator<HousingMarketRecord> getOffersIterator() { return(offersPQ.iterator()); }

    /**
     * Get the highest quality house being offered for a price up to that of the bid (OfferPrice <= bidPrice)
     *
     * @param bid The highest possible price the buyer is ready to pay
     */
    protected HouseSaleRecord getBestOffer(HouseBuyerRecord bid) { return (HouseSaleRecord)offersPQ.peek(bid); }

    int getnHousesOnMarket() { return offersPQ.size(); }
}
