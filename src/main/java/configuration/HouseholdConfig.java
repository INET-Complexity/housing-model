package configuration;


import com.typesafe.config.Config;


/** Class encapsulating all of the household parameters.
 *
 * @author davidrpugh
 */
public class HouseholdConfig {

    private HouseholdBehaviorConfig behaviorConfig;

    HouseholdConfig(Config config) {
        behaviorConfig = new HouseholdBehaviorConfig(config.getConfig("behavior"));
    }

    public HouseholdBehaviorConfig getBehaviorConfig() {
        return behaviorConfig;
    }

}
