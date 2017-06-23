package configuration;


import com.typesafe.config.Config;


public class BankConfig {

    private int mortgageDurationYears;
    private double initialBaseRate;
    private double creditSupplyTarget;

    BankConfig(Config config) {
        mortgageDurationYears = config.getInt("mortgage-duration-years");
        initialBaseRate = config.getDouble("initial=base-rate");
        creditSupplyTarget = config.getDouble("credit-supply-target");
    }

    /** Mortgage duration in years. */
    public int getMortgageDurationYears() {
        return mortgageDurationYears;
    }

    /** Bank initial base-rate, which remains currently unchanged. */
    public double getInitialBaseRate() {
        return initialBaseRate;
    }

    /** Bank's target supply of credit per household per month. */
    public double getCreditSupplyTarget() {
        return creditSupplyTarget;
    }

}
