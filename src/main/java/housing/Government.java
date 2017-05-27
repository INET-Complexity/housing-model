package housing;

/*****************************************
 * This class represents the government.
 * This is the class where taxation policy should be encoded.
 * 
 * @author daniel
 *
 ****************************************/
public class Government {

	private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

	/******************************************
	 * Calculates the income tax due in one year for a given 
	 * gross annual income. Doesn't account for married couple's allowance.
	 * 
	 * @param grossIncome The gross, annual income in pounds.
	 * @return The annual income tax due in pounds.
	 ******************************************/
	public double incomeTaxDue(double grossIncome) {
		double tax = bandedPercentage(grossIncome, data.Government.tax.bands, data.Government.tax.rates);
		if(grossIncome > config.GOVERNMENT_PERSONAL_ALLOWANCE_LIMIT) {
			//double personalAllowance = Math.max((grossIncome - config.GOVERNMENT_PERSONAL_ALLOWANCE_LIMIT)/2.0,0.0);
			double personalAllowance = Math.max(
					data.Government.tax.bands[0]-(grossIncome-config.GOVERNMENT_PERSONAL_ALLOWANCE_LIMIT)/2.0,
					0.0);
			tax += (data.Government.tax.bands[0]-personalAllowance)*data.Government.tax.rates[0]; // TODO: what does this do?
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
	public double class1NICsDue(double grossIncome) {
		return(bandedPercentage(grossIncome, data.Government.nationalInsurance.bands, data.Government.nationalInsurance.rates));
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
	protected double bandedPercentage(double taxableIncome, Double [] bands, Double [] rates) {
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
}
