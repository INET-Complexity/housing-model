package housing;

import java.io.Serializable;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import ec.util.MersenneTwisterFast;

public class HouseholdBehaviour implements Serializable {// implements IHouseholdBehaviour {
	private static final long serialVersionUID = -7785886649432814279L;
	public static LogNormalDistribution FTB_DOWNPAYMENT = new LogNormalDistribution(null, 10.30, 0.9093);
	public static LogNormalDistribution OO_DOWNPAYMENT = new LogNormalDistribution(null, 11.155, 0.7538);
	
	public double DOWNPAYMENT_FRACTION = 0.75 + 0.0025*Model.rand.nextGaussian(); // Fraction of bank-balance household would like to spend on mortgage downpayments
	public double HPA_EXPECTATION_WEIGHT = 1.0; // expectation value for HPI(t+DT) = HPI(t) + WEIGHT*DT*dHPI/dt (John Muellbauer: less than 1)
	
//	public double BTL_LOSS_TOLERANCE = 0.2 + 0.1*Model.rand.nextGaussian(); // loss as proportion of rent at which 50% per month chance of selling a BtL house on the rental market 
//	public double BTL_YIELD_SENSITIVITY = 10.0 + 2.5*Model.rand.nextGaussian(); // sensitivity to yield when buying BtL property
//	public double BTL_CAPITAL_GAIN_SENSITIVITY = 0.9; // sensitivity to HPAexpectation in decision to buy or sell BtL property
	public double BTL_CAP_GAIN_COEFF = 1.0; // Sensitivity to capital gain, 0.0 only cares about yield, 1.0 cares equally about cap gain & yield
//	public double BTL_TRANSACTION_COST = 0.06; // cost of selling a BTL house as fraction of sale price
	
//	public double MATERIALISM = 10.0 + Model.rand.nextGaussian(); // sensitivity to property quality
	public double INTENSITY_OF_CHOICE = 10.0;
	protected MersenneTwisterFast 	rand = Model.rand;
	public boolean					BTLInvestor;
	public double 					propensityToSave;
	public double					desiredBalance;
	
