package housing;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.lang.Integer;
import java.util.Set;

/**************************************************************************************************
 * Class to encapsulate all the configuration parameters of the model. It also contains all methods
 * needed to read these parameter values from a configuration properties file.
 *
 * @author Adrian Carro
 * @since 20/02/2017
 *
 *************************************************************************************************/
public class Config {

    //---------------------------------//
    //----- Fields and subclasses -----//
    //---------------------------------//

    /** Declaration of parameters **/

    /*
     * General model control parameters
     */
    int SEED = 1;                               // Seed for the random number generator
    int N_STEPS = 6000;				            // Simulation duration in time steps
    int TIME_TO_START_RECORDING = 0;	        // Time steps before recording statistics (initialisation time)
    int N_SIMS = 1; 					        // Number of simulations to run (monte-carlo)
    boolean recordCoreIndicators = false;		    // True to write time series for each core indicator
    boolean recordMicroData = false;			    // True to write micro data for each transaction made

    /*
     * House parameters
     */
    public int N_QUALITY = 48;                   // Number of quality bands for houses

    /*
     * Housing market parameters
     */
    int DAYS_UNDER_OFFER = 7;                       // Time (in days) that a house remains under offer
    double BIDUP = 1.0075;                               // Smallest proportional increase in price that can cause a gazump
    public double MARKET_AVERAGE_PRICE_DECAY = 0.25;   // Decay constant for the exponential moving average of sale prices
    // TODO: Reference for this value and justification for this discounting are needed! (the parameter is declared, but no source nor justification is given)
    public double INITIAL_HPI = 0.8;                  // Initial housing price index
    // TODO: Replace by the 2011 value
    double HPI_MEDIAN = 195000.0;                          // Median house price

    // Shape parameter for the log-normal distribution of housing prices, taken from the ONS (2013) house price index data tables, table 34 (double)
    // TODO: Replace by the 2011 value, compare with Land Registry Paid Price data and decide whether to use real distribution
    public double HPI_SHAPE = 0.555;
    // Yield on rent had average 6% between 2009/01 and 2015/01, minimum in 2009/10 maximum in 2012/04 peak-to-peak amplitude of 0.4%. Source: Bank of England, unpublished analysis based on Zoopla/Land Registry matching (Philippe Bracke)
    public double RENT_GROSS_YIELD = 0.05;             // Profit margin for buy-to-let investors

    /*
     * Demographic parameters
     */
    public int TARGET_POPULATION = 10000;           // Target number of households

    // Future birth rate (births per year per capita), calibrated with flux of FTBs, Council of Mortgage Lenders Regulated Mortgage Survey, 2015 (double)
    // TODO: Also described as "calibrated against average advances to first time buyers, core indicators 1987-2006". Check which explanation holds and replace by the 2011 value.
    public double FUTURE_BIRTH_RATE = 0.018;

    /*
     * Household parameters
     */
    double RETURN_ON_FINANCIAL_WEALTH = 0.002;      // Monthly percentage growth of financial investments
    // Source: ARLA - Members survey of the Private Rented Sector Q4 2013
    public int TENANCY_LENGTH_AVERAGE = 18;      // Average number of months a tenant will stay in a rented house
    int TENANCY_LENGTH_EPSILON = 6;             // Standard deviation of the noise in determining the tenancy length

