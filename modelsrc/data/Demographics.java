package data;

import org.apache.commons.math3.distribution.BetaDistribution;

import utilities.DoubleUnaryOperator;
import utilities.Pdf;

public class Demographics {
	//public static final int SPINUP_YEARS = 80;			// number of years to spinup
	public static final BetaDistribution betaDist = new BetaDistribution(null,2,2);
	/**
	 * Target probability density of age of representative householder
	 * at time t=0
	 * Calibrated against (LCFS 2012)
	 */
	public static Pdf pdfAge = new Pdf("modelsrc/data/AgeMarginalPDF.csv");

	/**
	 * Probability density by age of the representative householder given that
	 * the household is newly formed.
	 * New households can be formed by, e.g., children leaving home,
	 * divorce, separation, people leaving an HMO.
	 * Roughly calibrated against "The changing living arrangements of young adults in the UK"
	 *  ONS Population Trends winter 2009 
	 */
	public static Pdf pdfHouseholdAgeAtBirth = new Pdf(15.0, 29.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			return(betaDist.density((age-14.5)/15.0));
//			if(age>=15.0 && age < 16.0) {
//				return(1.0);
//			}
//			if(age>=18.0 && age<28.0) 
//				return(0.1);
//			return(0.0);
		}	
	});

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
	public static double futureBirthRate(double t) {
		return(0.0102);
	}

	/***
	 * Probability that a household 'dies' per year given age of the representative householder
	 * Death of a household may occur by marriage, death of single occupant, moving together.
	 * As first order approx: we use female death rates, assuming singles live at home until marriage,
	 * there is no divorce and the male always dies first
	 * TODO: Add marriage/co-habitation
	 */
	public static double probDeathGivenAge(double ageInYears) {
		double PdeathOfFemale2012 = 3.788e-5*Math.exp(8.642e-2*ageInYears);
		// ONS Statistical Bulletin: Historic and Projected Mortality. Data from the Period and Cohort
		// Life Tables, 2012-based, UK, 1981-2062
		return(PdeathOfFemale2012);
//		double averageDeathRate = futureBirthRate(0);		
//		return(averageDeathRate*ageInYears*ageInYears/7500.0);
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
