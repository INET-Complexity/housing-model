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

	public int      nNonBTLOwnerOccupier; // Number of non-BTL households owning their home
	private int     nRenting; // Number of (by definition, non-BTL) households renting their home
	private int     nNonBTLHomeless; // Number of homeless non-BTL households
    private int     nNonBTLBankruptcies; // Number of non-BTL households going bankrupt in a given time step

	// Fields for summing annualised total incomes
	private double  activeBTLAnnualisedTotalIncome;
	private double  ownerOccupierAnnualisedTotalIncome;
	private double  rentingAnnualisedTotalIncome;
	private double  homelessAnnualisedTotalIncome;
	
	// fields for summing annualised net incomes
	private double  activeBTLMonthlyNetIncome;
	private double  ownerOccupierMonthlyNetIncome;
	private double  rentingMonthlyNetIncome;
	private double  homelessMonthlyNetIncome;
	
	// fields of summing monthly employment incomes
	private double activeBTLMonthlyGrossEmploymentIncome;
	private double ownerOccupierMonthlyGrossEmploymentIncome;
	private double rentingMonthlyGrossEmploymentIncome;
	private double homelessMonthlyGrossEmploymentIncome;
	
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
    private double totalBankBalancesBeforeConsumption;
    private double totalBankBalancesBeforeConsumptionCounter;
    private double totalBankBalancesVeryBeginningOfPeriod;
    private double totalBankBalancesVeryBeginningOfPeriodCounter;
    private double totalBankBalanceEndowment;
    private double totalBankBalanceEndowmentCounter;
    private double totalPrincipalRepayments;
    private double totalPrincipalRepaymentsCounter;
    private double totalPrincipalRepaymentsDueToHouseSale;
    private double totalPrincipalRepaymentsDueToHouseSaleCounter;
    private double totalPrincipalPaidBackForInheritance;
    private double totalPrincipalPaidBackForInheritanceCounter;
    private double totalInterestRepayments;
    private double totalInterestRepaymentsCounter;
    private double totalRentalPayments;
    private double totalRentalPaymentsCounter;
    private double totalMonthlyTaxesPaid;
    private double totalMonthlyTaxesPaidCounter;
    private double totalMonthlyNICPaid;
    private double totalMonthlyNICPaidCounter;
    private double totalBankruptcyCashInjection;
    private double totalBankruptcyCashInjectionCounter;
    private double totalDebtReliefOfDeceasedHouseholds; // when households die, they pass on their wealth, if they cannot pay back all their credit, it is forgiven
    private double totalDebtReliefOfDeceasedHouseholdsCounter; 
    private double totalPrincipalRepaymentDeceasedHousehold; // when households die, they pay off as much of their debt as possible
    private double totalPrincipalRepaymentDeceasedHouseholdCounter; 
    // number of households that have a total negative equity position
    private int nNegativeEquity;
    
    private double totalIncomeConsumption; // sum all consumption out of income
    private double totalFinancialWealthConsumption; // sum all consumption out of wealth
    private double totalIncomeConsumptionCounter; // counter for the sum of all consumption out of income
    private double totalFinancialWealthConsumptionCounter; // counter for the sum of all consumption out of wealth
    private double totalHousingWealthConsumption;
    private double totalHousingWealthConsumptionCounter;
    private double totalDebtConsumption;
    private double totalDebtConsumptionCounter;
    private double totalSavingForDeleveraging;
    private double totalSavingForDeleveragingCounter;
    
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
        activeBTLMonthlyNetIncome = 0.0;
    	ownerOccupierMonthlyNetIncome = 0.0;
    	rentingMonthlyNetIncome = 0.0;
    	homelessMonthlyNetIncome = 0.0;
    	activeBTLMonthlyGrossEmploymentIncome = 0.0;
    	ownerOccupierMonthlyGrossEmploymentIncome = 0.0;
    	rentingMonthlyGrossEmploymentIncome = 0.0;
    	homelessMonthlyGrossEmploymentIncome = 0.0;
        sumStockYield = 0.0;
        nNonBTLBidsAboveExpAvSalePrice = 0;
        nBTLBidsAboveExpAvSalePrice = 0;
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
        //RUBEN initialise totalConsumption and Savings, etc
        totalConsumption = 0.0;
        totalSaving = 0.0;
        totalBankBalancesBeforeConsumption = 0.0;
        totalBankBalancesVeryBeginningOfPeriod = 0.0;
        totalBankBalanceEndowment = 0.0;
        totalPrincipalRepayments = 0.0;
        totalPrincipalRepaymentsDueToHouseSale = 0.0;
        totalPrincipalPaidBackForInheritance = 0.0;
        totalInterestRepayments = 0.0;
        totalRentalPayments = 0.0;
        totalMonthlyTaxesPaid = 0.0;
        totalMonthlyNICPaid = 0.0;
        totalBankruptcyCashInjection = 0.0;
        totalDebtReliefOfDeceasedHouseholds = 0.0;
        totalPrincipalRepaymentDeceasedHousehold = 0.0;
        nNegativeEquity = 0;
        totalIncomeConsumption = 0.0;
        totalFinancialWealthConsumption = 0.0;
        totalHousingWealthConsumption = 0.0;
        totalDebtConsumption = 0.0;
        totalSavingForDeleveraging = 0.0;
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
        activeBTLMonthlyNetIncome = 0.0;
    	ownerOccupierMonthlyNetIncome = 0.0;
    	rentingMonthlyNetIncome = 0.0;
    	homelessMonthlyNetIncome = 0.0;
    	activeBTLMonthlyGrossEmploymentIncome = 0.0;
    	ownerOccupierMonthlyGrossEmploymentIncome = 0.0;
    	rentingMonthlyGrossEmploymentIncome = 0.0;
    	homelessMonthlyGrossEmploymentIncome = 0.0;
        sumStockYield = 0.0;
        totalPrincipalRepayments = 0.0;
        totalPrincipalRepaymentsDueToHouseSale = 0.0;
        totalPrincipalPaidBackForInheritance = 0.0;
        totalInterestRepayments = 0.0;
        totalRentalPayments = 0.0;
        totalMonthlyTaxesPaid = 0.0;
        totalMonthlyNICPaid = 0.0;
        totalBankruptcyCashInjection = 0.0;
        //RUBEN initialise nNegativeEquity
        nNegativeEquity = 0;
        // Time stamp householdStats mesoRecorders
        Model.microDataRecorder.timeStampSingleRunSingleVariableFiles(Model.getTime(), config.recordBankBalance,
                config.recordHousingWealth, config.recordNHousesOwned, config.recordSavingRate, config.recordMonthlyGrossTotalIncome,
                config.recordMonthlyGrossEmploymentIncome, config.recordMonthlyGrossRentalIncome,
                config.recordDebt, config.recordConsumption, config.recordIncomeConsumption, config.recordFinancialWealthConsumption, 
                config.recordHousingWealthConsumption, config.recordDebtConsumption, config.recordBTL);
        // Run through all households counting population in each type and summing their gross incomes
        for (Household h : Model.households) {
        	
        	//TODO Ruben: check if removable, as I implemented totalPrincipalRepaymentDeceasedHousehold
        	// record household fields containing credit repayments, rent payments and cash injections
            totalPrincipalRepaymentsCounter += h.getPrincipalPaidBack();
            totalPrincipalRepaymentsDueToHouseSaleCounter += h.getPrincipalDueToHouseSale();
            //totalPrincipalPaidBackForInheritanceCounter += h.getPrincipalPaidBackForInheritance();
            // the principal repayments due to inheritance are recorded before households are managed..
            //... therefore they are set back to zero here and not in the household.step() method
            //h.setPrincipalPaidBackForInheritance(0.0);
            totalInterestRepaymentsCounter += h.getInterestPaidBack();
            totalRentalPaymentsCounter += h.getRentalPayment();
            totalMonthlyTaxesPaidCounter += h.getMonthlyTaxesPaid();
            totalMonthlyNICPaidCounter += h.getMonthlyNICPaid();
            totalBankruptcyCashInjectionCounter += h.getCashInjection();
        	
            if (h.getEquityPosition() < 0) {
            	nNegativeEquity++;
            }
            
        	if (h.behaviour.isPropertyInvestor()) {
                ++nBTL;
                if (h.isBankrupt()) nBTLBankruptcies += 1;
                // Active BTL investors
                if (h.getNProperties() > 1) {
                    ++nActiveBTL;
                    activeBTLAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    activeBTLMonthlyNetIncome += h.getMonthlyNetTotalIncome();
                    activeBTLMonthlyGrossEmploymentIncome += h.getMonthlyGrossEmploymentIncome();
                // Inactive BTL investors who own their house
                } else if (h.getNProperties() == 1) {
                    ++nBTLOwnerOccupier;
                    ownerOccupierAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    ownerOccupierMonthlyNetIncome += h.getMonthlyNetTotalIncome();
                    ownerOccupierMonthlyGrossEmploymentIncome += h.getMonthlyGrossEmploymentIncome();
                    // Inactive BTL investors in social housing
                } else {
                    ++nBTLHomeless;
                    homelessAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    homelessMonthlyNetIncome += h.getMonthlyNetTotalIncome();
                    homelessMonthlyGrossEmploymentIncome += h.getMonthlyGrossEmploymentIncome();
                }
            } else {
                if (h.isBankrupt()) nNonBTLBankruptcies += 1;
                // Non-BTL investors who own their house
                if (h.isHomeowner()) {
                    ++nNonBTLOwnerOccupier;
                    ownerOccupierAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    ownerOccupierMonthlyNetIncome += h.getMonthlyNetTotalIncome();
                    ownerOccupierMonthlyGrossEmploymentIncome += h.getMonthlyGrossEmploymentIncome();
                    // Non-BTL investors renting
                } else if (h.isRenting()) {
                    ++nRenting;
                    rentingAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    rentingMonthlyNetIncome += h.getMonthlyNetTotalIncome();
                    rentingMonthlyGrossEmploymentIncome += h.getMonthlyGrossEmploymentIncome();
                    if (Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality()) > 0) {
                        sumStockYield += h.getHousePayments().get(h.getHome()).monthlyPayment
                                *config.constants.MONTHS_IN_YEAR
                                /Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality());
                    }
                    // Non-BTL investors in social housing
                } else if (h.isInSocialHousing()) {
                    ++nNonBTLHomeless;
                    homelessAnnualisedTotalIncome += h.getMonthlyGrossTotalIncome();
                    homelessMonthlyNetIncome += h.getMonthlyNetTotalIncome();
                    homelessMonthlyGrossEmploymentIncome += h.getMonthlyGrossEmploymentIncome();
                }
            }
            // Record household micro-data
            if (config.recordBankBalance) {
                Model.microDataRecorder.recordBankBalance(Model.getTime(), h.getBankBalance());
            }
            if (config.recordHousingWealth) {
                // Housing wealth is computed as mark-to-market net housing wealth, thus looking at current average
                // prices for houses of the same quality
                double housingWealth = 0.0;
                for (Map.Entry<House, PaymentAgreement> entry : h.getHousePayments().entrySet()) {
                    House house = entry.getKey();
                    PaymentAgreement payment = entry.getValue();
                    if (payment instanceof MortgageAgreement && house.owner == h) {
                        housingWealth += Model.housingMarketStats.getExpAvSalePriceForQuality(house.getQuality())
                                - ((MortgageAgreement) payment).principal;
                    }
                }
                Model.microDataRecorder.recordHousingWealth(Model.getTime(), housingWealth);
            }
            //TODO: this does not seem to work properly, as the total number of houses recorded here is considerably higher than the housing stock
            if (config.recordNHousesOwned) {
                Model.microDataRecorder.recordNHousesOwned(Model.getTime(), h.getNProperties());
            }
            if (config.recordSavingRate) {
                Model.microDataRecorder.recordSavingRate(Model.getTime(), h.getSavingRate());
            }
            if(config.recordMonthlyGrossTotalIncome) {
            	Model.microDataRecorder.recordMonthlyGrossTotalIncome(Model.getTime(), h.getMonthlyGrossTotalIncome());
            }
            if(config.recordMonthlyGrossEmploymentIncome) {
            	Model.microDataRecorder.recordMonthlyGrossEmploymentIncome(Model.getTime(), h.getMonthlyGrossEmploymentIncome());
            }
            if(config.recordMonthlyGrossRentalIncome) {
            	Model.microDataRecorder.recordMonthlyGrossRentalIncome(Model.getTime(), h.returnMonthlyGrossRentalIncome());
            }
            if(config.recordDebt) {
            	Model.microDataRecorder.recordDebt(Model.getTime(), h.getTotalDebt());
            }
            if(config.recordConsumption) {
            	// record non-essential and essential consumption
            	Model.microDataRecorder.recordConsumption(Model.getTime(), (h.getConsumption()+config.ESSENTIAL_CONSUMPTION_FRACTION*config.GOVERNMENT_MONTHLY_INCOME_SUPPORT));
            }
            if(config.recordIncomeConsumption) {
            	// record non-essential income consumption and essential consumption
            	Model.microDataRecorder.recordIncomeConsumption(Model.getTime(), (h.getIncomeConsumption()+config.ESSENTIAL_CONSUMPTION_FRACTION*config.GOVERNMENT_MONTHLY_INCOME_SUPPORT));
            }
            if(config.recordFinancialWealthConsumption) {
            	// record consumption induced by financial wealth
            	Model.microDataRecorder.recordFinancialWealthConsumption(Model.getTime(), (h.getFinancialWealthConsumption()));
            }
            if(config.recordHousingWealthConsumption) {
            	// record consumption induced by housing wealth
            	Model.microDataRecorder.recordHousingWealthConsumption(Model.getTime(), (h.getHousingWealthConsumption()));
            }
            if(config.recordDebtConsumption) {
            	// record consumption induced by debt
            	Model.microDataRecorder.recordDebtConsumption(Model.getTime(), (h.getDebtConsumption()));
            }
            if(config.recordBTL) {
            	Model.microDataRecorder.recordBTL(Model.getTime(), h.behaviour.isPropertyInvestor());
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
        totalBankBalancesBeforeConsumption = totalBankBalancesBeforeConsumptionCounter;
        totalBankBalancesVeryBeginningOfPeriod = totalBankBalancesVeryBeginningOfPeriodCounter;
        totalBankBalanceEndowment = totalBankBalanceEndowmentCounter;
        totalBankBalancesBeforeConsumptionCounter = 0.0;
        totalBankBalancesVeryBeginningOfPeriodCounter = 0.0;
        totalBankBalanceEndowmentCounter = 0.0;
        
        totalPrincipalRepayments = totalPrincipalRepaymentsCounter;
        totalPrincipalRepaymentsDueToHouseSale = totalPrincipalRepaymentsDueToHouseSaleCounter;
        totalPrincipalPaidBackForInheritance = totalPrincipalPaidBackForInheritanceCounter;
        totalInterestRepayments = totalInterestRepaymentsCounter;
        totalRentalPayments = totalRentalPaymentsCounter;
        totalMonthlyTaxesPaid = totalMonthlyTaxesPaidCounter;
        totalMonthlyNICPaid = totalMonthlyNICPaidCounter;
        totalBankruptcyCashInjection = totalBankruptcyCashInjectionCounter;
        totalPrincipalRepaymentsCounter = 0.0;
        totalPrincipalRepaymentsDueToHouseSaleCounter = 0.0;
        totalPrincipalPaidBackForInheritanceCounter = 0.0;
        totalInterestRepaymentsCounter = 0.0;
        totalRentalPaymentsCounter = 0.0;
        totalMonthlyTaxesPaidCounter = 0.0;
        totalMonthlyNICPaidCounter = 0.0;
        totalBankruptcyCashInjectionCounter = 0.0;
        
        totalDebtReliefOfDeceasedHouseholds = totalDebtReliefOfDeceasedHouseholdsCounter;
        totalDebtReliefOfDeceasedHouseholdsCounter = 0.0;
        totalPrincipalRepaymentDeceasedHousehold = totalPrincipalRepaymentDeceasedHouseholdCounter;
        totalPrincipalRepaymentDeceasedHouseholdCounter = 0.0;
        
        totalIncomeConsumption = totalIncomeConsumptionCounter;
        totalFinancialWealthConsumption = totalFinancialWealthConsumptionCounter;
        totalHousingWealthConsumption = totalHousingWealthConsumptionCounter;
        totalDebtConsumption = totalDebtConsumptionCounter;
        totalSavingForDeleveraging = totalSavingForDeleveragingCounter;
        totalIncomeConsumptionCounter = 0.0;
        totalFinancialWealthConsumptionCounter = 0.0;
        totalHousingWealthConsumptionCounter = 0.0;
        totalDebtConsumptionCounter = 0.0;
        totalSavingForDeleveragingCounter = 0.0;
        
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
    											double housingWealthConsumption, double debtConsumption, double savingForDeleveraging) {
    	totalSavingCounter += saving;
    	totalConsumptionCounter += consumption;
    	totalIncomeConsumptionCounter += incomeConsumption;
    	totalFinancialWealthConsumptionCounter += financialWealthConsumption;
    	totalHousingWealthConsumptionCounter += housingWealthConsumption;
    	totalDebtConsumptionCounter += debtConsumption;
    	totalSavingForDeleveragingCounter += savingForDeleveraging;
    }
    
    public void recordBankBalanceBeforeConsumption(double bankBalance) {
    	totalBankBalancesBeforeConsumptionCounter += bankBalance;
    }
    
    public void recordBankBalanceVeryBeginningOfPeriod(double bankBalance) {
    	totalBankBalancesVeryBeginningOfPeriodCounter += bankBalance;
    }
    
    public void recordBankBalanceEndowment(double bankBalance) {
    	totalBankBalanceEndowmentCounter += bankBalance;
    }

	public void recordDebtReliefDeceasedHousehold(double principal) {
		// record the debt relief of a deceased household
		totalDebtReliefOfDeceasedHouseholdsCounter += principal;
	}
	
	public void recordPrincipalRepaymentDeceasedHousehold(double principal) {
		totalPrincipalRepaymentDeceasedHouseholdCounter += principal;
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
    double getNonOwnerAnnualisedTotalIncome() { return rentingAnnualisedTotalIncome + homelessAnnualisedTotalIncome; }
    double getActiveMonthlyNetIncome() { return activeBTLMonthlyNetIncome; }
    double getOwnerOccupierMonthlyNetIncome() { return ownerOccupierMonthlyNetIncome; }
    double getRentingMonthlyNetIncome() { return rentingMonthlyNetIncome; }
    double getHomelessMonthlyNetIncome() { return homelessMonthlyNetIncome; }
    double getNonOwnerMonthlyNetIncome() { 
    	return rentingMonthlyNetIncome + homelessMonthlyNetIncome; 
    } 
    double getMonthlyGrossEmploymentIncome() { return 	
    		activeBTLMonthlyGrossEmploymentIncome +	
    		ownerOccupierMonthlyGrossEmploymentIncome + 
    		rentingMonthlyGrossEmploymentIncome + 
    		homelessMonthlyGrossEmploymentIncome;
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
    
    
    // getters of different types of payments made as well as bank's cash injection and debt relief
    public double getTotalPrincipalRepayments() { return totalPrincipalRepayments;	}
    
    public double getTotalPrincipalRepaymentsDueToHouseSale() { return totalPrincipalRepaymentsDueToHouseSale; }
    
    public double getTotalPrincipalPaidBackForInheritance() { return totalPrincipalPaidBackForInheritance;}

	public double getTotalInterestRepayments() { return totalInterestRepayments;	}

	public double getTotalRentalPayments() { return totalRentalPayments; }

	public double getTotalMonthlyTaxesPaid() { return totalMonthlyTaxesPaid; }

	public double getTotalMonthlyNICPaid() { return totalMonthlyNICPaid; }

	public double getTotalBankruptcyCashInjection() { return totalBankruptcyCashInjection; }

	public double getTotalDebtReliefOfDeceasedHouseholds() { return totalDebtReliefOfDeceasedHouseholds; }

	public double getTotalPrincipalRepaymentDeceasedHouseholds() { return totalPrincipalRepaymentDeceasedHousehold; }
	//RUBEN getters for totalConsumption and Savings
    double getTotalConsumption() { return totalConsumption; }
    double getTotalSaving() {return totalSaving; }
    double getTotalBankBalancesBeforeConsumption() { return totalBankBalancesBeforeConsumption; }
    double getTotalBankBalancesVeryBeginningOfPeriod() { return totalBankBalancesVeryBeginningOfPeriod; }
    double getTotalBankBalanceEndowment() { return totalBankBalanceEndowment; }
    double getIncomeConsumption() { return totalIncomeConsumption; }
    double getFinancialWealthConsumption() { return totalFinancialWealthConsumption; }
    double getHousingWealthConsumption() { return totalHousingWealthConsumption; }
    double getDebtConsumption() { return totalDebtConsumption; }
    double getTotalSavingForDeleveraging() { return totalSavingForDeleveraging; }
    int getNNegativeEquity() { return nNegativeEquity; }

}
