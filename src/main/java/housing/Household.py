import random

from typing import Dict

from .House import House

#*************************************************************************************************
# This represents a household who receives an income, consumes, saves and can buy, sell, let, and
# invest in houses.
#
# @author daniel, davidrpugh, Adrian Carro
#
#************************************************************************************************/

class Household(IHouseOwner):
    # Initialises behaviour (determine whether the household will be a BTL investor). Households start off in social
    # housing and with their "desired bank balance" in the bank
    id_pool = 0
    def __init__(self, Model):
        self.config = Model.config
        self.home: House = None
        self.isFirstTimeBuyer = True
        self.isBankrupt = False
        # Only used for identifying households within the class MicroDataRecorder
        self.id = Household.id_pool  # TODOTODO verify this
        Household.id_pool += 1
        # Age of the household representative person
        self.age = data.Demographics.pdfHouseholdAgeAtBirth.nextDouble()
        # Fixed for the whole lifetime of the household
        self.incomePercentile = random.random()
        # Behavioural plugin
        self.behaviour = HouseholdBehaviour(incomePercentile)
        # Find initial values for the annual and monthly gross employment income
        self.annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(self.age, self.incomePercentile)
        self.monthlyGrossEmploymentIncome = self.annualGrossEmploymentIncome / self.config.constants.MONTHS_IN_YEAR
        self.bankBalance = self.behaviour.getDesiredBankBalance(self.getAnnualGrossTotalIncome()) # Desired bank balance is used as initial value for actual bank balance
        # Keeps track of monthly rental income, as only tenants keep a reference to the rental contract, not landlords
        self.monthlyGrossRentalIncome = 0.0
        # Houses owned and their payment agreements
        self.housePayments: Dict[House, PaymentAgreement] = {}

     #----- General methods -----#

    # Main simulation step for each household. They age, receive employment and other forms of income, make their rent
    # or mortgage payments, perform an essential consumption, make non-essential consumption decisions, manage their
    # owned properties, and make their housing decisions depending on their current housing state:
    # - Buy or rent if in social housing
    # - Sell house if owner-occupier
    # - Buy/sell/rent out properties if BTL investor
    def step(self) -> None:
        self.isBankrupt = False  # Delete bankruptcies from previous time step
        self.age += 1.0 / self.config.constants.MONTHS_IN_YEAR
        # Update annual and monthly gross employment income
        self.annualGrossEmploymentIncome = data.EmploymentIncome.getAnnualGrossEmploymentIncome(self.age, self.incomePercentile)
        self.monthlyGrossEmploymentIncome = self.annualGrossEmploymentIncome / self.config.constants.MONTHS_IN_YEAR
        # Add monthly disposable income (net total income minus essential consumption and housing expenses) to bank balance
        self.bankBalance += self.getMonthlyDisposableIncome()
        # Consume based on monthly disposable income (after essential consumption and house payments have been subtracted)
        self.bankBalance -= self.behaviour.getDesiredConsumption(self.getBankBalance(), self.getAnnualGrossTotalIncome())  # Old implementation: if(isFirstTimeBuyer() || !isInSocialHousing()) bankBalance -= behaviour.getDesiredConsumption(getBankBalance(), getAnnualGrossTotalIncome())
        # Deal with bankruptcies
        # TODO: Improve bankruptcy procedures (currently, simple cash injection), such as terminating contracts!
        if self.bankBalance < 0.0:
            self.bankBalance = 1.0
            self.isBankrupt = True
        # Manage owned properties and close debts on previously owned properties. To this end, first, create an
        # iterator over the house-paymentAgreement pairs at the household's housePayments object
        # Iterate over these house-paymentAgreement pairs...
        for h, payment in self.housePayments.items():
            # ...if the household is the owner of the house, then manage it
            if h.owner == self:
                self.manageHouse(h)
            # ...otherwise, if the household is not the owner nor the resident, then it is an old debt due to
            # the household's inability to pay the remaining principal off after selling a property...
            elif h.resident != self:
                mortgage = MortgageAgreement(payment)
                # ...remove this type of houses from payments as soon as the household pays the debt off
                if (payment.nPayments == 0) and (mortgage.principal == 0.0):
                    del self.housePayments[h]
        # Make housing decisions depending on current housing state
        if self.isInSocialHousing():
            self.bidForAHome() # When BTL households are born, they enter here the first time and until they manage to buy a home!
        elif self.isRenting():
            if housePayments.get(self.home).nPayments == 0:  # End of rental period for this tenant
                self.endTenancy()
                self.bidForAHome()
        elif self.behaviour.isPropertyInvestor():  # Only BTL investors who already own a home enter here
            price = self.behaviour.btlPurchaseBid(self)
            self.Model.householdStats.countBTLBidsAboveExpAvSalePrice(price)
            if self.behaviour.decideToBuyInvestmentProperty(self):
                self.Model.houseSaleMarket.BTLbid(self, price)
        elif not isHomeowner():
            print("Strange: this household is not a type I recognize")

    # Subtracts the essential, necessary consumption and housing expenses (mortgage and rental payments) from the net
    # total income (employment income, property income, financial returns minus taxes)
    def getMonthlyDisposableIncome(self) -> float:
        # Start with net monthly income
        monthlyDisposableIncome = self.getMonthlyNetTotalIncome()
        # Subtract essential, necessary consumption
        # TODO: ESSENTIAL_CONSUMPTION_FRACTION is not explained in the paper, all support is said to be consumed
        monthlyDisposableIncome -= self.config.ESSENTIAL_CONSUMPTION_FRACTION * self.config.GOVERNMENT_MONTHLY_INCOME_SUPPORT
        # Subtract housing consumption
        for payment in housePayments.values():
            monthlyDisposableIncome -= payment.makeMonthlyPayment()
        return monthlyDisposableIncome

    # Subtracts the monthly aliquot part of all due taxes from the monthly gross total income. Note that only income
    # tax on employment income and national insurance contributions are implemented!
    def getMonthlyNetTotalIncome(self) -> float:
        # TODO: Note that this implies there is no tax on rental income nor on bank balance returns
        return (self.getMonthlyGrossTotalIncome() -
                (self.Model.government.incomeTaxDue(self.annualGrossEmploymentIncome) +  # Employment income tax
                 self.Model.government.class1NICsDue(self.annualGrossEmploymentIncome)) /  # National insurance contributions
                self.config.constants.MONTHS_IN_YEAR)

    # Adds up all sources of (gross) income on a monthly basis: employment, property, returns on financial wealth
    def getMonthlyGrossTotalIncome(self) -> float:
        if self.bankBalance > 0.0:
            return self.monthlyGrossEmploymentIncome + self.monthlyGrossRentalIncome + self.bankBalance * self.config.RETURN_ON_FINANCIAL_WEALTH
        else:
            return self.monthlyGrossEmploymentIncome + self.monthlyGrossRentalIncome

    def getAnnualGrossTotalIncome(self) -> float:
        return self.getMonthlyGrossTotalIncome() * self.config.constants.MONTHS_IN_YEAR

    #----- Methods for house owners -----#

    # Decide what to do with a house h owned by the household:
    # - if the household lives in the house, decide whether to sell it or not
    # - if the house is up for sale, rethink its offer price, and possibly put it up for rent instead (only BTL investors)
    # - if the house is up for rent, rethink the rent demanded
    #
    # @param house A house owned by the household
    def manageHouse(self, house: House) -> None:
        forSale = house.getSaleRecord()
        if forSale is not None:  # reprice house for sale
            newPrice = self.behaviour.rethinkHouseSalePrice(forSale)
            if newPrice > mortgageFor(house).principal:
                self.Model.houseSaleMarket.updateOffer(forSale, newPrice)
            else:
                self.Model.houseSaleMarket.removeOffer(forSale)
                # TODO: Is first condition redundant?
                if house != self.home and house.resident is None:
                    self.Model.houseRentalMarket.offer(house, buyToLetRent(house), false)
        elif self.decideToSellHouse(house):  # put house on market?
            if house.isOnRentalMarket():
                self.Model.houseRentalMarket.removeOffer(house.getRentalRecord())
            self.putHouseForSale(house)

        forRent = house.getRentalRecord()
        if forRent is not None:  # reprice house for rent
            newPrice = self.behaviour.rethinkBuyToLetRent(forRent)
            self.Model.houseRentalMarket.updateOffer(forRent, newPrice)

    #*****************************************************
    # Having decided to sell house h, decide its initial sale price and put it up in the market.
    #
    # @param h the house being sold
    #*****************************************************/
    def putHouseForSale(self, h: House) -> None:
        principal: float
        mortgage: MortgageAgreement = mortgageFor(h)
        if mortgage is not None:
            principal = mortgage.principal
        else:
            principal = 0.0
        if h == self.home:
            self.Model.houseSaleMarket.offer(h, behaviour.getInitialSalePrice(h.getQuality(), principal), False)
        else:
            self.Model.houseSaleMarket.offer(h, behaviour.getInitialSalePrice(h.getQuality(), principal), True)

    ############################/
    # Houseowner interface
    ############################/

    #*******************************************************
    # Do all the stuff necessary when this household
    # buys a house:
    # Give notice to landlord if renting,
    # Get loan from mortgage-lender,
    # Pay for house,
    # Put house on rental market if buy-to-let and no tenant.
    #*******************************************************/
    def completeHousePurchase(self, sale: HouseOfferRecord) -> None:
        if self.isRenting():  # give immediate notice to landlord and move out
            if sale.getHouse().resident is not None:
                print("Strange: my new house has someone in it!")
            if self.home == sale.getHouse():
                print("Strange: I've just bought a house I'm renting out")
            else:
                self.endTenancy()
        mortgage = self.Model.bank.requestLoan(self, sale.getPrice(), self.behaviour.decideDownPayment(self, sale.getPrice()), self.home is None, sale.getHouse())
        if mortgage is None:
            # TODO: need to either provide a way for house sales to fall through or to ensure that pre-approvals are always satisfiable
            print("Can't afford to buy house: strange")
            print("Bank balance is " + str(bankBalance))
            print("Annual income is " + str(self.monthlyGrossEmploymentIncome * self.config.constants.MONTHS_IN_YEAR))
            if self.isRenting():
                print("Is renting")
            if self.isHomeowner():
                print("Is homeowner")
            if self.isInSocialHousing():
                print("Is homeless")
            if self.isFirstTimeBuyer():
                print("Is firsttimebuyer")
            if self.behaviour.isPropertyInvestor():
                print("Is investor")
            print("House owner = " + str(sale.getHouse().owner))
            print("me = " + str(self))
        else:
            self.bankBalance -= mortgage.downPayment
            self.housePayments[sale.getHouse()] = mortgage
            if self.home is None:  # move in to house
                self.home = sale.getHouse()
                sale.getHouse().resident = self
            elif sale.getHouse().resident is None:  # put empty buy-to-let house on rental market
                self.Model.houseRentalMarket.offer(sale.getHouse(), self.buyToLetRent(sale.getHouse()), False)
            self.isFirstTimeBuyer = False

    #*******************************************************
    # Do all stuff necessary when this household sells a house
    #*******************************************************/
    def completeHouseSale(self, sale: HouseOfferRecord) -> None:
        # First, receive money from sale
        self.bankBalance += sale.getPrice()
        # Second, find mortgage object and pay off as much outstanding debt as possible given bank balance
        mortgage = self.mortgageFor(sale.getHouse())
        self.bankBalance -= mortgage.payoff(self.bankBalance)
        # Third, if there is no more outstanding debt, remove the house from the household's housePayments object
        if mortgage.nPayments == 0:
            del housePayments[sale.getHouse()]
            # TODO: Warning, if bankBalance is not enough to pay mortgage back, then the house stays in housePayments,
            # TODO: consequences to be checked. Looking forward, properties and payment agreements should be kept apart
        # Fourth, if the house is still being offered on the rental market, withdraw the offer
        if sale.getHouse().isOnRentalMarket():
            self.Model.houseRentalMarket.removeOffer(sale)
        # Fifth, if the house is the household's home, then the household moves out and becomes temporarily homeless...
        if sale.getHouse() == self.home:
            self.home.resident = None
            self.home = None
        # ...otherwise, if the house has a resident, it must be a renter, who must get evicted, also the rental income
        # corresponding to this tenancy must be subtracted from the owner's monthly rental income
        elif sale.getHouse().resident is not None:
            self.monthlyGrossRentalIncome -= sale.getHouse().resident.housePayments.get(sale.getHouse()).monthlyPayment
            sale.getHouse().resident.getEvicted()

    #*******************************************************
    # A BTL investor receives this message when a tenant moves
    # out of one of its buy-to-let houses.
    # 
    # The household simply puts the house back on the rental
    # market.
    #*******************************************************/
    def endOfLettingAgreement(self, h: House, contract: PaymentAgreement) -> None:
        self.monthlyGrossRentalIncome -= contract.monthlyPayment

        # put house back on rental market
        if h not in self.housePayments:
            print("Strange: I don't own this house in endOfLettingAgreement")
