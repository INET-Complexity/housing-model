package housing;

import org.apache.commons.math3.distribution.LogNormalDistribution;

public class Lifecycle {
	public Lifecycle(double iage) {
		age = iage;
		incomePercentile = Model.rand.nextDouble();
	}
			
	public void step() {
		age += 1.0/12.0;
	}
	
	/*** 
	 * TODO: Make this age dependent
	 * 
	 * @param age
	 * @param percentile
	 * @return Household income given age and percentile of population
	 */
	public double annualIncome() {
		return(incomeDistribution.inverseCumulativeProbability(incomePercentile));
	}

	public static double INCOME_LOG_MEDIAN = Math.log(29580); // Source: IFS: living standards, poverty and inequality in the UK (22,938 after taxes) //Math.log(20300); // Source: O.N.S 2011/2012
	public static double INCOME_SHAPE = (Math.log(44360) - INCOME_LOG_MEDIAN)/0.6745; // Source: IFS: living standards, poverty and inequality in the UK (75th percentile is 32692 after tax)
	public static LogNormalDistribution incomeDistribution = new LogNormalDistribution(INCOME_LOG_MEDIAN, INCOME_SHAPE);

	double	age;				// age of representative householder
	double	incomePercentile; 	// fixed for lifetime of household
}
