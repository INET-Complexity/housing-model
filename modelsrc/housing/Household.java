package housing;

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


	//////////////////////////////////////////////////////////////////////////////////////
	// Model
	//////////////////////////////////////////////////////////////////////////////////////

	/********************************************************
	 * Constructor.
	 ********************************************************/
	public Household(double age) {
		bank = Model.bank;
		houseMarket = Model.housingMarket;
		rentalMarket = Model.rentalMarket;
		rand = Model.rand;
		home = null;
		isFirstTimeBuyer = true;
		id = ++id_pool;
		lifecycle = new Lifecycle(age);
		behaviour = new HouseholdBehaviour(lifecycle.incomePercentile);
		annualEmploymentIncome = lifecycle.annualIncome();
		bankBalance = Math.exp(4.07*Math.log(annualEmploymentIncome)-33.1);
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
				if(h.isOnRentalMarket()) rentalMarket.removeOffer(h.getRentalRecord());
				if(h.isOnMarket()) houseMarket.removeOffer(h.getSaleRecord());
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
		nullMortgage.purchasePrice = 0.0;
		housePayments.put(h, nullMortgage);
		h.owner = this;
		if(!isHomeowner()) {
			// move into house if not already a homeowner
			if(isRenting()) {
				home.owner.endOfLettingAgreement(home);
				endTenancy();
			}
			if(h.resident != null) {
				h.resident.endTenancy();
			}
			home = h;
			h.resident = this;
		} else if(behaviour.isPropertyInvestor()) {
			if(decideToSellHouse(h)) {
				putHouseForSale(h);
			} else if(h.resident == null) {
				endOfLettingAgreement(h); // put inherited house on rental market
			}
		} else {
			putHouseForSale(h);
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
		bankBalance += disposableIncome - behaviour.desiredConsumptionB(getMonthlyEmploymentIncome(),bankBalance);
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
			rentalMarket.bid(this, behaviour.desiredRent(getMonthlyEmploymentIncome()));
		} else if(behaviour.isPropertyInvestor()) { // this is a buy-to-let investor
			houseMarket.bid(this, bank.getMaxMortgage(this, false));
		}
	}
	
	/********************************************************
	 *  Make decision to buy/sell houses
	 ********************************************************/
	void makeHousingDecision() {
		// --- add and manage houses for sale
		HouseSaleRecord forSale, forRent;
		double newPrice;
		
		for(House h : housePayments.keySet()) {
			if(h.owner == this) {
				forSale = h.getSaleRecord();
				if(forSale != null) { // reprice house for sale
					newPrice = behaviour.rethinkHouseSalePrice(forSale);
					if(newPrice > housePayments.get(h).principal) {
						houseMarket.updateOffer(h.getSaleRecord(), newPrice);						
					} else {
						houseMarket.removeOffer(h.getSaleRecord());
						if(h != home && h.resident == null) {
							rentalMarket.offer(h, buyToLetRent(h));
						}
					}
				} else if(decideToSellHouse(h)) { // put house on market
					putHouseForSale(h);
				}
				
				forRent = h.getRentalRecord();
				if(forRent != null) {
					newPrice = behaviour.rethinkBuyToLetRent(forRent);
					rentalMarket.updateOffer(h.getRentalRecord(), newPrice);		
				}
			}
		}
		
		// ---- try to buy house?
		if(!isHomeowner()) {
			decideToStopRenting();
		}
	}

	protected void putHouseForSale(House h) {
		houseMarket.offer(h, behaviour.initialSalePrice(
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
		MortgageApproval mortgage = bank.requestLoan(this, sale.price, behaviour.downPayment(bankBalance), home == null);
		if(mortgage == null) {
			// TODO: need to either provide a way for house sales to fall through or to
			// TODO: ensure that pre-approvals are always satisfiable
			System.out.println("Can't afford to buy house: strange");
			System.out.println("Want "+sale.price+" but can only get "+bank.getMaxMortgage(this,home==null));
			System.out.println("Bank balance is "+bankBalance+". DisposableIncome is "+ getMonthlyDiscretionaryIncome());
			System.out.println("Annual income is "+ getMonthlyEmploymentIncome() *12.0);
			if(isRenting()) System.out.println("Is renting");
			if(isHomeowner()) System.out.println("Is homeowner");
			if(isHomeless()) System.out.println("Is homeless");
			if(isFirstTimeBuyer()) System.out.println("Is firsttimebuyer");
			if(behaviour.isPropertyInvestor()) System.out.println("Is investor");
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
		double profit = sale.price - housePayments.get(sale.house).payoff(bankBalance+sale.price);
		if(profit < 0) System.out.println("Strange: Profit is negative.");
		bankBalance += profit;
		if(sale.house.isOnMarket()) {
			rentalMarket.removeOffer(sale);
		}
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
			if(h.isOnRentalMarket()) System.out.println("Strange: got endOfLettingAgreement on house on rental market");
			rentalMarket.offer(h, buyToLetRent(h));
	}

	/**********************************************************
	 * This household moves out of current rented accommodation
	 * and becomes homeless (possibly temporarily)
	 **********************************************************/
	public void endTenancy() {
		if(home.owner == this) {
			System.out.println("Strange: got endTenancy on a home I own");
		}
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
			rent.monthlyPayment = sale.price;
			rent.monthlyInterestRate = 0.0;
			rent.nPayments = (int)(12.0*rand.nextDouble()+1);
			rent.principal = rent.monthlyPayment*rent.nPayments;
			rent.purchasePrice = 0.0;
			housePayments.put(sale.house, rent);
		}
		if(home != null) System.out.println("Strange: I'm renting a house but not homeless");
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
		double desiredPrice = behaviour.desiredPurchasePrice(getMonthlyTotalIncome(), houseMarket.housePriceAppreciation());
		double maxMortgage = bank.getMaxMortgage(this, true);
		double ltiConstraint =  annualEmploymentIncome * bank.loanToIncome(this,true)/bank.loanToValue(this, true); // ##### TEST #####
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
		double housePrice = behaviour.desiredPurchasePrice(getMonthlyTotalIncome(), houseMarket.housePriceAppreciation());
		double maxMortgage = bank.getMaxMortgage(this, true);
		double ltiConstraint =  annualEmploymentIncome * bank.loanToIncome(this,true)/bank.loanToValue(this, true); // ##### TEST #####
		if(housePrice > ltiConstraint) housePrice = ltiConstraint - 1.0; // ##### TEST #####
		if(housePrice <= maxMortgage) {
			if(home != null) {
				costOfRent = housePayments.get(home).monthlyPayment*12;
			} else {
				costOfRent = rentalMarket.averageSalePrice[0]*12;
			}
			if(behaviour.renterPurchaseDecision(this, housePrice, costOfRent)) {
				houseMarket.bid(this, housePrice);
			}
		}
	}
	
	
	/********************************************************
	 * Decide whether to sell ones own house.
	 ********************************************************/
	private boolean decideToSellHouse(House h) {
		if(h == home) {
			return(behaviour.decideToSellHome());
		}
		return(behaviour.decideToSellInvestmentProperty(h, this));
	}

		
	/********************************************************
	 * Decide whether to buy a house as a buy-to-let investment
	 ********************************************************/
	public boolean decideToBuyBuyToLet(House h, double price) {
		return(behaviour.decideToBuyBuyToLet(h, this, price));
	}

	@Override
	public void completeHouseLet(House house) {
		if(house.isOnMarket()) {
			houseMarket.removeOffer(house.getSaleRecord());
		}
	}

	public double buyToLetRent(House h) {
		return(behaviour.buyToLetRent(
				rentalMarket.getAverageSalePrice(h.quality), 
				rentalMarket.averageDaysOnMarket,
				housePayments.get(h).monthlyPayment));
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
			if(h.owner == this && h != home && !h.isOnMarket()) {
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

	/***
	 * @return Number of properties this household currently has on the sale market
	 */
	public int nPropertiesForSale() {
		int n=0;
		for(House h : housePayments.keySet()) {
			if(h.isOnMarket()) ++n;
		}
		return(n);
	}
	
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
		return bankBalance * RETURN_ON_FINANCIAL_WEALTH;
	}

	/**
	 * @return gross property income will be zero for most households
	 */
	public double getMonthlyPropertyIncome() {
		double propertyIncome = 0.0;
		House h;
		for (Map.Entry<House, MortgageApproval> payment : housePayments.entrySet()) {
			h = payment.getKey();
			if (isCollectingRentFrom(h)) {
				propertyIncome += h.resident.housePayments.get(h).monthlyPayment;
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
	
	public static double RETURN_ON_FINANCIAL_WEALTH = 0.002; // monthly percentage growth of financial investements

	HouseSaleMarket		houseMarket;
	HouseRentalMarket	rentalMarket;

	protected double 	annualEmploymentIncome;
	protected double 	bankBalance;
	protected House		home; // current home
	protected Map<House, MortgageApproval> 		housePayments = new TreeMap<House, MortgageApproval>(); // houses owned
	private boolean		isFirstTimeBuyer;
//	public	double		desiredPropertyInvestmentFraction;
	Bank				bank;
	public int		 	id;		// only to ensure deterministic execution
	protected MersenneTwisterFast 	rand;
	
	public Lifecycle	lifecycle;	// lifecycle plugin
	public IHouseholdBehaviour behaviour;
	
//	static Diagnostics	diagnostics = new Diagnostics(Model.households);
	static int		 id_pool;


}