	public HouseholdBehaviour(double incomePercentile) {
		propensityToSave = 0.1*Model.rand.nextGaussian();
		if(Household.BTL_ENABLED) {
			if( incomePercentile > 0.5 && rand.nextDouble() < data.Households.P_INVESTOR) {
				BTLInvestor = true;//(data.Households.buyToLetDistribution.inverseCumulativeProbability(rand.nextDouble())+0.5);
			} else {
				BTLInvestor = false;
			}
		} else {
			BTLInvestor = false;
		}
		desiredBalance = -1.0;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Owner-Ocupier behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////

	/********************************
	 * How much a household consumes
	 * Consumption rule made to fit ONS wealth in Great Britain data.
	 * @author daniel
	 *
	 ********************************/
	public double desiredConsumptionB(Household me) {//double monthlyIncome, double bankBalance) {
		return(0.5*Math.max(me.bankBalance - desiredBankBalance(me),0.0));
	}
	
	public double desiredBankBalance(Household me) {
		if(desiredBalance == -1.0) {
//			desiredBalance = 1.1*OO_DOWNPAYMENT.inverseCumulativeProbability(me.lifecycle.incomePercentile);
			desiredBalance = 3.0*Math.exp(4.07*Math.log(me.getMonthlyPreTaxIncome()*12.0)-33.1 - propensityToSave);
			if(me.lifecycle.incomePercentile < 0.3) desiredBalance = 1.0;
		}
//		if(me.isInSocialHousing() && !me.isFirstTimeBuyer()) return(me.monthlyEmploymentIncome); // retain equity from house sale
		return(desiredBalance);
//		return(Math.exp(4.07*Math.log(me.monthlyEmploymentIncome*12.0)-33.1 - propensityToSave));
	}

	/***************************
	 * After having decided to buy a house, 
	 * decide on desired purchase price as a function of monthly income and current
	 * value of house price appreciation.
	 ****************************/
	public double desiredPurchasePrice(Household me, double monthlyIncome) {
		final double A = 0.8;//0.48;			// sensitivity to house price appreciation
		final double EPSILON = 0.17;//3;//0.36;//0.48;//0.365; // S.D. of noise
		final double SIGMA = 4.0*12.0;//5.6;	// scale
		return(SIGMA*monthlyIncome*Math.exp(EPSILON*Model.rand.nextGaussian())/(1.0 - A*HPAExpectation()));
		
//		PurchasePlan plan = findBestPurchase(me);
//		double housePrice = Model.housingMarket.getAverageSalePrice(plan.quality);//behaviour.desiredPurchasePrice(getMonthlyPreTaxIncome(), houseMarket.housePriceAppreciation());
//		return(1.01*housePrice*Math.exp(0.05*Model.rand.nextGaussian()));
	}

	/********************************
	 * @param pbar average sale price of houses of the same quality
	 * @param d average number of days on the market before sale
	 * @param principal amount of principal left on any mortgage on this house
	 * @return initial sale price of a house 
	 ********************************/
	public double initialSalePrice(double pbar, double d, double principal) {
		final double C = 0.02;//0.095;	// initial markup from average price (more like 0.2 from BoE calibration)
		final double M = 6.0; // equilibrium months on market 
		final double D = C/Math.log(M);//0.024;//0.01;//0.001;		// Size of Days-on-market effect
		final double E = 0.05; //0.05;	// SD of noise
		double exponent = C + Math.log(pbar) - D*Math.log((d + 1.0)/31.0) + E*Model.rand.nextGaussian();
		return(Math.max(Math.exp(exponent), principal));
	}
	

	/**
	 * @return Does an owner-occupier decide to sell house?
	 */
	public boolean decideToSellHome(Household me) {
		// TODO: need to add expenditure

		return(rand.nextDouble() < data.Households.P_SELL *(1.0 + 4.0*(0.05 - Model.housingMarket.offersPQ.size()*1.0/Model.households.size())) + 5.0*(0.03-Model.bank.getMortgageInterestRate()));
		
		// reference 
		//int potentialQualityChange = Model.housingMarket.maxQualityGivenPrice(Model.bank.getMaxMortgage(me,true))- me.home.getQuality();
		//double p_move = data.Households.P_FORCEDTOMOVE + (data.Households.P_SELL-data.Households.P_FORCEDTOMOVE)/(1.0+Math.exp(5.0-2.0*potentialQualityChange));
		
		/*
		
		// calc purchase price
		PurchasePlan plan = findBestPurchase(me);
		if(plan.quality < 0) return(false); // can't afford new home anyway
		int currentQuality = me.home.getQuality();
		double currentUtility;// = utilityOfHome(me,me.home.getQuality()) - me.mortgageFor(me.home).nextPayment()/me.getMonthlyPreTaxIncome();
//		currentUtility = utilityOfHome(me,currentQuality) +(Model.housingMarket.getAverageSalePrice(currentQuality)*HPAExpectation()/12.0 - me.mortgageFor(me.home).nextPayment())/me.getMonthlyPreTaxIncome();
		double currentLeftForConsumption = 1.0 - (me.mortgageFor(me.home).nextPayment() - Model.housingMarket.getAverageSalePrice(currentQuality)*HPAExpectation()/12.0)/me.monthlyEmploymentIncome;
//		currentUtility = (currentQuality-me.desiredQuality)/House.Config.N_QUALITY + qualityOfLiving(currentLeftForConsumption);
		currentUtility = utilityOfHome(me, currentQuality) + qualityOfLiving(currentLeftForConsumption);
//	System.out.println("Move utility = "+(plan.utility- currentUtility));

		double p_move = data.Households.P_FORCEDTOMOVE;
		p_move += 2.0*(data.Households.P_SELL-data.Households.P_FORCEDTOMOVE)/(1.0+Math.exp(4.0-INTENSITY_OF_CHOICE*(plan.utility - currentUtility)));
		p_move *= 1.0 - data.HouseSaleMarket.SEASONAL_VOL_ADJ*Math.cos((2.0*3.141/12.0)*Model.getMonth());
	//	System.out.println("Move utility = "+INTENSITY_OF_CHOICE*(plan.utility- currentUtility)+"  "+p_move);
		return(rand.nextDouble() < p_move);
		*/
	}

	public double downPayment(Household me, double housePrice) {
//		return(me.bankBalance - (1.0 - DOWNPAYMENT_FRACTION)*desiredBankBalance(me));
		if(me.bankBalance > housePrice*1.25) {
			return(housePrice);
		}
		if(me.isFirstTimeBuyer()) {
			return(Model.housingMarket.housePriceIndex*FTB_DOWNPAYMENT.inverseCumulativeProbability(Math.max(0.0,(me.lifecycle.incomePercentile-0.3)/0.7)));
		} else if(isPropertyInvestor()) {
			return(housePrice*0.30*(1.0+0.2*rand.nextGaussian()));
		}
		return(Model.housingMarket.housePriceIndex*OO_DOWNPAYMENT.inverseCumulativeProbability(Math.max(0.0, (me.lifecycle.incomePercentile-0.3)/0.7)));		
//		return(Model.housingMarket.housePriceIndex*OO_DOWNPAYMENT.inverseCumulativeProbability(me.lifecycle.incomePercentile));	
	}

	
	/********************************************************
	 * Decide how much to drop the list-price of a house if
	 * it has been on the market for (another) month and hasn't
	 * sold. Calibrated against Zoopla dataset in Bank of England
	 * 
	 * @param sale The HouseSaleRecord of the house that is on the market.
	 ********************************************************/
	public double rethinkHouseSalePrice(HouseSaleRecord sale) {
//		return(sale.getPrice() *0.95);

		if(rand.nextDouble() < data.Households.P_SALEPRICEREDUCE) {
//			double logReduction = Math.min(-5.1e-3, data.Households.REDUCTION_MU+(rand.nextGaussian()*data.Households.REDUCTION_SIGMA));
//			return(sale.getPrice() * (1.0-Math.exp(logReduction)));
			return(sale.getPrice() * data.Households.REDUCTION_MU + rand.nextGaussian()*data.Households.REDUCTION_SIGMA);
		}
		return(sale.getPrice());
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Renter behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/*** renters or OO after selling home decide whether to rent or buy
	 * N.B. even though the HH may not decide to rent a house of the
	 * same quality as they would buy, the cash value of the difference in quality
	 *  is assumed to be the difference in rental price between the two qualities.
	 *  @return true if we should buy a house, false if we should rent
	 */
	public boolean rentOrPurchaseDecision(Household me, double maxMortgage) {
		if(isPropertyInvestor()) return(true);
//		final double SCALE = 1.0;//1.25
		final double COST_OF_RENTING = 1.5/12.0; // annual psychological cost of renting
		final double FTB_K = 1.0/3500.0;//1.0/100000.0;//0.005 // Heterogeneity of sensitivity of desire to first-time-buy to cost
//		double costOfHouse;
//		double costOfRent;

		double purchasePrice = Math.min(desiredPurchasePrice(me, me.monthlyEmploymentIncome), Model.bank.getMaxMortgage(me, true));
		MortgageAgreement mortgageApproval = Model.bank.requestApproval(me, purchasePrice, downPayment(me,purchasePrice), true);
		int newHouseQuality = Model.housingMarket.maxQualityGivenPrice(purchasePrice);
//		int rentalQuality = Model.rentalMarket.maxQualityGivenPrice(desiredRent(me, me.monthlyEmploymentIncome));
//		if(rentalQuality > newHouseQuality+House.Config.N_QUALITY/8) return(false); // better quality to rent
		if(newHouseQuality < 0) return(false); // can't afford a house anyway
		double costOfHouse = mortgageApproval.monthlyPayment*12 - purchasePrice*HPAExpectation();
		double costOfRent = Model.rentalMarket.getAverageSalePrice(newHouseQuality)*12;
//		System.out.println(FTB_K*(costOfRent + COST_OF_RENTING - costOfHouse));
		return(rand.nextDouble() < 1.0/(1.0 + Math.exp(-FTB_K*(costOfRent*(1.0+COST_OF_RENTING) - costOfHouse))));

		/*
		
		PurchasePlan purchase = findBestPurchase(me);
		if(purchase.quality == 0 && purchase.utility <-10.0) return(false); // can't afford to buy anyway
		int rentQuality = findBestRentalQuality(me);
		
		if(me.isFirstTimeBuyer()) {
			COST_OF_RENTING = 0.001;
		} else {
			COST_OF_RENTING = 0.01;
		}
		
		double pBuy = 1.0/(1.0 + Math.exp(-INTENSITY_OF_CHOICE*(COST_OF_RENTING + purchase.utility - utilityOfRenting(me, rentQuality))));
//		System.out.println(utilityOfRenting(me, rentQuality) + " : "+purchase.utility+" : "+INTENSITY_OF_CHOICE*(COST_OF_RENTING+purchase.utility-utilityOfRenting(me, rentQuality))+" ... "+pBuy);
		return(Model.rand.nextDouble() < pBuy);
				 */

	}

	/********************************************************
	 * Decide how much to bid on the rental market
	 * Source: Zoopla rental prices 2008-2009 (at Bank of England)
	 ********************************************************/
	public double desiredRent(Household me, double monthlyIncome) {
		return(monthlyIncome * 0.3);

//		int quality = findBestRentalQuality(me);
//		return(1.01*Model.rentalMarket.getAverageSalePrice(quality)*Math.exp(0.1*Model.rand.nextGaussian()));
		
		/*
		// Zoopla calibrated values
		double annualIncome = monthlyIncome*12.0; // TODO: this should be net annual income, not gross
		double rent;
		if(annualIncome < 12000.0) {
			rent = 386.0;
		} else {
			rent = 11.72*Math.pow(annualIncome, 0.372);
		}
		rent *= Math.exp(Model.rand.nextGaussian()*0.0826);
		return(rent);
		*/
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Property investor behaviour
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param h The house in question
	 * @param me The investor
	 * @return Does an investor decide to sell a buy-to-let property (per month)
	 */
	public boolean decideToSellInvestmentProperty(House h, Household me) {
		final double INTENSITY = 50.0; // intensity of choice on effective yield
		final double AGGREGATE_RATE = 1.0/12.0; // controls the average rate of sales
		// sell if not selling on rental market at interest coverage ratio of 1.0
		if(!h.isOnRentalMarket()) return(false);
		MortgageAgreement mortgage = me.mortgageFor(h);
		if(mortgage == null) {
			System.out.println("Strange: deciding to sell investment property that I don't own");
			return(false);
		}
		// TODO: add transaction costs to expected capital gain
//		double icr = (h.rentalRecord.getPrice()-mortgage.nextPayment())/h.rentalRecord.getPrice();
		double equity = Math.max(0.01, Model.housingMarket.getAverageSalePrice(h.getQuality()) - mortgage.principal);
		double capitalGainRate = HPAExpectation()*Model.housingMarket.getAverageSalePrice(h.getQuality())/equity;
		double rentalYield = h.rentalRecord.getPrice()*12.0/equity;
		double mortgageRate = mortgage.nextPayment()*12.0/equity;
		double effectiveYield = (rentalYield-0.04) + BTL_CAP_GAIN_COEFF*(capitalGainRate-0.04) - mortgageRate;
		double pKeep = Math.pow(1.0/(1.0 + Math.exp(-INTENSITY*effectiveYield)),AGGREGATE_RATE);
//		System.out.println("DontSell = "+(-INTENSITY*(effectiveYield-0.05))+" "+pKeep);
		return(Model.rand.nextDouble() < (1.0-pKeep));
	}
	

	/**
	 * How much rent does an investor decide to charge on a buy-to-let house? 
	 * @param pbar average rent for house of this quality
	 * @param d average days on market
	 */
	public double buyToLetRent(double pbar, double d, MortgageAgreement mortgagePayment, House h) {
		final double C = 0.015; // markup over market price when zero days on market
		final double M = 6.0; // equilibrium months on market 
		final double D = C/Math.log(M); // Size of Days-on-market effect
		final double E = 0.05; //0.05;	// SD of noise
		double exponent = C + Math.log(pbar) - D*Math.log((d + 1.0)/31.0) + E*Model.rand.nextGaussian();
		double result = Math.exp(exponent);
		double minAcceptable = Model.housingMarket.getAverageSalePrice(h.getQuality())*0.048/12.0;
		if(result < minAcceptable) result = minAcceptable;
		return(result);

	//	if(result < mortgagePayment.purchasePrice*0.050/12.0) result = mortgagePayment.purchasePrice*0.050/12.0; // TODO: TEST!!
//		return(mortgagePayment*(1.0+RENT_PROFIT_MARGIN));
//		return(Model.housingMarket.getAverageSalePrice(h.getQuality())*0.051/12.0);
	}

	public double rethinkBuyToLetRent(HouseSaleRecord sale) {
		return(0.99*sale.getPrice());
//		if(rand.nextDouble() > 0.944) {
//			double logReduction = Math.min(4.6, 1.603+(rand.nextGaussian()*0.6173));
//			return(sale.getPrice() * (1.0-0.01*Math.exp(logReduction)));
//		}
//		return(sale.getPrice());
	}

	/***
	 * Monthly opportunity of buying a new BTL property
	 * @param me
	 * @return true if decision to buy
	 */
	public boolean decideToBuyBuyToLet(Household me) {
		if(me.nInvestmentProperties() < 1) return(true);
		final double INTENSITY = 50.0;
		final double AGGREGATE_RATE = 1.0/12.0;
		
		if(!isPropertyInvestor()) return false;
		if(me.bankBalance < desiredBankBalance(me)*0.75) {
			return(false);
		}
		// --- calculate expected yield on zero quality house
		double maxPrice = Model.bank.getMaxMortgage(me, false);
		if(maxPrice < Model.housingMarket.getAverageSalePrice(0)) return false;
		
		MortgageAgreement m = Model.bank.requestApproval(me, maxPrice, 0.0, false); // maximise leverege with min downpayment
		
		double leverage = m.purchasePrice/m.downPayment;
		double rentalYield = Model.rentalMarket.averageSoldGrossYield*leverage;
		double capitalGainRate = HPAExpectation()*leverage;
		double mortgageRate = m.monthlyPayment*12.0/m.downPayment;
		double effectiveYield = (rentalYield-0.04) + BTL_CAP_GAIN_COEFF*(capitalGainRate - 0.04) - mortgageRate;
		double pDontBuy = Math.pow(1.0/(1.0 + Math.exp(INTENSITY*effectiveYield)),AGGREGATE_RATE);
//		System.out.println("DontBuy = "+(INTENSITY*effectiveYield) + " " + pDontBuy);
		return(Model.rand.nextDouble() < (1.0-pDontBuy));
	}
	
	public double btlPurchaseBid(Household me) {
		return(Math.min(Model.bank.getMaxMortgage(me, false), 1.1*Model.housingMarket.getAverageSalePrice(House.Config.N_QUALITY-1)));
	}

	public boolean isPropertyInvestor() {
		return(BTLInvestor);
	}

	public boolean setPropertyInvestor(boolean isInvestor) {
		return(BTLInvestor = isInvestor);
	}

//	public int nDesiredBTLProperties() {
//		return desiredBTLProperties;
//	}

	/*** @returns expectation value of HPI in one year's time divided by today's HPI*/
	public double HPAExpectation() {
		return(Model.housingMarket.housePriceAppreciation()*HPA_EXPECTATION_WEIGHT);
	}
	
	/*
	public double utilityOfRenting(Household me, int q) {
		double leftForConsumption = 1.0 - Model.rentalMarket.getAverageSalePrice(q)/me.monthlyEmploymentIncome;
		return(utilityOfHome(me,q) + qualityOfLiving(leftForConsumption));
	}


	public int findBestRentalQuality(Household me) {
		int bestQ = 0;
		double bestU = utilityOfRenting(me,0);
		double u;
		for(int q = 0; q<House.Config.N_QUALITY; ++q) {
			u = utilityOfRenting(me, q);
			if(u > bestU) {
				bestU = u;
				bestQ = q;
			}
		}
		return(bestQ);
	}

	public double utilityOfPurchase(Household me, int q, MortgageAgreement mortgage) {
		double price = Model.housingMarket.getAverageSalePrice(q);
		if(price > mortgage.purchasePrice)	return(-10.0);
		double principal = price - mortgage.downPayment;
		double leftForConsumption = 1.0 - (principal*Model.bank.monthlyPaymentFactor(true) - price*HPAExpectation()/12.0)/me.monthlyEmploymentIncome;
		return(utilityOfHome(me,q) + qualityOfLiving(leftForConsumption));
		
	}
	
	public PurchasePlan findBestPurchase(Household me) {
		PurchasePlan result = new PurchasePlan();
		MortgageAgreement maxMortgage = Model.bank.requestApproval(me, Model.bank.getMaxMortgage(me, true), downPayment(me) + me.getHomeEquity(), true);

		result.quality = 0;
		result.utility = utilityOfPurchase(me,0,maxMortgage);
		double u;
		for(int q = 1; q<House.Config.N_QUALITY; ++q) {
			u = utilityOfPurchase(me, q, maxMortgage);
			if(u > result.utility) {
				result.utility = u;
				result.quality = q;
			}
		}
		return(result);
		
	}

	public double utilityOfHome(Household me, int q) {
		final double rmo = 0.3; // reference housing spend
		final double k = 0.5; // flexibility of spend on hpi change
		final double c = k*rmo/(1.0+rmo*(k-1.0)); 	// 0.0968
    	final double lambda = (rmo-c)/(1-rmo);		// 0.290
		double Pref = (0.05/12.0)*Model.housingMarket.referencePrice(q)/me.monthlyEmploymentIncome;
		if(Pref < c) return(-10.0);
		return(lambda*Math.log(Pref-c));
	}
	
	public double qualityOfLiving(double consumptionFraction) {
		return(Math.log(consumptionFraction));
	}
	*/
}
