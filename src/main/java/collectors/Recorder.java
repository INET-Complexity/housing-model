package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.util.Locale;

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
    private PrintWriter qualityBandPriceFile;

    private PrintWriter ooLTV;
    private PrintWriter ooLTI;
    private PrintWriter btlLTV;
    private PrintWriter creditGrowth;
    private PrintWriter debtToIncome;
    private PrintWriter ooDebtToIncome;
    private PrintWriter mortgageApprovals;
    private PrintWriter housingTransactions;
    private PrintWriter advancesToFTB;
    private PrintWriter advancesToBTL;
    private PrintWriter advancesToHM;
    private PrintWriter housePriceGrowth;
    private PrintWriter priceToIncome;
    private PrintWriter rentalYield;
    private PrintWriter interestRateSpread;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public Recorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openMultiRunFiles(boolean recordCoreIndicators) {
        // If recording of core indicators is active...
        if(recordCoreIndicators) {
            // ...try opening necessary files
            try {
                ooLTV = new PrintWriter(outputFolder + "coreIndicator-ooLTV.csv",
                        "UTF-8");
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
                advancesToFTB = new PrintWriter(outputFolder + "coreIndicator-advancesToFTB.csv",
                        "UTF-8");
                advancesToBTL = new PrintWriter(outputFolder + "coreIndicator-advancesToBTL.csv",
                        "UTF-8");
                advancesToHM = new PrintWriter(outputFolder + "coreIndicator-advancesToHM.csv",
                        "UTF-8");
                housePriceGrowth = new PrintWriter(outputFolder + "coreIndicator-housePriceGrowth.csv",
                        "UTF-8");
                priceToIncome = new PrintWriter(outputFolder + "coreIndicator-priceToIncome.csv",
                        "UTF-8");
                rentalYield = new PrintWriter(outputFolder + "coreIndicator-rentalYield.csv",
                        "UTF-8");
                interestRateSpread = new PrintWriter(outputFolder + "coreIndicator-interestRateSpread.csv",
                        "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void openSingleRunFiles(int nRun, boolean recordQualityBandPrice, int nQualityBands) {
        // Try opening general output file and write first row header with column names
        try {
            outfile = new PrintWriter(outputFolder + "Output-run" + nRun + ".csv", "UTF-8");
            outfile.print("Model time; "
                    // Number of households of each type
                    + "nNonBTLHomeless; nBTLHomeless; nHomeless; nRenting; nNonOwner; "
                    + "nNonBTLOwnerOccupier; nBTLOwnerOccupier; nOwnerOccupier; nActiveBTL; nBTL; nNonBTLBankrupt; "
                    + "nBTLBankrupt; TotalPopulation; "
                    // Numbers of houses of each type
                    + "HousingStock; nEmptyHouses; BTLStockFraction; "
                    // House sale market data
                    + "Sale HPI; Sale AnnualHPA; Sale AvBidPrice; Sale AvOfferPrice; Sale AvSalePrice; "
                    + "Sale AvMonthsOnMarket; Sale nBuyers; "
                    + "Sale nBTLBuyers; Sale nSellers; Sale nNewSellers; Sale nBTLSellers; Sale nSales; "
                    + "Sale nNonBTLBidsAboveExpAvSalePrice; Sale nBTLBidsAboveExpAvSalePrice; Sale nSalesToBTL; "
                    + "Sale nSalesToFTB; "
                    // Rental market data
                    + "Rental HPI; Rental AnnualHPA; Rental AvBidPrice; Rental AvOfferPrice; Rental AvSalePrice; "
                    + "Rental AvMonthsOnMarket; Rental nBuyers; Rental nSellers; "
                    + "Rental nSales; Rental ExpAvFlowYield; "
                    // Credit data
                    + "nStockMortgages; nNewFTBMortgages; nNewFTBMortgagesToBTL; nNewHMMortgages; "
                    + "nNewBTLMortgages; newFTBCredit; newHMCredit; newBTLCredit; newTotalCredit; interestRate");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // If recording of quality band prices is active...
        if(recordQualityBandPrice) {
            // ...try opening output file and write first row header with column names
            try {
                qualityBandPriceFile = new PrintWriter(outputFolder + "QualityBandPrice-run" + nRun + ".csv", "UTF-8");
                qualityBandPriceFile.print("Time");
                for (int i = 0; i < nQualityBands; i++) {
                    qualityBandPriceFile.format(Locale.ROOT, "; Q%d", i);
                }
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeTimeStampResults(boolean recordCoreIndicators, int time, boolean recordQualityBandPrice) {
        if (recordCoreIndicators) {
            // If not at the first point in time...
            if (time > 0) {
                // ...write value separation for core indicators (except for time 0)
                ooLTV.print("; ");
                ooLTI.print("; ");
                btlLTV.print("; ");
                creditGrowth.print("; ");
                debtToIncome.print("; ");
                ooDebtToIncome.print("; ");
                mortgageApprovals.print("; ");
                housingTransactions.print("; ");
                advancesToFTB.print("; ");
                advancesToBTL.print("; ");
                advancesToHM.print("; ");
                housePriceGrowth.print("; ");
                priceToIncome.print("; ");
                rentalYield.print("; ");
                interestRateSpread.print("; ");
            }
            // Write core indicators results
            ooLTV.format(Locale.ROOT, "%.4f", Model.coreIndicators.getOwnerOccupierLTVMeanAboveMedian());
            ooLTI.format(Locale.ROOT, "%.4f", Model.coreIndicators.getOwnerOccupierLTIMeanAboveMedian());
            btlLTV.format(Locale.ROOT, "%.4f", Model.coreIndicators.getBuyToLetLTVMean());
            creditGrowth.format(Locale.ROOT, "%.4f", Model.coreIndicators.getHouseholdCreditGrowth());
            debtToIncome.format(Locale.ROOT, "%.4f", Model.coreIndicators.getMortgageDebtToIncome());
            ooDebtToIncome.format(Locale.ROOT, "%.4f", Model.coreIndicators.getOOMortgageDebtToIncome());
            mortgageApprovals.format(Locale.ROOT, "%d", Model.coreIndicators.getMortgageApprovals());
            housingTransactions.format(Locale.ROOT, "%d", Model.coreIndicators.getHousingTransactions());
            advancesToFTB.format(Locale.ROOT, "%d", Model.coreIndicators.getAdvancesToFTB());
            advancesToBTL.format(Locale.ROOT, "%d", Model.coreIndicators.getAdvancesToBTL());
            advancesToHM.format(Locale.ROOT, "%d", Model.coreIndicators.getAdvancesToHM());
            housePriceGrowth.format(Locale.ROOT, "%.4f", Model.coreIndicators.getHousePriceGrowth());
            priceToIncome.format(Locale.ROOT, "%.4f", Model.coreIndicators.getPriceToIncome());
            rentalYield.format(Locale.ROOT, "%.4f", Model.coreIndicators.getAvStockRentalYield());
            interestRateSpread.format(Locale.ROOT, "%.4f", Model.coreIndicators.getInterestRateSpread());
        }

        // Write general output results to output file
        outfile.format(Locale.ROOT, "%n%d; %d; %d; %d; %d; %d; %d; %d; %d; %d; %d; %d; %d; %d; " +
                        "%d; %d; %.4f; " +
                        "%.4f; %.4e; %.2f; %.2f; %.2f; %.4f; %d; %d; %d; %d; %d; %d; %d; %d; %d; %d; " +
                        "%.4f; %.4e; %.2f; %.2f; %.2f; %.4f; %d; %d; %d; %.4f; " +
                        "%d; %d; %d; %d; %d; %.2f; %.2f; %.2f; %.2f; %.6f", time,
                // Number of households of each type
                Model.householdStats.getnNonBTLHomeless(),
                Model.householdStats.getnBTLHomeless(),
                Model.householdStats.getnHomeless(),
                Model.householdStats.getnRenting(),
                Model.householdStats.getnNonOwner(),
                Model.householdStats.getnNonBTLOwnerOccupier(),
                Model.householdStats.getnBTLOwnerOccupier(),
                Model.householdStats.getnOwnerOccupier(),
                Model.householdStats.getnActiveBTL(),
                Model.householdStats.getnBTL(),
                Model.householdStats.getnNonBTLBankruptcies(),
                Model.householdStats.getnBTLBankruptcies(),
                Model.households.size(),
                // Numbers of houses of each type
                Model.construction.getHousingStock(),
                Model.householdStats.getnEmptyHouses(),
                Model.householdStats.getBTLStockFraction(),
                // House sale market data
                Model.housingMarketStats.getHPI(),
                Model.housingMarketStats.getAnnualHPA(),
                Model.housingMarketStats.getAvBidPrice(),
                Model.housingMarketStats.getAvOfferPrice(),
                Model.housingMarketStats.getAvSalePrice(),
                Model.housingMarketStats.getAvMonthsOnMarket(),
                Model.housingMarketStats.getnBuyers(),
                Model.housingMarketStats.getnBTLBuyers(),
                Model.housingMarketStats.getnSellers(),
                Model.housingMarketStats.getnNewSellers(),
                Model.housingMarketStats.getnBTLSellers(),
                Model.housingMarketStats.getnSales(),
                Model.householdStats.getnNonBTLBidsAboveExpAvSalePrice(),
                Model.householdStats.getnBTLBidsAboveExpAvSalePrice(),
                Model.housingMarketStats.getnSalesToBTL(),
                Model.housingMarketStats.getnSalesToFTB(),
                // Rental market data
                Model.rentalMarketStats.getHPI(),
                Model.rentalMarketStats.getAnnualHPA(),
                Model.rentalMarketStats.getAvBidPrice(),
                Model.rentalMarketStats.getAvOfferPrice(),
                Model.rentalMarketStats.getAvSalePrice(),
                Model.rentalMarketStats.getAvMonthsOnMarket(),
                Model.rentalMarketStats.getnBuyers(),
                Model.rentalMarketStats.getnSellers(),
                Model.rentalMarketStats.getnSales(),
                Model.rentalMarketStats.getExpAvFlowYield(),
                // Credit data
                Model.creditSupply.getnStockMortgages(),
                Model.creditSupply.getnNewFTBMortgages(),
                Model.creditSupply.getnNewFTBMortgagesToBTL(),
                Model.creditSupply.getnNewHMMortgages(),
                Model.creditSupply.getnNewBTLMortgages(),
                Model.creditSupply.getNewCreditToFTB(),
                Model.creditSupply.getNewCreditToHM(),
                Model.creditSupply.getNewCreditToBTL(),
                Model.creditSupply.getNewCreditTotal(),
                Model.creditSupply.getInterestRate());

        // Write quality band prices to file
        if (recordQualityBandPrice) {
            qualityBandPriceFile.format(Locale.ROOT, "%n%d", time);
            for (double element : Model.housingMarketStats.getAvSalePricePerQuality()) {
                qualityBandPriceFile.format(Locale.ROOT, "; %.2f", element);
            }
        }
    }

    public void finishRun(boolean recordCoreIndicators, boolean recordQualityBandPrice, boolean lastRun) {
        if (recordCoreIndicators && !lastRun) {
            ooLTV.println("");
            ooLTI.println("");
            btlLTV.println("");
            creditGrowth.println("");
            debtToIncome.println("");
            ooDebtToIncome.println("");
            mortgageApprovals.println("");
            housingTransactions.println("");
            advancesToFTB.println("");
            advancesToBTL.println("");
            advancesToHM.println("");
            housePriceGrowth.println("");
            priceToIncome.println("");
            rentalYield.println("");
            interestRateSpread.println("");
        }
        outfile.close();
        if (recordQualityBandPrice) {
            qualityBandPriceFile.close();
        }
    }

    public void finish(boolean recordCoreIndicators) {
        if (recordCoreIndicators) {
            ooLTV.close();
            ooLTI.close();
            btlLTV.close();
            creditGrowth.close();
            debtToIncome.close();
            ooDebtToIncome.close();
            mortgageApprovals.close();
            housingTransactions.close();
            advancesToFTB.close();
            advancesToBTL.close();
            advancesToHM.close();
            housePriceGrowth.close();
            priceToIncome.close();
            rentalYield.close();
            interestRateSpread.close();
        }
    }
}
