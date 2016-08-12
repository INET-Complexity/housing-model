package optimiser;

import housing.CoreIndicators;

import java.util.ArrayList;

public class SimulationRecord {

    public ArrayList<Indicator> averageCoreIndicators = new ArrayList<>();

    public SimulationRecord(CoreIndicators inds) {
        averageCoreIndicators.add(new Indicator(inds.getOwnerOccupierLTVMeanAboveMedian(), Indicator.CoreIndicator.OO_LTV));
        averageCoreIndicators.add(new Indicator(inds.getOwnerOccupierLTIMeanAboveMedian(), Indicator.CoreIndicator.OO_LTI));
        averageCoreIndicators.add(new Indicator(inds.getBuyToLetLTVMean(), Indicator.CoreIndicator.BTL_LTV));
        averageCoreIndicators.add(new Indicator(inds.getHouseholdCreditGrowth(), Indicator.CoreIndicator.CREDIT_GROWTH));
        averageCoreIndicators.add(new Indicator(inds.getDebtToIncome(), Indicator.CoreIndicator.OO_DEBT_TO_INCOME));
        averageCoreIndicators.add(new Indicator(inds.getMortgageApprovals(), Indicator.CoreIndicator.MORTGAGE_APPROVALS));
        averageCoreIndicators.add(new Indicator(inds.getAdvancesToHomeMovers(), Indicator.CoreIndicator.ADVANCES_TO_HOME_OWNERS));
        averageCoreIndicators.add(new Indicator(inds.getAdvancesToFTBs(), Indicator.CoreIndicator.ADVANCES_TO_FTB));
        averageCoreIndicators.add(new Indicator(inds.getAdvancesToBTL(), Indicator.CoreIndicator.ADVANCES_TO_BTL));
        averageCoreIndicators.add(new Indicator(inds.getHousePriceGrowth(), Indicator.CoreIndicator.HOUSE_PRICE_GROWTH));
        averageCoreIndicators.add(new Indicator(inds.getDebtToIncome(), Indicator.CoreIndicator.OO_DEBT_TO_INCOME));
        averageCoreIndicators.add(new Indicator(inds.getRentalYield(), Indicator.CoreIndicator.RENTAL_YIELD));
        averageCoreIndicators.add(new Indicator(inds.getInterestRateSpread(), Indicator.CoreIndicator.INTEREST_RATE_SPREAD));
    }
}
