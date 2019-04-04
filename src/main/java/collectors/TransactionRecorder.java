package collectors;

import housing.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class TransactionRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String outputFolder;

    private PrintWriter outfile;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public TransactionRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunFiles(int nRun) {
        // Try opening output files and write first row header with column names
        try {
            outfile = new PrintWriter(outputFolder + "Transactions-run" + nRun + ".csv", "UTF-8");
            outfile.println("Model time, "
                    + "transactionType, houseId, houseQuality, initialListedPrice, timeFirstOffered, "
                    + "transactionPrice, rentalYield, buyerId, buyerAge, buyerHasBTLGene, buyerMonthlyGrossTotalIncome, "
                    + "buyerMonthlyGrossEmploymentIncome, buyerPostPurchaseBankBalance, buyerCapGainCoeff, "
                    + "mortgageDownpayment, firstTimeBuyerMortgage, buyToLetMortgage, sellerId, sellerAge, "
                    + "sellerHasBTLGene, sellerMonthlyGrossTotalIncome, sellerMonthlyGrossEmploymentIncome, "
                    + "sellerPostPurchaseBankBalance, sellerCapGainCoeff");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
	
	void recordSale(HouseBidderRecord purchase, HouseOfferRecord sale, MortgageAgreement mortgage,
                    HousingMarket market) {
		outfile.print(Model.getTime() + ", ");
		if (market instanceof HouseSaleMarket) {
			outfile.print("sale, ");
		} else {
			outfile.print("rental, ");
		}
		outfile.print(
    			sale.getHouse().id + ", " +
    			sale.getHouse().getQuality() + ", " +
				sale.getInitialListedPrice() + ", " +
				sale.gettInitialListing() + ", " +
    			sale.getPrice() + ", " +
				sale.getYield() + ", " + 
    			purchase.getBidder().id + ", " +
    			purchase.getBidder().getAge() + ", " +
    			purchase.getBidder().behaviour.isPropertyInvestor() + ", " +
    			purchase.getBidder().getMonthlyGrossTotalIncome() + ", " +
    			purchase.getBidder().getMonthlyGrossEmploymentIncome() + ", " +
    			purchase.getBidder().getBankBalance() + ", "+
    			purchase.getBidder().behaviour.getBTLCapGainCoefficient() + ", ");
		if (mortgage != null) {
			outfile.print(
					mortgage.downPayment + ", " +
					mortgage.isFirstTimeBuyer + ", " +
					mortgage.isBuyToLet + ", ");
		} else {
			outfile.print("-1, false, false, ");
		}
		if (sale.getHouse().owner instanceof Household) {
			Household seller = (Household) sale.getHouse().owner;
			outfile.print(
					seller.id + ", " +
					seller.getAge() + ", " +
					seller.behaviour.isPropertyInvestor() + ", " +
					seller.getMonthlyGrossTotalIncome() + ", " +
					seller.getMonthlyGrossEmploymentIncome() + ", " +
					seller.getBankBalance() + ", " +
					seller.behaviour.getBTLCapGainCoefficient() +", ");
		} else {
			// must be construction sector
			outfile.println("-1, 0, false, 0, 0, 0, 0");
		}
	}

	public void finishRun() { outfile.close(); }
}
