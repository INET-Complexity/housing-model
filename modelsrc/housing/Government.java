package housing;

/*****************************************
 * This class represents the government.
 * This is the class where taxation policy should be encoded.
 * 
 * @author daniel
 *
 ****************************************/
public class Government {
	
	/**
	 * Configuration for the govenment. This contains tax banding, NI rates etc.
	 * @author daniel
	 *
	 */
	static public class Config {
		public double PERSONAL_ALLOWANCE_LIMIT = 100000.0;

		// -- 2013/2014 rates
		public double [] TAX_BANDS = {9440, 9440+32010, 9440+150000};
		public double [] TAX_RATES = {.20, .40, .45};

		public double [] NI_BANDS = {7755, 41450};
		public double [] NI_RATES = {.12, .02};
		
		static double INCOME_SUPPORT = 113.7*52.0/12.0; // married couple's monthly lower earnings from income support Source: www.nidirect.gov.uk
	}
	
	public Government() {
		this(new Government.Config());
	}

	public Government(Government.Config c) {
		config = c;
	}

	/******************************************
	 * Calculates the income tax due in one year for a given 
	 * gross annual income. Doesn't account for married couple's allowance.
	 * 
	 * @param grossIncome The gross, annual income in pounds.
	 * @return The annual income tax due in pounds.
	 ******************************************/
	public double incomeTaxDue(double grossIncome) {
		double tax = bandedPercentage(grossIncome, config.TAX_BANDS, config.TAX_RATES);
		if(grossIncome > config.PERSONAL_ALLOWANCE_LIMIT) {
			double personalAllowance = Math.max((grossIncome - config.PERSONAL_ALLOWANCE_LIMIT)/2.0,0.0);
			tax += (config.TAX_BANDS[0]-personalAllowance)*config.TAX_RATES[0]; //TODO: this doens't make any sense!!!!
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
		return(bandedPercentage(grossIncome, config.NI_BANDS, config.NI_RATES));
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
	protected double bandedPercentage(double taxableIncome, double [] bands, double [] rates) {
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

	Government.Config	config;
	
}
