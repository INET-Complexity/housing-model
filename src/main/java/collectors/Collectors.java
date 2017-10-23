package collectors;

import housing.Model;

import java.io.Serializable;

public class Collectors implements Serializable {
	private static final long serialVersionUID = -1116526042375828663L;

	public Collectors(String outputFolder) {
        creditSupply 	= new CreditSupply(outputFolder);
        coreIndicators 	= new CoreIndicators();
        householdStats	= new HouseholdStats();
        housingMarketStats = new HousingMarketStats();
        rentalMarketStats = new RentalMarketStats();
    }
	
	public void init() {
		housingMarketStats.init(Model.houseSaleMarkets);
		rentalMarketStats.init(Model.houseRentalMarkets);
	}
	
	public void step() {
		if(creditSupply.isActive()) creditSupply.step();
		if(householdStats.isActive()) householdStats.step();
//		if(housingMarketStats.isActive()) housingMarketStats.step();
//		if(rentalMarketStats.isActive()) rentalMarketStats.step();
	}
		
	public CreditSupply		creditSupply;
	public CoreIndicators	coreIndicators;
	public HouseholdStats	householdStats;
	public HousingMarketStats housingMarketStats;
	public RentalMarketStats rentalMarketStats;
}
