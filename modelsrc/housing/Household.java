package housing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import ec.util.MersenneTwisterFast;
/**********************************************
 * This represents a household who receives an income, consumes,
 * saves and can buy/sell/let/invest-in houses.
 * 
 * @author daniel, davidrpugh
 *
 **********************************************/
public class Household implements IHouseOwner {


	/**
	 * Configuration for a household.
	 * @author daniel
	 */
	static public class Config {

		// ---- Parameters
		public ConsumptionEqn	consumptionEqn = new ConsumptionEqn();
		public PurchaseEqn		purchaseEqn = new PurchaseEqn();
		public SaleEqn			saleEqn = new SaleEqn();
		public RenterPurchaseDecision renterPurchaseDecision = new RenterPurchaseDecision();
		public BuyToLetPurchaseDecision buyToLetPurchaseDecision = new BuyToLetPurchaseDecision();
		public static double RENT_PROFIT_MARGIN = 0.0; // profit margin for buy-to-let investors
		public double HOUSE_SALE_PRICE_DISCOUNT = 0.95; // monthly discount on price of house for sale
		public double DOWNPAYMENT_FRACTION = 0.1 + 0.0025*Model.rand.nextGaussian(); // Fraction of bank-balance household would like to spend on mortgage downpayments

		public double P_SELL = 1.0/(7.0*12.0);  // monthly probability of selling home
		public double P_INVESTOR = 0.04; 		// Probability of being (wanting to be) a property investor
		public static double RETURN_ON_FINANCIAL_WEALTH = 0.002; // monthly percentage growth of financial investements
		protected MersenneTwisterFast 	rand = Model.rand;
		LogNormalDistribution buyToLetDistribution  = new LogNormalDistribution(Math.log(3.44), 1.050); // No. of houses owned by buy-to-let investors Source: ARLA review and index Q2 2014

		/********************************
		 * How much a household consumes
		 * @author daniel
		 *
		 ********************************/
		static public class ConsumptionEqn {
			public double ALPHA = 0.2; // propensity to consume income
			public double BETA = 0.01; // propensity to consume liquid wealth
			
			/**
			 * Linear consumption rule. Economists seem to like it.
			 */
			public double desiredConsumption(double disposableIncome, double bankBalance) {
				if(disposableIncome > 0.0) {
					return(ALPHA*disposableIncome + BETA*bankBalance);
				} else {
					return(BETA*Math.max(bankBalance + disposableIncome,0.0));
				}
			}
			
			/**
			 * Consumption rule made to fit wealth data.
			 */
			public double desiredConsumptionB(double monthlyIncome, double bankBalance) {
				return(0.1*Math.max((bankBalance - Math.exp(4.07*Math.log(monthlyIncome*12.0)-33.1 + 0.2*Model.rand.nextGaussian())),0.0));
			}

			public double getALPHA() {
				return ALPHA;
			}
			public void setALPHA(double aLPHA) {
				ALPHA = aLPHA;
			}
			public double getBETA() {
				return BETA;
			}
			public void setBETA(double bETA) {
				BETA = bETA;
			}
			public String toString() {return "Household Consumption Equation";}
			public String desALPHA() {return("Marginal propensity to consume income");}
			public String desBETA() {return("Marginal propensity to consume liquid wealth");}
		}

		/**
		 * Decide on desired purchase price as a function of monthly income and current
		 * appreciation of the house price index.
		 * @author daniel
		 */
		static public class PurchaseEqn {
			static public double A = 0.0;//0.4;//0.48;			// sensitivity to house price appreciation
			static public double EPSILON = 0.40;//0.36;//0.48;//0.365; // S.D. of noise
			static public double SIGMA = 5.6*12.0;//5.5;	// scale

			public double desiredPrice(double monthlyIncome, double hpa) {
				double p = SIGMA*monthlyIncome*Math.exp(EPSILON*Model.rand.nextGaussian())/(1.0 - A*hpa);
				return(p);
			}

			public String toString() {return("(SIGMA.i.exp^r)/(1-A.hpa)");}
			public String desA() {return("Sensitivity to house price appreciation");}
			public String desEPSILON() {return("Standard Deviation of noise term");}
			public String desSIGMA() {return("Price level factor");}
			public static double getA() {
				return A;
			}

			public static void setA(double a) {
				A = a;
			}

			public static double getEPSILON() {
				return EPSILON;
			}

			public static void setEPSILON(double ePSILON) {
				EPSILON = ePSILON;
			}

			public static double getSIGMA() {
				return SIGMA;
			}

			public static void setSIGMA(double sIGMA) {
				SIGMA = sIGMA;
			}
			
		}

		/**
		 * The desired sale price of a house
		 * @author daniel
		 *
		 */
		static public class SaleEqn {
			static public double C = 0.095;	// initial markup from average price
			static public double D = 0.0;//0.024;//0.01;//0.001;		// Size of Days-on-market effect
			static public double E = 0.05; //0.05;	// SD of noise
			
