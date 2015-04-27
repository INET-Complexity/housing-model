package housing;

public class Lifecycle {
	public Lifecycle() {
		age = chooseInitialAge();
		
	}
	
	public double chooseInitialAge() {
		return(18.0 + Model.rand.nextDouble()*10.0);
	}
		
	double	age;				// age of representative householder
	double	income;				// household income
	double	incomePercentile; 	// fixed for lifetime of household
}
