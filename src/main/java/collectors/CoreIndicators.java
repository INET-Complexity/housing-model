package collectors;

import housing.Config;
import housing.Model;
import utilities.MeanAboveMedian;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**************************************************************************************************
 * Class to collect the information contained in the Bank of England "Core Indicators" set for LTV
 * and LTI limits, as set out in the Bank of England's draft policy statement "The Financial policy
 * committee's power over housing tools" (Feb 2015). See Table A for a list of these indicators and
 * notes on their definition.
 *
 * @author daniel, Adrian Carro
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

    // Additional measures to read for the Monte Carlo Simulations
	// simple HPI
	double getHPI() {
		return Model.housingMarketStats.getHPI();
	}
	
	// get total net wealth share of the top 10 % of households
	
	double getS90TotalNetWealth() {
		// get the sorted array of total net wealth of every household
		double[] sortedNetWealth = Model.householdStats.totalNetWealth.getSortedValues();
		long numberAgents = Model.householdStats.totalNetWealth.getN();
		int arrayPositionTop10 = (int) (numberAgents-(numberAgents/10));
		double[] S90Array = Arrays.copyOfRange(sortedNetWealth, arrayPositionTop10, (int) numberAgents) ;
		// share of top 10 wealth
		double sumS90 = DoubleStream.of(S90Array).parallel().sum();
		double sumTotalNetWealth = DoubleStream.of(sortedNetWealth).parallel().sum();
		return sumS90/sumTotalNetWealth;
	}
	
	// number of bankruptcies
	int getNumberBankruptcies() {
		return Model.householdStats.getnNonBTLBankruptcies() + Model.householdStats.getnBTLBankruptcies();
	}
	
	double getShareEmptyHouses() {
		return ((double)Model.householdStats.getnEmptyHouses())/Model.construction.getHousingStock();
	}
	
	double getBTLMarketShare() {
		return Model.householdStats.getBTLStockFraction();
	}
	
	double getTotalFinancialWealth() {
		return Model.householdStats.getTotalBankBalancesVeryBeginningOfPeriod();
	}
	
	double getTotalIncomeConsumption() {
		return Model.householdStats.getIncomeConsumption();
	}
	
	double getTotalFinancialConsumption() {
		return Model.householdStats.getFinancialWealthConsumption();
	}
	
	double getTotalGrossHousingWealthConsumption() {
		return Model.householdStats.getHousingWealthConsumption();
	}
	
	double getDebtConsumption() {
		return Model.householdStats.getDebtConsumption();
	}
	
	double getSavingForDeleveraging() {
		return Model.householdStats.getTotalSavingForDeleveraging();
	}
	
	//consumption
	double getConsumptionOverIncome() {
		return Model.householdStats.getTotalConsumption()/((Model.householdStats.getOwnerOccupierAnnualisedTotalIncome()
                + Model.householdStats.getActiveBTLAnnualisedTotalIncome()
                + Model.householdStats.getNonOwnerAnnualisedTotalIncome())/config.constants.MONTHS_IN_YEAR);
	}
	
	
	// Owner-occupier mortgage LTI ratio (mean above the median)
	double getOwnerOccupierLTIMeanAboveMedian() {
        if (Model.creditSupply.oo_lti.getN() > 0) {
            return Model.creditSupply.oo_lti.apply(new MeanAboveMedian());
        } else {
            return 0.0;
        }
	}

    // Owner-occupier mortage LTV ratio (mean above the median)
	double getOwnerOccupierLTVMeanAboveMedian() {
        if (Model.creditSupply.oo_ltv.getN() > 0) {
            return Model.creditSupply.oo_ltv.apply(new MeanAboveMedian());
        } else {
            return 0.0;
        }
	}

    // Buy-to-let loan-to-value ratio (mean)
	double getBuyToLetLTVMean() {
        if (Model.creditSupply.btl_ltv.getN() > 0) {
            return Model.creditSupply.btl_ltv.getMean();
        } else {
            return 0.0;
        }
    }

	// Annualised household credit growth (credit growth: rate of change of credit, current month new credit divided by
    //  new credit in previous step)
	double getHouseholdCreditGrowth() { return Model.creditSupply.netCreditGrowth*12.0*100.0; }

	// Household mortgage debt to income ratio (%)
	double getDebtToIncome() {
		return 100.0*(Model.creditSupply.totalBTLCredit + Model.creditSupply.totalOOCredit)
                /(Model.householdStats.getOwnerOccupierAnnualisedTotalIncome()
                + Model.householdStats.getActiveBTLAnnualisedTotalIncome()
                + Model.householdStats.getNonOwnerAnnualisedTotalIncome());
	}

	// Household debt to income ratio (owner-occupier mortgages only) (%)
	double getOODebtToIncome() {
        return 100.0*Model.creditSupply.totalOOCredit/Model.householdStats.getOwnerOccupierAnnualisedTotalIncome();
    }

	// Number of mortgage approvals per month (scaled for 26.5 million households)
	int getMortgageApprovals() {
		return (int)(Model.creditSupply.nApprovedMortgages*config.getUKHouseholds()
                /Model.households.size());
	}

    // Number of houses bought/sold per month (scaled for 26.5 million households)
	int getHousingTransactions() {
		return (int)(Model.housingMarketStats.getnSales()*config.getUKHouseholds()
                /Model.households.size());
	}

	// Number of advances to first-time-buyers (scaled for 26.5 million households)
	int getAdvancesToFTBs() {
		return (int)(Model.creditSupply.nFTBMortgages*config.getUKHouseholds()
                /Model.households.size());
	}

    // Number of advances to buy-to-let purchasers (scaled for 26.5 million households)
	int getAdvancesToBTL() {
		return (int)(Model.creditSupply.nBTLMortgages*config.getUKHouseholds()
                /Model.households.size());
	}

	// Number of advances to home-movers (scaled for 26.5 million households)
	int getAdvancesToHomeMovers() { return(getMortgageApprovals() - getAdvancesToFTBs() - getAdvancesToBTL()); }

    // House price to household disposable income ratio
	// TODO: BoE uses Gross Disposable Income, which is defined: "money that that all of the individuals in the 
	// household sector have available for spending or saving after income distribution measures 
	// (for example, taxes, social contributions and benefits) have taken effect." 
	// TODO: therefore, I use Net Income of ALL households
	public double getPriceToIncome() {
	    // TODO: Also, why to use HPI*HPIReference? Why not average house price?
		return(Model.housingMarketStats.getHPI()*config.derivedParams.getHPIReference()
				*(Model.households.size())
                /((Model.householdStats.getOwnerOccupierMonthlyNetIncome()
                		+ Model.householdStats.getActiveMonthlyNetIncome()
                		+ Model.householdStats.getNonOwnerMonthlyNetIncome())*12));
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
