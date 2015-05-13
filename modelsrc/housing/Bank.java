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

	public int    N_PAYMENTS = 12*25; // number of monthly repayments
	public double INITIAL_BASE_RATE = 0.5; // Bank base-rate
	public double MAX_OO_LTV = 1.0;		// maximum LTV bank will give to owner-occupier when not regulated	
	public double MAX_BTL_LTV = 0.6;	// maximum LTV bank will give to BTL when not regulated
	public double MAX_OO_LTI = 6.5;		// maximum LTI bank will give to owner-occupier when not regulated	
	public double MAX_BTL_LTI = 10.0;	// maximum LTI bank will give to BTL when not regulated

	/********************************
	 * Constructor. This just sets up a few
	 * pre-computed values.
	 ********************************/
	public Bank(CentralBank c) {
		mortgages = new HashSet<MortgageApproval>();
		centralBank = c;
		setMortgageInterestRate(0.03);
		dDemand_dInterest = 10*1e10;
		baseRate = INITIAL_BASE_RATE;
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
		nOverLTVCapLoans = 0;
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
		return(baseRate + interestSpread);
	}
	

	/******************************
	 * Get the interest rate on mortgages.
	 * @return The interest rate on mortgages.
	 *****************************/
	public void setMortgageInterestRate(double rate) {
		interestSpread = rate - baseRate;
		recalculateK();
	}
	
	public double getBaseRate() {
		return baseRate;
	}

	public void setBaseRate(double baseRate) {
		this.baseRate = baseRate;
		recalculateK();
	}

	protected void recalculateK() {
		double r = getMortgageInterestRate()/12.0;
		k = r/(1.0 - Math.pow(1.0+r, -N_PAYMENTS));		
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
		if(isHome) {
			if(approval.principal/h.annualEmploymentIncome > centralBank.loanToIncomeRegulation(h,isHome)) {
				++nOverLTICapLoans;
			}
			if(approval.principal/(approval.principal + approval.downPayment) > centralBank.loanToValueRegulation(h,isHome)) {
				++nOverLTVCapLoans;
			}
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
		double ltv_principal, lti_principal;

		// --- calculate maximum allowable principal
		approval.principal = Math.max(0.0,h.getMonthlyDisposableIncome())/monthlyPaymentFactor();

		ltv_principal = housePrice*loanToValue(h, isHome);
		approval.principal = Math.min(approval.principal, ltv_principal);

		lti_principal = h.annualEmploymentIncome * loanToIncome(h,isHome);
		approval.principal = Math.min(approval.principal, lti_principal);

		approval.downPayment = housePrice - approval.principal;
		
		if(h.bankBalance < approval.downPayment) {
			System.out.println("Failed down-payment constraint: bank balance = "+h.bankBalance+" Downpayment = "+approval.downPayment);
			System.out.println("isHome = "+isHome+" isFirstTimeBuyer = "+h.isFirstTimeBuyer());
			approval.downPayment = h.bankBalance;
//			return(null);
		}
		// --- allow larger downpayments
		if(desiredDownPayment < 0.0) desiredDownPayment = 0.0;
		if(desiredDownPayment > h.bankBalance) desiredDownPayment = h.bankBalance;
		if(desiredDownPayment > housePrice) desiredDownPayment = housePrice;
		if(desiredDownPayment > approval.downPayment) {
			approval.downPayment = desiredDownPayment;
			approval.principal = housePrice - desiredDownPayment;
		}
		
		approval.monthlyPayment = approval.principal*monthlyPaymentFactor();		
		approval.nPayments = N_PAYMENTS;
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

		pdi_max = h.bankBalance + Math.max(0.0,h.getMonthlyDisposableIncome())/monthlyPaymentFactor();
		
		ltv_max = h.bankBalance/(1.0 - loanToValue(h, isHome));
		pdi_max = Math.min(pdi_max, ltv_max);

		lti_max = h.annualEmploymentIncome * loanToIncome(h,isHome)/loanToValue(h,isHome);
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
		double limit;
		if(isHome) {
			limit = MAX_OO_LTV;
		} else {
			limit = MAX_BTL_LTV;
		}
		if((nOverLTVCapLoans+1.0)/(nLoans + 1.0) > centralBank.proportionOverLTVLimit) {
			limit = Math.min(limit, centralBank.loanToValueRegulation(h,isHome));
		}
		return(limit);
	}

	public double loanToIncome(Household h, boolean isHome) {
		double limit;
		if(isHome) {
			limit = MAX_OO_LTI;
		} else {
			limit = MAX_BTL_LTI;
		}
		if((nOverLTICapLoans+1.0)/(nLoans + 1.0) > centralBank.proportionOverLTILimit) {
			limit = Math.min(limit, centralBank.loanToIncomeRegulation(h,isHome));
		}
		return(limit);
	}

	public CentralBank 		centralBank;
	
	public HashSet<MortgageApproval>		mortgages;	// all unpaid mortgage contracts supplied by the bank
	public double 		k; 				// principal to monthly payment factor
	public double		interestSpread;	// current mortgage interest spread above base rate (monthly rate*12)
	public double		baseRate;
	// --- supply strategy stuff
	public double		supplyTarget; 	// target supply of mortgage lending (pounds)
	public double		demand;			// monthly demand for mortgage loans (pounds)
	public double		supplyVal;		// monthly supply of mortgage loans (pounds)
	public double		lastMonthsSupplyVal;
	public double		dDemand_dInterest; // rate of change of demand with interest rate (pounds)
	public int			nOverLTICapLoans; 	// number of (non-BTL) loans above LTI cap this step
	public int			nOverLTVCapLoans;	// number of (non-BTL) loans above LTV cap this step
	public int			nLoans; 			// total number of non-BTL loans this step
	
}
