package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class OfferAndBidRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String outputFolder;

    public PrintWriter outfile;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public OfferAndBidRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunFiles(int nRun) {
        // Try opening output files and write first row header with column names
        try {
            outfile = new PrintWriter(outputFolder + "OffersMatchedWithBids-run" + nRun + ".csv", "UTF-8");
            outfile.println("Model time, "
                    + "CounterOfferInThisClearingRound, OfferID, HouseID, OfferQuality, OfferYield, OfferPrice, "
                    + "InitialOfferPrice, TimeOfInitialListing, BidPrice1, BidderId1, BidderBankBalance1, BidderIsBTL1, "
                    + "BidPrice2, BidderId2, BidderBankBalance1, BidderIsBTL2, "
                    // After the last bid recorded, the ID of the winning bidder and the transaction price are recorded
                    // As the number of bids could be higher or lower, it is not guaranteed that the headings will fit
                    + "WinningBid OR BidPrice3, BidderId3 OR TransactionPrice, BidderBankBalance1, BidderIsBTL3, "
                    );
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

	public void finishRun() { outfile.close(); }
}