			/**
			 * 
			 * @param pbar average sale price of houses of the same quality
			 * @param d average number of days on the market before sale
			 * @param principal amount of principal left on any mortgage on this house
			 * @return desired sale price
			 */
			public double desiredPrice(double pbar, double d, double principal) {
				double exponent = C + Math.log(pbar) - D*Math.log((d + 1.0)/31.0) + E*Model.rand.nextGaussian();
				return(Math.max(Math.exp(exponent), principal));
			}
			
			public String desA() {return("Initial markup");}
			public String desB() {return("Sensitivity to days-on-market");}
			public String desC() {return("Standard deviation of noise term");}
			
			public static double getC() {
				return C;
			}
			public static void setC(double c) {
				C = c;
			}
			public static double getD() {
				return D;
			}
			public static void setD(double d) {
				D = d;
			}
			public static double getE() {
				return E;
			}
			public static void setE(double e) {
				E = e;
			}

		}
		
		/////////////////////////////////////////////////////////////////////////////////
		static public class RenterPurchaseDecision {
			public double COST_OF_RENTING = 600; // Annual psychological cost of renting
			public double FTB_K = 1.0/600.0;//1.0/100000.0;//0.005 // Heterogeneity of sensitivity of desire to first-time-buy to cost
			
			public boolean buy(Household h, double housePrice, double annualRent) {
				double costOfHouse;
//				costOfHouse = housePrice*((1.0-HousingMarketTest.bank.config.THETA_FTB)*HousingMarketTest.bank.mortgageInterestRate() - HousingMarketTest.housingMarket.housePriceAppreciation());
				costOfHouse = housePrice*(Model.bank.loanToValue(h,true)*Model.bank.getMortgageInterestRate() - Model.housingMarket.housePriceAppreciation());
				return(Model.rand.nextDouble() < 1.0/(1.0 + Math.exp(-FTB_K*(annualRent + COST_OF_RENTING - costOfHouse))));
			}			
			public String desCOST_OF_RENTING() {return("Annual psychological cost of not owning a home");}
			public String desFTB_K() {return("Steepness of sigma function");}

			public double getCOST_OF_RENTING() {
				return COST_OF_RENTING;
			}

			public void setCOST_OF_RENTING(double cOST_OF_RENTING) {
				COST_OF_RENTING = cOST_OF_RENTING;
			}

			public double getFTB_K() {
				return FTB_K;
			}

			public void setFTB_K(double fTB_K) {
				FTB_K = fTB_K;
			}
		}

		/**
		 * Investors decision as to whether to buy a house as a buy-to-let investment.
		 * @author daniel
		 *
		 */
		static public class BuyToLetPurchaseDecision {
			/**
			 * 
			 * @param price The asking price of the house
			 * @param monthlyPayment The monthly payment on a mortgage for this house
			 * @param downPayment The minimum downpayment on a mortgage for this house
			 * @return will the investor decide to buy this house?
			 */
			public boolean buy(double price, double monthlyPayment, double downPayment) {
				double yield;
				yield = (monthlyPayment*12*Household.Config.RENT_PROFIT_MARGIN + Model.housingMarket.housePriceAppreciation()*price)/
						downPayment;
				
//				if(HousingMarketTest.rand.nextDouble() < 1.0/(1.0 + Math.exp(4.5 - yield*24.0))) {
				if(Model.rand.nextDouble() < 1.0/(1.0 + Math.exp(4.4 - yield*16.0))) {
					return(true);
				}
				return(false);
			}
		}
		
		/**
		 * How much rent does an investor decide to charge on a buy-to-let house? 
		 * @author daniel
		 *
		 */
		static public class RentalOfferPriceEqn {
			public double price(double mortgagePayment) {
				return(mortgagePayment*(1.0+Config.RENT_PROFIT_MARGIN));
			}
		}

		/**
		 * @return Does an owner-occupier decide to sell house?
		 */
		public boolean decideToSellHome() {
			if(rand.nextDouble() < P_SELL) return(true);
			return false;
		}
		
		/**
		 * 
		 * @param h The house in question
		 * @param me The investor
		 * @return Does an investor decide to sell a buy-to-let property
		 */
		public boolean decideToSellInvestmentProperty(House h, Household me) {
	//		System.out.println(me.desiredPropertyInvestmentFraction + " " + me.getDesiredPropertyInvestmentValue() + " -> "+me.getPropertyInvestmentValuation());
	//		if(me.getDesiredPropertyInvestmentValue() < 
	//				(me.getPropertyInvestmentValuation() - me.houseMarket.getAverageSalePrice(h.quality))) {
	//			return(true);
	//		}
			if(me.desiredBTLProperties > me.housePayments.size()+1) {
				return(true);
			}
			return(false);
		}
		
		
		public ConsumptionEqn getConsumptionEqn() {
			return consumptionEqn;
		}

