package housing;

import housing.HousingMarket.Config;

public class HousingMarketStats {

	public HousingMarketStats(HousingMarket m) {
		averageSoldPriceToOLP = 1.0;
		saleCount = 0;
		ftbSaleCount = 0;
		btlSaleCount = 0;
		nFTBSales = 0;
		nBTLSales = 0;
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
		nSales = saleCount; saleCount = 0;
		nFTBSales = ftbSaleCount; ftbSaleCount = 0;
		nBTLSales = btlSaleCount; btlSaleCount = 0;
		nSellers = market.offersPQ.size();
		nBuyers = market.bids.size();

		// -- Record average bid price
		// ---------------------------
		averageBidPrice = 0.0;
		for(HouseBuyerRecord buyer : market.bids) {
			averageBidPrice += buyer.getPrice();
		}
		if(market.bids.size() > 0) averageBidPrice /= market.bids.size();

		// -- Record average offer price
		// -----------------------------
		averageOfferPrice = 0.0;
		for(HousingMarketRecord sale : market.offersPQ) {
			averageOfferPrice += sale.getPrice();
		}
		if(market.offersPQ.size() > 0) averageOfferPrice /= market.offersPQ.size();
		recordOfferPrices();
		recordBidPrices();
	}
	
	public void step() {
        int i;
        for(i=0; i<House.Config.N_QUALITY; ++i) {
        	priceData[1][i] = Model.housingMarket.averageSalePrice[i];
        }
	}
	
	public void recordSale(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		if(sale.initialListedPrice > 0.01) {
			averageSoldPriceToOLP = Config.E*averageSoldPriceToOLP + (1.0-Config.E)*sale.getPrice()/sale.initialListedPrice;
		}
		saleCount += 1;
		MortgageApproval mortgage = purchase.buyer.housePayments.get(sale.house);
		if(mortgage.isFirstTimeBuyer) {
			ftbSaleCount += 1;
		} else if(mortgage.isBuyToLet) {
			btlSaleCount += 1;
		}
		
	}
		
	protected void recordOfferPrices() {
		offerPrices = new double[market.offersPQ.size()];
		int i = 0;
		for(HousingMarketRecord sale : market.offersPQ) {
			offerPrices[i] = sale.getPrice();
			++i;
		}
	}

	protected void recordBidPrices() {
		bidPrices = new double[market.bids.size()];
		int i = 0;
		
		for(HouseBuyerRecord bid : market.bids) {
			bidPrices[i] = bid.getPrice();
			++i;
		}
	}

	public double averageSoldPriceToOLP;
	public double averageBidPrice;
	public double averageOfferPrice;
	public int    nSales, saleCount;
	public int	  nFTBSales, ftbSaleCount;	  // number of sales to first-time-buyers
	public int	  nBTLSales, btlSaleCount;	  // number of sales to first-time-buyers
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
	public String nameOfferPrices() {
		return("Offer prices");
	}

	public double[] getBidPrices() {
		return(bidPrices);
	}
	public String nameBidPrices() {
		return("Bid prices");
	}

	public double getAverageBidPrice() {
		return averageBidPrice;
	}
	public String nameAverageBidPrice() {
		return("Average bid price");
	}

	public double getAverageOfferPrice() {
		return averageOfferPrice;
	}
	public String nameAverageOfferPrice() {
		return("Averrage offer price");
	}

	public int getnSales() {
		return nSales;
	}
	public String namenSales() {
		return("Number of sales");
	}

	public int getnBuyers() {
		return nBuyers;
	}
	public String namenBuyers() {
		return("Number of buyers");
	}

	public int getnSellers() {
		return nSellers;
	}
	public String namenSellers() {
		return("Number of sellers");
	}
	
	public double getFTBSalesProportion() {
		return nFTBSales/(nSales+1e-8);
	}
	public String nameFTBSalesProportion() {
		return("Proportion of Sales FTB");
	}
	public String desFTBSalesProportion() {
		return("Proportion of monthly sales that are to First-time-buyers");
	}
	
	public double getBTLSalesProportion() {
		return nBTLSales/(nSales+1e-8);
	}
	public String nameBTLSalesProportion() {
		return("Proportion of Sales BTL");
	}
	public String desBTLSalesProportion() {
		return("Proportion of monthly sales that are to Buy-to-let investors");
	}
	
	public double getHPA() {
		return(Model.housingMarket.getHPIAppreciation());
	}
	public String nameHPA() {
		return("Annualised house price growth");
	}
	public String desHPA() {
		return("House price growth year-on-year");
	}
}
