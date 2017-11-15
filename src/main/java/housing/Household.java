package housing;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.random.MersenneTwister;

/**************************************************************************************************
 * This represents a household who receives an income, consumes, saves and can buy, sell, let, and
 * invest in houses.
 *
 * @author daniel, davidrpugh, Adrian Carro
 *
 *************************************************************************************************/

public class Household implements IHouseOwner, Serializable {
    private static final long   serialVersionUID = -5042897399316333745L;

    //------------------//
    //----- Fields -----//
    //------------------//

    private static int          id_pool;

    public int                  id; // Only used for identifying households within the class MicroDataRecorder
    public double               monthlyEmploymentIncome;
    public HouseholdBehaviour   behaviour; // Behavioural plugin

    double                      incomePercentile; // Fixed for the whole lifetime of the household

    private House                           home;
    private Map<House, PaymentAgreement>    housePayments = new TreeMap<>(); // Houses owned and their payment agreements
    private Config                          config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister                 rand; // Private field to contain the Model's random number generator
    private double                          age; // Age of the household representative person
    private double                          bankBalance;
    private double                          monthlyPropertyIncome; // TODO: Check how this is computed and make sure it is OK
    private boolean                         isFirstTimeBuyer;
    private boolean                         isBankrupt;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /********************************************************
     * Initialises behaviour (determine whether the household
     * will be a BTL investor). Households start off in social
     * housing and with their 'desired bank balance' in the bank.
     ********************************************************/
    public Household(double householdAgeAtBirth) {
        rand = Model.rand;    // Passes the Model's random number generator to a private field of each instance
        home = null;
        isFirstTimeBuyer = true;
        isBankrupt = false;
        id = ++id_pool;
        age = householdAgeAtBirth;
        incomePercentile = rand.nextDouble();
        behaviour = new HouseholdBehaviour(incomePercentile);
        monthlyEmploymentIncome = annualIncome()/config.constants.MONTHS_IN_YEAR;
        bankBalance = behaviour.getDesiredBankBalance(this); // Desired bank balance is used as initial value for actual bank balance
        monthlyPropertyIncome = 0.0;
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

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

        isBankrupt = false; // Delete bankruptcies from previous time step
        age += 1.0/config.constants.MONTHS_IN_YEAR;
        monthlyEmploymentIncome = annualIncome()/config.constants.MONTHS_IN_YEAR;
        disposableIncome = getMonthlyPostTaxIncome()
                - config.ESSENTIAL_CONSUMPTION_FRACTION * config.GOVERNMENT_INCOME_SUPPORT; // necessary consumption
        for(PaymentAgreement payment: housePayments.values()) {
            disposableIncome -= payment.makeMonthlyPayment();
        }

        // --- consume based on disposable income after house payments
        // TODO: What? Does this mean only FTB consume?
        bankBalance += disposableIncome;
        // TODO: What is the purpose of this if condition?
        if(isFirstTimeBuyer() || !isInSocialHousing()) bankBalance -= behaviour.getDesiredConsumption(this);
        // TODO: Need to improve bankruptcy procedures further than current simple cash injection
        if(bankBalance < 0.0) { // Behaviour if household is bankrupt
            bankBalance = 1.0;
            isBankrupt = true;
        }
        for(House h : housePayments.keySet()) {
            if(h.owner == this) manageHouse(h); // Manage all owned properties
        }
        
        if(isInSocialHousing()) {
            bidForAHome(); // When BTL households are born, they enter here the first time!
        } else if(isRenting()) {
            if(housePayments.get(home).nPayments == 0) { // end of rental period for renter
                endTenancy();
                bidForAHome();
            }            
        } else if(behaviour.isPropertyInvestor()) {
            if(behaviour.decideToBuyBuyToLet(this)) {
                Model.houseSaleMarket.BTLbid(this, behaviour.btlPurchaseBid(this));
            }
        } else if (!isHomeowner()){
            System.out.println("Strange: this household is not a type I recognize");
        }
    }

    /***
     * @return Household income given age and percentile of population
     */
    private double annualIncome() {
        double boundAge = age;
        if(boundAge < data.Lifecycle.lnIncomeGivenAge.getSupportLowerBound()) {
            boundAge = data.Lifecycle.lnIncomeGivenAge.getSupportLowerBound();
        }
        else if(boundAge > data.Lifecycle.lnIncomeGivenAge.getSupportUpperBound()) {
            boundAge = data.Lifecycle.lnIncomeGivenAge.getSupportUpperBound() - 1e-7;
        }
        double income = data.Lifecycle.lnIncomeGivenAge.getBinAt(boundAge).inverseCumulativeProbability(incomePercentile);
        income = Math.exp(income);
        if(income < config.GOVERNMENT_INCOME_SUPPORT) income = config.GOVERNMENT_INCOME_SUPPORT; // minimum income is govt. support
        return(income);
    }

