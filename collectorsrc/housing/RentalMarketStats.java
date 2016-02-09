package housing;

import sim.util.Double2D;

public class RentalMarketStats extends HousingMarketStats {
//	public double[] getExpectedGrossYieldByQuality() {
//		return(Model.rentalMarket.expectedGrossYield);
//	}
	
    public Double2D [] getExpectedGrossYieldByQuality() {
    	Double2D [] data = new Double2D[House.Config.N_QUALITY];
    	for(int i=0; i<House.Config.N_QUALITY; ++i) {
    		data[i] = new Double2D(i, Model.rentalMarket.getExpectedGrossYield(i));    		
    	}
    	return data;
	}

    public Double2D [] getExpectedOccupancyByQuality() {
    	Double2D [] data = new Double2D[House.Config.N_QUALITY];
    	for(int i=0; i<House.Config.N_QUALITY; ++i) {
    		data[i] = new Double2D(i, Model.rentalMarket.expectedOccupancy(i));    		
    	}
    	return data;
	}

}