		public void setConsumptionEqn(ConsumptionEqn consumptionEqn) {
			this.consumptionEqn = consumptionEqn;
		}

		public PurchaseEqn getPurchaseEqn() {
			return purchaseEqn;
		}

		public void setPurchaseEqn(PurchaseEqn purchaseEqn) {
			this.purchaseEqn = purchaseEqn;
		}

		public SaleEqn getSaleEqn() {
			return saleEqn;
		}

		public void setSaleEqn(SaleEqn saleEqn) {
			this.saleEqn = saleEqn;
		}

		public RenterPurchaseDecision getRenterPurchaseDecision() {
			return renterPurchaseDecision;
		}

		public void setRenterPurchaseDecision(
				RenterPurchaseDecision renterPurchaseDecision) {
			this.renterPurchaseDecision = renterPurchaseDecision;
		}

		public double getRENT_PROFIT_MARGIN() {
			return RENT_PROFIT_MARGIN;
		}

		public void setRENT_PROFIT_MARGIN(double rENT_PROFIT_MARGIN) {
			RENT_PROFIT_MARGIN = rENT_PROFIT_MARGIN;
		}

		public double getHOUSE_SALE_PRICE_DISCOUNT() {
			return HOUSE_SALE_PRICE_DISCOUNT;
		}

		public void setHOUSE_SALE_PRICE_DISCOUNT(double hOUSE_SALE_PRICE_DISCOUNT) {
			HOUSE_SALE_PRICE_DISCOUNT = hOUSE_SALE_PRICE_DISCOUNT;
		}


		public double getDOWNPAYMENT_FRACTION() {
			return DOWNPAYMENT_FRACTION;
		}

		public void setDOWNPAYMENT_FRACTION(double dOWNPAYMENT_FRACTION) {
			DOWNPAYMENT_FRACTION = dOWNPAYMENT_FRACTION;
		}

		public double getP_SELL() {
			return P_SELL;
		}

		public void setP_SELL(double p_SELL) {
			P_SELL = p_SELL;
		}

		public static double getRETURN_ON_FINANCIAL_WEALTH() {
			return RETURN_ON_FINANCIAL_WEALTH;
		}

		public static void setRETURN_ON_FINANCIAL_WEALTH(
				double rETURN_ON_FINANCIAL_WEALTH) {
			RETURN_ON_FINANCIAL_WEALTH = rETURN_ON_FINANCIAL_WEALTH;
		}
		
		public String desRENT_PROFIT_MARGIN() {return("Profit margin on rent charged by buy-to-let investors");}
		public String desHOUSE_SALE_PRICE_DISCOUNT() {return("Monthly discount on price of un-sold house");}
		public String desDOWNPAYMENT_FRACTION() {return("Fraction of bank-balance household would like to spend on mortgage downpayments");}
		public String desRETURN_ON_FINANCIAL_WEALTH() {return("Monthly percentage growth of liquid wealth");}
		public String desP_SELL() {return("Monthly probability of homeowner deciding to sell house");}
	}

	/**
	 * Visualisation for a household
	 * @author daniel
	 *
	 */
	static public class Diagnostics {
//	    public double [][]    homelessData;
//	    public double [][]    rentingData;
//	    public double [][]    bankBalData;
//	    public double [][]    referenceBankBalData;
		public double [][]	  tenureData;
	    public ArrayList<Household>	  households;
	    public double 		  nRenting;
	    public double 		  nHomeless;
	    public double 		  nNonOwner;
	    public double		  nHouseholds;
	    public double		  nEmpty;
	    public static int 	  NBINS;	// number of bins on histograms	
	    
	    public Diagnostics(ArrayList<Household> array) {	    	
	    	households = array;
//	        homelessData = new double[2][NBINS];
//	        rentingData = new double[2][NBINS];
//	        bankBalData = new double[2][NBINS];
//	        referenceBankBalData = new double[2][NBINS];
	    	tenureData = new double[2][4];
	    }
	    
	    public void init() {
//	    	int i;
//	    	double income;
//	        for(i = 0; i<NBINS; ++i) {
//				income = Household.Config.incomeDistribution.inverseCumulativeProbability((i+0.5)/NBINS);

//	        	homelessData[0][i/50] = income;
//	        	rentingData[0][i/50] = income;
//	        	bankBalData[0][i/50] = income;
//	        	homelessData[1][i/50] = 0.0;
//	        	referenceBankBalData[0][i/50] = income;
//	        	referenceBankBalData[1][i/50] = Model.grossFinancialWealth.inverseCumulativeProbability((i+0.5)/NBINS);
//	        }
	    	
	    }
	    
