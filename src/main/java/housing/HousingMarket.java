package housing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utilities.PriorityQueue2D;

/**********************************************************
 * Implementation of the mechanism of the house-sale and
 * house-rental markets.
 * 
 * See model description for details.
 * 
 * @author daniel
 *
 *********************************************************/
public abstract class HousingMarket implements Serializable {
    private static final long serialVersionUID = -7249221876467520088L;

    private Config    config = Model.config;    // Passes the Model's configuration parameters object to a private field

    // TODO: Make sure this authority class is actually needed
    static public class Authority {
        private Authority() {}
    }

    public HousingMarket() {
        offersPQ = new PriorityQueue2D<>(new HousingMarketRecord.PQComparator()); //Priority Queue of (Price, Quality)
        // The integer passed to the ArrayList constructor is an initially declared capacity (for initial memory
        // allocation purposes), it will actually have size zero and only grow by adding elements
        // TODO: Check if this integer is too large or small, check speed penalty for using ArrayList as opposed to normal arrays
        bids = new ArrayList<>(config.TARGET_POPULATION/16);
        HPIRecord = new DescriptiveStatistics(config.derivedParams.HPI_RECORD_LENGTH);
        quarterlyHPI.addValue(1.0);
        quarterlyHPI.addValue(1.0);        
        init();
    }

    public double[] getAverageSalePrice() {
        return averageSalePrice;
    }

    public ArrayList<HouseBuyerRecord> getBids() {
        return bids;
    }

    public PriorityQueue2D<HousingMarketRecord> getOffersPQ() {
        return offersPQ;
    }
    
    public void init() {
        int i;
        for(i = 0; i<config.N_QUALITY; ++i) {
            averageSalePrice[i] = referencePrice(i);
        }
        housePriceIndex = 1.0;
        // TODO: Why to initiate averageDaysOnMarket to 30? Check if this has any influence!
        // TODO: Make this and any other dummy initial values explicit in the paper
        averageDaysOnMarket = config.constants.DAYS_IN_MONTH;
        for(i=0; i<config.derivedParams.HPI_RECORD_LENGTH; ++i) HPIRecord.addValue(1.0);
        offersPQ.clear();
//        matches.clear();
    }
    
    /******************************************
     * Put a new offer on the market.
     * @param house House to put on the market
     * @param price List price for the house.
     ******************************************/
    public HouseSaleRecord offer(House house, double price) {
        HouseSaleRecord hsr = new HouseSaleRecord(house, price);
        offersPQ.add(hsr);
        return(hsr);
    }
    
    /******************************************
     * Change the list-price on a house that is already on
     * the market.
     * 
     * @param hsr The HouseSaleRecord to change the price for.
     * @param newPrice The new price of the house.
     ******************************************/
    public void updateOffer(HouseSaleRecord hsr, double newPrice) {
        offersPQ.remove(hsr);
        hsr.setPrice(newPrice, authority);
        offersPQ.add(hsr);
    }
    
    /*******************************************
     * Take a house off the market.
     * 
     * @param hsr The HouseSaleRecord to take off the market.
     *******************************************/
    public void removeOffer(HouseSaleRecord hsr) {
        offersPQ.remove(hsr);
    }

    /*******************************************
     * Make a bid on the market (i.e. make an offer on
     * a (yet to be decided) house).
     * 
     * @param buyer The household that is making the bid.
     * @param price The price that the household is willing to pay.
     ******************************************/
    public void bid(Household buyer, double price) {
        bids.add(new HouseBuyerRecord(buyer, price));
        // match bid with current offers
    }


    /***************************
     * Get the highest quality offer for a price up to that of the bid
     *
     * @param bid the highest possible price we are looking for
     * @return the highest quality house being offered for a price <= bid
     */
    protected HouseSaleRecord getBestOffer(HouseBuyerRecord bid) {
        return (HouseSaleRecord)offersPQ.peek(bid);
    }
    
    public Iterator<HousingMarketRecord> offersIterator() {
        return(offersPQ.iterator());
    }

