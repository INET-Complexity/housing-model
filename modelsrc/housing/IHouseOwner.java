package housing;

/***************************************************
 * Interface for agents entering a housing market.
 * 
 * @author daniel
 *
 **************************************************/
public interface IHouseOwner {
	/** Called when the agent buys a house **/
	public void completeHousePurchase(HouseSaleRecord sale);
	
	/** Called when an agent sells a house **/
	public void completeHouseSale(HouseSaleRecord sale);
	
	/** Called when a tenant moves out of a house that the agent owns **/
	public void endOfLettingAgreement(House h);

	public void completeHouseLet(House house);
	
}
