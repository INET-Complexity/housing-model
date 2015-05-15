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
	}

	public void step() {
		if(Model.t > 1) {
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
		} else if(Model.nSimulation > 0) {
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
		}
        ooLTI.print(Collectors.coreIndicators.getOwnerOccupierLTIMeanAboveMedian());
    	btlLTV.print(Collectors.coreIndicators.getBuyToLetLTVMean());
    	creditGrowth.print(Collectors.coreIndicators.getHouseholdCreditGrowth());
    	debtToIncome.print(Collectors.coreIndicators.getDebtToIncome());
    	ooDebtToIncome.print(Collectors.coreIndicators.getOODebtToIncome());
    	mortgageApprovals.print(Collectors.coreIndicators.getMortgageApprovals());
    	housingTransactions.print(Collectors.coreIndicators.getHousingTransactions());
    	advancesToFTBs.print(Collectors.coreIndicators.getAdvancesToFTBs());
    	advancesToBTL.print(Collectors.coreIndicators.getAdvancesToBTL());
    	advancesToHomeMovers.print(Collectors.coreIndicators.getAdvancesToHomeMovers());
    	priceToIncome.print(Collectors.coreIndicators.getPriceToIncome());
    	rentalYield.print(Collectors.coreIndicators.getRentalYield());
    	housePriceGrowth.print(Collectors.coreIndicators.getHousePriceGrowth());
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
}
