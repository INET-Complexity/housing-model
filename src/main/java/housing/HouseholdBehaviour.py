import math
import random

#**************************************************************************************************
#* Class to implement the behavioural decisions made by households
#*
#* @author daniel, Adrian Carro
#*
#*************************************************************************************************/
class HouseholdBehaviour:
    # Initialise behavioural variables for a new household: propensity to save, whether the household will have the BTL
    # investor "gene" (provided its income percentile is above a certain minimum), and whether the household will be a
    # fundamentalist or a trend follower investor (provided it has received the BTL investor gene)
    #
    # @param incomePercentile Fixed income percentile for the household (assumed constant over a lifetime)
    def __init__(self, Model, incomePercentile: float):
        self.config = Model.config
        self.Model = Model

        # Set downpayment distributions for both first-time-buyers and owner-occupiers
        # Size distribution for downpayments of first-time-buyers
        self.downpaymentDistFTB = LogNormalDistribution(self.config.DOWNPAYMENT_FTB_SCALE, self.config.DOWNPAYMENT_FTB_SHAPE)
        # Size distribution for downpayments of owner-occupiers
        self.downpaymentDistOO = LogNormalDistribution(self.config.DOWNPAYMENT_OO_SCALE, self.config.DOWNPAYMENT_OO_SHAPE)
        # Compute propensity to save, so that it is constant for a given household
        self.propensityToSave = self.config.DESIRED_BANK_BALANCE_EPSILON * random.normalvariate(0, 1)
        # Decide if household is a BTL investor and, if so, its tendency to seek capital gains or rental yields
        # Sensitivity of BTL investors to capital gain, 0.0 cares only about rental yield, 1.0 cares only about cap gain
        self.BTLCapGainCoefficient = 0.0
        if incomePercentile > self.config.MIN_INVESTOR_PERCENTILE and random.random() < self.config.getPInvestor() / self.config.MIN_INVESTOR_PERCENTILE:
            self.BTLInvestor = True
            if random.random() < config.P_FUNDAMENTALIST:
                self.BTLCapGainCoefficient = self.config.FUNDAMENTALIST_CAP_GAIN_COEFF
            else:
                self.BTLCapGainCoefficient = self.config.TREND_CAP_GAIN_COEFF
        else:
            self.BTLInvestor = False

    #-------------------#
    #----- Methods -----#
    #-------------------#

    #----- General behaviour -----#

    #* Compute the monthly non-essential or optional consumption by a household. It is calibrated so that the output
    #* wealth distribution fits the ONS wealth data for Great Britain.
    #*
    #* @param bankBalance Household's liquid wealth
    #* @param annualGrossTotalIncome Household's annual gross total income
    def getDesiredConsumption(self, bankBalance: float, annualGrossTotalIncome: float) -> float:
        return self.config.CONSUMPTION_FRACTION * max(bankBalance - self.getDesiredBankBalance(annualGrossTotalIncome), 0.0)

    # Minimum bank balance each household is willing to have at the end of the month for the whole population to match
    # the wealth distribution obtained from the household survey (LCFS). In particular, in line with the Wealth and
    # Assets Survey, we model the relationship between liquid wealth and gross annual income as log-normal. This
    # desired bank balance will be then used to determine non-essential consumption.
    # TODO: Relationship described as log-normal here but power-law implemented! Dan's version of article described the
    # TODO: the distributions of gross income and of liquid wealth as log-normal, not their relationship. Change paper!
    #
    # @param annualGrossTotalIncome Household
    def getDesiredBankBalance(self, annualGrossTotalIncome: float) -> float:
        return math.exp(self.config.DESIRED_BANK_BALANCE_ALPHA + self.config.DESIRED_BANK_BALANCE_BETA * math.log(annualGrossTotalIncome) + self.propensityToSave)

    #----- Owner-Occupier behaviour -----#

    # Desired purchase price used to decide whether to buy a house and how much to bid for it
    #
    # @param monthlyGrossEmploymentIncome Monthly gross employment income of the household
    def getDesiredPurchasePrice(self, monthlyGrossEmploymentIncome: float) -> float:
        # TODO: This product is generally so small that it barely has any impact on the results, need to rethink if
        # TODO: it is necessary and if this small value makes any sense
        HPAFactor: float = self.config.BUY_WEIGHT_HPA * self.getLongTermHPAExpectation()
        # TODO: The capping of this factor intends to avoid negative and too large desired prices, the 0.9 is a
        # TODO: purely artificial fudge parameter. This formula should be reviewed and changed!
        if HPAFactor > 0.9:
            HPAFactor = 0.9
        # TODO: Note that wealth is not used here, but only monthlyGrossEmploymentIncome
        return self.config.BUY_SCALE * self.config.constants.MONTHS_IN_YEAR * monthlyGrossEmploymentIncome * math.exp(self.config.BUY_EPSILON * random.normalvariate(0, 1)) / (1.0 - HPAFactor)

    # Initial sale price of a house to be listed
    #
    # @param quality Quality of the house ot be sold
    # @param principal Amount of principal left on any mortgage on this house
    def getInitialSalePrice(self, quality: int, principal: float) -> float:
        exponent = (self.config.SALE_MARKUP +
                    math.log(self.Model.housingMarketStats.getExpAvSalePriceForQuality(quality) + 1.0) -
                    self.config.SALE_WEIGHT_DAYS_ON_MARKET * math.log((self.Model.housingMarketStats.getExpAvDaysOnMarket() + 1.0) / (self.config.constants.DAYS_IN_MONTH + 1.0)) +
                    self.config.SALE_EPSILON * random.normalvariate(0, 1))
        # TODO: ExpAv days on market could be computed for each quality band so as to use here only the correct one
        return max(math.exp(exponent), principal)

    # This method implements a household's decision to sell their owner-occupied property. On average, households sell
    # owner-occupied houses every 11 years, due to exogenous reasons not addressed in the model. In order to prevent
    # an unrealistic build-up of housing stock and unrealistic fluctuations of the interest rate, we modify this
    # probability by introducing two extra factors, depending, respectively, on the number of houses per capita
    # currently on the market and its exponential moving average, and on the interest rate and its exponential moving
    # average. In this way, the long-term selling probability converges to 1/11.
    # TODO: This method includes 2 unidentified fudge parameters, DECISION_TO_SELL_HPC (houses per capita) and
    # TODO: DECISION_TO_SELL_INTEREST, which are explicitly explained otherwise in the manuscript. URGENT!
    # TODO: Basically, need to implement both exponential moving averages referred above
    #
    # @return True if the owner-occupier decides to sell the house and false otherwise.
    def decideToSellHome(self) -> bool:
        # TODO: This if implies BTL agents never sell their homes, need to explain in paper!
        return (not self.isPropertyInvestor()) and (random.random() < self.config.derivedParams.MONTHLY_P_SELL * (
            1.0 +
            self.config.DECISION_TO_SELL_ALPHA * (self.config.DECISION_TO_SELL_HPC - self.Model.houseSaleMarket.getnHousesOnMarket() / len(Model.households)) +
            self.config.DECISION_TO_SELL_BETA * (self.config.DECISION_TO_SELL_INTEREST - self.Model.bank.getMortgageInterestRate())))

    # Decide amount to pay as initial downpayment
    #
    # @param me the household
    # @param housePrice the price of the house
    def decideDownPayment(self, me: Household, housePrice: float) -> float:
        if me.getBankBalance() > housePrice * self.config.BANK_BALANCE_FOR_CASH_DOWNPAYMENT:
            return housePrice

        if me.isFirstTimeBuyer():
            # Since the function of the HPI is to move the down payments distribution upwards or downwards to
            # accommodate current price levels, and the distribution is itself aggregate, we use the aggregate HPI
            downpayment = self.Model.housingMarketStats.getHPI() * self.downpaymentDistFTB.inverseCumulativeProbability(max(0.0, (me.incomePercentile - self.config.DOWNPAYMENT_MIN_INCOME) / (1 - self.config.DOWNPAYMENT_MIN_INCOME)))
        elif self.isPropertyInvestor():
            downpayment = housePrice * (max(0.0, self.config.DOWNPAYMENT_BTL_MEAN + self.config.DOWNPAYMENT_BTL_EPSILON * random.normalvariate(0, 1)))
        else:
            downpayment = self.Model.housingMarketStats.getHPI() * self.downpaymentDistOO.inverseCumulativeProbability(max(0.0, (me.incomePercentile - self.config.DOWNPAYMENT_MIN_INCOME) / (1 - self.config.DOWNPAYMENT_MIN_INCOME)))

        if downpayment > me.getBankBalance():
            downpayment = me.getBankBalance()
        return downpayment

    #############################/
    ############/ REVISED ############/
    #############################/

    #*******************************************************
    # Decide how much to drop the list-price of a house if
    # it has been on the market for (another) month and hasn't
    # sold. Calibrated against Zoopla dataset in Bank of England
    # 
    # @param sale The HouseOfferRecord of the house that is on the market.
    #*******************************************************/
    def rethinkHouseSalePrice(self, sale: HouseOfferRecord) -> float:
        if random.random() < self.config.P_SALE_PRICE_REDUCE:
            logReduction = self.config.REDUCTION_MU + (random.normalvariate(0, 1) * self.config.REDUCTION_SIGMA)
            return sale.getPrice() * (1.0 - math.exp(logReduction) / 100.0)
        return sale.getPrice()

    ###############################################/
    # Renter behaviour
    ###############################################/

    #** renters or OO after selling home decide whether to rent or buy
    # N.B. even though the HH may not decide to rent a house of the
    # same quality as they would buy, the cash value of the difference in quality
    #  is assumed to be the difference in rental price between the two qualities.
    #  @return true if we should buy a house, false if we should rent
    #/
    def decideRentOrPurchase(self, me: Household, purchasePrice: float) -> bool:
        if self.isPropertyInvestor():
            return True
        mortgageApproval = self.Model.bank.requestApproval(me, purchasePrice, self.decideDownPayment(me, purchasePrice), True)
        newHouseQuality: int = self.Model.housingMarketStats.getMaxQualityForPrice(purchasePrice)
        if newHouseQuality < 0:
            return False  # can't afford a house anyway
        costOfHouse: float = mortgageApproval.monthlyPayment * self.config.constants.MONTHS_IN_YEAR - purchasePrice * self.getLongTermHPAExpectation()
        costOfRent: float = self.Model.rentalMarketStats.getExpAvSalePriceForQuality(newHouseQuality) * self.config.constants.MONTHS_IN_YEAR
        return random.random() < self.sigma(self.config.SENSITIVITY_RENT_OR_PURCHASE * (costOfRent * (1.0 + self.config.PSYCHOLOGICAL_COST_OF_RENTING) - costOfHouse))

    #*******************************************************
    # Decide how much to bid on the rental market
    # Source: Zoopla rental prices 2008-2009 (at Bank of England)
    #*******************************************************/
    def desiredRent(self, monthlyGrossEmploymentIncome: float) -> float:
        return monthlyGrossEmploymentIncome * self.config.DESIRED_RENT_INCOME_FRACTION

    ###############################################/
    # Property investor behaviour
    ###############################################/

    #*
    # Decide whether to sell or not an investment property. Investor households with only one investment property do
    # never sell it. A sale is never attempted when the house is occupied by a tenant. Households with at least two
    # investment properties will calculate the expected yield of the property in question based on two contributions:
    # rental yield and capital gain (with their corresponding weights which depend on the type of investor)
    # 
    # @param h The house in question
    # @param me The investor household
    # @return True if investor me decides to sell investment property h
    #/
    def decideToSellInvestmentProperty(self, h: House, me: Household) -> bool:
        # Fast decisions...
        # ...always keep at least one investment property
        if me.nInvestmentProperties() < 2:
            return False
        # ...don't sell while occupied by tenant
        if not h.isOnRentalMarket():
            return False

        # Find the expected equity yield rate of this property as a weighted mix of both rental yield and capital gain
        # times the leverage
        # ...find the mortgage agreement for this property
        mortgage: MortgageAgreement = me.mortgageFor(h)
        # ...find its current (fair market value) sale price
        currentMarketPrice: float = self.Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality())
        # ...find equity, or assets minus liabilities
        equity: float = max(0.01, currentMarketPrice - mortgage.principal)  # The 0.01 prevents possible divisions by zero later on
        # ...find the leverage on that mortgage (Assets divided by equity, or return on equity)
        leverage: float = currentMarketPrice / equity
        # ...find the expected rental yield of this property as its current rental price divided by its current (fair market value) sale price
        # TODO: ATTENTION ---> This rental yield is not accounting for expected occupancy... shouldn't it?
        currentRentalYield: float = h.getRentalRecord().getPrice() * self.config.constants.MONTHS_IN_YEAR / currentMarketPrice
        # ...find the mortgage rate (pounds paid a year per pound of equity)
        mortgageRate: float = mortgage.nextPayment() * self.config.constants.MONTHS_IN_YEAR / equity
        # ...finally, find expected equity yield, or yield on equity
        expectedEquityYield: float
        if self.config.BTL_YIELD_SCALING:
            expectedEquityYield = (
                leverage * ((1.0 - self.BTLCapGainCoefficient) * currentRentalYield +
                            self.BTLCapGainCoefficient * (self.Model.rentalMarketStats.getLongTermExpAvFlowYield() +
                                                          self.getLongTermHPAExpectation())) -
                mortgageRate)
        else:
            expectedEquityYield = (
                leverage * ((1.0 - self.BTLCapGainCoefficient) * currentRentalYield +
                            self.BTLCapGainCoefficient * self.getLongTermHPAExpectation()) -
                mortgageRate)
        # Compute a probability to keep the property as a function of the effective yield
        pKeep: float = pow(self.sigma(self.config.BTL_CHOICE_INTENSITY * expectedEquityYield), 1.0 / self.config.constants.MONTHS_IN_YEAR)
        # Return true or false as a random draw from the computed probability
        return random.random() < (1.0 - pKeep)

    # Decide whether to buy or not a new investment property. Investor households with no investment properties always
    # attempt to buy one. If the household's bank balance is below its desired bank balance, then no attempt to buy is
    # made. If the resources available to the household (maximum mortgage) are below the average price for the lowest
    # quality houses, then no attempt to buy is made. Households with at least one investment property will calculate
    # the expected yield of a new property based on two contributions: rental yield and capital gain (with their
    # corresponding weights which depend on the type of investor)
    #
    # @param me The investor household
    # @return True if investor me decides to try to buy a new investment property
    def decideToBuyInvestmentProperty(self, me: Household) -> bool:
        # Fast decisions...
        # ...always decide to buy if owning no investment property yet
        if me.nInvestmentProperties() < 1:
            return True
        # ...never buy (keep on saving) if bank balance is below the household's desired bank balance
        # TODO: This mechanism and its parameter are not declared in the article! Any reference for the value of the parameter?
        if me.getBankBalance() < self.getDesiredBankBalance(me.getAnnualGrossTotalIncome()) * self.config.BTL_CHOICE_MIN_BANK_BALANCE:
            return False
        # ...find maximum price (maximum mortgage) the household could pay
        maxPrice: float = self.Model.bank.getMaxMortgage(me, False)
        # ...never buy if that maximum price is below the average price for the lowest quality
        if maxPrice < self.Model.housingMarketStats.getExpAvSalePriceForQuality(0):
            return False

        # Find the expected equity yield rate for a hypothetical house maximising the leverage available to the
        # household and assuming an average rental yield (over all qualities). This is found as a weighted mix of both
        # rental yield and capital gain times the leverage
        # ...find mortgage with maximum leverage by requesting maximum mortgage with minimum downpayment
        mortgage: MortgageAgreement = self.Model.bank.requestApproval(me, maxPrice, 0.0, False)
        # ...find equity, or assets minus liabilities (which, initially, is simply the downpayment)
        equity: float = max(0.01, mortgage.downPayment)  # The 0.01 prevents possible divisions by zero later on
        # ...find the leverage on that mortgage (Assets divided by equity, or return on equity)
        leverage: float = mortgage.purchasePrice / equity
        # ...find the expected rental yield as an (exponential) average over all house qualities
        rentalYield: float = self.Model.rentalMarketStats.getExpAvFlowYield()
        # ...find the mortgage rate (pounds paid a year per pound of equity)
        mortgageRate: float = mortgage.nextPayment() * self.config.constants.MONTHS_IN_YEAR / equity
        # ...finally, find expected equity yield, or yield on equity
        expectedEquityYield: float
        if self.config.BTL_YIELD_SCALING:
            expectedEquityYield = leverage * (
                (1.0 - self.BTLCapGainCoefficient) * rentalYield +
                self.BTLCapGainCoefficient * (self.Model.rentalMarketStats.getLongTermExpAvFlowYield() +
                                              self.getLongTermHPAExpectation())) - mortgageRate
        else:
            expectedEquityYield = leverage * (
                (1.0 - self.BTLCapGainCoefficient) * rentalYield +
                self.BTLCapGainCoefficient * self.getLongTermHPAExpectation()) - mortgageRate

        # Compute the probability to decide to buy an investment property as a function of the expected equity yield
        # TODO: This probability has been changed to correctly reflect the conversion from annual to monthly
        # TODO: probability. This needs to be explained in the article
        # double pBuy = Math.pow(sigma(config.BTL_CHOICE_INTENSITY*expectedEquityYield),
        #         1.0/config.constants.MONTHS_IN_YEAR)
        pBuy: float = 1.0 - pow((1.0 - self.sigma(self.config.BTL_CHOICE_INTENSITY * expectedEquityYield)),
                                1.0 / self.config.constants.MONTHS_IN_YEAR)
        # Return true or false as a random draw from the computed probability
        return random.random() < pBuy

    def btlPurchaseBid(self, me: Household) -> float:
        # TODO: What is this 1.1 factor? Another fudge parameter? It prevents wealthy investors from offering more than
        # TODO: 10% above the average price of top quality houses. The effect of this is to prevent fast increases of
        # TODO: price as BTL investors buy all supply till prices are too high for everybody. Fairly unclear mechanism,
        # TODO: check for removal!
        return (min(self.Model.bank.getMaxMortgage(me, False),
                    1.1 * self.Model.housingMarketStats.getExpAvSalePriceForQuality(self.config.N_QUALITY - 1)))

    # How much rent does an investor decide to charge on a buy-to-let house? 
    # @param rbar exponential average rent for house of this quality
    # @param d average days on market
    # @param h house being offered for rent
    def buyToLetRent(self, rbar: float, d: float, h: House) -> float:
        # TODO: What? Where does this equation come from?
        beta: float = self.config.RENT_MARKUP / math.log(self.config.RENT_EQ_MONTHS_ON_MARKET)  # Weight of days-on-market effect

        exponent: float = (
            self.config.RENT_MARKUP + math.log(rbar + 1.0) -
            beta * math.log((d + 1.0) / (self.config.constants.DAYS_IN_MONTH + 1)) +
            self.config.RENT_EPSILON * random.normalvariate(0, 1)
        )
        result: float = math.exp(exponent)
        # TODO: The following contains a fudge (config.RENT_MAX_AMORTIZATION_PERIOD) to keep rental yield up
        minAcceptable: float = self.Model.housingMarketStats.getExpAvSalePriceForQuality(h.getQuality()) / (self.config.RENT_MAX_AMORTIZATION_PERIOD * self.config.constants.MONTHS_IN_YEAR)
        if result < minAcceptable:
            result = minAcceptable
        return result

    # Update the demanded rent for a property
    #
    # @param sale the HouseOfferRecord of the property for rent
    # @return the new rent
    def rethinkBuyToLetRent(self, sale: HouseOfferRecord) -> float:
        return (1.0 - self.config.RENT_REDUCTION) * sale.getPrice()

    # Logistic function, sometimes called sigma function, 1/1+e^(-x)
    #
    # @param x Parameter of the sigma or logistic function
    def sigma(self, x: float) -> float:
        return 1.0 / (1.0 + math.exp(-x))

    # @return expectation value of HPI in one year's time divided by today's HPI
    def getLongTermHPAExpectation(self) -> float:
        # Dampening or multiplier factor, depending on its value being <1 or >1, for the current trend of HPA when
        # computing expectations as in HPI(t+DT) = HPI(t) + FACTOR*DT*dHPI/dt (double)
        return self.Model.housingMarketStats.getLongTermHPA() * self.config.HPA_EXPECTATION_FACTOR

    def getBTLCapGainCoefficient(self) -> float:
        return self.BTLCapGainCoefficient

    def isPropertyInvestor(self) -> bool:
        return self.BTLInvestor