#        if(h.resident != null) System.out.println("Strange: renting out a house that has a resident")        
#        if(h.resident != null && h.resident == h.owner) System.out.println("Strange: renting out a house that belongs to a homeowner")        
        if h.isOnRentalMarket():
            prnt("Strange: got endOfLettingAgreement on house on rental market")
        if not h.isOnMarket():
            self.Model.houseRentalMarket.offer(h, self.buyToLetRent(h), False)

    #*********************************************************
    # This household moves out of current rented accommodation
    # and becomes homeless (possibly temporarily). Move out,
    # inform landlord and delete rental agreement.
    #*********************************************************/
    def endTenancy(self) -> None:
        self.home.owner.endOfLettingAgreement(self.home, housePayments.get(self.home))
        del self.housePayments[self.home]
        self.home.resident = null
        self.home = null

    #*** Landlord has told this household to get out: leave without informing landlord */
    def getEvicted(self) -> None:
        if self.home is None:
            print("Strange: got evicted but I'm homeless")
        if self.home.owner == self:
            print("Strange: got evicted from a home I own")
        del self.housePayments[self.home]
        self.home.resident = None
        self.home = None

    #*******************************************************
    # Do all the stuff necessary when this household moves
    # in to rented accommodation (i.e. set up a regular
    # payment contract. At present we use a MortgageApproval).
    #*******************************************************/
    def completeHouseRental(self, sale: HouseOfferRecord) -> None:
        if sale.getHouse().owner != self:  # if renting own house, no need for contract
            rent = RentalAgreement()
            rent.monthlyPayment = sale.getPrice()
            rent.nPayments = self.config.TENANCY_LENGTH_AVERAGE + random.randint(0, 2 * self.config.TENANCY_LENGTH_EPSILON + 1) - self.config.TENANCY_LENGTH_EPSILON
