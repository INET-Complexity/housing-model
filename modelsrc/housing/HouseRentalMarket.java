package housing;

/***********************************************
 * Class that represents the market for houses for rent.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseRentalMarket extends HousingMarket {
	@Override
	public void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		super.completeTransaction(purchase, sale);
		purchase.buyer.completeHouseRental(sale);
		sale.house.owner.completeHouseLet(sale.house);
		Collectors.rentalMarketStats.recordSale(sale);
	}
}
