package housing;

import markets.HouseSaleRecord;

/***************************************************
 * Common interface for agents who enter the sale
 * market as sellers and, thus, house owners. This
 * is thought to encompass households and the
 * construction sector under a common framework,
 * such that they can both participate in the sale
 * market
 * 
 * @author daniel
 *
 **************************************************/
public interface IHouseOwner {
	/** Called when an agent sells a house **/
	void completeHouseSale(HouseSaleRecord saleRecord);
	
	/** Called when a tenant moves out of a house that the agent owns **/
	void endOfLettingAgreement(House house, PaymentAgreement contract);

    /** Called when an agent lets a house **/
	void completeHouseLet(HouseSaleRecord saleRecord);
	
}
