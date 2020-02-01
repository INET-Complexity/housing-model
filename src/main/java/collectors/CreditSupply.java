package collectors;

import housing.Model;
import housing.Household;
import housing.MortgageAgreement;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**************************************************************************************************
 * Class to record mortgage data
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class CreditSupply {

    //------------------//
    //----- Fields -----//
    //------------------//

    private DescriptiveStatistics oo_lti;
    private DescriptiveStatistics oo_ltv;
    private DescriptiveStatistics btl_ltv;
    private DescriptiveStatistics btl_icr;
    private DescriptiveStatistics downpayments; // TODO: This quantity only includes downpayments when the principal of the loan is > 0
    private double interestRate;                // To record interest rate and give it to recorder object for file writing
    private double totalBTLCredit = 0.0;        // Buy to let mortgage credit
    private double totalOOCredit = 0.0;         // Owner-occupier mortgage credit
    private double netCreditGrowth;             // Rate of change of credit per month as percentage
    private int mortgageCounter;                // Counter for total number of new mortgages
    private int nApprovedMortgages;             // total number of new mortgages
    private int ftbCounter;                     // Counter for total number of new first time buyer mortgages
    private int nFTBMortgages;                  // Total number of new first time buyer mortgages
    private int ftbbtlCounter;                  // Counter for the total number of new first time buyer mortgages to households with the BTL gene
    private int nFTBMortgagesToBTL;             // Total number of new first time buyer mortgages to households with the BTL gene
    private int btlCounter;                     // Counter for total number of new buy to let mortgages
    private int nBTLMortgages;                  // Total number of new buy to let mortgages
    private double newCreditToHMCounter;        // Counter for total amount (principals) of new home mover mortgages
    private double newCreditToHM;               // Total amount (principals) of new home mover mortgages
    private double newCreditToFTBCounter;       // Counter for total amount (principals) of new first time buyer mortgages
    private double newCreditToFTB;              // Total amount (principals) of new first time buyer mortgages
    private double newCreditToBTLCounter;       // Counter for total amount (principals) of new buy-to-let mortgages
    private double newCreditToBTL;              // Total amount (principals) of new buy-to-let mortgages

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	public CreditSupply() {
		mortgageCounter = 0;
		ftbCounter = 0;
        ftbbtlCounter = 0;
        btlCounter = 0;
        newCreditToHMCounter = 0.0;
        newCreditToFTBCounter = 0.0;
        newCreditToBTLCounter = 0.0;
		// TODO: This limit in the number of events taken into account to build statistics is not explained in the paper
        // TODO: (affects oo_lti, oo_ltv, btl_ltv, btl_icr, downpayments)
		setArchiveLength(10000);
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

	/**
     * Collect information for this time step
	 */
	public void step() {
	    interestRate = Model.bank.getMortgageInterestRate();
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
        if (oldTotalCredit > 0.0) {
            netCreditGrowth = (totalOOCredit + totalBTLCredit - oldTotalCredit)/oldTotalCredit;
        } else {
            netCreditGrowth = 0;
        }
        nApprovedMortgages = mortgageCounter;
        nFTBMortgages = ftbCounter;
        nFTBMortgagesToBTL = ftbbtlCounter;
        nBTLMortgages = btlCounter;
        mortgageCounter = 0;
        ftbCounter = 0;
        btlCounter = 0;
        ftbbtlCounter = 0;
        newCreditToFTB = newCreditToFTBCounter;
        newCreditToHM = newCreditToHMCounter;
        newCreditToBTL = newCreditToBTLCounter;
        newCreditToFTBCounter = 0.0;
        newCreditToHMCounter = 0.0;
        newCreditToBTLCounter = 0.0;
    }

	/**
	 * Record information for a newly issued mortgage
	 * @param h Household being awarded the loan
	 * @param approval Mortgage agreement
	 */
	public void recordLoan(Household h, MortgageAgreement approval) {
		double housePrice;
		housePrice = approval.principal + approval.downPayment;
		if (approval.isBuyToLet) {
			btl_ltv.addValue(100.0*approval.principal/housePrice);
			double icr = Model.rentalMarketStats.getExpAvFlowYield()*approval.purchasePrice/
					(approval.principal*Model.centralBank.getInterestCoverRatioStressedRate(false));
			btl_icr.addValue(icr);
		} else {
			oo_ltv.addValue(100.0*approval.principal/housePrice);
			oo_lti.addValue(approval.principal/h.getAnnualGrossEmploymentIncome());
		}
		downpayments.addValue(approval.downPayment);
		mortgageCounter += 1;
		if (approval.isFirstTimeBuyer) {
		    ftbCounter += 1;
		    newCreditToFTBCounter += approval.principal;
		    if (h.behaviour.isPropertyInvestor()) {
		        ftbbtlCounter += 1;
            }
        } else if (approval.isBuyToLet) {
		    btlCounter += 1;
		    newCreditToBTLCounter += approval.principal;
        } else {
            newCreditToHMCounter += approval.principal;
        }
	}

	private void setArchiveLength(int archiveLength) {
		oo_lti = new DescriptiveStatistics(archiveLength);
		oo_ltv = new DescriptiveStatistics(archiveLength);
		btl_ltv = new DescriptiveStatistics(archiveLength);
		btl_icr = new DescriptiveStatistics(archiveLength);
		downpayments = new DescriptiveStatistics(archiveLength);
	}

    //----- Getter/setter methods -----//

    DescriptiveStatistics getOO_lti() { return oo_lti; }

    DescriptiveStatistics getOO_ltv() { return oo_ltv; }

    DescriptiveStatistics getBTL_ltv() { return btl_ltv; }

    double getInterestRate() { return interestRate; }

    int getnRegisteredMortgages() { return Model.bank.mortgages.size(); }

    int getnApprovedMortgages() { return nApprovedMortgages; }

    int getnFTBMortgages() { return nFTBMortgages; }

    int getnFTBMortgagesToBTL() { return nFTBMortgagesToBTL; }

    int getnHMMortgages() { return nApprovedMortgages - nFTBMortgages - nBTLMortgages; }

    int getnBTLMortgages() { return nBTLMortgages; }

    double getTotalBTLCredit() { return totalBTLCredit; }

    double getTotalOOCredit() { return totalOOCredit; }

    double getNetCreditGrowth() { return netCreditGrowth; }

    double getNewCreditToFTB() { return newCreditToFTB; }

    double getNewCreditToHM() { return newCreditToHM; }

    double getNewCreditToBTL() { return newCreditToBTL; }

    double getNewCreditTotal() { return newCreditToFTB + newCreditToHM + newCreditToBTL; }
}
