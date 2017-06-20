package configuration;


import com.typesafe.config.Config;


/** Class encapsulating all of the housing market parameters.
 *
 * @author davidrpugh
 */
public class HousingMarketConfig {

    private int daysUnderOffer;
    private double bidUp;
    private double marketAveragePriceDecay;
    private double initialHousePriceIndex;
    private double medianHousePriceIndex;
    private double shapeHousePriceIndex;
    private int averageTenancyLength;
    private double rentGrossYield;

    HousingMarketConfig(Config config) {
        daysUnderOffer = config.getInt("days-under-offer");
        bidUp = config.getDouble("bid-up");
        marketAveragePriceDecay = config.getDouble("market-average-price-decay");
        initialHousePriceIndex = config.getDouble("initial-house-price-index");
        medianHousePriceIndex = config.getDouble("median-house-price-index");
        shapeHousePriceIndex = config.getDouble("shape-house-price-index");
        averageTenancyLength = config.getInt("average-tenancy-length");
        rentGrossYield = config.getDouble("rent-gross-yield");
    }

    public int getDaysUnderOffer() {
        return daysUnderOffer;
    }

    public double getBidUp() {
        return bidUp;
    }

    public double getMarketAveragePriceDecay() {
        return marketAveragePriceDecay;
    }

    public double getInitialHousePriceIndex() {
        return initialHousePriceIndex;
    }

    public double getMedianHousePriceIndex() {
        return medianHousePriceIndex;
    }

    public double getShapeHousePriceIndex() {
        return shapeHousePriceIndex;
    }

    public int getAverageTenancyLength() {
        return averageTenancyLength;
    }

    public double getRentGrossYield() {
        return rentGrossYield;
    }

}
