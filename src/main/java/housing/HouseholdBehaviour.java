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
	private static final long serialVersionUID = -7785886649432814279L;

    //------------------//
    //----- Fields -----//
    //------------------//

    private Config                  config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister	        rand = Model.rand; // Passes the Model's random number generator to a private field
    private boolean                 BTLInvestor;
    private double                  BTLCapGainCoefficient; // Sensitivity of BTL investors to capital gain, 0.0 cares only about rental yield, 1.0 cares only about cap gain
    private double                  propensityToSave;
    private double                  desiredBankBalance; // TODO: Do we even need this variable?
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
	HouseholdBehaviour(double incomePercentile) {
        // Set downpayment distributions for both first-time-buyers and owner-occupiers
        downpaymentDistFTB = new LogNormalDistribution(rand, config.DOWNPAYMENT_FTB_SCALE,
                config.DOWNPAYMENT_FTB_SHAPE);
        downpaymentDistOO = new LogNormalDistribution(rand, config.DOWNPAYMENT_OO_SCALE,
                config.DOWNPAYMENT_OO_SHAPE);
	    // Compute propensity to save, so that it is constant for a given household
        propensityToSave = config.DESIRED_BANK_BALANCE_EPSILON*rand.nextGaussian();
        // Decide if household is a BTL investor and, if so, its tendency to seek capital gains or rental yields
		BTLCapGainCoefficient = 0.0;
        // TODO: Check this if condition... why to divide by config.MIN_INVESTOR_PERCENTILE?
        if(incomePercentile > config.MIN_INVESTOR_PERCENTILE &&
                rand.nextDouble() < config.getPInvestor()/config.MIN_INVESTOR_PERCENTILE) {
            BTLInvestor = true;
            if(rand.nextDouble() < config.P_FUNDAMENTALIST) {
                BTLCapGainCoefficient = config.FUNDAMENTALIST_CAP_GAIN_COEFF;
            } else {
                BTLCapGainCoefficient = config.TREND_CAP_GAIN_COEFF;
            }
        } else {
            BTLInvestor = false;
        }
		desiredBankBalance = -1.0;
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- Owner-Occupier behaviour -----//

	/**
	 * Monthly non-essential or optional consumption by a household. It is calibrated so that the output wealth
     * distribution fits the ONS wealth data for Great Britain.
	 *
	 * @param me Household
	 */
	double getDesiredConsumption(Household me) {
		return config.CONSUMPTION_FRACTION*Math.max(me.getBankBalance() - getDesiredBankBalance(me), 0.0);
	}

	/**
     * Minimum bank balance a household would like to have at the end of the month. Used to determine non-essential
     * consumption.
     *
	 * @param me Household
     */
	double getDesiredBankBalance(Household me) {
        // TODO: why only if desired bank balance is set to -1? (does this get calculated only once? why?)
		if(desiredBankBalance == -1.0) {
			double lnDesiredBalance = config.DESIRED_BANK_BALANCE_ALPHA
                    + config.DESIRED_BANK_BALANCE_BETA
                        * Math.log(me.getMonthlyPreTaxIncome()*config.constants.MONTHS_IN_YEAR) + propensityToSave;
			desiredBankBalance = Math.exp(lnDesiredBalance);
			// TODO: What is this next rule? Not declared in the article! Check if 0.3 should be included as a parameter
			if(me.incomePercentile < 0.3 && !isPropertyInvestor()) desiredBankBalance = 1.0;
			// TODO: Note that this rule makes poor investors save more... could affect final wealth distributions!
		}
		return desiredBankBalance;
	}

	/**
     * Desired purchase price used to decide whether to buy a house
     *
	 * @param monthlyIncome Monthly income of the household
	 */
	double getDesiredPurchasePrice(double monthlyIncome) {
        double HPAFactor = config.BUY_WEIGHT_HPA*getLongTermHPAExpectation();
        // TODO: The capping of this factor intends to avoid negative and too large desired prices, the 0.9 is a
        // TODO: purely artificial fudge parameter. This formula should be reviewed and changed!
        if (HPAFactor > 0.9) HPAFactor = 0.9;
		return config.BUY_SCALE*config.constants.MONTHS_IN_YEAR*monthlyIncome
                *Math.exp(config.BUY_EPSILON*rand.nextGaussian())
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
                + config.SALE_EPSILON*rand.nextGaussian();
        // TODO: ExpAv days on market should probably be computed for each quality band so as to use here only the correct one
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
        return !isPropertyInvestor() && (rand.nextDouble() < config.derivedParams.MONTHLY_P_SELL*(1.0
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
					config.DOWNPAYMENT_BTL_MEAN + config.DOWNPAYMENT_BTL_EPSILON*rand.nextGaussian()));
		} else {
		    // TODO: Downpayments for inactive BTL investors (who are actually OO) should behave as for OO...
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
	public double rethinkHouseSalePrice(HouseSaleRecord sale) {
		if(rand.nextDouble() < config.P_SALE_PRICE_REDUCE) {
			double logReduction = config.REDUCTION_MU+(rand.nextGaussian()*config.REDUCTION_SIGMA);
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
    public boolean decideRentOrPurchase(Household me, double desiredPurchasePrice) {
        if(isPropertyInvestor()) return(true);
        double purchasePrice = Math.min(desiredPurchasePrice, Model.bank.getMaxMortgage(me, true));
        MortgageAgreement mortgageApproval = Model.bank.requestApproval(me, purchasePrice,
                decideDownPayment(me,purchasePrice), true);
        int newHouseQuality = Model.housingMarketStats.getMaxQualityForPrice(purchasePrice);
        if (newHouseQuality < 0) return false; // can't afford a house anyway
        double costOfHouse = mortgageApproval.monthlyPayment*config.constants.MONTHS_IN_YEAR
				- purchasePrice*getLongTermHPAExpectation();
        double costOfRent = Model.rentalMarketStats.getExpAvSalePriceForQuality(newHouseQuality)
                *config.constants.MONTHS_IN_YEAR;
        return rand.nextDouble() < sigma(config.SENSITIVITY_RENT_OR_PURCHASE*(costOfRent*(1.0
                + config.PSYCHOLOGICAL_COST_OF_RENTING) - costOfHouse));
    }

	/********************************************************
	 * Decide how much to bid on the rental market
	 * Source: Zoopla rental prices 2008-2009 (at Bank of England)
	 ********************************************************/
	public double desiredRent(Household me, double monthlyIncome) {
		return(monthlyIncome * config.DESIRED_RENT_INCOME_FRACTION);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Property investor behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Decide whether to sell an investment property
	 * 
	 * @param h The house in question
	 * @param me The investor
	 * @return True if investor me decides to sell investment property h
	 */
	boolean decideToSellInvestmentProperty(House h, Household me) {
		if(me.nInvestmentProperties() < 2) return false; // Always keep at least one property
		if(!h.isOnRentalMarket()) return false; // Don't sell while occupied by tenant
		if(h.owner!=me){
            System.out.println("Strange: deciding to sell investment property that I don't own");
            return false;
        }
        double marketPrice = Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality());
        // TODO: Why to call this "equity"? It is called "downpayment" in the article!
        MortgageAgreement mortgage = me.mortgageFor(h);
        double equity = Math.max(0.01, marketPrice - mortgage.principal); // Dummy security parameter to avoid dividing by zero
		double leverage = marketPrice/equity;
		// TODO: ATTENTION ---> This rental yield is not accounting for expected occupancy
		double rentalYield = h.rentalRecord.getPrice()*config.constants.MONTHS_IN_YEAR/marketPrice;
		double mortgageRate = mortgage.nextPayment()*config.constants.MONTHS_IN_YEAR/equity;
		double effectiveYield;
		if(config.BTL_YIELD_SCALING) {
			effectiveYield = leverage*((1.0 - BTLCapGainCoefficient)*rentalYield
                    + BTLCapGainCoefficient*(Model.rentalMarketStats.getLongTermExpAvFlowYield()
					+ getLongTermHPAExpectation())) - mortgageRate;
		} else {
			effectiveYield = leverage*(rentalYield + BTLCapGainCoefficient*getLongTermHPAExpectation())
                    - mortgageRate;
		}
		double pKeep = Math.pow(sigma(config.BTL_CHOICE_INTENSITY*effectiveYield),
                1.0/config.constants.MONTHS_IN_YEAR);
		return(rand.nextDouble() < (1.0 - pKeep));
	}
	

	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? 
	 * @param rbar exponential average rent for house of this quality
	 * @param d average days on market
	 * @param h house being offered for rent
	 */
	public double buyToLetRent(double rbar, double d, House h) {
		// TODO: What? Where does this equation come from?
		final double beta = config.RENT_MARKUP/Math.log(config.RENT_EQ_MONTHS_ON_MARKET); // Weight of days-on-market effect

		double exponent = config.RENT_MARKUP + Math.log(rbar + 1.0)
                - beta*Math.log((d + 1.0)/(config.constants.DAYS_IN_MONTH + 1))
                + config.RENT_EPSILON*rand.nextGaussian();
		double result = Math.exp(exponent);
        // TODO: The following contains a fudge (config.RENT_MAX_AMORTIZATION_PERIOD) to keep rental yield up
		double minAcceptable = Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality())
                /(config.RENT_MAX_AMORTIZATION_PERIOD*config.constants.MONTHS_IN_YEAR);
		if(result < minAcceptable) result = minAcceptable;
		return(result);

	}

	/**
	 * Update the demanded rent for a property
	 *
	 * @param sale the HouseSaleRecord of the property for rent
	 * @return the new rent
     */
	public double rethinkBuyToLetRent(HouseSaleRecord sale) {
		return((1.0 - config.RENT_REDUCTION)*sale.getPrice());
	}

	/***
	 * Monthly opportunity of buying a new BTL property.
	 *
	 * Investor households with no investment properties always attempt to buy one. Households with at least
	 * one investment property will calculate the expected yield of a new property based on two contributions:
	 * capital gain and rental yield (with their corresponding weights which depend on the type of investor).
	 *
	 * @param me household
	 * @return true if decision to buy
	 */
	public boolean decideToBuyBuyToLet(Household me) {
        // If I don't have any BTL properties, I always decide to buy one!
		if (me.nInvestmentProperties() < 1) { return true ; }
		// If my bank balance is below my desired bank balance, then I keep on saving instead of buying new properties
		// TODO: This mechanism and its parameter are not declared in the article! Any reference for the value of the parameter?
		if (me.getBankBalance() < getDesiredBankBalance(me)*config.BTL_CHOICE_MIN_BANK_BALANCE) { return false; }
		// Compute maximum price I could pay (maximum mortgage I could get)
		double maxPrice = Model.bank.getMaxMortgage(me, false);
		// If my maximum price is below the average price for the lowest quality, then I won't even try
		if (maxPrice < Model.housingMarketStats.getExpAvSalePriceForQuality(0)) return false;

        // --- calculate expected yield on zero quality house
        double effectiveYield;
		MortgageAgreement m = Model.bank.requestApproval(me, maxPrice, 0.0, false); // maximise leverage with min downpayment
		double leverage = m.purchasePrice/m.downPayment;
		double rentalYield = Model.rentalMarketStats.getExpAvFlowYield();
		double mortgageRate = m.monthlyPayment*config.constants.MONTHS_IN_YEAR/m.downPayment;
		if(config.BTL_YIELD_SCALING) {
			effectiveYield = leverage*((1.0 - BTLCapGainCoefficient)*rentalYield
                    + BTLCapGainCoefficient*(Model.rentalMarketStats.getLongTermExpAvFlowYield()
					+ getLongTermHPAExpectation())) - mortgageRate;
		} else {
			effectiveYield = leverage*(rentalYield + BTLCapGainCoefficient*getLongTermHPAExpectation())
                    - mortgageRate;
		}
	    return (rand.nextDouble() < Math.pow(sigma(config.BTL_CHOICE_INTENSITY*effectiveYield),
                1.0/config.constants.MONTHS_IN_YEAR));
	}
	
	public double btlPurchaseBid(Household me) {
	    // TODO: What is this 1.1 factor? Another fudge parameter???????????????????????????
        // TODO: It prevents wealthy investors from offering more than 10% above the average price of top quality houses
		return(Math.min(Model.bank.getMaxMortgage(me, false),
                1.1*Model.housingMarketStats.getExpAvSalePriceForQuality(config.N_QUALITY-1)));
	}

	public boolean isPropertyInvestor() {
		return(BTLInvestor);
	}

	// TODO: No need to return anything here!
	public boolean setPropertyInvestor(boolean isInvestor) {
		return(BTLInvestor = isInvestor);
	}

    /**
     * Logistic function, sometimes called sigma function, 1/1+e^(-x)
     *
     * @param x Parameter of the sigma or logistic function
     */
    private double sigma(double x) {
        return 1.0/(1.0+Math.exp(-1.0*x));
    }

	/**
     * @return expectation value of HPI in one year's time divided by today's HPI
     */
	public double getLongTermHPAExpectation() {
		// Dampening or multiplier factor, depending on its value being <1 or >1, for the current trend of HPA when
		// computing expectations as in HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt (double)
		return(Model.housingMarketStats.getLongTermHPA()*config.HPA_EXPECTATION_FACTOR);
    }

    public double getBTLCapGainCoefficient() { return BTLCapGainCoefficient; }
}
