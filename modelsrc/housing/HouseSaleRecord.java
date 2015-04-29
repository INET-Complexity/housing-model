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
public class HouseSaleRecord implements Comparable<HouseSaleRecord> {
	
	/***********************************************
	 * Construct a new record.
	 * 
	 * @param h The house that is for sale.
	 * @param p The initial list price for the house.
	 ***********************************************/
	public HouseSaleRecord(House h, double p) {
		house = h;
		setPrice(p);
		initialListedPrice = currentPrice;
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
		currentPrice = Math.round(p*100.0)/100.0; // round to nearest penny
	}

//	public double doubleValue() {
//		return(currentPrice);
//	}

	public House 	house;
	public int		quality;
	public double 	initialListedPrice;
	public double	currentPrice;
	public int		tInitialListing; // time of initial listing
	
	/************************************************
	 * order by quality then price
	 ************************************************/
	@Override
	public int compareTo(HouseSaleRecord o) {
		double diff = quality - o.quality;
		if(diff == 0.0) {
			diff = o.currentPrice - currentPrice;
			if(diff == 0.0) {
				diff = house.id - o.house.id;
			}
		}
		return((int)Math.signum(diff));
	}
	
	static public class PriceComparator implements Comparator<HouseSaleRecord> {
		@Override
		public int compare(HouseSaleRecord arg0, HouseSaleRecord arg1) {
			double diff = arg0.currentPrice - arg1.currentPrice;
			if(diff == 0.0) {
				diff = arg0.quality - arg1.quality;
				if(diff == 0.0) {
					diff = arg0.house.id - arg1.house.id;
				}
			}
			return((int)Math.signum(diff));
		}
		
	}
}
