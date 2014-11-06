package eu.crisis_economics.abm.markets.housing;

/************************************************
 * Class representing a house.
 * Use this to represent the intrinsic properties of the house.
 * 
 * @author daniel
 *
 ************************************************/
public class House {
	
	// placeholder
	public String getUniqueName() {
		return(Integer.toString(hashCode()));
	}

	////////////////////////////////////////////////////////////////////////
	/***
	static public int randomInitialQuality() {
		// based on initial income distribution
		int quality = (int)(Household.randomInitiaAnnuallIncome()*N_QUALITY/Household.INCOME_95_PERCENTILE);
		if(quality >= N_QUALITY) quality = N_QUALITY-1;
		return(quality);
	}
***/
	////////////////////////////////////////////////////////////////////////
	// Assigns a quality based on an annual income
	// quality is log of income, so should be normal distribution for
	// log-normal income distribution. Scaled to centre the normal distribution
	// on the quality scale with the edges of the scale at 2 standard deviations.
	// 
	////////////////////////////////////////////////////////////////////////
/***
	public void initialiseQuality(double income) {
		final double SDs = 2.0; // number of SDs to fit within the scale
		income = 1.64*(Math.log(income)-Household.INCOME_LOG_MEDIAN)/(Household.INCOME_LOG_95_PERCENTILE - Household.INCOME_LOG_MEDIAN); //transform to N(0,1) Gaussian
		quality = (int)(N_QUALITY*(income+SDs)/(2.0*SDs)); // scale to fit +-SDs standard deviations
		if(quality >= N_QUALITY) quality = N_QUALITY-1;
		if(quality < 0) quality = 0;
	}
	***/
	
	public int 				quality;
	public IHouseOwner  	owner;
	public Household		resident;
	
	public static final int N_QUALITY = 48; // number of quality bands
}
