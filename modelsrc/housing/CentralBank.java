package housing;

public class CentralBank {
	
	public CentralBank() {
		// Setup initial values
		firstTimeBuyerLTVLimit = 0.9;
		ownerOccupierLTVLimit= 0.8;
		buyToLetLTVLimit = 0.6;
		
		firstTimeBuyerLTILimit = 6.5;
		ownerOccupierLTILimit = 6.5;
		buyToLetLTILimit = 1000.0; // unregulated

		proportionOverLTILimit= 0.15;
		proportionOverLTVLimit= 0.0;		
	}
	
	
	public void step(CoreIndicators coreIndicators) {
		if(coreIndicators.getHouseholdCreditGrowth() > 1.5) {
			// do something e.g. firstTimeBuyerLTILimit = 4.5;
		}
		if(coreIndicators.getHouseholdCreditGrowth() > -0.9) {
			// do something else e.g. firstTimeBuyerLTILimit = 8.0;
		}
	}
	
	public double loanToIncomeRegulation(Household h, boolean isHome) {
		if(isHome) {
			if(h.isFirstTimeBuyer()) {
				return(firstTimeBuyerLTILimit);
			}
			return(ownerOccupierLTILimit);
		}
		return(buyToLetLTILimit);
	}

	public double loanToValueRegulation(Household h, boolean isHome) {
		if(isHome) {
			if(h.isFirstTimeBuyer()) {
				return(firstTimeBuyerLTVLimit);
			}
			return(ownerOccupierLTVLimit);
		}
		return(buyToLetLTVLimit);
	}

	public double ownerOccupierLTILimit;	// LTI upper limit for owner-occupiers
	public double ownerOccupierLTVLimit;	// LTV upper limit for owner-occupiers
	public double buyToLetLTILimit;			// LTI upper limit for Buy-to-let investors
	public double buyToLetLTVLimit;			// LTV upper limit for Buy-to-let investors
	public double firstTimeBuyerLTILimit;	// LTI upper limit for first-time buyers
	public double firstTimeBuyerLTVLimit;	// LTV upper limit for first-time buyers
	public double proportionOverLTILimit;	// proportion of mortgages that are allowed to be above the respective LTI limit
	public double proportionOverLTVLimit;	// proportion of mortgages that are allowed to be above the respective LTV limit
}
