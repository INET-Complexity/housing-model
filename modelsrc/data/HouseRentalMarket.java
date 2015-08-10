package data;

public class HouseRentalMarket {
	
	/**
	 * Based of 3% gross yield
	 */
	static public double referencePrice(int quality) {
		return(HouseSaleMarket.referencePrice(quality)*0.03/12.0);
	}
/*
 * NOTES ON AGGREGATE DATA:
 * 57% of home moves in 2008/09 were private renters English Housing Survey report 08-09
 */
}
