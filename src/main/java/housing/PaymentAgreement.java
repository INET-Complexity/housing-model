package housing;

/**************************************************************************************************
 * Class to represent a payment contract in general, so as to include both mortgage and rental
 * contracts. It keeps track of the monthly payments associated to the contract and the number of
 * payments left.
 *
 * @author daniel, davidrpugh, Adrian Carro
 *
 *************************************************************************************************/
public class PaymentAgreement {

    //------------------//
    //----- Fields -----//
    //------------------//

    int 		    nPayments;
    public double 	monthlyPayment;

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * This method updates the internal variables to simulate a monthly payment being made, though it does not move any
     * assets from payer to payee!
     *
     * @return The amount of the monthly payment
     */
	public double makeMonthlyPayment() {
		if (nPayments == 0) {
		    return 0.0;
        } else {
            nPayments -= 1;
            return monthlyPayment;
        }
	}

	/**
	 * Use this method to return the next monthly payment without actually making any payment nor updating the
     * corresponding internal variables
	 *
	 * @return The amount of the next monthly payment
	 */
	double nextPayment() {
		if (nPayments == 0) {
		    return 0.0;
        } else {
		    return monthlyPayment;
        }
	}
}
