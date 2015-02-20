package housing;

public class Person {
	static class Config {
		public static double PMale = 105.1/205.1; // Probability of being male given that you were born in the UK 2007-2011 (Source: Birth ratios in the UK, 2013, Dept of health)
		public static SampledFunction PBirthGivenMarried = // monthly probability of birth by age for married female
				// source: ONS Characteristics of mother 2 (2012 figures)
			new SampledFunction(new Double[][] {
				{16.0, 0.0},	// minimum age for marriage in England and wales
				{20.0, 0.2232/12.0},
				{25.0, 0.2749/12.0},
				{30.0, 0.2290/12.0},
				{35.0, 0.1806/12.0},
				{40.0, 0.0762/12.0},
				{45.0, 0.0139/12.0},
				{51.0, 0.0009/12.0} // average age of menopause in UK (NHS choices http://www.nhs.uk/Conditions/Menopause/Pages/Introduction.aspx)
			});
		public static SampledFunction PBirthGivenUnmarried = // monthly probability of birth by age for unmarried female
				// source: ONS Characteristics of mother 2 (2012 figures)
			new SampledFunction(new Double [][] {
				{11.0, 0.0},	// average age of puberty in UK (NHS choices http://www.nhs.uk/conditions/Puberty/Pages/Introduction.aspx)
				{20.0, 0.0190/12.0},
				{25.0, 0.0579/12.0},
				{30.0, 0.0657/12.0},
				{35.0, 0.0659/12.0},
				{40.0, 0.0438/12.0},
				{45.0, 0.0130/12.0},
				{51.0, 0.0010/12.0} // average age of menopause in UK (NHS choices http://www.nhs.uk/Conditions/Menopause/Pages/Introduction.aspx)
			});
		
		public static SampledFunction PDeathGivenMale = // monthly mortality rate by age for males
				// Source: ONE Death registrations summary statistics, England and Wales 2013
				new SampledFunction(new Double [][] {
						{1.0,  0.0044/12.0},
						{5.0,  0.0002/12.0},
						{10.0, 0.0001/12.0},
						{15.0, 0.0001/12.0},
						{20.0, 0.0003/12.0},
						{25.0, 0.0005/12.0},
						{30.0, 0.0006/12.0},
						{35.0, 0.0008/12.0},
						{40.0, 0.0012/12.0},
						{45.0, 0.0017/12.0},
						{50.0, 0.0025/12.0},
						{55.0, 0.0037/12.0},
						{60.0, 0.0059/12.0},
						{65.0, 0.0096/12.0},
						{70.0, 0.0143/12.0},
						{75.0, 0.0245/12.0},
						{80.0, 0.0407/12.0},
						{85.0, 0.0732/12.0},
						{90.0, 0.1294/12.0},
						{1000.0, 0.2383/12.0}
				});

		public static SampledFunction PDeathGivenFemale = // monthly mortality rate by age for males
				// Source: ONE Death registrations summary statistics, England and Wales 2013
				new SampledFunction(new Double [][] {
						{1.0,  0.0035/12.0},
						{5.0,  0.0002/12.0},
						{10.0, 0.0001/12.0},
						{15.0, 0.0001/12.0},
						{20.0, 0.0001/12.0},
						{25.0, 0.0002/12.0},
						{30.0, 0.0003/12.0},
						{35.0, 0.0004/12.0},
						{40.0, 0.0007/12.0},
						{45.0, 0.0010/12.0},
						{50.0, 0.0016/12.0},
						{55.0, 0.0025/12.0},
						{60.0, 0.0040/12.0},
						{65.0, 0.0061/12.0},
						{70.0, 0.0094/12.0},
						{75.0, 0.0160/12.0},
						{80.0, 0.0281/12.0},
						{85.0, 0.0533/12.0},
						{90.0, 0.1004/12.0},
						{1000.0, 0.2101/12.0}
				});
}

	public Person(Household h) {
		household = h;
		status = Status.SINGLE;
		sex = (Model.rand.nextDouble() < Config.PMale?Sex.MALE:Sex.FEMALE);
		age = 0.0;
		income = 0.0;
	}
	
	enum Sex {
		MALE,
		FEMALE
	}
	enum Status {
		SINGLE,
//		PARTNERSHIP,
		MARRIED, // married or in civil partnership
		COHABITINGCOUPLE // income support distinction...? 
//		COHABITINGORMARRIED
	}
	
	public void step() {
		age += 1.0/12.0;
		
	}

	
	double		age;
	Sex 		sex;
	Status		status;
	double		income;
	Household 	household;
}
