package housing;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import ec.util.MersenneTwisterFast;

public class HouseholdBehaviour implements IHouseholdBehaviour {
	public static double RENT_PROFIT_MARGIN = 0.05; // profit margin for buy-to-let investors
	public double DOWNPAYMENT_FRACTION = 0.1 + 0.0025*Model.rand.nextGaussian(); // Fraction of bank-balance household would like to spend on mortgage downpayments

	public double P_SELL = 1.0/(7.0*12.0);  // monthly probability of selling home
	public double P_INVESTOR = 0.08; 		// Probability of being (wanting to be) a property investor
	protected MersenneTwisterFast 	rand = Model.rand;
	LogNormalDistribution buyToLetDistribution  = new LogNormalDistribution(Math.log(3.44), 1.050); // No. of houses owned by buy-to-let investors Source: ARLA review and index Q2 2014

	public int			desiredBTLProperties;	// number of properties

	
	public HouseholdBehaviour() {
		if(rand.nextDouble() < P_INVESTOR) {
			desiredBTLProperties = (int)buyToLetDistribution.inverseCumulativeProbability(rand.nextDouble());
		} else {
			desiredBTLProperties = 0;
		}
	}

	/********************************
	 * How much a household consumes
	 * Consumption rule made to fit ONS wealth in Great Britain data.
	 * @author daniel
	 *
	 ********************************/
	@Override
	public double desiredConsumptionB(double monthlyIncome, double bankBalance) {
		return(0.1*Math.max((bankBalance - Math.exp(4.07*Math.log(monthlyIncome*12.0)-33.1 + 0.2*Model.rand.nextGaussian())),0.0));
	}

	/**
	 * Decide on desired purchase price as a function of monthly income and current
	 *  of house price appreciation.
	 */
	@Override
	public double desiredPurchasePrice(double monthlyIncome, double hpa) {
		final double A = 0.0;//0.4;//0.48;			// sensitivity to house price appreciation
		final double EPSILON = 0.40;//0.36;//0.48;//0.365; // S.D. of noise
		final double SIGMA = 5.4*12.0;//5.6;	// scale
		return(SIGMA*monthlyIncome*Math.exp(EPSILON*Model.rand.nextGaussian())/(1.0 - A*hpa));
	}

	/**
	 * @param pbar average sale price of houses of the same quality
	 * @param d average number of days on the market before sale
	 * @param principal amount of principal left on any mortgage on this house
	 * @return initial sale price of a house 
	 */
	@Override
	public double initialSalePrice(double pbar, double d, double principal) {
		final double C = 0.02;//0.095;	// initial markup from average price
		final double D = 0.0;//0.024;//0.01;//0.001;		// Size of Days-on-market effect
		final double E = 0.05; //0.05;	// SD of noise
		double exponent = C + Math.log(pbar) - D*Math.log((d + 1.0)/31.0) + E*Model.rand.nextGaussian();
		return(Math.max(Math.exp(exponent), principal));
	}
	

	/**
	 * @return Does an owner-occupier decide to sell house?
	 */
	@Override
	public boolean decideToSellHome() {
		if(rand.nextDouble() < P_SELL) return(true);
		return false;
	}

	@Override
	public double downPayment(double bankBalance) {
		return(bankBalance*DOWNPAYMENT_FRACTION);
	}

	
	/********************************************************
	 * Decide how much to drop the list-price of a house if
	 * it has been on the market for (another) month and hasn't
	 * sold. Calibrated against Zoopla dataset in Bank of England
	 * 
	 * @param sale The HouseSaleRecord of the house that is on the market.
	 ********************************************************/
	public double rethinkHouseSalePrice(HouseSaleRecord sale) {
		if(rand.nextDouble() > 0.944) {
			double logReduction = 1.603+(rand.nextGaussian()*0.6173);
			return(sale.currentPrice * (1.0-Math.exp(logReduction)));
		}
		return(sale.currentPrice);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Renter behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean renterPurchaseDecision(Household h, double housePrice, double annualRent) {
		final double COST_OF_RENTING = 600; // Annual psychological cost of renting
		final double FTB_K = 1.0/600.0;//1.0/100000.0;//0.005 // Heterogeneity of sensitivity of desire to first-time-buy to cost
		double costOfHouse;
//			costOfHouse = housePrice*((1.0-HousingMarketTest.bank.config.THETA_FTB)*HousingMarketTest.bank.mortgageInterestRate() - HousingMarketTest.housingMarket.housePriceAppreciation());
		costOfHouse = housePrice*(Model.bank.loanToValue(h,true)*Model.bank.getMortgageInterestRate() - Model.housingMarket.housePriceAppreciation());
		return(Model.rand.nextDouble() < 1.0/(1.0 + Math.exp(-FTB_K*(annualRent + COST_OF_RENTING - costOfHouse))));
	}

	/********************************************************
	 * Decide how much to bid on the rental market
	 ********************************************************/
	public double desiredRent(double monthlyIncome) {
		return(0.33*monthlyIncome);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Property investor behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param h The house in question
	 * @param me The investor
	 * @return Does an investor decide to sell a buy-to-let property
	 */
	@Override
	public boolean decideToSellInvestmentProperty(House h, Household me) {
//		System.out.println(me.desiredPropertyInvestmentFraction + " " + me.getDesiredPropertyInvestmentValue() + " -> "+me.getPropertyInvestmentValuation());
//		if(me.getDesiredPropertyInvestmentValue() < 
//				(me.getPropertyInvestmentValuation() - me.houseMarket.getAverageSalePrice(h.quality))) {
//			return(true);
//		}
		if(me.housePayments.size()+1 > desiredBTLProperties) {
			// only count properties on market if necessary as it's a slow operation
			if(me.housePayments.size()+1 > desiredBTLProperties - me.nPropertiesForSale()) {
				return(true);
			}
		}
		return(rand.nextDouble() < P_SELL);
	}
	

	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? 
	 */
	@Override
	public double buyToLetRent(double mortgagePayment) {
		return(mortgagePayment*(1.0+RENT_PROFIT_MARGIN));
	}

	/********************************************************
	 * Decide whether to buy a house as a buy-to-let investment
	 ********************************************************/
	public boolean decideToBuyBuyToLet(House h, Household me, double price) {
		if(price <= Model.bank.getMaxMortgage(me, false)) {
			MortgageApproval mortgage;
			mortgage = Model.bank.requestApproval(me, price, 0.0, false); // maximise leverege with min downpayment
			return(buyToLetPurchaseDecision(price, mortgage.monthlyPayment, mortgage.downPayment));

		}
//		System.out.println("BTL refused mortgage on "+price+" can get "+Model.bank.getMaxMortgage(me, false));
		return(false);
	}
	/**
	 * @param price The asking price of the house
	 * @param monthlyPayment The monthly payment on a mortgage for this house
	 * @param downPayment The minimum downpayment on a mortgage for this house
	 * @return will the investor decide to buy this house?
	 */
	public boolean buyToLetPurchaseDecision(double price, double monthlyPayment, double downPayment) {
		double yield;
		yield = (monthlyPayment*12*RENT_PROFIT_MARGIN + Model.housingMarket.housePriceAppreciation()*price)/
				downPayment;
		if(Model.rand.nextDouble() < 1.0/(1.0 + Math.exp(4.4 - yield*16.0))) {
			return(true);
		}
		return(false);
	}

	public boolean isPropertyInvestor() {
		return(desiredBTLProperties > 0);
	}


}
