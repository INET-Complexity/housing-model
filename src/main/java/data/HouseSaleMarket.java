package data;

import housing.Config;
import housing.Model;

import org.apache.commons.math3.distribution.LogNormalDistribution;

/**************************************************************************************************
 * Class to encapsulate reference house prices from Land Registry Price Paid Data as a log-normal
 * distribution, as well as a reference price for each house quality band
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseSaleMarket {

    //------------------//
    //----- Fields -----//
    //------------------//

	private static Config                   config = Model.config; // Passes the Model's configuration parameters object to a private field
    // In the log-normal distribution, the 1st parameter is the scale (mean of the normally distributed logarithm of the
    // distribution), the 2nd parameter is the shape (standard deviation of the normally distributed natural logarithm
    // of the distribution)
	private static LogNormalDistribution    housePriceDistribution =
            new LogNormalDistribution(config.HOUSE_PRICES_SCALE, config.HOUSE_PRICES_SHAPE);
    private static LogNormalDistribution    rentPriceDistribution =
            new LogNormalDistribution(config.RENTAL_PRICES_SCALE, config.RENTAL_PRICES_SHAPE);
	private static double []                referencePrices = setupReferencePrices(housePriceDistribution);
    private static double []                referenceRentalPrices = setupReferencePrices(rentPriceDistribution);

    //-------------------//
    //----- Methods -----//
    //-------------------//

	/**
	 * @return referencePrices Array of doubles with the reference price for each quality band
	 */
	public static double [] getReferencePricePerQuality() { return referencePrices; }

    /**
     * @return referenceRentalPrices Array of doubles with the reference rental price for each quality band
     */
    public static double [] getReferenceRentalPricePerQuality() { return referenceRentalPrices; }

	/**
	 * Given a certain distribution of prices, set up initial reference prices for each quality band
     *
     * @param logNormalDist Price distribution to be used for setting up reference prices (whether sale or rental)
     */
	private static double [] setupReferencePrices(LogNormalDistribution logNormalDist) {
		double [] result = new double[config.N_QUALITY];
		for (int q = 0; q < config.N_QUALITY; ++q) {
			result[q] = logNormalDist.inverseCumulativeProbability((q + 0.5) / config.N_QUALITY);
		}
		return result;
	}
}
