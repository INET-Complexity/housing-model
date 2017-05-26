package data;

import housing.Config;
import housing.House;

import housing.Model;
import org.apache.commons.math3.distribution.LogNormalDistribution;

/***
 * This class contains the reference values for HPI and HPI distributions, as well as reference
 * price for each house quality
 *
 * @author daniel
 */
public class HouseSaleMarket {

	// TODO: The whole of this class content can be easily moved to the HousingMarket or the HouseSaleMarket class in the housing package

	private static Config config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public static LogNormalDistribution listPriceDistribution = new LogNormalDistribution(config.derivedParams.HPI_LOG_MEDIAN, config.HPI_SHAPE);
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
		double [] result = new double[config.N_QUALITY];
		for(int q=0; q<config.N_QUALITY; ++q) {
		    // TODO: Why to discount these initial price distribution with INITIAL_HPI (which is < 1)?
			result[q] = config.INITIAL_HPI*listPriceDistribution.inverseCumulativeProbability((q+0.5)/config.N_QUALITY);
		}
		return(result);
	}
	
/*
 * NOTES ON AGGREGATE CALIBRATION
 * HMRC land transaction returns (stamp duty) a month: roughly 120000/month = 0.45% of pop
 * ((land an buildings transaction tax in scotland) not applicable to inheritance/gift)
 * Ratio of monthly sales to stock: roughly 0.11
 * Variation in stock of houses on market roughly +- 20%
 * Number of households: 26.7 million (2014 ONS Families and households)
 * 
 * 37% of households own with mortgage, 32% own outright (Social Trends:Housing 2011 ONS)
 * 
 * LTI ratio mean = median = 2.9, SD = 1.15 (2013) Home-movers (not FTB) (Bank of England data)
 * LTI ratio mean = 2.99, median = 3.04, SD = 1.15 (2014) Home-movers (not FTB) (Bank of England data)
 * LTI ratio mean = 3.32[3.37], median = 3.35[3.42], SD = 0.99[0.97] (2013[2014]) Home-movers (not FTB) (Bank of England data)
 * 
 */
}
