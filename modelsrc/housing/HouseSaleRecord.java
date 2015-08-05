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
	
	/***********************************************
	 * Construct a new record.
	 * 
	 * @param h The house that is for sale.
	 * @param p The initial list price for the house.
	 ***********************************************/
	public HouseSaleRecord(House h, double price) {
		super(price);
		house = h;
//		setPrice(p);
		initialListedPrice = price;
//		quality = house.quality;
		tInitialListing = Model.t;
		matchedBids = new ArrayList<>(8);
		yield = Model.rentalMarket.getExpectedGrossYield(house.getQuality());
	}
	
	/***********************************************
	 * Set the list price to a given value,
	 * rounded to the nearest penny.
	 * 
	 * @param p The list-price.
	 **********************************************/
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
	 * expected gross yield of this house (at time of initial listing
	 * as this needs to stay constant)
	 */
	@Override
	public double getYield() {
		return yield;
	}
	
	public void matchWith(HouseBuyerRecord bid) {
//		if(house.owner != bid.buyer) {
			matchedBids.add(bid);
//		}
	}
	
	public House 	house;
	public double 	initialListedPrice;
	public int		tInitialListing; // time of initial listing
	public ArrayList<HouseBuyerRecord> matchedBids;
	private double	yield;
}
