package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import housing.Config;
import housing.Model;



/*********************************************************************************************
 * 
 * @author Ruben Tarne
 *
 *********************************************************************************************/
public class AgentDecisionRecorder extends CollectorBase{
	
    //------------------//
    //----- Fields -----//
    //------------------//	

	private String 						outputFolderCopy;
	private Config                     	config = Model.config; // Passes the Model's configuration parameters object to a private field	
	public boolean 						active = false;	
	
	public PrintWriter							rentOrBuy;
	public PrintWriter 							decideBuyInvestmentProperty;

    //------------------------//
    //----- Constructors -----//
    //------------------------//
	
	public AgentDecisionRecorder(String outputFolder) {
		outputFolderCopy = outputFolder;
	}
	
    //-------------------//
    //----- Methods -----//
    //-------------------//
	
	// start the recorder and a new file and write into the new file
    public void openNewFiles(int run){
    	if(active) {
	        try{
	        	//TODO insert all the new outputs
	            rentOrBuy = new PrintWriter(outputFolderCopy + "AgentDecisions-rentOrBuy" + run + ".csv", "UTF-8");
	            rentOrBuy.println(
	            		// data from the getMaxMortgage method
	            		"ModelTime, " + "agentID, " +"LTVMaxPurchasePrice, " + "AffordabilityMaxPurchasePrice, " + "LTIMaxPurchasePrice, "
	            		// data from the bidForHome method	
	            		+ "DesiredPurchasePrice, "
	            		// data from the request approval method 
	            		+ "isBTL, " + "LTVPrincipal, " 
	            		+ "affordablePrincipal, " + "LTIPrincipal, " 
	            		// data from the rentOrBuy method
	            		+ "BankBalance, " + "MonthlyDisposableIncome, " 
	            		+ "MonthlyGrossEmploymentIncome, " + "EquityPosition, " 
	            		+ "costOfBuying, " + "costOfRenting, " + "monthlyPayments, " + "approvedMaxPurchasePrice, "
	            		+ "desiredDownPayment, " + "approvedDownPayment, " 
	            		+ "monthlyInterestRate, " + "longTermHPAExpectation, " 
	            		+ "desiredHouseQuality, " + "probabilityBidOnHousingMarket, " + "placeBidOnHousingMarket, ");
	            
	            decideBuyInvestmentProperty = new PrintWriter(outputFolderCopy + "AgentDecisions-InvestmentDecision" + run + ".csv", "UTF-8");
	            decideBuyInvestmentProperty.println(
	            		// data from the maxMortgage method
	            		"ModelTime, " + "agentID, " + "LTVMaxInvestmentPrice, " + "ICRMaxInvestmentPrice, "
	            		// data from the requestApproval method
	            		+ "LTVPrincipal, " + "ICRPrincipal, "
	            		// data from the decideToBuyInvestmentProperty
	            		+ "ModelTestTime2, " + "AgentIDTest2, " + "BankBalance, " + "MonthlyDisposableIncome, "      
	            		+ "MonthlyGrossEmploymentIncome, " + "EquityPosition, " + "bidPrice, " + "EquityOfHouse, " + "LeverageOnMortgage, "
	            		+ "expectedRentalYield, " + "MortgageRate, " +  "expectedEquityYield, " + "HPAExpectation, "
	            		+ "probToInvest, " + "BidOnHousingMarket, " + "Reason, "
	            		);
	            
	            }
	        catch (FileNotFoundException | UnsupportedEncodingException e){
	            e.printStackTrace();
	        }
    	}
    }

    
    public void finish() {
    	if (active) {
	        rentOrBuy.close();
	        decideBuyInvestmentProperty.close();
    	}
    }

	public void setActive(boolean isActive) {
		this.active = isActive;
		if(isActive) {
				Model.housingMarketStats.setActive(true);
				Model.rentalMarketStats.setActive(true);
				//openNewFiles(Model.nSimulation);
		}
	}
}
