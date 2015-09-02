package housing;

import java.io.Serializable;
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
public class Bank implements Serializable {
	private static final long serialVersionUID = -8301089924358306706L;

	public int    N_PAYMENTS = 12*25; // number of monthly repayments
	public double INITIAL_BASE_RATE = 0.005; // Bank base-rate (0.5%)
	public double MAX_OO_LTV = 0.9;		// maximum LTV bank will give to owner-occupier when not regulated	
	public double MAX_BTL_LTV = 0.8;	// maximum LTV bank will give to BTL when not regulated
	public double MAX_OO_LTI = 4.5;		// maximum LTI bank will give to owner-occupier when not regulated
	public double INTEREST_MARGIN = 3.0; // Interest rate rise in affordability stress test (http://www.bankofengland.co.uk/financialstability/Pages/fpc/intereststress.aspx)
	public double CREDIT_SUPPLY_TARGET = 490.0; // target supply of credit per household per month
	
	/********************************
	 * Constructor. This just sets up a few
	 * pre-computed values.
	 ********************************/
	public Bank() {
		mortgages = new HashSet<>();
		init();
	}
	
	public void init() {
		mortgages.clear();
		baseRate = INITIAL_BASE_RATE;
		dDemand_dInterest = 10*1e10;
		setMortgageInterestRate(0.02);
		resetMonthlyCounters();
	}
	