    /*
     * Household behaviour parameters: buy-to-let
     */
    /*** Buy-To-Let parameters ***/
    // TODO: Shouldn't this be 4% according to the article?
    double P_INVESTOR = 0.16;                      // Prior probability of being (wanting to be) a BTL investor
    double MIN_INVESTOR_PERCENTILE = 0.5;         // Minimum income percentile for a household to be a BTL investor
    double FUNDAMENTALIST_CAP_GAIN_COEFF = 0.5;   // Weight that fundamentalists put on cap gain
    double TREND_CAP_GAIN_COEFF = 0.9;			// Weight that trend-followers put on cap gain
    double P_FUNDAMENTALIST = 0.5; 			    // Probability that a BTL investor is a fundamentalist versus a trend-follower
    boolean BTL_YIELD_SCALING = false;			    // Chooses between two possible equations for BTL investors to make their buy/sell decisions
    /*** Household behaviour parameters: rent ***/
    double DESIRED_RENT_INCOME_FRACTION = 0.33;    // Desired proportion of income to be spent on rent
    // TODO: This value comes from 1.1/12.0... Where does that come from?
    double PSYCHOLOGICAL_COST_OF_RENTING = 0.0916666666667;   // Annual psychological cost of renting
    // TODO: This value comes from 1.0/3500.0... Where does that come from?
    double SENSITIVITY_RENT_OR_PURCHASE = 0.000285714285714;    // Sensitivity parameter of the decision between buying and renting
    /*** Household behaviour parameters: general ***/
    // If the ratio between the buyer's bank balance and the house price is above this,
    // payment will be made fully in cash (double)
    // Calibrated against mortgage approval/housing transaction ratio, core indicators average 1987-2006
    // TODO: Find these sources and clarify this calibration!
    double BANK_BALANCE_FOR_CASH_DOWNPAYMENT = 2.0;
    // Dampening or multiplier factor, depending on its value being <1 or >1, for the current trend when computing expectations as in
    // HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt (double)
    // TODO: According to John Muellbauer, this is a dampening factor (<1). Find a reference for this!
    double HPA_EXPECTATION_FACTOR = 0.5;
    // Number of years of the HPI record to check when computing the annual HPA, i.e., how much backward looking households are
    public int HPA_YEARS_TO_CHECK = 1;
    // Average period, in years, for which owner-occupiers hold their houses (double)
    // British housing survey 2008
    double HOLD_PERIOD = 11.0;                         // Average period, in years, for which owner-occupiers hold their houses
    /*** Household behaviour parameters: sale price reduction ***/
    // This subsection was calibrated against Zoopla data at the BoE
    // Monthly probability of reducing the price of a house on the market
    // This value comes from 1.0-0.945
    double P_SALE_PRICE_REDUCE = 0.055;
    double REDUCTION_MU = 1.603;                    // Mean percentage reduction for prices of houses on the market
    double REDUCTION_SIGMA = 0.617;                 // Standard deviation of percentage reductions for prices of houses on the market
    /*** Household behaviour parameters: consumption ***/
    double CONSUMPTION_FRACTION = 0.5;            // Fraction of monthly budget for consumption (monthly budget = bank balance - minimum desired bank balance)
    double ESSENTIAL_CONSUMPTION_FRACTION = 0.8;  // Fraction of Government support necessarily spent monthly by all households as essential consumption
    /*** Household behaviour parameters: initial sale price ***/
    // TODO: Note says that, according to BoE calibration, this should be around 0.2. Check and solve this!
    double SALE_MARKUP = 0.04;                     // Initial markup over average price of same quality houses
    double SALE_WEIGHT_DAYS_ON_MARKET = 0.011;      // Weight of the days-on-market effect
    double SALE_EPSILON = 0.05;                    // Standard deviation of the noise
    /*** Household behaviour parameters: buyer's desired expenditure ***/
    // TODO: This has been macro-calibrated against owner-occupier LTI and LTV ration, core indicators average 1987-2006. Find sources!
    double BUY_SCALE = 4.5;                       // Scale, number of annual salaries the buyer is willing to spend for buying a house
    double BUY_WEIGHT_HPA = 0.08;                  // Weight given to house price appreciation when deciding how much to spend for buying a house
    double BUY_EPSILON = 0.14;                     // Standard deviation of the noise
    /*** Household behaviour parameters: demand rent ***/
    double RENT_MARKUP = 0.00;                         // Markup over average rent demanded for houses of the same quality
    double RENT_EQ_MONTHS_ON_MARKET = 6.0;            // Number of months on the market in an equilibrium situation
    double RENT_EPSILON = 0.05;                        // Standard deviation of the noise
    // Maximum period of time BTL investors are ready to wait to get back their investment through rents,
    // this determines the minimum rent they are ready to accept
    // TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: This parameter and its associated mechanism are not declared in the article! Need to declare and reference!
    public double RENT_MAX_AMORTIZATION_PERIOD = 20.833333333;
    double RENT_REDUCTION = 0.05;                      // Percentage reduction of demanded rent for every month the property is in the market, not rented
    /*** Household behaviour parameters: downpayment ***/
    // TODO: Both functional form and parameters are micro-calibrated against BoE data. Need reference or disclose distribution!
    double DOWNPAYMENT_FTB_SCALE = 10.30;           // Scale parameter for the log-normal distribution of downpayments by first-time-buyers
    double DOWNPAYMENT_FTB_SHAPE = 0.9093;           // Shape parameter for the log-normal distribution of downpayments by first-time-buyers
    double DOWNPAYMENT_OO_SCALE = 11.155;            // Scale parameter for the log-normal distribution of downpayments by owner-occupiers
    double DOWNPAYMENT_OO_SHAPE = 0.7538;            // Shape parameter for the log-normal distribution of downpayments by owner-occupiers
    // TODO: Calibrated against PSD data, need clearer reference or disclose distribution!
    double DOWNPAYMENT_MIN_INCOME = 0.3;          // Minimum income percentile to consider any downpayment, below this level, downpayment is set to 0
    // TODO: Said to be calibrated to match LTV ratios, but no reference is given. Need reference!
    // TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: Functional form slightly different to the one presented in the article
    double DOWNPAYMENT_BTL_MEAN = 0.3;            // Average downpayment, as percentage of house price, by but-to-let investors
    double DOWNPAYMENT_BTL_EPSILON = 0.1;         // Standard deviation of the noise
    /*** Household behaviour parameters: desired bank balance ***/
    // Micro-calibrated to match the log-normal relationship between wealth and income from the Wealth and Assets Survey
    // Log-normal function parameter
    double DESIRED_BANK_BALANCE_ALPHA = -32.0013877;
    // Log-normal function parameter
    double DESIRED_BANK_BALANCE_BETA = 4.07;
    // Standard deviation of a noise, it states a propensity to save (double)
    double DESIRED_BANK_BALANCE_EPSILON = 0.1;
    /*** Household behaviour parameters: selling decision ***/
    double DECISION_TO_SELL_ALPHA = 4.0;          // Weight of houses per capita effect
    double DECISION_TO_SELL_BETA = 5.0;           // Weight of interest rate effect
    // TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: fudge parameter, explicitly explained otherwise in the article
    double DECISION_TO_SELL_HPC = 0.05;            // TODO: fudge parameter, explicitly explained otherwise in the paper
    // TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: fudge parameter, explicitly explained otherwise in the article
    double DECISION_TO_SELL_INTEREST = 0.03;       // TODO: fudge parameter, explicitly explained otherwise in the paper
    /*** Household behaviour parameters: BTL buy/sell choice ***/
    double BTL_CHOICE_INTENSITY = 50.0;            // Shape parameter, or intensity of choice on effective yield
    // TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: This parameter and its associated mechanism are not declared in the article! Need to declare and reference!
    double BTL_CHOICE_MIN_BANK_BALANCE = 0.75;     // Minimun bank balance, as a percentage of the desired bank balance, to buy new properties

