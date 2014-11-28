package housing;

/*************************************************
 * This class represents a mortgage-lender (i.e. a bank or building society),
 * rather than a deposit holder. Its only function is to approve/decline
 * mortgage requests, so this is where mortgage-lending policy is encoded.
 *  
 * 
 * @author daniel, davidrpugh
 *
 *************************************************/
public class Bank {

	static public class Config {
		public double THETA_FTB = 0.1; // first-time buyer haircut (LTV)
		public double THETA_HOME = 0.2; // home buyer haircut (LTV)
		public double THETA_BTL = 0.4; // buy-to-let buyer haircut (LTV)
		public double PHI = 1.0/100.0;//1.0/8.0;//0.25; // minimum income-to-value (ITV) ratio
		public double LTI = 4.5;//6.5;//4.5; // loan-to-income ratio. Capped at 4.5 for all lenders from 01/10/14 
		public int    N_PAYMENTS = 12*25; // number of monthly repayments
		public boolean RECORD_STATS = true; // record mortgage statistics?		
		public double STATS_DECAY = 0.9; 	// Decay constant (per step) for exp averaging of stats
		public double AFFORDABILITY_DECAY = Math.exp(-1.0/100.0); 	// Decay constant for exp averaging of affordability
		static public int ARCHIVE_LEN = 1000; // number of mortgage approvals to remember
	}
	
	/********************************
	 * Constructor. This just sets up a few
	 * pre-computed values.
	 ********************************/
	public Bank() {
		this(new Bank.Config());
		if(config.RECORD_STATS) {
			for(int i=0; i<=100; ++i) { // set up x-values for distribution
				ltv_distribution[0][i] = i/100.0;
				itv_distribution[0][i] = i/100.0;
				lti_distribution[0][i] = i/100.0;
				ltv_distribution[1][i] = 0.0;
				itv_distribution[1][i] = 0.0;
				lti_distribution[1][i] = 0.0;
			}
		}
	}
	
	public Bank(Bank.Config c) {
		config = c;
		double r = mortgageInterestRate()/12.0;
		k = r/(1.0 - Math.pow(1.0+r, -config.N_PAYMENTS));
	}
	
	/******************************
	 * Get the interest rate on mortgages.
	 * @return The interest rate on mortgages.
	 *****************************/
	public double mortgageInterestRate() {
		return(0.03);
	}
	
	/*******************************
	 * Get the monthly payment on a mortgage as a fraction of the mortgage principle.
	 * @return The monthly payment fraction.
	 *******************************/
	public double monthlyInterestFactor() {
		return(k);
	}

	/*****************************
	 * Use this to arrange a Mortgage and get a MortgageApproval object.
	 * 
	 * @param h The household that is requesting the mortgage.
	 * @param housePrice The price of the house that 'h' wants to buy
	 * @return The MortgageApproval object, or NULL if the mortgage is declined
	 ****************************/
	public MortgageApproval requestLoan(Household h, double housePrice) {
		MortgageApproval approval = new MortgageApproval();
		double r = mortgageInterestRate()/12.0; // monthly interest rate
		double ltv_principal, pdi_principal, lti_principal;
		

		if(housePrice > h.getAnnualDiscretionaryIncome() / config.PHI) {
			System.out.println("Failed ITV constraint");
			return(null); // ITV constraint not satisfied
		}

		// --- calculate maximum allowable principal
		ltv_principal = housePrice*(1 - mortgageHaircut(h));
		pdi_principal = Math.max(0.0,h.getMonthlyDiscretionaryIncome())/ monthlyInterestFactor();
		lti_principal = h.getAnnualDiscretionaryIncome() * config.LTI;
		approval.principal = Math.min(ltv_principal, pdi_principal);
		approval.principal = Math.min(approval.principal, lti_principal);
		approval.monthlyPayment = approval.principal* monthlyInterestFactor();

		approval.downPayment = housePrice - approval.principal;
		if(h.bankBalance < approval.downPayment) {
			System.out.println("Failed down-payment constraint: bank balance = "+h.bankBalance+" Downpayment = "+approval.downPayment);
			return(null);
		}
		
		approval.nPayments = config.N_PAYMENTS;
		approval.monthlyInterestRate = r;
		
		if(config.RECORD_STATS) {
			if(h.isFirstTimeBuyer()) {
				affordability = config.AFFORDABILITY_DECAY*affordability + (1.0-config.AFFORDABILITY_DECAY)*approval.monthlyPayment/h.getMonthlyEmploymentIncome();
			}
			ltv_distribution[1][(int)(100.0*approval.principal/housePrice)] += 1.0-config.STATS_DECAY;
			itv_distribution[1][(int)Math.min(100.0*h.getAnnualDiscretionaryIncome() / housePrice,100.0)] += 1.0-config.STATS_DECAY;
			lti_distribution[1][(int)Math.min(10.0*approval.principal/(h.getAnnualDiscretionaryIncome()),100.0)] += 1.0-config.STATS_DECAY;
			approved_mortgages[0][approved_mortgages_i] = approval.principal/(h.getAnnualDiscretionaryIncome());
			approved_mortgages[1][approved_mortgages_i] = approval.downPayment/(h.getAnnualDiscretionaryIncome());
			approved_mortgages_i += 1;
			if(approved_mortgages_i == Config.ARCHIVE_LEN) approved_mortgages_i = 0;

		}
		
		
		return(approval);
	}

