package housing;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import org.apache.commons.math3.linear.ArrayRealVector;

/***
 * For recording output to file
 * @author daniel
 *
 */
public class Recorder {
	
	public void start() throws FileNotFoundException, UnsupportedEncodingException {
		// --- open files for core indicators
        ooLTI = new PrintWriter("coreIndicator-ooLTI.csv", "UTF-8");
        btlLTV = new PrintWriter("coreIndicator-btlLTV.csv", "UTF-8");
        creditGrowth = new PrintWriter("coreIndicator-creditGrowth.csv", "UTF-8");
        debtToIncome = new PrintWriter("coreIndicator-debtToIncome.csv", "UTF-8");
        ooDebtToIncome = new PrintWriter("coreIndicator-ooDebtToIncome.csv", "UTF-8");
        mortgageApprovals = new PrintWriter("coreIndicator-mortgageApprovals.csv", "UTF-8");
        housingTransactions = new PrintWriter("coreIndicator-housingTransactions.csv", "UTF-8");
        advancesToFTBs = new PrintWriter("coreIndicator-advancesToFTB.csv", "UTF-8");
        advancesToBTL = new PrintWriter("coreIndicator-advancesToBTL.csv", "UTF-8");
        advancesToHomeMovers = new PrintWriter("coreIndicator-advancesToMovers.csv", "UTF-8");
        priceToIncome = new PrintWriter("coreIndicator-priceToIncome.csv", "UTF-8");
        rentalYield = new PrintWriter("coreIndicator-rentalYield.csv", "UTF-8");
        housePriceGrowth = new PrintWriter("coreIndicator-housePriceGrowth.csv", "UTF-8");
        interestRateSpread = new PrintWriter("coreIndicator-interestRateSpread.csv", "UTF-8");
		
        newSim = true;
	}

	public void step() {
		if(newSim) {
			String simID = Integer.toHexString(UUID.randomUUID().hashCode());
	        try {
				outfile = new PrintWriter("output-"+simID+".csv", "UTF-8");
		    	outfile.println(
		    			"Model time, NRegisteredMortgages, nBtL, nEmpty, nHomeless, nHouseholds, nRenting, AverageBidPrice, "+
		    			"AverageDaysOnMarket, AverageOfferPrice, BTLSalesProportion, FTBSalesProportion, HPA, HPI, nBuyers, "+
		    			"nSellers, nSales, nNewBuild, Rental AverageBidPrice, Rental AverageDaysOnMarket, Rental AverageOfferPrice, Rental HPA, Rental HPI, "+
		    			"Rental nBuyers, Rental nSellers, Rental nSales");
		        paramfile = new PrintWriter("parameters-"+simID+".csv", "UTF-8");
		        paramfile.println("BtL P_INVESTOR, CentralBank ICR Limit");
		        paramfile.println(
		        		data.Households.P_INVESTOR+", "+
		        		Model.centralBank.interestCoverRatioLimit
		        );
		        paramfile.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        // write parameters
	        paramfile.close();
			newSim = false;
		} else {
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
        ooLTI.print(Model.collectors.coreIndicators.getOwnerOccupierLTIMeanAboveMedian());
    	btlLTV.print(Model.collectors.coreIndicators.getBuyToLetLTVMean());
    	creditGrowth.print(Model.collectors.coreIndicators.getHouseholdCreditGrowth());
    	debtToIncome.print(Model.collectors.coreIndicators.getDebtToIncome());
    	ooDebtToIncome.print(Model.collectors.coreIndicators.getOODebtToIncome());
    	mortgageApprovals.print(Model.collectors.coreIndicators.getMortgageApprovals());
    	housingTransactions.print(Model.collectors.coreIndicators.getHousingTransactions());
    	advancesToFTBs.print(Model.collectors.coreIndicators.getAdvancesToFTBs());
    	advancesToBTL.print(Model.collectors.coreIndicators.getAdvancesToBTL());
    	advancesToHomeMovers.print(Model.collectors.coreIndicators.getAdvancesToHomeMovers());
    	priceToIncome.print(Model.collectors.coreIndicators.getPriceToIncome());
    	rentalYield.print(Model.collectors.coreIndicators.getRentalYield());
    	housePriceGrowth.print(Model.collectors.coreIndicators.getHousePriceGrowth());
    	interestRateSpread.print(Model.collectors.coreIndicators.getInterestRateSpread());
    	
    	outfile.println(
    			Model.getTime()+", "+
    			Model.collectors.creditSupply.getNRegisteredMortgages()+", "+
    			Model.collectors.householdStats.getnBtL()+", "+
    			Model.collectors.householdStats.getnEmpty()+", "+
    			Model.collectors.householdStats.getnHomeless()+", "+
    			Model.collectors.householdStats.getnHouseholds()+", "+
    			Model.collectors.householdStats.getnRenting()+", "+
    			Model.collectors.housingMarketStats.getAverageBidPrice()+", "+
    			Model.collectors.housingMarketStats.getAverageDaysOnMarket()+", "+
    			Model.collectors.housingMarketStats.getAverageOfferPrice()+", "+
    			Model.collectors.housingMarketStats.getBTLSalesProportion()+", "+
    			Model.collectors.housingMarketStats.getFTBSalesProportion()+", "+
    			Model.collectors.housingMarketStats.getHPA()+", "+
    			Model.collectors.housingMarketStats.getHPI()+", "+
    			Model.collectors.housingMarketStats.getnBuyers()+", "+
    			Model.collectors.housingMarketStats.getnSellers()+", "+
    			Model.collectors.housingMarketStats.getnSales()+", "+
    			Model.collectors.housingMarketStats.getnNewBuild()+", "+
    			Model.collectors.rentalMarketStats.getAverageBidPrice()+", "+
    			Model.collectors.rentalMarketStats.getAverageDaysOnMarket()+", "+
    			Model.collectors.rentalMarketStats.getAverageOfferPrice()+", "+
    			Model.collectors.rentalMarketStats.getHPA()+", "+
    			Model.collectors.rentalMarketStats.getHPI()+", "+
    			Model.collectors.rentalMarketStats.getnBuyers()+", "+
    			Model.collectors.rentalMarketStats.getnSellers()+", "+
    			Model.collectors.rentalMarketStats.getnSales());
	}
	
	public void finish() {
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
		outfile.close();
	}
		
	public void endOfSim() {
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
		outfile.close();
		newSim = true;
	}
	
	PrintWriter ooLTI;
	PrintWriter btlLTV;
	PrintWriter creditGrowth;
	PrintWriter debtToIncome;
	PrintWriter ooDebtToIncome;
	PrintWriter mortgageApprovals;
	PrintWriter housingTransactions;
	PrintWriter advancesToFTBs;
	PrintWriter advancesToBTL;
	PrintWriter advancesToHomeMovers;
	PrintWriter priceToIncome;
	PrintWriter	rentalYield;
	PrintWriter	housePriceGrowth;
	PrintWriter	interestRateSpread;

	ArrayRealVector output;
	PrintWriter 	outfile;
	PrintWriter 	paramfile;
	public boolean newSim = true;
}
