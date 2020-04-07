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

    private String outputFolder;

    private PrintWriter outfileTransactions;
    private PrintWriter outfileNBidUpFrequency;


    private Config                                  config = Model.config; // Passes the Model's configuration parameters object to a private field

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
                outfileTransactions = new PrintWriter(outputFolder + "Transactions-run" + nRun + ".csv", "UTF-8");
                outfileTransactions.print("Model time, "
                        + "transactionType, houseId, houseQuality, initialListedPrice, timeFirstOffered, "
                        + "transactionPrice, buyerId, buyerAge, buyerHasBTLGene, buyerMonthlyGrossTotalIncome, "
                        + "buyerMonthlyGrossEmploymentIncome, buyerPostPurchaseBankBalance, buyerCapGainCoeff, "
                        + "mortgageDownpayment, mortgagePrincipal, firstTimeBuyerMortgage, buyToLetMortgage, sellerId, "
                        + "sellerAge, sellerHasBTLGene, sellerMonthlyGrossTotalIncome, sellerMonthlyGrossEmploymentIncome, "
                        + "sellerPostPurchaseBankBalance, sellerCapGainCoeff");
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
	
	void recordSale(HouseBidderRecord purchase, HouseOfferRecord sale, MortgageAgreement mortgage,
                    HousingMarket market) {
        if (config.recordTransactions) {
            if (Model.getTime() >= config.TIME_TO_START_RECORDING_TRANSACTIONS) {
                outfileTransactions.format("%n%d, ", Model.getTime());
                if (market instanceof HouseSaleMarket) {
                    outfileTransactions.print("sale, ");
                } else {
                    outfileTransactions.print("rental, ");
                }
                outfileTransactions.format("%d, %d, %.2f, %d, %.2f, %d, %.2f, %b, %.2f, %.2f, %.2f, %.2f, ",
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
                        purchase.getBidder().getBankBalance(),
                        purchase.getBidder().behaviour.getBTLCapGainCoefficient());
                if (mortgage != null) {
                    outfileTransactions.format("%.2f, %.2f, %b, %b, ",
                            mortgage.downPayment,
                            mortgage.principal,
                            mortgage.isFirstTimeBuyer,
                            mortgage.isBuyToLet);
                } else {
                    outfileTransactions.print("-1, -1, false, false, ");
                }
                if (sale.getHouse().owner instanceof Household) {
                    Household seller = (Household) sale.getHouse().owner;
                    outfileTransactions.format("%d, %.2f, %b, %.2f, %.2f, %.2f, %.2f",
                            seller.id,
                            seller.getAge(),
                            seller.behaviour.isPropertyInvestor(),
                            seller.getMonthlyGrossTotalIncome(),
                            seller.getMonthlyGrossEmploymentIncome(),
                            seller.getBankBalance(),
                            seller.behaviour.getBTLCapGainCoefficient());
                } else {
                    // must be construction sector
                    outfileTransactions.print("-1, 0, false, 0, 0, 0, 0");
                }
            }
        }
	}

    public void recordNBidUpFrequency(int time, int[] nBidUpFrequency) {
        outfileNBidUpFrequency.format("%n%d, %s", time,
                Arrays.toString(nBidUpFrequency)
                        .replace("[", "")
                        .replace("]", ""));
    }

	public void finishRun(boolean recordTransations, boolean recordNBidUpFrequency) {
        if (recordTransations) {
            outfileTransactions.close();
        }
        if (recordNBidUpFrequency) {
            outfileNBidUpFrequency.close();
        }
    }
}
