package data;

import housing.Config;
import housing.Model;

public class HouseRentalMarket {

	// TODO: The whole of this class content can be easily moved to the HousingMarket or the HouseRentalMarket class in the housing package

    private static Config config = Model.config;	// Passes the Model's configuration parameters object to a private field
	
	static public double referencePrice(int quality) {
        return(HouseSaleMarket.referencePrice(quality)*config.RENT_GROSS_YIELD/config.constants.MONTHS_IN_YEAR);
	}
	
/*
 * NOTES ON AGGREGATE DATA:
 * 57% of home moves in 2008/09 were private renters English Housing Survey report 08-09
 * 13% of households privately rent / 18% social housing (Social Trends:Housing 2011 ONS)
 */
}
