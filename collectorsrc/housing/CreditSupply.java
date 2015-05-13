package housing;

public class CreditSupply {

	public CreditSupply() {
		for(int i=0; i<HISTOGRAM_NBINS; ++i) { // set up x-values for distribution
			btl_ltv_distribution[0][i] = i/(HISTOGRAM_NBINS-1.0);
			oo_ltv_distribution[0][i] = i/(HISTOGRAM_NBINS-1.0);
			oo_lti_distribution[0][i] = i/(HISTOGRAM_NBINS-1.0);
			btl_lti_distribution[0][i] = i/(HISTOGRAM_NBINS-1.0);
			btl_ltv_distribution[1][i] = 0.0;
			oo_ltv_distribution[1][i] = 0.0;
			oo_lti_distribution[1][i] = 0.0;
			btl_lti_distribution[1][i] = 0.0;
		}
		mortgageCounter = 0;
		approved_mortgages_index = 0;
		ftbCounter = 0;
		btlCounter = 0;
	}

	/***
	 * collect information for this timestep
	 */
	public void step() {
		int i;
        for(i=0; i<HISTOGRAM_NBINS; ++i) {
			btl_ltv_distribution[1][i] *= STATS_DECAY;
			oo_ltv_distribution[1][i] *= STATS_DECAY;
			oo_lti_distribution[1][i] *= STATS_DECAY;
			btl_lti_distribution[1][i] *= STATS_DECAY;
        }
        
        double oldTotalCredit = totalOOCredit + totalBTLCredit;
        totalOOCredit = 0.0;
        totalBTLCredit = 0.0;
        for(MortgageApproval m : Model.bank.mortgages) {
        	if(m.isBuyToLet) {
            	totalBTLCredit += m.principal;
        	} else {
        		totalOOCredit += m.principal;
        	}
        }
        netCreditGrowth = (totalOOCredit + totalBTLCredit - oldTotalCredit)/oldTotalCredit;
        nApprovedMortgages = mortgageCounter;
        nFTBMortgages = ftbCounter;
        nBTLMortgages = btlCounter;
        mortgageCounter = 0;
        ftbCounter = 0;
        btlCounter = 0;
	}

	/***
	 * record information for a newly issued mortgage
	 * @param h
	 * @param approval
	 */
	public void recordLoan(Household h, MortgageApproval approval) {
		double housePrice;
		if(DIAGNOSTICS_ACTIVE) {
			housePrice = approval.principal + approval.downPayment;
			affordability = AFFORDABILITY_DECAY*affordability + (1.0-AFFORDABILITY_DECAY)*approval.monthlyPayment/h.getMonthlyEmploymentIncome();
			if(approval.principal > 1.0) {
				if(approval.isBuyToLet) {
					btl_lti_distribution[1][(int)Math.min(10.0*approval.principal/h.annualEmploymentIncome,100.0)] += 1.0-STATS_DECAY;
					btl_ltv_distribution[1][(int)(100.0*approval.principal/housePrice)] += (1.0-STATS_DECAY)/10.0;
				} else {
					oo_lti_distribution[1][(int)Math.min(10.0*approval.principal/h.annualEmploymentIncome,100.0)] += 1.0-STATS_DECAY;
					oo_ltv_distribution[1][(int)(100.0*approval.principal/housePrice)] += (1.0-STATS_DECAY)/10.0;
				}
			}
			approved_mortgages[0][approved_mortgages_index] = approval.principal/(h.getMonthlyEmploymentIncome()*12.0);
			approved_mortgages[1][approved_mortgages_index] = approval.downPayment/(h.getMonthlyEmploymentIncome()*12.0);
			approved_mortgages_index += 1;
			if(approved_mortgages_index == ARCHIVE_LEN) approved_mortgages_index = 0;
			mortgageCounter += 1;
			if(approval.isFirstTimeBuyer) ftbCounter += 1;
			if(approval.isBuyToLet) btlCounter += 1;
		}
	}
	

	// ---- Mason stuff
	// ----------------
	public String desLTI() {return("Loan to Income constraint on mortgages");}
	public String desTHETA_FTB() {return("Loan to Value haircut for first time buyers");}
	public String desTHETA_HOME() {return("Loan to Value haircut for homeowners");}
	public String desTHETA_BTL() {return("Loan to Value haircut for buy-to-let investors");}
	public String desN_PAYMENTS() {return("Number of monthly repayments in a mortgage");}
	public double getBaseRate() {
		return Model.bank.getBaseRate();
	}
	public void setBaseRate(double rate) {
		Model.bank.setBaseRate(rate);
	}

	public double AFFORDABILITY_DECAY = Math.exp(-1.0/100.0); 	// Decay constant for exp averaging of affordability
	public double STATS_DECAY = 0.98; 	// Decay constant (per step) for exp averaging of stats
	public int ARCHIVE_LEN = 1000; // number of mortgage approvals to remember
	public boolean DIAGNOSTICS_ACTIVE = true; // record mortgage statistics?		
	public int 	HISTOGRAM_NBINS = 101;
	
	public double affordability = 0.0;
	public double [][] btl_ltv_distribution = new double[2][HISTOGRAM_NBINS]; // buy to let Loan to value ratio. index/100 = LTV
	public double [][] oo_ltv_distribution = new double[2][HISTOGRAM_NBINS]; // owner-occupier Loan to value ratio. index/100 = LTV
	public double [][] btl_lti_distribution = new double[2][HISTOGRAM_NBINS]; // buy to let Loan to income. index/10 = LTI
	public double [][] oo_lti_distribution = new double[2][HISTOGRAM_NBINS]; // owner occupier Loan to income index/10 = LTI
	public double [][] approved_mortgages = new double [2][ARCHIVE_LEN]; // (loan/income, downpayment/income) pairs
	public int approved_mortgages_index;
	public int mortgageCounter;
	public int ftbCounter;	
	public int btlCounter;	
	public int nApprovedMortgages; // total number of new mortgages
	public int nFTBMortgages; // number of new first time buyer mortgages given
	public int nBTLMortgages; // number of new buy to let mortages given
	public double totalBTLCredit = 0.0; // buy to let mortgage credit
	public double totalOOCredit = 0.0; // owner-occupier mortgage credit	
	public double netCreditGrowth; // rate of change of credit per month as percentage
}
