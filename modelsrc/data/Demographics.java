package data;

import org.apache.commons.math3.distribution.BetaDistribution;

import utilities.DoubleUnaryOperator;
import utilities.Pdf;

public class Demographics {
	//public static final int SPINUP_YEARS = 80;			// number of years to spinup
//	public static final BetaDistribution betaDist = new BetaDistribution(null,2,2);
	public static final BetaDistribution betaDist = new BetaDistribution(null,5,1);
	
	/**
	 * Target probability density of age of representative householder
	 * at time t=0
	 * Calibrated against (LCFS 2012)
	 */
	public static Pdf pdfAge = new Pdf("modelsrc/data/AgeMarginalPDFstatic.csv");

	/**
	 * Probability density by age of the representative householder given that
	 * the household is newly formed.
	 * New households can be formed by, e.g., children leaving home,
	 * divorce, separation, people leaving an HMO.
	 * Roughly calibrated against "The changing living arrangements of young adults in the UK"
	 *  ONS Population Trends winter 2009 
	 */
// --- calibrated version...
//	public static Pdf pdfHouseholdAgeAtBirth = new Pdf(15.0, 29.0, new DoubleUnaryOperator() {
//		public double applyAsDouble(double age) {
//			return(betaDist.density((age-14.5)/15.0));
//		}	
//	});
	// --- version to make correct age distribution at equilibrium demographics
	public static Pdf pdfHouseholdAgeAtBirth = new Pdf(15.0, 55.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			if(age<15.0) return(0.0);
			if(age<25.0) return(0.0198);
			if(age<35.0) return(0.0528);
			if(age<45.0) return(0.0162);
			if(age<=55.0) return(0.0112);
			return(0.0);
		}	
	},800);

	public static Pdf pdfSpinupHouseholdAgeAtBirth = new Pdf(15.0, 29.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			if(age>=15.0 && age < 16.0) {
				return(1.0);
			}
//			return(0.5*pdfHouseholdAgeAtBirth.density(age));
			return(0.0);
		}	
	});

	/****
	 * Birth rates into the future (roughly calibrated against current individual birth rate)
	 * @param t	time (months) into the future
	 * @return number of births per year per capita
	 * Calibrated against flux of FTBs, Council of Mortgage Lenders Regulated Mortgage Survey (2015)
	 */
//	public static double futureBirthRate(double t) {
//		return(0.0102);
//	}
	public static double futureBirthRate(double t) {
		return(0.018); // calibrated against average advances to first time buyers, core indicators 1987-2006
	}
	

	/***
	 * Probability that a household 'dies' per year given age of the representative householder
	 * Death of a household may occur by marriage, death of single occupant, moving together.
	 * As first order approx: we use female death rates, assuming singles live at home until marriage,
	 * there is no divorce and the male always dies first
	 * TODO: Add marriage/co-habitation
	 */
	public static double probDeathGivenAge(double ageInYears) {
		// calibrated against AgeMarginalPDF.csv for static age distribution
		if(ageInYears<55.0) return(0.0);
		if(ageInYears<65.0) return(0.0181);
		if(ageInYears<75.0) return(0.0142);
		if(ageInYears<85.0) return(0.0035);
		if(ageInYears<95.0) return(0.02); // not calibrated (calibrated against FTBs per capita)
		if(ageInYears<105.0) return(0.2); // not calibrated (calibrated against FTBs per capita)	
		return(6.0); // kill off anyone over 105
//		double PdeathOfFemale2012 = 3.788e-5*Math.exp(8.642e-2*ageInYears);
//		double tempFudgeFactor = 0.0002*Math.exp(6.2e-2*ageInYears); // to ensure population doesn't decrease after spinup (i.e. to allow correct birth rate)
		// ONS Statistical Bulletin: Historic and Projected Mortality. Data from the Period and Cohort
		// Life Tables, 2012-based, UK, 1981-2062
//		return(tempFudgeFactor*PdeathOfFemale2012);
	}

	/*
	 * This calculates the pdf of Household age at death from probDeathGivenAge() according to
	 * 
	 * P(a) = r(a) exp(-integral_0^a r(a') da')
	 * 
	 * where r(a) is probDeathGivenAge.
	 * 
	 */
	/*
	public static Pdf pdfHouseholdAgeAtDeath = new Pdf(0.0, 150.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			double a = 0.0;
			double da = 0.1;
			double integral = 0.0;
			double p;
			do {
				p = probDeathGivenAge(a + 0.5*da);
				integral += p*da;
				a += da;
			} while(a<=age);
			integral -= (a - age)*p;
			return(p*Math.exp(-integral));
//			double p = probDeathGivenAge(0.0);
//			return(p*Math.exp(-age*p));
		}
	}, 100);
	*/
}
