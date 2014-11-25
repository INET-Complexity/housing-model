package housing;

/****************************************************
 * This class is created by a mortgage-lender when it approves a mortgage.
 * It acts as a convenient container of information pertaining to a
 * mortgage, and doubles up as a contract that represents the mortgage
 * itself.
 * 
 * @author daniel
 *
 ***************************************************/
public class MortgageApproval {
	
	/********************************************
	 * Updates internal variables to simulate a payment
	 * being made (Does not move any assets from payer to payee).
	 * 
	 * @return The amount of the payment
	 ********************************************/
	public double makeMonthlyPayment() {
		if(nPayments == 0) return(0.0);
		nPayments -= 1;
		principal = principal*(1.0 + monthlyInterest) - monthlyPayment;
		return(monthlyPayment);
	}

	/*******************************************
	 * Use this to pay off the mortgage early or make
	 * a one-off payment.
	 * 
	 * @param amount Desired amount to pay off
	 * @return Amount that was actually payed off.
	 *******************************************/
	public double payoff(double amount) {
		if(amount >= principal) {
			principal = 0.0;
			monthlyPayment = 0.0;
			nPayments = 0;
			return(principal);
		}
		monthlyPayment *= (principal-amount)/principal;
		principal -= amount;
		return(amount);
	}
	
	public int 		nPayments;
	public double 	monthlyPayment;
	public double	principal;
	public double	monthlyInterest;
	public double	downPayment;
}
