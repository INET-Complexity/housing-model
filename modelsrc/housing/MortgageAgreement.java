package housing;

import java.io.Serializable;

public class MortgageAgreement extends PaymentAgreement {
	private static final long serialVersionUID = -1610029355056926296L;
	public double	downPayment;
	public double	purchasePrice;
	public final boolean	isBuyToLet;
	public boolean	isFirstTimeBuyer;
	public double	principal;			// remaining principal to be paid off
	public double 	monthlyInterestRate;

	public MortgageAgreement(Household borrower, boolean isBuyToLet) {
		this.isBuyToLet = isBuyToLet;
		if(!isBuyToLet && borrower.isFirstTimeBuyer()) {
			this.isFirstTimeBuyer = true;
		} else {
			this.isFirstTimeBuyer = false;
		}
	}

	/********************************************
	 * Updates internal variables to simulate a payment
	 * being made (Does not move any assets from payer to payee).
	 * 
	 * @return The amount of the payment
	 ********************************************/
	public double makeMonthlyPayment() {
		double payment = super.makeMonthlyPayment();
		principal = principal*(1.0 + monthlyInterestRate) - payment;
		if(nPayments == 0) Model.bank.endMortgageContract(this);
		return(payment);
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
			Model.bank.endMortgageContract(this);
			return(principal);
		}
		monthlyPayment *= (principal-amount)/principal;
		principal -= amount;
		return(amount);
	}


}
