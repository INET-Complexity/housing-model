package housing;

/***********************************************
 * Class that represents the market for houses for rent.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseRentalMarket extends HousingMarket {
		
	@Override
	public void init() {
		super.init();
		int i;
		for(i = 0; i<House.Config.N_QUALITY; ++i) {
			averageSalePrice[i] *= 0.03/12.0; // assume 3% gross yield on house price
		}
	}
	
	@Override
	public void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		super.completeTransaction(purchase, sale);
		purchase.buyer.completeHouseRental(sale);
		sale.house.owner.completeHouseLet(sale.house);
		Collectors.rentalMarketStats.recordSale(purchase, sale);
	}
}