#            rent.principal = rent.monthlyPayment*rent.nPayments
            self.housePayments[sale.getHouse()] = rent
        if self.home is not None:
            print("Strange: I'm renting a house but not homeless")
        self.home = sale.getHouse()
        if sale.getHouse().resident is not None:
            print("Strange: tenant moving into an occupied house")
            if sale.getHouse().resident == self:
                print("...It's me!")
            if sale.getHouse().owner == self:
                print("...It's my house!")
            if sale.getHouse().owner == sale.getHouse().resident:
                print("...It's a homeowner!")
        sale.getHouse().resident = self

    #*******************************************************
    # Make the decision whether to bid on the housing market or rental market.
    # This is an "intensity of choice" decision (sigma function)
    # on the cost of renting compared to the cost of owning, with
    # COST_OF_RENTING being an intrinsic psychological cost of not
    # owning. 
    #*******************************************************/
    def bidForAHome(self) -> None:
        # Find household's desired housing expenditure
        price: float = self.behaviour.getDesiredPurchasePrice(self.monthlyGrossEmploymentIncome)
        # Cap this expenditure to the maximum mortgage available to the household
        price = min(price, self.Model.bank.getMaxMortgage(self, True))
        # Record the bid on householdStats for counting the number of bids above exponential moving average sale price
        self.Model.householdStats.countNonBTLBidsAboveExpAvSalePrice(price)
        # Compare costs to decide whether to buy or rent...
        if self.behaviour.decideRentOrPurchase(self, price):
            # ... if buying, bid in the house sale market for the capped desired price
            self.Model.houseSaleMarket.bid(self, price)
        else:
            # ... if renting, bid in the house rental market for the desired rent price
            self.Model.houseRentalMarket.bid(self, self.behaviour.desiredRent(self.monthlyGrossEmploymentIncome))

    #*******************************************************
    # Decide whether to sell ones own house.
    #*******************************************************/
    def decideToSellHouse(self, h: House) -> bool:
        if h == self.home:
            return self.behaviour.decideToSellHome()
        else:
            return self.behaviour.decideToSellInvestmentProperty(h, self)

    # Do stuff necessary when BTL investor lets out a rental
    # property
    def completeHouseLet(self, sale: HouseOfferRecord) -> None:
        if sale.getHouse().isOnMarket():
            self.Model.houseSaleMarket.removeOffer(sale.getHouse().getSaleRecord())
        self.monthlyGrossRentalIncome += sale.getPrice()

    def buyToLetRent(h: House) -> float:
        return self.behaviour.buyToLetRent(
            self.Model.rentalMarketStats.getExpAvSalePriceForQuality(h.getQuality()),
            self.Model.rentalMarketStats.getExpAvDaysOnMarket(), h)

    ############################/
    # Inheritance behaviour
    ############################/

    # Implement inheritance: upon death, transfer all wealth to the previously selected household.
    #
    # Take all houses off the markets, evict any tenants, pay off mortgages, and give property and remaining
    # bank balance to the beneficiary.
    # @param beneficiary The household that will inherit the wealth
    def transferAllWealthTo(beneficiary: Household) -> None:
        # Check if beneficiary is the same as the deceased household
        if beneficiary == self:  # TODO: I don't think this check is really necessary
            print("Strange: I'm transferring all my wealth to myself")
            exit()
        # Iterate over these house-paymentAgreement pairs
        for h, payment in self.housePayments.items():
            # If the deceased household owns the house, then...
            if h.owner == self:
                # ...first, withdraw the house from any market where it is currently being offered
                if h.isOnRentalMarket():
                    self.Model.houseRentalMarket.removeOffer(h.getRentalRecord())
                if h.isOnMarket():
                    self.Model.houseSaleMarket.removeOffer(h.getSaleRecord())
                # ...then, if there is a resident in the house...
                if h.resident is not None:
                    # ...and this resident is different from the deceased household, then this resident must be a
                    # tenant, who must get evicted
                    if h.resident != self:
                        h.resident.getEvicted() # TODO: Explain in paper that renters always get evicted, not just if heir needs the house
                    # ...otherwise, if the resident is the deceased household, remove it from the house
                    else:
                        h.resident = None
                # ...finally, transfer the property to the beneficiary household
                beneficiary.inheritHouse(h)
            # Otherwise, if the deceased household does not own the house but it is living in it, then it must have
            # been renting it: end the letting agreement
            elif h == self.home:
                h.owner.endOfLettingAgreement(h, housePayments.get(h))
                h.resident = None
            # If payment agreement is a mortgage, then try to pay off as much as possible from the deceased household's bank balance
            if isinstance(payment, MortgageAgreement):
                self.bankBalance -= payment.payoff()
            # Remove the house-paymentAgreement entry from the deceased household's housePayments object
            del self.housePayments[h]  # TODO: Not sure this is necessary. Note, though, that this implies erasing all outstanding debt
        # Finally, transfer all remaining liquid wealth to the beneficiary household
        beneficiary.bankBalance += max(0.0, self.bankBalance)

    # Inherit a house.
    #
    # Write off the mortgage for the house. Move into the house if renting or in social housing.
    # 
    # @param h House to inherit
    def inheritHouse(h: House) -> None:
        # Create a null (zero payments) mortgage
        nullMortgage = MortgageAgreement(self, False)
        nullMortgage.nPayments = 0
        nullMortgage.downPayment = 0.0
        nullMortgage.monthlyInterestRate = 0.0
        nullMortgage.monthlyPayment = 0.0
        nullMortgage.principal = 0.0
        nullMortgage.purchasePrice = 0.0
        # Become the owner of the inherited house and include it in my housePayments list (with a null mortgage)
        # TODO: Make sure the paper correctly explains that no debt is inherited
        self.housePayments.put[h] = nullMortgage
        h.owner = self
        # Check for residents in the inherited house
        if h.resident is not None:
            print("Strange: inheriting a house with a resident")
            exit()
        # If renting or homeless, move into the inherited house
        if not self.isHomeowner():
            # If renting, first cancel my current tenancy
            if self.isRenting():
                self.endTenancy()
            self.home = h
            h.resident = this
        # If owning a home and having the BTL gene...
        elif self.behaviour.isPropertyInvestor():
            # ...decide whether to sell the inherited house
            if self.decideToSellHouse(h):
                self.putHouseForSale(h)
            # ...or rent it out
            elif h.resident is None:
                self.Model.houseRentalMarket.offer(h, self.buyToLetRent(h), False)
        # If being an owner-occupier, put inherited house for sale
        else:
            self.putHouseForSale(h)

    #----- Helpers -----#

    def getAge(self) -> float:
        return age

    def isHomeowner(self) -> bool:
        if self.home is None:
            return False
        return self.home.owner == self

    def isRenting(self) -> bool:
        if self.home is None:
            return False
        return self.home.owner != self

    def isInSocialHousing(self) -> bool:
        return self.home is None

    def isFirstTimeBuyer(self) -> bool:
        return self.isFirstTimeBuyer

    def isBankrupt(self) -> bool:
        return self.isBankrupt

    def getBankBalance(self) -> float:
        return self.bankBalance

    def getHome(self) -> House:
        return self.home

    def getHousePayments(self) -> Dict[House, PaymentAgreement]:
        return self.housePayments

    def getAnnualGrossEmploymentIncome(self) -> float:
        return self.annualGrossEmploymentIncome

    def getMonthlyGrossEmploymentIncome(self) -> float:
        return self.monthlyGrossEmploymentIncome

    # @return Number of properties this household currently has on the sale market
    def nPropertiesForSale(self) -> int:
        n = 0
        for h in self.housePayments.keys():
            if h.isOnMarket():
                n += 1
        return n

    def nInvestmentProperties(self) -> int:
        return len(self.housePayments) - 1

    # @return Current mark-to-market (with exponentially averaged prices per quality) equity in this household's home.
    def getHomeEquity(self) -> float:
        if not self.isHomeowner():
            return 0.0
        return self.Model.housingMarketStats.getExpAvSalePriceForQuality(self.home.getQuality()) - self.mortgageFor(self.home).principal

    def mortgageFor(self, h: House) -> MortgageAgreement:
        payment = self.housePayments.get(h)
        if isinstance(payment, MortgageAgreement):
            return MortgageAgreement(payment)
        return None

    def monthlyPaymentOn(self, h: House) -> float:
        payment = self.housePayments.get(h)
        if payment is not None:
            return payment.monthlyPayment
        return 0.0
