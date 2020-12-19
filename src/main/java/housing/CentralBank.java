package housing;

/**************************************************************************************************
 * Class to represent the mortgage policy regulator or Central Bank. It reads a number of policy
 * thresholds from the config object into local variables with the purpose of allowing for dynamic
 * policies varying those thresholds over time.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/

public class CentralBank {

    //------------------//
    //----- Fields -----//
    //------------------//

    // General fields
    private Config      config = Model.config;      // Passes the Model's configuration parameters object to a private field

    // Monetary policy
    private double      baseRate;

    // LTV internal policy thresholds
    private double      firstTimeBuyerHardMaxLTV;   // Loan-To-Value hard maximum for first-time buyer mortgages
    private double      homeMoverHardMaxLTV;        // Loan-To-Value hard maximum for home mover mortgages
    private double      buyToLetHardMaxLTV;         // Loan-To-Value hard maximum for buy-to-let mortgages

    // LTI policy thresholds
    private double      firstTimeBuyerSoftMaxLTI;               // Loan-To-Income soft maximum for first-time buyer mortgages
    private double      firstTimeBuyerMaxFracOverSoftMaxLTI;    // Maximum fraction of first-time buyer mortgages allowed to exceed their LTI soft maximum
    private double      homeMoverSoftMaxLTI;                    // Loan-To-Income soft maximum for home mover mortgages
    private double      homeMoverMaxFracOverSoftMaxLTI;         // Maximum fraction of home mover mortgages allowed to exceed their LTI soft maximum
    private int         monthsToCheckLTI;                       // Months to check for moving average of fraction of mortgages over their LTI soft limit

    // Affordability policy thresholds
    private double      hardMaxAffordability;       // Affordability hard maximum (monthly mortgage payment / household's monthly net employment income)

    // ICR policy thresholds
    private double      hardMinICR;                 // ICR hard minimum for the ratio of expected rental yield over interest monthly payment

    //-------------------//
    //----- Methods -----//
    //-------------------//

    void init() {
        // Set initial monetary policy
        baseRate = config.CENTRAL_BANK_INITIAL_BASE_RATE;
        // Set initial LTV mandatory policy thresholds
        firstTimeBuyerHardMaxLTV = config.CENTRAL_BANK_LTV_HARD_MAX_FTB;
        homeMoverHardMaxLTV = config.CENTRAL_BANK_LTV_HARD_MAX_HM;
        buyToLetHardMaxLTV = config.CENTRAL_BANK_LTV_HARD_MAX_BTL;
        // Set initial LTI mandatory policy thresholds
        firstTimeBuyerSoftMaxLTI = config.CENTRAL_BANK_LTI_SOFT_MAX_FTB;
        firstTimeBuyerMaxFracOverSoftMaxLTI = config.CENTRAL_BANK_LTI_MAX_FRAC_OVER_SOFT_MAX_FTB;
        homeMoverSoftMaxLTI = config.CENTRAL_BANK_LTI_SOFT_MAX_HM;
        homeMoverMaxFracOverSoftMaxLTI = config.CENTRAL_BANK_LTI_MAX_FRAC_OVER_SOFT_MAX_HM;
        monthsToCheckLTI = config.CENTRAL_BANK_LTI_MONTHS_TO_CHECK;
        // Set initial affordability mandatory policy thresholds
        hardMaxAffordability = config.CENTRAL_BANK_AFFORDABILITY_HARD_MAX;
        // Set initial ICR mandatory policy thresholds
        hardMinICR = config.CENTRAL_BANK_ICR_HARD_MIN;
    }

    /**
     * This method implements the policy strategy of the Central Bank
     *
     * @param coreIndicators The current value of the core indicators
     */
    public void step(collectors.CoreIndicators coreIndicators) {
        /* Use this method to express the policy strategy of the central bank by setting the value of the various limits
         in response to the current value of the core indicators.

         Example policy: if house price growth is greater than 0.001 then FTB LTV limit is 0.75 otherwise (if house
         price growth is less than or equal to  0.001) FTB LTV limit is 0.95
         Example code:
            if(coreIndicators.getHousePriceGrowth() > 0.001) {
                firstTimeBuyerLTVLimit = 0.75;
            } else {
                firstTimeBuyerLTVLimit = 0.95;
            }
         */
    }


    //----- Getter/setter methods -----//

    double getFirstTimeBuyerHardMaxLTV() { return firstTimeBuyerHardMaxLTV; }

    double getHomeMoverHardMaxLTV() { return homeMoverHardMaxLTV; }

    double getBuyToLetHardMaxLTV() { return buyToLetHardMaxLTV; }

    double getFirstTimeBuyerSoftMaxLTI() { return firstTimeBuyerSoftMaxLTI; }

    double getHomeMoverSoftMaxLTI() { return homeMoverSoftMaxLTI; }

    /**
     * Get the maximum fraction of mortgages to first-time buyers that can go over their Loan-To-Income soft limit.
     */
    double getFirstTimeBuyerMaxFracOverSoftMaxLTI() { return firstTimeBuyerMaxFracOverSoftMaxLTI; }

    /**
     * Get the maximum fraction of mortgages to home movers that can go over their Loan-To-Income soft limit.
     */
    double getHomeMoverMaxFracOverSoftMaxLTI() { return homeMoverMaxFracOverSoftMaxLTI; }

    int getMonthsToCheckLTI() { return monthsToCheckLTI; }

    double getHardMaxAffordability() { return hardMaxAffordability; }

    double getHardMinICR() { return hardMinICR; }

    double getBaseRate() { return baseRate; }

}
