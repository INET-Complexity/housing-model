package housing;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

//import ec.util.MersenneTwisterFast;
//import org.apache.commons.math3.random.MersenneTwister;

/**********************************************
 * This represents a household who receives an income, consumes,
 * saves and can buy/sell/let/invest-in houses.
 * 
 * @author daniel, davidrpugh
 *
 **********************************************/
public class Household implements IHouseOwner, Serializable {


    //////////////////////////////////////////////////////////////////////////////////////
    // Model
    //////////////////////////////////////////////////////////////////////////////////////

    private static final long serialVersionUID = -5042897399316333745L;

    private Config    config = Model.config;    // Passes the Model's configuration parameters object to a private field

    public static int bankruptcies = 0;        // TODO: Unused variable... counts bankruptcies, but it's never used!
    
    /********************************************************
     * Constructor.
     *
     * Initialises behaviour (determine whether the household
     * will be a BTL investor). Households start off in social
     * housing and with their 'desired bank balance' in the bank.
     ********************************************************/
    public Household(double age) {
//        bank = Model.bank;
//        houseMarket = Model.housingMarket;
//        rentalMarket = Model.rentalMarket;
        rand = Model.rand;    // Passes the Model's random number generator to a private field of each instance
        home = null;
        isFirstTimeBuyer = true;
        id = ++id_pool;
        lifecycle = new Lifecycle(age);
        behaviour = new HouseholdBehaviour(lifecycle.incomePercentile);
        monthlyEmploymentIncome = lifecycle.annualIncome()/config.constants.MONTHS_IN_YEAR;
        bankBalance = behaviour.desiredBankBalance(this);
        monthlyPropertyIncome = 0.0;
        desiredQuality = 0;
        bankrupt=false;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public House getHome() {
        return home;
    }

    public Map<House, PaymentAgreement> getHousePayments() {
        return housePayments;
    }
    
    /////////////////////////////////////////////////////////
    // House market behaviour
    /////////////////////////////////////////////////////////

    /********************************************************
     * Main simulation step for each household.
     *
     * Receive income, pay rent/mortgage, make consumption decision
     * and make decision to:
     * - buy or rent if in social housing
     * - sell house if owner-occupier
     * - buy/sell/rent out properties if BTL investor
     ********************************************************/
    public void step() {
        double disposableIncome;
//        House  house;
        
        lifecycle.step();
        monthlyEmploymentIncome = lifecycle.annualIncome()/config.constants.MONTHS_IN_YEAR;
        disposableIncome = getMonthlyPostTaxIncome()
                - config.ESSENTIAL_CONSUMPTION_FRACTION * config.GOVERNMENT_INCOME_SUPPORT; // necessary consumption
        for(PaymentAgreement payment : housePayments.values()) {
            disposableIncome -= payment.makeMonthlyPayment();
        }
        
        // --- consume based on disposable income after house payments
        bankBalance += disposableIncome;
        if(isFirstTimeBuyer() || !isInSocialHousing()) bankBalance -= behaviour.desiredConsumptionB(this); //getMonthlyPreTaxIncome(),bankBalance);
        if(bankBalance < 0.0) { // bankrupt behaviour
            bankBalance = 1.0;    // TODO: cash injection for now...
            if (Model.getTime()>1000) {
                if (!bankrupt) bankruptcies += 1;
                bankrupt = true;
            }
        }

        for(House h : housePayments.keySet()) {
            if(h.owner == this) manageHouse(h); // Manage all owned properties

        }
        
        if(isInSocialHousing()) {
            bidForAHome();
        } else if(isRenting()) {
            if(housePayments.get(home).nPayments == 0) { // end of rental period for renter
                endTenancy();
                bidForAHome();
            }            
        } else if(behaviour.isPropertyInvestor()) {
//            for(House h : housePayments.keySet()) {
//                manageHouse(h);
//            }
            if(config.BTL_ENABLED) {
                if(behaviour.decideToBuyBuyToLet(this)) {
                    Model.housingMarket.BTLbid(this, behaviour.btlPurchaseBid(this));
                }
            }        
        } else if(isHomeowner()) {
//            manageHouse(home);
        } else {
            System.out.println("Strange: this household is not a type I recognize");
        }
        
        
//        makeHousingDecision();
    }

    /******************************
     * Decide what to do with a house h owned by the household:
     *  - if the household lives in h, decide whether to sell it
     *  - if h is up for sale, rethink its offer price, and possibly put it up for rent instead (only BTL investors)
     *  - if h is up for rent, rethink the rent demanded
     *
     * @param h a house owned by the household
     *****************************/
    protected void manageHouse(House h) {
        HouseSaleRecord forSale, forRent;
        double newPrice;
        
        forSale = h.getSaleRecord();
        if(forSale != null) { // reprice house for sale
            newPrice = behaviour.rethinkHouseSalePrice(forSale);
            if(newPrice > mortgageFor(h).principal) {
                Model.housingMarket.updateOffer(forSale, newPrice);                        
            } else {
                Model.housingMarket.removeOffer(forSale);
                if(h != home && h.resident == null) {
                    Model.rentalMarket.offer(h, buyToLetRent(h));
                }
            }
        } else if(decideToSellHouse(h)) { // put house on market?
            if(h.isOnRentalMarket()) Model.rentalMarket.removeOffer(h.getRentalRecord());
            putHouseForSale(h);
        }
        
        forRent = h.getRentalRecord();
        if(forRent != null) { // reprice house for rent
            newPrice = behaviour.rethinkBuyToLetRent(forRent);
            Model.rentalMarket.updateOffer(forRent, newPrice);        
        }        
    }

    /******************************************************
     * Having decided to sell house h, decide its initial sale price and put it up in the market.
     *
     * @param h the house being sold
     ******************************************************/
    protected void putHouseForSale(House h) {
        double principal;
        MortgageAgreement mortgage = mortgageFor(h);
        if(mortgage != null) {
            principal = mortgage.principal;
        } else {
            principal = 0.0;
        }
        Model.housingMarket.offer(h, behaviour.initialSalePrice(
                Model.housingMarket.averageSalePrice[h.getQuality()],
                Model.housingMarket.averageDaysOnMarket,
                principal
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
            if(home == sale.house) {
                System.out.println("Strange: I've just bought a house I'm renting out");
            } else {
                endTenancy();
            }
        }
        MortgageAgreement mortgage = Model.bank.requestLoan(this, sale.getPrice(), behaviour.downPayment(this,sale.getPrice()), home == null, sale.house);
        if(mortgage == null) {
            // TODO: need to either provide a way for house sales to fall through or to ensure that pre-approvals are always satisfiable
            System.out.println("Can't afford to buy house: strange");
//            System.out.println("Want "+sale.getPrice()+" but can only get "+bank.getMaxMortgage(this,home==null));
            System.out.println("Bank balance is "+bankBalance);
            System.out.println("Annual income is "+ monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR);
            if(isRenting()) System.out.println("Is renting");
            if(isHomeowner()) System.out.println("Is homeowner");
            if(isInSocialHousing()) System.out.println("Is homeless");
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
//            System.out.println((sale.house.getQuality()-desiredQuality)*1.0/House.Config.N_QUALITY);
            desiredQuality = sale.house.getQuality();
        } else if(sale.house.resident == null) { // put empty buy-to-let house on rental market
            Model.rentalMarket.offer(sale.house, buyToLetRent(sale.house));
//            endOfLettingAgreement(sale.house);
        }
        isFirstTimeBuyer = false;
    }
        
    /********************************************************
     * Do all stuff necessary when this household sells a house
     ********************************************************/
    public void completeHouseSale(HouseSaleRecord sale) {
        MortgageAgreement mortgage = mortgageFor(sale.house);
        bankBalance += sale.getPrice();
        bankBalance -= mortgage.payoff(bankBalance);
        if(sale.house.isOnRentalMarket()) {
            Model.rentalMarket.removeOffer(sale);
        }
        if(mortgage.nPayments == 0) {
            housePayments.remove(sale.house);
        }
        if(sale.house == home) { // move out of home and become (temporarily) homeless
            home.resident = null;
            home = null;
//            bidOnHousingMarket(1.0);
        } else if(sale.house.resident != null) { // evict current renter
            monthlyPropertyIncome -= sale.house.resident.housePayments.get(sale.house).monthlyPayment;
            sale.house.resident.getEvicted();
        }
    }
    
    /********************************************************
     * A BTL investor receives this message when a tenant moves
     * out of one of its buy-to-let houses.
     * 
     * The household simply puts the house back on the rental
     * market.
     ********************************************************/
    @Override
    public void endOfLettingAgreement(House h, PaymentAgreement contract) {
        monthlyPropertyIncome -= contract.monthlyPayment;

        // put house back on rental market
        if(!housePayments.containsKey(h)) {
            System.out.println("Strange: I don't own this house in endOfLettingAgreement");
        }
//        if(h.resident != null) System.out.println("Strange: renting out a house that has a resident");        
//        if(h.resident != null && h.resident == h.owner) System.out.println("Strange: renting out a house that belongs to a homeowner");        
        if(h.isOnRentalMarket()) System.out.println("Strange: got endOfLettingAgreement on house on rental market");
        if(!h.isOnMarket()) Model.rentalMarket.offer(h, buyToLetRent(h));
    }

    /**********************************************************
     * This household moves out of current rented accommodation
     * and becomes homeless (possibly temporarily). Move out,
     * inform landlord and delete rental agreement.
     **********************************************************/
    public void endTenancy() {
        home.owner.endOfLettingAgreement(home, housePayments.get(home));
        housePayments.remove(home);
        home.resident = null;
        home = null;
    //    endOfTenancyAgreement(home, housePayments.remove(home));
    }
    
    /*** Landlord has told this household to get out: leave without informing landlord */
    public void getEvicted() {
        if(home == null) {
            System.out.println("Strange: got evicted but I'm homeless");            
        }
        if(home.owner == this) {
            System.out.println("Strange: got evicted from a home I own");
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
            RentalAgreement rent = new RentalAgreement();
            rent.monthlyPayment = sale.getPrice();
            rent.nPayments = config.TENANCY_LENGTH_AVERAGE
                    + rand.nextInt(2*config.TENANCY_LENGTH_EPSILON + 1) - config.TENANCY_LENGTH_EPSILON;
//            rent.principal = rent.monthlyPayment*rent.nPayments;
            housePayments.put(sale.house, rent);
        }
        if(home != null) System.out.println("Strange: I'm renting a house but not homeless");
        home = sale.house;
        if(sale.house.resident != null) {
            System.out.println("Strange: tenant moving into an occupied house");
            if(sale.house.resident == this) System.out.println("...It's me!");
            if(sale.house.owner == this) System.out.println("...It's my house!");
            if(sale.house.owner == sale.house.resident) System.out.println("...It's a homeowner!");
        }
        sale.house.resident = this;
        desiredQuality = sale.house.getQuality();
    }


    /********************************************************
     * Make the decision whether to bid on the housing market or rental market.
     * This is an "intensity of choice" decision (sigma function)
     * on the cost of renting compared to the cost of owning, with
     * COST_OF_RENTING being an intrinsic psychological cost of not
     * owning. 
     ********************************************************/
    protected void bidForAHome() {
        double maxMortgage = Model.bank.getMaxMortgage(this, true);
        double price = behaviour.desiredPurchasePrice(this, monthlyEmploymentIncome);
        if(behaviour.rentOrPurchaseDecision(this, price)) {
            if(price > maxMortgage - 1.0) {
                price = maxMortgage -1.0;
            }
            Model.housingMarket.bid(this, price);
        } else {
            Model.rentalMarket.bid(this, behaviour.desiredRent(this, monthlyEmploymentIncome));
        }
    }
    
    
    /********************************************************
     * Decide whether to sell ones own house.
     ********************************************************/
    private boolean decideToSellHouse(House h) {
        if(h == home) {
            return(behaviour.decideToSellHome(this));
        }
        if(config.BTL_ENABLED) return(behaviour.decideToSellInvestmentProperty(h, this));
        return(false);
    }

        

    /***
     * Do stuff necessary when BTL investor lets out a rental
     * property
     */
    @Override
    public void completeHouseLet(HouseSaleRecord sale) {
        if(sale.house.isOnMarket()) {
            Model.housingMarket.removeOffer(sale.house.getSaleRecord());
        }
        monthlyPropertyIncome += sale.getPrice();
    }

    public double buyToLetRent(House h) {
        return(behaviour.buyToLetRent(
                Model.rentalMarket.getAverageSalePrice(h.getQuality()), 
                Model.rentalMarket.averageDaysOnMarket,h));
    }

    /////////////////////////////////////////////////////////
    // Inheritance behaviour
    /////////////////////////////////////////////////////////

    /**
     * Implement inheritance: upon death, transfer all wealth to the previously selected household.
     *
     * Take all houses off the markets, evict any tenants, pay off mortgages, and give property and remaining
     * bank balance to the beneficiary.
     * @param beneficiary The household that will inherit the wealth
     */
    public void transferAllWealthTo(Household beneficiary) {
        if(beneficiary == this) System.out.println("Strange: I'm transfering all my wealth to myself");
        boolean isHome;
        Iterator<Entry<House, PaymentAgreement>> paymentIt = housePayments.entrySet().iterator();
        Entry<House, PaymentAgreement> entry;
        House h;
        PaymentAgreement payment;
        while(paymentIt.hasNext()) {
            entry = paymentIt.next();
            h = entry.getKey();
            payment = entry.getValue();
            if(h == home) {
                isHome = true;
                h.resident = null;
                home = null;
            } else {
                isHome = false;
            }
            if(h.owner == this) {
                if(h.isOnRentalMarket()) Model.rentalMarket.removeOffer(h.getRentalRecord());
                if(h.isOnMarket()) Model.housingMarket.removeOffer(h.getSaleRecord());
                if(h.resident != null) h.resident.getEvicted();
                beneficiary.inheritHouse(h, isHome);
            } else {
                h.owner.endOfLettingAgreement(h, housePayments.get(h));
            }
            if(payment instanceof MortgageAgreement) {
                bankBalance -= ((MortgageAgreement) payment).payoff();
            }
            paymentIt.remove();
        }
        beneficiary.bankBalance += Math.max(0.0, bankBalance);
    }
    
    /**
     * Inherit a house.
     *
     * Write off the mortgage for the house. Move into the house if renting or in social housing.
     * 
     * @param h House to inherit
     */
    public void inheritHouse(House h, boolean wasHome) {
        MortgageAgreement nullMortgage = new MortgageAgreement(this,false);
        nullMortgage.nPayments = 0;
        nullMortgage.downPayment = 0.0;
        nullMortgage.monthlyInterestRate = 0.0;
        nullMortgage.monthlyPayment = 0.0;
        nullMortgage.principal = 0.0;
        nullMortgage.purchasePrice = 0.0;
        housePayments.put(h, nullMortgage);
        h.owner = this;
        if(h.resident != null) {
            System.out.println("Strange: inheriting a house with a resident");
        }
        if(!isHomeowner()) {
            // move into house if renting or homeless
            if(isRenting()) {
                endTenancy();                
            }
            home = h;
            h.resident = this;
            desiredQuality = h.getQuality();
        } else if(behaviour.isPropertyInvestor()) {
            if(config.BTL_ENABLED) {
                if(decideToSellHouse(h)) {
                    putHouseForSale(h);
                } else if(h.resident == null) {
                    Model.rentalMarket.offer(h, buyToLetRent(h));
                }
            } else {
                if(wasHome) {
                    putHouseForSale(h);
                } else if(h.resident == null) {
                    Model.rentalMarket.offer(h, buyToLetRent(h));
                }
            }
        } else {
            // I'm an owner-occupier
            if(config.BTL_ENABLED) {
                putHouseForSale(h);
            } else {
                if(wasHome) {
                    putHouseForSale(h);
                } else {
                    behaviour.setPropertyInvestor(true);
                    if(h.resident == null) {
                        Model.rentalMarket.offer(h, buyToLetRent(h));
                    }                    
                }
            }
        }
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

    public boolean isInSocialHousing() {
        return(home == null);
    }

    public boolean isFirstTimeBuyer() {
        return isFirstTimeBuyer;
    }
    
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
    
    /**
     * @return monthly disposable (i.e., after tax) income
     */
    public double getMonthlyPostTaxIncome() {
        return getMonthlyPreTaxIncome()
                - (Model.government.incomeTaxDue(monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR)
                + Model.government.class1NICsDue(monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR))
                / config.constants.MONTHS_IN_YEAR;
    }
    
    /**
     * @return gross monthly total income
     */
    public double getMonthlyPreTaxIncome() {
        double monthlyTotalIncome = (monthlyEmploymentIncome +
                monthlyPropertyIncome + bankBalance * config.RETURN_ON_FINANCIAL_WEALTH);
        return monthlyTotalIncome;
    }
    
    public double annualEmploymentIncome() {
        return monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR;
    }
    
    public int nInvestmentProperties() {
        return(housePayments.size()-1);
    }
    
    /***
     * @return Current mark-to-market equity in this household's home.
     */
    public double getHomeEquity() {
        if(!isHomeowner()) return(0.0);
        return(Model.housingMarket.getAverageSalePrice(home.getQuality()) - mortgageFor(home).principal);
    }
    
    public MortgageAgreement mortgageFor(House h) {
        PaymentAgreement payment = housePayments.get(h);
        if(payment instanceof MortgageAgreement) {
            return((MortgageAgreement)payment);
        }
        return(null);
    }

    public double monthlyPaymentOn(House h) {
        PaymentAgreement payment = housePayments.get(h);
        if(payment != null) {
            return(payment.monthlyPayment);
        }
        return(0.0);        
    }
    
    ///////////////////////////////////////////////

//    HouseSaleMarket        houseMarket;
//    HouseRentalMarket    rentalMarket;
//    Bank                bank;

    public double monthlyEmploymentIncome;
    private double bankBalance;
    private House home;
    private Map<House, PaymentAgreement> housePayments = new TreeMap<House, PaymentAgreement>(); // houses owned
    double monthlyPropertyIncome;
    private boolean isFirstTimeBuyer;
//    public    double        desiredPropertyInvestmentFraction;
    public int id;        // only to ensure deterministic execution
    private Model.MersenneTwister rand;        // Private field to contain the Model's random number generator
    
    public Lifecycle lifecycle;    // lifecycle plugin
    public HouseholdBehaviour behaviour;
    public int desiredQuality;
    
//    static Diagnostics    diagnostics = new Diagnostics(Model.households);
    static int id_pool;
    boolean bankrupt;

    /*
     * Second step in a time-step. At this point, the
     * household may have sold their house, but not managed
     * to buy a new one, so must enter the rental market.
     * 
     * This is also where investors get to bid for buy-to-let
     * housing.
     ********************************************************/
//    public void preRentalClearingStep() {
//    }
    
    /*
     *  Make decision to buy/sell houses
     ********************************************************/
    /*
    void makeHousingDecision() {
        // --- add and manage houses for sale
        HouseSaleRecord forSale, forRent;
        double newPrice;
        
        for(House h : housePayments.keySet()) {
            if(h.owner == this) {
                forSale = h.getSaleRecord();
                if(forSale != null) { // reprice house for sale
                    newPrice = behaviour.rethinkHouseSalePrice(forSale);
                    if(newPrice > mortgageFor(h).principal) {
                        houseMarket.updateOffer(forSale, newPrice);                        
                    } else {
                        houseMarket.removeOffer(forSale);
                        if(h != home && h.resident == null) {
                            rentalMarket.offer(h, buyToLetRent(h));
                        }
                    }
                } else if(decideToSellHouse(h)) { // put house on market?
                    if(h.isOnRentalMarket()) rentalMarket.removeOffer(h.getRentalRecord());
                    putHouseForSale(h);
                }
                
                forRent = h.getRentalRecord();
                if(forRent != null) {
                    newPrice = behaviour.rethinkBuyToLetRent(forRent);
                    rentalMarket.updateOffer(forRent, newPrice);        
                }
            }
        }        
    }
    */
    /*
     * This gets called when we are a renter and a tenancy
     * agreement has come to an end. Move out and inform landlord.
     * Don't delete rental agreement because we're probably iterating over payments.
     */
    /*
    public void endOfTenancyAgreement(House house, PaymentAgreement rentalContract) {
        if(home == null) System.out.println("Strange: paying rent and homeless");
        if(house != home) System.out.println("Strange: I seem to have been renting a house but not living in it");
        if(home.resident != this) System.out.println("home/resident link is broken");
        house.owner.endOfLettingAgreement(house, rentalContract);        
        home.resident = null;
        home = null;
    }
    */
    /*
     * Decide whether to buy a house as a buy-to-let investment
     ********************************************************/
//    public boolean decideToBuyBuyToLet(House h, double price) {
//        return(behaviour.decideToBuyBuyToLet(h, this, price));
//    }
    /////////////////////////////////////////////////////////
    // Homeowner helper stuff
    /////////////////////////////////////////////////////////

    /*
     * Put a bid on the housing market if this household can afford a
     * mortgage at its desired price.
     * 
     * @param p The probability that the household will actually bid,
     * given that it can afford a mortgage.
     ****************************************/
    /*
    protected void bidOnHousingMarket(double p) {
        double desiredPrice = behaviour.desiredPurchasePrice(getMonthlyPreTaxIncome(), houseMarket.housePriceAppreciation());
        double maxMortgage = bank.getMaxMortgage(this, true);
//        double ltiConstraint =  annualEmploymentIncome * bank.loanToIncome(isFirstTimeBuyer(),true)/bank.loanToValue(isFirstTimeBuyer(), true); // ##### TEST #####
//        if(desiredPrice > ltiConstraint) desiredPrice = ltiConstraint - 1.0; // ##### TEST #####
        if(desiredPrice >= maxMortgage) desiredPrice = maxMortgage - 1;
//        desiredPrice = maxMortgage-1; // ####################### TEST!!!!!
        if(desiredPrice <= maxMortgage) {
            if(p<1.0) {
                if(rand.nextDouble() < p) houseMarket.bid(this, desiredPrice);
            } else {
                // no need to call random if p = 1.0
                houseMarket.bid(this, desiredPrice);                
            }
        }
    }
    */
//    public boolean isCollectingRentFrom(House h) {
//    return(h.owner == this && h != home && h.resident != null);
//}

    //////////////////////////////////////////////////////////////////
    // Fraction of property+financial wealth that I want to invest
    // in buy-to-let housing
    //////////////////////////////////////////////////////////////////
//    public void setDesiredPropertyInvestmentFraction(double val) {
//        this.desiredPropertyInvestmentFraction = val;
//    }

    /////////////////////////////////////////////////////////////////
    // Current valuation of buy-to-let properties, not including
    // houses up for sale.
    /////////////////////////////////////////////////////////////////
    /*
    public double getPropertyInvestmentValuation() {
        double valuation = 0.0;
        for(House h : housePayments.keySet()) {
            if(h.owner == this && h != home && !h.isOnMarket()) {
                valuation += houseMarket.getAverageSalePrice(h.getQuality());
            }
        }
        return(valuation);
    }
    */
    ///////////////////////////////////////////////////////////////
    // returns current desired cash value of buy-to-let property investment
    //////////////////////////////////////////////////////////////
//    public double getDesiredPropertyInvestmentValue() {
//        return(desiredPropertyInvestmentFraction * (getPropertyInvestmentValuation() + bankBalance));
//    }


}
