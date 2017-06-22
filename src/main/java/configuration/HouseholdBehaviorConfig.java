package configuration;


import com.typesafe.config.Config;


public class HouseholdBehaviorConfig {

    private BuyToLetConfig buyToLetConfig;

    HouseholdBehaviorConfig(Config config) {
        buyToLetConfig = new BuyToLetConfig(config.getConfig("buy-to-let"));
    }

    public BuyToLetConfig getBuyToLetConfig() {
        return buyToLetConfig;
    }

}
