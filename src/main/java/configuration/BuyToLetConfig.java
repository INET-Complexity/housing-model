package configuration;

import com.typesafe.config.Config;

/** Class encapsulating the parameters for "buy-to-let" investors.
 *
 * @author davidrpugh
 */
public class BuyToLetConfig {

    private double probabilityInvestor;
    private double minIncomePercentile;
    private double fundamentalistCapitalGainCoefficient;
    private double trendFollowersCapitalGainCoefficient;
    private double probabilityFundamentalist;
    private boolean buyToLetYieldScaling;

    BuyToLetConfig(Config config) {
        probabilityInvestor = config.getDouble("probability-investor");
        minIncomePercentile = config.getDouble("min-income-percentile");
        fundamentalistCapitalGainCoefficient = config.getDouble("fundamentalist-capitalGain-coefficient");
        trendFollowersCapitalGainCoefficient = config.getDouble("trend-followers-capital-gain-coefficient");
        probabilityFundamentalist = config.getDouble("probability-fundamentalist");
        buyToLetYieldScaling = config.getBoolean("buy-to-let-yield-scaling");
    }

    /** Prior probability of being (wanting to be) a BTL investor (double)
     *
     * TODO: 6/22/2017 Shouldn't this be 4% according to the article?
     */
    public double getProbabilityInvestor() {
        return probabilityInvestor;
    }

    /** Minimum income percentile for a household to be a BTL investor. */
    public double getMinIncomePercentile() {
        return minIncomePercentile;
    }

    /** Weight that fundamentalists put on cap gain. */
    public double getFundamentalistCapitalGainCoefficient() {
        return fundamentalistCapitalGainCoefficient;
    }

    /** Weight that trend-followers put on cap gain. */
    public double getTrendFollowersCapitalGainCoefficient() {
        return trendFollowersCapitalGainCoefficient;
    }

    /** Probability that a BTL investor is a fundamentalist versus a trend-follower. */
    public double getProbabilityFundamentalist() {
        return probabilityFundamentalist;
    }

    /** Chooses between two possible equations for BTL investors to make their buy/sell decisions (boolean) */
    public boolean isBuyToLetYieldScaling() {
        return buyToLetYieldScaling;
    }

}
