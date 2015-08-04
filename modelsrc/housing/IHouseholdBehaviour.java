package housing;

public interface IHouseholdBehaviour {

	/********************************
	 * How much a household consumes
	 * Consumption rule made to fit ONS wealth in Great Britain data.
	 ********************************/
	double desiredConsumptionB(double monthlyIncome,
			double bankBalance);

	/**
	 * Decide on desired purchase price as a function of monthly income and current
	 *  of house price appreciation.
	 */
	double desiredPurchasePrice(double monthlyIncome, double hpa);

	/**
	 * @param pbar average sale price of houses of the same quality
	 * @param d average number of days on the market before sale
	 * @param principal amount of principal left on any mortgage on this house
	 * @return initial sale price of a house 
	 */
	double initialSalePrice(double pbar, double d, double principal);

	/** @return Does an owner-occupier decide to sell house? */
	boolean decideToSellHome();

	double downPayment(double bankBalance);
	double rethinkHouseSalePrice(HouseSaleRecord forSale);

	///////////////////////////////////////////////////////////////////////////
	// Renter stuff
	///////////////////////////////////////////////////////////////////////////

	double desiredRent(double monthlyIncome);
	boolean renterPurchaseDecision(Household h,double housePrice, double annualRent);
	
	///////////////////////////////////////////////////////////////////////////
	// Buy to let stuff
	///////////////////////////////////////////////////////////////////////////
	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? 
	 */
//	public abstract double buyToLetRent(double mortgagePayment);
	double buyToLetRent(double pbar, double d, double mortgagePayment);
	double rethinkBuyToLetRent(HouseSaleRecord sale);

	/**
	 * @param h The house in question
	 * @param me The investor
	 * @return Does an investor decide to sell a buy-to-let property
	 */
	boolean decideToSellInvestmentProperty(House h, Household me);
	boolean decideToBuyBuyToLet(House h, Household me, double price);
	boolean isPropertyInvestor();
	int nDesiredBTLProperties();

}