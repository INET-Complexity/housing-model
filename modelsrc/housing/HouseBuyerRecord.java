package housing;

/**********************************************
 * This class represents information regarding a household that has
 * entered the housing market. Think of it as the file that an
 * estate agent has on a customer who wants to buy.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseBuyerRecord implements Comparable<HouseBuyerRecord> {
	
	public HouseBuyerRecord(Household h, double p) {
		buyer = h;
		price = p;
	}
	
	@Override
	public int compareTo(HouseBuyerRecord other) {
		double diff = other.price - price;
		if(diff == 0.0) {
			diff = other.buyer.id - buyer.id;
		}
		return((int)Math.signum(diff));
	}

	/////////////////////////////////////////////////////////////////
	
	public Household buyer; // Who wants to buy the house
	public double    price; // how much he is willing to pay
}
