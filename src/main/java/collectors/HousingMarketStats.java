package collectors;

import housing.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**************************************************************************************************
 * Class to collect regional sale market statistics
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HousingMarketStats extends CollectorBase {

	//------------------//
	//----- Fields -----//
	//------------------//

	// General fields
	private HousingMarket           market; // Declared HousingMarket so that it can accommodate both sale and rental markets
	private Config                  config = Model.config; // Passes the Model's configuration parameters object to a private field

	// Variables computed at initialisation
	double []                       referencePricePerQuality;

	// Variables computed before market clearing
	private int                     nBuyers;
    private int                     nBTLBuyers;
	private int                     nSellers;
    private int                     nNewSellers;
    private int                     nBTLSellers;
	private double                  sumBidPrices;
	private double                  sumOfferPrices;
	private double []               offerPrices;
	private double []               bidPrices;

	// Variables computed during market clearing, counters
	private int                     salesCount; // Dummy variable to count sales
	private int                     ftbSalesCount; // Dummy variable to count sales to first-time buyers
	private int                     btlSalesCount; // Dummy variable to count sales to buy-to-let investors
	private double                  sumSoldReferencePriceCount; // Dummy counter
	private double                  sumSoldPriceCount; // Dummy counter
	private double                  sumDaysOnMarketCount; // Dummy counter
	private double []               sumSalePricePerQualityCount; // Dummy counter
	private int []                  nSalesPerQualityCount; // Dummy counter

	// Variables computed after market clearing to keep the previous values during the clearing
	private int                     nSales; // Number of sales
	private int	                    nFTBSales; // Number of sales to first-time buyers
	private int	                    nBTLSales; // Number of sales to buy-to-let investors
	private int 	                nUnsoldNewBuild; // Accumulated number of new built properties still unsold after market clearing
	private double                  sumSoldReferencePrice; // Sum of reference prices for the qualities of properties sold this month
	private double                  sumSoldPrice; // Sum of prices of properties sold this month
	// TODO: This should probably be months, not days... also in HousingMarketStats
	private double                  sumDaysOnMarket; // Sum of the number of days on the market for properties sold this month
	private double []               sumSalePricePerQuality; // Sum of the price for each quality band for properties sold this month
	private int []                  nSalesPerQuality; // Number of sales for each quality band for properties sold this month

	// Other variables computed after market clearing
	private double                  expAvDaysOnMarket; // Exponential moving average of the number of days on the market
	private double []               expAvSalePricePerQuality; // Exponential moving average of the price for each quality band
	private double                  housePriceIndex;
	private DescriptiveStatistics   HPIRecord;
	private double                  annualHousePriceAppreciation;
	private double                  longTermHousePriceAppreciation;

	//------------------------//
	//----- Constructors -----//
	//------------------------//

	/**
	 * Initialises the regional sale market statistics collector
	 *
	 * @param market Reference to the sale or rental market of the region, depending on being called as a constructor
	 *               for this class or as part of the construction of a RegionalRentalMarketStats
	 */
	public HousingMarketStats(HousingMarket market) {
		setActive(true);
		this.market = market;
		referencePricePerQuality = new double[config.N_QUALITY];
		System.arraycopy(data.HouseSaleMarket.getReferencePricePerQuality(), 0, referencePricePerQuality, 0,
				config.N_QUALITY); // Copies reference prices from data/HouseSaleMarket into referencePricePerQuality
		HPIRecord = new DescriptiveStatistics(config.derivedParams.HPI_RECORD_LENGTH);
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- Initialisation methods -----//

    /**
     * Sets initial values for all relevant variables to enforce a controlled first measure for statistics
     */
    public void init() {
        // Set zero initial value for variables computed before market clearing
        nBuyers = 0;
        nSellers = 0;
        nUnsoldNewBuild = 0;
        sumBidPrices = 0.0;
        sumOfferPrices = 0.0;
        offerPrices = new double[nSellers];
        bidPrices = new double[nBuyers];

        // Set zero initial value for persistent variables whose count is computed during market clearing
        nSales = 0;
        nFTBSales = 0;
        nBTLSales = 0;
        sumSoldReferencePrice = 0;
        sumSoldPrice = 0;
        sumDaysOnMarket = 0;
        sumSalePricePerQuality = new double[config.N_QUALITY];
        nSalesPerQuality = new int[config.N_QUALITY];

        // Set initial values for other variables computed after market clearing
        expAvDaysOnMarket = config.constants.DAYS_IN_MONTH; // TODO: Make this initialisation explicit in the paper! Is 30 days similar to the final simulated value?
        expAvSalePricePerQuality = new double[config.N_QUALITY];
        System.arraycopy(referencePricePerQuality, 0, expAvSalePricePerQuality, 0,
                config.N_QUALITY); // Exponential averaging of prices is initialised from reference prices
        housePriceIndex = 1.0;
        for (int i = 0; i < config.derivedParams.HPI_RECORD_LENGTH; ++i) HPIRecord.addValue(1.0);
        annualHousePriceAppreciation = housePriceAppreciation(1);
        longTermHousePriceAppreciation = housePriceAppreciation(config.HPA_YEARS_TO_CHECK);
    }

    //----- Pre-market-clearing methods -----//

    /**
     * Computes pre-clearing statistics and resets counters to zero
     */
    public void preClearingRecord() {
        // Re-initialise to zero variables to be computed later on, during market clearing, counters
        salesCount = 0;
        ftbSalesCount = 0;
        btlSalesCount = 0;
        sumSoldReferencePriceCount = 0;
        sumSoldPriceCount = 0;
        sumDaysOnMarketCount = 0;
        sumSalePricePerQualityCount = new double[config.N_QUALITY];
        nSalesPerQualityCount = new int[config.N_QUALITY];

        // Re-initialise to zero variables computed before market clearing
        nBuyers = market.getBids().size();
        nBTLBuyers = 0;
        for (HouseBuyerRecord bid: market.getBids()) {
            if (bid.buyer.behaviour.isPropertyInvestor() && bid.buyer.getHome() != null) {
                nBTLBuyers++;
            }
        }
        nSellers = market.getOffersPQ().size();
        nNewSellers = 0;
        nBTLSellers = 0;
        for (HousingMarketRecord element: market.getOffersPQ()) {
            HouseSaleRecord offer = (HouseSaleRecord)element;
            if (offer.tInitialListing == Model.getTime()) {
                nNewSellers++;
            }
            if (offer.house.owner != Model.construction) {
                Household h = (Household)offer.house.owner;
                if (h.behaviour.isPropertyInvestor()) {
                    nBTLSellers++;
                }
            }
        }
        sumBidPrices = 0.0;
        sumOfferPrices = 0.0;
        offerPrices = new double[nSellers];
        bidPrices = new double[nBuyers];


        // Record bid prices and their average
        int i = 0;
        for(HouseBuyerRecord bid : market.getBids()) {
            sumBidPrices += bid.getPrice();
            bidPrices[i] = bid.getPrice();
            ++i;
        }

        // Record offer prices, their average, and the number of empty and new houses
        i = 0;
        for(HousingMarketRecord sale : market.getOffersPQ()) {
            sumOfferPrices += sale.getPrice();
            offerPrices[i] = sale.getPrice();
            ++i;
        }
    }

    //----- During-market-clearing methods -----//

    /**
     * This method updates the values of several counters every time a buyer and a seller are matched and the
     * transaction is completed. Note that only counter variables can be modified within this method
     *
     * @param purchase The HouseBuyerRecord of the buyer
     * @param sale The HouseSaleRecord of the house being sold
     */
    // TODO: Need to think if this method and recordTransaction can be joined in a single method!
    public void recordSale(HouseBuyerRecord purchase, HouseSaleRecord sale) {
        salesCount += 1;
        MortgageAgreement mortgage = purchase.buyer.mortgageFor(sale.house);
        if(mortgage != null) {
            if(mortgage.isFirstTimeBuyer) {
                ftbSalesCount += 1;
            } else if(mortgage.isBuyToLet) {
                btlSalesCount += 1;
            }
        }
        // TODO: Attention, calls to Model class should be avoided: need to pass transactionRecorder as constructor arg
        Model.transactionRecorder.recordSale(purchase, sale, mortgage, market);
    }

    /**
     * This method updates the values of several counters every time a buyer and a seller are matched and the
     * transaction is completed. Note that only counter variables can be modified within this method
     *
     * @param sale The HouseSaleRecord of the house being sold
     */
    public void recordTransaction(HouseSaleRecord sale) {
        sumDaysOnMarketCount += config.constants.DAYS_IN_MONTH*(Model.getTime() - sale.tInitialListing);
        sumSalePricePerQualityCount[sale.getQuality()] += sale.getPrice();
        nSalesPerQualityCount[sale.getQuality()]++;
        sumSoldReferencePriceCount += referencePricePerQuality[sale.getQuality()];
        sumSoldPriceCount += sale.getPrice();
    }

    //----- Post-market-clearing methods -----//

    /**
     * This method updates several statistic records after bids have been matched by clearing the market. The
     * computation of the HPI is included here. Note that reference prices from data are used for computing the HPI, and
     * thus the value for t=1 is not 1
     */
    public void postClearingRecord() {
        // Pass count value obtained during market clearing to persistent variables
        nSales = salesCount;
        nFTBSales = ftbSalesCount;
        nBTLSales = btlSalesCount;
        sumSoldReferencePrice = sumSoldReferencePriceCount;
        sumSoldPrice = sumSoldPriceCount;
        sumDaysOnMarket = sumDaysOnMarketCount;
        System.arraycopy(nSalesPerQualityCount, 0, nSalesPerQuality, 0, config.N_QUALITY);
        System.arraycopy(sumSalePricePerQualityCount, 0, sumSalePricePerQuality, 0, config.N_QUALITY);
        // Compute the rest of variables after market clearing...
        // ... exponential averages of days in the market and prices per quality band (only if there have been sales)
        if (nSales > 0) {
            expAvDaysOnMarket = config.derivedParams.E*expAvDaysOnMarket
                    + (1.0 - config.derivedParams.E)*sumDaysOnMarket/nSales;
        }
        for (int q = 0; q < config.N_QUALITY; q++) {
            if (nSalesPerQuality[q] > 0) {
                expAvSalePricePerQuality[q] = config.derivedParams.G*expAvSalePricePerQuality[q]
                        + (1.0 - config.derivedParams.G)*sumSalePricePerQuality[q]/nSalesPerQuality[q];
            }
        }
        // ... current house price index (only if there have been sales)
        if(nSales > 0) {
            housePriceIndex = sumSoldPrice/sumSoldReferencePrice;
        }
        // ... HPIRecord with the new house price index value
        HPIRecord.addValue(housePriceIndex);
        // ... current house price appreciation values (both annual and long term value)
        annualHousePriceAppreciation = housePriceAppreciation(1);
        longTermHousePriceAppreciation = housePriceAppreciation(config.HPA_YEARS_TO_CHECK);
        // ... relaxation of the price distribution towards the reference price distribution (described in appendix A3)
        for(int q = 0; q < config.N_QUALITY; q++) {
            expAvSalePricePerQuality[q] = config.MARKET_AVERAGE_PRICE_DECAY*expAvSalePricePerQuality[q]
                    + (1.0 - config.MARKET_AVERAGE_PRICE_DECAY)*(housePriceIndex*referencePricePerQuality[q]);
        }
        // ...record number of unsold new build houses
        nUnsoldNewBuild = 0;
        for(HousingMarketRecord sale : market.getOffersPQ()) {
            if(((HouseSaleRecord)sale).house.owner == Model.construction) nUnsoldNewBuild++;
        }
    }

    /**
     * This method computes the annualised appreciation in house price index by comparing the most recent quarter
     * (previous 3 months, to smooth changes) to the quarter nYears years before (full years to avoid seasonal effects)
     * and computing the geometric mean over that period
     *
     * @param nYears Integer with the number of years over which to average house price growth
     * @return Annualised house price appreciation over nYears years
     */
    private double housePriceAppreciation(int nYears) {
        double HPI = (HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH - 1)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH - 2)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH - 3));
        double oldHPI = (HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH
                - nYears*config.constants.MONTHS_IN_YEAR - 1)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH
                - nYears*config.constants.MONTHS_IN_YEAR - 2)
                + HPIRecord.getElement(config.derivedParams.HPI_RECORD_LENGTH
                - nYears*config.constants.MONTHS_IN_YEAR - 3));
        return(Math.pow(HPI/oldHPI, 1.0/nYears) - 1.0);
    }

    /**
     * This method computes the quarter on quarter appreciation in house price index by comparing the most recent
     * quarter (previous 3 months, to smooth changes) to the previous one and computing the percentage change
     *
     * @return Quarter on quarter house price growth
     */
    double getQoQHousePriceGrowth() {
        double HPI = HPIRecord.getElement(config.derivedParams.getHPIRecordLength() - 1)
                + HPIRecord.getElement(config.derivedParams.getHPIRecordLength() - 2)
                + HPIRecord.getElement(config.derivedParams.getHPIRecordLength() - 3);
        double oldHPI = HPIRecord.getElement(config.derivedParams.getHPIRecordLength() - 4)
                + HPIRecord.getElement(config.derivedParams.getHPIRecordLength() - 5)
                + HPIRecord.getElement(config.derivedParams.getHPIRecordLength() - 6);
        return(100.0*(HPI - oldHPI)/oldHPI);
    }

    //----- Getter/setter methods -----//

    // Note that, for security reasons, getters should never give counter variables, as their value changes during
    // market clearing

    // Getters for variables computed at initialisation
    public double [] getReferencePricePerQuality() { return referencePricePerQuality; }
    public double getReferencePriceForQuality(int quality) { return referencePricePerQuality[quality]; }

    // Getters for variables computed before market clearing
    int getnBuyers() { return nBuyers; }
    int getnBTLBuyers() { return nBTLBuyers; }
    int getnSellers() { return nSellers; }
    int getnNewSellers() { return nNewSellers; }
    int getnBTLSellers() { return nBTLSellers; }
    int getnUnsoldNewBuild() { return nUnsoldNewBuild; }
    double getSumBidPrices() { return sumBidPrices; }
    double getSumOfferPrices() { return sumOfferPrices; }
    double [] getOfferPrices() { return offerPrices; }
    double [] getBidPrices() { return bidPrices; }

    // Getters for variables computed after market clearing to keep the previous values during the clearing
    int getnSales() { return nSales; }
    int getnFTBSales() { return nFTBSales; }
    int getnBTLSales() { return nBTLSales; }
    double getSumSoldReferencePrice() { return sumSoldReferencePrice; }
    double getSumSoldPrice() { return sumSoldPrice; }
    double getSumDaysOnMarket() { return sumDaysOnMarket; }
    public double [] getSumSalePricePerQuality() { return sumSalePricePerQuality; }
    double getSumSalePriceForQuality(int quality) { return sumSalePricePerQuality[quality]; }
    public int [] getnSalesPerQuality() { return nSalesPerQuality; }
    int getnSalesForQuality(int quality) { return nSalesPerQuality[quality]; }

    // Getters for other variables computed after market clearing
    public double getExpAvDaysOnMarket() { return expAvDaysOnMarket; }
    public double [] getExpAvSalePricePerQuality() { return expAvSalePricePerQuality; }
    public double getExpAvSalePriceForQuality(int quality) { return expAvSalePricePerQuality[quality]; }
    double getExpAvSalePrice() {
        double sum = 0.0;
        int n = 0;
        for (double element: expAvSalePricePerQuality) {
            sum += element;
            n++;
        }
        return sum/n;
    }
    public double getHPI() { return housePriceIndex; }
    public DescriptiveStatistics getHPIRecord() { return HPIRecord; }
    double getAnnualHPA() { return annualHousePriceAppreciation; }
    public double getLongTermHPA() {return longTermHousePriceAppreciation; }

    // Getters for derived variables
    double getAvBidPrice() {
        if (nBuyers > 0) {
            return sumBidPrices/nBuyers;
        } else {
            return 0.0;
        }
    }
    double getAvOfferPrice() {
        if (nSellers > 0) {
            return sumOfferPrices/nSellers;
        } else {
            return 0.0;
        }
    }
    double getAvSalePrice() {
        if (nSales > 0) {
            return sumSoldPrice/nSales;
        } else {
            return 0.0;
        }
    }
    // Number of monthly sales that are to first-time buyers
    int getnSalesToFTB() { return nFTBSales; }
    // Number of monthly sales that are to buy-to-let investors
    int getnSalesToBTL() { return nBTLSales; }
    double getAvDaysOnMarket() {
        if (nSales > 0) {
            return sumDaysOnMarket/nSales;
        } else {
            return 0.0;
        }
    }
    public double [] getAvSalePricePerQuality() {
        double [] avSalePricePerQuality;
        avSalePricePerQuality = new double[config.N_QUALITY];
        for (int q = 0; q < config.N_QUALITY; q++) {
            if (nSalesPerQuality[q] > 0) {
                avSalePricePerQuality[q] = sumSalePricePerQuality[q]/nSalesPerQuality[q];
            } else {
                avSalePricePerQuality[q] = 0.0;
            }
        }
        return avSalePricePerQuality;
    }
    public double getAvSalePriceForQuality(int quality) {
        if (nSalesPerQuality[quality] > 0) {
            return sumSalePricePerQuality[quality]/nSalesPerQuality[quality];
        } else {
            return 0.0;
        }
    }
    /**
     * Computes the best quality of house that a buyer could expect to get for a given price. If return value is -1,
     * the buyer can't afford even lowest quality house.
     *
     * @param price Price the buyer is ready to pay
     */
    public int getMaxQualityForPrice(double price) {
        int q = config.N_QUALITY - 1;
        while(q >= 0 && getExpAvSalePriceForQuality(q) > price) --q;
        return q;
    }
}
