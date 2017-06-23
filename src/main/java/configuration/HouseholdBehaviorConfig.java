package configuration;


import com.typesafe.config.Config;


public class HouseholdBehaviorConfig {

    private BuyToLetConfig buyToLetConfig;
    private DesiredBankBalanceConfig desiredBankBalanceConfig;
    private ConsumptionDecisionRuleConfig consumptionDecisionRuleConfig;

    HouseholdBehaviorConfig(Config config) {
        buyToLetConfig = new BuyToLetConfig(config.getConfig("buy-to-let"));
        desiredBankBalanceConfig = new DesiredBankBalanceConfig(config.getConfig("desired-bank-balance"));
        consumptionDecisionRuleConfig = new ConsumptionDecisionRuleConfig(config.getConfig("consumption-decision-rule"));
    }

    public BuyToLetConfig getBuyToLetConfig() {
        return buyToLetConfig;
    }

    public DesiredBankBalanceConfig getDesiredBankBalanceConfig() { return desiredBankBalanceConfig; }

    public ConsumptionDecisionRuleConfig getConsumptionDecisionRuleConfig() { return consumptionDecisionRuleConfig; }

    /** Class used to configure a household's desired bank balances decision rule.
     *
     * @author davidrpugh
     */
    public static class DesiredBankBalanceConfig {

        private double alpha;
        private double beta;
        private double epsilon;

        DesiredBankBalanceConfig (Config config) {
            alpha = config.getDouble("alpha");
            beta = config.getDouble("beta");
            epsilon = config.getDouble("epsilon");
        }

        public double getAlpha() { return alpha; }

        public double getBeta() { return beta; }

        public double getEpsilon() { return epsilon; }

    }


    public static class ConsumptionDecisionRuleConfig {

        private double consumptionFraction;
        private double essentialConsumptionFraction;

        ConsumptionDecisionRuleConfig(Config config) {
            consumptionFraction = config.getDouble("consumption-fraction");
            essentialConsumptionFraction = config.getDouble("essential-consumption-fraction");
        }

        public double getConsumptionFraction() { return consumptionFraction; }

        public double getEssentialConsumptionFraction() { return essentialConsumptionFraction; }

    }

}
