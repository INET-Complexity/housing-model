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
	double buyToLetRent(double pbar, double d, double mortgagePayment);

	/***
	 * Decide how to reduce offered monthly rent when a house is
	 * on the rental market and does not get matched to a tennant.
	 * (The figures used here are copied from the behaviour on the
	 * house-sale market, calibrated against sales on the zoopla dataset)
	 */
	double rethinkBuyToLetRent(HouseSaleRecord sale);

	/**
	 * @param h The house in question
	 * @param me The investor
	 * @return Does an investor decide to sell a buy-to-let property
	 */
	boolean decideToSellInvestmentProperty(House h, Household me);
	
	/***
	 * Decide whether to buy a house as a buy-to-let investment.
	 * Returns the price to bid
	 ***/
	boolean decideToBuyBuyToLet(Household me);
	
	/** @return amount to bid on the sale market for a new investment property */
	double btlPurchaseBid(Household me);
	boolean isPropertyInvestor();
	int nDesiredBTLProperties();

}