    /**********************************
     * The first step to clear the market.
     *
     * Iterate through all *bids* and, for each bid, find the best house being offered
     * for that price or lower (if it exists) and record the match. Note that
     * offers could be matched with multiple bids.
     *
     */
    protected void matchBidsWithOffers() {
        HouseSaleRecord offer;
        for(HouseBuyerRecord bid : bids) {
            offer = getBestOffer(bid);
            if(offer != null && (offer.house.owner != bid.buyer)) {
                offer.matchWith(bid);
            }
        }
        bids.clear();
    }

    /***********************************
     * The second step to clear the market.
     *
     * Iterate through all *offers* and, for each offer, loop through its matched bids.
     *
     * If BIDUP is implemented, the offer price is bid up according to a geometric distribution with
     * mean dependent on the number of matched bids.
     *
     */
    protected void clearMatches() {
        // --- clear and resolve oversubscribed offers
        // 
        HouseSaleRecord offer;
        GeometricDistribution geomDist;
        int nBids;
        double pSuccessfulBid;
        double salePrice;
        int winningBid;
        int enoughBids; // upper bounded number of bids on one house
        Iterator<HousingMarketRecord> record = offersIterator();
        while(record.hasNext()) {
            offer = (HouseSaleRecord)record.next();
            nBids = offer.matchedBids.size(); // if there are no bids matched, skip this offer
            if(nBids > 0) {
                // bid up the price
                if(config.BIDUP != 1.0) {
                    // TODO: the 10000/N factor, the 0.5 added, and the topping of the function at 4 are not declared in the paper. Remove or explain!
                    enoughBids = Math.min(4, (int)(0.5 + nBids*10000.0/config.TARGET_POPULATION));
                    pSuccessfulBid = Math.exp(-enoughBids*config.derivedParams.MONTHS_UNDER_OFFER);
                    geomDist = new GeometricDistribution(Model.rand, pSuccessfulBid);
                    salePrice = offer.getPrice() * Math.pow(config.BIDUP, geomDist.sample());
                } else {
                    salePrice = offer.getPrice();                    
                }
                // choose a bid above the new price
                Collections.sort(offer.matchedBids, new HouseBuyerRecord.PComparator()); // highest price last
                --nBids;
                if(offer.matchedBids.get(nBids).getPrice() < salePrice) {
                    salePrice = offer.matchedBids.get(nBids).getPrice();
                    winningBid = nBids;
                } else {
                    while(nBids >=0 && offer.matchedBids.get(nBids).getPrice() > salePrice) {
                        --nBids;
                    }
                    ++nBids;
                    winningBid = nBids + rand.nextInt(offer.matchedBids.size()-nBids);
                }
                record.remove();
                offer.setPrice(salePrice, authority);
                completeTransaction(offer.matchedBids.get(winningBid), offer);
                // put failed bids back on array
                bids.addAll(offer.matchedBids.subList(0, winningBid));
                bids.addAll(offer.matchedBids.subList(winningBid+1, offer.matchedBids.size()));            
            }
        }        
    }
    
