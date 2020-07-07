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
    double                  purchasePrice;
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
        // If no more payments are due, simply return a zero payment
        if (nPayments == 0) {
            return 0.0;
        // If more payments are still due...
        } else {
            // ...then reduce number of payments due by one
            nPayments -= 1;
            // ...reduce amount due by amount to be paid this month
            principal = principal*(1.0 + monthlyInterestRate) - monthlyPayment;
            // ...and, if this would bring the number of payments to zero, then add any principal left (zero or close to
            // zero for capital and interest mortgages, full principal for interest-only mortgages)
            if (nPayments == 0) {
                return monthlyPayment + payoff(principal);
            } else {
                return monthlyPayment;
            }
        }
    }

    /**
     * Use this method to pay off the mortgage early or make a one-off payment. Note that if the mortgage is completely
     * paid off, then it is removed from the bank's list of mortgages.
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
            monthlyPayment *= (principal - amount) / principal;
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

    public double getAnnualInterestRate() { return monthlyInterestRate * 12.0; }

    public int getMaturity() { return nPayments; }
}
