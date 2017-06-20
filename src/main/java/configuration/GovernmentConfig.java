package configuration;


import com.typesafe.config.Config;


public class GovernmentConfig {

    private double personalAllowanceLimit;
    private double incomeSupport;

    GovernmentConfig (Config config) {
        personalAllowanceLimit = config.getDouble("personal-allowance-limit");
        incomeSupport = config.getDouble("income-support");
    }

    public double getPersonalAllowanceLimit() {
        return personalAllowanceLimit;
    }

    public double getIncomeSupport() {
        return incomeSupport;
    }

}
