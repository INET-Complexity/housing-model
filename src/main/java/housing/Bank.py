#*************************************************************************************************
# Class to represent a mortgage-lender (i.e. a bank or building society), whose only function is
# to approve/decline mortgage requests, so this is where mortgage-lending policy is encoded
#
# @author daniel, davidrpugh, Adrian Carro
#
#************************************************************************************************/
class Bank:
    def __init__(self):
        # all unpaid mortgage contracts supplied by the bank
        self.mortgages = []
        self.init()

    def init(self):
        # General fields
        self.config = Model.config  # Passes the Model's configuration parameters object to a private field

        # Bank fields
        interestSpread: float = 0.0  # current mortgage interest spread above base rate (monthly rate*12)
        monthlyPaymentFactor: float = 0.0  # Monthly payment as a fraction of the principal for non-BTL mortgages
        monthlyPaymentFactorBTL: float = 0.0  # Monthly payment as a fraction of the principal for BTL (interest-only) mortgages
        self.baseRate: float = self.config.BANK_INITIAL_BASE_RATE

        # Credit supply strategy fields
        supplyTarget: float = 0.0  # target supply of mortgage lending (pounds)
        supplyVal: float = 0.0  # monthly supply of mortgage loans (pounds)
        # TODO: Is this (dDemand_dInterest) a parameter? Shouldn't it depend somehow on other variables of the model?
        self.dDemand_dInterest: float = 10 * 1e10  # rate of change of demand with interest rate (pounds)
        nOOMortgagesOverLTI: int = 0  # Number of mortgages for owner-occupying that go over the LTI cap this time step
        nOOMortgages: int = 0  # Total number of mortgages for owner-occupying

        # LTV internal policy thresholds
        self.firstTimeBuyerLTVLimit: float = self.config.BANK_MAX_FTB_LTV  # Loan-To-Value upper limit for first-time buyer mortgages
        self.ownerOccupierLTVLimit: float = self.config.BANK_MAX_OO_LTV  # Loan-To-Value upper limit for owner-occupying mortgages
        self.buyToLetLTVLimit: float = self.config.BANK_MAX_BTL_LTV  # Loan-To-Value upper limit for buy-to-let mortgages

        # LTI internal policy thresholds
        self.firstTimeBuyerLTILimit: float = self.config.BANK_MAX_FTB_LTI  # Loan-To-Income internal upper limit for first-time buyer mortgages
        self.ownerOccupierLTILimit: float = self.config.BANK_MAX_OO_LTI  # Loan-To-Income internal upper limit for owner-occupying mortgages

        self.mortgages.clear()

        # TODO: Is this (0.02) a parameter? Does it affect results in any significant way or is it just a dummy initialisation?
        self.setMortgageInterestRate(0.02)
        self.resetMonthlyCounters()

    # Redo all necessary monthly calculations and reset counters.
    def step(self, totalPopulation: int) -> None:
        supplyTarget = self.config.BANK_CREDIT_SUPPLY_TARGET * totalPopulation
        self.setMortgageInterestRate(self.recalculateInterestRate())
        self.resetMonthlyCounters()

    # Reset counters for the next month
    def resetMonthlyCounters(self) -> None:
        self.supplyVal = 0.0
        self.nOOMortgagesOverLTI = 0
        self.nOOMortgages = 0

    # Calculate the mortgage interest rate for next month based on the rate for this month and the resulting demand.
    # This assumes a linear relationship between interest rate and demand, and aims to halve the difference between
    # current demand and the target supply
    def recalculateInterestRate(self) -> float:
        rate = self.getMortgageInterestRate() + 0.5 * (self.supplyVal - self.supplyTarget) / self.dDemand_dInterest
        if rate < self.baseRate:
            rate = self.baseRate
        return rate

    # Get the interest rate on mortgages.
    def getMortgageInterestRate(self) -> float:
        return self.baseRate + self.interestSpread

    # Set the interest rate on mortgages
    def setMortgageInterestRate(self, rate: float) -> None:
        self.interestSpread = rate - self.baseRate
        self.recalculateMonthlyPaymentFactor()

    # Compute the monthly payment factor, i.e., the monthly payment on a mortgage as a fraction of the mortgage
    # principal for both BTL (interest-only) and non-BTL mortgages.
    def recalculateMonthlyPaymentFactor(self) -> None:
        r = self.getMortgageInterestRate() / self.config.constants.MONTHS_IN_YEAR
        self.monthlyPaymentFactor = r / (1.0 - pow(1.0 + r, -self.config.derivedParams.N_PAYMENTS))
        self.monthlyPaymentFactorBTL = r

    # Get the monthly payment factor, i.e., the monthly payment on a mortgage as a fraction of the mortgage principal.
    def getMonthlyPaymentFactor(self, isHome: bool) -> float:
        if isHome:
            return self.monthlyPaymentFactor  # Monthly payment factor to pay off the principal in N_PAYMENTS
        else:
            return self.monthlyPaymentFactorBTL  # Monthly payment factor for interest-only mortgages

    # Method to arrange a Mortgage and get a MortgageAgreement object.
    # @param h The household requesting the mortgage
    # @param housePrice The price of the house that household h wants to buy
    # @param isHome True if household h plans to live in the house (non-BTL mortgage)
    # @return The MortgageApproval object, or NULL if the mortgage is declined
    def requestLoan(self, h: Household, housePrice: float, desiredDownPayment: float, isHome: bool,
                    house: House) -> MortgageAgreement:
        MortgageAgreement approval = requestApproval(h, housePrice, desiredDownPayment, isHome)
        if approval is None:
            return None
        # --- if all's well, go ahead and arrange mortgage
        supplyVal += approval.principal
        if approval.principal > 0.0:
            mortgages.append(approval)
            self.Model.creditSupply.recordLoan(h, approval, house)
            if isHome:
                self.nOOMortgages += 1
                if approval.principal / h.getAnnualGrossEmploymentIncome() > self.Model.centralBank.getLoanToIncomeLimit(h.isFirstTimeBuyer(), isHome):
                    self.nOOMortgagesOverLTI += 1
        return approval

    # Method to request a mortgage approval but not actually sign a mortgage contract. This is useful if you want to
    # explore the details of the mortgage contract before deciding whether to actually go ahead and sign it.
    #
    # @param h The household requesting the mortgage
    # @param housePrice The price of the house that household h wants to buy
    # @param isHome True if household h plans to live in the house (non-BTL mortgage)
    # @return The MortgageApproval object, or NULL if the mortgage is declined
    def requestApproval(self, h: Household, housePrice: float, desiredDownPayment: float,
                        isHome: bool) -> MortgageAgreement:
        approval = MortgageAgreement(h, not isHome)
        r = self.getMortgageInterestRate() / self.config.constants.MONTHS_IN_YEAR  # monthly interest rate
        double lti_principal, affordable_principal, icr_principal
        liquidWealth: float = h.getBankBalance()  # No home equity needs to be added here: home-movers always sell their homes before trying to buy new ones

        if isHome:
            liquidWealth += h.getHomeEquity()

        # --- LTV constraint
        approval.principal = housePrice * self.getLoanToValueLimit(h.isFirstTimeBuyer(), isHome)

        if isHome:
            # --- affordability constraint TODO: affordability for BTL?
            affordable_principal = max(0.0, self.config.CENTRAL_BANK_AFFORDABILITY_COEFF * h.getMonthlyNetTotalIncome()) / self.getMonthlyPaymentFactor(isHome)
            approval.principal = min(approval.principal, affordable_principal)

            # --- lti constraint
            lti_principal = h.getAnnualGrossEmploymentIncome() * self.getLoanToIncomeLimit(h.isFirstTimeBuyer(), isHome)
            approval.principal = min(approval.principal, lti_principal)
        else:
            # --- BTL ICR constraint
            icr_principal = self.Model.rentalMarketStats.getExpAvFlowYield() * housePrice / (self.Model.centralBank.getInterestCoverRatioLimit(isHome) * self.config.CENTRAL_BANK_BTL_STRESSED_INTEREST)
            approval.principal = min(approval.principal, icr_principal)

        approval.downPayment = housePrice - approval.principal

        if liquidWealth < approval.downPayment:
            print("Failed down-payment constraint: bank balance = " + str(liquidWealth) + " downpayment = " + str(approval.downPayment))
            exit()

        # --- allow larger downpayments
        if desiredDownPayment < 0.0:
            desiredDownPayment = 0.0
        if desiredDownPayment > liquidWealth:
            desiredDownPayment = liquidWealth
        if desiredDownPayment > housePrice:
            desiredDownPayment = housePrice
        if desiredDownPayment > approval.downPayment:
            approval.downPayment = desiredDownPayment
            approval.principal = housePrice - desiredDownPayment

        approval.monthlyPayment = approval.principal * self.getMonthlyPaymentFactor(isHome)
        approval.nPayments = self.config.derivedParams.N_PAYMENTS
        approval.monthlyInterestRate = r
        approval.purchasePrice = approval.principal + approval.downPayment

        return approval

    #* Find, for a given household, the maximum house price that this mortgage-lender is willing to approve a mortgage
    #* for.
    #* 
    #* @param h The household applying for the mortgage
    #* @param isHome True if household h plans to live in the house (non-BTL mortgage)
    #* @return The maximum house price that this mortgage-lender is willing to approve a mortgage for
    def getMaxMortgage(self, h: Household, isHome: bool) -> float:
        affordability_max_price: float  # Affordability (disposable income) constraint for maximum house price
        lti_max_price: float  # Loan to income constraint for maximum house price
        icr_max_price: float  # Interest cover ratio constraint for maximum house price
        liquidWealth: float = h.getBankBalance()  # No home equity needs to be added here: households always sell their homes before trying to buy new ones
        max_downpayment: float = liquidWealth - 0.01 # Maximum down-payment the household could make, where 1 cent is subtracted to avoid rounding errors

        # LTV constraint: maximum house price the household could pay with the maximum mortgage the bank could provide
        # to the household given the Loan-To-Value limit and the maximum down-payment the household could make
        max_price: float = max_downpayment / (1.0 - self.getLoanToValueLimit(h.isFirstTimeBuyer(), isHome))

        if isHome:  # No LTI nor affordability constraints for BTL investors
            # Affordability constraint
            affordability_max_price = max_downpayment + max(0.0, self.config.CENTRAL_BANK_AFFORDABILITY_COEFF * h.getMonthlyNetTotalIncome()) / self.getMonthlyPaymentFactor(isHome)
            max_price = min(max_price, affordability_max_price)
            # Loan-To-Income constraint
            lti_max_price = h.getAnnualGrossEmploymentIncome() * self.getLoanToIncomeLimit(h.isFirstTimeBuyer(), isHome) + max_downpayment
            max_price = min(max_price, lti_max_price)
        else:
            # Interest-Cover-Ratio constraint
            icr_max_price = max_downpayment / (1.0 - self.Model.rentalMarketStats.getExpAvFlowYield() / (self.Model.centralBank.getInterestCoverRatioLimit(isHome) * self.config.CENTRAL_BANK_BTL_STRESSED_INTEREST))
            max_price = min(max_price, icr_max_price)

        return max_price

    # This method removes a mortgage contract by removing it from the HashSet of mortgages
    # @param mortgage The MortgageAgreement object to be removed
    def endMortgageContract(self, mortgage: MortgageAgreement) -> None:
        self.mortgages.remove(mortgage)

    #----- Mortgage policy methods -----#

    # Get the Loan-To-Value ratio limit applicable by this private bank to a given household. Note that this limit is
    # self-imposed by the private bank.
    #
    # @param isFirstTimeBuyer True if the household is a first-time buyer
    # @param isHome True if the mortgage is to buy a home for the household (non-BTL mortgage)
    # @return The Loan-To-Value ratio limit applicable to the given household
    def getLoanToValueLimit(isFirstTimeBuyer: bool, isHome: bool) -> float:
        if isHome:
            if isFirstTimeBuyer:
                return self.firstTimeBuyerLTVLimit
            else:
                return self.ownerOccupierLTVLimit
        return self.buyToLetLTVLimit

    # Get the Loan-To-Income ratio limit applicable by this private bank to a given household. Note that Loan-To-Income
    # constraints apply only to non-BTL applicants. The private bank always imposes its own (hard) limit. Apart from
    # this, it also imposes the Central Bank regulated limit, which allows for a certain fraction of residential loans
    # (mortgages for owner-occupying) to go over it (and thus it is considered here a soft limit).
    #
    # @param isFirstTimeBuyer true if the household is a first-time buyer
    # @param isHome True if the mortgage is to buy a home for the household (non-BTL mortgage)
    # @return The Loan-To-Income ratio limit applicable to the given household
    def getLoanToIncomeLimit(self, isFirstTimeBuyer: bool, isHome: bool) -> float:
        # First compute the private bank self-imposed (hard) limit, which applies always
        if isHome:
            if isFirstTimeBuyer:
                limit = firstTimeBuyerLTILimit
            else:
                limit = ownerOccupierLTILimit
        else:
            print("Strange: The bank is trying to impose a Loan-To-Income limit on a Buy-To-Let investor!")
            limit = 0.0  # Dummy limit value
        # If the fraction of non-BTL mortgages already underwritten over the Central Bank LTI limit exceeds a certain
        # maximum (regulated also by the Central Bank)...
        if (self.nOOMortgagesOverLTI + 1.0) / (self.nOOMortgages + 1.0) > self.Model.centralBank.getMaxFractionOOMortgagesOverLTILimit():
            # ... then compare the Central Bank LTI (soft) limit and that of the private bank (hard) and choose the smallest
            limit = min(limit, self.Model.centralBank.getLoanToIncomeLimit(isFirstTimeBuyer, isHome))
        return limit
