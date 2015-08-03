package housing;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.LogNormalDistribution;

/**********************************************************
 * Implementation of the mechanism of the house-sale and
 * house-rental markets.
 * 
 * See model description for details.
 * 
 * @author daniel
 *
 *********************************************************/
public class HousingMarket {

	/**
	 * Configuration for the housing market.
	 * @author daniel
	 *
	 */
	static public class Config {
		public static final double HPI_LOG_MEDIAN = Math.log(195000); // Median price from ONS: 2013 housse price index data tables table 34
		public static final double HPI_SHAPE = 0.555; // shape parameter for lognormal dist. ONS: 2013 house price index data tables table 34
		public static final double HPI_MEAN = Math.exp(HPI_LOG_MEDIAN + HPI_SHAPE*HPI_SHAPE/2.0);
		public static LogNormalDistribution listPriceDistribution = new LogNormalDistribution(HPI_LOG_MEDIAN, HPI_SHAPE);
		public static final double T = 200.0; // characteristic number of data-points over which to average market statistics
		public static final double F = Math.exp(-1.0/12.0); // House Price Index appreciation decay const (in market clearings)
		public static final double E = Math.exp(-1.0/T); // decay const for averaging days on market
		public static final double G = Math.exp(-1.0/8); // Decay const for averageListPrice averaging
	}

	
	public HousingMarket() {
		init();
	}
	
	public void init() {
		int i;
		for(i = 0; i<House.Config.N_QUALITY; ++i) {
			averageSalePrice[i] = referencePrice(i);
		}
		housePriceIndex = 1.0;
		lastHousePriceIndex = 1.0;
		HPIAppreciation = 0.0;
		averageDaysOnMarket = 30;
		offersPQ.clear();
		offersPY.clear();
		matches.clear();
	}
	
	/******************************************
	 * Put a new offer on the market.
	 * @param house House to put on the market
	 * @param price List price for the house.
	 ******************************************/
	public HouseSaleRecord offer(House house, double price) {
		HouseSaleRecord hsr = new HouseSaleRecord(house, price);
		offersPQ.add(hsr);
		offersPY.add(hsr);
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
		offersPY.remove(hsr);
		hsr.price = newPrice;
		offersPQ.add(hsr);
		offersPY.add(hsr);
	}
	
	/*******************************************
	 * Take a house off the market.
	 * 
	 * @param house The house to take off the market.
	 *******************************************/
	public void removeOffer(HouseSaleRecord hsr) {
		offersPQ.remove(hsr);
		offersPY.remove(hsr);
	}

	/*******************************************
	 * Make a bid on the market (i.e. make an offer on
	 * a (yet to be decided) house).
	 * 
	 * @param buyer The household that is making the bid.
	 * @param price The price that the household is willing to pay.
	 ******************************************/
	public void bid(Household buyer, double price) {
//		buyers.add(new HouseBuyerRecord(buyer, price));
		// match bid with current offers
	}

	/**************************************************
	 * Get information on a given house that is on the market.
	 * @param h House we're interested in.
	 * @return The sale-record for the given house (NULL if not on the market)
	 *************************************************/
	public HouseSaleRecord getSaleRecord(House h) {
		return(onMarket.get(h));
	}
	
	/**************************************************
	 * Clears all current bids and offers on the housing market.
	 * 
	 **************************************************/
	public void clearMarket() {
		// onMarket contains offers (House->HouseSaleRecords)
		// buyers contains bids (HouseBuyerRecords)
		
		
		
		/***
		HouseBuyerRecord buyer;
		HouseSaleRecord  seller;
		HouseSaleRecord	 ceilingSeller = new HouseSaleRecord(new House(), 0.0);

		recordMarketStats();

		// --- create set of sellers, sorted by quality then price
		// --- (TODO: better computational complexity with R-tree (or KD-tree))
		// ---
		TreeSet<HouseSaleRecord> sellers = new TreeSet<HouseSaleRecord>();
		for(HouseSaleRecord sale : onMarket.values()) {
			sellers.add(sale);
		}
		
		while(!buyers.isEmpty()) {
			buyer = buyers.poll();
			ceilingSeller.quality = House.Config.N_QUALITY;
			seller = sellers.lower(ceilingSeller); // cheapest seller at this quality
			while(seller != null && 
				(seller.price > buyer.price || seller.house.owner == buyer.buyer)) {
				ceilingSeller.quality = seller.quality-1;
				seller = sellers.lower(ceilingSeller); // cheapest seller at this quality
			}
			if(seller != null) {
				removeOffer(seller.house);
				completeTransaction(buyer, seller);
				sellers.remove(seller);
			}
		}
		***/
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
		averageDaysOnMarket = Config.E*averageDaysOnMarket + (1.0-Config.E)*30*(Model.t - sale.tInitialListing);
		averageSalePrice[sale.quality] = Config.G*averageSalePrice[sale.quality] + (1.0-Config.G)*sale.price;
	}
	
	/***************************************************
	 * Get the annualised appreciation in house price index (HPI is compared to the
	 * reference HPI_MEAN)
	 * 
	 * @return Annualised appreciation
	 ***************************************************/
	public double housePriceAppreciation() {
		return(12.0*HPIAppreciation);
	}
	
	/***********************************************
	 * HPI reference price of a house for a given quality
	 * 
	 * @param q quality of the house
	************************************************/
	static public double referencePrice(int q) {
		return(Config.listPriceDistribution.inverseCumulativeProbability((q+0.5)/House.Config.N_QUALITY) * 0.9);
	}

	/***
	 * 
	 * @param q the quality of the house
	 * @return the average sale price of houses of the given quality
	 */
	public double getAverageSalePrice(int q) {
		return(averageSalePrice[q]);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected void recordMarketStats() {
		// --- House Price Index stuff
		// ---------------------------
		HPIAppreciation = Config.F*HPIAppreciation - (1.0-Config.F)*housePriceIndex;
		housePriceIndex = 0.0;
		for(Double price : averageSalePrice) {
			housePriceIndex += price; // TODO: assumes equal distribution of houses over qualities
		}
		housePriceIndex /= House.Config.N_QUALITY*Config.HPI_MEAN;
		HPIAppreciation += (1.0-Config.F)*housePriceIndex;

	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	public double getHPIAppreciation() {
		return HPIAppreciation;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////

	//protected Map<House, HouseSaleRecord> 	onMarket = new TreeMap<House, HouseSaleRecord>();

	protected PriorityQueue2D<HouseSaleRecord>	offersPQ;
	protected PriorityQueue2D<HouseSaleRecord>	offersPY;	
	protected HashMap<HouseSaleRecord, HouseBuyerRecord> matches;

//	protected PriorityQueue<HouseBuyerRecord> buyers = new PriorityQueue<HouseBuyerRecord>();
	
	// ---- statistics
	public double averageDaysOnMarket;
	protected double averageSalePrice[] = new double[House.Config.N_QUALITY];
	public double HPIAppreciation;
	public double housePriceIndex;
	public double lastHousePriceIndex;
	
}
