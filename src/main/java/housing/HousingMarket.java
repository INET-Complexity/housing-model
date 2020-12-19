package housing;

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
public abstract class HousingMarket {

    //------------------//
    //----- Fields -----//
    //------------------//

    private Config                                  config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister                         prng;
    private PriorityQueue2D<HousingMarketRecord>    offersPQ;
    private ArrayList<HouseBidderRecord>            bids;
    private int []                                  nBidUpFrequency; // Counts the frequency of the number of bid-ups. TODO: Move to a collector class

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    HousingMarket(MersenneTwister prng) {
        offersPQ = new PriorityQueue2D<>(new HousingMarketRecord.PQComparator()); //Priority Queue of (Price, Quality)
        // The integer passed to the ArrayList constructor is an initially declared capacity (for initial memory
        // allocation purposes), it will actually have size zero and only grow by adding elements
        bids = new ArrayList<>(config.TARGET_POPULATION/10);
        this.prng = prng;
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
     * @return HouseOfferRecord for the house
     */
    public HouseOfferRecord offer(House house, double price, boolean BTLOffer) {
        HouseOfferRecord hsr = new HouseOfferRecord(house, price, BTLOffer);
        offersPQ.add(hsr);
        return hsr;
    }
    
    /**
     * Change the list-price on a house that is already on the market
     * 
     * @param hsr The HouseOfferRecord of the house to change the price for
     * @param newPrice The new price of the house
     */
    public void updateOffer(HouseOfferRecord hsr, double newPrice) {
        offersPQ.remove(hsr);
        hsr.setPrice(newPrice);
        offersPQ.add(hsr);
    }
    
    /**
     * Take a house off the market
     * 
     * @param hsr The HouseOfferRecord of the house to take off the market
     */
    public void removeOffer(HouseOfferRecord hsr) { offersPQ.remove(hsr); }

    /**
     * Make a non-BTL bid on the market, i.e. make an offer on a (yet to be decided) house to become the household's home
     * 
     * @param buyer The household that is making the bid
     * @param price The price that the household is willing to pay
     */
    public void bid(Household buyer, double price, boolean BTLBid, double desiredDownPayment) {
        bids.add(new HouseBidderRecord(buyer, price, BTLBid, desiredDownPayment));
    }

    //----- Market clearing methods -----//

    /**
     * Main simulation step. For a number of rounds, matches bids with offers and clears the matches.
     */
    void clearMarket() {
        nBidUpFrequency = new int[21]; // Re-start bid-up counter (while this array size is arbitrary, anything above 10 should be enough)
        // Before any use, priorities must be sorted by filling in the uncoveredElements TreeSet at the corresponding
        // PriorityQueue2D, in this case, the offersPQ object contains a Price-Quality 2D-priority queue of offers
        offersPQ.sortPriorities();
        while (bids.size() > 0 && offersPQ.size() > 0) {
            matchBidsWithOffers(); // Step 1: iterate through bids
            clearMatches(); // Step 2: iterate through offers
        }
        bids.clear();
        // Record the frequency of bid-ups
        if (config.recordNBidUpFrequency) {
            Model.transactionRecorder.recordNBidUpFrequency(Model.getTime(), nBidUpFrequency);
        }
    }

    /**
     * First step to clear the market. Iterate through all bids and, for each bid, find the best quality house being
     * offered for that price or lower (if it exists) and record the match. Note that offers could be matched with
     * multiple bids.
     */
    private void matchBidsWithOffers() {
        HouseOfferRecord offer;
        for(HouseBidderRecord bid : bids) {
            offer = getBestOffer(bid);
            // If buyer and seller is the same household, then the bid falls through and the household will need to
            // reissue it next month. Also, if the bid price is not enough to buy anything in this market and at this
            // time, the bid also falls through
            if(offer != null && (offer.getHouse().owner != bid.getBidder())) {
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
        HouseOfferRecord offer;
        GeometricDistribution geomDist;
        int nBids;
        double pSuccessfulBid;
        double salePrice;
        int winningBid;
        Iterator<HousingMarketRecord> record = getOffersIterator();
        while(record.hasNext()) {
            offer = (HouseOfferRecord)record.next();
            nBids = offer.getMatchedBids().size();
            // If matches for this offer are multiple...
            if(nBids > 1) {
                // ...first bid up the price
                if(config.BIDUP > 1.0) {
                    // Bearing in mind the design of the market mechanism, which leads to an unreasonably large number
                    // of bids per offer as bidders "trickle up" to more expensive offers, we need to take the logarithm
                    // (base 10) of the model number of bidders to get a realistic one for computing bid-ups
                    int rescaledNBids = Math.max((int)(Math.log10(nBids)), 1);
                    // Assuming bids a randomly distributed throughout the month, this is the probability of two
                    // consecutive bids having at least a week between them
                    pSuccessfulBid = Math.pow((1.0 - config.derivedParams.MONTHS_UNDER_OFFER), (rescaledNBids - 1));
                    if (pSuccessfulBid == 0.0) pSuccessfulBid = Float.MIN_VALUE; // Keeping the probability non-zero
                    // Given the previous probability of success (two consecutive bids more than a week apart), find the
                    // number of attempts before a success (number of consecutive bids less than a week apart before two
                    // consecutive bids more than a week apart), which corresponds to a draw from a geometric
                    // distribution
                    geomDist = new GeometricDistribution(prng, pSuccessfulBid);
                    int nBidUps = geomDist.sample();
                    addNBidUps(nBidUps);
                    // Finally compute the new price
                    salePrice = offer.getPrice()*Math.pow(config.BIDUP, nBidUps);
                } else {
                    salePrice = offer.getPrice();                    
                }
                // ...then choose a bid above the new price
                offer.getMatchedBids().sort(new HouseBidderRecord.PComparator()); // This orders the list with the highest price last
                while(nBids > 0 && offer.getMatchedBids().get(nBids - 1).getPrice() >= salePrice) {
                    --nBids; // This counts the number of bids above the new price
                }
                if (offer.getMatchedBids().size() - nBids > 1) {
                    winningBid = nBids + prng.nextInt(offer.getMatchedBids().size()- nBids); // This chooses a random one if they are multiple
                } else if (offer.getMatchedBids().size() - nBids == 1) {
                    winningBid = nBids; // This chooses the only one if there is only one
                } else {
                    winningBid = nBids - 1;
                    salePrice = offer.getMatchedBids().get(winningBid).getPrice(); // This chooses the highest bid if all of them are below the new price
                }
                // Remove this offer from the offers priority queue, offersPQ, underlying the record iterator (and, for HouseSaleMarket, also from the PY queue)
                // Note that this needs to be done before modifying offer, so that it can be also found in the PY queue for the HouseSaleMarket case
                removeOfferFromQueues(record, offer);
                // ...update price for the offer
                offer.setPrice(salePrice);
                // ...complete successful transaction and record it into the corresponding housingMarketStats
                completeTransaction(offer.getMatchedBids().get(winningBid), offer);
                // Put the rest of the bids for this property (failed bids) back on bids array
                bids.addAll(offer.getMatchedBids().subList(0, winningBid));
                bids.addAll(offer.getMatchedBids().subList(winningBid + 1, offer.getMatchedBids().size()));
            // If there is only one match...
            } else if (nBids == 1) {
                addNBidUps(0);
                // ...complete successful transaction and record it into the corresponding housingMarketStats
                completeTransaction(offer.getMatchedBids().get(0), offer);
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
    void removeOfferFromQueues(Iterator<HousingMarketRecord> record, HouseOfferRecord offer) {
        record.remove();
    }

    /**
     * This abstract method allows for the different implementations at HouseSaleMarket and HouseRentalMarket to be
     * called as appropriate
     *
     * @param purchase HouseBidderRecord with information on the offer
     * @param sale HouseOfferRecord with information on the bid
     */
    public abstract void completeTransaction(HouseBidderRecord purchase, HouseOfferRecord sale);

    // Add a transaction with a number nBidUps of bid-up attempts
    private void addNBidUps(int nBidUps) {
        if (nBidUps < nBidUpFrequency.length) {
            nBidUpFrequency[nBidUps] += 1;
        } else {
            nBidUpFrequency[nBidUpFrequency.length - 1] += 1;
        }
    }

    //----- Getter/setter methods -----//

    public ArrayList<HouseBidderRecord> getBids() { return bids; }

    public PriorityQueue2D<HousingMarketRecord> getOffersPQ() { return offersPQ; }

    private Iterator<HousingMarketRecord> getOffersIterator() { return(offersPQ.iterator()); }

    /**
     * Get the highest quality house being offered for a price up to that of the bid (OfferPrice <= bidPrice)
     *
     * @param bid HouseBidderRecord with the highest possible price the buyer is ready to pay
     * @return HouseOfferRecord of the best offer available, null if the household cannot afford any offer
     */
    protected HouseOfferRecord getBestOffer(HouseBidderRecord bid) { return (HouseOfferRecord)offersPQ.peek(bid); }

    int getnHousesOnMarket() { return offersPQ.size(); }
}
