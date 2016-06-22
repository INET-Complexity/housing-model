package housing;

import java.util.ArrayList;
import java.util.Comparator;

/*************************************************
 * This is the record containing information on a house that is for-sale.
 * Think of this as the record that an estate-agent keeps on a customer
 * that is selling a house.
 * 
 * @author daniel
 *
 *************************************************/
public class HouseSaleRecord extends HousingMarketRecord {
	private static final long serialVersionUID = 8626260055548234106L;
	/***********************************************
	 * Construct a new record.
	 * 
	 * @param h The house that is for sale.
	 * @param price The initial list price for the house.
	 ***********************************************/
	public HouseSaleRecord(House h, double price) {
		super(price);
		house = h;
//		setPrice(p);
		initialListedPrice = price;
//		quality = house.quality;
		tInitialListing = Model.getTime();
		matchedBids = new ArrayList<>(8); // Why 8 bids?
		recalcYield();
	}
	
//	/***********************************************
//	 * Set the list price to a given value,
//	 * rounded to the nearest penny.
//	 *
//	 * @param p The list-price.
//	 **********************************************/
//	public void setPrice(double p) {
//		price = Math.round(p*100.0)/100.0; // round to nearest penny
//	}

//	public double doubleValue() {
//		return(currentPrice);
//	}
	
	@Override
	public int getQuality() {
		return(house.getQuality());
	}
	
	/***
	 * expected gross yield of this house (including expected vacancy period)
	 */
	@Override
	public double getYield() {
		return yield;
	}
	
	/*** returns gross yield */
//	public double getGrossYield() {
//		return(getExpectedAnnualRent()/getPrice());
//	}
	
	public double getExpectedAnnualRent() {
		return(Model.rentalMarket.getAverageSalePrice(house.getQuality())*12.0);
	}

	public void setPrice(double newPrice, HousingMarket.Authority auth) {
		super.setPrice(newPrice, auth);
		recalcYield();
	}

	
	public void matchWith(HouseBuyerRecord bid) {
//		if(house.owner != bid.buyer) {
			matchedBids.add(bid);
//		}
	}
	
	protected void recalcYield() {
		int q = house.getQuality();
		yield = Model.rentalMarket.getExpectedGrossYield(q)*Model.housingMarket.getAverageSalePrice(q)/getPrice();		
	}
	
	public House 	house;
	public double 	initialListedPrice;
	public int		tInitialListing; // time of initial listing
	public ArrayList<HouseBuyerRecord> matchedBids;
	private double	yield;
}
