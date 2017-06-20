package configuration;


import com.typesafe.config.Config;


/**
 *
 * @author davidrpugh
 */
public class CentralBankConfig {

    private double maxFirstTimeBuyerLTV;
    private double maxOwnerOccupiersLTV;
    private double maxBuyToLetLTV;
    private double fractionOverMaxLTV;
    private double maxFirstTimeBuyersLTI;
    private double maxOwnerOccupiersLTI;
    private double fractionOverMaxLTI;
    private double affordabilityCoefficient;
    private double buyToLetStressedInterest;
    private double maxInterestCoverageRatio;

    CentralBankConfig(Config config) {
        maxFirstTimeBuyerLTV = config.getDouble("max-first-time-buyers-ltv");
        maxOwnerOccupiersLTV = config.getDouble("max-owner-occupiers-ltv");
        maxBuyToLetLTV = config.getDouble("max-buy-to-let-ltv");
        fractionOverMaxLTV = config.getDouble("fraction-over-max-ltv");
        maxFirstTimeBuyersLTI = config.getDouble("max-first-time-buyers-lti");
        maxOwnerOccupiersLTI = config.getDouble("max-owner-occupiers-lti");
        fractionOverMaxLTI = config.getDouble("fraction=over-max-lti");
        affordabilityCoefficient = config.getDouble("affordability-coefficient");
        buyToLetStressedInterest = config.getDouble("buy-to-let-stressed-interest-ratio");
        maxInterestCoverageRatio = config.getDouble("max-interest-coverage-ratio");
    }

    /** Maximum LTV ratio that the bank would allow for first-time-buyers when not regulated.
     *
     * @return
     */
    public double getMaxFirstTimeBuyerLTV() {
        return maxFirstTimeBuyerLTV;
    }

    /** Maximum LTV ratio that the bank would allow for owner-occupiers when not regulated.
     *
     * @return
     */
    public double getMaxOwnerOccupiersLTV() {
        return maxOwnerOccupiersLTV;
    }

    /** Maximum LTV ratio that the bank would allow for BTL investors when not regulated.
     *
     * @return
     */
    public double getMaxBuyToLetLTV() {
        return maxBuyToLetLTV;
    }

    /** Maximum fraction of mortgages that the bank can give over the LTV ratio limit.
     *
     * @return
     */
    public double getFractionOverMaxLTV() {
        return fractionOverMaxLTV;
    }

    /** Maximum LTI ratio that the bank would allow for first-time-buyers when not regulated.
     *
     * @return
     */
    public double getMaxFirstTimeBuyersLTI() {
        return maxFirstTimeBuyersLTI;
    }

    /** Maximum LTI ratio that the bank would allow for owner-occupiers when not regulated.
     *
     * @return
     */
    public double getMaxOwnerOccupiersLTI() {
        return maxOwnerOccupiersLTI;
    }

    /** Maximum fraction of mortgages that the bank can give over the LTI ratio limit.
     *
     * @return
     */
    public double getFractionOverMaxLTI() {
        return fractionOverMaxLTI;
    }

    /** Maximum fraction of the household's income to be spent on mortgage repayments under stressed conditions.
     *
     * @return
     */
    public double getAffordabilityCoefficient() {
        return affordabilityCoefficient;
    }

    /** Interest rate under stressed condition for BTL investors when calculating interest coverage ratios, ICR.
     *
     * @return
     */
    public double getBuyToLetStressedInterest() {
        return buyToLetStressedInterest;
    }

    /** Interest coverage ratio (ICR) limit imposed by the central bank.
     *
     * @return
     */
    public double getMaxInterestCoverageRatio() {
        return maxInterestCoverageRatio;
    }

}