	    public void step() {
	//    	int i,j,n,r;
	    	
	    	// --- fill in tenure histogram
	    	nRenting = 0;
	    	nHomeless = 0;
	    	nHouseholds = Model.households.size();
	    	for(Household h : households) {
	    		if(h.isHomeless()) {
	    			++nHomeless;
	    		} else if(h.isRenting()) {
	    			++nRenting;
	    		}
	    	}
	    	nNonOwner = nHomeless + nRenting;
	    	nEmpty = Model.construction.housingStock + nHomeless - nHouseholds;
	    	/**
	    	nRenting = 0;
	    	nHomeless = 0;
	    	for(Household h : households) {
	    		i = h.annualEmploymentIncome
	    		if(h.isHomeless()) {
	    			homelessData[1][h.annualEmploymentIncome]
	    		}
	    	}
	        for(i = 0; i<Model.N-50; i += 50) {
	        	n = 0;
	        	r = 0;
	        	for(j = 0; j<50; ++j) {
	        		if(households.get(i+j).isHomeless()) {
	        			n++;
	        			nHomeless++;
	        		} else if(households.get(i+j).isRenting()) {
	        			r++;
	        			nRenting++;
	        		}
	        	}
	        	homelessData[1][i/50] = n/50.0;
	        	rentingData[1][i/50] = r/50.0;
	        }
	        **/
	        // --- fill in bank balance data
	    	/**
	        for(i=0; i<Model.N-50; i+=50) {
	        	bankBalData[1][i/50] = households.get(i).bankBalance;
	        }
			**/
	    }
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Model
	//////////////////////////////////////////////////////////////////////////////////////

	/********************************************************
	 * Constructor.
	 ********************************************************/
	public Household(double age) {
		this(new Household.Config(), age);
	}

	public Household(Household.Config c, double iage) {
		config = c;
		bank = Model.bank;
		houseMarket = Model.housingMarket;
		rentalMarket = Model.rentalMarket;
		rand = Model.rand;
		home = null;
		bankBalance = 0.0;
		isFirstTimeBuyer = true;
		if(c.P_INVESTOR < rand.nextDouble()) {
			desiredBTLProperties = (int)c.buyToLetDistribution.inverseCumulativeProbability(rand.nextDouble());
		} else {
			desiredBTLProperties = 0;
		}
//		setDesiredPropertyInvestmentFraction(0.0);
		id = ++id_pool;
		lifecycle = new Lifecycle(iage);
	}


	/////////////////////////////////////////////////////////
	// Inheritance behaviour
	/////////////////////////////////////////////////////////

	public void transferAllWealthTo(Household beneficiary) {
		for(House h : housePayments.keySet()) {
			if(home == h) {
				h.resident = null;
				home = null;
			}
			if(h.owner == this) {
				if(rentalMarket.isOnMarket(h)) rentalMarket.removeOffer(h);
				if(houseMarket.isOnMarket(h)) houseMarket.removeOffer(h);
				beneficiary.inheritHouse(h);
			} else {
				h.owner.endOfLettingAgreement(h);
			}
		}
		housePayments.clear();
		beneficiary.bankBalance += bankBalance;
	}
	
	/**
	 * 
	 * @param h House to inherit
	 */
	public void inheritHouse(House h) {
		MortgageApproval nullMortgage = new MortgageApproval();
		nullMortgage.nPayments = 0;
		nullMortgage.downPayment = 0.0;
		nullMortgage.monthlyInterestRate = 0.0;
		nullMortgage.monthlyPayment = 0.0;
		nullMortgage.principal = 0.0;
		housePayments.put(h, nullMortgage);
		h.owner = this;
		if(!isHomeowner()) {
			if(isRenting()) {
				home.owner.endOfLettingAgreement(home);
				endTenancy();
			}
			if(h.resident != null) {
				h.resident.endTenancy();
			}
			home = h;
			h.resident = this;
		} else if(decideToSellHouse(h)) {
			putHouseForSale(h);
		} else if(h.resident == null) {
			endOfLettingAgreement(h); // put inherited house on rental market
		}
	}
	
	/////////////////////////////////////////////////////////
	// House market behaviour
	/////////////////////////////////////////////////////////

