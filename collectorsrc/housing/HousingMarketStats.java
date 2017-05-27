package housing;

import sim.util.Double2D;

public class HousingMarketStats extends CollectorBase {
	private static final long serialVersionUID = -535310555732796139L;

	private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public HousingMarketStats() {
		setActive(true);
		averageSoldPriceToOLP = 1.0;
		saleCount = 0;
		ftbSaleCount = 0;
		btlSaleCount = 0;
		nFTBSales = 0;
		nBTLSales = 0;
		nSales = 0;
		nBuyers = 0;
		nSellers = 0;
		nNewBuild = 0;
        priceData = new double[2][config.N_QUALITY];
        referencePriceData = new double[2][config.N_QUALITY];
        
        nEmpty = 0;
	}

	public void init(HousingMarket m) {
		market = m;
//		recorder = Model.transactionRecorder;
        int i;
        if(market != null) {
        	for(i=0; i<config.N_QUALITY; ++i) {
        		priceData[0][i] = market.referencePrice(i);
        		referencePriceData[0][i] = market.referencePrice(i);
        		referencePriceData[1][i] = market.referencePrice(i);
        	}
        }
	}
	
	public void record() {
		nSales = saleCount; saleCount = 0;
		nFTBSales = ftbSaleCount; ftbSaleCount = 0;
		nBTLSales = btlSaleCount; btlSaleCount = 0;
		nNewBuild = 0;
		nEmpty = 0;
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
			if(((HouseSaleRecord)sale).house.owner == Model.construction) nNewBuild++;
			if(((HouseSaleRecord)sale).house.resident == null) nEmpty++;
		}
		if(market.offersPQ.size() > 0) averageOfferPrice /= market.offersPQ.size();
		recordOfferPrices();
		recordBidPrices();
	}
	
	/*
	public void step() {
        int i;
        for(i=0; i<House.Config.N_QUALITY; ++i) {
        	priceData[1][i] = market.averageSalePrice[i];
        }
	}
	*/
	
	public void recordSale(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		if(sale.initialListedPrice > 0.01) {
			averageSoldPriceToOLP = config.derivedParams.E*averageSoldPriceToOLP + (1.0-config.derivedParams.E)*sale.getPrice()/sale.initialListedPrice;
		}
		saleCount += 1;
		MortgageAgreement mortgage = purchase.buyer.mortgageFor(sale.house);
		if(mortgage != null) {
			if(mortgage.isFirstTimeBuyer) {
				ftbSaleCount += 1;
			} else if(mortgage.isBuyToLet) {
				btlSaleCount += 1;
			}
		}
		Model.transactionRecorder.recordSale(purchase, sale, mortgage, market);
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
	public int 	  nNewBuild;
    public double [][]    priceData;
	public double [][]    referencePriceData;
	double [] offerPrices;
	double [] bidPrices;
	HousingMarket market;
//	MicroDataRecorder recorder;
	
	public int 	nEmpty;

	///////////////////////////////////////////////////////////////////////////////////////
	// Getters and setters for Mason
	///////////////////////////////////////////////////////////////////////////////////////
	
	public double getAverageDaysOnMarket() {
		return market.averageDaysOnMarket;
	}
	
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

	public int getnNewBuild() {
		return nNewBuild;
	}
	public String namenNewBuild() {
		return("Number of new-build houses on market");
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
		return(market.housePriceAppreciation(1));
	}
	public String nameHPA() {
		return("Annualised house price growth");
	}
	public String desHPA() {
		return("House price growth year-on-year");
	}

	public double getHPI() {
		return(market.housePriceIndex);
	}
	public String nameHPI() {
		return("House price index");
	}
	public String desHPI() {
		return("House price index");
	}

	public double getnEmpty() {
		return nEmpty;
	}

	
    public Double2D [] getmasonPriceData() {
    	Double2D [] data = new Double2D[config.N_QUALITY];
    	for(int i=0; i<config.N_QUALITY; ++i) {
    		data[i] = new Double2D(market.referencePrice(i), market.getAverageSalePrice(i));    		
    	}
    	return data;
	}
    
    
	public String namemasonPriceData() {
		return("Average Transaction Price / Reference Price");
	}
	public String desmasonPriceData() {
		return("Average Transaction Price / Reference Price");
	}

    public double [][] priceData() {
        int i;
        for(i=0; i<config.N_QUALITY; ++i) {
        	priceData[1][i] = market.averageSalePrice[i];
        }
        return(priceData);
    }

	

}
