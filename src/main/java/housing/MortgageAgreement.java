package housing;

/**************************************************************************************************
 * Class to represent a mortgage contract, keeping track of the updated principal due, the monthly
 * payment, the number of payments left, etc.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class MortgageAgreement extends PaymentAgreement {
	private static final long serialVersionUID = -1610029355056926296L;

    //------------------//
    //----- Fields -----//
    //------------------//

	public double           downPayment;
	public double           purchasePrice;
    private boolean         isActive;
	public final boolean    isBuyToLet;
	public final boolean    isFirstTimeBuyer;
	public double           principal; // Remaining principal to be paid off
	double                  monthlyInterestRate;

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
	 * @return The amount of the monthly payment
	 */
	@Override
    public double makeMonthlyPayment() {
	    // If no more payments are due...
        if (nPayments == 0) {
            // ...but mortgage is still active...
            if (isActive) {
                isActive = false; // ...then deactivate the mortgage...
                return payoff(principal); // ...by paying off all remaining principle (this also removes mortgage from the bank's list)...
            // ...otherwise, if mortgage is already inactive...
            } else {
                return 0.0; // ...simply return a zero payment
            }
        // If more payments are still due...
        } else {
            nPayments -= 1; // ...then reduce number of payments due by one,
            principal = principal*(1.0 + monthlyInterestRate) - monthlyPayment; // ...reduce amount due by amount to be paid this month
            return monthlyPayment; // ...and return the monthly payment
        }
	}

	/**
	 * Use this method to pay off the mortgage early or make a one-off payment.
	 * 
	 * @param amount Desired amount to pay off
	 * @return The amount that was actually paid off
	 */
	double payoff(double amount) {
		if (amount >= principal) {
            amount = principal;
            principal = 0.0;
            monthlyPayment = 0.0;
            nPayments = 0;
            Model.bank.endMortgageContract(this);
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
	double payoff() { return payoff(principal); }
}