    /******************************
     * Decide what to do with a house h owned by the household:
     *  - if the household lives in h, decide whether to sell it
     *  - if h is up for sale, rethink its offer price, and possibly put it up for rent instead (only BTL investors)
     *  - if h is up for rent, rethink the rent demanded
     *
     * @param h a house owned by the household
     *****************************/
    private void manageHouse(House h) {
        HouseSaleRecord forSale, forRent;
        double newPrice;
        
        forSale = h.getSaleRecord();
        if(forSale != null) { // reprice house for sale
            newPrice = behaviour.rethinkHouseSalePrice(forSale);
            if(newPrice > mortgageFor(h).principal) {
                Model.houseSaleMarket.updateOffer(forSale, newPrice);
            } else {
                Model.houseSaleMarket.removeOffer(forSale);
                // TODO: First condition is redundant!
                if(h != home && h.resident == null) {
                    Model.houseRentalMarket.offer(h, buyToLetRent(h));
                }
            }
        } else if(decideToSellHouse(h)) { // put house on market?
            if(h.isOnRentalMarket()) Model.houseRentalMarket.removeOffer(h.getRentalRecord());
            putHouseForSale(h);
        }
        
        forRent = h.getRentalRecord();
        if(forRent != null) { // reprice house for rent
            newPrice = behaviour.rethinkBuyToLetRent(forRent);
            Model.houseRentalMarket.updateOffer(forRent, newPrice);
        }        
    }

