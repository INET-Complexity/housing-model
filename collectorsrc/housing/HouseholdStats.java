package housing;

public class HouseholdStats {

	public void step() {
		totalDisposableIncome = 0.0;
		for(Household h : Model.households) {
			totalDisposableIncome += h.getMonthlyDisposableIncome();
		}

		nRenting = 0;
    	nHomeless = 0;
    	nHouseholds = Model.households.size();
    	for(Household h : Model.households) {
    		if(h.isHomeless()) {
    			++nHomeless;
    		} else if(h.isRenting()) {
    			++nRenting;
    		}
    	}
    	nNonOwner = nHomeless + nRenting;
    	nEmpty = Model.construction.housingStock + nHomeless - nHouseholds;

	}

    public double 		  nRenting;
    public double 		  nHomeless;
    public double 		  nNonOwner;
    public double		  nHouseholds;
    public double		  nEmpty;
	public double 		  totalDisposableIncome;
}
