package housing;

import java.io.Serializable;

/****************************************************
 * This class is created by a mortgage-lender when it approves a mortgage.
 * It acts as a convenient container of information pertaining to a
 * mortgage, and doubles up as a contract that represents the mortgage
 * itself.
 * 
 * @author daniel, davidrpugh
 *
 ***************************************************/
public class PaymentAgreement implements Serializable {
	private static final long serialVersionUID = -2680643507296169409L;
	/********************************************
	 * Updates internal variables to simulate a payment
	 * being made (Does not move any assets from payer to payee).
	 * 
	 * @return The amount of the payment
	 ********************************************/
	public double makeMonthlyPayment() {
		if(nPayments == 0) return(0.0);
		nPayments -= 1;
		return(monthlyPayment);
	}
	
	public double nextPayment() {
		if(nPayments == 0) return(0.0);
		return(monthlyPayment);
	}
	
	public int 		nPayments;
	public double 	monthlyPayment;
}
