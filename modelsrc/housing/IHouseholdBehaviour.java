package housing;

public interface IHouseholdBehaviour {

	/********************************
	 * How much a household consumes
	 * Consumption rule made to fit ONS wealth in Great Britain data.
	 ********************************/
	public abstract double desiredConsumptionB(double monthlyIncome,
			double bankBalance);

	/**
	 * Decide on desired purchase price as a function of monthly income and current
	 *  of house price appreciation.
	 */
	public abstract double desiredPurchasePrice(double monthlyIncome, double hpa);

	/**
	 * @param pbar average sale price of houses of the same quality
	 * @param d average number of days on the market before sale
	 * @param principal amount of principal left on any mortgage on this house
	 * @return initial sale price of a house 
	 */
	public abstract double initialSalePrice(double pbar, double d,
			double principal);

	/** @return Does an owner-occupier decide to sell house? */
	public abstract boolean decideToSellHome();

	public abstract double downPayment(double bankBalance);
	public abstract double rethinkHouseSalePrice(HouseSaleRecord forSale);

	///////////////////////////////////////////////////////////////////////////
	// Renter stuff
	///////////////////////////////////////////////////////////////////////////

	public abstract double desiredRent(double monthlyIncome);
	public abstract boolean renterPurchaseDecision(Household h,double housePrice, double annualRent);
	
	///////////////////////////////////////////////////////////////////////////
	// Buy to let stuff
	///////////////////////////////////////////////////////////////////////////
	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? 
	 */
	public abstract double buyToLetRent(double mortgagePayment);

	/**
	 * @param h The house in question
	 * @param me The investor
	 * @return Does an investor decide to sell a buy-to-let property
	 */
	public abstract boolean decideToSellInvestmentProperty(House h, Household me);

	public abstract boolean decideToBuyBuyToLet(House h, Household me, double price);
	public abstract boolean isPropertyInvestor();
}