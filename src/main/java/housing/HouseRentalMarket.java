package housing;

import org.apache.commons.math3.random.MersenneTwister;

/**************************************************************************************************
 * Class to represent the rental market
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseRentalMarket extends HousingMarket {

    //-------------------//
    //----- Methods -----//
    //-------------------//

	public HouseRentalMarket(MersenneTwister prng) {
	    super(prng);
    }

    @Override
	public void completeTransaction(HouseBidderRecord purchase, HouseOfferRecord sale) {
        Model.rentalMarketStats.recordTransaction(sale);
		sale.getHouse().rentalRecord = null;
		RentalAgreement rentalAgreement = purchase.getBidder().completeHouseRental(sale);
		sale.getHouse().owner.completeHouseLet(sale, rentalAgreement);
		Model.rentalMarketStats.recordSale(purchase, sale);
	}

	@Override
	public HouseOfferRecord offer(House house, double price, boolean BTLOffer) {
		if(house.isOnMarket()) {
			System.out.println("Got offer on rental market of house already on sale market");			
		}
		HouseOfferRecord hsr = super.offer(house, price, false);
		house.putForRent(hsr);
		return(hsr);
	}
	
	@Override
	public void removeOffer(HouseOfferRecord hsr) {
		super.removeOffer(hsr);
		hsr.getHouse().resetRentalRecord();
	}
}
