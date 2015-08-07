package data;

public class HouseRentalMarket {
	
	/**
	 * Based of 3% gross yield
	 */
	static public double referencePrice(int quality) {
		return(HouseSaleMarket.referencePrice(quality)*0.03/12.0);
	}

}
