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

	private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

//	public double INTEREST_MARGIN = 0.03; // Interest rate rise in affordability stress test (http://www.bankofengland.co.uk/financialstability/Pages/fpc/intereststress.aspx)
	
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
		baseRate = config.BANK_INITIAL_BASE_RATE;
		// TODO: Is this (dDemand_dInterest) a parameter? Shouldn't it depend somehow on other variables of the model?
		dDemand_dInterest = 10*1e10;
        // TODO: Is this (0.02) a parameter? Does it affect results in any significant way or is it just a dummy initialisation?
        setMortgageInterestRate(0.02);
		resetMonthlyCounters();
    }
	
	/***
	 * This is where the bank gets to do its monthly calculations
	 */
	public void step() {
		supplyTarget = config.BANK_CREDIT_SUPPLY_TARGET*Model.households.size();
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
		double rate = getMortgageInterestRate() + 0.5*(supplyVal - supplyTarget)/dDemand_dInterest;
//		System.out.println(supplyVal/Model.households.size());
		if(rate < baseRate) rate = baseRate;
		return(rate);
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
		double r = getMortgageInterestRate()/config.constants.MONTHS_IN_YEAR;
		k = r/(1.0 - Math.pow(1.0+r, -config.derivedParams.N_PAYMENTS));
	}

	/*******************************
	 * Get the monthly payment on a mortgage as a fraction of the mortgage principle.
	 * @return The monthly payment fraction.
	 *******************************/
	public double monthlyPaymentFactor(boolean isHome) {
		if(isHome) {
			return(k); // Pay off in N_PAYMENTS
		} else {
			return(getMortgageInterestRate()/config.constants.MONTHS_IN_YEAR); // interest only
		}
	}

	/*
	public double stressedMonthlyPaymentFactor(boolean isHome) {
		if(isHome) {
			return(k); // Fixed rate for OO
		} else {
			return((getMortgageInterestRate()+INTEREST_MARGIN)/12.0); // interest only
		}
	}
*/
	/*****************************
	 * Use this to arrange a Mortgage and get a MortgageApproval object.
	 * 
	 * @param h The household that is requesting the mortgage.
	 * @param housePrice The price of the house that 'h' wants to buy
	 * @param isHome true if 'h' plans to live in the house.
	 * @return The MortgageApproval object, or NULL if the mortgage is declined
	 ****************************/
	public MortgageAgreement requestLoan(Household h, double housePrice, double desiredDownPayment, boolean isHome, House house) {
		MortgageAgreement approval = requestApproval(h, housePrice, desiredDownPayment, isHome);
		if(approval == null) return(null);
		// --- if all's well, go ahead and arrange mortgage
		supplyVal += approval.principal;
		if(approval.principal > 0.0) {
			mortgages.add(approval);
			Model.collectors.creditSupply.recordLoan(h, approval, house);
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
		double r = getMortgageInterestRate()/config.constants.MONTHS_IN_YEAR; // monthly interest rate
		double lti_principal, affordable_principal, icr_principal;
		double liquidWealth = h.getBankBalance();
		
		if(isHome) liquidWealth += h.getHomeEquity();

		// --- LTV constraint
		approval.principal = housePrice*loanToValue(h.isFirstTimeBuyer(), isHome);

		if(isHome) {
			// --- affordability constraint TODO: affordability for BtL?
			affordable_principal = Math.max(0.0,config.CENTRAL_BANK_AFFORDABILITY_COEFF*h.getMonthlyPostTaxIncome())/monthlyPaymentFactor(isHome);
			approval.principal = Math.min(approval.principal, affordable_principal);

			// --- lti constraint
			lti_principal = h.annualEmploymentIncome() * loanToIncome(h.isFirstTimeBuyer());
			approval.principal = Math.min(approval.principal, lti_principal);
		} else {
			// --- BtL ICR constraint
			icr_principal = Model.houseRentalMarkets.averageSoldGrossYield*housePrice/(interestCoverageRatio()*config.CENTRAL_BANK_BTL_STRESSED_INTEREST);
			approval.principal = Math.min(approval.principal, icr_principal);
	//		System.out.println(icr_principal/housePrice);
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
		approval.nPayments = config.derivedParams.N_PAYMENTS;
		approval.monthlyInterestRate = r;
//		approval.isFirstTimeBuyer = h.isFirstTimeBuyer();
		approval.purchasePrice = approval.principal + approval.downPayment;
		return(approval);
	}


	/*****************************************
	 * Find the maximum mortgage that this mortgage-lender will approve
	 * to a household.
	 * 
	 * @param h household who is applying for the mortgage
	 * @param isHome true if 'h' plans to live in the house
	 * @return The maximum value of house that this mortgage-lender is willing
	 * to approve a mortgage for.
	 ****************************************/
	public double getMaxMortgage(Household h, boolean isHome) {
		double max;
		double pdi_max; // disposable income constraint
		double lti_max; // loan to income constraint
		double icr_max; // interest rate coverage
		double liquidWealth = h.getBankBalance();

		if(isHome) {
			liquidWealth += h.getHomeEquity(); // assume h will sell current home
		}
		
		max = liquidWealth/(1.0 - loanToValue(h.isFirstTimeBuyer(), isHome)); // LTV constraint

		if(isHome) { // no LTI for BtL investors
//			lti_max = h.getMonthlyPreTaxIncome()*12.0* loanToIncome(h.isFirstTimeBuyer())/loanToValue(h.isFirstTimeBuyer(),isHome);
			pdi_max = liquidWealth + Math.max(0.0,config.CENTRAL_BANK_AFFORDABILITY_COEFF*h.getMonthlyPostTaxIncome())/monthlyPaymentFactor(isHome);
			max = Math.min(max, pdi_max);
			lti_max = h.annualEmploymentIncome()* loanToIncome(h.isFirstTimeBuyer()) + liquidWealth;
			max = Math.min(max, lti_max);
		} else {
			icr_max = Model.houseRentalMarkets.averageSoldGrossYield/(interestCoverageRatio()*config.CENTRAL_BANK_BTL_STRESSED_INTEREST);
			if(icr_max < 1.0) {
				icr_max = liquidWealth/(1.0 - icr_max);
				max = Math.min(max,  icr_max);
			}
		}
		
		max = Math.floor(max*100.0)/100.0; // round down to nearest penny
		return(max);
	}

	/**********************************************
	 * Get the Loan-To-Value ratio applicable to a given household.
	 * 
	 * @param firstTimeBuyer true if the household is a first time buyer
	 * @param isHome true if the household plans to live in the house
	 * @return The loan-to-value ratio applicable to the given household.
	 *********************************************/
	public double loanToValue(boolean firstTimeBuyer, boolean isHome) {
		double limit;
		if(isHome) {
			limit = config.CENTRAL_BANK_MAX_OO_LTV;
		} else {
			limit = config.CENTRAL_BANK_MAX_BTL_LTV;
		}
		if((nOverLTVCapLoans+1.0)/(nLoans + 1.0) > Model.centralBank.proportionOverLTVLimit) {
			limit = Math.min(limit, Model.centralBank.loanToValueRegulation(firstTimeBuyer, isHome));
		}
		return(limit);
	}

	/**********************************************
	 * Get the Loan-To-Income ratio applicable to a given household.
	 *
	 * @param firstTimeBuyer true if the household is a first time buyer
	 * @return The loan-to-income ratio applicable to the given household.
	 *********************************************/
	public double loanToIncome(boolean firstTimeBuyer) {
		double limit;
		limit = config.CENTRAL_BANK_MAX_OO_LTI;
		if((nOverLTICapLoans+1.0)/(nLoans + 1.0) > Model.centralBank.proportionOverLTILimit) {
			limit = Math.min(limit, Model.centralBank.loanToIncomeRegulation(firstTimeBuyer));
		}
		return(limit);
	}
	
	public double interestCoverageRatio() {
		return(Model.centralBank.interestCoverageRatioRegulation());
	}

	// TODO: Remove (no needed anymore)
//	public double getBtLStressedMortgageInterestRate() {
//		return(config.CENTRAL_BANK_BTL_STRESSED_INTEREST);
//	}
	
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
