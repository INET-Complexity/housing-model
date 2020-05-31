#*************************************************************************************************
# Class to represent the mortgage policy regulator or Central Bank. It reads a number of policy
# thresholds from the config object into local variables with the purpose of allowing for dynamic
# policies varying those thresholds over time.
#
# @author daniel, Adrian Carro
#
#************************************************************************************************/
class CentralBank:
    def __init__(self):
        # General fields
        self.config = Model.config  # Passes the Model's configuration parameters object to a private field

        # LTI policy thresholds
        self.firstTimeBuyerLTILimit: float = 0.0  # Loan-To-Income upper limit for first-time buying mortgages
        self.ownerOccupierLTILimit: float = 0.0  # Loan-To-Income upper limit for owner-occupying mortgages
        self.maxFractionOOMortgagesOverLTILimit: float = 0.0  # Fraction of owner-occupying mortgages allowed to exceed the Loan-To-Income limit

        # ICR policy thresholds
        self.interestCoverRatioLimit: float = 0.0  # Ratio of expected rental yield over interest monthly payment under stressed interest conditions
        self.interestCoverRatioStressedRate: float = 0.0  # Stressed interest rate used for Interest-Cover-Ratio assessments
        self.init()

    #-------------------#
    #----- Methods -----#
    #-------------------#
    def init(self):
        # Setup initial LTI policy thresholds
        self.firstTimeBuyerLTILimit = self.config.CENTRAL_BANK_MAX_FTB_LTI
        self.ownerOccupierLTILimit = self.config.CENTRAL_BANK_MAX_OO_LTI
        self.maxFractionOOMortgagesOverLTILimit = self.config.CENTRAL_BANK_FRACTION_OVER_MAX_LTI
        # Setup initial ICR policy thresholds
        self.interestCoverRatioLimit = self.config.CENTRAL_BANK_MAX_ICR
        self.interestCoverRatioStressedRate = self.config.CENTRAL_BANK_BTL_STRESSED_INTEREST

    # This method implements the policy strategy of the Central Bank
    #
    # @param coreIndicators The current value of the core indicators
    def step(self, coreIndicators: collectors.CoreIndicators):
        pass
        #/* Use this method to express the policy strategy of the central bank by setting the value of the various limits
        # in response to the current value of the core indicators.

        # Example policy: if house price growth is greater than 0.001 then FTB LTV limit is 0.75 otherwise (if house
        # price growth is less than or equal to  0.001) FTB LTV limit is 0.95
        # Example code:
        #        if(coreIndicators.getHousePriceGrowth() > 0.001) {
        #                firstTimeBuyerLTVLimit = 0.75
        #        } else {
        #                firstTimeBuyerLTVLimit = 0.95
        #        }
        # */

    # Get the Loan-To-Income ratio limit applicable to a given household. Note that Loan-To-Income constraints apply
    # only to non-BTL applicants. The private bank always imposes its own limit. Apart from this, it also imposes the
    # Central Bank regulated limit, which allows for a certain fraction of residential loans (mortgages for
    # owner-occupying) to go over it
    #
    # @param isFirstTimeBuyer True if the household is first-time buyer
    # @param isHome True if the mortgage is to buy a home for the household (non-BTL mortgage)
    # @return Loan-To-Income ratio limit applicable to the household
    def getLoanToIncomeLimit(self, isFirstTimeBuyer: bool, isHome: bool) -> float:
        if isHome:
            if isFirstTimeBuyer:
                return self.firstTimeBuyerLTILimit
            else:
                return self.ownerOccupierLTILimit
        else:
            print("Strange: The Central Bank is trying to impose a Loan-To-Income limit on a Buy-To-Let investor!")
            return 0.0  # Dummy return statement

    # Get the maximum fraction of mortgages to owner-occupying households that can go over the Loan-To-Income limit
    def getMaxFractionOOMortgagesOverLTILimit(self) -> float:
        return self.maxFractionOOMortgagesOverLTILimit

    # Get the Interest-Cover-Ratio limit applicable to a particular household
    def getInterestCoverRatioLimit(self, isHome: bool) -> float:
        if not isHome:
            return self.interestCoverRatioLimit
        else:
            print("Strange: Interest-Cover-Ratio limit is being imposed on an owner-occupying buyer!")
            return 0.0  # Dummy return statement

    # Get the stressed interest rate for the Interest-Cover-Ratio assessment for a particular household
    def getInterestCoverRatioStressedRate(self, isHome: bool) -> float:
        if not isHome:
            return self.interestCoverRatioStressedRate
        else:
            print("Strange: Interest-Cover-Ratio rate is being used for assessing an owner-occupying buyer!")
            return 0.0  # Dummy return statement
