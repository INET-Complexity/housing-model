package collectors;

import housing.Config;
import housing.Model;
import utilities.MeanAboveMedian;

/**************************************************************************************************
 * Class to collect the information contained in the Bank of England "Core Indicators" set for LTV
 * and LTI limits, as set out in the Bank of England's draft policy statement "The Financial policy
 * committee's power over housing tools" (Feb 2015). See Table A for a list of these indicators and
 * notes on their definition.
 *
 * @author danial, Adrian Carro
 *
 *************************************************************************************************/
public class CoreIndicators {

    //------------------//
    //----- Fields -----//
    //------------------//

	private Config config = Model.config;	// Passes the Model's configuration parameters object to a private field

    //-------------------//
    //----- Methods -----//
    //-------------------//

    //----- Getter/setter methods -----//

    // Note that some of these methods are just wrappers around methods contained in other classes with the purpose of
    // storing here a coherent set of core indicators getters

    // Owner-occupier mortgage LTI ratio (mean above the median)
	double getOwnerOccupierLTIMeanAboveMedian() {
        if (Model.creditSupply.getOO_lti().getN() > 0) {
            return Model.creditSupply.getOO_lti().apply(new MeanAboveMedian());
        } else {
            return 0.0;
        }
	}

    // Owner-occupier mortage LTV ratio (mean above the median)
	double getOwnerOccupierLTVMeanAboveMedian() {
        if (Model.creditSupply.getOO_ltv().getN() > 0) {
            return Model.creditSupply.getOO_ltv().apply(new MeanAboveMedian());
        } else {
            return 0.0;
        }
	}

    // Buy-to-let loan-to-value ratio (mean)
	double getBuyToLetLTVMean() {
        if (Model.creditSupply.getBTL_ltv().getN() > 0) {
            return Model.creditSupply.getBTL_ltv().getMean();
        } else {
            return 0.0;
        }
    }

	// Annualised household credit growth (credit growth: rate of change of credit, current month new credit divided by
    //  new credit in previous step)
	double getHouseholdCreditGrowth() { return Model.creditSupply.getNetCreditGrowth()*12.0*100.0; }

	// Household mortgage debt to income ratio (%)
	double getDebtToIncome() {
		return 100.0*(Model.creditSupply.getTotalBTLCredit() + Model.creditSupply.getTotalOOCredit())
                /(Model.householdStats.getOwnerOccupierAnnualisedTotalIncome()
                + Model.householdStats.getActiveBTLAnnualisedTotalIncome()
                + Model.householdStats.getNonOwnerAnnualisedTotalIncome());
	}

	// Household debt to income ratio (owner-occupier mortgages only) (%)
	double getOODebtToIncome() {
        return 100.0*Model.creditSupply.getTotalOOCredit()/Model.householdStats.getOwnerOccupierAnnualisedTotalIncome();
    }

	// Number of mortgage approvals per month (scaled for 26.5 million households)
	int getMortgageApprovals() {
		return (int)(Model.creditSupply.getnApprovedMortgages()*config.getUKHouseholds()
                /Model.households.size());
	}

    // Number of houses bought/sold per month (scaled for 26.5 million households)
	int getHousingTransactions() {
		return (int)(Model.housingMarketStats.getnSales()*config.getUKHouseholds()
                /Model.households.size());
	}

	// Number of advances to first-time-buyers (scaled for 26.5 million households)
	int getAdvancesToFTBs() {
		return (int)(Model.creditSupply.getnFTBMortgages()*config.getUKHouseholds()
                /Model.households.size());
	}

    // Number of advances to buy-to-let purchasers (scaled for 26.5 million households)
	int getAdvancesToBTL() {
		return (int)(Model.creditSupply.getnBTLMortgages()*config.getUKHouseholds()
                /Model.households.size());
	}

	// Number of advances to home-movers (scaled for 26.5 million households)
	int getAdvancesToHomeMovers() { return(getMortgageApprovals() - getAdvancesToFTBs() - getAdvancesToBTL()); }

    // House price to household disposable income ratio
    // TODO: ATTENTION ---> Gross total income is used here, not disposable income! Post-tax income should be used!
	double getPriceToIncome() {
	    // TODO: Also, why to use HPI*HPIReference? Why not average house price?
		return(Model.housingMarketStats.getHPI()*config.derivedParams.getHousePricesMean()
				*(Model.households.size()
                - Model.householdStats.getnRenting()
                - Model.householdStats.getnHomeless())
                /(Model.householdStats.getOwnerOccupierAnnualisedTotalIncome()
                + Model.householdStats.getActiveBTLAnnualisedTotalIncome()));
		// TODO: Finally, for security, population count should be made with nActiveBTL and nOwnerOccupier
	}

	// Wrapper around the HouseHoldStats method, which computes the average stock gross rental yield for all currently
    // occupied rental properties (%)
	double getAvStockYield() { return 100.0*Model.householdStats.getAvStockYield(); }

	// Wrapper around the HousingMarketStats method, which computes the quarter on quarter appreciation in HPI
	double getQoQHousePriceGrowth() { return Model.housingMarketStats.getQoQHousePriceGrowth(); }

	// Spread between mortgage-lender interest rate and bank base-rate (%)
	double getInterestRateSpread() { return 100.0*Model.bank.interestSpread; }
}