    /******************************************************
     * Having decided to sell house h, decide its initial sale price and put it up in the market.
     *
     * @param h the house being sold
     ******************************************************/
    private void putHouseForSale(House h) {
        double principal;
        MortgageAgreement mortgage = mortgageFor(h);
        if(mortgage != null) {
            principal = mortgage.principal;
        } else {
            principal = 0.0;
        }
        Model.houseSaleMarket.offer(h, behaviour.getInitialSalePrice(h.getQuality(), principal));
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
    void completeHousePurchase(HouseSaleRecord sale) {
        if(isRenting()) { // give immediate notice to landlord and move out
            if(sale.house.resident != null) System.out.println("Strange: my new house has someone in it!");
            if(home == sale.house) {
                System.out.println("Strange: I've just bought a house I'm renting out");
            } else {
                endTenancy();
            }
        }
        MortgageAgreement mortgage = Model.bank.requestLoan(this, sale.getPrice(), behaviour.decideDownPayment(this,sale.getPrice()), home == null, sale.house);
        if(mortgage == null) {
            // TODO: need to either provide a way for house sales to fall through or to ensure that pre-approvals are always satisfiable
            System.out.println("Can't afford to buy house: strange");
            System.out.println("Bank balance is "+bankBalance);
            System.out.println("Annual income is "+ monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR);
            if(isRenting()) System.out.println("Is renting");
            if(isHomeowner()) System.out.println("Is homeowner");
            if(isInSocialHousing()) System.out.println("Is homeless");
            if(isFirstTimeBuyer()) System.out.println("Is firsttimebuyer");
            if(behaviour.isPropertyInvestor()) System.out.println("Is investor");
            System.out.println("House owner = "+sale.house.owner);
            System.out.println("me = "+this);
        } else {
            bankBalance -= mortgage.downPayment;
            housePayments.put(sale.house, mortgage);
            if (home == null) { // move in to house
                home = sale.house;
                sale.house.resident = this;
            } else if (sale.house.resident == null) { // put empty buy-to-let house on rental market
                Model.houseRentalMarket.offer(sale.house, buyToLetRent(sale.house));
            }
            isFirstTimeBuyer = false;
        }
    }

    /********************************************************
     * Do all stuff necessary when this household sells a house
     ********************************************************/
    public void completeHouseSale(HouseSaleRecord sale) {
        MortgageAgreement mortgage = mortgageFor(sale.house);
        bankBalance += sale.getPrice();
        bankBalance -= mortgage.payoff(bankBalance);
        if(sale.house.isOnRentalMarket()) {
            Model.houseRentalMarket.removeOffer(sale);
        }
        // TODO: Warning, if bankBalance is not enough to pay mortgage back, then the house stays in housePayments, consequences to be checked!
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
        if(!h.isOnMarket()) Model.houseRentalMarket.offer(h, buyToLetRent(h));
    }

    /**********************************************************
     * This household moves out of current rented accommodation
     * and becomes homeless (possibly temporarily). Move out,
     * inform landlord and delete rental agreement.
     **********************************************************/
    private void endTenancy() {
        home.owner.endOfLettingAgreement(home, housePayments.get(home));
        housePayments.remove(home);
        home.resident = null;
        home = null;
    //    endOfTenancyAgreement(home, housePayments.remove(home));
    }
    
    /*** Landlord has told this household to get out: leave without informing landlord */
    private void getEvicted() {
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
    void completeHouseRental(HouseSaleRecord sale) {
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
    }


    /********************************************************
     * Make the decision whether to bid on the housing market or rental market.
     * This is an "intensity of choice" decision (sigma function)
     * on the cost of renting compared to the cost of owning, with
     * COST_OF_RENTING being an intrinsic psychological cost of not
     * owning. 
     ********************************************************/
    private void bidForAHome() {
        // Find household's desired housing expenditure
        double price = behaviour.getDesiredPurchasePrice(monthlyEmploymentIncome);
        // Cap this expenditure to the maximum mortgage available to the household
        price = Math.min(price, Model.bank.getMaxMortgage(this, true));
        // Compare costs to decide whether to buy or rent...
        if(behaviour.decideRentOrPurchase(this, price)) {
            // ... if buying, bid in the house sale market for the capped desired price
            Model.houseSaleMarket.bid(this, price);
        } else {
            // ... if renting, bid in the house rental market for the desired rent price
            Model.houseRentalMarket.bid(this, behaviour.desiredRent(this, monthlyEmploymentIncome));
        }
    }
    
    
    /********************************************************
     * Decide whether to sell ones own house.
     ********************************************************/
    private boolean decideToSellHouse(House h) {
        if(h == home) {
            return(behaviour.decideToSellHome());
        } else {
            return(behaviour.decideToSellInvestmentProperty(h, this));
        }
    }



    /***
     * Do stuff necessary when BTL investor lets out a rental
     * property
     */
    @Override
    public void completeHouseLet(HouseSaleRecord sale) {
        if(sale.house.isOnMarket()) {
            Model.houseSaleMarket.removeOffer(sale.house.getSaleRecord());
        }
        monthlyPropertyIncome += sale.getPrice();
    }

    private double buyToLetRent(House h) {
        return(behaviour.buyToLetRent(
                Model.rentalMarketStats.getExpAvSalePriceForQuality(h.getQuality()),
                Model.rentalMarketStats.getExpAvDaysOnMarket(), h));
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
    void transferAllWealthTo(Household beneficiary) {
        if(beneficiary == this) System.out.println("Strange: I'm transferring all my wealth to myself");
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
                if(h.isOnRentalMarket()) Model.houseRentalMarket.removeOffer(h.getRentalRecord());
                if(h.isOnMarket()) Model.houseSaleMarket.removeOffer(h.getSaleRecord());
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
    private void inheritHouse(House h, boolean wasHome) {
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
        } else if(behaviour.isPropertyInvestor()) {
            if(decideToSellHouse(h)) {
                putHouseForSale(h);
            } else if(h.resident == null) {
                Model.houseRentalMarket.offer(h, buyToLetRent(h));
            }
        } else {
            // I'm an owner-occupier
            putHouseForSale(h);
        }
    }
    
    /////////////////////////////////////////////////////////
    // Helpers
    /////////////////////////////////////////////////////////

    public double getAge() { return age; }

    public boolean isHomeowner() {
        if(home == null) return(false);
        return(home.owner == this);
    }

    public boolean isRenting() {
        if(home == null) return(false);
        return(home.owner != this);
    }

    public boolean isInSocialHousing() { return home == null; }

    boolean isFirstTimeBuyer() { return isFirstTimeBuyer; }

    public boolean isBankrupt() { return isBankrupt; }

    public double getBankBalance() { return bankBalance; }

    public House getHome() { return home; }

    public Map<House, PaymentAgreement> getHousePayments() { return housePayments; }
    
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
    double getMonthlyPostTaxIncome() {
        return getMonthlyPreTaxIncome()
                - (Model.government.incomeTaxDue(monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR)
                + Model.government.class1NICsDue(monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR))
                / config.constants.MONTHS_IN_YEAR;
    }
    
    /**
     * @return gross monthly total income
     */
    public double getMonthlyPreTaxIncome() {
        return (monthlyEmploymentIncome + monthlyPropertyIncome +
                bankBalance * config.RETURN_ON_FINANCIAL_WEALTH);
    }
    
    public double annualEmploymentIncome() { return monthlyEmploymentIncome*config.constants.MONTHS_IN_YEAR; }
    
    public int nInvestmentProperties() { return housePayments.size() - 1; }
    
    /***
     * @return Current mark-to-market (with exponentially averaged prices per quality) equity in this household's home.
     */
    double getHomeEquity() {
        if(!isHomeowner()) return(0.0);
        return Model.housingMarketStats.getExpAvSalePriceForQuality(home.getQuality())
                - mortgageFor(home).principal;
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
}
