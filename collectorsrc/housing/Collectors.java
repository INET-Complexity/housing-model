package housing;

public class Collectors {
		
	public Collectors() {
		housingMarketStats = new HousingMarketStats(Model.housingMarket);
		rentalMarketStats = new HousingMarketStats(Model.rentalMarket);
	}
	
	public void step() {
		creditSupply.step();
		householdStats.step();
		housingMarketStats.step();
//		rentalMarketStats.step();
	}
	
	public static CreditSupply		creditSupply 	= new CreditSupply();
	public static CoreIndicators	coreIndicators 	= new CoreIndicators();
	public static HouseholdStats	householdStats	= new HouseholdStats();
	public static HousingMarketStats housingMarketStats;// = new HousingMarketStats(Model.housingMarket);
	public static HousingMarketStats rentalMarketStats;// = new HousingMarketStats(Model.rentalMarket);

	/////////////////////////////////////////////////////////////////////
	// Getters for MASON...yawn.
	/////////////////////////////////////////////////////////////////////
	
	public static CreditSupply getCreditSupply() {
		return creditSupply;
	}

	public static HousingMarketStats getHousingMarketStats() {
		return housingMarketStats;
	}

	public static HousingMarketStats getRentalMarketStats() {
		return rentalMarketStats;
	}

	public static CoreIndicators getCoreIndicators() {
		return coreIndicators;
	}

	public static HouseholdStats getHouseholdStats() {
		return householdStats;
	}	
}
