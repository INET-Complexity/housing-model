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
	public double monthlyPaymentFactor() {
		return(k);
	}

	/*****************************
	 * Use this to arrange a Mortgage and get a MortgageApproval object.
	 * 
	 * @param h The household that is requesting the mortgage.
	 * @param housePrice The price of the house that 'h' wants to buy
	 * @param isHome true if 'h' plans to live in the house.
	 * @return The MortgageApproval object, or NULL if the mortgage is declined
	 ****************************/
	public MortgageApproval requestLoan(Household h, double housePrice, boolean isHome) {
		MortgageApproval approval = new MortgageApproval();
		double r = mortgageInterestRate()/12.0; // monthly interest rate
		double ltv_principal, pdi_principal, lti_principal;
		
		
		if(housePrice > h.annualEmploymentIncome / config.PHI) {
			System.out.println("Failed ITV constraint");
			return(null); // ITV constraint not satisfied
		}

		// --- calculate maximum allowable principal
		ltv_principal = housePrice*loanToValue(h, isHome);
		pdi_principal = Math.max(0.0,h.getMonthlyDiscretionaryIncome())/monthlyPaymentFactor();
		lti_principal = h.annualEmploymentIncome * config.LTI;
		approval.principal = Math.min(ltv_principal, pdi_principal);
		approval.principal = Math.min(approval.principal, lti_principal);
		approval.monthlyPayment = approval.principal*monthlyPaymentFactor();
		/**
		double pdi;
		approval.principal = housePrice*loanToValue(h, isHome);
		approval.monthlyPayment = approval.principal*monthlyPaymentFactor();
		pdi = Math.max(0.0,h.monthlyPersonalDiscretionaryIncome());
		if(approval.monthlyPayment > pdi) {
			// constrained by PDI constraint: increase downpayment
			approval.principal = pdi/monthlyPaymentFactor();
			approval.monthlyPayment = pdi;
		}
		**/
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
			itv_distribution[1][(int)Math.min(100.0*h.annualEmploymentIncome / housePrice,100.0)] += 1.0-config.STATS_DECAY;
			lti_distribution[1][(int)Math.min(10.0*approval.principal/(h.annualEmploymentIncome),100.0)] += 1.0-config.STATS_DECAY;
			approved_mortgages[0][approved_mortgages_i] = approval.principal/(h.annualEmploymentIncome);
			approved_mortgages[1][approved_mortgages_i] = approval.downPayment/(h.annualEmploymentIncome);
			approved_mortgages_i += 1;
			if(approved_mortgages_i == Config.ARCHIVE_LEN) approved_mortgages_i = 0;

		}
		
		
		return(approval);
	}

	/*****************************************
	 * Find the maximum mortgage that this mortgage-lender will approve
	 * to a household.
	 * 
	 * @param h The household who is applying for the mortgage
	 * @param isHome true if 'h' plans to live in the house
	 * @return The maximum value of house that this mortgage-lender is willing
	 * to approve a mortgage for.
	 ****************************************/
	public double getMaxMortgage(Household h, boolean isHome) {
		double ltv_max; // loan to value constraint
		double itv_max; // income to value constraint
		double pdi_max; // disposable income constraint
		double lti_max; // loan to income constraint
		
		ltv_max = h.bankBalance/(1.0 - loanToValue(h, isHome));
		itv_max = h.annualEmploymentIncome / config.PHI;
		pdi_max = h.bankBalance + Math.max(0.0,h.getMonthlyDiscretionaryIncome())/monthlyPaymentFactor();
		lti_max = h.annualEmploymentIncome * config.LTI/loanToValue(h,isHome);
		
		pdi_max = Math.min(pdi_max, ltv_max); // find minimum
		pdi_max = Math.min(pdi_max, lti_max);
		pdi_max = Math.min(pdi_max, itv_max);
		pdi_max = Math.floor(pdi_max*100.0)/100.0; // round down to nearest penny
		
		return(pdi_max);
	}

	/**********************************************
	 * Get the Loan-To-Value ratio applicable to a given household.
	 * 
	 * @param h The houshold that is applying for the mortgage
	 * @param isHome true if 'h' plans to live in the house
	 * @return The loan-to-value ratio applicable to the given household.
	 *********************************************/
	public double loanToValue(Household h, boolean isHome) {
		if(isHome) {
			if(h.isFirstTimeBuyer()) {
				return(1.0 - config.THETA_FTB);
			}
			return(1.0 - config.THETA_HOME);
		}
		return(1.0 - config.THETA_BTL);
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
