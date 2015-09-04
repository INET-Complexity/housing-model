package housing;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import sim.util.Double2D;

public class CreditSupply extends CollectorBase {
	private static final long serialVersionUID = 1630707025974306844L;

	public CreditSupply() {
		oo_lti = new DescriptiveStatistics(ARCHIVE_LEN);
		oo_ltv = new DescriptiveStatistics(ARCHIVE_LEN);
		btl_ltv = new DescriptiveStatistics(ARCHIVE_LEN);
		downpayments = new DescriptiveStatistics(ARCHIVE_LEN);
		mortgageCounter = 0;
//		approved_mortgages_index = 0;
		ftbCounter = 0;
		btlCounter = 0;
	}

	/***
	 * collect information for this timestep
	 */
	public void step() {
        double oldTotalCredit = totalOOCredit + totalBTLCredit;
        totalOOCredit = 0.0;
        totalBTLCredit = 0.0;
        for(MortgageAgreement m : Model.bank.mortgages) {
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
	public void recordLoan(Household h, MortgageAgreement approval) {
		double housePrice;
		if(DIAGNOSTICS_ACTIVE) {
			housePrice = approval.principal + approval.downPayment;
			affordability = AFFORDABILITY_DECAY*affordability + (1.0-AFFORDABILITY_DECAY)*approval.monthlyPayment/(h.monthlyEmploymentIncome);
			if(approval.principal > 1.0) {
				if(approval.isBuyToLet) {
					btl_ltv.addValue(100.0*approval.principal/housePrice);
				} else {
					oo_ltv.addValue(100.0*approval.principal/housePrice);
					oo_lti.addValue(approval.principal/h.annualEmploymentIncome());
				}
				downpayments.addValue(approval.downPayment);
			}
//			approved_mortgages[0][approved_mortgages_index] = approval.principal/(h.annualEmploymentIncome());
//			approved_mortgages[1][approved_mortgages_index] = approval.downPayment/(h.annualEmploymentIncome());
//			approved_mortgages_index += 1;
//			if(approved_mortgages_index == ARCHIVE_LEN) approved_mortgages_index = 0;
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
	
    public double [] getOOLTVDistribution() {return(oo_ltv.getValues());}
    public double [] getOOLTIDistribution() {return(oo_lti.getValues());}
    public double [] getBTLLTVDistribution() {return(btl_ltv.getValues());}
    public double [] getDownpaymentDistribution() {return(downpayments.getValues());}
    public int getNRegisteredMortgages() {
    	return(Model.bank.mortgages.size());
    }


	public double AFFORDABILITY_DECAY = Math.exp(-1.0/100.0); 	// Decay constant for exp averaging of affordability
	public double STATS_DECAY = 0.98; 	// Decay constant (per step) for exp averaging of stats
	public int ARCHIVE_LEN = 1000; // number of mortgage approvals to remember
	public boolean DIAGNOSTICS_ACTIVE = true; // record mortgage statistics?
	public int 	HISTOGRAM_NBINS = 101;
	
	public double affordability = 0.0;
	public DescriptiveStatistics oo_lti;
	public DescriptiveStatistics oo_ltv;
	public DescriptiveStatistics btl_ltv;
	public DescriptiveStatistics downpayments;
//	public double [][] approved_mortgages = new double [2][ARCHIVE_LEN]; // (loan/income, downpayment/income) pairs
//	public int approved_mortgages_index;
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
