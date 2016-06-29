package data;

import housing.House;

import org.apache.commons.math3.distribution.LogNormalDistribution;

/***
 * This class contains the reference values for HPI and HPI distributions, as well as reference
 * price for each house quality
 *
 * @author daniel
 */
public class HouseSaleMarket {
	public static final double INITIAL_HPI = 0.8;
	public static final double HPI_LOG_MEDIAN = Math.log(195000); // Median price from ONS: 2013 housse price index data tables table 34
	public static final double HPI_SHAPE = 0.555; // shape parameter for lognormal dist. ONS: 2013 house price index data tables table 34
	public static final double HPI_REFERENCE = Math.exp(HPI_LOG_MEDIAN + HPI_SHAPE*HPI_SHAPE/2.0); // Mean of reference house prices
	public static LogNormalDistribution listPriceDistribution = new LogNormalDistribution(HPI_LOG_MEDIAN, HPI_SHAPE);
//	public static LogNormalDistribution buyToLetDistribution  = new LogNormalDistribution(Math.log(3.44), 1.050); // No. of houses owned by buy-to-let investors Source: ARLA review and index Q2 2014
//	public static double P_INVESTOR = 0.04; 		// Prior probability of being (wanting to be) a property investor (should be 4%, 3% for stability for now)
//	public static double SEASONAL_VOL_ADJ = 0.2; // amplitude of seasonal oscillation of volume of sales on market (approximated from HM Revenue and Customs UK Property Transactions Count - July 2015)
	public static double [] refPrice = setupRefPrice();

	/***
	 * @param quality Quality of a house for sale
	 * @return Reference price for that quality
	 */
	static public double referencePrice(int quality) {
		return(refPrice[quality]);
	}

	/**
	 * @return Set up initial reference prices for each house quality
     */
	static public double [] setupRefPrice() {
		double [] result = new double[House.Config.N_QUALITY];
		for(int q=0; q<House.Config.N_QUALITY; ++q) {
			result[q] = INITIAL_HPI*listPriceDistribution.inverseCumulativeProbability((q+0.5)/House.Config.N_QUALITY);
		}
		return(result);
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
 * 
 * LTI ratio mean = median = 2.9, SD = 1.15 (2013) Home-movers (not FTB) (Bank of England data)
 * LTI ratio mean = 2.99, median = 3.04, SD = 1.15 (2014) Home-movers (not FTB) (Bank of England data)
 * LTI ratio mean = 3.32[3.37], median = 3.35[3.42], SD = 0.99[0.97] (2013[2014]) Home-movers (not FTB) (Bank of England data)
 * 
 */
}
