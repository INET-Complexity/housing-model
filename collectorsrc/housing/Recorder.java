package housing;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

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
	}

	public void step() {
		if(Model.getTime() > 1) {
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
		} else if(Model.root.nSimulation > 0) {
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
}
