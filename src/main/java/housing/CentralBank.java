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
    private Config      config = Model.config;	// Passes the Model's configuration parameters object to a private field

    // Monetary policy
    private double      baseRate;

    // LTI policy thresholds
    private double      firstTimeBuyerLTILimit; // Loan-To-Income limit for first-time buyer mortgages
    private double      homeMoverLTILimit; // Loan-To-Income upper limit for home mover mortgages
    private double      maxFractionOverLTILimit; // Fraction of owner-occupier mortgages allowed to exceed their Loan-To-Income limit (whether the FTB or the HM one)

    // ICR policy thresholds
    private double      interestCoverRatioLimit; // Ratio of expected rental yield over interest monthly payment under stressed interest conditions

    //-------------------//
    //----- Methods -----//
    //-------------------//

    void init() {
        // Set initial monetary policy
        baseRate = config.CENTRAL_BANK_INITIAL_BASE_RATE;
        // Set initial LTI policy thresholds
        firstTimeBuyerLTILimit = config.CENTRAL_BANK_MAX_FTB_LTI;
        homeMoverLTILimit = config.CENTRAL_BANK_MAX_OO_LTI;
        maxFractionOverLTILimit = config.CENTRAL_BANK_FRACTION_OVER_MAX_LTI;
        // Set initial ICR policy thresholds
        interestCoverRatioLimit = config.CENTRAL_BANK_MAX_ICR;
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

    /**
     * Get the Loan-To-Income ratio limit applicable to a given household. Note that Loan-To-Income constraints apply
     * only to non-BTL applicants. The private bank always imposes its own limit. Apart from this, it also imposes the
     * Central Bank regulated limit, which allows for a certain fraction of residential loans (mortgages for
     * owner-occupying) to go over it
     *
     * @param isFirstTimeBuyer True if the household is first-time buyer
     * @return Loan-To-Income ratio limit applicable to the household
     */
    double getLoanToIncomeLimit(boolean isFirstTimeBuyer) {
        if (isFirstTimeBuyer) {
            return firstTimeBuyerLTILimit;
        } else {
            return homeMoverLTILimit;
        }
    }

    /**
     * Get the maximum fraction of mortgages to owner-occupying households that can go over their Loan-To-Income limit,
     * whether they are subject to the first-time buyer or the home mover specific limit
     */
    double getMaxFractionOverLTILimit() { return maxFractionOverLTILimit; }

    double getInterestCoverRatioLimit() { return interestCoverRatioLimit; }

    double getBaseRate() { return baseRate; }

}
