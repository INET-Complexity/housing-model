package housing;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import sim.util.Double2D;

public class CreditSupply extends CollectorBase {
	private static final long serialVersionUID = 1630707025974306844L;

	private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public CreditSupply() {
		mortgageCounter = 0;
		ftbCounter = 0;
		btlCounter = 0;
		setArchiveLength(10000);
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
	public void recordLoan(Household h, MortgageAgreement approval, House house) {
		double housePrice;
		if(DIAGNOSTICS_ACTIVE) {
			housePrice = approval.principal + approval.downPayment;
			affordability = AFFORDABILITY_DECAY*affordability + (1.0-AFFORDABILITY_DECAY)*approval.monthlyPayment/(h.monthlyEmploymentIncome);
			if(approval.principal > 1.0) {
				if(approval.isBuyToLet) {
					btl_ltv.addValue(100.0*approval.principal/housePrice);
//					double icr = Model.rentalMarket.getAverageSalePrice(house.getQuality())*12.0/(approval.principal*Model.bank.getBtLStressedMortgageInterestRate());
					double icr = Model.rentalMarket.averageSoldGrossYield*approval.purchasePrice/(approval.principal*config.CENTRAL_BANK_BTL_STRESSED_INTEREST);
					btl_icr.addValue(icr);
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
    public double [] getBTLICRDistribution() {return(btl_icr.getValues());}
       
    public boolean getSaveOOLTVDistribution() { return(false);}
    public void setSaveOOLTVDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getOOLTVDistribution(),"ooLTVVals.csv");
    }
    public boolean getSaveBTLLTVDistribution() { return(false);}
    public void setSaveBTLLTVDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getBTLLTVDistribution(),"btlLTVVals.csv");
    }
    public boolean getSaveOOLTIDistribution() { return(false);}
    public void setSaveOOLTIDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getOOLTIDistribution(),"ooLTIVals.csv");
    }
    public boolean getSaveBTLICRDistribution() { return(false);}
    public void setSaveBTLICRDistribution(boolean doSave) throws FileNotFoundException, UnsupportedEncodingException {
        writeDistributionToFile(getBTLICRDistribution(),"btlICRVals.csv");
    }
    

    public int getNRegisteredMortgages() {
    	return(Model.bank.mortgages.size());
    }

	public int getArchiveLength() {
		return archiveLength;
	}
	
	public void writeDistributionToFile(double [] vals, String filename) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter dist = new PrintWriter(filename, "UTF-8");
        if(vals.length > 0) {
        	dist.print(vals[0]);
        	for(int i=1; i<vals.length; ++i) {
        		dist.print(", "+vals[i]);
        	}
        }
        dist.close();
	}

	public void setArchiveLength(int archiveLength) {
		this.archiveLength = archiveLength;
		oo_lti = new DescriptiveStatistics(archiveLength);
		oo_ltv = new DescriptiveStatistics(archiveLength);
		btl_ltv = new DescriptiveStatistics(archiveLength);
		btl_icr = new DescriptiveStatistics(archiveLength);
		downpayments = new DescriptiveStatistics(archiveLength);
	}




	public double AFFORDABILITY_DECAY = Math.exp(-1.0/100.0); 	// Decay constant for exp averaging of affordability
	public double STATS_DECAY = 0.98; 	// Decay constant (per step) for exp averaging of stats
	public boolean DIAGNOSTICS_ACTIVE = true; // record mortgage statistics?
	public int 	HISTOGRAM_NBINS = 101;

	public int archiveLength; // number of mortgage approvals to remember
	public double affordability = 0.0;
	public DescriptiveStatistics oo_lti;
	public DescriptiveStatistics oo_ltv;
	public DescriptiveStatistics btl_ltv;
	public DescriptiveStatistics btl_icr;
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
