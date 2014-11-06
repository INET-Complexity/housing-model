package eu.crisis_economics.abm.markets.housing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
/**********************************************
 * This represents a household who receives an income, consumes,
 * saves and can buy/sell/let/invest-in houses.
 * 
 * @author daniel
 *
 **********************************************/
public class Household implements IHouseOwner {
		
	/********************************************************
	 * Constructor.
	 ********************************************************/
	public Household() {
		bank = HousingMarketTest.bank;
		houseMarket = HousingMarketTest.housingMarket;
		rentalMarket = HousingMarketTest.rentalMarket;
		home = null;
		bankBalance = 0.0;
		isFirstTimeBuyer = true;
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
		double consumption;
		
		disposableIncome = monthlyIncome - Government.incomeTaxDue(monthlyIncome*12)/12.0 - Government.class1NICsDue(monthlyIncome*12)/12.0;

		// ---- Pay rent/mortgage(s)
		Iterator<Map.Entry<House,MortgageApproval> > mapIt = housePayments.entrySet().iterator();
		Map.Entry<House,MortgageApproval> payment;
		while(mapIt.hasNext()) {
			payment = mapIt.next();
			if(payment.getValue().nPayments > 0) {
				disposableIncome -= payment.getValue().makeMonthlyPayment();					
				if(isCollectingRentFrom(payment.getKey())) {
					// profit from rent collection
					disposableIncome += payment.getValue().monthlyPayment*(1.0+RENT_PROFIT_MARGIN);
				}
				if(payment.getValue().nPayments == 0) { // do paid-off stuff
					if(payment.getKey().owner != this) { // renting
						payment.getKey().owner.endOfLettingAgreement(payment.getKey());
						if(payment.getKey() == home) {
							home.resident = null;
							home = null;
						}
						mapIt.remove();
					}
				}
			}
		}
		
		// --- consume
		consumption = ALPHA*Math.max(disposableIncome,0.0);
		if(disposableIncome > 0.0) {
			consumption += BETA*bankBalance;
		} else {
			if(bankBalance + disposableIncome < 0.0) {
				// bankrupt behaviour
				System.out.println("Household gone bankrupt!");
				System.out.println("...Houses = "+housePayments.size());
				int i = 0;
				for(House h : housePayments.keySet()) {
					if(h.resident == null) ++i;
				}
				System.out.println("...Empty = "+i);
				
				// TODO: cash injection for now...
				bankBalance = 1 - disposableIncome;
			}
			consumption += BETA*(bankBalance + disposableIncome);
		}
		bankBalance += disposableIncome - consumption;
		
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
		} else if(housePayments.size() > 1) { // this is a buy-to-let investor
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
					}
				} else if(decideToSellHouse(h)) { // put house on market
					houseMarket.offer(h, desiredHouseSalePrice(h));
				}
			}
		}
		
		// ---- try to buy house?
		if(!isHomeowner()) {
			decideToBuyFirstHome();
		}
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
		if(isRenting()) { // give immediate notice to landlord
			if(home != sale.house) home.owner.endOfLettingAgreement(home);
			housePayments.remove(home);
			home.resident = null;
			home = null;
		}
		MortgageApproval mortgage = bank.requestLoan(this, sale.currentPrice, home == null);
		if(mortgage == null) {
			// TODO: throw exception
			System.out.println("Can't afford to buy house: strange");
			System.out.println("Want "+sale.currentPrice+" but can only get "+bank.getMaxMortgage(this,home==null));
			System.out.println("Bank balance is "+bankBalance+". DisposableIncome is "+monthlyDisposableIncome());			
			System.out.println("Annual income is "+monthlyIncome*12.0);			
			if(isRenting()) System.out.println("Is renting");
			if(isHomeowner()) System.out.println("Is homeowner");
			if(isHomeless()) System.out.println("Is homeless");
			if(isFirstTimeBuyer()) System.out.println("Is firsttimebuyer");
		}
		bankBalance -= mortgage.downPayment;
		housePayments.put(sale.house, mortgage);
		if(home == null) {
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
		if(sale.house == home) {
			home.resident = null;
			home = null;
			bidOnHousingMarket(1.0);
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
		rentalMarket.offer(h, housePayments.get(h).monthlyPayment*(1.0+RENT_PROFIT_MARGIN));
	}


	/********************************************************
	 * Do all the stuff necessary when this household moves
	 * in to rented accommodation (i.e. set up a regular
	 * payment contract. At present we use a MortgageApproval).
	 ********************************************************/
	public void completeHouseRental(HouseSaleRecord sale) {
		MortgageApproval rent = new MortgageApproval();
		rent.downPayment = 0.0;
		rent.monthlyPayment = sale.currentPrice;
		rent.monthlyInterest = 0.0;
		rent.nPayments = (int)(12.0*Math.random()+1);
		rent.principal = rent.monthlyPayment*rent.nPayments;
		home = sale.house;
		sale.house.resident = this;
		housePayments.put(home, rent);
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
		double desiredPrice = desiredHousePurchasePrice();
		double maxMortgage = bank.getMaxMortgage(this, true);
		if(desiredPrice <= maxMortgage) {
			if(p<1.0) {
				if(Math.random() < p) houseMarket.bid(this, desiredPrice);
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
	protected void decideToBuyFirstHome() {
		double costOfHouse;
		double costOfRent;
		double p = desiredHousePurchasePrice();
		double maxMortgage = bank.getMaxMortgage(this, true);
		if(p <= maxMortgage) {
			costOfHouse = p*(1.0-Bank.THETA_FTB)*bank.mortgageInterestRate() - p*houseMarket.housePriceAppreciation();
			if(home != null) {
				costOfRent = housePayments.get(home).monthlyPayment*12;
			} else {
				costOfRent = rentalMarket.averageSalePrice[0];
			}
			if(Math.random() < 1.0/(1.0 + Math.exp(-FTB_K*(costOfRent + COST_OF_RENTING - costOfHouse)))) {
				houseMarket.bid(this, p);
			}
		}
	}
	
	/********************************************************
	 * Calculate the price of a house that this household would like to buy
	 * 
	 * @return The desired price.
	 ********************************************************/
	public double desiredHousePurchasePrice() {
		final double h = 0.4;//38.8;
		final double g = 1.0;//0.56;
		final double a = 0.01;//0.16;//0.16;
		final double tau = 0.02;
		final double c = 0.03;
		double epsilon;
		
		epsilon = Math.exp(0.46*rand.nextGaussian() - 0.13);
		
		return(4.5*monthlyIncome*12.0*Math.exp(0.3*rand.nextGaussian())/(1.0 - a*houseMarket.housePriceAppreciation()));
//		return(epsilon * h * Math.pow(monthlyIncome*12, g)/
//				(tau + c + bank.loanToValue(this,true)*bank.mortgageInterestRate() - a*houseMarket.housePriceAppreciation()));
	}
	
	/********************************************************
	 * Decide whether to sell ones own house.
	 ********************************************************/
	private boolean decideToSellHouse(House h) {
		if(Math.random() < P_SELL) return(true);
		return false;
	}

	/********************************************************
	 * Decide the initial list price if this household was to put
	 * its own home on the market.
	 ********************************************************/
	public double desiredHouseSalePrice(House house) {
		/**	Original version (Axtell):	
		double exponent = 
				0.22
				+ 0.99*Math.log(houseMarket.averageListPrice[house.quality])
				+ 0.22*Math.log(houseMarket.averageSoldPriceToOLP)
				- 0.01*Math.log(houseMarket.averageDaysOnMarket + 1)
				+ 0.01*rand.nextGaussian();
				**/
		double exponent = 
				0.095
				+ Math.log(houseMarket.averageSalePrice[house.quality])
				- 0.001*Math.log((houseMarket.averageDaysOnMarket + 1.0)/31.0)
				+ 0.05*rand.nextGaussian();
		return(Math.max(Math.exp(exponent), housePayments.get(house).principal));
	}

	
	/********************************************************
	 * Decide how much to drop the list-price of a house if
	 * it has been on the market for (another) month and hasn't
	 * sold.
	 * 
	 * @param sale The HouseSaleRecord of the house that is on the market.
	 ********************************************************/
	protected double rethinkHouseSalePrice(HouseSaleRecord sale) {
		return(sale.currentPrice * HOUSE_SALE_PRICE_DISCOUNT);
	}

	
	/********************************************************
	 * Decide how much to bid on the rental market
	 ********************************************************/
	public double desiredRent() {
		return(0.3*monthlyIncome);
	}
	
	/********************************************************
	 * Decide whether to buy a house as a buy-to-let investment
	 ********************************************************/
	public boolean decideToBuyBuyToLet(House h, double price) {
		if(price <= bank.getMaxMortgage(this, false)) {
			MortgageApproval mortgage;
			double yield;
			mortgage = bank.requestLoan(this, price, false);
			
			yield = (mortgage.monthlyPayment*12*RENT_PROFIT_MARGIN + houseMarket.housePriceAppreciation()*price)/
					mortgage.downPayment;
			
			if(Math.random() < 1.0/(1.0 + Math.exp(4.5 - yield*24.0))) {
				return(true);
			}
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
		return(housePayments.size() > 1);
	}
	
	public boolean isCollectingRentFrom(House h) {
		return(h.owner == this && h != home && h.resident != null);
	}
	
	public double monthlyDisposableIncome() {
		double di = monthlyIncome;
		for(Map.Entry<House, MortgageApproval> payment : housePayments.entrySet()) {
			if(isCollectingRentFrom(payment.getKey())) {
				di += payment.getValue().monthlyPayment*RENT_PROFIT_MARGIN;
			} else {
				di -= payment.getValue().monthlyPayment;
			}

		}
//		if(!isHomeless()) di -= housePayments.get(home).monthlyPayment;
		
		return(di);
	}
	
	static public double randomInitialAnnuallIncome() {
		return(Math.exp(INCOME_LOG_MEDIAN+INCOME_SHAPE*rand.nextGaussian()));
	}
	
	public double grossAnnualIncome() {
		// TODO: work out tax contributions
		double income = monthlyIncome;
		for(Map.Entry<House,MortgageApproval> payment : housePayments.entrySet()) {
			if(isCollectingRentFrom(payment.getKey())) {
				// profit from rent collection
				income += payment.getValue().monthlyPayment*(1.0+RENT_PROFIT_MARGIN);
			}
		}
		income *=12.0;
		
		return(income);
	}

	///////////////////////////////////////////////
	

	HouseSaleMarket		houseMarket;
	HouseRentalMarket	rentalMarket;
	
	protected double 	bankBalance; // bank plus fund balance
	protected double	monthlyIncome;		// monthly income
	protected House		home; // current home
	protected Map<House, MortgageApproval> 		housePayments = new HashMap<House, MortgageApproval>(); // houses owned
	private boolean		isFirstTimeBuyer;
	Bank				bank;
	protected static Random rand = new Random();
	
	// ---- Parameters
	protected static final double ALPHA = 0.2;
	protected static final double BETA = 0.01;
	protected static final double RENT_PROFIT_MARGIN = 0.0;
	protected static final double P_SELL = 1.0/(7.0*12.0); // monthly probability of selling house
	protected static final double HOUSE_SALE_PRICE_DISCOUNT = 0.95; // monthly discount on price of house for sale
	protected static final double INCOME_LOG_MEDIAN = Math.log(29580); // Source: IFS: living standards, poverty and inequality in the UK (22,938 after taxes) //Math.log(20300); // Source: O.N.S 2011/2012
	protected static final double INCOME_SHAPE = (Math.log(44360) - INCOME_LOG_MEDIAN)/0.6745; // Source: IFS: living standards, poverty and inequality in the UK (75th percentile is 32692 after tax)
	protected static final double COST_OF_RENTING = 600; // Annual psychological cost of renting
	protected static final double FTB_K = 0.005; // Heterogeneity of sensitivity of desire to first-time-buy to cost
	
//	protected static final double INCOME_LOG_95_PERCENTILE = Math.log(66200); // One-tailed percentile. Source: O.N.S. 2011/2012
//	protected static final double INCOME_SHAPE = (INCOME_LOG_95_PERCENTILE-INCOME_LOG_MEDIAN)/1.64; // Shape parameter of lognormal distribution
	
	
}
