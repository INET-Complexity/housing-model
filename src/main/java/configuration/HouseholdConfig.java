package configuration;


import com.typesafe.config.Config;

public class HouseholdConfig {

    private HouseholdBehaviorConfig behaviorConfig;

    HouseholdConfig(Config config) {
        behaviorConfig = new HouseholdBehaviorConfig(config.getConfig("behavior"));
    }

    public HouseholdBehaviorConfig getBehaviorConfig() {
        return behaviorConfig;
    }

}
