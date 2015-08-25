package housing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
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

	/**
	 * Configuration for the housing market.
	 * @author daniel
	 *
	 */
	static public class Config {
		public static final double UNDEROFFER = 7.0/30.0; // time (in months) that a house remains 'under offer'
		public static final double BIDUP = 1.015; // smallest proportion increase in price that can cause a gazump
		public static final double T = 0.02*Demographics.TARGET_POPULATION; // characteristic number of data-points over which to average market statistics
		public static final int HPI_LENGTH = 15; // Number of months to record HPI //F = Math.exp(-1.0/4.0); // House Price Index appreciation decay const (in market clearings)
		public static final double E = Math.exp(-1.0/T); // decay const for averaging days on market (in transactions)
		public static final double G = Math.exp(-House.Config.N_QUALITY/T); // Decay const for averageListPrice averaging (in transactions)
	}
	
	static public class Authority {
		private Authority() {}
	}

	
	public HousingMarket() {
		offersPQ = new PriorityQueue2D<>(new HousingMarketRecord.PQComparator());
		bids = new ArrayList<>(Demographics.TARGET_POPULATION/16);
		HPIRecord = new DescriptiveStatistics(Config.HPI_LENGTH);
		init();
	}
	
	public void init() {
		int i;
		for(i = 0; i<House.Config.N_QUALITY; ++i) {
			averageSalePrice[i] = referencePrice(i);
		}
		housePriceIndex = 1.0;
		lastHousePriceIndex = 1.0;
		averageDaysOnMarket = 30;
		for(i=0; i<Config.HPI_LENGTH; ++i) HPIRecord.addValue(1.0);
		offersPQ.clear();
//		matches.clear();
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
	 * @param h The house to change the price for.
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
	 * @param house The house to take off the market.
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
	
	protected HouseSaleRecord getBestOffer(HouseBuyerRecord bid) {
		return (HouseSaleRecord)offersPQ.peek(bid);
	}
	
	public Iterator<HousingMarketRecord> offersIterator() {
		return(offersPQ.iterator());
	}

	
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
			nBids = offer.matchedBids.size();
			if(nBids > 0) {
				// bid up the price
				enoughBids = Math.min(4, nBids);
				pSuccessfulBid = Math.exp(-enoughBids*Config.UNDEROFFER);
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
				offer.setPrice(salePrice, authority);
				completeTransaction(offer.matchedBids.get(winningBid), offer);
				// put failed bids back on array
				bids.addAll(offer.matchedBids.subList(0, winningBid));
				bids.addAll(offer.matchedBids.subList(winningBid+1, offer.matchedBids.size()));			
			}
		}		
	}
	
	/**************************************************
	 * Clears all current bids and offers on the housing market.
	 * 
	 **************************************************/
	public void clearMarket() {
		// offersPQ contains Price-Quality 2D-priority queue of offers
		// offersPY contains Price-Yeild 2D-priority queue of offers
		// bids contains bids (HouseBuyerRecords) in an array
		
		recordMarketStats();
		for(int i=0; i<2; ++i) {
			matchBidsWithOffers();
			clearMatches();
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
//		System.out.println("starting clearing");
		while(record.hasNext()) {
			offer = (HouseSaleRecord)record.next();
//			System.out.println("Offer quality "+offer.getQuality());
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
		averageDaysOnMarket = Config.E*averageDaysOnMarket + (1.0-Config.E)*30*(Model.getTime() - sale.tInitialListing);
		averageSalePrice[sale.getQuality()] = Config.G*averageSalePrice[sale.getQuality()] + (1.0-Config.G)*sale.getPrice();
		if(averageSalePrice[sale.getQuality()] < 0.0) {
			System.out.println("Average sale price "+sale.getQuality()+" is "+averageSalePrice[sale.getQuality()]);
		}
	}
	
	/***************************************************
	 * Get the annualised appreciation in house price index
	 * (Compares the previous quarter to the quarter last year to get rid of seasonality)
	 * 
	 * @return Annualised appreciation
	 ***************************************************/
	public double housePriceAppreciation() {
//		return((HPIRecord.getElement(Config.HPI_LENGTH-1)+HPIRecord.getElement(Config.HPI_LENGTH-2)+HPIRecord.getElement(Config.HPI_LENGTH-3))/
//				(HPIRecord.getElement(Config.HPI_LENGTH-13)+HPIRecord.getElement(Config.HPI_LENGTH-14)+HPIRecord.getElement(Config.HPI_LENGTH-15))
//				-1.0);
		return(HPIRecord.getElement(Config.HPI_LENGTH-1)/
				HPIRecord.getElement(Config.HPI_LENGTH-13)
				-1.0);
	}
	
	/***********************************************
	 * HPI reference price of a house for a given quality
	 * 
	 * @param q quality of the house
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
	 * @return 	 The best quality of house you would expect to get
	 * for the given price. If return value is -1, can't afford
	 * even lowest quality house.
	 */
	public int maxQualityGivenPrice(double price) {
		int q=House.Config.N_QUALITY-1;
		while(q >= 0 && averageSalePrice[q] > price) --q;
		return(q);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected void recordMarketStats() {
		// --- House Price Index stuff
		// ---------------------------
//		HPIAppreciation = Config.F*HPIAppreciation - (1.0-Config.F)*housePriceIndex;
		housePriceIndex = 0.0;
		for(Double price : averageSalePrice) {
			housePriceIndex += price; // TODO: assumes equal distribution of houses over qualities
		}
		housePriceIndex /= House.Config.N_QUALITY*data.HouseSaleMarket.HPI_REFERENCE;
//		HPIAppreciation += (1.0-Config.F)*housePriceIndex;
		HPIRecord.addValue(housePriceIndex);
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////

	//protected Map<House, HouseSaleRecord> 	onMarket = new TreeMap<House, HouseSaleRecord>();

	protected PriorityQueue2D<HousingMarketRecord>	offersPQ;
//	protected HashMap<HouseSaleRecord, ArrayList<HouseBuyerRecord> > matches;
	protected ArrayList<HouseBuyerRecord> bids;
	private static Authority authority = new Authority();

//	protected PriorityQueue<HouseBuyerRecord> buyers = new PriorityQueue<HouseBuyerRecord>();
	
	// ---- statistics
	public double averageDaysOnMarket;
	protected double averageSalePrice[] = new double[House.Config.N_QUALITY];
	public DescriptiveStatistics HPIRecord;
	public double housePriceIndex;
	public double lastHousePriceIndex;
	public double dLogPriceMean;
	public double dLogPriceSD;
}