    /*
     * Bank parameters
     */
    // TODO: We need references or justification for all these values!
    int MORTGAGE_DURATION_YEARS = 25;            // Mortgage duration in years
    double BANK_INITIAL_BASE_RATE = 0.005;          // Bank initial base-rate (currently remains unchanged)
    double BANK_CREDIT_SUPPLY_TARGET = 380;       // Bank's target supply of credit per household per month
    double BANK_MAX_FTB_LTV = 0.95;                // Maximum LTV ratio that the private bank would allow for first-time-buyers
    double BANK_MAX_OO_LTV = 0.9;                 // Maximum LTV ratio that the private bank would allow for owner-occupiers
    double BANK_MAX_BTL_LTV = 0.8;                // Maximum LTV ratio that the private bank would allow for BTL investors
    double BANK_MAX_FTB_LTI = 6.0;                // Maximum LTI ratio that the private bank would allow for first-time-buyers (private bank's hard limit)
    double BANK_MAX_OO_LTI = 6.0;                 // Maximum LTI ratio that the private bank would allow for owner-occupiers (private bank's hard limit)


    /*
     * Central bank parameters
     */
    // TODO: We need references or justification for all these values! Also, need to clarify meaning of "when not regulated"
    double CENTRAL_BANK_MAX_FTB_LTI = 6.0;		    // Maximum LTI ratio that the bank would allow for first-time-buyers when not regulated
    double CENTRAL_BANK_MAX_OO_LTI = 6.0;		        // Maximum LTI ratio that the bank would allow for owner-occupiers when not regulated
    double CENTRAL_BANK_FRACTION_OVER_MAX_LTI = 0.15;  // Maximum fraction of mortgages that the bank can give over the LTI ratio limit
    double CENTRAL_BANK_AFFORDABILITY_COEFF = 0.5;    // Maximum fraction of the household's income to be spent on mortgage repayments under stressed conditions
    double CENTRAL_BANK_BTL_STRESSED_INTEREST = 0.05;  // Interest rate under stressed condition for BTL investors when calculating interest coverage ratios (ICR)
    double CENTRAL_BANK_MAX_ICR = 1.25;                // Interest coverage ratio (ICR) limit imposed by the central bank

