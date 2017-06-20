package housing;

import configuration.CentralBankConfig;

import java.io.Serializable;

public class CentralBank implements Serializable {
    
    private static final long serialVersionUID = -2857716547766065142L;
    private CentralBankConfig config;

    public CentralBank(CentralBankConfig config) {
        this.config = config;
    }
    
    /***
     * This method implements the policy strategy of the Central Bank.
     * @param coreIndicators The current value of the core indicators
     */
    public void step(collectors.CoreIndicators coreIndicators) {
        /** Use this method to express the policy strategy of the central bank by
         * setting the value of the various limits in response to the current
         * value of the core indicators.
         *
         * Example policy: if house price growth is greater than 0.001 then FTB LTV limit is 0.75
         *                  otherwise (if house price growth is less than or equal to  0.001)
         *                    FTB LTV limit is 0.95
         *
         * Example code:
         *
         *        if(coreIndicators.getHousePriceGrowth() > 0.001) {
         *            firstTimeBuyerLTVLimit = 0.75;
         *        } else {
         *            firstTimeBuyerLTVLimit = 0.95;
         *        }
         */

        // Include the policy strategy code here:


    }
    
    public double loanToIncomeRegulation(boolean firstTimeBuyer) {
        if(firstTimeBuyer) {
            return config.getMaxFirstTimeBuyersLTI();
        } else {
            return config.getMaxOwnerOccupiersLTI();
        }
    }

    public double loanToValueRegulation(boolean firstTimeBuyer, boolean isHome) {
        if(isHome) {
            if (firstTimeBuyer) {
                return config.getMaxFirstTimeBuyerLTV();
            } else {
                return config.getMaxOwnerOccupiersLTV();
            }
        } else {
            return config.getMaxBuyToLetLTV();
        }
    }
    
    public double interestCoverageRatioRegulation() {
        return config.getMaxInterestCoverageRatio();
    }

}
