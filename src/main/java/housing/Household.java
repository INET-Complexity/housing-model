package housing;

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

public class Household implements IHouseOwner {

    //------------------//
    //----- Fields -----//
    //------------------//

    private static int          id_pool;

    public int                  id; // Only used for identifying households within the class MicroDataRecorder
    private double              annualGrossEmploymentIncome;
    private double              monthlyGrossEmploymentIncome;
    public HouseholdBehaviour   behaviour; // Behavioural plugin

    double                      incomePercentile; // Fixed for the whole lifetime of the household

    private House                           home;
    private Map<House, PaymentAgreement>    housePayments = new TreeMap<>(); // Houses owned and their payment agreements
    private Config                          config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister                 prng;
    private double                          age; // Age of the household representative person
    private double                          bankBalance;
    private double                          monthlyGrossRentalIncome; // Keeps track of monthly rental income, as only tenants keep a reference to the rental contract, not landlords
    private boolean                         isFirstTimeBuyer;
    private boolean                         isBankrupt;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /**
     * Initialises behaviour (determine whether the household will be a BTL investor). Households start off in social
     * housing and with their "desired bank balance" in the bank
     */
    public Household(MersenneTwister prng) {
        this.prng = prng; // Passes the Model's random number generator to a private field of each instance
        home = null;
        isFirstTimeBuyer = true;
        isBankrupt = false;
        id = ++id_pool;
        age = data.Demographics.pdfHouseholdAgeAtBirth.nextDouble(this.prng);
        incomePercentile = this.prng.nextDouble();
        behaviour = new HouseholdBehaviour(this.prng, incomePercentile);
        // Find initial values for the annual and monthly gross employment income
        annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(age, incomePercentile);
        monthlyGrossEmploymentIncome = annualGrossEmploymentIncome/config.constants.MONTHS_IN_YEAR;
        bankBalance = behaviour.getDesiredBankBalance(getAnnualGrossTotalIncome()); // Desired bank balance is used as initial value for actual bank balance
        monthlyGrossRentalIncome = 0.0;
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- General methods -----//

    /**
     * Main simulation step for each household. They age, receive employment and other forms of income, make their rent
     * or mortgage payments, perform an essential consumption, make non-essential consumption decisions, manage their
     * owned properties, and make their housing decisions depending on their current housing state:
     * - Buy or rent if in social housing
     * - Sell house if owner-occupier
     * - Buy/sell/rent out properties if BTL investor
     */
    public void step() {
        isBankrupt = false; // Delete bankruptcies from previous time step
        age += 1.0/config.constants.MONTHS_IN_YEAR;
        // Update annual and monthly gross employment income
        annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(age, incomePercentile);
        monthlyGrossEmploymentIncome = annualGrossEmploymentIncome/config.constants.MONTHS_IN_YEAR;
        // Add monthly disposable income (net total income minus essential consumption and housing expenses) to bank balance
        bankBalance += getMonthlyDisposableIncome();
        // Consume based on monthly disposable income (after essential consumption and house payments have been subtracted)
        bankBalance -= behaviour.getDesiredConsumption(getBankBalance(), getAnnualGrossTotalIncome()); // Old implementation: if(isFirstTimeBuyer() || !isInSocialHousing()) bankBalance -= behaviour.getDesiredConsumption(getBankBalance(), getAnnualGrossTotalIncome());
        // Deal with bankruptcies
        // TODO: Improve bankruptcy procedures (currently, simple cash injection), such as terminating contracts!
        if (bankBalance < 0.0) {
            bankBalance = 1.0;
            isBankrupt = true;
        }
        // Manage owned properties and close debts on previously owned properties. To this end, first, create an
        // iterator over the house-paymentAgreement pairs at the household's housePayments object
        Iterator<Entry<House, PaymentAgreement>> paymentIt = housePayments.entrySet().iterator();
        Entry<House, PaymentAgreement> entry;
        House h;
        PaymentAgreement payment;
        // Iterate over these house-paymentAgreement pairs...
        while (paymentIt.hasNext()) {
            entry = paymentIt.next();
            h = entry.getKey();
            payment = entry.getValue();
            // ...if the household is the owner of the house, then manage it
            if (h.owner == this) {
                manageHouse(h);
            // ...otherwise, if the household is not the owner nor the resident, then it is an old debt due to
            // the household's inability to pay the remaining principal off after selling a property...
            } else if (h.resident != this) {
                MortgageAgreement mortgage = (MortgageAgreement) payment;
                // ...remove this type of houses from payments as soon as the household pays the debt off
                if ((payment.nPayments == 0) & (mortgage.principal == 0.0)) {
                    paymentIt.remove();
                }
            }
        }
        // Make housing decisions depending on current housing state
        if (isInSocialHousing()) {
            bidForAHome(); // When BTL households are born, they enter here the first time and until they manage to buy a home!
        } else if (isRenting()) {
            if (housePayments.get(home).nPayments == 0) { // End of rental period for this tenant
                endTenancy();
                bidForAHome();
            }            
        } else if (behaviour.isPropertyInvestor()) { // Only BTL investors who already own a home enter here
            double price = behaviour.btlPurchaseBid(this);
            Model.householdStats.countBTLBidsAboveExpAvSalePrice(price);
            if (behaviour.decideToBuyInvestmentProperty(this)) {
                Model.houseSaleMarket.BTLbid(this, price);
            }
        } else if (!isHomeowner()){
            System.out.println("Strange: this household is not a type I recognize");
        }
    }

    /**
     * Subtracts the essential, necessary consumption and housing expenses (mortgage and rental payments) from the net
     * total income (employment income, property income, financial returns minus taxes)
     */
    private double getMonthlyDisposableIncome() {
        // Start with net monthly income
        double monthlyDisposableIncome = getMonthlyNetTotalIncome();
        // Subtract essential, necessary consumption
        // TODO: ESSENTIAL_CONSUMPTION_FRACTION is not explained in the paper, all support is said to be consumed
        monthlyDisposableIncome -= config.ESSENTIAL_CONSUMPTION_FRACTION*config.GOVERNMENT_MONTHLY_INCOME_SUPPORT;
        // Subtract housing consumption
        for(PaymentAgreement payment: housePayments.values()) {
            monthlyDisposableIncome -= payment.makeMonthlyPayment();
        }
        return monthlyDisposableIncome;
    }

    /**
     * Subtracts the monthly aliquot part of all due taxes from the monthly gross total income. Note that only income
     * tax on employment income and national insurance contributions are implemented!
     */
    double getMonthlyNetTotalIncome() {
        // TODO: Note that this implies there is no tax on rental income nor on bank balance returns
        return getMonthlyGrossTotalIncome()
                - (Model.government.incomeTaxDue(annualGrossEmploymentIncome)   // Employment income tax
                + Model.government.class1NICsDue(annualGrossEmploymentIncome))  // National insurance contributions
                /config.constants.MONTHS_IN_YEAR;
    }

    /**
     * Adds up all sources of (gross) income on a monthly basis: employment, property, returns on financial wealth
     */
    public double getMonthlyGrossTotalIncome() {
        if (bankBalance > 0.0) {
            return monthlyGrossEmploymentIncome + monthlyGrossRentalIncome
                    + bankBalance*config.RETURN_ON_FINANCIAL_WEALTH;
        } else {
            return monthlyGrossEmploymentIncome + monthlyGrossRentalIncome;
        }
    }

    double getAnnualGrossTotalIncome() { return getMonthlyGrossTotalIncome()*config.constants.MONTHS_IN_YEAR; }

    //----- Methods for house owners -----//

    /**
     * Decide what to do with a house h owned by the household:
     * - if the household lives in the house, decide whether to sell it or not
     * - if the house is up for sale, rethink its offer price, and possibly put it up for rent instead (only BTL investors)
     * - if the house is up for rent, rethink the rent demanded
     *
     * @param house A house owned by the household
     */
    private void manageHouse(House house) {
        HouseOfferRecord forSale, forRent;
        double newPrice;
        
        forSale = house.getSaleRecord();
        if(forSale != null) { // reprice house for sale
            newPrice = behaviour.rethinkHouseSalePrice(forSale);
            if(newPrice > mortgageFor(house).principal) {
                Model.houseSaleMarket.updateOffer(forSale, newPrice);
            } else {
                Model.houseSaleMarket.removeOffer(forSale);
                // TODO: Is first condition redundant?
                if(house  != home && house.resident == null) {
                    Model.houseRentalMarket.offer(house, buyToLetRent(house), false);
                }
            }
        } else if(decideToSellHouse(house)) { // put house on market?
            if(house.isOnRentalMarket()) Model.houseRentalMarket.removeOffer(house.getRentalRecord());
            putHouseForSale(house);
        }
        
        forRent = house.getRentalRecord();
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
        if (h == home) {
            Model.houseSaleMarket.offer(h, behaviour.getInitialSalePrice(h.getQuality(), principal), false);
        } else {
            Model.houseSaleMarket.offer(h, behaviour.getInitialSalePrice(h.getQuality(), principal), true);
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
    void completeHousePurchase(HouseOfferRecord sale) {
        if(isRenting()) { // give immediate notice to landlord and move out
            if(sale.getHouse().resident != null) System.out.println("Strange: my new house has someone in it!");
            if(home == sale.getHouse()) {
                System.out.println("Strange: I've just bought a house I'm renting out");
            } else {
                endTenancy();
            }
        }
        MortgageAgreement mortgage = Model.bank.requestLoan(this, sale.getPrice(), behaviour.decideDownPayment(this,sale.getPrice()), home == null, sale.getHouse());
        if(mortgage == null) {
            // TODO: need to either provide a way for house sales to fall through or to ensure that pre-approvals are always satisfiable
            System.out.println("Can't afford to buy house: strange");
            System.out.println("Bank balance is "+bankBalance);
            System.out.println("Annual income is "+ monthlyGrossEmploymentIncome *config.constants.MONTHS_IN_YEAR);
            if(isRenting()) System.out.println("Is renting");
            if(isHomeowner()) System.out.println("Is homeowner");
            if(isInSocialHousing()) System.out.println("Is homeless");
            if(isFirstTimeBuyer()) System.out.println("Is firsttimebuyer");
            if(behaviour.isPropertyInvestor()) System.out.println("Is investor");
            System.out.println("House owner = "+ sale.getHouse().owner);
            System.out.println("me = "+this);
        } else {
            bankBalance -= mortgage.downPayment;
            housePayments.put(sale.getHouse(), mortgage);
            if (home == null) { // move in to house
                home = sale.getHouse();
                sale.getHouse().resident = this;
            } else if (sale.getHouse().resident == null) { // put empty buy-to-let house on rental market
                Model.houseRentalMarket.offer(sale.getHouse(), buyToLetRent(sale.getHouse()), false);
            }
            isFirstTimeBuyer = false;
        }
    }

    /********************************************************
     * Do all stuff necessary when this household sells a house
     ********************************************************/
    public void completeHouseSale(HouseOfferRecord sale) {
        // First, receive money from sale
        bankBalance += sale.getPrice();
        // Second, find mortgage object and pay off as much outstanding debt as possible given bank balance
        MortgageAgreement mortgage = mortgageFor(sale.getHouse());
        bankBalance -= mortgage.payoff(bankBalance);
        // Third, if there is no more outstanding debt, remove the house from the household's housePayments object
        if (mortgage.nPayments == 0) {
            housePayments.remove(sale.getHouse());
            // TODO: Warning, if bankBalance is not enough to pay mortgage back, then the house stays in housePayments,
            // TODO: consequences to be checked. Looking forward, properties and payment agreements should be kept apart
        }
        // Fourth, if the house is still being offered on the rental market, withdraw the offer
        if (sale.getHouse().isOnRentalMarket()) {
            Model.houseRentalMarket.removeOffer(sale);
        }
        // Fifth, if the house is the household's home, then the household moves out and becomes temporarily homeless...
        if (sale.getHouse() == home) {
            home.resident = null;
            home = null;
        // ...otherwise, if the house has a resident, it must be a renter, who must get evicted, also the rental income
        // corresponding to this tenancy must be subtracted from the owner's monthly rental income
        } else if (sale.getHouse().resident != null) {
            monthlyGrossRentalIncome -= sale.getHouse().resident.housePayments.get(sale.getHouse()).monthlyPayment;
            sale.getHouse().resident.getEvicted();
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
        monthlyGrossRentalIncome -= contract.monthlyPayment;

        // put house back on rental market
        if(!housePayments.containsKey(h)) {
            System.out.println("Strange: I don't own this house in endOfLettingAgreement");
        }
//        if(h.resident != null) System.out.println("Strange: renting out a house that has a resident");        
//        if(h.resident != null && h.resident == h.owner) System.out.println("Strange: renting out a house that belongs to a homeowner");        
        if(h.isOnRentalMarket()) System.out.println("Strange: got endOfLettingAgreement on house on rental market");
        if(!h.isOnMarket()) Model.houseRentalMarket.offer(h, buyToLetRent(h), false);
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
    void completeHouseRental(HouseOfferRecord sale) {
        if(sale.getHouse().owner != this) { // if renting own house, no need for contract
            RentalAgreement rent = new RentalAgreement();
            rent.monthlyPayment = sale.getPrice();
            rent.nPayments = config.TENANCY_LENGTH_AVERAGE
                    + prng.nextInt(2*config.TENANCY_LENGTH_EPSILON + 1) - config.TENANCY_LENGTH_EPSILON;
//            rent.principal = rent.monthlyPayment*rent.nPayments;
            housePayments.put(sale.getHouse(), rent);
        }
        if(home != null) System.out.println("Strange: I'm renting a house but not homeless");
        home = sale.getHouse();
        if(sale.getHouse().resident != null) {
            System.out.println("Strange: tenant moving into an occupied house");
            if(sale.getHouse().resident == this) System.out.println("...It's me!");
            if(sale.getHouse().owner == this) System.out.println("...It's my house!");
            if(sale.getHouse().owner == sale.getHouse().resident) System.out.println("...It's a homeowner!");
        }
        sale.getHouse().resident = this;
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
        double price = behaviour.getDesiredPurchasePrice(monthlyGrossEmploymentIncome);
        // Cap this expenditure to the maximum mortgage available to the household
        price = Math.min(price, Model.bank.getMaxMortgage(this, true));
        // Record the bid on householdStats for counting the number of bids above exponential moving average sale price
        Model.householdStats.countNonBTLBidsAboveExpAvSalePrice(price);
        // Compare costs to decide whether to buy or rent...
        if (behaviour.decideRentOrPurchase(this, price)) {
            // ... if buying, bid in the house sale market for the capped desired price
            Model.houseSaleMarket.bid(this, price);
        } else {
            // ... if renting, bid in the house rental market for the desired rent price
            Model.houseRentalMarket.bid(this, behaviour.desiredRent(monthlyGrossEmploymentIncome));
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
    public void completeHouseLet(HouseOfferRecord sale) {
        if(sale.getHouse().isOnMarket()) {
            Model.houseSaleMarket.removeOffer(sale.getHouse().getSaleRecord());
        }
        monthlyGrossRentalIncome += sale.getPrice();
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
        // Check if beneficiary is the same as the deceased household
        if (beneficiary == this) { // TODO: I don't think this check is really necessary
            System.out.println("Strange: I'm transferring all my wealth to myself");
            System.exit(0);
        }
        // Create an iterator over the house-paymentAgreement pairs at the deceased household's housePayments object
        Iterator<Entry<House, PaymentAgreement>> paymentIt = housePayments.entrySet().iterator();
        Entry<House, PaymentAgreement> entry;
        House h;
        PaymentAgreement payment;
        // Iterate over these house-paymentAgreement pairs
        while (paymentIt.hasNext()) {
            entry = paymentIt.next();
            h = entry.getKey();
            payment = entry.getValue();
            // If the deceased household owns the house, then...
            if (h.owner == this) {
                // ...first, withdraw the house from any market where it is currently being offered
                if (h.isOnRentalMarket()) Model.houseRentalMarket.removeOffer(h.getRentalRecord());
                if (h.isOnMarket()) Model.houseSaleMarket.removeOffer(h.getSaleRecord());
                // ...then, if there is a resident in the house...
                if (h.resident != null) {
                    // ...and this resident is different from the deceased household, then this resident must be a
                    // tenant, who must get evicted
                    if (h.resident != this) {
                        h.resident.getEvicted(); // TODO: Explain in paper that renters always get evicted, not just if heir needs the house
                    // ...otherwise, if the resident is the deceased household, remove it from the house
                    } else {
                        h.resident = null;
                    }
                }
                // ...finally, transfer the property to the beneficiary household
                beneficiary.inheritHouse(h);
            // Otherwise, if the deceased household does not own the house but it is living in it, then it must have
            // been renting it: end the letting agreement
            } else if (h == home) {
                h.owner.endOfLettingAgreement(h, housePayments.get(h));
                h.resident = null;
            }
            // If payment agreement is a mortgage, then try to pay off as much as possible from the deceased household's bank balance
            if (payment instanceof MortgageAgreement) {
                bankBalance -= ((MortgageAgreement) payment).payoff();
            }
            // Remove the house-paymentAgreement entry from the deceased household's housePayments object
            paymentIt.remove(); // TODO: Not sure this is necessary. Note, though, that this implies erasing all outstanding debt
        }
        // Finally, transfer all remaining liquid wealth to the beneficiary household
        beneficiary.bankBalance += Math.max(0.0, bankBalance);
    }
    
    /**
     * Inherit a house.
     *
     * Write off the mortgage for the house. Move into the house if renting or in social housing.
     * 
     * @param h House to inherit
     */
    private void inheritHouse(House h) {
        // Create a null (zero payments) mortgage
        MortgageAgreement nullMortgage = new MortgageAgreement(this,false);
        nullMortgage.nPayments = 0;
        nullMortgage.downPayment = 0.0;
        nullMortgage.monthlyInterestRate = 0.0;
        nullMortgage.monthlyPayment = 0.0;
        nullMortgage.principal = 0.0;
        nullMortgage.purchasePrice = 0.0;
        // Become the owner of the inherited house and include it in my housePayments list (with a null mortgage)
        // TODO: Make sure the paper correctly explains that no debt is inherited
        housePayments.put(h, nullMortgage);
        h.owner = this;
        // Check for residents in the inherited house
        if(h.resident != null) {
            System.out.println("Strange: inheriting a house with a resident");
            System.exit(0);
        }
        // If renting or homeless, move into the inherited house
        if(!isHomeowner()) {
            // If renting, first cancel my current tenancy
            if(isRenting()) {
                endTenancy();                
            }
            home = h;
            h.resident = this;
        // If owning a home and having the BTL gene...
        } else if(behaviour.isPropertyInvestor()) {
            // ...decide whether to sell the inherited house
            if(decideToSellHouse(h)) {
                putHouseForSale(h);
            // ...or rent it out
            } else if(h.resident == null) {
                Model.houseRentalMarket.offer(h, buyToLetRent(h), false);
            }
        // If being an owner-occupier, put inherited house for sale
        } else {
            putHouseForSale(h);
        }
    }

    //----- Helpers -----//

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

    public double getAnnualGrossEmploymentIncome() { return annualGrossEmploymentIncome; }

    public double getMonthlyGrossEmploymentIncome() { return monthlyGrossEmploymentIncome; }

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