	/********************************************************
	 * First step in a time-step:
	 * Receive income, pay rent/mortgage, make consumption decision
	 * and make decision to buy/sell house.
	 ********************************************************/
	public void preHouseSaleStep() {
		double disposableIncome;
		
		lifecycle.step();
		annualEmploymentIncome = lifecycle.annualIncome();
		disposableIncome = getMonthlyDisposableIncome() - 0.8 * Government.Config.INCOME_SUPPORT;

//		System.out.println("income = "+monthlyIncome+" disposable = "+disposableIncome );
		
		// ---- Pay rent/mortgage(s)
		Iterator<Map.Entry<House,MortgageApproval> > mapIt = housePayments.entrySet().iterator();
		Map.Entry<House,MortgageApproval> payment;
		while(mapIt.hasNext()) {
			payment = mapIt.next();
			if(payment.getValue().nPayments > 0) {
				disposableIncome -= payment.getValue().makeMonthlyPayment();
				if(isCollectingRentFrom(payment.getKey())) {
					// profit from rent collection
					//disposableIncome += payment.getValue().monthlyPayment*(1.0+config.RENT_PROFIT_MARGIN);
				}
				if(payment.getValue().nPayments == 0) { // do paid-off stuff
					if(payment.getKey().owner != this) { // renting
						if(home == null) System.out.println("Strange: paying rent and homeless");
						if(payment.getKey() != home) System.out.println("Strange: I seem to be renting a house but not living in it");
						if(home.resident != this) System.out.println("home/resident link is broken");
						payment.getKey().owner.endOfLettingAgreement(payment.getKey());
						home.resident = null;
						home = null;
						mapIt.remove();
					}
				}
			}
		}
		
		// --- consume
//		bankBalance += disposableIncome - config.consumptionEqn.desiredConsumption(disposableIncome,bankBalance);
		bankBalance += disposableIncome - config.consumptionEqn.desiredConsumptionB(getMonthlyEmploymentIncome(),bankBalance);
//		bankBalance += -config.consumptionEqn.desiredConsumptionB(monthlyIncome,bankBalance);
		
		if(bankBalance < 0.0) {
			// bankrupt behaviour
//			System.out.println("Household gone bankrupt!");
//			System.out.println("...Houses = "+housePayments.size());
//			int i = 0;
//			for(House h : housePayments.keySet()) {
//				if(h.resident == null) ++i;
//			}
//			System.out.println("...Empty = "+i);
				
			// TODO: cash injection for now...
			bankBalance = 1.0;
		}
		
		makeHousingDecision();
	}

	/********************************************************
	 * Second step in a time-step. At this point, the
	 * household may have sold their house, but not managed
	 * to buy a new one, so must enter the rental market.
	 * 
	 * This is also where investors get to bid for buy-to-let
	 * housing.
	 ********************************************************/
	public void preHouseLettingStep() {
		if(isHomeless()) {
			rentalMarket.bid(this, desiredRent());
		} else if(isPropertyInvestor()) { // this is a buy-to-let investor
			houseMarket.bid(this, bank.getMaxMortgage(this, false));
		}
	}
	
	/********************************************************
	 *  Make decision to buy/sell houses
	 ********************************************************/
	void makeHousingDecision() {
		// --- add and manage houses for sale
		HouseSaleRecord forSale;
		double newPrice;
		
		for(House h : housePayments.keySet()) {
			if(h.owner == this) {
				forSale = houseMarket.getSaleRecord(h);
				if(forSale != null) { // reprice house for sale
					newPrice = rethinkHouseSalePrice(forSale);
					if(newPrice > housePayments.get(h).principal) {
						houseMarket.updateOffer(h, newPrice);						
					} else {
						houseMarket.removeOffer(h);
						if(h != home && h.resident == null) {
							rentalMarket.offer(h, housePayments.get(h).monthlyPayment*(1.0+Config.RENT_PROFIT_MARGIN));							
						}
					}
				} else if(decideToSellHouse(h)) { // put house on market
					putHouseForSale(h);
				}
			}
		}
		
		// ---- try to buy house?
		if(!isHomeowner()) {
			decideToStopRenting();
		}
	}

	protected void putHouseForSale(House h) {
		if(rentalMarket.isOnMarket(h)) {
			rentalMarket.removeOffer(h);
		}
		houseMarket.offer(h, config.saleEqn.desiredPrice(
				houseMarket.averageSalePrice[h.quality],
				houseMarket.averageDaysOnMarket,
				housePayments.get(h).principal
		));
	}
	
	/////////////////////////////////////////////////////////
	// Houseowner interface
	/////////////////////////////////////////////////////////

