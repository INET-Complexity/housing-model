package housing;

/***********************************************
 * Class that represents the market for houses for rent.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseRentalMarket extends HousingMarket {
	private static final long serialVersionUID = -3039057421808432696L;
	static final double K = Math.exp(-1.0/50.0); // decay rate for averageGrossYield

	public HouseRentalMarket() {
		for(int i=0; i< House.Config.N_QUALITY; ++i) {
			daysOnMarket[i] = 5.0;			
		}
		recalculateExpectedGrossYield();
		averageSoldGrossYield = 0.05;
	}
	
	@Override
	public void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		super.completeTransaction(purchase, sale);
		daysOnMarket[sale.house.getQuality()] = Config.E*daysOnMarket[sale.house.getQuality()] + (1.0-Config.E)*(Model.getTime() - sale.tInitialListing);
		sale.house.rentalRecord = null;
		purchase.buyer.completeHouseRental(sale);
		sale.house.owner.completeHouseLet(sale);
		Model.collectors.rentalMarketStats.recordSale(purchase, sale);
		averageSoldGrossYield = averageSoldGrossYield*K + (1.0-K)*sale.getPrice()*12.0/Model.housingMarket.getAverageSalePrice(sale.house.getQuality());
	}
	
	public HouseSaleRecord offer(House house, double price) {
//		if(house.resident != null) {
//			System.out.println("Got offer on rental market of house with resident");
//		}
		if(house.isOnMarket()) {
			System.out.println("Got offer on rental market of house already on sale market");			
		}
		HouseSaleRecord hsr = super.offer(house, price);
		house.putForRent(hsr);
		return(hsr);
	}
	
	@Override
	public void removeOffer(HouseSaleRecord hsr) {
		super.removeOffer(hsr);
		hsr.house.resetRentalRecord();
	}

	@Override
	public double referencePrice(int quality) {
		return(data.HouseRentalMarket.referencePrice(quality));
	}
	
	/***
	 * @param quality Quality of the house
	 * @return Expected fraction of time that the house will be occupied, based on
	 *         a 18 month average tenant stay (ARLA figures) and the average number
	 *         of days on the rental market of a house of this quality.
	 *         TODO: Take the 18 out as data
	 */
	public double expectedOccupancy(int quality) {
		return(18.0*30.0/(18.0*30.0 + daysOnMarket[quality]));
	}

	public double getExpectedGrossYield(int quality) {
		return expectedGrossYield[quality];
	}

	@Override
	protected void recordMarketStats() {
		super.recordMarketStats();
		recalculateExpectedGrossYield();
	}
	
	protected void recalculateExpectedGrossYield() {
//		bestGrossYield = 0.0;
		for(int q=0; q < House.Config.N_QUALITY; ++q) {
			expectedGrossYield[q] = getAverageSalePrice(q)*12.0*expectedOccupancy(q)/Model.housingMarket.getAverageSalePrice(q);
//			if(expectedGrossYield[q] > bestGrossYield) bestGrossYield = expectedGrossYield[q];
		}		
	}
	
	public double daysOnMarket[] = new double[House.Config.N_QUALITY];
	public double expectedGrossYield[] = new double[House.Config.N_QUALITY];
	public double averageSoldGrossYield;
//	public double bestGrossYield;
}
