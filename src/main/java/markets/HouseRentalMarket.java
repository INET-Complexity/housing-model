package markets;

import housing.House;
import housing.Model;

/**************************************************************************************************
 * Class to represent the rental market
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseRentalMarket extends HousingMarket {
	private static final long serialVersionUID = -3039057421808432696L;

    //-------------------//
    //----- Methods -----//
    //-------------------//

    @Override
	public void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale) {
        Model.rentalMarketStats.recordTransaction(sale);
		sale.house.rentalRecord = null;
		purchase.buyer.completeHouseRental(sale);
		sale.house.owner.completeHouseLet(sale);
		Model.rentalMarketStats.recordSale(purchase, sale);
	}

	@Override
	public HouseSaleRecord submitOffer(House house, double price) {
		if(house.isOnMarket()) {
			System.out.println("Got submitOffer on rental market of house already on sale market");
		}
		HouseSaleRecord hsr = super.submitOffer(house, price);
		house.putForRent(hsr);
		return(hsr);
	}
	
	@Override
	public void cancelOffer(HouseSaleRecord hsr) {
		super.cancelOffer(hsr);
		hsr.house.resetRentalRecord();
	}
}
