package housing;

public class MarketStatistics {
	public MarketStatistics(HousingMarket m) {
		market = m;
	}
	
	public double getHPI() {
		return(market.housePriceIndex);
	}
	
	public double[] getOfferPrices() {
		return(offerPrices);
	}

	public double[] getBidPrices() {
		return(bidPrices);
	}
	
	public void recordStats() {
		recordOfferPrices();
		recordBidPrices();
	}
	
	protected void recordOfferPrices() {
		offerPrices = new double[market.onMarket.size()];
		int i = 0;
		for(HouseSaleRecord sale : market.onMarket.values()) {
			offerPrices[i] = sale.currentPrice;
			++i;
		}
	}

	protected void recordBidPrices() {
		bidPrices = new double[market.buyers.size()];
		int i = 0;
		
		for(HouseBuyerRecord bid : market.buyers) {
			bidPrices[i] = bid.price;
			++i;
		}
	}

	public HousingMarket market;
	double [] offerPrices;
	double [] bidPrices;
}
