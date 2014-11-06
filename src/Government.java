package eu.crisis_economics.abm.markets.housing;

/*****************************************
 * This class represents the government.
 * This is the class where taxation policy should be encoded.
 * 
 * @author daniel
 *
 ****************************************/
public class Government {
	
	
	/******************************************
	 * Calculates the income tax due in one year for a given 
	 * gross annual income. Doesn't account for married couple's allowance.
	 * 
	 * @param grossIncome The gross, annual income in pounds.
	 * @return The annual income tax due in pounds.
	 ******************************************/
	public static double incomeTaxDue(double grossIncome) {
		double tax = bandedPercentage(grossIncome, TAX_BANDS, TAX_RATES);
		if(grossIncome > PERSONAL_ALLOWANCE_LIMIT) {
			double personalAllowance = Math.max((grossIncome - PERSONAL_ALLOWANCE_LIMIT)/2.0,0.0);
			tax += (TAX_BANDS[0]-personalAllowance)*TAX_RATES[0];
		}
		return(tax);
	}
	
	/***********************************
	 * Calculate the class 1 National Insurance Contributions due on a
	 * given annual income (under PAYE).
	 * 
	 * @param grossIncome Gross annual income in pounds
	 * @return Annual class 1 NICs due.
	 **********************************/
	public static double class1NICsDue(double grossIncome) {
		return(bandedPercentage(grossIncome, NI_BANDS, NI_RATES));
	}
	
	/**********************************
	 * Calculate a "banded percentage" on a value.
	 * A "banded percentage" is a way of calculating a non-linear
	 * function, f(x), widely used by HMRC. The domain of
	 * values of f(x) is split into bands: from 0 to x1, from x1 to x2
	 * etc. Each band is associated with a percentage p1, p2 etc.
	 * The final value of f(x) is the sum of the percentages of each band.
	 * So, for example, if x lies somewhere between x1 and x2, f(x) would be 
	 * p1x1 + p2(x-x1)
	 * 
	 * @param taxableIncome the value to apply the banded percentage to.
	 * @param bands an array holding the upper limit of each band
	 * @param rates an array holding the percentage applicable to each band
	 * @return The banded percentage of 'taxableIncome'
	 ***********************************/
	protected static double bandedPercentage(double taxableIncome, final double [] bands, final double [] rates) {
		int i = 0;
		double lastRate = 0.0;
		double tax = 0.0;
		
		while(i < bands.length && taxableIncome > bands[i]) {
			tax += (taxableIncome - bands[i])*(rates[i] - lastRate);
			lastRate = rates[i];
			++i;
		}
		return(tax);
	}
	
	static final double PERSONAL_ALLOWANCE_LIMIT = 100000.0;

	// -- 2013/2014 rates
	static final double [] TAX_BANDS = {9440, 9440+32010, 9440+150000};
	static final double [] TAX_RATES = {.20, .40, .45};

	static final double [] NI_BANDS = {7755, 41450};
	static final double [] NI_RATES = {.12, .02};
	
}
