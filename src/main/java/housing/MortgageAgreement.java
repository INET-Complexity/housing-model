package housing;

/**************************************************************************************************
 * Class to represent a mortgage contract, keeping track of the updated principal due, the monthly
 * payment, the number of payments left, etc.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class MortgageAgreement extends PaymentAgreement {

    //------------------//
    //----- Fields -----//
    //------------------//

	public double           downPayment;
	public double           purchasePrice;
    private boolean         isActive;
	public final boolean    isBuyToLet;
	public final boolean    isFirstTimeBuyer;
	public double           principal; // Remaining principal to be paid off
	public double           monthlyInterestRate;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	MortgageAgreement(Household borrower, boolean isBuyToLet) {
		this.isBuyToLet = isBuyToLet;
		this.isFirstTimeBuyer = !isBuyToLet && borrower.isFirstTimeBuyer();
		isActive = true;
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

	/**
	 * This method updates the internal variables to simulate a monthly payment being made, though it does not move any
     * assets from payer to payee!
	 * 
	 * @return The amount of the monthly payment and record the payments
	 */
	@Override
    public double makeMonthlyPayment(Household h) {
	    // If no more payments are due...
        if (nPayments == 0) {
            // ...but mortgage is still active...
            if (isActive) {
//            	if(principal>20) {
//            	//	System.out.println("BTL repayment of principal: " + principal);
//            	}
                isActive = false; // ...then deactivate the mortgage...
                double payoff = payoff(principal, h);
                return payoff; // ...by paying off all remaining principle (this also removes mortgage from the bank's list)...
            // ...otherwise, if mortgage is already inactive...
            } else {
                return 0.0; // ...simply return a zero payment
            }
        // If more payments are still due...
        } else {
            nPayments -= 1; // ...then reduce number of payments due by one,
            double principalBefore = principal;
            principal = principal*(1.0 + monthlyInterestRate) - monthlyPayment; // ...reduce amount due by amount to be paid this month
            // ... calculate the principal and interest repayment and record it ...
            h.setPrincipalPaidBack(principalBefore-principal);
            // as there are tiny rounding errors, i subtract a small amount for this test
            if((principal-0.01)>principalBefore) {
            	System.out.println("weird, the mortgage volume increased");
            }
            // ... calculate the the interest repayment as the amount of interest accrued this month
            h.setInterestPaidBack(principalBefore*(1.0 + monthlyInterestRate)-principalBefore);
            // as there are tiny rounding errors, I add 0.01 to monthly payments
            if((principalBefore*(1.0 + monthlyInterestRate)-principalBefore)> (monthlyPayment+0.1) 
            		&& monthlyPayment != 0.0) {
            	System.out.println("weird, the monthly payment made was smaller than the increase in credit by the interest rate");
            }
            return monthlyPayment; // ...and return the monthly payment
        }
	}
	
	/**
	 * Use this method to pay off the mortgage early or make a one-off payment.
	 * 
	 * @param amount Desired amount to pay off
	 * @return The amount that was actually paid off
	 */
	double payoff(double amount, Household h) {
		if (amount >= principal) {
            amount = principal;
            principal = 0.0;
            monthlyPayment = 0.0;
            nPayments = 0;
            Model.bank.endMortgageContract(this);
            h.setPrincipalPaidBack(amount); // (record the repayment..)
		} else {
			monthlyPayment *= (principal - amount)/principal;
			principal -= amount;
		}
        return amount;
    }

    /**
     * Pay off method in case no specific amount is provided. It assumes full principal payment.
     *
     * @return The amount that was actually paid off
     */
	double payoff(Household h) { return payoff(principal, h); }
}
