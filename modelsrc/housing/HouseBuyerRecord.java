package housing;

/**********************************************
 * This class represents information regarding a household that has
 * entered the housing market. Think of it as the file that an
 * estate agent has on a customer who wants to buy.
 * 
 * @author daniel
 *
 **********************************************/
public class HouseBuyerRecord extends HousingMarketRecord {
	
	public HouseBuyerRecord(Household h, double p) {
		buyer = h;
		price = p;
	}
	
	@Override
	public int getId() {
		return buyer.id;
	}

	/////////////////////////////////////////////////////////////////
	
	public Household buyer; // Who wants to buy the house
	public double    price; // how much he is willing to pay
}