    /**************************************************
     * Main simulation step.
     *
     * For a number of rounds, matches bids with offers and
     * clears the matches.
     * 
     **************************************************/
    public void clearMarket() {
        // offersPQ contains Price-Quality 2D-priority queue of offers
        // offersPY contains Price-Yeild 2D-priority queue of offers
        // bids contains bids (HouseBuyerRecords) in an array
        
        recordMarketStats();
        // TODO: 500 is reported in the paper as 5000000. In any case, why this number? Why the 1000?
        // TODO: These numbers should be made a less arbitrary or better justified "model rule", not even parameters
        // TODO: Also, why to necessarily iterate rounds times if market might be cleared before? Is this often the case?
        int rounds = Math.min(config.TARGET_POPULATION/1000,1 + (offersPQ.size()+bids.size())/500);
        for(int i=0; i<rounds; ++i) {
            matchBidsWithOffers(); // Step 1: iterate through bids
            clearMatches(); // Step 2: iterate through offers
        }
        bids.clear();
        /*
        // --- create matches
        HouseSaleRecord offer;
        for(HouseBuyerRecord bid : bids) {
            if(bid.getClass() == HouseBuyerRecord.class) { // OO buyer (quality driven)
                offer = (HouseSaleRecord)offersPQ.peek(bid);
            } else { // BTL buyer (yield driven)
                offer = (HouseSaleRecord)offersPY.peek(bid);
            }
            if(offer != null && (offer.house.owner != bid.buyer)) {
                offer.matchWith(bid);
            }
        }
        bids.clear();
        
        // --- clear and resolve oversubscribed offers
        // 
        GeometricDistribution geomDist;
        int nBids;
        double pSuccessfulBid;
        double salePrice;
        int winningBid;
        Iterator<HousingMarketRecord> record = offersPQ.iterator();
//        System.out.println("starting clearing");
        while(record.hasNext()) {
            offer = (HouseSaleRecord)record.next();
//            System.out.println("Offer quality "+offer.getQuality());
            nBids = offer.matchedBids.size();
            if(nBids > 0) {
                // bid up the price
                pSuccessfulBid = Math.exp(-nBids*Config.UNDEROFFER);
                geomDist = new GeometricDistribution(Model.rand, pSuccessfulBid);
                salePrice = offer.getPrice() * Math.pow(Config.BIDUP, geomDist.sample());
                // choose a bid above the new price
                Collections.sort(offer.matchedBids, new HouseBuyerRecord.PComparator()); // highest price last
                --nBids;
                if(offer.matchedBids.get(nBids).getPrice() < salePrice) {
                    salePrice = offer.matchedBids.get(nBids).getPrice();
                    winningBid = nBids;
                } else {
                    while(nBids >=0 && offer.matchedBids.get(nBids).getPrice() > salePrice) {
                        --nBids;
                    }
                    ++nBids;
                    winningBid = nBids + Model.rand.nextInt(offer.matchedBids.size()-nBids);
                }
                record.remove();
                offersPY.remove(offer);
                offer.setPrice(salePrice, authority);
                completeTransaction(offer.matchedBids.get(winningBid), offer);
                bids.addAll(offer.matchedBids.subList(0, winningBid));
                bids.addAll(offer.matchedBids.subList(winningBid+1, offer.matchedBids.size()));            
            }
        }        
        bids.clear();
        */
    }

        
    /**********************************************
     * Do all stuff necessary when a buyer and seller is matched
     * and the transaction is completed.
     * 
     * @param b The buyer's record
     * @param sale The seller's record
     **********************************************/
    public void completeTransaction(HouseBuyerRecord b, HouseSaleRecord sale) {
        // --- update sales statistics
        // TODO: This an exponential moving average with smoothing parameter E. Not explained in the paper!
        // TODO: No justification is given for the smoothing parameter... check how much it matters for the results
        averageDaysOnMarket = config.derivedParams.E*averageDaysOnMarket + (1.0-config.derivedParams.E)*config.constants.DAYS_IN_MONTH*(Model.getTime() - sale.tInitialListing);
        averageSalePrice[sale.getQuality()] = config.derivedParams.G*averageSalePrice[sale.getQuality()] + (1.0-config.derivedParams.G)*sale.getPrice();
        
//        housePriceRegression.addData(referencePrice(sale.getQuality()), sale.getPrice());
        aveSoldRefPrice += referencePrice(sale.getQuality());
        aveSoldPrice += sale.getPrice();
        nSold += 1;
        
        if(averageSalePrice[sale.getQuality()] < 0.0) {
            System.out.println("Average sale price "+sale.getQuality()+" is "+averageSalePrice[sale.getQuality()]);
        }
    }
    
