package data;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class Households {
	static public double P_SELL = 1.0/(11.0*12.0);  // monthly probability of Owner-Occupier selling home (British housing survey 2008)
	static public double P_FORCEDTOMOVE = P_SELL*0.2;		// monthly probability of an OO being forced to move due to external factors (job change etc)
	static public double BTL_MU = Math.log(3.44); 	// location parameter for No. of houses owned by BtL
	static public double BTL_SIGMA = 1.050;			// shape parameter for No. of houses owned by BtL
	static public double P_INVESTOR = 0.04; 		// Prior probability of being (wanting to be) a property investor (should be 4%, 3% for stability for now)
	static public LogNormalDistribution buyToLetDistribution  = new LogNormalDistribution(BTL_MU, BTL_SIGMA); // No. of houses owned by buy-to-let investors Source: ARLA review and index Q2 2014

	// House price reduction behaviour. Calibrated against Zoopla data at BoE
	static public double P_SALEPRICEREDUCE = 1.0-0.944; 	// monthly probability of reducing the price of house on market
	static public double REDUCTION_MU = -3.002; 	// log-normal location parameter of house price reductions for houses on the market. 
	static public double REDUCTION_SIGMA = 0.6173;		// log-normal scale parameter of house price reductions for houses on the market
														
	
}
