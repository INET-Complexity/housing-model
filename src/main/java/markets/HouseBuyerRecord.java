package markets;

import housing.Household;

import java.util.Comparator;

/**********************************************
 * This class represents information regarding a household that has
 * entered the housing market. Think of it as the file that an
 * estate agent has on a customer who wants to buy.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseBuyerRecord extends HousingMarketRecord {
	private static final long serialVersionUID = -4092951887680947486L;

	public HouseBuyerRecord(Household h, double price) {
		super(price);
		buyer = h;
	}
	
	public static class PComparator implements Comparator<HouseBuyerRecord> {
		@Override
		public int compare(HouseBuyerRecord arg0, HouseBuyerRecord arg1) {
			double diff = arg0.getPrice() - arg1.getPrice();
			if(diff == 0.0) {
				diff = arg0.getId() - arg1.getId();
			}
			return (int)Math.signum(diff);
		}
	}
	
	

	/////////////////////////////////////////////////////////////////
	
	public Household buyer; // Who wants to buy the house

	@Override
	public int getQuality() {
		// TODO Auto-generated method stub
		return 0;
	}
}
