package housing;

/***************************************************
 * Interface for agents entering a housing market.
 * 
 * @author daniel
 *
 **************************************************/
public interface IHouseOwner {
	/** Called when the agent buys a house **/
	public void completeHousePurchase(HouseSaleRecord saleRecord);
	
	/** Called when an agent sells a house **/
	public void completeHouseSale(HouseSaleRecord saleRecord);
	
	/** Called when a tenant moves out of a house that the agent owns **/
	public void endOfLettingAgreement(House house, PaymentAgreement contract);

	public void completeHouseLet(HouseSaleRecord saleRecord);
	
}