    /*
     * Construction sector parameters
     */
    // TODO: We need references or justification for all these values!
    double CONSTRUCTION_HOUSES_PER_HOUSEHOLD = 0.82;   // Target ratio of houses per household

    /*
     * Government parameters
     */
    double GOVERNMENT_GENERAL_PERSONAL_ALLOWANCE = 9440.0;           // General personal allowance to be deducted when computing taxable income
    double GOVERNMENT_INCOME_LIMIT_FOR_PERSONAL_ALLOWANCE = 100000.0;  // Limit of income above which personal allowance starts to decrease £1 for every £2 of income above this limit
    // TODO: We need a reference or justification for this value!
    public double GOVERNMENT_MONTHLY_INCOME_SUPPORT = 492.7;        // Minimum monthly earnings for a married couple from income support

    /*
     * Collectors parameters
     */
    // TODO: Reference needed
    double UK_HOUSEHOLDS = 26.5e6;                       // Approximate number of households in UK, used to scale up results for core indicators
    boolean MORTGAGE_DIAGNOSTICS_ACTIVE = true;        // Whether to record mortgage statistics

    /*
     * Declaration of addresses
     */        // They must be public to be accessed from data package
    // TODO: We need clearer references for the values contained in these files! Also, current values are for 2013/2014, replace for 2011!
    // Data addresses: Government
    public String DATA_TAX_RATES = "src/main/resources/TaxRates.csv";                   // Address for tax bands and rates data
    public String DATA_NATIONAL_INSURANCE_RATES = "src/main/resources/NationalInsuranceRates.csv";    // Address for national insurance bands and rates data

    // Data addresses: EmploymentIncome
    public String DATA_INCOME_GIVEN_AGE = "src/main/resources/IncomeGivenAge.csv";            // Address for conditional probability of income band given age band

    // Data addresses: Demographics
    // Target probability density of age of representative person in the household at time t=0, calibrated against LCFS (2012)
    public String DATA_HOUSEHOLD_AGE_AT_BIRTH_PDF = "src/main/resources/HouseholdAgeAtBirthPDF.csv";  // Address for pdf of household representative person's age at household birth
    public String DATA_DEATH_PROB_GIVEN_AGE = "src/main/resources/DeathProbGivenAge.csv";        // Address for data on the probability of death given the age of the household representative person

    /** Construction of objects to contain derived parameters and constants **/

    // Create object containing all constants
    public Config.Constants constants = new Constants();

    // Finally, create object containing all derived parameters
    public Config.DerivedParams derivedParams = new DerivedParams();

    /**
     * Class to contain all parameters which are not read from the configuration (.properties) file, but derived,
     * instead, from these configuration parameters
     */
    public class DerivedParams {
        // Housing market parameters
        public int HPI_RECORD_LENGTH;   // Number of months to record HPI (to compute price growth at different time scales)
        double MONTHS_UNDER_OFFER;      // Time (in months) that a house remains under offer
        double T;                       // Characteristic number of data-points over which to average market statistics
        public double E;                // Decay constant for averaging days on market (in transactions)
        public double G;                // Decay constant for averageListPrice averaging (in transactions)
        public double HPI_LOG_MEDIAN;   // Logarithmic median house price (scale parameter of the log-normal distribution)
        double HPI_REFERENCE;           // Mean of reference house prices
        // Household behaviour parameters: general
        double MONTHLY_P_SELL;          // Monthly probability for owner-occupiers to sell their houses
        // Bank parameters
        int N_PAYMENTS;                 // Number of monthly repayments (mortgage duration in months)
        // House rental market parameters
        public double K;                // Decay factor for exponential moving average of gross yield from rentals (averageSoldGrossYield)
        public double KL;               // Decay factor for long-term exponential moving average of gross yield from rentals (longTermAverageGrossYield)
        // Collectors parameters
        double AFFORDABILITY_DECAY; 	// Decay constant for the exponential moving average of affordability