	private boolean satisfiesLoanToValue(double downPayment,
										 double housePrice,
										 double haircut) {
		return (downPayment / housePrice) >= haircut;
	}

	private boolean satisfiesIncomeToValue(double discretionaryIncome,
										   double housePrice,
										   double itvConstraint) {
		return (discretionaryIncome / housePrice) >= itvConstraint;
	}

	/**
	 * Bank pre-approves each household for a mortgage under the assumption that
	 * the household uses its entire liquid wealth as a down payment.
	 *
	 * @param household
	 * @return The maximum allowable house price for a given household.
	 */
	public double preApproveMortgage(Household household) {
		double maxHousePriceLTV; // loan to value
		double maxHousePriceITV; // income to value
		double maxHousePriceLTI; // loan to income
		double maxHousePrice;

		// maximum allowable house price under LTV constraint
		double expectedDownPayment = household.bankBalance;
		maxHousePriceLTV = expectedDownPayment / mortgageHaircut(household);

		// maximum allowable house price under ITV constraint
		double discretionaryIncome = household.getAnnualDiscretionaryIncome();
		maxHousePriceITV = discretionaryIncome / config.PHI;

		// maximum allowable house price under LTI constraint
		double loanToValueRatio = 1 - mortgageHaircut(household);
		maxHousePriceLTI = discretionaryIncome * config.LTI / loanToValueRatio;

		// maximum allowable house price is min of the above max house prices
		maxHousePrice = Math.min(maxHousePriceLTV, maxHousePriceITV);
		maxHousePrice = Math.min(maxHousePrice, maxHousePriceLTI);

		return maxHousePrice;
	}

	/**
	 * The percentage of the purchase price that a household is expected to
	 * contribute as a down payment. The mortgage "haircut" depends on whether
	 * the household is a "first-time" buyer, a current homeowner, or a
	 * "buy-to-let" property investor.
	 *
	 * @param household
	 * @return The mortgage "haircut".
	 */
	private double mortgageHaircut(Household household) {
		double haircut;

		if (household.isFirstTimeBuyer()) {
			haircut = config.THETA_FTB;
		} else if (household.isHomeOwner()) {
			haircut = config.THETA_HOME;
		} else {
			haircut = config.THETA_BTL;
		}

		return haircut;
	}

	public Bank.Config config;
	
	public double k; // principal to monthly payment factor
	/** First time buyer affordability **/
	public double affordability = 0.0;
	public double [][] ltv_distribution = new double[2][101]; // index/100 = LTV
	public double [][] itv_distribution = new double[2][101]; // index/100 = ITV
	public double [][] lti_distribution = new double[2][101]; // index/10 = LTI
	public double [][] approved_mortgages = new double [2][Config.ARCHIVE_LEN]; // (loan/income, downpayment/income) pairs
	public int approved_mortgages_i;
	
}
