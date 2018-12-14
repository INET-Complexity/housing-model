package collectors;

import housing.*;

import java.util.Map;

/**************************************************************************************************
 * Class to collect regional household statistics
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseholdStats {

	//------------------//
	//----- Fields -----//
	//------------------//

	// General fields
	private Config  config = Model.config; // Passes the Model's configuration parameters object to a private field

	// Fields for counting numbers of the different types of households and household conditions
	private int     nBTL; // Number of buy-to-let (BTL) households, i.e., households with the BTL gene (includes both active and inactive)
	private int     nActiveBTL; // Number of BTL households with, at least, one BTL property
	private int     nBTLOwnerOccupier; // Number of BTL households owning their home but without any BTL property
	private int     nBTLHomeless; // Number of homeless BTL households
    private int     nBTLBankruptcies; // Number of BTL households going bankrupt in a given time step
    
    //RUBEN - back to private
	public int     nNonBTLOwnerOccupier; // Number of non-BTL households owning their home
	private int     nRenting; // Number of (by definition, non-BTL) households renting their home
	private int     nNonBTLHomeless; // Number of homeless non-BTL households
    private int     nNonBTLBankruptcies; // Number of non-BTL households going bankrupt in a given time step

	// Fields for summing annualised total incomes
	private double  activeBTLAnnualisedTotalIncome;
	private double  ownerOccupierAnnualisedTotalIncome;
	private double  rentingAnnualisedTotalIncome;
	private double  homelessAnnualisedTotalIncome;

	// Other fields
	private double  sumStockYield; // Sum of stock gross rental yields of all currently occupied rental properties
    private int     nNonBTLBidsAboveExpAvSalePrice; // Number of normal (non-BTL) bids with desired housing expenditure above the exponential moving average sale price
    private int     nBTLBidsAboveExpAvSalePrice; // Number of BTL bids with desired housing expenditure above the exponential moving average sale price
    private int     nNonBTLBidsAboveExpAvSalePriceCounter; // Counter for the number of normal (non-BTL) bids with desired housing expenditure above the exp. mov. av. sale price
    private int     nBTLBidsAboveExpAvSalePriceCounter; // Counter for the number of BTL bids with desired housing expenditure above the exp. mov. av. sale price

    //RUBEN additional variable totalConsumption and Savings
    private double totalConsumption;
    private double totalConsumptionCounter;
    private double totalSaving;
    private double totalSavingCounter;
    //RUBEN number of households that have a total negative equity position
    private int nNegativeEquity;
    
    private double totalIncomeConsumption; // sum all consumption out of income
    private double totalFinancialWealthConsumption; // sum all consumption out of wealth
    private double totalIncomeConsumptionCounter; // counter for the sum of all consumption out of income
    private double totalFinancialWealthConsumptionCounter; // counter for the sum of all consumption out of wealth
    private double totalHousingWealthConsumption;
    private double totalHousingWealthConsumptionCounter;
    private double totalDebtConsumption;
    private double totalDebtConsumptionCounter;
    
    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Sets initial values for all relevant variables to enforce a controlled first measure for statistics
     */
    public void init() {
        nBTL = 0;
        nActiveBTL = 0;
        nBTLOwnerOccupier = 0;
        nBTLHomeless = 0;
        nBTLBankruptcies = 0;
        nNonBTLOwnerOccupier = 0;
        nRenting = 0;
        nNonBTLHomeless = 0;
        nNonBTLBankruptcies = 0;
        activeBTLAnnualisedTotalIncome = 0.0;
        ownerOccupierAnnualisedTotalIncome = 0.0;
        rentingAnnualisedTotalIncome = 0.0;
        homelessAnnualisedTotalIncome = 0.0;
        sumStockYield = 0.0;
        nNonBTLBidsAboveExpAvSalePrice = 0;
        nBTLBidsAboveExpAvSalePrice = 0;
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
        //RUBEN initialise totalConsumption and Savings, etc
        totalConsumption = 0.0;
        totalSaving = 0.0;
        nNegativeEquity = 0;
        totalIncomeConsumption = 0.0;
        totalFinancialWealthConsumption = 0.0;
        totalHousingWealthConsumption = 0.0;
        totalDebtConsumption = 0.0;
    }

    public void record() {
        // Initialise variables to sum
        nBTL = 0;
        nActiveBTL = 0;
        nBTLOwnerOccupier = 0;
        nBTLHomeless = 0;
        nBTLBankruptcies = 0;
        nNonBTLOwnerOccupier = 0;
        nRenting = 0;
        nNonBTLHomeless = 0;
        nNonBTLBankruptcies = 0;
        activeBTLAnnualisedTotalIncome = 0.0;
        ownerOccupierAnnualisedTotalIncome = 0.0;
        rentingAnnualisedTotalIncome = 0.0;
        homelessAnnualisedTotalIncome = 0.0;
        sumStockYield = 0.0;
        //RUBEN initialise nNegativeEquity
        nNegativeEquity = 0;
        // Time stamp householdStats mesoRecorders
        Model.microDataRecorder.timeStampSingleRunSingleVariableFiles(Model.getTime(), config.recordBankBalance,
                config.recordHousingWealth, config.recordNHousesOwned, config.recordSavingRate);
        // Run through all households counting population in each type and summing their gross incomes
        for (Household h : Model.households) {

            if (h.getEquityPosition() < 0) {
            	nNegativeEquity++;
            }
            
        	if (h.behaviour.isPropertyInvestor()) {
                ++nBTL;
                if (h.isBankrupt()) nBTLBankruptcies += 1;
                // Active BTL investors
                if (h.nInvestmentProperties() > 0) {
                    ++nActiveBTL;
                    activeBTLAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    // Inactive BTL investors who own their house
                } else if (h.nInvestmentProperties() == 0) {
                    ++nBTLOwnerOccupier;
                    ownerOccupierAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    // Inactive BTL investors in social housing
                } else {
                    ++nBTLHomeless;
                    homelessAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                }
            } else {
                if (h.isBankrupt()) nNonBTLBankruptcies += 1;
                // Non-BTL investors who own their house
                if (h.isHomeowner()) {
                    ++nNonBTLOwnerOccupier;
                    ownerOccupierAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    // Non-BTL investors renting
                } else if (h.isRenting()) {
                    ++nRenting;
                    rentingAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    if (Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality()) > 0) {
                        sumStockYield += h.getHousePayments().get(h.getHome()).monthlyPayment
                                *config.constants.MONTHS_IN_YEAR
                                /Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality());
                    }
                    // Non-BTL investors in social housing
                } else if (h.isInSocialHousing()) {
                    ++nNonBTLHomeless;
                    homelessAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                }
            }
            // Record household micro-data
            if (config.recordBankBalance) {
                Model.microDataRecorder.recordBankBalance(Model.getTime(), h.getBankBalance());
            }
            if (config.recordHousingWealth) {
                double housingWealth = 0.0;
                for (Map.Entry<House, PaymentAgreement> entry : h.getHousePayments().entrySet()) {
                    House house = entry.getKey();
                    PaymentAgreement payment = entry.getValue();
                    if (payment instanceof MortgageAgreement && house.owner == h) {
                        housingWealth += ((MortgageAgreement) payment).purchasePrice
                                - ((MortgageAgreement) payment).principal;
                    }
                }
                Model.microDataRecorder.recordHousingWealth(Model.getTime(), housingWealth);
            }
            if (config.recordNHousesOwned) {
                Model.microDataRecorder.recordNHousesOwned(Model.getTime(), h.nInvestmentProperties() + 1);
            }
            if (config.recordSavingRate) {
                Model.microDataRecorder.recordSavingRate(Model.getTime(), h.getSavingRate());
            }
        }
        // Annualise monthly income data
        activeBTLAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        ownerOccupierAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        rentingAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        homelessAnnualisedTotalIncome *= config.constants.MONTHS_IN_YEAR;
        // Pass number of bidders above the exponential moving average sale price to persistent variable and
        // re-initialise to zero the counter
        nNonBTLBidsAboveExpAvSalePrice = nNonBTLBidsAboveExpAvSalePriceCounter;
        nBTLBidsAboveExpAvSalePrice = nBTLBidsAboveExpAvSalePriceCounter;
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
        totalConsumption = totalConsumptionCounter;
        totalSaving = totalSavingCounter;
        totalConsumptionCounter = 0.0;
        totalSavingCounter = 0.0;
        totalIncomeConsumption = totalIncomeConsumptionCounter;
        totalFinancialWealthConsumption = totalFinancialWealthConsumptionCounter;
        totalHousingWealthConsumption = totalHousingWealthConsumptionCounter;
        totalDebtConsumption = totalDebtConsumptionCounter;
        totalIncomeConsumptionCounter = 0.0;
        totalFinancialWealthConsumptionCounter = 0.0;
        totalHousingWealthConsumptionCounter = 0.0;
        totalDebtConsumptionCounter = 0.0;
        
    }

    /**
     * Count number of normal (non-BTL) bidders with desired expenditures above the (minimum quality, q=0) exponential
     * moving average sale price
     */
    public void countNonBTLBidsAboveExpAvSalePrice(double price) {
        if (price >= Model.housingMarketStats.getExpAvSalePriceForQuality(0)) {
            nNonBTLBidsAboveExpAvSalePriceCounter++;
        }
    }
    
    /**
     * Count number of BTL bidders with desired expenditures above the (minimum quality, q=0) exponential moving average
     * sale price
     */
    public void countBTLBidsAboveExpAvSalePrice(double price) {
        if (price >= Model.housingMarketStats.getExpAvSalePriceForQuality(0)) {
            nBTLBidsAboveExpAvSalePriceCounter++;
        }
    }
    
    // count consumption out of wealth and consumption out of income
    public void countIncomeAndWealthConsumption(double saving, double consumption, double incomeConsumption, double financialWealthConsumption, 
    											double housingWealthConsumption, double debtConsumption) {
    	totalSavingCounter += saving;
    	totalConsumptionCounter += consumption;
    	totalIncomeConsumptionCounter += incomeConsumption;
    	totalFinancialWealthConsumptionCounter += financialWealthConsumption;
    	totalHousingWealthConsumptionCounter += housingWealthConsumption;
    	totalDebtConsumptionCounter += debtConsumption;
    }

    //----- Getter/setter methods -----//

    // Getters for numbers of households variables
    int getnBTL() { return nBTL; }
    int getnActiveBTL() { return nActiveBTL; }
    int getnBTLOwnerOccupier() { return nBTLOwnerOccupier; }
    int getnBTLHomeless() { return nBTLHomeless; }
    int getnBTLBankruptcies() { return nBTLBankruptcies; }
    //RUBEN changed to public
    public int getnNonBTLOwnerOccupier() { return nNonBTLOwnerOccupier; }
    int getnRenting() { return nRenting; }
    int getnNonBTLHomeless() { return nNonBTLHomeless; }
    int getnNonBTLBankruptcies() { return nNonBTLBankruptcies; }
    int getnOwnerOccupier() { return nBTLOwnerOccupier + nNonBTLOwnerOccupier; }
    int getnHomeless() { return nBTLHomeless + nNonBTLHomeless; }
    int getnNonOwner() { return nRenting + getnHomeless(); }

    // Getters for annualised income variables
    double getActiveBTLAnnualisedTotalIncome() { return activeBTLAnnualisedTotalIncome; }
    double getOwnerOccupierAnnualisedTotalIncome() { return ownerOccupierAnnualisedTotalIncome; }
    double getRentingAnnualisedTotalIncome() { return rentingAnnualisedTotalIncome; }
    double getHomelessAnnualisedTotalIncome() { return homelessAnnualisedTotalIncome; }
    double getNonOwnerAnnualisedTotalIncome() {
        return rentingAnnualisedTotalIncome + homelessAnnualisedTotalIncome;
    }

    // Getters for yield variables
    double getSumStockYield() { return sumStockYield; }
    double getAvStockYield() {
        if(nRenting > 0) {
            return sumStockYield/nRenting;
        } else {
            return 0.0;
        }
    }

    // Getters for other variables...
    // ... number of empty houses (total number of houses minus number of non-homeless households)
    int getnEmptyHouses() {
        return Model.construction.getHousingStock() + nBTLHomeless + nNonBTLHomeless - Model.households.size();
    }
    // ... proportion of housing stock owned by buy-to-let investors (all rental properties, plus all empty houses not
    // owned by the construction sector)
    double getBTLStockFraction() {
        return ((double)(getnEmptyHouses() - Model.housingMarketStats.getnUnsoldNewBuild()
                + nRenting))/Model.construction.getHousingStock();
    }
    // ... number of normal (non-BTL) bidders with desired housing expenditure above the exponential moving average sale price
    int getnNonBTLBidsAboveExpAvSalePrice() { return nNonBTLBidsAboveExpAvSalePrice; }
    // ... number of BTL bidders with desired housing expenditure above the exponential moving average sale price
    int getnBTLBidsAboveExpAvSalePrice() { return nBTLBidsAboveExpAvSalePrice; }
    
    //RUBEN getters for totalConsumption and Savings
    double getTotalConsumption() { return totalConsumption; }
    double getTotalSaving() {return totalSaving;}
    double getIncomeConsumption() {return totalIncomeConsumption;}
    double getFinancialWealthConsumption() { return totalFinancialWealthConsumption;}
    double getHousingWealthConsumption() {return totalHousingWealthConsumption;}
    double getDebtConsumption() { return totalDebtConsumption; }
    int getNNegativeEquity() {return nNegativeEquity;}

}
