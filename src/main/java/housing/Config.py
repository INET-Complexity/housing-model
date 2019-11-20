# Configuration file where all parameter values are set

##################################################
################ General comments ################
##################################################

# Seed for random number generation is set by calling the program with argument
# "-seed <your_seed>", where <your_seed> must be a positive integer. In the
# absence of this argument, seed is set from machine time

#*************************************************************************************************
# Class to encapsulate all the configuration parameters of the model. It also contains all methods
# needed to read these parameter values from a configuration properties file.
#
# @author Adrian Carro
# @since 20/02/2017
#
#************************************************************************************************/
class Config:
    #---------------------------------#
    #----- Fields and subclasses -----#
    #---------------------------------#

    #** Declaration of parameters **/

    ##################################################
    ######## General model control parameters ########
    ##################################################

    # Seed for random number generator (int)
    SEED = 1
    # Simulation duration in time steps (int)
    N_STEPS = 6000
    # Time steps before recording statistics, initialisation time (int)
    TIME_TO_START_RECORDING = 0
    # Number of simulations to run (int)
    N_SIMS = 1
    # True to write time series for each core indicator (boolean)
    recordCoreIndicators = False
    # True to write micro data for each transaction made (boolean)
    recordMicroData = False

    ##################################################
    ################ House parameters ################
    ##################################################

    # Number of quality bands for houses (int)
    N_QUALITY = 48

    ##################################################
    ########### Housing market parameters ############
    ##################################################

    # Time, in days, that a house remains under offer (int)
    DAYS_UNDER_OFFER = 7
    # Smallest proportional increase in price that can cause a gazump (double)
    BIDUP = 1.0075
    # Decay constant for the exponential moving average of sale prices (double)
    MARKET_AVERAGE_PRICE_DECAY = 0.25
    # Initial housing price index, HPI (double)
    # TODO: Reference for this value and justification for this discounting are needed! (the parameter is declared, but no source nor justification is given)
    INITIAL_HPI = 0.8
    # Median house price (double)
    # TODO: Replace by the 2011 value
    HPI_MEDIAN = 195000.0
    # Shape parameter for the log-normal distribution of housing prices, taken from the ONS (2013) house price index data tables, table 34 (double)
    # TODO: Replace by the 2011 value, compare with Land Registry Paid Price data and decide whether to use real distribution
    HPI_SHAPE = 0.555
    # Profit margin for buy-to-let investors (double)
    # Yield on rent had average 6% between 2009/01 and 2015/01, minimum in 2009/10 maximum in 2012/04 peak-to-peak amplitude of 0.4%. Source: Bank of England, unpublished analysis based on Zoopla/Land Registry matching (Philippe Bracke)
    RENT_GROSS_YIELD = 0.05

    ##################################################
    ############# Demographic parameters #############
    ##################################################

    # Target number of households (int)
    TARGET_POPULATION = 10000
    # Future birth rate (births per year per capita), calibrated with flux of FTBs, Council of Mortgage Lenders Regulated Mortgage Survey, 2015 (double)
    # TODO: Also described as "calibrated against average advances to first time buyers, core indicators 1987-2006". Check which explanation holds and replace by the 2011 value.
    FUTURE_BIRTH_RATE = 0.018

    ##################################################
    ############## Household parameters ##############
    ##################################################

    # Monthly percentage growth of financial investments (double)
    RETURN_ON_FINANCIAL_WEALTH = 0.002
    # Average number of months a tenant will stay in a rented house (int)
    # Source: ARLA - Members survey of the Private Rented Sector Q4 2013
    TENANCY_LENGTH_AVERAGE = 18
    # Standard deviation of the noise in determining the tenancy length (int)
    TENANCY_LENGTH_EPSILON = 6

    ##################################################
    ######### Household behaviour parameters #########
    ##################################################

    ############# Buy-To-Let parameters ##############
    # Prior probability of being (wanting to be) a BTL investor (double)
    # TODO: Shouldn't this be 4% according to the article?
    P_INVESTOR = 0.16
    # Minimum income percentile for a household to be a BTL investor (double)
    MIN_INVESTOR_PERCENTILE = 0.5
    # Weight that fundamentalists put on cap gain (double)
    FUNDAMENTALIST_CAP_GAIN_COEFF = 0.5
    # Weight that trend-followers put on cap gain (double)
    TREND_CAP_GAIN_COEFF = 0.9
    # Probability that a BTL investor is a fundamentalist versus a trend-follower (double)
    P_FUNDAMENTALIST = 0.5
    # Chooses between two possible equations for BTL investors to make their buy/sell decisions (boolean)
    BTL_YIELD_SCALING = False

    ################ Rent parameters #################
    # Desired proportion of income to be spent on rent (double)
    DESIRED_RENT_INCOME_FRACTION = 0.33
    # Annual psychological cost of renting (double)
    # TODO: This value comes from 1.1/12.0... Where does that come from?
    PSYCHOLOGICAL_COST_OF_RENTING = 0.0916666666667
    # Sensitivity parameter of the decision between buying and renting (double)
    # TODO: This value comes from 1.0/3500.0... Where does that come from?
    SENSITIVITY_RENT_OR_PURCHASE = 0.000285714285714

    ############### General parameters ###############
    # If the ratio between the buyer's bank balance and the house price is above this,
    # payment will be made fully in cash (double)
    # Calibrated against mortgage approval/housing transaction ratio, core indicators average 1987-2006
    # TODO: Find these sources and clarify this calibration!
    BANK_BALANCE_FOR_CASH_DOWNPAYMENT = 2.0
    # Weight assigned to current trend when computing expectations
    # Dampening or multiplier factor, depending on its value being <1 or >1, for the current trend when computing expectations as in
    # HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt (double)
    # TODO: According to John Muellbauer, this is a dampening factor (<1). Find a reference for this!
    HPA_EXPECTATION_FACTOR = 0.5
    # Number of years of the HPI record to check when computing the annual HPA, i.e., how much backward looking households are (int)
    HPA_YEARS_TO_CHECK = 1
    # Average period, in years, for which owner-occupiers hold their houses (double)
    # British housing survey 2008
    HOLD_PERIOD = 11.0

    ######### Sale price reduction parameters ########
    # This subsection was calibrated against Zoopla data at the BoE
    # Monthly probability of reducing the price of a house on the market (double)
    # This value comes from 1.0-0.945
    P_SALE_PRICE_REDUCE = 0.055
    # Mean percentage reduction for prices of houses on the market (double)
    REDUCTION_MU = 1.603
    # Standard deviation of percentage reductions for prices of houses on the market (double)
    REDUCTION_SIGMA = 0.617

    ############# Comsumption parameters #############
    # Fraction of the monthly budget allocated for consumption, being the monthly
    # budget equal to the bank balance minus the minimum desired bank balance (double)
    CONSUMPTION_FRACTION = 0.5
    # Fraction of Government support representing the amount necessarily spent monthly by
    # all households as essential consumption (double)
    ESSENTIAL_CONSUMPTION_FRACTION = 0.8

    ######### Initial sale price parameters ##########
    # Initial markup over average price of same quality houses (double)
    # TODO: Note says that, according to BoE calibration, this should be around 0.2. Check and solve this!
    SALE_MARKUP = 0.04
    # Weight of the days-on-market effect (double)
    SALE_WEIGHT_DAYS_ON_MARKET = 0.011
    # Standard deviation of the noise (double)
    SALE_EPSILON = 0.05

    ##### Buyer's desired expenditure parameters #####
    # Scale, number of annual salaries the buyer is willing to spend for buying a house (double)
    # TODO: This has been macro-calibrated against owner-occupier LTI and LTV ration, core indicators average 1987-2006. Find sources!
    BUY_SCALE = 4.5
    # Weight given to house price appreciation when deciding how much to spend for buying a house (double)
    BUY_WEIGHT_HPA = 0.08
    # Standard deviation of the noise (double)
    BUY_EPSILON = 0.14

    ############ Demanded rent parameters ############
    # Markup over average rent demanded for houses of the same quality (double)
    RENT_MARKUP = 0.00
    # Number of months on the market in an equilibrium situation (double)
    RENT_EQ_MONTHS_ON_MARKET = 6.0
    # Standard deviation of the noise (double)
    RENT_EPSILON = 0.05
    # Maximum period of time BTL investors are ready to wait to get back their investment through rents,
    # this determines the minimum rent they are ready to accept (double)
    # TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: This parameter and its associated mechanism are not declared in the article! Need to declare and reference!
    RENT_MAX_AMORTIZATION_PERIOD = 20.833333333
    # Percentage reduction of demanded rent for every month the property is in the market, not rented (double)
    RENT_REDUCTION = 0.05

    ############# Downpayment parameters #############
    # Minimum income percentile to consider any downpayment, below this level, downpayment is set to 0 (double)
    # TODO: Calibrated against PSD data, need clearer reference or disclose distribution!
    DOWNPAYMENT_MIN_INCOME = 0.3
    # TODO: Both functional form and parameters are micro-calibrated against BoE data. Need reference or disclose distribution!
    # Scale parameter for the log-normal distribution of downpayments by first-time-buyers (double)
    DOWNPAYMENT_FTB_SCALE = 10.30
    # Shape parameter for the log-normal distribution of downpayments by first-time-buyers (double)
    DOWNPAYMENT_FTB_SHAPE = 0.9093
    # Scale parameter for the log-normal distribution of downpayments by owner-occupiers (double)
    DOWNPAYMENT_OO_SCALE = 11.155
    # Shape parameter for the log-normal distribution of downpayments by owner-occupiers (double)
    DOWNPAYMENT_OO_SHAPE = 0.7538
    # Average downpayment, as percentage of house price, by but-to-let investors (double)
    # TODO: Said to be calibrated to match LTV ratios, but no reference is given. Need reference!
    # TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: Functional form slightly different to the one presented in the article
    DOWNPAYMENT_BTL_MEAN = 0.3
    # Standard deviation of the noise (double)
    DOWNPAYMENT_BTL_EPSILON = 0.1

    ######## Desired bank balance parameters #########
    # Micro-calibrated to match the log-normal relationship between wealth and income from the Wealth and Assets Survey
    # Log-normal function parameter (double)
    DESIRED_BANK_BALANCE_ALPHA = -32.0013877
    # Log-normal function parameter (double)
    DESIRED_BANK_BALANCE_BETA = 4.07
    # Standard deviation of a noise, it states a propensity to save (double)
    DESIRED_BANK_BALANCE_EPSILON = 0.1

    ########## Selling decision parameters ###########
    # Weight of houses per capita effect
    DECISION_TO_SELL_ALPHA = 4.0
    # Weight of interest rate effect
    DECISION_TO_SELL_BETA = 5.0
    # TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: fudge parameter, explicitly explained otherwise in the article
    DECISION_TO_SELL_HPC = 0.05
    # TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: fudge parameter, explicitly explained otherwise in the article
    DECISION_TO_SELL_INTEREST = 0.03

    ######### BTL buy/sell choice parameters #########
    # Shape parameter, or intensity of choice on effective yield (double)
    BTL_CHOICE_INTENSITY = 50.0
    # Minimun bank balance, as a percentage of the desired bank balance, to buy new properties (double)
    # TODO: @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Attention: This parameter and its associated mechanism are not declared in the article! Need to declare and reference!
    BTL_CHOICE_MIN_BANK_BALANCE = 0.75

    ##################################################
    ################ Bank parameters #################
    ##################################################
    # TODO: We need references or justification for all these values!

    # Mortgage duration in years (int)
    MORTGAGE_DURATION_YEARS = 25
    # Bank initial base-rate, which remains currently unchanged (double)
    BANK_INITIAL_BASE_RATE = 0.005
    # Bank's target supply of credit per household per month (double)
    BANK_CREDIT_SUPPLY_TARGET = 380
    # Maximum LTV ratio that the private bank would allow for first-time-buyers (double)
    BANK_MAX_FTB_LTV = 0.95
    # Maximum LTV ratio that the private bank would allow for owner-occupiers (double)
    BANK_MAX_OO_LTV = 0.9
    # Maximum LTV ratio that the private bank would allow for BTL investors (double)
    BANK_MAX_BTL_LTV = 0.8
    # Maximum LTI ratio that the private bank would allow for first-time-buyers (private bank's hard limit) (double)
    BANK_MAX_FTB_LTI = 6.0
    # Maximum LTI ratio that the private bank would allow for owner-occupiers (private bank's hard limit) (double)
    BANK_MAX_OO_LTI = 6.0

    ##################################################
    ############# Central bank parameters ############
    ##################################################
    # TODO: We need references or justification for all these values! Also, need to clarify meaning of "when not regulated"

    # Maximum LTI ratio that the bank would allow for first-time-buyers when not regulated (double)
    CENTRAL_BANK_MAX_FTB_LTI = 6.0
    # Maximum LTI ratio that the bank would allow for owner-occupiers when not regulated (double)
    CENTRAL_BANK_MAX_OO_LTI = 6.0
    # Maximum fraction of mortgages that the bank can give over the LTI ratio limit (double)
    CENTRAL_BANK_FRACTION_OVER_MAX_LTI = 0.15
    # Maximum fraction of the household's income to be spent on mortgage repayments under stressed conditions (double)
    CENTRAL_BANK_AFFORDABILITY_COEFF = 0.5
    # Interest rate under stressed condition for BTL investors when calculating interest coverage ratios, ICR (double)
    CENTRAL_BANK_BTL_STRESSED_INTEREST = 0.05
    # Interest coverage ratio (ICR) limit imposed by the central bank (double)
    CENTRAL_BANK_MAX_ICR = 1.25

    ##################################################
    ############ Construction parameters #############
    ##################################################
    # TODO: We need references or justification for all these values!

    # Target ratio of houses per household (double)
    CONSTRUCTION_HOUSES_PER_HOUSEHOLD = 0.82

    ##################################################
    ############# Government parameters ##############
    ##################################################

    # General personal allowance to be deducted when computing taxable income (double)
    GOVERNMENT_GENERAL_PERSONAL_ALLOWANCE = 9440.0
    # Limit of income above which personal allowance starts to decrease £1 for every £2 of income above this limit (double)
    GOVERNMENT_INCOME_LIMIT_FOR_PERSONAL_ALLOWANCE = 100000.0
    # Minimum monthly earnings for a married couple from income support (double)
    # TODO: We need a reference or justification for this value!
    GOVERNMENT_MONTHLY_INCOME_SUPPORT = 492.7

    ##################################################
    ############## Collectors parameters #############
    ##################################################

    # Approximate number of households in UK, used to scale up results for core indicators (double)
    # TODO: Reference needed
    UK_HOUSEHOLDS = 26.5e6
    # Whether to record mortgage statistics (boolean)
    MORTGAGE_DIAGNOSTICS_ACTIVE = true

    ##################################################
    ################# Data addresses #################
    ##################################################

    ############ Government data addresses ###########
    # TODO: We need clearer references for the values contained in these files! Also, current values are for 2013/2014, replace for 2011!
    DATA_TAX_RATES = "src/main/resources/TaxRates.csv"
    DATA_NATIONAL_INSURANCE_RATES = "src/main/resources/NationalInsuranceRates.csv"

    ############ Lifecycle data addresses ############
    DATA_INCOME_GIVEN_AGE = "src/main/resources/IncomeGivenAge.csv"

    ########### Demographics data addresses ##########
    # Target probability density of age of representative person in the household at time t=0, calibrated against LCFS (2012)
    DATA_HOUSEHOLD_AGE_AT_BIRTH_PDF = "src/main/resources/HouseholdAgeAtBirthPDF.csv"
    DATA_DEATH_PROB_GIVEN_AGE = "src/main/resources/DeathProbGivenAge.csv"

    #** Construction of objects to contain derived parameters and constants **/

    # Create object containing all constants
    constants = Constants

    # Finally, create object containing all derived parameters
    derivedParams = DerivedParams

