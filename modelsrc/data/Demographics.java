package data;

import utilities.DoubleUnaryOperator;
import utilities.Pdf;

public class Demographics {
	/**
	 * Target probability density of age of representative householder
	 * at time t=0
	 */
	public static Pdf pdfAge = new Pdf("modelsrc/data/AgeMarginalPDF.csv");
			/*
			18.0, 100.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			if(age > 18.0 && age < 50.0) {
				return(0.01125 + 0.0002734375*(age-18.0));
			}
			if(age <100.0) {
				return(0.02-0.0004*(age-50.0));
			}
			return(0.0);	
		}
	});
*/
	/**
	 * Probability density by age of the representative householder given that
	 * the household is newly formed.
	 * New households can be formed by, e.g., children leaving home,
	 * divorce, separation, people leaving an HMO.
	 */
	public static Pdf pdfHouseholdAgeAtBirth = new Pdf(18.0, 28.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			if(age>=18.0 && age < 19.0) {
				return(1.0);
			}
//			if(age>=18.0 && age<28.0) 
//				return(0.1);
			return(0.0);
		}	
	});

	/***
	 * Probability that a household 'dies' per year given age of the representative householder
	 * Death of a household may occur by marriage, death of single occupant, moving together
	 */
	public static double probDeathGivenAge(double ageInYears) {
		double averageDeathRate = housing.Demographics.futureBirthRate(0)*1.0/housing.Demographics.TARGET_POPULATION;
		return(averageDeathRate*ageInYears*ageInYears/7500.0);
	}

	/***
	 * This calculates the pdf of Household age at death from probDeathGivenAge() according to
	 * 
	 * P(a) = r(a) exp(-integral_0^a r(a') da')
	 * 
	 * where r(a) is probDeathGivenAge.
	 * 
	 */
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
}