	/********************************************************
	 * Do all the stuff necessary when this household
	 * buys a house:
	 * Give notice to landlord if renting,
	 * Get loan from mortgage-lender,
	 * Pay for house,
	 * Put house on rental market if buy-to-let and no tenant.
	 ********************************************************/
	public void completeHousePurchase(HouseSaleRecord sale) {
		if(isRenting()) { // give immediate notice to landlord and move out
			if(sale.house.resident != null) System.out.println("Strange: my new house has someone in it!");
			if(home == sale.house) System.out.println("Strange: I've just bought a house I'm renting out");
			if(home != sale.house) home.owner.endOfLettingAgreement(home);
			endTenancy();
		}
		MortgageApproval mortgage = bank.requestLoan(this, sale.currentPrice, bankBalance*config.DOWNPAYMENT_FRACTION, home == null);
		if(mortgage == null) {
			// TODO: throw exception
			System.out.println("Can't afford to buy house: strange");
			System.out.println("Want "+sale.currentPrice+" but can only get "+bank.getMaxMortgage(this,home==null));
			System.out.println("Bank balance is "+bankBalance+". DisposableIncome is "+ getMonthlyDiscretionaryIncome());
			System.out.println("Annual income is "+ getMonthlyEmploymentIncome() *12.0);
			if(isRenting()) System.out.println("Is renting");
			if(isHomeowner()) System.out.println("Is homeowner");
			if(isHomeless()) System.out.println("Is homeless");
			if(isFirstTimeBuyer()) System.out.println("Is firsttimebuyer");
			if(isPropertyInvestor()) System.out.println("Is investor");
			System.out.println("House owner = "+sale.house.owner);
			System.out.println("me = "+this);
		}
		bankBalance -= mortgage.downPayment;
		housePayments.put(sale.house, mortgage);
		if(home == null) { // move in to house
			home = sale.house;
			sale.house.resident = this;
		} else if(sale.house.resident == null) { // put empty buy-to-let house on rental market
			endOfLettingAgreement(sale.house);
		}
		isFirstTimeBuyer = false;
	}
		
	/********************************************************
	 * Do all stuff necessary when this household sells a house
	 ********************************************************/
	public void completeHouseSale(HouseSaleRecord sale) {
		double profit = sale.currentPrice - housePayments.get(sale.house).payoff(bankBalance+sale.currentPrice);
		if(profit < 0) System.out.println("Strange: Profit is negative.");
		bankBalance += profit;
		if(housePayments.get(sale.house).nPayments == 0) {
			housePayments.remove(sale.house);
		}
		if(sale.house == home) { // move out of home and become (temporarily) homeless
			home.resident = null;
			home = null;
			bidOnHousingMarket(1.0);
		} else if(sale.house.resident != null) { // evict current renter
			sale.house.resident.endTenancy();
		}
	}
	
	/********************************************************
	 * A household receives this message when a tenant moves
	 * out of one of its buy-to-let houses.
	 * 
	 * The household simply puts the house back on the rental
	 * market.
	 ********************************************************/
	@Override
	public void endOfLettingAgreement(House h) {
		// put house back on rental market
		if(!housePayments.containsKey(h)) {
			System.out.println("I don't own this house: strange");
		}
		if(h.resident != null && h.resident == h.owner) System.out.println("Strange: renting out a house that belongs to a homeowner");		
		if(!houseMarket.isOnMarket(h)) {
			if(rentalMarket.isOnMarket(h)) System.out.println("Strange: got endOfLettingAgreement on house on rental market");
			rentalMarket.offer(h, housePayments.get(h).monthlyPayment*(1.0+Config.RENT_PROFIT_MARGIN));
		}
	}

	/**********************************************************
	 * This household moves out of current rented accommodation
	 * and becomes homeless (possibly temporarily)
	 **********************************************************/
	public void endTenancy() {
		if(home.owner == this) System.out.println("Strange: got endTenancy on a home I own");
		housePayments.remove(home);
		home.resident = null;
		home = null;		
	}

	/********************************************************
	 * Do all the stuff necessary when this household moves
	 * in to rented accommodation (i.e. set up a regular
	 * payment contract. At present we use a MortgageApproval).
	 ********************************************************/
	public void completeHouseRental(HouseSaleRecord sale) {
		if(sale.house.owner != this) { // if renting own house, no need for contract
			MortgageApproval rent = new MortgageApproval();
			rent.downPayment = 0.0;
			rent.monthlyPayment = sale.currentPrice;
			rent.monthlyInterestRate = 0.0;
			rent.nPayments = (int)(12.0*rand.nextDouble()+1);
			rent.principal = rent.monthlyPayment*rent.nPayments;
			housePayments.put(sale.house, rent);
		}
		home = sale.house;
		if(sale.house.resident != null) {
			System.out.println("Strange: moving into an occupied house");
			if(sale.house.resident == this) System.out.println("...It's me!");
			if(sale.house.owner == this) System.out.println("...It's my house!");
			if(sale.house.owner == sale.house.resident) System.out.println("...It's a homeowner!");
		}
		sale.house.resident = this;
	}


	/////////////////////////////////////////////////////////
	// Homeowner helper stuff
	/////////////////////////////////////////////////////////

	/****************************************
	 * Put a bid on the housing market if this household can afford a
	 * mortgage at its desired price.
	 * 
	 * @param p The probability that the household will actually bid,
	 * given that it can afford a mortgage.
	 ****************************************/
	protected void bidOnHousingMarket(double p) {
		double desiredPrice = config.purchaseEqn.desiredPrice(getMonthlyEmploymentIncome(), houseMarket.housePriceAppreciation());
		double maxMortgage = bank.getMaxMortgage(this, true);
		double ltiConstraint =  annualEmploymentIncome * bank.config.LTI/bank.loanToValue(this, true); // ##### TEST #####
		if(desiredPrice > ltiConstraint) desiredPrice = ltiConstraint - 1.0; // ##### TEST #####
//		if(desiredPrice > maxMortgage) desiredPrice = maxMortgage - 1;
		if(desiredPrice <= maxMortgage) {
			if(p<1.0) {
				if(rand.nextDouble() < p) houseMarket.bid(this, desiredPrice);
			} else {
				// no need to call random if p = 1.0
				houseMarket.bid(this, desiredPrice);				
			}
		}
	}
	