    /***************************************************
     * Get the annualised appreciation in house price index
     * It compares the previous quarter (previous 3 months, to smooth changes) to the quarter nYears years before
     * (full years to avoid seasonal effects) to compute the geometric mean over the nYear years
     *
     * @param nYears number of years to average house price growth
     * @return Annualised appreciation
     ***************************************************/
    public double housePriceAppreciation(int nYears) {
        double HPI = (HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH - 1)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH - 2)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH - 3));
        double oldHPI = (HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH
                - nYears*config.constants.MONTHS_IN_YEAR - 1)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH
                - nYears*config.constants.MONTHS_IN_YEAR - 2)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH
                - nYears*config.constants.MONTHS_IN_YEAR - 3));
        return(Math.pow(HPI/oldHPI, 1.0/nYears) - 1.0);
    }
    
    /***********************************************
     * HPI reference price of a house for a given quality
     * 
     * @param quality quality of the house
    ************************************************/
    public abstract double referencePrice(int quality);

    /***
     * 
     * @param q the quality of the house
     * @return the average sale price of houses of the given quality
     */
    public double getAverageSalePrice(int q) {
        double price = averageSalePrice[q];
        if(price <= 0.0) {
            price = 0.01;
            System.out.println("Average sale price "+q+" is "+averageSalePrice[q]);
        }
        return(price);
    }
    
    /***
     * @param price
     * @return      The best quality of house you would expect to get
     * for the given price. If return value is -1, can't afford
     * even lowest quality house.
     */
    public int maxQualityGivenPrice(double price) {
        int q=config.N_QUALITY-1;
        while(q >= 0 && averageSalePrice[q] > price) --q;
        return(q);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    
    protected void recordMarketStats() {
        // --- House Price Index stuff
        // ---------------------------
        
        // ###### TODO: TEST!!!
        // --- calculate from avergeSalePrice from housePriceRegression
//        if(housePriceRegression.getN() > 4) {
        // TODO: Why only when nSold greater than 4? Undeclared in the paper!
        if(nSold > 4) {
//            housePriceRegression.regress();
//            double m = housePriceRegression.getSlope();
            // TODO: Is this a parameter? Could we remove it?
            double c = 0.0;//housePriceRegression.getIntercept();
            double m = aveSoldPrice/aveSoldRefPrice;
            aveSoldPrice = 0.0;
            aveSoldRefPrice = 0.0;
            nSold = 0;

            housePriceIndex = m;
//            quarterlyHPI.addValue(m);
            for(int q=0; q<config.N_QUALITY; ++q) {
                averageSalePrice[q] = config.MARKET_AVERAGE_PRICE_DECAY*averageSalePrice[q] + (1.0-config.MARKET_AVERAGE_PRICE_DECAY)*(m*referencePrice(q) + c);
//                averageSalePrice[q] = referencePrice(q)*quarterlyHPI.getMean();
            }
        }
//        housePriceRegression.clear();
        
        // --- calculate from averageSalePrice array
//        housePriceIndex = 0.0;
//        for(Double price : averageSalePrice) {
//            housePriceIndex += price; // assumes equal distribution of houses over qualities
//        }
//        housePriceIndex /= House.Config.N_QUALITY*data.HouseSaleMarket.HPI_REFERENCE;
        
        HPIRecord.addValue(housePriceIndex);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////

    //protected Map<House, HouseSaleRecord>     onMarket = new TreeMap<House, HouseSaleRecord>();

    PriorityQueue2D<HousingMarketRecord> offersPQ;
//    protected HashMap<HouseSaleRecord, ArrayList<HouseBuyerRecord> > matches;
    ArrayList<HouseBuyerRecord> bids;
    private static Authority authority = new Authority();

//    protected PriorityQueue<HouseBuyerRecord> buyers = new PriorityQueue<HouseBuyerRecord>();
    
    // ---- statistics
//    SimpleRegression housePriceRegression = new SimpleRegression(); // linear regression of (transaction price,reference price)
    private Model.MersenneTwister    rand = Model.rand;    // Passes the Model's random number generator to a private field
    public double aveSoldRefPrice = 0.0;
    public double aveSoldPrice = 0.0;
    public int nSold = 0;
    public double averageDaysOnMarket;
    protected double averageSalePrice[] = new double[config.N_QUALITY];
    public DescriptiveStatistics HPIRecord;
    public DescriptiveStatistics quarterlyHPI = new DescriptiveStatistics(3);
    public double housePriceIndex;
    public double dLogPriceMean;
    public double dLogPriceSD;
}