#*
# Class to contain all parameters which are not read from the configuration (.properties) file, but derived,
# instead, from these configuration parameters
#/
class DerivedParams:
    # Housing market parameters
    public int HPI_RECORD_LENGTH   # Number of months to record HPI (to compute price growth at different time scales)
    double MONTHS_UNDER_OFFER      # Time (in months) that a house remains under offer
    double T                       # Characteristic number of data-points over which to average market statistics
    public double E                # Decay constant for averaging days on market (in transactions)
    public double G                # Decay constant for averageListPrice averaging (in transactions)
    public double HPI_LOG_MEDIAN   # Logarithmic median house price (scale parameter of the log-normal distribution)
    double HPI_REFERENCE           # Mean of reference house prices
    # Household behaviour parameters: general
    double MONTHLY_P_SELL          # Monthly probability for owner-occupiers to sell their houses
    # Bank parameters
    int N_PAYMENTS                 # Number of monthly repayments (mortgage duration in months)
    # House rental market parameters
    public double K                # Decay factor for exponential moving average of gross yield from rentals (averageSoldGrossYield)
    public double KL               # Decay factor for long-term exponential moving average of gross yield from rentals (longTermAverageGrossYield)
    # Collectors parameters
    double AFFORDABILITY_DECAY 	# Decay constant for the exponential moving average of affordability

    def getAffordabilityDecay(self) -> float:
        return self.AFFORDABILITY_DECAY

    def getHPIRecordLength(self) -> int:
        return self.HPI_RECORD_LENGTH

    def getHPIReference(self) -> float:
        return self.HPI_REFERENCE

