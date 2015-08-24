package data;

import housing.House;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class HouseSaleMarket {
	public static final double HPI_LOG_MEDIAN = Math.log(195000); // Median price from ONS: 2013 housse price index data tables table 34
	public static final double HPI_SHAPE = 0.555; // shape parameter for lognormal dist. ONS: 2013 house price index data tables table 34
	public static final double HPI_REFERENCE = Math.exp(HPI_LOG_MEDIAN + HPI_SHAPE*HPI_SHAPE/2.0); // Mean of reference house prices
	public static LogNormalDistribution listPriceDistribution = new LogNormalDistribution(HPI_LOG_MEDIAN, HPI_SHAPE);
	public static LogNormalDistribution buyToLetDistribution  = new LogNormalDistribution(Math.log(3.44), 1.050); // No. of houses owned by buy-to-let investors Source: ARLA review and index Q2 2014
	public static double P_INVESTOR = 0.04; 		// Prior probability of being (wanting to be) a property investor (should be 4%, 3% for stability for now)
	public static double SEASONAL_VOL_ADJ = 0.2; // amplitude of seasonal oscillation of volume of sales on market (approximated from HM Revenue and Customs UK Property Transactions Count - July 2015)
	
	/***
	 * Based on current price distribution
	 * @param quality
	 * @return
	 */
	static public double referencePrice(int quality) {
		return(listPriceDistribution.inverseCumulativeProbability((quality+0.5)/House.Config.N_QUALITY));
	}
/*
 * NOTES ON AGGREGATE CALIBRATION
 * HMRC land transaction returns (stamp duty) a month: roughly 120000/month = 0.45% of pop
 * ((land an buildings transaction tax in scotland) not applicable to inheritance/gift)
 * Ratio of monthly sales to stock: roughly 0.11
 * Variation in stock of houses on market roughly +- 20%
 * Number of households: 26.7 million (2014 ONS Families and Households)
 * 
 * 37% of households own with mortgage, 32% own outright (Social Trends:Housing 2011 ONS)
 */
}
