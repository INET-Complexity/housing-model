package data;

public class HouseRentalMarket {
	public static double RENT_PROFIT_MARGIN = 0.05; // profit margin for buy-to-let investors 
	// Yield on rent had average 6% between 2009/01 and 2015/01, 
	// minimum in 2009/10 maximum in 2012/04 peak-to-peak amplitude of 0.4%
	// source: Bank of England, unpublished analysis based on Zoopla/Land reg matching, Philippe Bracke 
	public static int AVERAGE_TENANCY_LENGTH = 18; // average number of months a tenant will stay in a rented house. Source: ARLA - Members survey of the Private Rented Sector Q4 2013
	
	static public double referencePrice(int quality) {
		return(HouseSaleMarket.referencePrice(quality)*RENT_PROFIT_MARGIN/12.0);
	}
	
/*
 * NOTES ON AGGREGATE DATA:
 * 57% of home moves in 2008/09 were private renters English Housing Survey report 08-09
 * 13% of households privately rent / 18% social housing (Social Trends:Housing 2011 ONS)
 */
}
