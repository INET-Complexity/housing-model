package housing;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import collectors.HousingMarketStats;
import collectors.RentalMarketStats;

import utilities.BinnedDataDouble;
import utilities.Pdf;

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
    private static MersenneTwister          prng = Model.prng; // Passes the Model's random number generator to a private static field
    private static HousingMarketStats       housingMarketStats = Model.housingMarketStats; // Passes the Model's housing market stats object to a private static field
    private static RentalMarketStats        rentalMarketStats = Model.rentalMarketStats; // Passes the Model's rental market stats object to a private static field
    private static Pdf                      saleMarkUpPdf = new Pdf(config.DATA_INITIAL_SALE_MARKUP_DIST); // Read initial sale price mark-up distribution from file
    private static Pdf                      rentMarkUpPdf = new Pdf(config.DATA_INITIAL_RENT_MARKUP_DIST); // Read initial rent price mark-up distribution from file
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
     * investor "gene" (given its income percentile), and, if so, the specific attitude of the BTL household towards
     * investing, that is,whether rental-income-driven, capital-gains-driven or both
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
            // ...and, if so, whether it will have a rental-income-driven, capital-gains-driven or mixed strategy
            double rand = prng.nextDouble();
            if (rand < config.BTL_P_INCOME_DRIVEN) {
                BTLCapGainCoefficient = config.BTL_INCOME_DRIVEN_CAP_GAIN_COEFF;
            } else if (rand < config.BTL_P_INCOME_DRIVEN + config.BTL_P_CAPITAL_DRIVEN) {
                BTLCapGainCoefficient = config.BTL_CAPITAL_DRIVEN_CAP_GAIN_COEFF;
            } else {
                BTLCapGainCoefficient = config.BTL_MIX_DRIVEN_CAP_GAIN_COEFF;
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
     * Desired purchase price used to decide whether to buy a house and how much to bid for it. This desired purchase
     * price is assumed to be a non-linear function of the household's annual gross employment income
     *
     * @param annualGrossEmploymentIncome Annual gross employment income of the household
     */
    double updateDesiredPurchasePrice(double annualGrossEmploymentIncome) {
        return config.BUY_SCALE * Math.pow(annualGrossEmploymentIncome, config.BUY_EXPONENT)
                * Math.exp(config.BUY_MU + config.BUY_SIGMA*prng.nextGaussian());
    }

    /**
     * TODO: Decision needed between this and previous specification. If this chose, 0.05 to be moved to config file
     * Alternative specification of the desired purchase price used to decide whether to buy a house and how much to bid
     * for it. This specification assumes an accounting relation between housing costs (including mortgage expenses,
     * taxes and maintenance costs, capital gains and foregone interests at the risk-free rate) and a non-linear
     * function of the household's annual gross employment income
     *
     * @param annualGrossEmploymentIncome Annual gross employment income of the household
     * @param LTV Loan to value ratio targeted by the household
     */
    double getAltDesiredPurchasePrice(double annualGrossEmploymentIncome, double LTV) {
        double r = Model.bank.getMortgageInterestRate();
        double denominator = 0.05
                + LTV * r / (1 - Math.pow((1 + r / config.constants.MONTHS_IN_YEAR), -config.derivedParams.N_PAYMENTS))
                + (1.0 - LTV) * Model.centralBank.getBaseRate()
                - config.BUY_WEIGHT_HPA*getLongTermHPAExpectation();
        // Denominator capped to arbitrary minimum to avoid dividing by zero, unrealistically large desired budgets and
        // negative values
        denominator = Math.max(denominator, 0.001);
        return config.BUY_SCALE * Math.pow(annualGrossEmploymentIncome, config.BUY_EXPONENT)
                * Math.exp(config.BUY_MU + config.BUY_SIGMA*prng.nextGaussian()) / denominator;
    }

    /**
     * Desired rental price used to bid on the rental market.
     *
     * @param annualGrossEmploymentIncome Annual gross employment income of the household
     */
    double getDesiredRentPrice(double annualGrossEmploymentIncome, double monthlyNetTotalIncome) {
        double desiredRentalPrice = config.DESIRED_RENT_SCALE
                * Math.pow(annualGrossEmploymentIncome, config.DESIRED_RENT_EXPONENT)
                * Math.exp(config.DESIRED_RENT_MU + config.DESIRED_RENT_SIGMA * prng.nextGaussian());
        // Note the capping of rental bids to the available net income after essential consumption
        return Math.min(desiredRentalPrice, monthlyNetTotalIncome
                - config.ESSENTIAL_CONSUMPTION_FRACTION * config.GOVERNMENT_MONTHLY_INCOME_SUPPORT);
    }

    /**
     * Initial sale price of a house to be listed. This is modelled as the exponentially moving average sale price of
     * houses of the same quality times a mark-up which is drawn from a real distribution of mark-ups, calibrated using
     * a combination of Zoopla and HPI data.
     *
     * @param quality Quality of the house to be sold
     * @param principal Amount of principal left on any mortgage on this house
     */
    double getInitialSalePrice(int quality, double principal) {
        return Math.max(saleMarkUpPdf.nextDouble(prng) * housingMarketStats.getExpAvSalePriceForQuality(quality),
                principal);
    }

    /**
     * Initial rent price of a house to be listed on the rental market. This is modelled as the exponentially moving
     * average rent price of houses of the same quality times a mark-up which is drawn from a real distribution of
     * rental mark-ups, calibrated using a combination of Zoopla and HPI data.
     *
     * @param quality Quality of the house to be rented out
     */
    double getInitialRentPrice(int quality) {
        return rentMarkUpPdf.nextDouble(prng) * rentalMarketStats.getExpAvSalePriceForQuality(quality);
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
    double decideDownPayment(Household me, double housePrice, boolean isHome) {
        if (me.getBankBalance() > housePrice*config.DOWNPAYMENT_BANK_BALANCE_FOR_CASH_SALE) {
            return housePrice;
        }
        double downpayment;
        if (me.isFirstTimeBuyer()) {
            // Since the function of the HPI is to move the down payments distribution upwards or downwards to
            // accommodate current price levels, and the distribution is itself aggregate, we use the aggregate HPI
            downpayment = me.getBankBalance();
//            TODO: In the future, remove this old FTB down-payment implementation, kept for now as legacy/alternative
//            downpayment = housingMarketStats.getHPI()
//                    * downpaymentDistFTB.inverseCumulativeProbability(me.incomePercentile);
        } else if (!isHome) {
            downpayment = housePrice*(Math.max(0.0,
                    config.DOWNPAYMENT_BTL_MEAN + config.DOWNPAYMENT_BTL_EPSILON * prng.nextGaussian()));
        } else {
            downpayment = housingMarketStats.getHPI()
                    * downpaymentDistOO.inverseCumulativeProbability(me.incomePercentile);
        }
        if (downpayment > me.getBankBalance()) downpayment = me.getBankBalance();
        return downpayment;
    }

    /**
     * TODO: If getAltDesiredPurchasePrice() is used, then this initial decision on target LTV is also needed
     * TODO: In that case, all hard-coded parameters need to be moved to config file
     * Decide the household's target loan-to-value ratio in order to then decide on a desired purchase price
     *
     * @param me the household
     */
    double decideLTV(Household me) {
        if (me.isFirstTimeBuyer()) {
            return (0.0002826 * me.getAnnualGrossEmploymentIncome()
                    - 0.00000000128 * Math.pow(me.getAnnualGrossEmploymentIncome(), 2)
                    - 0.5791559 * me.getAge()
                    + 83.92332) / 100.0;
        } else if (isPropertyInvestor()) {
            return Math.max(0.0, config.DOWNPAYMENT_BTL_MEAN + config.DOWNPAYMENT_BTL_EPSILON * prng.nextGaussian());
        } else {
            return (0.0002175 * me.getAnnualGrossEmploymentIncome()
                    - 0.000000000834 * Math.pow(me.getAnnualGrossEmploymentIncome(), 2)
                    - 0.9625552 * me.getAge()
                    + 93.82513) / 100.0;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Renter behaviour
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Rules for households currently in social housing or at the end of their rental contracts to decide whether to
     * try to rent or buy. Note that households in social housing could be potential first-time buyers (whether BTL or
     * non-BTL households), ex-renters unable to find a new tenancy or buy a house in previous time steps or home movers
     * (ex-owner-occupiers). This decision is based on first checking the maximum quality the household could afford on
     * the ownership market and then comparing the annual costs of this purchase with the annual costs of renting a
     * house of the same quality. While the household may later on decide to rent a house of a different quality than
     * the one they could buy, thus assuming higher or lower costs, it is assumed that the difference in costs is equal
     * to the quality difference in terms of cash (i.e., monetised), such that there is indifference between these
     * options and the decision between buying and renting would remain the same.
     *
     * @return True if the household decides to buy a house, False if the household decides to rent
     */
    boolean decideRentOrPurchase(Household me, double purchasePrice, double desiredDownPayment, double desiredPrice) {
        // By definition, BTL households never rent
        if(isPropertyInvestor()) return(true);
        // First, find the maximum quality the household could afford in the ownership market
        int newHouseQuality = housingMarketStats.getMaxQualityForPrice(purchasePrice);
        // Then, force renting if the household cannot afford even the minimum quality...
        if (newHouseQuality < 0) return false;
        // ...and cap the purchase price to the average price of the maximum quality, so as to prevent unreasonable
        // mortgage costs when comparing to the equivalent rental option
        if (newHouseQuality == config.derivedParams.N_QUALITIES - 1) {
            purchasePrice = housingMarketStats.getExpAvSalePriceForQuality(config.derivedParams.N_QUALITIES - 1);
        }
        // If maximum mortgage price is below desired price (capped by maximum quality price), then rent
        // TODO: Re-check if we're fine with this rule after output calibration
        if (Math.min(desiredPrice, housingMarketStats.getExpAvSalePriceForQuality(config.derivedParams.N_QUALITIES - 1))
                > Model.bank.getMaxMortgagePrice(me, true)) {
            return false;
        }
        // Find out potential mortgage characteristics...
        MortgageAgreement mortgageApproval = Model.bank.requestApproval(me, purchasePrice,
                desiredDownPayment, true);
        // ...compute both purchase and rental annual costs...
        double costOfHouse = mortgageApproval.monthlyPayment * config.constants.MONTHS_IN_YEAR
                - purchasePrice*getLongTermHPAExpectation();
        double costOfRent = rentalMarketStats.getExpAvSalePriceForQuality(newHouseQuality)
                *config.constants.MONTHS_IN_YEAR;
        // ...and, finally, compare these costs by building a sigma-shaped probability to buy
        return prng.nextDouble() < sigma(config.SENSITIVITY_RENT_OR_PURCHASE*(costOfRent*(1.0
                + config.PSYCHOLOGICAL_COST_OF_RENTING) - costOfHouse));
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
        // First find the mortgage agreement for this property
        MortgageAgreement mortgage = me.mortgageFor(h);

        // Fast decisions...
        // ... from 2 years to maturity, put property for sale whenever bank balance not enough for principal repayment
        if (mortgage.nPayments <= 24 & mortgage.principal > me.getBankBalance()) {
            return true;
        }
        // ...always keep at least one investment property (i.e., at least two properties)
        if (me.getNProperties() < 3) return false;
        // ...don't sell while occupied by tenant
        if (!h.isOnRentalMarket()) return false;

        // Find the expected equity yield rate of this property as a weighted mix of both rental yield and capital gain
        // times the leverage...
        // ...find its current (fair market value) sale price
        double currentMarketPrice = housingMarketStats.getExpAvSalePriceForQuality(h.getQuality());
        // ...find equity, or assets minus liabilities
        double equity = Math.max(0.01, currentMarketPrice - mortgage.principal); // The 0.01 prevents possible divisions by zero later on
        // ...find the leverage on that mortgage (Assets divided by equity, or return on equity)
        double leverage = currentMarketPrice / equity;
        // ...find the expected rental yield of this property as its current rental price (under current average
        // occupancy) divided by its current (fair market value) sale price
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
     * attempt to buy one. If the resources available to the household (maximum mortgage) are below the average price
     * for the lowest quality houses, then no attempt to buy is made. Note that there is no capping of desired bid
     * prices to the average price of the maximum quality band, since this would not affect the decision in any case.
     * Households with at least one investment property will calculate the expected yield of a new property based on two
     * contributions: rental yield and capital gain (with their corresponding weights which depend on the type of
     * investor).
     *
     * @param me The investor household
     * @return True if investor me decides to try to buy a new investment property, False otherwise
     */
    boolean decideToBuyInvestmentProperty(Household me) {
        // Fast decisions...
        // ...do not buy if current bank balance below desired level
        // TODO: Review whether to keep this mechanism as well as its particular position (before or after next rule)
        if (me.getBankBalance() < data.Wealth.getDesiredBankBalance(me.getAnnualGrossTotalIncome(), propensityToSave)) {
            return false;
        }
        // ...otherwise, always decide to buy if owning no investment property yet (i.e., if owning only one property, a home)
        if (me.getNProperties() < 2) { return true ; }
        // ...also, do not buy if the maximum price of the household (corresponding to its maximum mortgage) is below
        // the average price for the lowest quality
        double maxPrice = Model.bank.getMaxMortgagePrice(me, false);
        if (maxPrice < housingMarketStats.getExpAvSalePriceForQuality(0)) { return false; }

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
        double rentalYield = rentalMarketStats.getExpAvFlowYield();
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
     * Decide whether and how much to drop the list-price of a house for sale if it has been on the market for (another)
     * month and hasn't got sold
     *
     * @param sale The HouseOfferRecord of the house that is on the sale market
     */
    double rethinkHouseSalePrice(HouseOfferRecord sale) {
        if (prng.nextDouble() < config.P_SALE_PRICE_REDUCE) {
            double logReduction = config.REDUCTION_MU + (prng.nextGaussian() * config.REDUCTION_SIGMA);
            // This prevents negative and too small new prices. While the limit of 0.5 is ad-hoc, being the probability
            // of larger decreases smaller than 1%, it should not affect the results in any significant way
            return sale.getPrice() * Math.max(1.0 - Math.exp(logReduction) / 100.0, 0.5);
        }
        return sale.getPrice();
    }

    /**
     * Decide whether and how much to drop the list-price of a house for rent if it has been on the market for (another)
     * month and hasn't got sold
     *
     * @param sale The HouseOfferRecord of the house that is on the rental market
     */
    double rethinkHouseRentPrice(HouseOfferRecord sale) {
        if (prng.nextDouble() < config.P_RENT_PRICE_REDUCE) {
            double logReduction = config.RENT_REDUCTION_MU + (prng.nextGaussian() * config.RENT_REDUCTION_SIGMA);
            // This prevents negative and too small new prices. While the limit of 0.5 is ad-hoc, being the probability
            // of larger decreases smaller than 1%, it should not affect the results in any significant way
            return sale.getPrice() * Math.max(1.0 - Math.exp(logReduction) / 100.0, 0.5);
        }
        return sale.getPrice();
    }

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
        return housingMarketStats.getLongTermHPA() * config.HPA_EXPECTATION_FACTOR + config.HPA_EXPECTATION_CONST;
    }

    public double getBTLCapGainCoefficient() { return BTLCapGainCoefficient; }

    public boolean isPropertyInvestor() { return BTLInvestor; }

    double getPropensityToSave() { return propensityToSave; }
}
