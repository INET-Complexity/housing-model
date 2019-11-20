from .CollectorBase import CollectorBase

import housing.Config
import housing.Model
import utilities.MeanAboveMedian

#**************************************************************************************************
#* Class to collect the information contained in the Bank of England "Core Indicators" set for LTV
#* and LTI limits, as set out in the Bank of England's draft policy statement "The Financial policy
#* committee's power over housing tools" (Feb 2015). See Table A for a list of these indicators and
#* notes on their definition.
#*
#* @author danial, Adrian Carro
#*
#*************************************************************************************************/
class CoreIndicators(CollectorBase):
    #------------------#
    #----- Fields -----#
    #------------------#
    def __init__(self):
        self.config = Model.config  # Passes the Model's configuration parameters object to a private field

    #-------------------#
    #----- Methods -----#
    #-------------------#

    #----- Getter/setter methods -----#

    # Note that some of these methods are just wrappers around methods contained in other classes with the purpose of
    # storing here a coherent set of core indicators getters

    def setActive(self, active: bool) -> None:
        super().setActive(active)
        Model.creditSupply.setActive(active)
        Model.housingMarketStats.setActive(active)
        Model.householdStats.setActive(active)

    # Owner-occupier mortgage LTI ratio (mean above the median)
    def getOwnerOccupierLTIMeanAboveMedian(self) -> float:
        if Model.creditSupply.oo_lti.getN() > 0:
            return Model.creditSupply.oo_lti.apply(MeanAboveMedian())
        else:
            return 0.0

    # Owner-occupier mortage LTV ratio (mean above the median)
    def getOwnerOccupierLTVMeanAboveMedian(self) -> float:
        if Model.creditSupply.oo_ltv.getN() > 0:
            return Model.creditSupply.oo_ltv.apply(MeanAboveMedian())
        else:
            return 0.0

    # Buy-to-let loan-to-value ratio (mean)
    def getBuyToLetLTVMean(self) -> float:
        if Model.creditSupply.btl_ltv.getN() > 0:
            return Model.creditSupply.btl_ltv.getMean()
        else:
            return 0.0

    # Annualised household credit growth (credit growth: rate of change of credit, current month new credit divided by
    #  new credit in previous step)
    def getHouseholdCreditGrowth(self) -> float:
        return Model.creditSupply.netCreditGrowth * 12.0 * 100.0

    # Household mortgage debt to income ratio (%)
    def getDebtToIncome(self) -> float:
        denominator = (Model.householdStats.getOwnerOccupierAnnualisedTotalIncome() + Model.householdStats.getActiveBTLAnnualisedTotalIncome() + Model.householdStats.getNonOwnerAnnualisedTotalIncome())
        return 100.0 * (Model.creditSupply.totalBTLCredit + Model.creditSupply.totalOOCredit) / denominator

    # Household debt to income ratio (owner-occupier mortgages only) (%)
    def getOODebtToIncome(self) -> float:
        return 100.0 * Model.creditSupply.totalOOCredit / Model.householdStats.getOwnerOccupierAnnualisedTotalIncome()

    # Number of mortgage approvals per month (scaled for 26.5 million households)
    def getMortgageApprovals(self) -> int:
        return int(Model.creditSupply.nApprovedMortgages * config.getUKHouseholds() / Model.households.size())

    # Number of houses bought/sold per month (scaled for 26.5 million households)
    def getHousingTransactions(self) -> int:
        return int(Model.housingMarketStats.getnSales() * config.getUKHouseholds() / Model.households.size())

    # Number of advances to first-time-buyers (scaled for 26.5 million households)
    def getAdvancesToFTBs(self) -> int:
        return int(Model.creditSupply.nFTBMortgages * config.getUKHouseholds() / Model.households.size())

    # Number of advances to buy-to-let purchasers (scaled for 26.5 million households)
    def getAdvancesToBTL(self) -> int:
        return int(Model.creditSupply.nBTLMortgages * config.getUKHouseholds() / Model.households.size())

    # Number of advances to home-movers (scaled for 26.5 million households)
    def getAdvancesToHomeMovers(self) -> int:
        return self.getMortgageApprovals() - self.getAdvancesToFTBs() - self.getAdvancesToBTL()

    # House price to household disposable income ratio
    # TODO: ATTENTION ---> Gross total income is used here, not disposable income! Post-tax income should be used!
    def getPriceToIncome(self) -> float:
        # TODO: Also, why to use HPI*HPIReference? Why not average house price?
        return (Model.housingMarketStats.getHPI() *
                config.derivedParams.getHPIReference() *
                (Model.households.size() - Model.householdStats.getnRenting() - Model.householdStats.getnHomeless()) /
                (Model.householdStats.getOwnerOccupierAnnualisedTotalIncome() +
                 Model.householdStats.getActiveBTLAnnualisedTotalIncome()))
        # TODO: Finally, for security, population count should be made with nActiveBTL and nOwnerOccupier

    # Wrapper around the HouseHoldStats method, which computes the average stock gross rental yield for all currently
    # occupied rental properties (%)
    def getAvStockYield(self) -> float:
        return 100.0 * Model.householdStats.getAvStockYield()

    # Wrapper around the HousingMarketStats method, which computes the quarter on quarter appreciation in HPI
    def getQoQHousePriceGrowth(self) -> float:
        return Model.housingMarketStats.getQoQHousePriceGrowth()

    # Spread between mortgage-lender interest rate and bank base-rate (%)
    def getInterestRateSpread(self) -> float:
        return 100.0 * Model.bank.interestSpread
