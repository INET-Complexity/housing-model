package collectors;

import housing.Config;
import housing.Model;
import sim.util.Double2D;

public class RentalMarketStats extends HousingMarketStats {
//	public double[] getExpectedGrossYieldByQuality() {
//		return(Model.rentalMarket.expectedGrossYield);
//	}

	private Config config = Model.config;	// Passes the Model's configuration parameters object to a private field
	
    public Double2D [] getExpectedGrossYieldByQuality() {
    	Double2D [] data = new Double2D[config.N_QUALITY];
    	for(int i=0; i<config.N_QUALITY; ++i) {
    		data[i] = new Double2D(i, Model.rentalMarket.getExpectedGrossYield(i));    		
    	}
    	return data;
	}

    public Double2D [] getExpectedOccupancyByQuality() {
    	Double2D [] data = new Double2D[config.N_QUALITY];
    	for(int i=0; i<config.N_QUALITY; ++i) {
    		data[i] = new Double2D(i, Model.rentalMarket.expectedOccupancy(i));    		
    	}
    	return data;
	}
    
    public double getAverageSoldGrossYield() {
    	return(Model.rentalMarket.averageSoldGrossYield);
    }

}