# Class to contain all constants (not read from the configuration file nor derived from it)
class Constants:
    DAYS_IN_MONTH = 30
    MONTHS_IN_YEAR = 12

#-------------------#
#----- Methods -----#
#-------------------#

def isMortgageDiagnosticsActive() -> bool:
    return MORTGAGE_DIAGNOSTICS_ACTIVE

def getUKHouseholds() -> float:
    return UK_HOUSEHOLDS

def getPInvestor() -> float:
    return P_INVESTOR

# Method to compute and set values for all derived parameters
def setDerivedParams() -> None:
    # Housing market parameters
    derivedParams.HPI_RECORD_LENGTH = HPA_YEARS_TO_CHECK*constants.MONTHS_IN_YEAR + 3  # Plus three months in a quarter
    derivedParams.MONTHS_UNDER_OFFER = (double)DAYS_UNDER_OFFER/constants.DAYS_IN_MONTH
    derivedParams.T = 0.02*TARGET_POPULATION                   # TODO: Clarify where does this 0.2 come from, and provide explanation for this formula
    derivedParams.E = Math.exp(-1.0/derivedParams.T)           # TODO: Provide explanation for this formula
    derivedParams.G = Math.exp(-N_QUALITY/derivedParams.T)     # TODO: Provide explanation for this formula
    derivedParams.HPI_LOG_MEDIAN = Math.log(HPI_MEDIAN)
    derivedParams.HPI_REFERENCE = Math.exp(derivedParams.HPI_LOG_MEDIAN + HPI_SHAPE*HPI_SHAPE/2.0)
    # Household behaviour parameters: general
    derivedParams.MONTHLY_P_SELL = 1.0/(HOLD_PERIOD*constants.MONTHS_IN_YEAR)
    # Bank parameters
    derivedParams.N_PAYMENTS = MORTGAGE_DURATION_YEARS*constants.MONTHS_IN_YEAR
    # House rental market parameters
    derivedParams.K = Math.exp(-10000.0/(TARGET_POPULATION*50.0))  # TODO: Are these decay factors well-suited? Any explanation, reasoning behind the numbers chosen?
    derivedParams.KL = Math.exp(-10000.0/(TARGET_POPULATION*50.0*200.0))   # TODO: Also, they are not reported in the paper!
    # Collectors parameters
    derivedParams.AFFORDABILITY_DECAY = Math.exp(-1.0/100.0)
