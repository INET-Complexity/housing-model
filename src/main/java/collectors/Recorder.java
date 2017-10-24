package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import housing.Model;

/**************************************************************************************************
 * Class to write output to files
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class Recorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String outputFolder;

    private PrintWriter outfile;

    private PrintWriter ooLTI;
    private PrintWriter btlLTV;
    private PrintWriter creditGrowth;
    private PrintWriter debtToIncome;
    private PrintWriter ooDebtToIncome;
    private PrintWriter mortgageApprovals;
    private PrintWriter housingTransactions;
    private PrintWriter advancesToFTBs;
    private PrintWriter advancesToBTL;
    private PrintWriter advancesToHomeMovers;
    private PrintWriter priceToIncome;
    private PrintWriter rentalYield;
    private PrintWriter housePriceGrowth;
    private PrintWriter interestRateSpread;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public Recorder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openMultiRunFiles(boolean recordCoreIndicators) {
        // If recording of core indicators is active...
        if(recordCoreIndicators) {
            // ...try opening necessary files
            try {
                ooLTI = new PrintWriter(outputFolder + "coreIndicator-ooLTI.csv",
                        "UTF-8");
                btlLTV = new PrintWriter(outputFolder + "coreIndicator-btlLTV.csv",
                        "UTF-8");
                creditGrowth = new PrintWriter(outputFolder + "coreIndicator-creditGrowth.csv",
                        "UTF-8");
                debtToIncome = new PrintWriter(outputFolder + "coreIndicator-debtToIncome.csv",
                        "UTF-8");
                ooDebtToIncome = new PrintWriter(outputFolder + "coreIndicator-ooDebtToIncome.csv",
                        "UTF-8");
                mortgageApprovals = new PrintWriter(outputFolder + "coreIndicator-mortgageApprovals.csv",
                        "UTF-8");
                housingTransactions = new PrintWriter(outputFolder + "coreIndicator-housingTransactions.csv",
                        "UTF-8");
                advancesToFTBs = new PrintWriter(outputFolder + "coreIndicator-advancesToFTB.csv",
                        "UTF-8");
                advancesToBTL = new PrintWriter(outputFolder + "coreIndicator-advancesToBTL.csv",
                        "UTF-8");
                advancesToHomeMovers = new PrintWriter(outputFolder + "coreIndicator-advancesToMovers.csv",
                        "UTF-8");
                priceToIncome = new PrintWriter(outputFolder + "coreIndicator-priceToIncome.csv",
                        "UTF-8");
                rentalYield = new PrintWriter(outputFolder + "coreIndicator-rentalYield.csv",
                        "UTF-8");
                housePriceGrowth = new PrintWriter(outputFolder + "coreIndicator-housePriceGrowth.csv",
                        "UTF-8");
                interestRateSpread = new PrintWriter(outputFolder + "coreIndicator-interestRateSpread.csv",
                        "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void openSingleRunFiles(int nRun) {
        // Try opening output files (national and for each region) and write first row header with column names
        try {
            outfile = new PrintWriter(outputFolder + "Output-run" + nRun + ".csv", "UTF-8");
            outfile.println("Model time, "
                    // Number of households of each type
                    + "nNonBTLHomeless, nBTLHomeless, nHomeless, nRenting, nNonOwner, "
                    + "nNonBTLOwnerOccupier, nBTLOwnerOccupier, nOwnerOccupier, nActiveBTL, nBTL, TotalPopulation, "
                    // Numbers of houses of each type
                    + "HousingStock, nNewBuild, nUnsoldNewBuild, nEmptyHouses, BTLStockFraction, "
                    // House sale market data
                    + "Sale HPI, Sale AnnualHPA, Sale AvBidPrice, Sale AvOfferPrice, Sale AvSalePrice, Sale AvDaysOnMarket, "
                    + "Sale nBuyers, Sale nSellers, Sale nSales, Sale BTLSalesProportion, Sale FTBSalesProportion, "
                    // Rental market data
                    + "Rental HPI, Rental AnnualHPA, Rental AvBidPrice, Rental AvOfferPrice, Rental AvSalePrice, Rental AvDaysOnMarket, "
                    + "Rental nBuyers, Rental nSellers, Rental nSales, Rental ExpAvFlowYield, "
                    // Credit data
                    + "nRegisteredMortgages");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void writeTimeStampResults(boolean recordCoreIndicators, int time) {
        if (recordCoreIndicators) {
            // If not at the first point in time...
            if (time > 0) {
                // ...write value separation for core indicators (except for time 0)
                ooLTI.print(", ");
                btlLTV.print(", ");
                creditGrowth.print(", ");
                debtToIncome.print(", ");
                ooDebtToIncome.print(", ");
                mortgageApprovals.print(", ");
                housingTransactions.print(", ");
                advancesToFTBs.print(", ");
                advancesToBTL.print(", ");
                advancesToHomeMovers.print(", ");
                priceToIncome.print(", ");
                rentalYield.print(", ");
                housePriceGrowth.print(", ");
                interestRateSpread.print(", ");
            }
            // Write core indicators results
            ooLTI.print(Model.coreIndicators.getOwnerOccupierLTIMeanAboveMedian());
            btlLTV.print(Model.coreIndicators.getBuyToLetLTVMean());
            creditGrowth.print(Model.coreIndicators.getHouseholdCreditGrowth());
            debtToIncome.print(Model.coreIndicators.getDebtToIncome());
            ooDebtToIncome.print(Model.coreIndicators.getOODebtToIncome());
            mortgageApprovals.print(Model.coreIndicators.getMortgageApprovals());
            housingTransactions.print(Model.coreIndicators.getHousingTransactions());
            advancesToFTBs.print(Model.coreIndicators.getAdvancesToFTBs());
            advancesToBTL.print(Model.coreIndicators.getAdvancesToBTL());
            advancesToHomeMovers.print(Model.coreIndicators.getAdvancesToHomeMovers());
            priceToIncome.print(Model.coreIndicators.getPriceToIncome());
            rentalYield.print(Model.coreIndicators.getAvStockYield());
            housePriceGrowth.print(Model.coreIndicators.getQoQHousePriceGrowth());
            interestRateSpread.print(Model.coreIndicators.getInterestRateSpread());
        }

        // Write general output results to output file
        outfile.println(time + ", " +
                // Number of households of each type
                Model.householdStats.getnNonBTLHomeless() + ", " +
                Model.householdStats.getnBTLHomeless() + ", " +
                Model.householdStats.getnHomeless() + ", " +
                Model.householdStats.getnRenting() + ", " +
                Model.householdStats.getnNonOwner() + ", " +
                Model.householdStats.getnNonBTLOwnerOccupier() + ", " +
                Model.householdStats.getnBTLOwnerOccupier() + ", " +
                Model.householdStats.getnOwnerOccupier() + ", " +
                Model.householdStats.getnActiveBTL() + ", " +
                Model.householdStats.getnBTL() + ", " +
                Model.households.size() + ", " +
                // Numbers of houses of each type
                Model.construction.getHousingStock() + ", " +
                Model.construction.getnNewBuild() + ", " +
                Model.housingMarketStats.getnUnsoldNewBuild() + ", " +
                Model.householdStats.getnEmptyHouses() + ", " +
                Model.householdStats.getBTLStockFraction() + ", " +
                // House sale market data
                Model.housingMarketStats.getHPI() + ", " +
                Model.housingMarketStats.getAnnualHPA() + ", " +
                Model.housingMarketStats.getAvBidPrice() + ", " +
                Model.housingMarketStats.getAvOfferPrice() + ", " +
                Model.housingMarketStats.getAvSalePrice() + ", " +
                Model.housingMarketStats.getAvDaysOnMarket() + ", " +
                Model.housingMarketStats.getnBuyers() + ", " +
                Model.housingMarketStats.getnSellers() + ", " +
                Model.housingMarketStats.getnSales() + ", " +
                Model.housingMarketStats.getBTLSalesProportion() + ", " +
                Model.housingMarketStats.getFTBSalesProportion() + ", " +
                // Rental market data
                Model.rentalMarketStats.getHPI() + ", " +
                Model.rentalMarketStats.getAnnualHPA() + ", " +
                Model.rentalMarketStats.getAvBidPrice() + ", " +
                Model.rentalMarketStats.getAvOfferPrice() + ", " +
                Model.rentalMarketStats.getAvSalePrice() + ", " +
                Model.rentalMarketStats.getAvDaysOnMarket() + ", " +
                Model.rentalMarketStats.getnBuyers() + ", " +
                Model.rentalMarketStats.getnSellers() + ", " +
                Model.rentalMarketStats.getnSales() + ", " +
                Model.rentalMarketStats.getExpAvFlowYield() + ", " +
                // Credit data
                Model.creditSupply.getnRegisteredMortgages());
    }

    public void finishRun(boolean recordCoreIndicators) {
        if (recordCoreIndicators) {
            ooLTI.println("");
            btlLTV.println("");
            creditGrowth.println("");
            debtToIncome.println("");
            ooDebtToIncome.println("");
            mortgageApprovals.println("");
            housingTransactions.println("");
            advancesToFTBs.println("");
            advancesToBTL.println("");
            advancesToHomeMovers.println("");
            priceToIncome.println("");
            rentalYield.println("");
            housePriceGrowth.println("");
            interestRateSpread.println("");
        }
        outfile.close();
    }

    public void finish(boolean recordCoreIndicators) {
        if (recordCoreIndicators) {
            ooLTI.close();
            btlLTV.close();
            creditGrowth.close();
            debtToIncome.close();
            ooDebtToIncome.close();
            mortgageApprovals.close();
            housingTransactions.close();
            advancesToFTBs.close();
            advancesToBTL.close();
            advancesToHomeMovers.close();
            priceToIncome.close();
            rentalYield.close();
            housePriceGrowth.close();
            interestRateSpread.close();
        }
    }
}
