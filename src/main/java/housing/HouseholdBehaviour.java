package housing;

import java.io.Serializable;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;

/**************************************************************************************************
 * Class to implement the behavioural decisions made by households
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseholdBehaviour implements Serializable {

    //------------------//
    //----- Fields -----//
    //------------------//

    private Config                  config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister	        prng;
    private boolean                 BTLInvestor;
    private double                  BTLCapGainCoefficient; // Sensitivity of BTL investors to capital gain, 0.0 cares only about rental yield, 1.0 cares only about cap gain
    private double                  propensityToSave;
    private LogNormalDistribution   downpaymentDistFTB; // Size distribution for downpayments of first-time-buyers
    private LogNormalDistribution   downpaymentDistOO; // Size distribution for downpayments of owner-occupiers

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
	HouseholdBehaviour(MersenneTwister prng, double incomePercentile) {
		this.prng = prng;  // initialize the random number generator

        // Set downpayment distributions for both first-time-buyers and owner-occupiers
        downpaymentDistFTB = new LogNormalDistribution(this.prng, config.DOWNPAYMENT_FTB_SCALE, config.DOWNPAYMENT_FTB_SHAPE);
        downpaymentDistOO = new LogNormalDistribution(this.prng, config.DOWNPAYMENT_OO_SCALE, config.DOWNPAYMENT_OO_SHAPE);
	    // Compute propensity to save, so that it is constant for a given household
        propensityToSave = config.DESIRED_BANK_BALANCE_EPSILON * prng.nextGaussian();
        // Decide if household is a BTL investor and, if so, its tendency to seek capital gains or rental yields
		BTLCapGainCoefficient = 0.0;
        if(incomePercentile > config.MIN_INVESTOR_PERCENTILE &&
                prng.nextDouble() < config.getPInvestor()/config.MIN_INVESTOR_PERCENTILE) {
            BTLInvestor = true;
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
     * wealth distribution fits the ONS wealth data for Great Britain.
	 *
	 * @param bankBalance Household's liquid wealth
     * @param annualGrossTotalIncome Household's annual gross total income
	 */
	public double getDesiredConsumption(double bankBalance, double annualGrossTotalIncome) {
		return config.CONSUMPTION_FRACTION*Math.max(bankBalance - getDesiredBankBalance(annualGrossTotalIncome), 0.0);
	}

	/**
     * Minimum bank balance each household is willing to have at the end of the month for the whole population to match
     * the wealth distribution obtained from the household survey (LCFS). In particular, in line with the Wealth and
     * Assets Survey, we model the relationship between liquid wealth and gross annual income as log-normal. This
     * desired bank balance will be then used to determine non-essential consumption.
     * TODO: Relationship described as log-normal here but power-law implemented! Dan's version of article described the
     * TODO: the distributions of gross income and of liquid wealth as log-normal, not their relationship. Change paper!
     *
	 * @param annualGrossTotalIncome Household
     */
	public double getDesiredBankBalance(double annualGrossTotalIncome) {
		return Math.exp(config.DESIRED_BANK_BALANCE_ALPHA
                + config.DESIRED_BANK_BALANCE_BETA*Math.log(annualGrossTotalIncome) + propensityToSave);
	}

    //----- Owner-Occupier behaviour -----//

	/**
     * Desired purchase price used to decide whether to buy a house and how much to bid for it
     *
	 * @param monthlyGrossEmploymentIncome Monthly gross employment income of the household
	 */
	double getDesiredPurchasePrice(double monthlyGrossEmploymentIncome) {
	    // TODO: This product is generally so small that it barely has any impact on the results, need to rethink if
        // TODO: it is necessary and if this small value makes any sense
        double HPAFactor = config.BUY_WEIGHT_HPA*getLongTermHPAExpectation();
        // TODO: The capping of this factor intends to avoid negative and too large desired prices, the 0.9 is a
        // TODO: purely artificial fudge parameter. This formula should be reviewed and changed!
        if (HPAFactor > 0.9) HPAFactor = 0.9;
        // TODO: Note that wealth is not used here, but only monthlyGrossEmploymentIncome
		return config.BUY_SCALE*config.constants.MONTHS_IN_YEAR*monthlyGrossEmploymentIncome
				*Math.exp(config.BUY_EPSILON*prng.nextGaussian())
                /(1.0 - HPAFactor);
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
                - config.SALE_WEIGHT_DAYS_ON_MARKET*Math.log((Model.housingMarketStats.getExpAvDaysOnMarket()
                + 1.0)/(config.constants.DAYS_IN_MONTH + 1.0))
                + config.SALE_EPSILON*prng.nextGaussian();
        // TODO: ExpAv days on market could be computed for each quality band so as to use here only the correct one
        return Math.max(Math.exp(exponent), principal);
	}

	/**
     * This method implements a household's decision to sell their owner-occupied property. On average, households sell
     * owner-occupied houses every 11 years, due to exogenous reasons not addressed in the model. In order to prevent
     * an unrealistic build-up of housing stock and unrealistic fluctuations of the interest rate, we modify this
     * probability by introducing two extra factors, depending, respectively, on the number of houses per capita
     * currently on the market and its exponential moving average, and on the interest rate and its exponential moving
     * average. In this way, the long-term selling probability converges to 1/11.
     * TODO: This method includes 2 unidentified fudge parameters, DECISION_TO_SELL_HPC (houses per capita) and
     * TODO: DECISION_TO_SELL_INTEREST, which are explicitly explained otherwise in the manuscript. URGENT!
     * TODO: Basically, need to implement both exponential moving averages referred above
     *
	 * @return True if the owner-occupier decides to sell the house and false otherwise.
	 */
	boolean decideToSellHome() {
        // TODO: This if implies BTL agents never sell their homes, need to explain in paper!
        return !isPropertyInvestor() && (prng.nextDouble() < config.derivedParams.MONTHLY_P_SELL*(1.0
                + config.DECISION_TO_SELL_ALPHA*(config.DECISION_TO_SELL_HPC
                - (double)Model.houseSaleMarket.getnHousesOnMarket()/Model.households.size())
                + config.DECISION_TO_SELL_BETA*(config.DECISION_TO_SELL_INTEREST
                - Model.bank.getMortgageInterestRate())));
    }

	/**
	 * Decide amount to pay as initial downpayment
     *
	 * @param me the household
	 * @param housePrice the price of the house
     */
	double decideDownPayment(Household me, double housePrice) {
		if (me.getBankBalance() > housePrice*config.BANK_BALANCE_FOR_CASH_DOWNPAYMENT) {
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
	 * @param sale The HouseSaleRecord of the house that is on the market.
	 ********************************************************/
	double rethinkHouseSalePrice(HouseSaleRecord sale) {
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
	    return monthlyGrossEmploymentIncome*config.DESIRED_RENT_INCOME_FRACTION;
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
        // ...always keep at least one investment property
		if(me.nInvestmentProperties() < 2) return false;
        // ...don't sell while occupied by tenant
		if(!h.isOnRentalMarket()) return false;

        // Find the expected equity yield rate of this property as a weighted mix of both rental yield and capital gain
        // times the leverage
        // ...find the mortgage agreement for this property
        MortgageAgreement mortgage = me.mortgageFor(h);
        // ...find its current (fair market value) sale price
        double currentMarketPrice = Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality());
        // ...find equity, or assets minus liabilities
        double equity = Math.max(0.01, currentMarketPrice - mortgage.principal); // The 0.01 prevents possible divisions by zero later on
        // ...find the leverage on that mortgage (Assets divided by equity, or return on equity)
		double leverage = currentMarketPrice/equity;
        // ...find the expected rental yield of this property as its current rental price divided by its current (fair market value) sale price
		// TODO: ATTENTION ---> This rental yield is not accounting for expected occupancy... shouldn't it?
		double currentRentalYield = h.getRentalRecord().getPrice()*config.constants.MONTHS_IN_YEAR/currentMarketPrice;
        // ...find the mortgage rate (pounds paid a year per pound of equity)
		double mortgageRate = mortgage.nextPayment()*config.constants.MONTHS_IN_YEAR/equity;
        // ...finally, find expected equity yield, or yield on equity
		double expectedEquityYield;
		if(config.BTL_YIELD_SCALING) {
			expectedEquityYield = leverage*((1.0 - BTLCapGainCoefficient)*currentRentalYield
                    + BTLCapGainCoefficient*(Model.rentalMarketStats.getLongTermExpAvFlowYield()
					+ getLongTermHPAExpectation())) - mortgageRate;
		} else {
			expectedEquityYield = leverage*((1.0 - BTLCapGainCoefficient)*currentRentalYield
                    + BTLCapGainCoefficient*getLongTermHPAExpectation())
                    - mortgageRate;
		}
		// Compute a probability to keep the property as a function of the effective yield
		double pKeep = Math.pow(sigma(config.BTL_CHOICE_INTENSITY*expectedEquityYield),
                1.0/config.constants.MONTHS_IN_YEAR);
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
        // ...always decide to buy if owning no investment property yet
        if (me.nInvestmentProperties() < 1) { return true ; }
        // ...never buy (keep on saving) if bank balance is below the household's desired bank balance
        // TODO: This mechanism and its parameter are not declared in the article! Any reference for the value of the parameter?
        if (me.getBankBalance() < getDesiredBankBalance(me.getAnnualGrossTotalIncome())*config.BTL_CHOICE_MIN_BANK_BALANCE) { return false; }
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
        double expectedEquityYield;
        if(config.BTL_YIELD_SCALING) {
            expectedEquityYield = leverage*((1.0 - BTLCapGainCoefficient)*rentalYield
                    + BTLCapGainCoefficient*(Model.rentalMarketStats.getLongTermExpAvFlowYield()
                    + getLongTermHPAExpectation())) - mortgageRate;
        } else {
            expectedEquityYield = leverage*((1.0 - BTLCapGainCoefficient)*rentalYield
                    + BTLCapGainCoefficient*getLongTermHPAExpectation())
                    - mortgageRate;
        }
        // Compute the probability to decide to buy an investment property as a function of the expected equity yield
        // TODO: This probability has been changed to correctly reflect the conversion from annual to monthly
        // TODO: probability. This needs to be explained in the article
//        double pBuy = Math.pow(sigma(config.BTL_CHOICE_INTENSITY*expectedEquityYield),
//                1.0/config.constants.MONTHS_IN_YEAR);
        double pBuy = 1.0 - Math.pow((1.0 - sigma(config.BTL_CHOICE_INTENSITY*expectedEquityYield)),
                1.0/config.constants.MONTHS_IN_YEAR);
        // Return true or false as a random draw from the computed probability
        return prng.nextDouble() < pBuy;
    }

    double btlPurchaseBid(Household me) {
        // TODO: What is this 1.1 factor? Another fudge parameter? It prevents wealthy investors from offering more than
        // TODO: 10% above the average price of top quality houses. The effect of this is to prevent fast increases of
        // TODO: price as BTL investors buy all supply till prices are too high for everybody. Fairly unclear mechanism,
        // TODO: check for removal!
        return(Math.min(Model.bank.getMaxMortgage(me, false),
                1.1*Model.housingMarketStats.getExpAvSalePriceForQuality(config.N_QUALITY-1)));
    }

	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? 
	 * @param rbar exponential average rent for house of this quality
	 * @param d average days on market
	 * @param h house being offered for rent
	 */
	double buyToLetRent(double rbar, double d, House h) {
		// TODO: What? Where does this equation come from?
		final double beta = config.RENT_MARKUP/Math.log(config.RENT_EQ_MONTHS_ON_MARKET); // Weight of days-on-market effect

		double exponent = config.RENT_MARKUP + Math.log(rbar + 1.0)
                - beta*Math.log((d + 1.0)/(config.constants.DAYS_IN_MONTH + 1))
                + config.RENT_EPSILON * prng.nextGaussian();
		double result = Math.exp(exponent);
        // TODO: The following contains a fudge (config.RENT_MAX_AMORTIZATION_PERIOD) to keep rental yield up
		double minAcceptable = Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality())
                /(config.RENT_MAX_AMORTIZATION_PERIOD*config.constants.MONTHS_IN_YEAR);
		if (result < minAcceptable) result = minAcceptable;
		return result;

	}

	/**
	 * Update the demanded rent for a property
	 *
	 * @param sale the HouseSaleRecord of the property for rent
	 * @return the new rent
     */
	double rethinkBuyToLetRent(HouseSaleRecord sale) { return (1.0 - config.RENT_REDUCTION)*sale.getPrice(); }

    /**
     * Logistic function, sometimes called sigma function, 1/1+e^(-x)
     *
     * @param x Parameter of the sigma or logistic function
     */
    private double sigma(double x) { return 1.0/(1.0 + Math.exp(-1.0*x)); }

	/**
     * @return expectation value of HPI in one year's time divided by today's HPI
     */
	private double getLongTermHPAExpectation() {
		// Dampening or multiplier factor, depending on its value being <1 or >1, for the current trend of HPA when
		// computing expectations as in HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt (double)
		return(Model.housingMarketStats.getLongTermHPA()*config.HPA_EXPECTATION_FACTOR);
    }

    public double getBTLCapGainCoefficient() { return BTLCapGainCoefficient; }

    public boolean isPropertyInvestor() { return BTLInvestor; }
}