        public double getAffordabilityDecay() {
          return AFFORDABILITY_DECAY;
        }

        public double getE() {
            return E;
        }

        public int getHPIRecordLength() {
            return HPI_RECORD_LENGTH;
        }

        public double getHPIReference() {
            return HPI_REFERENCE;
        }

    }

    /**
     * Class to contain all constants (not read from the configuration file nor derived from it)
     */
    public class Constants {
        final public int DAYS_IN_MONTH = 30;
        final public int MONTHS_IN_YEAR = 12;
    }

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /**
     * Empty constructor, mainly used for copying local instances of the Model Config instance into other classes
     */
    public Config () {}

    /**
     * Constructor with full initialization, used only for the original Model Config instance
     */
    public Config (String configFileName) {
        // Compute and set values for all derived parameters
        setDerivedParams();
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public boolean isMortgageDiagnosticsActive() {
        return MORTGAGE_DIAGNOSTICS_ACTIVE;
    }

    public double getUKHouseholds() {
        return UK_HOUSEHOLDS;
    }

    public double getPInvestor() {
        return P_INVESTOR;
    }

    /**
     * Method to compute and set values for all derived parameters
     */
    private void setDerivedParams() {
        // Housing market parameters
        derivedParams.HPI_RECORD_LENGTH = HPA_YEARS_TO_CHECK*constants.MONTHS_IN_YEAR + 3;  // Plus three months in a quarter
        derivedParams.MONTHS_UNDER_OFFER = (double)DAYS_UNDER_OFFER/constants.DAYS_IN_MONTH;
        derivedParams.T = 0.02*TARGET_POPULATION;                   // TODO: Clarify where does this 0.2 come from, and provide explanation for this formula
        derivedParams.E = Math.exp(-1.0/derivedParams.T);           // TODO: Provide explanation for this formula
        derivedParams.G = Math.exp(-N_QUALITY/derivedParams.T);     // TODO: Provide explanation for this formula
        derivedParams.HPI_LOG_MEDIAN = Math.log(HPI_MEDIAN);
        derivedParams.HPI_REFERENCE = Math.exp(derivedParams.HPI_LOG_MEDIAN + HPI_SHAPE*HPI_SHAPE/2.0);
        // Household behaviour parameters: general
        derivedParams.MONTHLY_P_SELL = 1.0/(HOLD_PERIOD*constants.MONTHS_IN_YEAR);
        // Bank parameters
        derivedParams.N_PAYMENTS = MORTGAGE_DURATION_YEARS*constants.MONTHS_IN_YEAR;
        // House rental market parameters
        derivedParams.K = Math.exp(-10000.0/(TARGET_POPULATION*50.0));  // TODO: Are these decay factors well-suited? Any explanation, reasoning behind the numbers chosen?
        derivedParams.KL = Math.exp(-10000.0/(TARGET_POPULATION*50.0*200.0));   // TODO: Also, they are not reported in the paper!
        // Collectors parameters
        derivedParams.AFFORDABILITY_DECAY = Math.exp(-1.0/100.0);
    }

    /**
     * Equivalent to NumberFormatException for detecting problems when parsing for boolean values
     */
    public class BooleanFormatException extends RuntimeException {
        BooleanFormatException(String message) { super(message); }
    }

    /**
     * Exception for detecting unrecognised (not implemented) field types
     */
    public class UnrecognisedFieldTypeException extends Exception {
        UnrecognisedFieldTypeException(Field field) {
            super("Field type \"" + field.getType().toString() + "\", found at field \"" + field.getName() +
                    "\", could not be recognised as any of the implemented types");
        }
    }

    /**
     * Exception for detecting fields declared in the code but not present in the config.properties file
     */
    public class FieldNotInFileException extends Exception {
        FieldNotInFileException(Field field) {
            super("Field \"" + field.getName() + "\" could not be found in the config.properties file");
        }
    }

    /**
     * Exception for detecting properties present in the config.properties file but not declared in the code
     */
    public class UndeclaredPropertyException extends Exception {
        UndeclaredPropertyException(Object property) {
            super("Property \"" + property + "\" could not be found among the fields declared within the Config class");
        }
    }
}
