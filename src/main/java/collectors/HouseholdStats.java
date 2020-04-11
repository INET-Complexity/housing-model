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
    private int     nNonBTLOwnerOccupier; // Number of non-BTL households owning their home
    private int     nRenting; // Number of (by definition, non-BTL) households renting their home
    private int     nNonBTLHomeless; // Number of homeless non-BTL households
    private int     nNonBTLBankruptcies; // Number of non-BTL households going bankrupt in a given time step

    // Fields for summing annualised net total incomes
    private double  activeBTLAnnualisedNetTotalIncome;
    private double  ownerOccupierAnnualisedNetTotalIncome;
    private double  rentingAnnualisedNetTotalIncome;
    private double  homelessAnnualisedNetTotalIncome;

    // Other fields
    private double  sumStockYield; // Sum of stock gross rental yields of all currently occupied rental properties
    private int     nNonBTLBidsAboveExpAvSalePrice; // Number of normal (non-BTL) bids with desired housing expenditure above the exponential moving average sale price
    private int     nBTLBidsAboveExpAvSalePrice; // Number of BTL bids with desired housing expenditure above the exponential moving average sale price
    private int     nNonBTLBidsAboveExpAvSalePriceCounter; // Counter for the number of normal (non-BTL) bids with desired housing expenditure above the exp. mov. av. sale price
    private int     nBTLBidsAboveExpAvSalePriceCounter; // Counter for the number of BTL bids with desired housing expenditure above the exp. mov. av. sale price

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Set initial values for variables for which a controlled first measure per run is needed
     */
    public void init() {
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
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
        activeBTLAnnualisedNetTotalIncome = 0.0;
        ownerOccupierAnnualisedNetTotalIncome = 0.0;
        rentingAnnualisedNetTotalIncome = 0.0;
        homelessAnnualisedNetTotalIncome = 0.0;
        sumStockYield = 0.0;
        // Time stamp householdStats microDataRecorders
        Model.microDataRecorder.timeStampSingleRunSingleVariableFiles(Model.getTime(), config.recordEmploymentIncome,
                config.recordRentalIncome, config.recordBankBalance, config.recordHousingWealth,
                config.recordNHousesOwned, config.recordAge, config.recordSavingRate);
        // Run through all households counting population in each type and summing their gross incomes
        for (Household h : Model.households) {
            if (h.behaviour.isPropertyInvestor()) {
                ++nBTL;
                if (h.isBankrupt()) nBTLBankruptcies += 1;
                // Active BTL investors
                if (h.getNProperties() > 1) {
                    ++nActiveBTL;
                    activeBTLAnnualisedNetTotalIncome += h.getMonthlyNetTotalIncome();
                // Inactive BTL investors who own their house
                } else if (h.getNProperties() == 1) {
                    ++nBTLOwnerOccupier;
                    ownerOccupierAnnualisedNetTotalIncome += h.getMonthlyNetTotalIncome();
                // Inactive BTL investors in social housing
                } else {
                    ++nBTLHomeless;
                    homelessAnnualisedNetTotalIncome += h.getMonthlyNetTotalIncome();
                }
            } else {
                if (h.isBankrupt()) nNonBTLBankruptcies += 1;
                // Non-BTL investors who own their house
                if (h.isHomeowner()) {
                    ++nNonBTLOwnerOccupier;
                    ownerOccupierAnnualisedNetTotalIncome += h.getMonthlyNetTotalIncome();
                    // Non-BTL investors renting
                } else if (h.isRenting()) {
                    ++nRenting;
                    rentingAnnualisedNetTotalIncome += h.getMonthlyNetTotalIncome();
                    if (Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality()) > 0) {
                        sumStockYield += h.getHousePayments().get(h.getHome()).monthlyPayment
                                *config.constants.MONTHS_IN_YEAR
                                /Model.housingMarketStats.getExpAvSalePriceForQuality(h.getHome().getQuality());
                    }
                    // Non-BTL investors in social housing
                } else if (h.isInSocialHousing()) {
                    ++nNonBTLHomeless;
                    homelessAnnualisedNetTotalIncome += h.getMonthlyNetTotalIncome();
                }
            }
            // Record household micro-data
            if (config.recordEmploymentIncome) {
                Model.microDataRecorder.recordEmploymentIncome(Model.getTime(), h.getMonthlyGrossEmploymentIncome());
            }
            if (config.recordRentalIncome) {
                Model.microDataRecorder.recordRentalIncome(Model.getTime(), h.getMonthlyGrossRentalIncome());
            }
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
            if (config.recordNHousesOwned) {
                Model.microDataRecorder.recordNHousesOwned(Model.getTime(), h.getNProperties());
            }
            if (config.recordAge) {
                Model.microDataRecorder.recordAge(Model.getTime(), h.getAge());
            }
            if (config.recordSavingRate) {
                Model.microDataRecorder.recordSavingRate(Model.getTime(), h.getSavingRate());
            }
        }
        // Annualise monthly income data
        activeBTLAnnualisedNetTotalIncome *= config.constants.MONTHS_IN_YEAR;
        ownerOccupierAnnualisedNetTotalIncome *= config.constants.MONTHS_IN_YEAR;
        rentingAnnualisedNetTotalIncome *= config.constants.MONTHS_IN_YEAR;
        homelessAnnualisedNetTotalIncome *= config.constants.MONTHS_IN_YEAR;
        // Pass number of bidders above the exponential moving average sale price to persistent variable and
        // re-initialise to zero the counter
        nNonBTLBidsAboveExpAvSalePrice = nNonBTLBidsAboveExpAvSalePriceCounter;
        nBTLBidsAboveExpAvSalePrice = nBTLBidsAboveExpAvSalePriceCounter;
        nNonBTLBidsAboveExpAvSalePriceCounter = 0;
        nBTLBidsAboveExpAvSalePriceCounter = 0;
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

    //----- Getter/setter methods -----//

    // Getters for numbers of households variables
    int getnBTL() { return nBTL; }
    int getnActiveBTL() { return nActiveBTL; }
    int getnBTLOwnerOccupier() { return nBTLOwnerOccupier; }
    int getnBTLHomeless() { return nBTLHomeless; }
    int getnBTLBankruptcies() { return nBTLBankruptcies; }
    int getnNonBTLOwnerOccupier() { return nNonBTLOwnerOccupier; }
    int getnRenting() { return nRenting; }
    int getnNonBTLHomeless() { return nNonBTLHomeless; }
    int getnNonBTLBankruptcies() { return nNonBTLBankruptcies; }
    int getnOwnerOccupier() { return nBTLOwnerOccupier + nNonBTLOwnerOccupier; }
    int getnHomeless() { return nBTLHomeless + nNonBTLHomeless; }
    int getnNonOwner() { return nRenting + getnHomeless(); }

    // Getters for annualised income variables
    double getActiveBTLAnnualisedNetTotalIncome() { return activeBTLAnnualisedNetTotalIncome; }
    double getOwnerOccupierAnnualisedNetTotalIncome() { return ownerOccupierAnnualisedNetTotalIncome; }
    double getRentingAnnualisedNetTotalIncome() { return rentingAnnualisedNetTotalIncome; }
    double getHomelessAnnualisedNetTotalIncome() { return homelessAnnualisedNetTotalIncome; }

    // Getters for yield variables
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

}