	/***
	 * This is where the bank gets to do its monthly calculations
	 */
	public void step() {
		supplyTarget = CREDIT_SUPPLY_TARGET * Model.households.size();
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
	public double monthlyPaymentFactor(boolean isHome) {
		if(isHome) {
			return(k); // Pay off in N_PAYMENTS
		} else {
			return(getMortgageInterestRate()/12.0); // interest only
		}
	}

	public double stressedMonthlyPaymentFactor(boolean isHome) {
		if(isHome) {
			return(k); // Fixed rate for OO
		} else {
			return((getMortgageInterestRate()+INTEREST_MARGIN)/12.0); // interest only
		}
	}

	/*****************************
	 * Use this to arrange a Mortgage and get a MortgageApproval object.
	 * 
	 * @param h The household that is requesting the mortgage.
	 * @param housePrice The price of the house that 'h' wants to buy
	 * @param isHome true if 'h' plans to live in the house.
	 * @return The MortgageApproval object, or NULL if the mortgage is declined
	 ****************************/
	public MortgageAgreement requestLoan(Household h, double housePrice, double desiredDownPayment, boolean isHome) {
		MortgageAgreement approval = requestApproval(h, housePrice, desiredDownPayment, isHome);
		if(approval == null) return(null);
		// --- if all's well, go ahead and arrange mortgage
		supplyVal += approval.principal;
		if(approval.principal > 0.0) {
			mortgages.add(approval);
			Model.collectors.creditSupply.recordLoan(h, approval);
			++nLoans;
			if(isHome) {
				if(approval.principal/h.annualEmploymentIncome() > Model.centralBank.loanToIncomeRegulation(h.isFirstTimeBuyer())) {
					++nOverLTICapLoans;
				}
				if(approval.principal/(approval.principal + approval.downPayment) > Model.centralBank.loanToValueRegulation(h.isFirstTimeBuyer(),isHome)) {
					++nOverLTVCapLoans;
				}
			}
		}
		return(approval);
	}
	
	
	public void endMortgageContract(MortgageAgreement mortgage) {
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
	public MortgageAgreement requestApproval(Household h, double housePrice, double desiredDownPayment, boolean isHome) {
		MortgageAgreement approval = new MortgageAgreement(h, !isHome);
		double r = getMortgageInterestRate()/12.0; // monthly interest rate
		double lti_principal, affordable_principal;
		double liquidWealth = h.bankBalance;
		
		if(isHome == true) liquidWealth += h.getHomeEquity();		

		// --- LTV constraint
		approval.principal = housePrice*loanToValue(h.isFirstTimeBuyer(), isHome);

		if(isHome) {
			// --- affordability constraint TODO: affordability for BtL
			affordable_principal = Math.max(0.0,0.5*h.getMonthlyPostTaxIncome())/stressedMonthlyPaymentFactor(isHome);
			approval.principal = Math.min(approval.principal, affordable_principal);

			// --- lti constraint
			lti_principal = h.annualEmploymentIncome() * loanToIncome(h.isFirstTimeBuyer());
			approval.principal = Math.min(approval.principal, lti_principal);
		}
		
		approval.downPayment = housePrice - approval.principal;
		
		if(liquidWealth < approval.downPayment) {
			System.out.println("Failed down-payment constraint: bank balance = "+liquidWealth+" Downpayment = "+approval.downPayment);
			System.out.println("isHome = "+isHome+" isFirstTimeBuyer = "+h.isFirstTimeBuyer());
			approval.downPayment = liquidWealth;
//			return(null);
		}
		// --- allow larger downpayments
		if(desiredDownPayment < 0.0) desiredDownPayment = 0.0;
		if(desiredDownPayment > liquidWealth) desiredDownPayment = liquidWealth;
		if(desiredDownPayment > housePrice) desiredDownPayment = housePrice;
		if(desiredDownPayment > approval.downPayment) {
			approval.downPayment = desiredDownPayment;
			approval.principal = housePrice - desiredDownPayment;
		}
		
		approval.monthlyPayment = approval.principal*monthlyPaymentFactor(isHome);		
		approval.nPayments = N_PAYMENTS;
		approval.monthlyInterestRate = r;
		approval.isFirstTimeBuyer = h.isFirstTimeBuyer();
		approval.purchasePrice = approval.principal + approval.downPayment;
		return(approval);
	}


	/*****************************************
	 * Find the maximum mortgage that this mortgage-lender will approve
	 * to a household.
	 * 
	 * @param Household household who is applying for the mortgage
	 * @param isHome true if 'h' plans to live in the house
	 * @return The maximum value of house that this mortgage-lender is willing
	 * to approve a mortgage for.
	 ****************************************/
	public double getMaxMortgage(Household h, boolean isHome) {
		double max;
		double pdi_max; // disposable income constraint
		double lti_max; // loan to income constraint
		double liquidWealth = h.bankBalance;

		if(isHome == true) {
			liquidWealth += h.getHomeEquity(); // assume h will sell current home
		}
		
		max = liquidWealth/(1.0 - loanToValue(h.isFirstTimeBuyer(), isHome)); // LTV constraint

		if(isHome) { // no LTI for BtL investors
//			lti_max = h.getMonthlyPreTaxIncome()*12.0* loanToIncome(h.isFirstTimeBuyer())/loanToValue(h.isFirstTimeBuyer(),isHome);
			pdi_max = liquidWealth + Math.max(0.0,h.getMonthlyPostTaxIncome())/stressedMonthlyPaymentFactor(isHome);
			max = Math.min(max, pdi_max);
			lti_max = h.annualEmploymentIncome()* loanToIncome(h.isFirstTimeBuyer()) + liquidWealth;
			max = Math.min(max, lti_max);
		}
		
		max = Math.floor(max*100.0)/100.0; // round down to nearest penny
		return(max);
	}

	/**********************************************
	 * Get the Loan-To-Value ratio applicable to a given household.
	 * 
	 * @param h The houshold that is applying for the mortgage
	 * @param isHome true if 'h' plans to live in the house
	 * @return The loan-to-value ratio applicable to the given household.
	 *********************************************/
	public double loanToValue(boolean firstTimeBuyer, boolean isHome) {
		double limit;
		if(isHome) {
			limit = MAX_OO_LTV;
		} else {
			limit = MAX_BTL_LTV;
		}
		if((nOverLTVCapLoans+1.0)/(nLoans + 1.0) > Model.centralBank.proportionOverLTVLimit) {
			limit = Math.min(limit, Model.centralBank.loanToValueRegulation(firstTimeBuyer, isHome));
		}
		return(limit);
	}

	public double loanToIncome(boolean firstTimeBuyer) {
		double limit;
		limit = MAX_OO_LTI;
		if((nOverLTICapLoans+1.0)/(nLoans + 1.0) > Model.centralBank.proportionOverLTILimit) {
			limit = Math.min(limit, Model.centralBank.loanToIncomeRegulation(firstTimeBuyer));
		}
		return(limit);
	}
	
	public double interestCoverageRatio() {
		return(Model.centralBank.interestCoverageRatioRegulation());
	}
	
	public HashSet<MortgageAgreement>		mortgages;	// all unpaid mortgage contracts supplied by the bank
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
