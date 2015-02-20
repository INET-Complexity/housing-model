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

	//////////////////////////////////////////////////////////////////////////////////////
	// Configuration
	//////////////////////////////////////////////////////////////////////////////////////
	static public class Config implements Configuration {
		public double THETA_FTB = 0.1; // first-time buyer haircut (LTV)
		public double THETA_HOME = 0.2; // home buyer haircut (LTV)
		public double THETA_BTL = 0.4; // buy-to-let buyer haircut (LTV)
		public double LTI = 6.5;//6.5;//4.5; // loan-to-income ratio. Capped at 4.5 for all lenders from 01/10/14
		public int    N_PAYMENTS = 12*25; // number of monthly repayments

		// ---- Mason stuff
		// ----------------
		public String desLTI() {return("Loan to Income constraint on mortgages");}
		public String desTHETA_FTB() {return("Loan to Value haircut for first time buyers");}
		public String desTHETA_HOME() {return("Loan to Value haircut for homeowners");}
		public String desTHETA_BTL() {return("Loan to Value haircut for buy-to-let investors");}
		public String desN_PAYMENTS() {return("Number of monthly repayments in a mortgage");}
		public double getLTI() {
			return(LTI);
		}		
		public void setLTI(double x) {
			LTI = x;
		}
		public double getTHETA_FTB() {
			return THETA_FTB;
		}
		public void setTHETA_FTB(double tHETA_FTB) {
			THETA_FTB = tHETA_FTB;
		}
		public double getTHETA_HOME() {
			return THETA_HOME;
		}
		public void setTHETA_HOME(double tHETA_HOME) {
			THETA_HOME = tHETA_HOME;
		}
		public double getTHETA_BTL() {
			return THETA_BTL;
		}
		public void setTHETA_BTL(double tHETA_BTL) {
			THETA_BTL = tHETA_BTL;
		}
		public int getN_PAYMENTS() {
			return N_PAYMENTS;
		}
		public void setN_PAYMENTS(int n_PAYMENTS) {
			N_PAYMENTS = n_PAYMENTS;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Diagnostics
	//////////////////////////////////////////////////////////////////////////////////////
	static public class Diagnostics {
		public double AFFORDABILITY_DECAY = Math.exp(-1.0/100.0); 	// Decay constant for exp averaging of affordability
		public double STATS_DECAY = 0.98; 	// Decay constant (per step) for exp averaging of stats
		public int ARCHIVE_LEN = 1000; // number of mortgage approvals to remember
		public boolean DIAGNOSTICS_ACTIVE = true; // record mortgage statistics?		

		public double affordability = 0.0;
		public double [][] ltv_distribution = new double[2][101]; // index/100 = LTV
		public double [][] lti_distribution = new double[2][101]; // index/10 = LTI
		public double [][] approved_mortgages = new double [2][ARCHIVE_LEN]; // (loan/income, downpayment/income) pairs
		public int approved_mortgages_i;		

		public Diagnostics() {
			for(int i=0; i<=100; ++i) { // set up x-values for distribution
				ltv_distribution[0][i] = i/100.0;
				lti_distribution[0][i] = i/100.0;
				ltv_distribution[1][i] = 0.0;
				lti_distribution[1][i] = 0.0;
			}
		}

		public void recordLoan(Household h, MortgageApproval approval) {
			double housePrice;
			if(DIAGNOSTICS_ACTIVE) {
				housePrice = approval.principal + approval.downPayment;
				affordability = AFFORDABILITY_DECAY*affordability + (1.0-AFFORDABILITY_DECAY)*approval.monthlyPayment/h.getMonthlyEmploymentIncome();
				if(approval.principal > 1.0) {
					ltv_distribution[1][(int)(100.0*approval.principal/housePrice)] += (1.0-STATS_DECAY)/10.0;
					lti_distribution[1][(int)Math.min(10.0*approval.principal/h.annualEmploymentIncome,100.0)] += 1.0-STATS_DECAY;
				}
				approved_mortgages[0][approved_mortgages_i] = approval.principal/(h.getMonthlyEmploymentIncome()*12.0);
				approved_mortgages[1][approved_mortgages_i] = approval.downPayment/(h.getMonthlyEmploymentIncome()*12.0);
				approved_mortgages_i += 1;
				if(approved_mortgages_i == ARCHIVE_LEN) approved_mortgages_i = 0;
			}
		}
		
		public void step() {
			int i;
	        for(i=0; i<=100; ++i) {
				ltv_distribution[1][i] *= STATS_DECAY;
				lti_distribution[1][i] *= STATS_DECAY;
	        }
		}

		public double getAFFORDABILITY_DECAY() {
			return AFFORDABILITY_DECAY;
		}

		public void setAFFORDABILITY_DECAY(double aFFORDABILITY_DECAY) {
			AFFORDABILITY_DECAY = aFFORDABILITY_DECAY;
		}

		public double getSTATS_DECAY() {
			return STATS_DECAY;
		}

		public void setSTATS_DECAY(double sTATS_DECAY) {
			STATS_DECAY = sTATS_DECAY;
		}

		public int getARCHIVE_LEN() {
			return ARCHIVE_LEN;
		}

		public void setARCHIVE_LEN(int aRCHIVE_LEN) {
			ARCHIVE_LEN = aRCHIVE_LEN;
		}

		public boolean isDIAGNOSTICS_ACTIVE() {
			return DIAGNOSTICS_ACTIVE;
		}

		public void setDIAGNOSTICS_ACTIVE(boolean dIAGNOSTICS_ACTIVE) {
			DIAGNOSTICS_ACTIVE = dIAGNOSTICS_ACTIVE;
		}
	}
	
	/********************************
	 * Constructor. This just sets up a few
	 * pre-computed values.
	 ********************************/
	public Bank() {
		this(new Bank.Config());
	}
	
	public Bank(Bank.Config c) {
		config = c;
		double r = mortgageInterestRate()/12.0;
		k = r/(1.0 - Math.pow(1.0+r, -config.N_PAYMENTS));
		diagnostics = new Diagnostics();
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
	public MortgageApproval requestLoan(Household h, double housePrice, double desiredDownPayment, boolean isHome) {
		MortgageApproval approval = new MortgageApproval();
		double r = mortgageInterestRate()/12.0; // monthly interest rate
		double ltv_principal, pdi_principal, lti_principal;

		// --- calculate maximum allowable principal
		ltv_principal = housePrice*loanToValue(h, isHome);
		pdi_principal = Math.max(0.0,h.getMonthlyDisposableIncome())/monthlyPaymentFactor();
		lti_principal = h.annualEmploymentIncome * config.LTI;
		approval.principal = Math.min(ltv_principal, pdi_principal);
		approval.principal = Math.min(approval.principal, lti_principal);
		approval.downPayment = housePrice - approval.principal;
		
		if(h.bankBalance < approval.downPayment) {
			System.out.println("Failed down-payment constraint: bank balance = "+h.bankBalance+" Downpayment = "+approval.downPayment);
			System.out.println("isHome = "+isHome+" isFirstTimeBuyer = "+h.isFirstTimeBuyer());
			approval.downPayment = h.bankBalance;
//			return(null);
		}
		// --- allow larger downpayments
		if(desiredDownPayment > h.bankBalance) desiredDownPayment = h.bankBalance;
		if(desiredDownPayment > housePrice) desiredDownPayment = housePrice;
		if(desiredDownPayment > approval.downPayment) {
			approval.downPayment = desiredDownPayment;
			approval.principal = housePrice - desiredDownPayment;
		}
		
		approval.monthlyPayment = approval.principal*monthlyPaymentFactor();		
		approval.nPayments = config.N_PAYMENTS;
		approval.monthlyInterestRate = r;

		diagnostics.recordLoan(h, approval);
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
		double pdi_max; // disposable income constraint
		double lti_max; // loan to income constraint
		
		ltv_max = h.bankBalance/(1.0 - loanToValue(h, isHome));
		pdi_max = h.bankBalance + Math.max(0.0,h.getMonthlyDisposableIncome())/monthlyPaymentFactor();
		lti_max = h.annualEmploymentIncome * config.LTI/loanToValue(h,isHome);
		
		pdi_max = Math.min(pdi_max, ltv_max); // find minimum
		pdi_max = Math.min(pdi_max, lti_max);
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
		

	
	public Config 		config;
	public Diagnostics 	diagnostics;
	
	public double 		k; // principal to monthly payment factor
	/** First time buyer affordability **/
	
}
