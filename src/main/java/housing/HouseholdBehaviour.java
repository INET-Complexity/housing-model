package housing;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;

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

    private Config                  config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister	        prng;
    private boolean                 BTLInvestor;
    private double                  BTLCapGainCoefficient; // Sensitivity of BTL investors to capital gain, 0.0 cares only about rental yield, 1.0 cares only about cap gain
    private double                  propensityToSave;
    private double					consumptionWealth;
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
        propensityToSave = prng.nextDouble();
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
	public double getDesiredConsumption(double bankBalance, double annualGrossTotalIncome, double incomePercentile,
										double disposableIncome, double propertyValues, 
										double totalDebt, double equityPosition) {
		double consumption;
		double saving;
		// if alternate consumption is active, use the following way to calculate it
		if(config.ALTERNATE_CONSUMPTION_FUNCTION) {
			double consumptionFraction = config.CONSUMPTION_FRACTION_DISP_INC;
			// these are monthly values! 
			double wealthEffect;
			double liquidWealthConsumptionCoefficient = config.CONSUMPTION_BANK_BALANCE;
			double propertyConsumptionCoefficient = config.CONSUMPTION_HOUSING;
			double debtConsumptionCoefficient = config.CONSUMPTION_DEBT;
			// set the wealth effect according to the employment income position of the household, so that 
			// households with higher employment income consume less out of their wealth
			if(incomePercentile<0.25) {
				wealthEffect = config.WEALTH_EFFECT_Q1;
				consumptionFraction = 0.95;
			}
			else if(0.25 <= incomePercentile && incomePercentile < 0.5) {
				wealthEffect = config.WEALTH_EFFECT_Q2;
				consumptionFraction = 0.85;
			}
			else if(0.5 <= incomePercentile && incomePercentile <0.75) {
				wealthEffect = config.WEALTH_EFFECT_Q3;
				consumptionFraction = 0.75;
			}
			else {
				wealthEffect = config.WEALTH_EFFECT_Q4;
				consumptionFraction = 0.6;
			}
			// calculate the desired consumption
			consumption = consumptionFraction*disposableIncome 
								 + wealthEffect*(liquidWealthConsumptionCoefficient*(bankBalance-disposableIncome) 
										 + propertyConsumptionCoefficient*propertyValues + debtConsumptionCoefficient*totalDebt);

			// calculate the different parts of consumption in order to extract this data
			double incomeConsumption = consumptionFraction*disposableIncome;
			double financialWealthConsumption = wealthEffect*(bankBalance-disposableIncome); 
			double housingWealthConsumption = wealthEffect*propertyConsumptionCoefficient*propertyValues;
			double debtConsumption = wealthEffect*debtConsumptionCoefficient*totalDebt;
			
			// restrict consumption so that the wealth effect cannot decrease liquid wealth below the ratio 
			// of twice the disposable income
			if((bankBalance-disposableIncome-consumption)<2*disposableIncome) {
				consumption = incomeConsumption;
			}
			
			// if HH wants to consume more than it has in cash, then limit to cash (otherwise bankrupt)
			// as disposable income is already added to the bankBalance before the method is called, the HH
			// effectively consumes all its disposable income
			if(consumption > bankBalance) { 
				consumption = bankBalance;
				}

			// if consumption is negative (due to high debt), consume at least either essential consumption
			// but not more than the actual bank balance to avoid bankruptcy
			// as essential consumption is already subtracted from disposable income, consumption here would be zero
			if(consumption < 0) {
				//they never consume negative (which could happen when bank balance negative
				consumption = Math.max(Math.min(0, bankBalance), 0);
			}
			saving = disposableIncome-consumption;
			if(consumption < 0) {
				System.out.println("weird, consumption is negative, exactly: " + consumption + "in Time: " + Model.getTime());
			}
			Model.householdStats.countIncomeAndWealthConsumption(saving, consumption, incomeConsumption, financialWealthConsumption, housingWealthConsumption, debtConsumption);
			consumptionWealth=financialWealthConsumption+housingWealthConsumption+debtConsumption;
			return consumption;
			}
			// possibly add a stronger effect on consumption if the household is "under water"
//			if (equityPosition > 0){
//			}			  			
		
		else{			
			consumption = config.CONSUMPTION_FRACTION*Math.max(bankBalance - getDesiredBankBalance(annualGrossTotalIncome), 0.0);
	double getDesiredConsumption(double bankBalance, double annualGrossTotalIncome) {
		return config.CONSUMPTION_FRACTION*Math.max(bankBalance
                - data.Wealth.getDesiredBankBalance(annualGrossTotalIncome, propensityToSave), 0.0);
			saving = disposableIncome-consumption;
			Model.householdStats.countIncomeAndWealthConsumption(saving, consumption, 0.0, 0.0, 0.0, 0.0);
			return consumption;
		}
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
	double getDesiredBankBalance(double annualGrossTotalIncome) {

//######################################################################################################################
//		return Math.exp(config.DESIRED_BANK_BALANCE_ALPHA
//                + config.DESIRED_BANK_BALANCE_BETA*Math.log(annualGrossTotalIncome) + propensityToSave);
        double[] incomeBins = {7.70124372, 7.95124372, 8.20124372, 8.45124372, 8.70124372, 8.95124372, 9.20124372,
                9.45124372, 9.70124372, 9.95124372, 10.20124372, 10.45124372, 10.70124372, 10.95124372, 11.20124372,
                11.45124372, 11.70124372};
        double[] wealthBins = {0.0, 0.78550467, 1.57100934, 2.35651401, 3.14201869, 3.92752336, 4.71302803, 5.4985327,
                6.28403737, 7.06954204, 7.85504671, 8.64055138, 9.42605606, 10.21156073, 10.9970654, 11.78257007,
                12.56807474, 13.35357941, 14.13908408, 14.92458876, 15.71009343};
        double[][] probability = {
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.07692308, 0.07692308, 0.07692308, 0.0, 0.07692308, 0.07692308, 0.0,
                        0.23076923, 0.23076923, 0.15384615, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.03030303, 0.0, 0.0, 0.06060606, 0.0, 0.0, 0.12121212, 0.12121212, 0.09090909, 0.06060606,
                        0.09090909, 0.27272727, 0.12121212, 0.0, 0.0, 0.03030303, 0.0, 0.0},
                {0.06382979, 0.0212766, 0.0, 0.0, 0.0212766, 0.0212766, 0.06382979, 0.08510638, 0.08510638, 0.08510638,
                        0.08510638, 0.06382979, 0.08510638, 0.08510638, 0.08510638, 0.10638298, 0.0, 0.04255319, 0.0, 0.0},
                {0.02439024, 0.00813008, 0.02439024, 0.00813008, 0.02439024, 0.04878049, 0.04065041, 0.08130081, 0.12195122,
                        0.10569106, 0.11382114, 0.05691057, 0.04065041, 0.06504065, 0.1300813, 0.04878049, 0.03252033, 0.02439024, 0.0, 0.0},
                {0.00934579, 0.0, 0.0, 0.0, 0.02803738, 0.01869159, 0.03738318, 0.1588785, 0.12149533, 0.12149533, 0.04672897,
                        0.07476636, 0.07476636, 0.11214953, 0.08411215, 0.07476636, 0.02803738, 0.00934579, 0.0, 0.0},
                {0.02173913, 0.0, 0.01449275, 0.03623188, 0.02898551, 0.07971014, 0.02173913, 0.07971014, 0.11594203,
                        0.11594203, 0.06521739, 0.11594203, 0.07971014, 0.11594203, 0.06521739, 0.02173913, 0.01449275, 0.00724638, 0.0, 0.0},
                {0.00840336, 0.0, 0.01680672, 0.01680672, 0.02941176, 0.01680672, 0.02941176, 0.07983193, 0.10504202, 0.10084034,
                        0.07563025, 0.10084034, 0.13445378, 0.08823529, 0.10504202, 0.06722689, 0.01680672, 0.00840336, 0.0, 0.0},
                {0.01123596, 0.01498127, 0.01872659, 0.00749064, 0.01498127, 0.02996255, 0.05243446, 0.08988764, 0.1011236,
                        0.12359551, 0.13108614, 0.06741573, 0.11985019, 0.07490637, 0.06741573, 0.05243446, 0.01498127, 0.00749064, 0.0, 0.0},
                {0.0026738, 0.00802139, 0.00534759, 0.01336898, 0.02406417, 0.02406417, 0.04010695, 0.07486631, 0.10427807,
                        0.10427807, 0.13636364, 0.11764706, 0.09625668, 0.10427807, 0.08823529, 0.04278075, 0.00534759, 0.0026738,
                        0.0026738, 0.0026738},
                {0.00383877,0.00191939,0.00767754,0.00191939,0.00575816,0.02111324,0.0403071,0.05758157,0.10940499,0.12284069,0.14395393,
                        0.14203455, 0.09980806, 0.11900192, 0.06333973, 0.03838772, 0.01919386, 0.00191939, 0.0, 0.0},
                {0.0, 0.0, 0.00606061, 0.0, 0.00757576, 0.01969697, 0.02878788, 0.0530303, 0.08333333, 0.11363636, 0.11515152, 0.14545455,
                        0.13333333, 0.11515152, 0.09848485, 0.05151515, 0.02424242, 0.0030303, 0.00151515, 0.0},
                {0.00128535, 0.00128535, 0.00385604, 0.00257069, 0.0, 0.00514139, 0.01542416, 0.04627249, 0.06298201, 0.10154242,
                        0.16580977, 0.14910026, 0.14910026, 0.14524422, 0.08868895, 0.03856041, 0.01928021, 0.00385604, 0.0, 0.0},
                {0.00278164, 0.0, 0.00417246, 0.00417246, 0.00417246, 0.00556328, 0.00834492, 0.02225313, 0.04867872, 0.07371349,
                        0.12517385, 0.14603616, 0.14325452, 0.18776078, 0.12378303, 0.07788595, 0.01668985, 0.00556328, 0.0, 0.0},
                {0.0, 0.0, 0.00163934, 0.00163934, 0.00327869, 0.00819672, 0.00655738, 0.01147541, 0.03114754, 0.04262295, 0.11147541,
                        0.14754098, 0.16393443, 0.19836066, 0.14590164, 0.07868852, 0.03114754, 0.01311475, 0.00163934, 0.00163934},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.00859599, 0.0, 0.02005731, 0.02292264, 0.08022923, 0.10601719, 0.13753582, 0.21489971,
                        0.19770774, 0.15186246, 0.03724928, 0.01719198, 0.00573066, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.01086957, 0.00543478, 0.02717391, 0.04891304, 0.08152174, 0.11413043, 0.2173913,
                        0.23913043, 0.1576087, 0.08152174, 0.01086957, 0.0, 0.00543478}
        };
        int incomeBin = floorSearch(incomeBins, Math.log(annualGrossTotalIncome));
        int i = 0;
        double prob = 0.0;
        double randNum = propensityToSave;
        while (prob < randNum) {
            prob += probability[incomeBin][i];
            i++;
        }
        i--;
        return Math.exp(wealthBins[i]*(randNum-(prob-probability[incomeBin][i]))/probability[incomeBin][i]
                + wealthBins[i+1]*(prob - randNum)/probability[incomeBin][i]);
	}

    /**
     * Given a sorted array of doubles arr[] and a double value x, the floor of x is the index of the largest element in
     * the array smaller than or equal to x
     *
     * @param arr Ordered array
     * @param x Value to find the floor of
     */
    private static int floorSearch(double arr[], double x) {
        // If last element is smaller than x, give index of last element
        if (x >= arr[arr.length-1]) {
            return arr.length - 2;
        }
        // If first element is greater than x, give index of first element anyway
        if (x < arr[0]) {
            return 0;
        }
        // Otherwise, linearly search for the first element greater than x
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > x) {
                return (i - 1);
            }
        }
        // Dummy return
        return -1;
    }
