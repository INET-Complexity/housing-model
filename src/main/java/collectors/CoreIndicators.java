package collectors;

import housing.Config;
import housing.Model;

import java.util.ArrayList;
import java.util.Collections;

/**************************************************************************************************
 * Class to collect the information on the "Core Indicators" as defined by the Bank of England at
 * the draft policy statement "The Financial policy committee's power over housing tools" from
 * February 2015 (see Table A for a list of these indicators and notes on their definition). Note
 * that some of these methods are just wrappers around methods contained in other classes with the
 * purpose of storing here a coherent set of core indicator getters
 *
 * @author danial, Adrian Carro
 *
 *************************************************************************************************/
public class CoreIndicators {

    //------------------//
    //----- Fields -----//
    //------------------//

    private Config config = Model.config;   // Passes the Model's configuration parameters object to a private field

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * 1.a - LTI and LTV ratios on new residential mortgages: Owner-occupier mortgage LTV ratio (mean above the median)
     */
    double getOwnerOccupierLTVMeanAboveMedian() {  return 100.0 * getMeanAboveMedian(Model.creditSupply.getOO_ltv()); }

    /**
     * 1.b - LTI and LTV ratios on new residential mortgages: Owner-occupier mortgage LTI ratio (mean above the median)
     */
    double getOwnerOccupierLTIMeanAboveMedian() { return getMeanAboveMedian(Model.creditSupply.getOO_lti()); }

    /**
     * 1.c - LTI and LTV ratios on new residential mortgages: Buy-to-let mortgage LTV ratio (mean)
     */
    double getBuyToLetLTVMean() {
        return 100.0 * getMeanOfSums(Model.creditSupply.getBTL_ltv_sums(), Model.creditSupply.getnBTLMortgagesArray());
    }

    /**
     * 2 - Household credit growth
     *
     * Defined as the twelve-month nominal growth rate of credit, that is, as the twelve-month cumulative net flow of
     * credit divided by the stock in the initial quarter, or, in other words, as the current stock of credit minus the
     * stock of credit twelve months ago divided by the stock of credit twelve months ago.
     */
    double getHouseholdCreditGrowth() { return 100.0 * Model.creditSupply.getNetCreditGrowth(); }

    /**
     * Compute the mean above the median of an ArrayList of ArrayList of Doubles by combining all ArrayLists into a
     * single one, ordering it, finding the mid-point and finding the mean for values at or above this mid-point.
     *
     * @param arrayListOfArrayLists ArrayList of ArrayLists of Doubles
     * @return Mean above the median if ArrayList of ArrayLists contains at least an element, NaN otherwise
     */
    private double getMeanAboveMedian(ArrayList<ArrayList<Double>> arrayListOfArrayLists) {
        // Combine all ArrayLists (all months) into a single one
        ArrayList<Double> combined = new ArrayList<>();
        for (ArrayList<Double> arrayList : arrayListOfArrayLists) {
            arrayList.clear();
            combined.addAll(arrayList);
        }
        // Order this combined ArrayList so that its mid-point is the median
        Collections.sort(combined);
        // Find the mid-point, that is, the position of the median in the ArrayList (or the position just before it)
        int midPoint = combined.size()/2;
        // Finally, compute the mean above the median (mean over elements at and above the mid-point)
        double sum = 0.0;
        for (int i = midPoint; i < combined.size(); i++) {
            sum += combined.get(i);
        }
        if ((combined.size() - midPoint) > 0) {
            return sum / (combined.size() - midPoint);
        } else {
            return Double.NaN;
        }
    }

    private double getMeanOfSums(double[] sumsArray, int[] nElementsArray) {
        int nElements =  0;
        double sum = 0.0;
        for (int i = 0; i < sumsArray.length; i++) {
            nElements += nElementsArray[i];
            sum += sumsArray[i];
        }
        if (nElements > 0) {
            return sum/nElements;
        } else {
            return Double.NaN;
        }
    }

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

    // Number of mortgage approvals per month, scaled to UK actual number of households (note integer division)
    int getMortgageApprovals() {
        return Model.creditSupply.getnApprovedMortgages() * config.getUKHouseholds() / Model.households.size();
    }

    // Number of houses bought/sold per month, scaled to UK actual number of households (note integer division)
    int getHousingTransactions() {
        return Model.housingMarketStats.getnSales() * config.getUKHouseholds() / Model.households.size();
    }

    // Number of advances to first-time-buyers, scaled to UK actual number of households (note integer division)
    int getAdvancesToFTBs() {
        return Model.creditSupply.getnFTBMortgages() * config.getUKHouseholds() / Model.households.size();
    }

    // Number of advances to buy-to-let purchasers, scaled to UK actual number of households (note integer division)
    int getAdvancesToBTL() {
        return Model.creditSupply.getnBTLMortgages() * config.getUKHouseholds() / Model.households.size();
    }

    // Number of advances to home-movers, scaled to UK actual number of households
    int getAdvancesToHomeMovers() { return getMortgageApprovals() - getAdvancesToFTBs() - getAdvancesToBTL(); }

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