	/********************************************************
	 * Make the decision whether to bid on the housing market when renting.
	 * This is an "intensity of choice" decision (sigma function)
	 * on the cost of renting compared to the cost of owning, with
	 * COST_OF_RENTING being an intrinsic psychological cost of not
	 * owning. 
	 ********************************************************/
	protected void decideToStopRenting() {
		double costOfRent;
		double housePrice = config.purchaseEqn.desiredPrice(getMonthlyEmploymentIncome(), houseMarket.housePriceAppreciation());
		double maxMortgage = bank.getMaxMortgage(this, true);
		double ltiConstraint =  annualEmploymentIncome * bank.config.LTI/bank.loanToValue(this, true); // ##### TEST #####
		if(housePrice > ltiConstraint) housePrice = ltiConstraint - 1.0; // ##### TEST #####
		if(housePrice <= maxMortgage) {
			if(home != null) {
				costOfRent = housePayments.get(home).monthlyPayment*12;
			} else {
				costOfRent = rentalMarket.averageSalePrice[0]*12;
//				costOfRent = 0.0;
			}
			if(config.renterPurchaseDecision.buy(this, housePrice, costOfRent)) {
				houseMarket.bid(this, housePrice);
			}
		}
	}
	
	
	/********************************************************
	 * Decide whether to sell ones own house.
	 ********************************************************/
	private boolean decideToSellHouse(House h) {
		if(h == home) {
			return(config.decideToSellHome());
		}
		return(config.decideToSellInvestmentProperty(h, this));
	}

	
	/********************************************************
	 * Decide how much to drop the list-price of a house if
	 * it has been on the market for (another) month and hasn't
	 * sold.
	 * 
	 * @param sale The HouseSaleRecord of the house that is on the market.
	 ********************************************************/
	protected double rethinkHouseSalePrice(HouseSaleRecord sale) {
		return(sale.currentPrice * config.HOUSE_SALE_PRICE_DISCOUNT);
	}

	
	/********************************************************
	 * Decide how much to bid on the rental market
	 ********************************************************/
	public double desiredRent() {
		return(0.3* getMonthlyEmploymentIncome());
	}
	
	/********************************************************
	 * Decide whether to buy a house as a buy-to-let investment
	 ********************************************************/
	public boolean decideToBuyBuyToLet(double price) {
		if(price <= bank.getMaxMortgage(this, false)) {
			MortgageApproval mortgage;
			mortgage = bank.requestApproval(this, price, bankBalance * config.DOWNPAYMENT_FRACTION, false);
			return(config.buyToLetPurchaseDecision.buy(price, mortgage.monthlyPayment, mortgage.downPayment));
		}
		return(false);
	}
	
	/////////////////////////////////////////////////////////
	// Helpers
	/////////////////////////////////////////////////////////


	public boolean isHomeowner() {
		if(home == null) return(false);
		return(home.owner == this);
	}

	public boolean isRenting() {
		if(home == null) return(false);
		return(home.owner != this);
	}

	public boolean isHomeless() {
		return(home == null);
	}

	public boolean isFirstTimeBuyer() {
		return isFirstTimeBuyer;
	}

	public boolean isPropertyInvestor() {
//		return(housePayments.size() > 1);
		return(desiredBTLProperties > 0);
	}
	
	//////////////////////////////////////////////////////////////////
	// Fraction of property+financial wealth that I want to invest
	// in buy-to-let housing
	//////////////////////////////////////////////////////////////////
//	public void setDesiredPropertyInvestmentFraction(double val) {
//		this.desiredPropertyInvestmentFraction = val;
//	}

	/////////////////////////////////////////////////////////////////
	// Current valuation of buy-to-let properties, not including
	// houses up for sale.
	/////////////////////////////////////////////////////////////////
	public double getPropertyInvestmentValuation() {
		double valuation = 0.0;
		for(House h : housePayments.keySet()) {
			if(h.owner == this && h != home && !houseMarket.isOnMarket(h)) {
				valuation += houseMarket.getAverageSalePrice(h.quality);
			}
		}
		return(valuation);
	}
	
	///////////////////////////////////////////////////////////////
	// returns current desired cash value of buy-to-let property investment
	//////////////////////////////////////////////////////////////
//	public double getDesiredPropertyInvestmentValue() {
//		return(desiredPropertyInvestmentFraction * (getPropertyInvestmentValuation() + bankBalance));
//	}
	
