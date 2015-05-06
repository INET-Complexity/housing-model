package housing;

import java.util.HashSet;

/*************************************************
 * This class represents a mortgage-lender (i.e. a bank or building society).
 * Its only function is to approve/decline
 * mortgage requests, so this is where mortgage-lending policy is encoded.
 *  
 * 
 * @author daniel, davidrpugh
 *
 *************************************************/
public class Bank {

	/**
	 * This object holds the configutation for the Bank.
	 * @author daniel
	 */
	static public class Config {
		public double THETA_FTB = 0.1; // first-time buyer haircut (LTV)
		public double THETA_HOME = 0.2; // home buyer haircut (LTV)
		public double THETA_BTL = 0.4; // buy-to-let buyer haircut (LTV)
		public double LTI = 6.5;//6.5;//4.5; // loan-to-income ratio. Capped at 4.5 for all lenders from 01/10/14
		public double LTI_CAP_PERCENTAGE = 0.85; // %age of loans that must be constrianed below the LTI limit
		public int    N_PAYMENTS = 12*25; // number of monthly repayments
	}

	/********************************
	 * Constructor. This just sets up a few
	 * pre-computed values.
	 ********************************/
	public Bank() {
		this(new Bank.Config());
		mortgages = new HashSet<MortgageApproval>();
	}
	
	public Bank(Bank.Config c) {
		config = c;
		setMortgageInterestRate(0.03);
		dDemand_dInterest = 10*1e10;
		resetMonthlyCounters();
	}
	
	/***
	 * This is where the bank gets to do its monthly calculations
	 */
	public void step() {
		supplyTarget = 1000.0 * Model.households.size();
		setMortgageInterestRate(recalcInterestRate());
		resetMonthlyCounters();
	}
	
	/***
	 *  Resets all the various monthly diagnostic measures ready for the next month
	 */
	public void resetMonthlyCounters() {
		lastMonthsSupplyVal = supplyVal;
		demand = 0.0;
		supplyVal = 0.0;
		nLoans = 0;
		nOverLTICapLoans = 0;
	}
	
	/***
	 * Calculates the next months mortgage interest based on this months
	 * rate and the resulting demand.
	 * 
	 * Assumes a linear relationship between interest rate and demand,
	 * and aims to halve the difference between current demand
	 * and target supply
	 */
	public double recalcInterestRate() {
		return(getMortgageInterestRate() + 0.5*(supplyVal - supplyTarget)/dDemand_dInterest);
	}
	
	/******************************
	 * Get the interest rate on mortgages.
	 * @return The interest rate on mortgages.
	 *****************************/
	public double getMortgageInterestRate() {
		return(interestRate);
	}
	

	/******************************
	 * Get the interest rate on mortgages.
	 * @return The interest rate on mortgages.
	 *****************************/
	public void setMortgageInterestRate(double rate) {
		interestRate = rate;
		double r = rate/12.0;
		k = r/(1.0 - Math.pow(1.0+r, -config.N_PAYMENTS));
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
		MortgageApproval approval = requestApproval(h, housePrice, desiredDownPayment, isHome);
		if(approval == null) return(null);
		// --- if all's well, go ahead and arrange mortgage
		supplyVal += approval.principal;
		mortgages.add(approval);
		Collectors.creditSupply.recordLoan(h, approval);
		++nLoans;
		if(isHome && (approval.principal/h.annualEmploymentIncome > config.LTI)) {
			++nOverLTICapLoans;
		}
		return(approval);
	}
	
	public void endMortgageContract(MortgageApproval mortgage) {
		mortgages.remove(mortgage);
	}

	/********
	 * Use this to request a mortgage approval but not actually sign a mortgage contract.
	 * This is useful if you want to inspect the details of the mortgage contract before
	 * deciding whether to actually go ahead and sign.
	 * 
	 * @param h 			The household that is requesting the approval.
	 * @param housePrice 	The price of the house that 'h' wants to buy
	 * @param isHome 		does 'h' plan to live in the house?
	 * @return A MortgageApproval object, or NULL if the mortgage is declined
	 */
	public MortgageApproval requestApproval(Household h, double housePrice, double desiredDownPayment, boolean isHome) {
		MortgageApproval approval = new MortgageApproval();
		double r = getMortgageInterestRate()/12.0; // monthly interest rate
		double ltv_principal, pdi_principal, lti_principal;

		// --- calculate maximum allowable principal
		ltv_principal = housePrice*loanToValue(h, isHome);
		pdi_principal = Math.max(0.0,h.getMonthlyDisposableIncome())/monthlyPaymentFactor();
		approval.principal = Math.min(ltv_principal, pdi_principal);
		if(isHome && (nOverLTICapLoans*1.0/nLoans < config.LTI_CAP_PERCENTAGE)) { // no LTI constraint for buy-to-let
			lti_principal = h.annualEmploymentIncome * config.LTI;
			approval.principal = Math.min(approval.principal, lti_principal);
		}
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
		approval.isBuyToLet = !isHome;
		approval.isFirstTimeBuyer = h.isFirstTimeBuyer();
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
		ltv_max = Math.min(pdi_max, ltv_max); // find minimum
		if(isHome && (nOverLTICapLoans*1.0/nLoans < config.LTI_CAP_PERCENTAGE)) { // no LTI constraint for buy-to-let
			lti_max = h.annualEmploymentIncome * config.LTI/loanToValue(h,isHome);
			ltv_max = Math.min(ltv_max, lti_max);
		}
		ltv_max = Math.floor(ltv_max*100.0)/100.0; // round down to nearest penny
		
		return(ltv_max);
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
	
	public HashSet<MortgageApproval>		mortgages;	// all unpaid mortgage contracts supplied by the bank
	public double 		k; 				// principal to monthly payment factor
	public double		interestRate;	// current mortgage interest rate (monthly rate*12)
	// --- supply strategy stuff
	public double		supplyTarget; 	// target supply of mortgage lending (pounds)
	public double		demand;			// monthly demand for mortgage loans (pounds)
	public double		supplyVal;		// monthly supply of mortgage loans (pounds)
	public double		lastMonthsSupplyVal;
	public double		dDemand_dInterest; // rate of change of demand with interest rate (pounds)
	public int			nOverLTICapLoans; 	// number of loans above LTI cap this step
	public int			nLoans; 			// total number of non-BTL loans this step
	
}
