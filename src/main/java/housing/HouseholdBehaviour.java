package housing;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import collectors.RentalMarketStats;

import utilities.BinnedDataDouble;

/**************************************************************************************************
 * Class to implement the behavioural decisions made by households
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseholdBehaviour {

    //------------------//
    //----- Fields -----//
    //------------------//

    private static Config                   config = Model.config; // Passes the Model's configuration parameters object to a private static field
    private static MersenneTwister	        prng = Model.prng; // Passes the Model's random number generator to a private static field
    private static RentalMarketStats        rentalMarketStats = Model.rentalMarketStats; // Passes the Model's rental market stats object to a private static field
    private static LogNormalDistribution    downpaymentDistFTB = new LogNormalDistribution(prng,
            config.DOWNPAYMENT_FTB_SCALE, config.DOWNPAYMENT_FTB_SHAPE); // Size distribution for downpayments of first-time-buyers
    private static LogNormalDistribution    downpaymentDistOO = new LogNormalDistribution(prng,
            config.DOWNPAYMENT_OO_SCALE, config.DOWNPAYMENT_OO_SHAPE); // Size distribution for downpayments of owner-occupiers
    private static BinnedDataDouble         BTLProbability = new BinnedDataDouble(config.DATA_BTL_PROBABILITY);
    private boolean                         BTLInvestor;
    private double                          BTLCapGainCoefficient; // Sensitivity of BTL investors to capital gain, 0.0 cares only about rental yield, 1.0 cares only about cap gain
    private double                          propensityToSave;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	/**
	 * Initialise behavioural variables for a new household: propensity to save, whether the household will have the BTL
     * investor "gene" (provided its income percentile is above a certain minimum), and whether the household will be a
     * fundamentalist or a trend follower investor (provided it has received the BTL investor gene)
	 *
	 * @param incomePercentile Fixed income percentile for the household (assumed constant over a lifetime)
     */
	HouseholdBehaviour(double incomePercentile) {
	    // Compute propensity to save, so that it is constant for a given household
        propensityToSave = prng.nextDouble();
        // Decide if household is a BTL investor and, if so, its tendency to seek capital gains or rental yields
		BTLCapGainCoefficient = 0.0;
		// Decide whether the household will have a BTL tendency...
        if (prng.nextDouble() < config.BTL_PROBABILITY_MULTIPLIER*BTLProbability.getBinAt(incomePercentile)) {
            BTLInvestor = true;
            // ...and, if so, whether it will have a fundamentalist or a trend-following attitude
            if(prng.nextDouble() < config.P_FUNDAMENTALIST) {
                BTLCapGainCoefficient = config.FUNDAMENTALIST_CAP_GAIN_COEFF;
            } else {
                BTLCapGainCoefficient = config.TREND_CAP_GAIN_COEFF;
            }
        } else {
            BTLInvestor = false;
        }
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- General behaviour -----//

	/**
	 * Compute the monthly non-essential or optional consumption by a household. It is calibrated so that the output
     * wealth distribution fits the Wealth and Assets Survey wealth distribution as a function of income. Subtracting a
     * monthly disposable income from the target bank balance sets this as the distance from which the household starts
     * relaxing its saving behaviour and thus allowing for some non-essential consumption in such a way that its bank
     * balance will exponentially approach the target
	 *
	 * @param bankBalance Household's liquid wealth
     * @param annualGrossTotalIncome Household's annual gross total income
     * @param monthlyDisposableIncome Household's monthly disposable income (gross employment income + rental income
     *                                - taxes - essential consumption - housing expenses)
	 */
	double getDesiredConsumption(double bankBalance, double annualGrossTotalIncome, double monthlyDisposableIncome) {
		return 0.5*Math.max(bankBalance - data.Wealth.getDesiredBankBalance(annualGrossTotalIncome, propensityToSave)
                + monthlyDisposableIncome, 0.0);
	}

    //----- Owner-Occupier behaviour -----//

	/**
     * Desired purchase price used to decide whether to buy a house and how much to bid for it
     *
	 * @param annualGrossEmploymentIncome Annual gross employment income of the household
	 */
	double getDesiredPurchasePrice(double annualGrossEmploymentIncome) {
        // Note the capping of the HPA factor to a arbitrary maximum level (0.9) to avoid dividing by zero as well as
        // unrealistically large desired budgets
        double HPAFactor = Math.min(config.BUY_WEIGHT_HPA*getLongTermHPAExpectation(), 0.9);
		return config.BUY_SCALE * Math.pow(annualGrossEmploymentIncome, config.BUY_EXPONENT)
				* Math.exp(config.BUY_MU + config.BUY_SIGMA*prng.nextGaussian())
                / (1.0 - HPAFactor);
	}

	/**
     * Initial sale price of a house to be listed
     *
	 * @param quality Quality of the house ot be sold
	 * @param principal Amount of principal left on any mortgage on this house
	 */
	double getInitialSalePrice(int quality, double principal) {
        double exponent = config.SALE_MARKUP
                + Math.log(Model.housingMarketStats.getExpAvSalePriceForQuality(quality) + 1.0)
                - config.SALE_WEIGHT_MONTHS_ON_MARKET
                * Math.log(Model.housingMarketStats.getExpAvMonthsOnMarketForQuality(quality) + 1.0)
                + config.SALE_EPSILON*prng.nextGaussian();
        return Math.max(Math.exp(exponent), principal);
	}

	/**
     * This method implements a household's decision to sell their owner-occupied property. On average, households sell
     * owner-occupied houses every 11 years, due to exogenous reasons not addressed in the model.
     *
	 * @return True if the owner-occupier decides to sell the house and false otherwise.
	 */
	boolean decideToSellHome() {
        // TODO: This if implies BTL agents never sell their homes, need to explain in paper!
        return !isPropertyInvestor() && (prng.nextDouble() < config.derivedParams.MONTHLY_P_SELL);
    }

	/**
	 * Decide amount to pay as initial downpayment
     *
	 * @param me the household
	 * @param housePrice the price of the house
     */
	double decideDownPayment(Household me, double housePrice) {
		if (me.getBankBalance() > housePrice*config.DOWNPAYMENT_BANK_BALANCE_FOR_CASH_SALE) {
			return housePrice;
		}
		double downpayment;
		if (me.isFirstTimeBuyer()) {
		    // Since the function of the HPI is to move the down payments distribution upwards or downwards to
            // accommodate current price levels, and the distribution is itself aggregate, we use the aggregate HPI
			downpayment = Model.housingMarketStats.getHPI()*downpaymentDistFTB.inverseCumulativeProbability(Math.max(0.0,
                    (me.incomePercentile - config.DOWNPAYMENT_MIN_INCOME)/(1 - config.DOWNPAYMENT_MIN_INCOME)));
		} else if (isPropertyInvestor()) {
			downpayment = housePrice*(Math.max(0.0,
					config.DOWNPAYMENT_BTL_MEAN + config.DOWNPAYMENT_BTL_EPSILON * prng.nextGaussian()));
		} else {
			downpayment = Model.housingMarketStats.getHPI()*downpaymentDistOO.inverseCumulativeProbability(Math.max(0.0,
                    (me.incomePercentile - config.DOWNPAYMENT_MIN_INCOME)/(1 - config.DOWNPAYMENT_MIN_INCOME)));
		}
		if (downpayment > me.getBankBalance()) downpayment = me.getBankBalance();
		return downpayment;
	}

    ///////////////////////////////////////////////////////////
	///////////////////////// REVISED /////////////////////////
    ///////////////////////////////////////////////////////////

	/********************************************************
	 * Decide how much to drop the list-price of a house if
	 * it has been on the market for (another) month and hasn't
	 * sold. Calibrated against Zoopla dataset in Bank of England
	 * 
	 * @param sale The HouseOfferRecord of the house that is on the market.
	 ********************************************************/
	double rethinkHouseSalePrice(HouseOfferRecord sale) {
		if(prng.nextDouble() < config.P_SALE_PRICE_REDUCE) {
			double logReduction = config.REDUCTION_MU + (prng.nextGaussian()*config.REDUCTION_SIGMA);
			return(sale.getPrice()*(1.0 - Math.exp(logReduction)/100.0));
		}
		return(sale.getPrice());
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Renter behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/*** renters or OO after selling home decide whether to rent or buy
	 * N.B. even though the HH may not decide to rent a house of the
	 * same quality as they would buy, the cash value of the difference in quality
	 *  is assumed to be the difference in rental price between the two qualities.
	 *  @return true if we should buy a house, false if we should rent
	 */
    boolean decideRentOrPurchase(Household me, double purchasePrice) {
        if(isPropertyInvestor()) return(true);
        MortgageAgreement mortgageApproval = Model.bank.requestApproval(me, purchasePrice,
                decideDownPayment(me, purchasePrice), true);
        int newHouseQuality = Model.housingMarketStats.getMaxQualityForPrice(purchasePrice);
        if (newHouseQuality < 0) return false; // can't afford a house anyway
        double costOfHouse = mortgageApproval.monthlyPayment*config.constants.MONTHS_IN_YEAR
				- purchasePrice*getLongTermHPAExpectation();
        double costOfRent = Model.rentalMarketStats.getExpAvSalePriceForQuality(newHouseQuality)
                *config.constants.MONTHS_IN_YEAR;
        return prng.nextDouble() < sigma(config.SENSITIVITY_RENT_OR_PURCHASE*(costOfRent*(1.0
                + config.PSYCHOLOGICAL_COST_OF_RENTING) - costOfHouse));
    }

	/********************************************************
	 * Decide how much to bid on the rental market
	 * Source: Zoopla rental prices 2008-2009 (at Bank of England)
	 ********************************************************/
	double desiredRent(double monthlyGrossEmploymentIncome) {
	    return monthlyGrossEmploymentIncome*config.BID_RENT_AS_FRACTION_OF_INCOME;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Property investor behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Decide whether to sell or not an investment property. Investor households with only one investment property do
     * never sell it. A sale is never attempted when the house is occupied by a tenant. Households with at least two
     * investment properties will calculate the expected yield of the property in question based on two contributions:
     * rental yield and capital gain (with their corresponding weights which depend on the type of investor)
	 * 
	 * @param h The house in question
	 * @param me The investor household
	 * @return True if investor me decides to sell investment property h
	 */
	boolean decideToSellInvestmentProperty(House h, Household me) {
		// Fast decisions...
        // ...always keep at least one investment property (i.e., at least two properties)
		if (me.getNProperties() < 3) return false;
        // ...don't sell while occupied by tenant
		if (!h.isOnRentalMarket()) return false;

        // Find the expected equity yield rate of this property as a weighted mix of both rental yield and capital gain
        // times the leverage
        // ...find the mortgage agreement for this property
        MortgageAgreement mortgage = me.mortgageFor(h);
        // ...find its current (fair market value) sale price
        double currentMarketPrice = Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality());
        // ...find equity, or assets minus liabilities
        double equity = Math.max(0.01, currentMarketPrice - mortgage.principal); // The 0.01 prevents possible divisions by zero later on
        // ...find the leverage on that mortgage (Assets divided by equity, or return on equity)
		double leverage = currentMarketPrice / equity;
        // ...find the expected rental yield of this property as its current rental price (under current average occupancy) divided by its current (fair market value) sale price
        double currentRentalYield = h.getRentalRecord().getPrice() * config.constants.MONTHS_IN_YEAR
                * rentalMarketStats.getAvOccupancyForQuality(h.getQuality()) / currentMarketPrice;
        // ...find the mortgage rate (pounds paid a year per pound of equity)
		double mortgageRate = mortgage.nextPayment() * config.constants.MONTHS_IN_YEAR / equity;
        // ...finally, find expected equity yield, or yield on equity
		double expectedEquityYield = leverage * ((1.0 - BTLCapGainCoefficient) * currentRentalYield
				+ BTLCapGainCoefficient * getLongTermHPAExpectation())
                - mortgageRate;
		// Compute a probability to keep the property as a function of the effective yield
		double pKeep = Math.pow(sigma(config.BTL_CHOICE_INTENSITY * expectedEquityYield),
                1.0 / config.constants.MONTHS_IN_YEAR);
		// Return true or false as a random draw from the computed probability
		return prng.nextDouble() < (1.0 - pKeep);
	}

    /**
     * Decide whether to buy or not a new investment property. Investor households with no investment properties always
     * attempt to buy one. If the household's bank balance is below its desired bank balance, then no attempt to buy is
     * made. If the resources available to the household (maximum mortgage) are below the average price for the lowest
     * quality houses, then no attempt to buy is made. Households with at least one investment property will calculate
     * the expected yield of a new property based on two contributions: rental yield and capital gain (with their
     * corresponding weights which depend on the type of investor)
     *
     * @param me The investor household
     * @return True if investor me decides to try to buy a new investment property
     */
    boolean decideToBuyInvestmentProperty(Household me) {
        // Fast decisions...
        // ...always decide to buy if owning no investment property yet (i.e., if owning only one property, a home)
        if (me.getNProperties() < 2) { return true ; }
        // ...never buy (keep on saving) if bank balance is below the household's desired bank balance
        // TODO: This mechanism and its parameter are not declared in the article! Any reference for the value of the parameter?
        if (me.getBankBalance() < data.Wealth.getDesiredBankBalance(me.getAnnualGrossTotalIncome(),
                me.behaviour.getPropensityToSave())*config.BTL_CHOICE_MIN_BANK_BALANCE) { return false; }
        // ...find maximum price (maximum mortgage) the household could pay
        double maxPrice = Model.bank.getMaxMortgage(me, false);
        // ...never buy if that maximum price is below the average price for the lowest quality
        if (maxPrice < Model.housingMarketStats.getExpAvSalePriceForQuality(0)) { return false; }

        // Find the expected equity yield rate for a hypothetical house maximising the leverage available to the
        // household and assuming an average rental yield (over all qualities). This is found as a weighted mix of both
        // rental yield and capital gain times the leverage
        // ...find mortgage with maximum leverage by requesting maximum mortgage with minimum downpayment
        MortgageAgreement mortgage = Model.bank.requestApproval(me, maxPrice, 0.0, false);
        // ...find equity, or assets minus liabilities (which, initially, is simply the downpayment)
        double equity = Math.max(0.01, mortgage.downPayment); // The 0.01 prevents possible divisions by zero later on
        // ...find the leverage on that mortgage (Assets divided by equity, or return on equity)
        double leverage = mortgage.purchasePrice/equity;
        // ...find the expected rental yield as an (exponential) average over all house qualities
        double rentalYield = Model.rentalMarketStats.getExpAvFlowYield();
        // ...find the mortgage rate (pounds paid a year per pound of equity)
        double mortgageRate = mortgage.nextPayment()*config.constants.MONTHS_IN_YEAR/equity;
        // ...finally, find expected equity yield, or yield on equity
        double expectedEquityYield = leverage*((1.0 - BTLCapGainCoefficient)*rentalYield
                + BTLCapGainCoefficient*getLongTermHPAExpectation())
                - mortgageRate;
        // Compute the probability to decide to buy an investment property as a function of the expected equity yield
        double pBuy = 1.0 - Math.pow((1.0 - sigma(config.BTL_CHOICE_INTENSITY*expectedEquityYield)),
                1.0/config.constants.MONTHS_IN_YEAR);
        // Return true or false as a random draw from the computed probability
        return prng.nextDouble() < pBuy;
    }

	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? To make a decision, the household will
     * check current exponential average rent prices for houses of the same quality, and the current exponential average
     * time on market for houses of the same quality.
     *
	 * @param quality The quality of the house
	 */
	double buyToLetRent(int quality) {
		// TODO: What? Where does this equation come from?
		final double beta = config.RENT_MARKUP/Math.log(config.RENT_EQ_MONTHS_ON_MARKET); // Weight of months-on-market effect
		double exponent = config.RENT_MARKUP
                + Math.log(Model.rentalMarketStats.getExpAvSalePriceForQuality(quality) + 1.0)
                - beta*Math.log(Model.rentalMarketStats.getExpAvMonthsOnMarketForQuality(quality) + 1.0)
                + config.RENT_EPSILON * prng.nextGaussian();
		double result = Math.exp(exponent);
        // TODO: The following contains a clamp for rent prices to be at least 12*RENT_MAX_AMORTIZATION_PERIOD times
        // TODO: below sale prices, thus setting also a minimum rental yield
//		double minAcceptable = Model.housingMarketStats.getExpAvSalePriceForQuality(quality)
//                /(config.RENT_MAX_AMORTIZATION_PERIOD*config.constants.MONTHS_IN_YEAR);
//		if (result < minAcceptable) result = minAcceptable;
		return result;
	}

	/**
	 * Update the demanded rent for a property
	 *
	 * @param sale the HouseOfferRecord of the property for rent
	 * @return the new rent
     */
	double rethinkBuyToLetRent(HouseOfferRecord sale) { return (1.0 - config.RENT_REDUCTION)*sale.getPrice(); }

    /**
     * Logistic function, sometimes called sigma function, 1/1+e^(-x)
     *
     * @param x Parameter of the sigma or logistic function
     */
    private double sigma(double x) { return 1.0/(1.0 + Math.exp(-1.0*x)); }

	/**
     * Expectations of future house price growth are based on previous trend (longTermHPA), times a dampening or
     * multiplier factor (depending on its value being <1 or >1), plus a constant (which can be positive or negative),
     * according to the equation HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt + CONST
     *
     * @return Expectation of HPI in one year's time divided by today's HPI
     */
	private double getLongTermHPAExpectation() {
		return Model.housingMarketStats.getLongTermHPA() * config.HPA_EXPECTATION_FACTOR + config.HPA_EXPECTATION_CONST;
    }

    public double getBTLCapGainCoefficient() { return BTLCapGainCoefficient; }

    public boolean isPropertyInvestor() { return BTLInvestor; }

    double getPropensityToSave() { return propensityToSave; }
}
