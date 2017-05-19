package housing;

import java.io.Serializable;

public class CentralBank implements Serializable {
	private static final long serialVersionUID = -2857716547766065142L;

	private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public CentralBank() {
		// Setup initial values
		firstTimeBuyerLTVLimit = config.CENTRAL_BANK_MAX_FTB_LTV;
		ownerOccupierLTVLimit= config.CENTRAL_BANK_MAX_OO_LTV;
		buyToLetLTVLimit = config.CENTRAL_BANK_MAX_BTL_LTV;
		
		firstTimeBuyerLTILimit = config.CENTRAL_BANK_MAX_FTB_LTI;
		ownerOccupierLTILimit = config.CENTRAL_BANK_MAX_OO_LTI;

        proportionOverLTVLimit = config.CENTRAL_BANK_FRACTION_OVER_MAX_LTV;
        proportionOverLTILimit = config.CENTRAL_BANK_FRACTION_OVER_MAX_LTI;

		interestCoverRatioLimit = config.CENTRAL_BANK_MAX_ICR;
	}
	
	/***
	 * This method implements the policy strategy of the Central Bank.
	 * @param coreIndicators The current value of the core indicators
	 */
	public void step(CoreIndicators coreIndicators) {
		/** Use this method to express the policy strategy of the central bank by
		 * setting the value of the various limits in response to the current
		 * value of the core indicators.
		 *
		 * Example policy: if house price growth is greater than 0.001 then FTB LTV limit is 0.75
		 *                  otherwise (if house price growth is less than or equal to  0.001)
		 *					FTB LTV limit is 0.95
		 *
		 * Example code:
		 *
		 *		if(coreIndicators.getHousePriceGrowth() > 0.001) {
		 *			firstTimeBuyerLTVLimit = 0.75;
		 *		} else {
		 *			firstTimeBuyerLTVLimit = 0.95;
		 *		}
		 */

		// Include the policy strategy code here:


	}
	
	public double loanToIncomeRegulation(boolean firstTimeBuyer) {
		if(firstTimeBuyer) {
			return(firstTimeBuyerLTILimit);
		}
		return(ownerOccupierLTILimit);
	}

	public double loanToValueRegulation(boolean firstTimeBuyer, boolean isHome) {
		if(isHome) {
			if(firstTimeBuyer) {
				return(firstTimeBuyerLTVLimit);
			}
			return(ownerOccupierLTVLimit);
		}
		return(buyToLetLTVLimit);
	}
	
	public double interestCoverageRatioRegulation() {
		return(interestCoverRatioLimit);
	}

	public double ownerOccupierLTILimit;	// LTI upper limit for owner-occupiers
	public double ownerOccupierLTVLimit;	// LTV upper limit for owner-occupiers
	public double buyToLetLTILimit;			// LTI upper limit for Buy-to-let investors
	public double buyToLetLTVLimit;			// LTV upper limit for Buy-to-let investors
	public double firstTimeBuyerLTILimit;	// LTI upper limit for first-time buyers
	public double firstTimeBuyerLTVLimit;	// LTV upper limit for first-time buyers
	public double proportionOverLTILimit;	// proportion of mortgages that are allowed to be above the respective LTI limit
	public double proportionOverLTVLimit;	// proportion of mortgages that are allowed to be above the respective LTV limit
	public double interestCoverRatioLimit;
}
