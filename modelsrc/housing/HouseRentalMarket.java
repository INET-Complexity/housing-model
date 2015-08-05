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

}
