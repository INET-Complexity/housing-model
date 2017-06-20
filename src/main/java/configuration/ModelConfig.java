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
    private CentralBankConfig centralBankConfig;

    public ModelConfig(String filename) {
        Config config = ConfigFactory.load(filename);
        housingMarketConfig = new HousingMarketConfig(config.getConfig("simulation.housing-market"));
        governmentConfig = new GovernmentConfig(config.getConfig("simulation.government"));
        centralBankConfig = new CentralBankConfig(config.getConfig("simulation.central-bank"));
    }

    /** Housing Market Configuration
     *
     * @return a `HousingMarketConfig` object encapsulating the housing market parameters.
     */
    public HousingMarketConfig getHousingMarketConfig() {
        return housingMarketConfig;
    }

    /** Government Configuration
     *
     * @return a `GovernmentConfig` object encapsulating the government parameters.
     */
    public GovernmentConfig getGovernmentConfig() {
        return governmentConfig;
    }

    /** Central Bank Configuration
     *
     * @return a `CentralBankConfig` object encapsulating the central bank parameters.
     */
    public CentralBankConfig getCentralBankConfig() {
        return centralBankConfig;
    }

}