	public boolean isCollectingRentFrom(House h) {
		return(h.owner == this && h != home && h.resident != null);
	}

	/**
	 * @return total annual income tax due
	 */
	public double getAnnualIncomeTax() {
		return Model.government.incomeTaxDue(annualEmploymentIncome);
	}

	/**
	 * @return total annual national insurance contributions
	 */
	public double getAnnualNationalInsuranceTax() {
		return Model.government.class1NICsDue(annualEmploymentIncome);
	}

	/**
	 * @return total annual taxes due
	 */
	public double getAnnualTotalTax() {
		return getAnnualIncomeTax() + getAnnualNationalInsuranceTax();
	}

	/**
	 * @return discretionary income is disposable income less any mortgage payments
	 */
	public double getMonthlyDiscretionaryIncome() {
		return getMonthlyDisposableIncome() - getMonthlyTotalMortgagePayments();
	}

	/**
	 * @return monthly disposable (i.e., after tax) income
	 */
	public double getMonthlyDisposableIncome() {
		return getMonthlyTotalIncome() - getMonthlyTotalTax();
	}

	/**
	 * @return gross monthly employment (i.e., before tax) income
	 */
	public double getMonthlyEmploymentIncome() {
		return annualEmploymentIncome / 12.0;
	}

	/**
	 * @return monthly interest income
	 */
	public double getMonthlyInterestIncome() {
		return bankBalance * Config.RETURN_ON_FINANCIAL_WEALTH;
	}

	/**
	 * @return gross property income will be zero for most households
	 */
	public double getMonthlyPropertyIncome() {
		double propertyIncome = 0.0;
		for (Map.Entry<House, MortgageApproval> payment : housePayments.entrySet()) {
			if (isCollectingRentFrom(payment.getKey())) {
				propertyIncome += payment.getValue().monthlyPayment * (1.0 + Config.RENT_PROFIT_MARGIN);
			}
		}
		return propertyIncome;
	}

	/**
	 * @return gross monthly total income
	 */
	public double getMonthlyTotalIncome() {
		double monthlyTotalIncome = (getMonthlyEmploymentIncome() +
				getMonthlyPropertyIncome() + getMonthlyInterestIncome());
		return monthlyTotalIncome;
	}

	/**
	 * @return monthly total monthly interest payments for all houses owned
	 */
	public double getMonthlyTotalInterestPayments() {
		double totalInterestPayments = 0.0;
		double interestPayment;
		if (! isRenting()) {
			for (Map.Entry<House, MortgageApproval> payment : housePayments.entrySet()) {
				interestPayment = payment.getValue().principal * payment.getValue().monthlyInterestRate;
				totalInterestPayments += interestPayment;
			}
		}
		return totalInterestPayments;
	}

	/**
	 * @return monthly total monthly mortgage payments for all houses owned
	 */
	public double getMonthlyTotalMortgagePayments() {
		double totalMortgagePayments = 0.0;
		if (! isRenting()) {
			for (Map.Entry<House, MortgageApproval> payment : housePayments.entrySet()) {
				totalMortgagePayments += payment.getValue().monthlyPayment;
			}
		}
		return totalMortgagePayments;
	}

	/**
	 * @return monthly total monthly principal payments for all houses owned
	 */
	public double getMonthlyTotalPrincipalPayments() {
		double totalPrincipalPayments = 0.0;
		double interestPayment, mortgagePayment;
		if (! isRenting()) {
			for (Map.Entry<House, MortgageApproval> payment : housePayments.entrySet()) {
				mortgagePayment = payment.getValue().monthlyPayment;
				interestPayment = payment.getValue().principal * payment.getValue().monthlyInterestRate;
				totalPrincipalPayments += mortgagePayment - interestPayment;
			}
		}
		return totalPrincipalPayments;
	}

	/**
	 * @return total monthly taxes due
	 */
	public double getMonthlyTotalTax() {
		return getAnnualTotalTax() / 12.0;
	}

	
	///////////////////////////////////////////////
	
	Household.Config	config;
	HouseSaleMarket		houseMarket;
	HouseRentalMarket	rentalMarket;

	protected double 	annualEmploymentIncome;
	protected double 	bankBalance;
	protected House		home; // current home
	protected Map<House, MortgageApproval> 		housePayments = new TreeMap<House, MortgageApproval>(); // houses owned
	private boolean		isFirstTimeBuyer;
//	public	double		desiredPropertyInvestmentFraction;
	public int			desiredBTLProperties;	// number of properties
	Bank				bank;
	public int		 	id;		// only to ensure deterministic execution
	protected MersenneTwisterFast 	rand;
	
	public Lifecycle	lifecycle;	// lifecycle plugin
	
	static Diagnostics	diagnostics = new Diagnostics(Model.households);
	static int		 id_pool;

}
