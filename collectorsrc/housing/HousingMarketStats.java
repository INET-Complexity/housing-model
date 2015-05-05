package housing;

import housing.HousingMarket.Config;

public class HousingMarketStats {

	public HousingMarketStats(HousingMarket m) {
		averageSoldPriceToOLP = 1.0;
		saleCount = 0;
		nSales = 0;
		nBuyers = 0;
		nSellers = 0;
        priceData = new double[2][House.Config.N_QUALITY];
        referencePriceData = new double[2][House.Config.N_QUALITY];
        int i;
        for(i=0; i<House.Config.N_QUALITY; ++i) {
        	priceData[0][i] = HousingMarket.referencePrice(i);
        	referencePriceData[0][i] = HousingMarket.referencePrice(i);
        	referencePriceData[1][i] = HousingMarket.referencePrice(i);
        }
        market = m;
	}

	public void record() {
		nSales = saleCount;
		saleCount = 0;
		nSellers = market.onMarket.size();
		nBuyers = market.buyers.size();

		// -- Record average bid price
		// ---------------------------
		averageBidPrice = 0.0;
		for(HouseBuyerRecord buyer : market.buyers) {
			averageBidPrice += buyer.price;
		}
		if(market.buyers.size() > 0) averageBidPrice /= market.buyers.size();

		// -- Record average offer price
		// -----------------------------
		averageOfferPrice = 0.0;
		for(HouseSaleRecord sale : market.onMarket.values()) {
			averageOfferPrice += sale.currentPrice;
		}
		if(market.onMarket.size() > 0) averageOfferPrice /= market.onMarket.size();
		recordOfferPrices();
		recordBidPrices();
	}
	
	public void step() {
        int i;
        for(i=0; i<House.Config.N_QUALITY; ++i) {
        	priceData[1][i] = Model.housingMarket.averageSalePrice[i];
        }
	}
	
	public void recordSale(HouseSaleRecord sale) {
		if(sale.initialListedPrice > 0.01) {
			averageSoldPriceToOLP = Config.E*averageSoldPriceToOLP + (1.0-Config.E)*sale.currentPrice/sale.initialListedPrice;
		}
		saleCount += 1;			
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

	public double averageSoldPriceToOLP;
	public double averageBidPrice;
	public double averageOfferPrice;
	public int    nSales, saleCount;
	public int    nBuyers;
	public int    nSellers;
    public double [][]    priceData;
    public double [][]    referencePriceData;
	double [] offerPrices;
	double [] bidPrices;
	HousingMarket market;


	///////////////////////////////////////////////////////////////////////////////////////
	// Getters and setters for Mason
	///////////////////////////////////////////////////////////////////////////////////////
	
	public double[] getOfferPrices() {
		return(offerPrices);
	}

	public double[] getBidPrices() {
		return(bidPrices);
	}

	public double getAverageBidPrice() {
		return averageBidPrice;
	}

	public double getAverageOfferPrice() {
		return averageOfferPrice;
	}

	public int getnSales() {
		return nSales;
	}

	public int getnBuyers() {
		return nBuyers;
	}

	public int getnSellers() {
		return nSellers;
	}
	
	

}
