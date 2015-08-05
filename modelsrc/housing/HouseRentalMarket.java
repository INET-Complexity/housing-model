package housing;

/***********************************************
 * Class that represents the market for houses for rent.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseRentalMarket extends HousingMarket {
	
	public HouseRentalMarket() {
		for(int i=0; i< House.Config.N_QUALITY; ++i) {
			daysOnMarket[i] = 30.0;			
		}
		recalculateExpectedGrossYield();
	}
	
	@Override
	public void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		super.completeTransaction(purchase, sale);
		daysOnMarket[sale.house.getQuality()] = Config.E*daysOnMarket[sale.house.getQuality()] + (1.0-Config.E)*(Model.t - sale.tInitialListing);
		sale.house.rentalRecord = null;
		purchase.buyer.completeHouseRental(sale);
		sale.house.owner.completeHouseLet(sale.house);
		Collectors.rentalMarketStats.recordSale(purchase, sale);
	}
	
	public HouseSaleRecord offer(House house, double price) {
		if(house.resident != null) {
			System.out.println("Got offer on rental market of house with resident");
		}
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
		return(super.referencePrice(quality)*0.03/12.0); // assume 3% gross yield on house price
	}
	
	/***
	 * @param quality Quality of the house
	 * @return Expected fraction of time that the house will be occupied, based on
	 *         a 12 month rental contract and the average number of days on the rental
	 *         market of a house of this quality.
	 */
	public double expectedOccupancy(int quality) {
		return(12.0*30.0/(12.0*30.0 + daysOnMarket[quality]));
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
		for(int q=0; q < House.Config.N_QUALITY; ++q) {
			expectedGrossYield[q] = getAverageSalePrice(q)*12.0*expectedOccupancy(q)/Model.housingMarket.getAverageSalePrice(q);
		}		
	}
	
	public double daysOnMarket[] = new double[House.Config.N_QUALITY];
	public double expectedGrossYield[] = new double[House.Config.N_QUALITY];
}
