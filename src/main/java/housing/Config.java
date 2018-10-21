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

    // General model control parameters
    int SEED;                               // Seed for the random number generator
    public int N_STEPS;				            // Simulation duration in time steps
    public int TIME_TO_START_RECORDING;	        // Time steps before recording statistics (initialisation time)
    int N_SIMS; 					        // Number of simulations to run (monte-carlo)
    boolean recordCoreIndicators;		    // True to write time series for each core indicator
    boolean recordMicroData;			    // True to write micro data for each transaction made
    boolean recordAgentData;				// True to write agent based data - very storage intensive
    boolean recordAgentDecisions; 			// True to write data from agent decision methods
    
    // Decision equations
    boolean FLEXIBLE_CREDIT_SUPPLY;			// make credit supply dependent on the housing cycle
    boolean FLEXIBLE_CREDIT_CONSTRAINTS;			// toggle flexible LTV, LTI and affordability ratios by the bank
    boolean ALTERNATE_CONSUMPTION_FUNCTION;			// make consumption solely dependent on income and wealth
    
    // alternate consumption function parameters
    public double CONSUMPTION_FRACTION_DISP_INC;			// propensity to consume out of disposable income
    public double WEALTH_EFFECT_Q1;							// lowest income quartile wealth effect
    public double WEALTH_EFFECT_Q2;							// second income quartile wealth effect
    public double WEALTH_EFFECT_Q3;							// third income quartile wealth effect
    public double WEALTH_EFFECT_Q4;							// highest income quartile wealth effect
    public double CONSUMPTION_BANK_BALANCE;					// coefficient for consumption out liquid wealth
    public double CONSUMPTION_HOUSING;						// coefficient for consumption out housing wealth
    public double CONSUMPTION_DEBT;							// coefficient for consumption out debt
    
    // House parameters
    public int N_QUALITY;                   // Number of quality bands for houses

    // Housing market parameters
    int DAYS_UNDER_OFFER;                       // Time (in days) that a house remains under offer
    double BIDUP;                               // Smallest proportional increase in price that can cause a gazump
    public double MARKET_AVERAGE_PRICE_DECAY;   // Decay constant for the exponential moving average of sale prices
    public double INITIAL_HPI;                  // Initial housing price index
    double HPI_MEDIAN;                          // Median house price
    public double HPI_SHAPE;                    // Shape parameter for the log-normal distribution of housing prices
    public double RENT_GROSS_YIELD;             // Profit margin for buy-to-let investors

    // Demographic parameters
    public int TARGET_POPULATION;           // Target number of households
    public double FUTURE_BIRTH_RATE;        // Future birth rate (births per year per capita), calibrated with flux of FTBs

    // Household parameters
    double RETURN_ON_FINANCIAL_WEALTH;      // Monthly percentage growth of financial investments
    public int TENANCY_LENGTH_AVERAGE;      // Average number of months a tenant will stay in a rented house
    int TENANCY_LENGTH_EPSILON;             // Standard deviation of the noise in determining the tenancy length

    // Household behaviour parameters: buy-to-let
    double P_INVESTOR;                      // Prior probability of being (wanting to be) a BTL investor
    double MIN_INVESTOR_PERCENTILE;         // Minimum income percentile for a household to be a BTL investor
    double FUNDAMENTALIST_CAP_GAIN_COEFF;   // Weight that fundamentalists put on cap gain
    double TREND_CAP_GAIN_COEFF;			// Weight that trend-followers put on cap gain
    double P_FUNDAMENTALIST; 			    // Probability that a BTL investor is a fundamentalist versus a trend-follower
    boolean BTL_YIELD_SCALING;			    // Chooses between two possible equations for BTL investors to make their buy/sell decisions
    // Household behaviour parameters: rent
    double DESIRED_RENT_INCOME_FRACTION;    // Desired proportion of income to be spent on rent
    double PSYCHOLOGICAL_COST_OF_RENTING;   // Annual psychological cost of renting
    double SENSITIVITY_RENT_OR_PURCHASE;    // Sensitivity parameter of the decision between buying and renting
    // Household behaviour parameters: general
    double BANK_BALANCE_FOR_CASH_DOWNPAYMENT;   // If bankBalance/housePrice is above this, payment will be made fully in cash
    double HPA_EXPECTATION_FACTOR;              // Weight assigned to current trend when computing expectations
    public int HPA_YEARS_TO_CHECK;              // Number of years of the HPI record to check when computing the annual HPA
    double HOLD_PERIOD;                         // Average period, in years, for which owner-occupiers hold their houses
    // Household behaviour parameters: sale price reduction
    double P_SALE_PRICE_REDUCE;             // Monthly probability of reducing the price of a house on the market
    double REDUCTION_MU;                    // Mean percentage reduction for prices of houses on the market
    double REDUCTION_SIGMA;                 // Standard deviation of percentage reductions for prices of houses on the market
    // Household behaviour parameters: consumption
    double CONSUMPTION_FRACTION;            // Fraction of monthly budget for consumption (monthly budget = bank balance - minimum desired bank balance)
    double ESSENTIAL_CONSUMPTION_FRACTION;  // Fraction of Government support necessarily spent monthly by all households as essential consumption
    // Household behaviour parameters: initial sale price
    double SALE_MARKUP;                     // Initial markup over average price of same quality houses
    double SALE_WEIGHT_DAYS_ON_MARKET;      // Weight of the days-on-market effect
    double SALE_EPSILON;                    // Standard deviation of the noise
    // Household behaviour parameters: buyer's desired expenditure
    double BUY_SCALE;                       // Scale, number of annual salaries the buyer is willing to spend for buying a house
    double BUY_WEIGHT_HPA;                  // Weight given to house price appreciation when deciding how much to spend for buying a house
    double BUY_EPSILON;                     // Standard deviation of the noise
    // Household behaviour parameters: demand rent
    double RENT_MARKUP;                         // Markup over average rent demanded for houses of the same quality
    double RENT_EQ_MONTHS_ON_MARKET;            // Number of months on the market in an equilibrium situation
    double RENT_EPSILON;                        // Standard deviation of the noise
    public double RENT_MAX_AMORTIZATION_PERIOD; // Maximum period BTL investors are ready to wait to get back their investment, this determines their minimum demanded rent
    double RENT_REDUCTION;                      // Percentage reduction of demanded rent for every month the property is in the market, not rented
    // Household behaviour parameters: downpayment
    double DOWNPAYMENT_FTB_SCALE;           // Scale parameter for the log-normal distribution of downpayments by first-time-buyers
    double DOWNPAYMENT_FTB_SHAPE;           // Shape parameter for the log-normal distribution of downpayments by first-time-buyers
    double DOWNPAYMENT_OO_SCALE;            // Scale parameter for the log-normal distribution of downpayments by owner-occupiers
    double DOWNPAYMENT_OO_SHAPE;            // Shape parameter for the log-normal distribution of downpayments by owner-occupiers
    double DOWNPAYMENT_MIN_INCOME;          // Minimum income percentile to consider any downpayment, below this level, downpayment is set to 0
    double DOWNPAYMENT_BTL_MEAN;            // Average downpayment, as percentage of house price, by but-to-let investors
    double DOWNPAYMENT_BTL_EPSILON;         // Standard deviation of the noise
    // Household behaviour parameters: desired bank balance
    double DESIRED_BANK_BALANCE_ALPHA;
    double DESIRED_BANK_BALANCE_BETA;
    double DESIRED_BANK_BALANCE_EPSILON;
    // Household behaviour parameters: selling decision
    double DECISION_TO_SELL_ALPHA;          // Weight of houses per capita effect
    double DECISION_TO_SELL_BETA;           // Weight of interest rate effect
    double DECISION_TO_SELL_HPC;            // TODO: fudge parameter, explicitly explained otherwise in the paper
    double DECISION_TO_SELL_INTEREST;       // TODO: fudge parameter, explicitly explained otherwise in the paper
    // Household behaviour parameters: BTL buy/sell choice
    double BTL_CHOICE_INTENSITY;            // Shape parameter, or intensity of choice on effective yield
    double BTL_CHOICE_MIN_BANK_BALANCE;     // Minimun bank balance, as a percentage of the desired bank balance, to buy new properties

    // Bank parameters
    double CREDIT_SUPPLY_ADJUSTMENT;		// Flexible credit supply adjustment parameter
    int MORTGAGE_DURATION_YEARS;            // Mortgage duration in years
    double BANK_INITIAL_BASE_RATE;          // Bank initial base-rate (currently remains unchanged)
    double BANK_CREDIT_SUPPLY_TARGET;       // Bank's target supply of credit per household per month
    double BANK_MAX_FTB_LTV;                // Maximum LTV ratio that the private bank would allow for first-time-buyers
    double BANK_MAX_OO_LTV;                 // Maximum LTV ratio that the private bank would allow for owner-occupiers
    double BANK_MAX_BTL_LTV;                // Maximum LTV ratio that the private bank would allow for BTL investors
    double BANK_MAX_FTB_LTI;                // Maximum LTI ratio that the private bank would allow for first-time-buyers (private bank's hard limit)
    double BANK_MAX_OO_LTI;                 // Maximum LTI ratio that the private bank would allow for owner-occupiers (private bank's hard limit)


    // Central bank parameters
    double CENTRAL_BANK_MAX_FTB_LTI;		    // Maximum LTI ratio that the bank would allow for first-time-buyers when not regulated
    double CENTRAL_BANK_MAX_OO_LTI;		        // Maximum LTI ratio that the bank would allow for owner-occupiers when not regulated
    double CENTRAL_BANK_FRACTION_OVER_MAX_LTI;  // Maximum fraction of mortgages that the bank can give over the LTI ratio limit
    double CENTRAL_BANK_AFFORDABILITY_COEFF;    // Maximum fraction of the household's income to be spent on mortgage repayments under stressed conditions
    double CENTRAL_BANK_BTL_STRESSED_INTEREST;  // Interest rate under stressed condition for BTL investors when calculating interest coverage ratios (ICR)
    double CENTRAL_BANK_MAX_ICR;                // Interest coverage ratio (ICR) limit imposed by the central bank

    // Construction sector parameters
    double CONSTRUCTION_HOUSES_PER_HOUSEHOLD;   // Target ratio of houses per household

    // Government parameters
    double GOVERNMENT_GENERAL_PERSONAL_ALLOWANCE;           // General personal allowance to be deducted when computing taxable income
    double GOVERNMENT_INCOME_LIMIT_FOR_PERSONAL_ALLOWANCE;  // Limit of income above which personal allowance starts to decrease £1 for every £2 of income above this limit
    public double GOVERNMENT_MONTHLY_INCOME_SUPPORT;        // Minimum monthly earnings for a married couple from income support

    // Collectors parameters
    double UK_HOUSEHOLDS;                       // Approximate number of households in UK, used to scale up results for core indicators
    boolean MORTGAGE_DIAGNOSTICS_ACTIVE;        // Whether to record mortgage statistics

    /** Declaration of addresses **/        // They must be public to be accessed from data package

    // Data addresses: Government
    public String DATA_TAX_RATES;                   // Address for tax bands and rates data
    public String DATA_NATIONAL_INSURANCE_RATES;    // Address for national insurance bands and rates data

    // Data addresses: EmploymentIncome
    public String DATA_INCOME_GIVEN_AGE;            // Address for conditional probability of income band given age band

    // Data addresses: Demographics
    public String DATA_AGE_MARGINAL_PDF;            // Address for target initial age probability density for household representative person
    public String DATA_HOUSEHOLD_AGE_AT_BIRTH_PDF;  // Address for pdf of household representative person's age at household birth
    public String DATA_DEATH_PROB_GIVEN_AGE;        // Address for data on the probability of death given the age of the household representative person

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
        getConfigValues(configFileName);
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
     * Method to read configuration parameters from a configuration (.properties) file
     * @param   configFileName    String with name of configuration (.properties) file (address inside source folder)
     */
    private void getConfigValues(String configFileName) {
        // Try-with-resources statement
        try (FileReader fileReader = new FileReader(configFileName)) {
            Properties prop = new Properties();
            prop.load(fileReader);
            // Check that all parameters declared in the configuration (.properties) file are also declared in this class
            try {
                Set<String> setOfFields = new HashSet<>();
                for (Field field : this.getClass().getDeclaredFields()) {
                    setOfFields.add(field.getName());
                }
                for (String property: prop.stringPropertyNames()) {
                    if (!setOfFields.contains(property)) {
                        throw new UndeclaredPropertyException(property);
                    }
                }
            } catch (UndeclaredPropertyException upe) {
                upe.printStackTrace();
            }
            // Run through all the fields of the Class using reflection
            for (Field field : this.getClass().getDeclaredFields()) {
                try {
                    // For int fields, parse the int with appropriate exception handling
                    if (field.getType().toString().equals("int")) {
                        try {
                            if (prop.getProperty(field.getName()) == null) {throw new FieldNotInFileException(field);}
                            field.set(this, Integer.parseInt(prop.getProperty(field.getName())));
                        } catch (NumberFormatException nfe) {
                            System.out.println("Exception " + nfe + " while trying to parse the field " +
                                    field.getName() + " for an integer");
                            nfe.printStackTrace();
                        } catch (IllegalAccessException iae) {
                            System.out.println("Exception " + iae + " while trying to set the field " +
                                    field.getName());
                            iae.printStackTrace();
                        } catch (FieldNotInFileException fnife) {
                            fnife.printStackTrace();
                        }
                    // For double fields, parse the double with appropriate exception handling
                    } else if (field.getType().toString().equals("double")) {
                        try {
                            if (prop.getProperty(field.getName()) == null) {throw new FieldNotInFileException(field);}
                            field.set(this, Double.parseDouble(prop.getProperty(field.getName())));
                        } catch (NumberFormatException nfe) {
                            System.out.println("Exception " + nfe + " while trying to parse the field " +
                                    field.getName() + " for an double");
                            nfe.printStackTrace();
                        } catch (IllegalAccessException iae) {
                            System.out.println("Exception " + iae + " while trying to set the field " +
                                    field.getName());
                            iae.printStackTrace();
                        } catch (FieldNotInFileException fnife) {
                            fnife.printStackTrace();
                        }
                    // For boolean fields, parse the boolean with appropriate exception handling
                    } else if (field.getType().toString().equals("boolean")) {
                        try {
                            if (prop.getProperty(field.getName()) == null) {throw new FieldNotInFileException(field);}
                            if (prop.getProperty(field.getName()).equals("true") ||
                                    prop.getProperty(field.getName()).equals("false")) {
                                field.set(this, Boolean.parseBoolean(prop.getProperty(field.getName())));
                            } else {
                                throw new BooleanFormatException("For input string \"" +
                                        prop.getProperty(field.getName()) + "\"");
                            }
                        } catch (BooleanFormatException bfe) {
                            System.out.println("Exception " + bfe + " while trying to parse the field " +
                                    field.getName() + " for a boolean");
                            bfe.printStackTrace();
                        } catch (IllegalAccessException iae) {
                            System.out.println("Exception " + iae + " while trying to set the field " +
                                    field.getName());
                            iae.printStackTrace();
                        } catch (FieldNotInFileException fnife) {
                            fnife.printStackTrace();
                        }
                    // For string fields, parse the string with appropriate exception handling
                    } else if (field.getType().toString().equals("class java.lang.String")) {
                        try {
                            if (prop.getProperty(field.getName()) == null) {throw new FieldNotInFileException(field);}
                            field.set(this, prop.getProperty(field.getName()).replace("\"", "").replace("\'", ""));
                        } catch (IllegalAccessException iae) {
                            System.out.println("Exception " + iae + " while trying to set the field " +
                                    field.getName());
                            iae.printStackTrace();
                        } catch (FieldNotInFileException fnife) {
                            fnife.printStackTrace();
                        }
                    // For unrecognised field types, except derivedParams and constants, throw exception
                    } else if (!field.getName().equals("derivedParams") && !field.getName().equals("constants")) {
                        throw new UnrecognisedFieldTypeException(field);
                    }
                } catch (UnrecognisedFieldTypeException ufte) {
                    ufte.printStackTrace();
                }
            }
        } catch (IOException ioe) {
            System.out.println("Exception " + ioe + " while trying to read file '" + configFileName + "'");
            ioe.printStackTrace();
        }
        // Finally, compute and set values for all derived parameters
        setDerivedParams();
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
