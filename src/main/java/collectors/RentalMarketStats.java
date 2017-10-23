package collectors;

import housing.Config;
import housing.Model;
import java.awt.geom.Point2D;

public class RentalMarketStats extends HousingMarketStats {
//	public double[] getExpectedGrossYieldByQuality() {
//		return(Model.houseRentalMarkets.expectedGrossYield);
//	}

	private Config config = Model.config;	// Passes the Model's configuration parameters object to a private field
	
    public Point2D [] getExpectedGrossYieldByQuality() {
		Point2D [] data = new Point2D[config.N_QUALITY];
    	for(int i=0; i<config.N_QUALITY; ++i) {
    		data[i] = new Point2D.Double(i, Model.houseRentalMarkets.getExpectedGrossYield(i));
    	}
    	return data;
	}

    public Point2D [] getExpectedOccupancyByQuality() {
		Point2D [] data = new Point2D[config.N_QUALITY];
    	for(int i=0; i<config.N_QUALITY; ++i) {
    		data[i] = new Point2D.Double(i, Model.houseRentalMarkets.expectedOccupancy(i));
    	}
    	return data;
	}
    
    public double getAverageSoldGrossYield() {
    	return(Model.houseRentalMarkets.averageSoldGrossYield);
    }

}
