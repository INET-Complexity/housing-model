package housing;

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
	public HouseSaleRecord(House h, double p) {
		house = h;
		setPrice(p);
		initialListedPrice = price;
		quality = house.quality;
		tInitialListing = Model.t;
	}
	
	/***********************************************
	 * Set the list price to a given value,
	 * rounded to the nearest penny.
	 * 
	 * @param p The list-price.
	 **********************************************/
	public void setPrice(double p) {
		price = Math.round(p*100.0)/100.0; // round to nearest penny
	}

//	public double doubleValue() {
//		return(currentPrice);
//	}
	
	@Override
	public int getQuality() {
		return(quality);
	}

	@Override
	public int getId() {
		return house.id;
	}
	
	public House 	house;
	public int		quality;
	public double 	initialListedPrice;
//	public double	price;
	public int		tInitialListing; // time of initial listing
	
	/************************************************
	 * order by quality then price
	 ************************************************/

}
