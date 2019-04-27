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
	private static LogNormalDistribution    priceDistribution = new LogNormalDistribution(config.HOUSE_PRICES_SCALE,
            config.HOUSE_PRICES_SHAPE);
	private static double []                referencePrices = setupReferencePrices();

    //-------------------//
    //----- Methods -----//
    //-------------------//

	/**
	 * @return referencePrices Array of doubles with the reference price for each quality band
	 */
	public static double [] getReferencePricePerQuality() { return referencePrices; }

    /**
     * @return rentalReferencePrices Array of doubles with the reference rental price for each quality band
     */
    // TODO: Replace this by actual data on rental prices!!!
    public static double [] getReferenceRentalPricePerQuality() {
        double [] rentalReferencePrices = new double[config.N_QUALITY];
        for (int i = 0; i < config.N_QUALITY; i++) {
            rentalReferencePrices[i] = referencePrices[i]
                    /(config.RENT_MAX_AMORTIZATION_PERIOD*config.constants.MONTHS_IN_YEAR);
        }
        return rentalReferencePrices;
    }

	/**
	 * Set up initial reference prices for each house quality
     */
	private static double [] setupReferencePrices() {
		double [] result = new double[config.N_QUALITY];
		for(int q = 0; q < config.N_QUALITY; ++q) {
			result[q] = priceDistribution.inverseCumulativeProbability((q + 0.5)/config.N_QUALITY);
		}
		return result;
	}
}
