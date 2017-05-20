package housing;

import java.io.Serializable;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class Lifecycle implements Serializable {
	private static final long serialVersionUID = -2455155016204679970L;

    private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public Lifecycle(double iage) {
		age = iage;
		incomePercentile = rand.nextDouble();
		
	}
			
	public void step() {
		age += 1.0/config.constants.MONTHS_IN_YEAR;
	}
	
	/*** 
	 *
	 * @return Household income given age and percentile of population
	 */
	public double annualIncome() {
		double boundAge = age;
		if(boundAge < data.Lifecycle.lnIncomeGivenAge.getSupportLowerBound()) {
			boundAge = data.Lifecycle.lnIncomeGivenAge.getSupportLowerBound();
		}
		else if(boundAge > data.Lifecycle.lnIncomeGivenAge.getSupportUpperBound()) {
			boundAge = data.Lifecycle.lnIncomeGivenAge.getSupportUpperBound() - 1e-7;
		}
		double income = data.Lifecycle.lnIncomeGivenAge.getBinAt(boundAge).inverseCumulativeProbability(incomePercentile);
//        income = Math.exp(income)*52;
		income = Math.exp(income);
		if(income < config.GOVERNMENT_INCOME_SUPPORT) income = config.GOVERNMENT_INCOME_SUPPORT; // minimum income is govt. support
		return(income);
	}

	// TODO: This function is never used, check if removing (it's an alternative method for computing income dist. using only shape and median params.)
//	public static LogNormalDistribution [] setupIncomeByAge() {
//		LogNormalDistribution [] result = new LogNormalDistribution[100];
//		double median;
//		int age;
//		for(age = 0; age<100; ++age) {
//			// TODO: Also, what are all these hidden parameters?
//			median = -30*age*age + 2520*age - 14250;
//			if(median < 15000) median = 15000;
//			result[age] = new LogNormalDistribution(Math.log(median), INCOME_SHAPE);
//		}
//		return(result);
//	}

    // TODO: This is never used, check if removing (it's an alternative method for computing income dist. using only shape and median params.)
//	public static double INCOME_LOG_MEDIAN = Math.log(29580); // Source: IFS: living standards, poverty and inequality in the UK (22,938 after taxes) //Math.log(20300); // Source: O.N.S 2011/2012
//	public static double INCOME_SHAPE = (Math.log(44360) - INCOME_LOG_MEDIAN)/0.6745; // Source: IFS: living standards, poverty and inequality in the UK (75th percentile is 32692 after tax)

//	public static LogNormalDistribution incomeDistribution = new LogNormalDistribution(INCOME_LOG_MEDIAN, INCOME_SHAPE);

	private Model.MersenneTwister	rand = Model.rand;	// Passes the Model's random number generator to a private field
	double	age;				// age of representative householder
	double	incomePercentile; 	// fixed for lifetime of household
    // TODO: This is never used, check if removing (it's an alternative method for computing income dist. using only shape and median params.)
//	static LogNormalDistribution [] incomeByAge = setupIncomeByAge();
}
