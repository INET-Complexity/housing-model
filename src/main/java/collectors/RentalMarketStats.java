package collectors;

import housing.Config;
import housing.HouseRentalMarket;
import housing.Model;

import java.util.Arrays;

/**************************************************************************************************
 * Class to collect rental market statistics
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class RentalMarketStats extends HousingMarketStats {

	//------------------//
	//----- Fields -----//
	//------------------//

	// General fields
	private HousingMarketStats  		housingMarketStats;
	private Config 						config = Model.config; // Passes the Model's configuration parameters object to a private field

	// Rental-specific variables computed after market clearing to keep the previous values during the clearing
	private double []                   avOccupancyPerQuality; // Average fraction of time a rental property stays rented for each quality band
	private double []                   avFlowYieldPerQuality; // Average gross rental yield for each quality band for properties rented out this month
	private double                      avFlowYield; // Average gross rental yield for properties rented out this month
	private double                      expAvFlowYield; // Exponential moving average of the average flow gross rental yield

	//------------------------//
	//----- Constructors -----//
	//------------------------//

	/**
	 * Initialises the rental market statistics collector
	 *
	 * @param housingMarketStats Reference to the housing market collector
	 * @param market Reference to the rental market
	 */
	public RentalMarketStats(HousingMarketStats housingMarketStats, HouseRentalMarket market) {
		super(market);
		this.housingMarketStats = housingMarketStats;
		referencePricePerQuality = new double[config.N_QUALITY];
		System.arraycopy(data.HouseSaleMarket.getReferenceRentalPricePerQuality(), 0, referencePricePerQuality, 0,
				config.N_QUALITY); // Copies reference rental prices from data/HouseSaleMarket
	}

	//-------------------//
	//----- Methods -----//
	//-------------------//

	//----- Rental-specific initialisation methods -----//

	/**
	 * This method extends the corresponding one at the HousingMarketStats class with some rental-specific variables.
	 * Sets initial values for all relevant variables to enforce a controlled first measure for statistics
	 */
	@Override
	public void init() {
		super.init();
		// Set initial value for all rental specific variables
		avOccupancyPerQuality = new double[config.N_QUALITY];
		Arrays.fill(avOccupancyPerQuality, 1.0);
		avFlowYieldPerQuality = new double[config.N_QUALITY];
		Arrays.fill(avFlowYieldPerQuality, config.RENT_GROSS_YIELD);
		avFlowYield = config.RENT_GROSS_YIELD;
		expAvFlowYield = config.RENT_GROSS_YIELD;
	}

	//----- Post-market-clearing methods -----//

	/**
	 * This method extends the corresponding one at the HousingMarketStats class with some rental-specific variables.
     * Updates several statistic records after bids have been matched by clearing the market.
	 */
	@Override
	public void postClearingRecord() {
		super.postClearingRecord();
		// Compute the rest of variables after market clearing...
		double avFlowYieldCount = 0; // Dummy counter
		for (int q = 0; q < config.N_QUALITY; q++) {
			// ... average fraction of time that a house of a given quality is occupied, based on average tenancy length
			// and exponential moving average of months that houses of this quality spend on the rental market
			avOccupancyPerQuality[q] = config.TENANCY_LENGTH_AVERAGE/(config.TENANCY_LENGTH_AVERAGE
					+ getExpAvMonthsOnMarketForQuality(q));
			// ... average flow gross rental yield per quality band (stick to previous value if no sales)
			if (housingMarketStats.getExpAvSalePriceForQuality(q) > 0) {
				avFlowYieldPerQuality[q] = getExpAvSalePriceForQuality(q)*config.constants.MONTHS_IN_YEAR
						*avOccupancyPerQuality[q]/housingMarketStats.getExpAvSalePriceForQuality(q);
			}
			// ... average flow gross rental yield (for all quality bands)
			avFlowYieldCount += avFlowYieldPerQuality[q]*getnSalesForQuality(q);
		}
		// If no new rentals, then avFlowYield keeps its previous value
		if (getnSales() > 0) {
			avFlowYield = avFlowYieldCount/getnSales();
		}
		// ... a short and a long term exponential moving average of the average flow gross rental yield
		expAvFlowYield = expAvFlowYield*config.derivedParams.K + (1.0 - config.derivedParams.K)*avFlowYield;
	}

	//----- Getter/setter methods -----//

	// Note that, for security reasons, getters should never give or use counter variables, as their value changes
	// during market clearing

	public double [] getAvOccupancyPerQuality() { return avOccupancyPerQuality; }
	public double getAvOccupancyForQuality(int quality) { return avOccupancyPerQuality[quality]; }
	public double [] getAvFlowYieldPerQuality() { return avFlowYieldPerQuality; }
	public double getAvFlowYieldForQuality(int quality) { return avFlowYieldPerQuality[quality]; }
	public double getAvFlowYield() { return avFlowYield; }
	public double getExpAvFlowYield() { return expAvFlowYield; }
}
