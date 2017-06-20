package configuration;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


/** Class encapsulating all of the model parameters.
 *
 * @author davidrpugh
 */
public class ModelConfig {

    private HousingMarketConfig housingMarketConfig;
    private GovernmentConfig governmentConfig;

    public ModelConfig(String filename) {
        Config config = ConfigFactory.load(filename);
        housingMarketConfig = new HousingMarketConfig(config.getConfig("simulation.housing-market"));
        governmentConfig = new GovernmentConfig(config.getConfig("simulation.governmentConfig"));
    }

    /** Housing Market Configuration
     *
     * @return a `HousingMarketConfig` object encapsulating the housing market parameters.
     */
    public HousingMarketConfig getHousingMarketConfig() {
        return housingMarketConfig;
    }

    public GovernmentConfig getGovernmentConfig() {
        return governmentConfig;
    }

}
