package housing;

public class MarketStatistics {
	public MarketStatistics(HousingMarket m) {
		market = m;
	}
	
	public double getHPI() {
		return(market.housePriceIndex);
	}
	
	public double[] getOffers() {
		double[] prices = new double[market.onMarket.size()];
		int i = 0;
		for(HouseSaleRecord sale : market.onMarket.values()) {
			prices[i] = sale.currentPrice;
			++i;
		}
		return(prices);
	}

	public double[] getBids() {
		double[] prices = new double[market.buyers.size()];
		int i = 0;
		
		for(HouseBuyerRecord bid : market.buyers) {
			prices[i] = bid.price;
			++i;
		}
		return(prices);
	}

	public HousingMarket market;
}
