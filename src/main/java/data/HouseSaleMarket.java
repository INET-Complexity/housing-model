package data;

import housing.Config;
import housing.Model;

import org.apache.commons.math3.distribution.LogNormalDistribution;

/**************************************************************************************************
 * Class to encapsulate the reference values for the housing price index, its distribution, as well
 * as reference prices for each house quality band
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseSaleMarket {

    //------------------//
    //----- Fields -----//
    //------------------//

	private static Config                   config = Model.config; // Passes the Model's configuration parameters object to a private field
    // TODO: Replace this theoretical distribution with an updated version or with the real frequencies
	private static LogNormalDistribution    listPriceDistribution
                                                    = new LogNormalDistribution(config.derivedParams.HPI_LOG_MEDIAN,
                                                            config.HPI_SHAPE);
	private static double []                refPrice = setupRefPrice();

    //-------------------//
    //----- Methods -----//
    //-------------------//

	/***
	 * @return refPrice Array of doubles with the reference price for each quality band
	 */
	public static double [] getReferencePricePerQuality() { return refPrice; }

    /***
     * @return rentalRefPrice Array of doubles with the reference rental price for each quality band
     */
    // TODO: Replace this by a proper reference rental prices!!!
    public static double [] getReferenceRentalPricePerQuality() {
        double [] rentalRefPrice = new double[config.N_QUALITY];
        for (int i = 0; i < config.N_QUALITY; i++) {
            rentalRefPrice[i] = refPrice[i]/(config.RENT_MAX_AMORTIZATION_PERIOD*config.constants.MONTHS_IN_YEAR);
        }
        return rentalRefPrice;
    }

	/**
	 * @return Set up initial reference prices for each house quality
     */
	private static double [] setupRefPrice() {
		double [] result = new double[config.N_QUALITY];
		for(int q = 0; q < config.N_QUALITY; ++q) {
			result[q] = listPriceDistribution.inverseCumulativeProbability((q + 0.5)/config.N_QUALITY);
		}
		return result;
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
