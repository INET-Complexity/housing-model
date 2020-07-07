package collectors;

import housing.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TransactionRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private Config          config = Model.config;      // Passes the Model's configuration parameters object to a private field
    private String          outputFolder;
    private PrintWriter     outfileSaleTransactions;
    private PrintWriter     outfileRentalTransactions;
    private PrintWriter     outfileNBidUpFrequency;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public TransactionRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunFiles(int nRun, boolean recordTransations, boolean recordNBidUpFrequency) {
        // Try opening output files and write first row header with column names
        if (recordTransations) {
            try {
                outfileSaleTransactions = new PrintWriter(outputFolder + "SaleTransactions-run" + nRun + ".csv",
                        "UTF-8");
                outfileSaleTransactions.print("modelTime, "
                        + "houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, buyerId,"
                        + "buyerAge, buyerHasBTLGene, buyerMonthlyGrossTotalIncome, buyerMonthlyGrossEmploymentIncome, "
                        + "buyerMonthlyNetEmploymentIncome, desiredPurchasePrice, buyerPostPurchaseBankBalance, "
                        + "buyerCapGainCoeff, mortgageDownpayment, mortgagePrincipal, mortgageMonthlyPayment, "
                        + "annualInterestRate, ICR, maturity, "
                        + "firstTimeBuyerMortgage, buyToLetMortgage, sellerId, sellerAge, sellerHasBTLGene, "
                        + "sellerMonthlyGrossTotalIncome, sellerMonthlyGrossEmploymentIncome, "
                        + "sellerPostPurchaseBankBalance, sellerCapGainCoeff");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                outfileRentalTransactions = new PrintWriter(outputFolder + "RentalTransactions-run" + nRun + ".csv",
                        "UTF-8");
                outfileRentalTransactions.print("modelTime, "
                        + "houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, buyerId, "
                        + "buyerAge, buyerMonthlyGrossTotalIncome, buyerMonthlyGrossEmploymentIncome, "
                        + "buyerPostPurchaseBankBalance, sellerId, sellerAge, sellerHasBTLGene, "
                        + "sellerMonthlyGrossTotalIncome, sellerMonthlyGrossEmploymentIncome, "
                        + "sellerPostPurchaseBankBalance");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordNBidUpFrequency) {
            try {
                outfileNBidUpFrequency = new PrintWriter(outputFolder + "NBidUpFrequency-run" + nRun
                        + ".csv", "UTF-8");
                outfileNBidUpFrequency.print("Model time, "
                        + "F(nBidUps=0), F(nBidUps=1), F(nBidUps=2), F(nBidUps=3), F(nBidUps=4), F(nBidUps=5), "
                        + "F(nBidUps=6), F(nBidUps=7), F(nBidUps=8), F(nBidUps=9), F(nBidUps=10), F(nBidUps=11), "
                        + "F(nBidUps=12), F(nBidUps=13), F(nBidUps=14), F(nBidUps=15), F(nBidUps=16), F(nBidUps=17), "
                        + "F(nBidUps=18), F(nBidUps=19), F(nBidUps=20)");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    void recordTransaction(HouseBidderRecord purchase, HouseOfferRecord sale, MortgageAgreement mortgage,
                           HousingMarket market) {
        if (config.recordTransactions && (Model.getTime() >= config.TIME_TO_START_RECORDING_TRANSACTIONS)) {
            if (market instanceof HouseSaleMarket) {
                recordSaleTransaction(purchase, sale, mortgage);
            } else {
                recordRentalTransaction(purchase, sale);
            }
        }
    }

    private void recordSaleTransaction(HouseBidderRecord purchase, HouseOfferRecord sale, MortgageAgreement mortgage) {
        double ICR;
        if (mortgage.principal > 0.0 && mortgage.isBuyToLet) {
            ICR = Model.rentalMarketStats.getExpAvFlowYield() * sale.getPrice() /
                    (mortgage.principal * mortgage.getAnnualInterestRate());
        } else {
            ICR = Double.NaN;
        }
        outfileSaleTransactions.format("%n%d, ", Model.getTime());
        outfileSaleTransactions.format("%d, %d, %.2f, %d, %.2f, %d, %.2f, %b, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, "
                        + "%.2f, %.2f, %.2f, %.4f, %.2f, %d, %b, %b, ",
                sale.getHouse().id,
                sale.getHouse().getQuality(),
                sale.getInitialListedPrice(),
                sale.gettInitialListing(),
                sale.getPrice(),
                purchase.getBidder().id,
                purchase.getBidder().getAge(),
                purchase.getBidder().behaviour.isPropertyInvestor(),
                purchase.getBidder().getMonthlyGrossTotalIncome(),
                purchase.getBidder().getMonthlyGrossEmploymentIncome(),
                purchase.getBidder().getMonthlyNetEmploymentIncome(),
                purchase.getBidder().behaviour.getDesiredPurchasePrice(
                        purchase.getBidder().getAnnualGrossEmploymentIncome()),
                purchase.getBidder().getBankBalance(),
                purchase.getBidder().behaviour.getBTLCapGainCoefficient(),
                mortgage.downPayment,
                mortgage.principal,
                mortgage.monthlyPayment,
                mortgage.getAnnualInterestRate(),
                ICR,
                mortgage.getMaturity(),
                mortgage.isFirstTimeBuyer,
                mortgage.isBuyToLet);
        if (sale.getHouse().owner instanceof Household) {
            Household seller = (Household) sale.getHouse().owner;
            outfileSaleTransactions.format("%d, %.2f, %b, %.2f, %.2f, %.2f, %.2f",
                    seller.id,
                    seller.getAge(),
                    seller.behaviour.isPropertyInvestor(),
                    seller.getMonthlyGrossTotalIncome(),
                    seller.getMonthlyGrossEmploymentIncome(),
                    seller.getBankBalance(),
                    seller.behaviour.getBTLCapGainCoefficient());
        } else {
            // must be construction sector
            outfileSaleTransactions.print("-1, -1, false, -1, -1, -1, -1");
        }
    }

    private void recordRentalTransaction(HouseBidderRecord purchase, HouseOfferRecord sale) {
        Household seller = (Household) sale.getHouse().owner;
        outfileRentalTransactions.format("%n%d, ", Model.getTime());
        outfileRentalTransactions.format("%d, %d, %.2f, %d, %.2f, %d, %.2f, %.2f, %.2f, %.2f, %d, %.2f, %b, %.2f, "
                        + "%.2f, %.2f",
                sale.getHouse().id,
                sale.getHouse().getQuality(),
                sale.getInitialListedPrice(),
                sale.gettInitialListing(),
                sale.getPrice(),
                purchase.getBidder().id,
                purchase.getBidder().getAge(),
                purchase.getBidder().getMonthlyGrossTotalIncome(),
                purchase.getBidder().getMonthlyGrossEmploymentIncome(),
                purchase.getBidder().getBankBalance(),
                seller.id,
                seller.getAge(),
                seller.behaviour.isPropertyInvestor(),
                seller.getMonthlyGrossTotalIncome(),
                seller.getMonthlyGrossEmploymentIncome(),
                seller.getBankBalance());
    }

    public void recordNBidUpFrequency(int time, int[] nBidUpFrequency) {
        outfileNBidUpFrequency.format("%n%d, %s", time,
                Arrays.toString(nBidUpFrequency)
                        .replace("[", "")
                        .replace("]", ""));
    }

    public void finishRun(boolean recordTransations, boolean recordNBidUpFrequency) {
        if (recordTransations) {
            outfileSaleTransactions.close();
            outfileRentalTransactions.close();
        }
        if (recordNBidUpFrequency) {
            outfileNBidUpFrequency.close();
        }
    }
}
