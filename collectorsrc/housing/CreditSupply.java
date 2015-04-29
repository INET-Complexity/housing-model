package housing;

public class CreditSupply {
	public double AFFORDABILITY_DECAY = Math.exp(-1.0/100.0); 	// Decay constant for exp averaging of affordability
	public double STATS_DECAY = 0.98; 	// Decay constant (per step) for exp averaging of stats
	public int ARCHIVE_LEN = 1000; // number of mortgage approvals to remember
	public boolean DIAGNOSTICS_ACTIVE = true; // record mortgage statistics?		
	public int 	HISTOGRAM_NBINS = 101;
	
	public double affordability = 0.0;
	public double [][] ltv_distribution = new double[2][HISTOGRAM_NBINS]; // index/100 = LTV
	public double [][] lti_distribution = new double[2][HISTOGRAM_NBINS]; // index/10 = LTI
	public double [][] approved_mortgages = new double [2][ARCHIVE_LEN]; // (loan/income, downpayment/income) pairs
	public int approved_mortgages_i;

	public CreditSupply() {
		for(int i=0; i<HISTOGRAM_NBINS; ++i) { // set up x-values for distribution
			ltv_distribution[0][i] = i/(HISTOGRAM_NBINS-1);
			lti_distribution[0][i] = i/(HISTOGRAM_NBINS-1);
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
        for(i=0; i<HISTOGRAM_NBINS; ++i) {
			ltv_distribution[1][i] *= STATS_DECAY;
			lti_distribution[1][i] *= STATS_DECAY;
        }
	}

	// ---- Mason stuff
	// ----------------
	public String desLTI() {return("Loan to Income constraint on mortgages");}
	public String desTHETA_FTB() {return("Loan to Value haircut for first time buyers");}
	public String desTHETA_HOME() {return("Loan to Value haircut for homeowners");}
	public String desTHETA_BTL() {return("Loan to Value haircut for buy-to-let investors");}
	public String desN_PAYMENTS() {return("Number of monthly repayments in a mortgage");}
	public double getLTI() {
		return(Model.bank.config.LTI);
	}		
	public void setLTI(double x) {
		Model.bank.config.LTI = x;
	}
	public double getTHETA_FTB() {
		return Model.bank.config.THETA_FTB;
	}
	public void setTHETA_FTB(double tHETA_FTB) {
		Model.bank.config.THETA_FTB = tHETA_FTB;
	}
	public double getTHETA_HOME() {
		return Model.bank.config.THETA_HOME;
	}
	public void setTHETA_HOME(double tHETA_HOME) {
		Model.bank.config.THETA_HOME = tHETA_HOME;
	}
	public double getTHETA_BTL() {
		return Model.bank.config.THETA_BTL;
	}
	public void setTHETA_BTL(double tHETA_BTL) {
		Model.bank.config.THETA_BTL = tHETA_BTL;
	}
	public int getN_PAYMENTS() {
		return Model.bank.config.N_PAYMENTS;
	}
	public void setN_PAYMENTS(int n_PAYMENTS) {
		Model.bank.config.N_PAYMENTS = n_PAYMENTS;
	}

}
