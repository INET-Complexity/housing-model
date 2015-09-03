package housing;

import java.io.Serializable;

public class Collectors implements Serializable {
	private static final long serialVersionUID = -1116526042375828663L;
	
	public void init() {
		housingMarketStats.init(Model.housingMarket);
		rentalMarketStats.init(Model.rentalMarket);
	}
	
	public void step() {
		if(creditSupply.isActive()) creditSupply.step();
		if(householdStats.isActive()) householdStats.step();
//		if(housingMarketStats.isActive()) housingMarketStats.step();
//		if(rentalMarketStats.isActive()) rentalMarketStats.step();
	}
		
	public CreditSupply		creditSupply 	= new CreditSupply();
	public CoreIndicators	coreIndicators 	= new CoreIndicators();
	public HouseholdStats	householdStats	= new HouseholdStats();
	public HousingMarketStats housingMarketStats = new HousingMarketStats();
	public HousingMarketStats rentalMarketStats = new HousingMarketStats();
	
	/////////////////////////////////////////////////////////////////////
	// Getters for MASON...yawn.
	/////////////////////////////////////////////////////////////////////
	
}