//######################################################################################################################


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
                - config.SALE_WEIGHT_MONTHS_ON_MARKET
                * Math.log(Model.housingMarketStats.getExpAvMonthsOnMarketForQuality(quality) + 1.0)
                + config.SALE_EPSILON*prng.nextGaussian();
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
//		return 0.0;
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
		if (downpayment > me.getBankBalance()) {
			//System.out.println("bankBalance restricts downpayment, desired downpayment " + downpayment/me.getBankBalance()+ "% bigger");
			downpayment = me.getBankBalance();
			}
		
		//System.out.println("the desired downpayment is "+ downpayment + ", Bank balance: " + me.getBankBalance() + ", monthly disposable income: " + me.getMonthlyDisposableIncome() );
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
        if(isPropertyInvestor()) {
        	if (config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) { 
            	Model.agentDecisionRecorder.rentOrBuy.println("true");
        	}
        	return(true);
        }
        MortgageAgreement mortgageApproval = Model.bank.requestApproval(me, purchasePrice,
                decideDownPayment(me, purchasePrice), true, false);
        int newHouseQuality = Model.housingMarketStats.getMaxQualityForPrice(purchasePrice);
        if (newHouseQuality < 0) {
            // if house household can't afford a house, record some basic facts DECISION DATA SH
            if (config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {        	
            	Model.agentDecisionRecorder.rentOrBuy.println(String.format("%.2f", me.getBankBalance())
            			+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
            			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
            			+ ", " + String.format("%.2f", me.getEquityPosition())
            			+ ", " + ", " 
            			+ ", " + String.format("%.2f", mortgageApproval.monthlyPayment)
            			+ ", " + String.format("%.2f", purchasePrice)
            			+ ", " + String.format("%.2f", decideDownPayment(me, purchasePrice))
            			+ ", " + String.format("%.2f", mortgageApproval.downPayment)
            			+ ", " + String.format("%.6f", mortgageApproval.monthlyInterestRate)
            			+ ", " + String.format("%.6f", getLongTermHPAExpectation())
            			+ ", " + newHouseQuality
            			+ ", " + "0"
            			+ ", " + "false"
            			+ ", " );
            }
        	return false; // can't afford a house anyway   
        }
        double costOfHouse = mortgageApproval.monthlyPayment*config.constants.MONTHS_IN_YEAR
				- purchasePrice*getLongTermHPAExpectation();
        double costOfRent = Model.rentalMarketStats.getExpAvSalePriceForQuality(newHouseQuality)
                *config.constants.MONTHS_IN_YEAR;
        double probabilityPlaceBidOnHousingMarket = sigma(config.SENSITIVITY_RENT_OR_PURCHASE*(costOfRent*(1.0
                + config.PSYCHOLOGICAL_COST_OF_RENTING) - costOfHouse));
        boolean placeBidOnHousingMarket = prng.nextDouble() < probabilityPlaceBidOnHousingMarket;
        //continue to record AgentDecision data here. DECISION DATA SH The first part (bank data) is written in the
        // bank.requestApproval method
        if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
        	Model.agentDecisionRecorder.rentOrBuy.println(String.format("%.2f", me.getBankBalance())
        			+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
        			+ ", " + String.format("%.2f", me.getEquityPosition())
        			+ ", " + String.format("%.2f", costOfHouse)
        			+ ", " + String.format("%.2f", costOfRent*(1.0 + config.PSYCHOLOGICAL_COST_OF_RENTING))
        			+ ", " + String.format("%.2f", mortgageApproval.monthlyPayment)
        			+ ", " + String.format("%.2f", purchasePrice)
        			+ ", " + String.format("%.2f", decideDownPayment(me, purchasePrice))
        			+ ", " + String.format("%.2f", mortgageApproval.downPayment)
        			+ ", " + String.format("%.6f", mortgageApproval.monthlyInterestRate)
        			+ ", " + String.format("%.6f", getLongTermHPAExpectation())
        			+ ", " + newHouseQuality
        			+ ", " + probabilityPlaceBidOnHousingMarket
        			+ ", " + placeBidOnHousingMarket
        			+ ", " );
        }
        
        return placeBidOnHousingMarket;
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
		if(me.nInvestmentProperties() < 2) {
			// if agent decisions are recorded, record basic information and reason for not selling
			if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
	        	Model.agentDecisionRecorder.decideSellInvestmentProperty.println(Model.getTime()
	        			+ ", " + me.id
	        			+ ", " + "true"
	        			+ ", " + String.format("%.2f", me.getBankBalance())
						+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
						+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
						+ ", " + String.format("%.2f", me.getEquityPosition())
						+ ", " );
			}
			return false;
		}
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
		double expectedEquityYield = leverage*((1.0 - BTLCapGainCoefficient)*currentRentalYield
				+ BTLCapGainCoefficient*getLongTermHPAExpectation())
                - mortgageRate;
		// Compute a probability to keep the property as a function of the effective yield
		double pKeep = Math.pow(sigma(config.BTL_CHOICE_INTENSITY*expectedEquityYield),
                1.0/config.constants.MONTHS_IN_YEAR);
		
		// if agent decision recorder is active, record decision parameters
		if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
        	Model.agentDecisionRecorder.decideSellInvestmentProperty.println(Model.getTime()
        			+ ", " + me.id
        			+ ", " + "false"
        			+ ", " + String.format("%.2f", me.getBankBalance())
					+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
					+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
					+ ", " + String.format("%.2f", me.getEquityPosition())
					+ ", " + BTLCapGainCoefficient
					+ ", " + h.getQuality()
					+ ", " + String.format("%.2f", currentMarketPrice)
					+ ", " + String.format("%.2f", equity)
					+ ", " + String.format("%.2f", leverage)
					+ ", " + String.format("%.4f", currentRentalYield)
					+ ", " + String.format("%.4f", mortgageRate)
					+ ", " + String.format("%.4f", Model.rentalMarketStats.getLongTermExpAvFlowYield())
					+ ", " + String.format("%.4f", getLongTermHPAExpectation())
					+ ", " + String.format("%.4f", expectedEquityYield)
					+ ", " + String.format("%.2f", pKeep)
					+ ", ");
        			
		}
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
    	//... with alternative consumption function some BTL investors seem to buy too many houses. Therefore,
    	// they cannot pay the "bills" and go bankrupt every month.
    	// if payments make up more than 30% of disposable income, don't invest
    	if(config.ALTERNATE_CONSUMPTION_FUNCTION) {
    		if(me.getMonthlyPayments() > 0.8*me.getMonthlyDisposableIncome()) {
    			// record DECISION DATA BTL
    			if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
    				Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
    						+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
    						+ ", " + String.format("%.2f", me.getBankBalance())
    						+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
    						+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
    						+ ", " + String.format("%.2f", me.getEquityPosition())
    						+ ", "+ ", " + ", " + ", " + ", " + ", " + ", " + ", " 
    						+ ", " + "false" 
    						+ ", " + "monthly mortgage payments already too high"
    						+ ", "); 

    			}
    			//System.out.println("monthly payments too high already. " + me.getMonthlyPayments() + " are the payments, the disposable income is: " + me.getMonthlyDisposableIncome());
    			return false;
    		}
    	}
    	
        // ...always decide to buy if owning no investment property yet
        if (me.nInvestmentProperties() < 1) { 
 			
        	// record some DECISION DATA BTL
        	if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
 				Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
 						+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
 						+ ", " + String.format("%.2f", me.getBankBalance())
 	        			+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
 	        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
 	        			+ ", " + String.format("%.2f", me.getEquityPosition())
 	        			+ ", " + String.format("%.2f", Model.bank.getMaxMortgage(me, false, false))
 	        			+ ", " + ", " + ", " + ", " + ", " + ", " + ", " 
 	     	        	+ ", " + "true"
 	     	        	+ ", " + "0 investment properties owned"
 	        			+ ", "); 
 			}
        	
        	return true ; }
        // ...never buy (keep on saving) if bank balance is below the household's desired bank balance
        // TODO: This mechanism and its parameter are not declared in the article! Any reference for the value of the parameter?
        // When the credit constraints are flexible, the BTL investors have a lower desire for deposits and more for housing wealth.
        // This is to mimic the effect banks pushing investors to buy more 
        if (config.FLEXIBLE_CREDIT_CONSTRAINTS) {
        	if (me.getBankBalance() < (getDesiredBankBalance(me.getAnnualGrossTotalIncome())*(config.BTL_CHOICE_MIN_BANK_BALANCE-Model.housingMarketStats.getLongTermHPA()))) {
        		// record DECISION DATA BTL
        		if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
        			Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
        					+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
        					+ ", " + String.format("%.2f", me.getBankBalance())
        					+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
        					+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
        					+ ", " + String.format("%.2f", me.getEquityPosition())
        					+ ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " 
        					+ "false" + ", " + "bb too far apart from desired bb"
        					+ ", " + String.format("%.2f", me.getBankBalance()) 
        					+ ", " + String.format("%.2f", getDesiredBankBalance(me.getAnnualGrossTotalIncome())) 
        					+ ", " + String.format("%.2f", getDesiredBankBalance(me.getAnnualGrossTotalIncome())*(config.BTL_CHOICE_MIN_BANK_BALANCE-Model.housingMarketStats.getLongTermHPA()))
        					+ ", "); 
        		}

        		return false; }
        }
        if(!config.FLEXIBLE_CREDIT_CONSTRAINTS) {
        if (me.getBankBalance() < data.Wealth.getDesiredBankBalance(me.getAnnualGrossTotalIncome(),
                me.behaviour.getPropensityToSave())*config.BTL_CHOICE_MIN_BANK_BALANCE) { return false; }
            	// record DECISION DATA BTL
            	if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
     				Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
     						+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
     						+ ", " + String.format("%.2f", me.getBankBalance())
     	        			+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
     	        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
     	        			+ ", " + String.format("%.2f", me.getEquityPosition())
     	        			+ ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " 
        					+ "false" + ", " + "bb too far apart from desired bb"
     	     	        	+ ", " + String.format("%.2f", me.getBankBalance()) 
     	     	        	+ ", " + String.format("%.2f", getDesiredBankBalance(me.getAnnualGrossTotalIncome())) 
     	     	        	+ ", " + String.format("%.2f", getDesiredBankBalance(me.getAnnualGrossTotalIncome())*(config.BTL_CHOICE_MIN_BANK_BALANCE))
     	     	        	+ ", "); 
     			}
            	
            	return false; }
        }
        // ...find maximum price (maximum mortgage) the household could pay
        double maxPrice = Model.bank.getMaxMortgage(me, false, true);
        // ...never buy if that maximum price is below the average price for the lowest quality
        if (maxPrice < Model.housingMarketStats.getExpAvSalePriceForQuality(0)) { 
 			// write DECISION DATA BTL
        	if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
 				Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(", " + ", " + Model.getTime() 
 						+ ", " + me.id 
 						+ ", " + String.format("%.2f", me.getBankBalance())
 	        			+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
 	        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
 	        			+ ", " + String.format("%.2f", me.getEquityPosition())
 	        			+ ", " + ", "+ ", " + ", " + ", " + ", " + ", " + ", " 
 	     	        	+ ", " + "false"
 	        			+ ", " + "max price too small for houses on market"
 	        			+ ", "); 
 			}
        	
        	return false; }

        // Find the expected equity yield rate for a hypothetical house maximising the leverage available to the
        // household and assuming an average rental yield (over all qualities). This is found as a weighted mix of both
        // rental yield and capital gain times the leverage
        // ...find mortgage with maximum leverage by requesting maximum mortgage with minimum downpayment
        MortgageAgreement mortgage = Model.bank.requestApproval(me, maxPrice, 0.0, false, false);
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
        boolean bidOnTheHousingMarket = prng.nextDouble() < pBuy;
     
        // last part of the DECISION DATA BTL output
     			if(config.recordAgentDecisions && (Model.getTime() >= config.TIME_TO_START_RECORDING)) {
     				Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
     						+ ", " + me.id
     						+ ", " + String.format("%.2f", me.getBankBalance())
     	        			+ ", " + String.format("%.2f", me.getMonthlyDisposableIncome())
     	        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
     	        			+ ", " + String.format("%.2f", me.getEquityPosition())
     	        			+ ", " + String.format("%.2f", Model.bank.getMaxMortgage(me, false, false))
     	        			+ ", " + String.format("%.2f", equity)
     	        			+ ", " + String.format("%.2f", leverage)
     	        			+ ", " + String.format("%.6f", rentalYield)
     	        			+ ", " + String.format("%.6f", mortgageRate)
     	        			+ ", " + String.format("%.6f", expectedEquityYield)
     	        			+ ", " + String.format("%.6f", getLongTermHPAExpectation())
     	        			+ ", " + String.format("%.4f", pBuy)
     	     	        	+ ", " + bidOnTheHousingMarket
     	        			+ ", "); 
     			}
        return bidOnTheHousingMarket;
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
        // TODO: The following contains a fudge (config.RENT_MAX_AMORTIZATION_PERIOD) to keep rental yield up
		double minAcceptable = Model.housingMarketStats.getExpAvSalePriceForQuality(quality)
                /(config.RENT_MAX_AMORTIZATION_PERIOD*config.constants.MONTHS_IN_YEAR);
		if (result < minAcceptable) result = minAcceptable;
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
     * @return expectation value of HPI in one year's time divided by today's HPI
     */
	public double getLongTermHPAExpectation() {
		// Dampening or multiplier factor, depending on its value being <1 or >1, for the current trend of HPA when
		// computing expectations as in HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt (double)
		return(Model.housingMarketStats.getLongTermHPA()*config.HPA_EXPECTATION_FACTOR);
    }

    public double getBTLCapGainCoefficient() { return BTLCapGainCoefficient; }

    public boolean isPropertyInvestor() { return BTLInvestor; }
    public double getConsumptionWealth() { return consumptionWealth;}

    double getPropensityToSave() { return propensityToSave; }
}
