package markets;

import housing.Household;
import markets.HouseBuyerRecord;

public class BTLBuyerRecord extends HouseBuyerRecord {
	private static final long serialVersionUID = 5314886568148212605L;

	public BTLBuyerRecord(Household buyer, double maxPrice) {
		super(buyer, maxPrice);
	}
	
}
