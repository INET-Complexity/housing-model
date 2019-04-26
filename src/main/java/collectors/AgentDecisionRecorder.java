package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import housing.Bank;
import housing.Config;
import housing.House;
import housing.Household;
import housing.HouseholdBehaviour;
import housing.Model;
import housing.MortgageAgreement;

//import housing.Config;
//import housing.Model;



/*********************************************************************************************
 * 
 * @author Ruben Tarne
 *
 *********************************************************************************************/
public class AgentDecisionRecorder{
	
    //------------------//
    //----- Fields -----//
    //------------------//	

	private String 						outputFolderCopy;
	private Config                     	config = Model.config; // Passes the Model's configuration parameters object to a private field	
	
	public PrintWriter					rentOrBuy;
	public PrintWriter 					decideBuyInvestmentProperty;
	public PrintWriter					decideSellInvestmentProperty;

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
        try{
        	//TODO insert all the new outputs
            rentOrBuy = new PrintWriter(outputFolderCopy + "AgentDecisions-rentOrBuy" + run + ".csv", "UTF-8");
            rentOrBuy.println(
            		// data from the getMaxMortgage method
            		"ModelTime, " 
            		+ "agentID, " 
            		+ "LTVMaxPurchasePrice, " 
            		+ "AffordabilityMaxPurchasePrice, " 
            		+ "LTIMaxPurchasePrice, "
            		// data from the bidForHome method	
            		+ "DesiredPurchasePrice, "
            		// data from the request approval method 
            		+ "isBTL, " 
            		+ "LTVPrincipal, " 
            		+ "affordablePrincipal, " 
            		+ "LTIPrincipal, " 
            		// data from the rentOrBuy method
            		+ "BankBalance, " + "MonthlyDisposableIncome, " 
            		+ "MonthlyGrossEmploymentIncome, " + "EquityPosition, " 
            		+ "costOfBuying, " + "costOfRenting, " + "monthlyPayments, " + "approvedMaxPurchasePrice, "
            		+ "desiredDownPayment, " + "approvedDownPayment, " 
            		+ "monthlyInterestRate, " + "longTermHPAExpectation, " + "HPI, "
            		+ "desiredHouseQuality, " + "probabilityBidOnHousingMarket, " + "placeBidOnHousingMarket, ");
            
            decideBuyInvestmentProperty = new PrintWriter(outputFolderCopy + "AgentDecisions-InvestmentDecision" + run + ".csv", "UTF-8");
            decideBuyInvestmentProperty.println(
            		// data from the maxMortgage method
            		"ModelTime, " 
            		+ "agentID, " 
            		+ "LTVMaxInvestmentPrice, " 
            		+ "ICRMaxInvestmentPrice, "
            		// data from the requestApproval method
            		+ "LTVPrincipal, " 
            		+ "ICRPrincipal, "
            		// data from the decideToBuyInvestmentProperty
            		+ "ModelTestTime2, " + "AgentIDTest2, " + "BankBalance, " + "MonthlyDisposableIncome, "      
            		+ "MonthlyGrossEmploymentIncome, " + "EquityPosition, " + "bidPrice, " 
            		+ "EquityOfHouse, " + "LeverageOnMortgage, " + "expectedRentalYield, " 
            		+ "MortgageRate, " +  "expectedEquityYield, " + "CapitalGainCoefficient, " 
            		+ "HPAExpectation, "+ "probToInvest, " + "BidOnHousingMarket, " 
            		+ "Reason, " + "bankBalance, " + "desiredBankBalance, " + "desiredBankBalanceMin, "
            		+ "monthlyMortgagePrincipalPayments, monthylInterestRepayments, HPI, "
            		);
            
            decideSellInvestmentProperty = new PrintWriter(outputFolderCopy + "AgentDecisions-DivestmentDecision" + run + ".csv", "UTF-8");
            decideSellInvestmentProperty.println(
            		// print data from the behaviourdecideToSellInvestmentProperty method
            		"ModelTime, " + "agentID, " + "only 2 houses, " + "bankBalance, " + "monthlyDisposableIncome, "
            		+ "MonthlyGrossEmploymentIncome, " + "EquityPosition, " + "CapitalGainCoeff," + "houseQuality, " + "currentMarketPrice, "
            		+ "equityOfHouse, " + "leverageOfHouse, " + "currentRentalYield, " + "mortgageRate, " 
            		+ "rentalExpAvFlowYield, "+ "longTermHPAExpectation, " + "expectedEquityYield, "
            		+ "probToKeepHouse, " + "sellHouse, " + "initialSalePrice, " + "HPI, "
            		);
            }
	        catch (FileNotFoundException | UnsupportedEncodingException e){
	            e.printStackTrace();
	        }
    }
    
    public void recordMaxMortgageSH(Household h, double ltv_max_price, 
    		double affordability_max_price, double lti_max_price) {
    	Model.agentDecisionRecorder.rentOrBuy.print(Model.getTime() 
				+ ", " + h.id
				+ ", " + String.format("%.2f", ltv_max_price)
				+ ", " + String.format("%.2f", affordability_max_price)
				+ ", " + String.format("%.2f", lti_max_price)
				+ ", ");
    }
    
    public void recordDesiredPurchasePriceSH(double desiredPurchasePrice) {
    	Model.agentDecisionRecorder.rentOrBuy.print(String.format("%.2f", desiredPurchasePrice) + ", ");
    }

    public void recordLoanRequestSH(double ltv_principal, 
    		double affordable_principal, double lti_principal) {
		Model.agentDecisionRecorder.rentOrBuy.print("false"
				+ ", " + String.format("%.2f", ltv_principal)
				+ ", " + String.format("%.2f", affordable_principal)
				+ ", " + String.format("%.2f", lti_principal)
				+ ", "
				);	
    }
    
    // recorder called from behaviour.rentOrPurchase(), to record when household can't afford house of quality 0
    public void recordCantAffordHouseRentOrPurchase(Household me, MortgageAgreement mortgageApproval,
    		double purchasePrice, double newHouseQuality, double desiredDownPayment) {
    	Model.agentDecisionRecorder.rentOrBuy.println(String.format("%.2f", me.getBankBalance())
    			+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
    			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
    			+ ", " + String.format("%.2f", me.getEquityPosition())
    			+ ", " + ", " 
    			+ ", " + String.format("%.2f", mortgageApproval.monthlyPayment)
    			+ ", " + String.format("%.2f", purchasePrice)
    			+ ", " + String.format("%.2f", desiredDownPayment)
    			+ ", " + String.format("%.2f", mortgageApproval.downPayment)
    			+ ", " + String.format("%.6f", mortgageApproval.monthlyInterestRate)
    			+ ", " + String.format("%.6f", me.behaviour.getLongTermHPAExpectation())
    			+ ", " + String.format("%.4f", Model.housingMarketStats.getHPI())
    			+ ", " + newHouseQuality
    			+ ", " + "0"
    			+ ", " + "false"
    			+ ", " );
    }
    
    public void recordDecisionRentOrPurchase(Household me, MortgageAgreement mortgageApproval,
    		double costOfHouse, double costOfRent, double purchasePrice, double desiredDownPayment,
    		double newHouseQuality, double probabilityPlaceBidOnHousingMarket, 
    		boolean placeBidOnHousingMarket) {
    	Model.agentDecisionRecorder.rentOrBuy.println(String.format("%.2f", me.getBankBalance())
    			+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
    			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
    			+ ", " + String.format("%.2f", me.getEquityPosition())
    			+ ", " + String.format("%.2f", costOfHouse)
    			+ ", " + String.format("%.2f", costOfRent*(1.0 + config.PSYCHOLOGICAL_COST_OF_RENTING))
    			+ ", " + String.format("%.2f", mortgageApproval.monthlyPayment)
    			+ ", " + String.format("%.2f", purchasePrice)
    			+ ", " + String.format("%.2f", desiredDownPayment)
    			+ ", " + String.format("%.2f", mortgageApproval.downPayment)
    			+ ", " + String.format("%.6f", mortgageApproval.monthlyInterestRate)
    			+ ", " + String.format("%.6f", me.behaviour.getLongTermHPAExpectation())
    			+ ", " + String.format("%.4f", Model.housingMarketStats.getHPI())
    			+ ", " + newHouseQuality
    			+ ", " + probabilityPlaceBidOnHousingMarket
    			+ ", " + placeBidOnHousingMarket
    			+ ", " );
    }
    
    public void recordKeepOneProperty(Household me) {
    	Model.agentDecisionRecorder.decideSellInvestmentProperty.println(Model.getTime()
    			+ ", " + me.id
    			+ ", " + "true"
    			+ ", " + String.format("%.2f", me.getBankBalance())
				+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
				+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
				+ ", " + String.format("%.2f", me.getEquityPosition())
				+ ", " + ", " + ", " + ", " + ", " + ", " 
				+ ", " + ", " + ", " + ", " + ", " + ", "
				+ "false, " 
				+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
				);
    }
    
    public void recordDivestmentDecision(Household me, House h,
    		double currentMarketPrice, double equity, double leverage, double currentRentalYield,
    		double mortgageRate, double expectedEquityYield, double pKeep, boolean sell) {
    	// in order to catch the initial sale price, if the household actually sells, it will use print 
    	// and not println, so that the household.putHouseForSale method can record the initial sale price
    	// this is necessary, as a prng is used.
    	if(sell) {
    		Model.agentDecisionRecorder.decideSellInvestmentProperty.print(Model.getTime()
    				+ ", " + me.id
    				+ ", " + "false"
    				+ ", " + String.format("%.2f", me.getBankBalance())
    				+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
    				+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
    				+ ", " + String.format("%.2f", me.getEquityPosition())
    				+ ", " + String.format("%.2f", me.behaviour.getBTLCapGainCoefficient())
    				+ ", " + h.getQuality()
    				+ ", " + String.format("%.2f", currentMarketPrice)
    				+ ", " + String.format("%.2f", equity)
    				+ ", " + String.format("%.2f", leverage)
    				+ ", " + String.format("%.4f", currentRentalYield)
    				+ ", " + String.format("%.4f", mortgageRate)
    				+ ", " + String.format("%.4f", Model.rentalMarketStats.getLongTermExpAvFlowYield())
    				+ ", " + String.format("%.4f", me.behaviour.getLongTermHPAExpectation())
    				+ ", " + String.format("%.4f", expectedEquityYield)
    				+ ", " + String.format("%.2f", pKeep)
    				+ ", " + sell
    				+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
    				+ ", ");
    	}
    	// if house won't be sold, and no initial sale price will be recorded later, use println
    	else {
    		Model.agentDecisionRecorder.decideSellInvestmentProperty.println(Model.getTime()
    				+ ", " + me.id
    				+ ", " + "false"
    				+ ", " + String.format("%.2f", me.getBankBalance())
    				+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
    				+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
    				+ ", " + String.format("%.2f", me.getEquityPosition())
    				+ ", " + String.format("%.2f", me.behaviour.getBTLCapGainCoefficient())
    				+ ", " + h.getQuality()
    				+ ", " + String.format("%.2f", currentMarketPrice)
    				+ ", " + String.format("%.2f", equity)
    				+ ", " + String.format("%.2f", leverage)
    				+ ", " + String.format("%.4f", currentRentalYield)
    				+ ", " + String.format("%.4f", mortgageRate)
    				+ ", " + String.format("%.4f", Model.rentalMarketStats.getLongTermExpAvFlowYield())
    				+ ", " + String.format("%.4f", me.behaviour.getLongTermHPAExpectation())
    				+ ", " + String.format("%.4f", expectedEquityYield)
    				+ ", " + String.format("%.2f", pKeep)
    				+ ", " + sell
    				+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
    				+ ", ");
    	}
    }
    
    public void recordMaxMortgageBTL(Household h, double ltv_max_price, double icr_max_price) {
    	Model.agentDecisionRecorder.decideBuyInvestmentProperty.print(Model.getTime() 
				+ ", " + h.id
				+ ", " + String.format("%.2f", ltv_max_price)
				+ ", " + String.format("%.2f", icr_max_price)
				+ ", ");
    }
    
    public void recordLoanRequestBTL(double ltv_principal, double icr_principal) {
    	Model.agentDecisionRecorder.decideBuyInvestmentProperty.print(String.format("%.2f", ltv_principal)
				+ ", " + String.format("%.2f", icr_principal)
				+ ", "
				);
    }
    
    public void recordTooHighMonthlyPaymentsBTL(Household me) {
    	Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
				+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
				+ ", " + String.format("%.2f", me.getBankBalance())
				+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
				+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
				+ ", " + String.format("%.2f", me.getEquityPosition())
				+ ", "+ ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " 
				+ ", " + "false" 
				+ ", " + "monthly mortgage payments already too high"
				+ ", " + String.format("%.2f", me.getBankBalance()) 
    			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
    					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())) 
    			+ ", " 
				+ ", " + String.format("%.2f", me.getPrincipalPaidBack())
				+ ", " + String.format("%.2f", me.getInterestPaidBack())
				+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
				+ ", "); 
    }
    
    public void recordNoInvestmentPropertyYet(Household me) {
    	Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
    			+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
    			+ ", " + String.format("%.2f", me.getBankBalance())
    			+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
    			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
    			+ ", " + String.format("%.2f", me.getEquityPosition())
    			+ ", " + String.format("%.2f", Model.bank.getMaxMortgage(me, false, false))
    			+ ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " 
    			+ ", " + "true"
    			+ ", " + "0 investment properties owned"
    			+ ", " + String.format("%.2f", me.getBankBalance()) 
    			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
    					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())) 
    			+ ", " 
				+ ", " + String.format("%.2f", me.getPrincipalPaidBack())
				+ ", " + String.format("%.2f", me.getInterestPaidBack())
    			+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
    			+ ", "); 
    }
    
    public void recordBankBalanceTooLow(Household me, boolean flexibleCredit) {
    	if(flexibleCredit) {
    		Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
        			+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
        			+ ", " + String.format("%.2f", me.getBankBalance())
        			+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
        			+ ", " + String.format("%.2f", me.getEquityPosition())
        			+ ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " 
        			+ "false" + ", " + "bb too far apart from desired bb"
        			+ ", " + String.format("%.2f", me.getBankBalance()) 
        			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
        					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())) 
        			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
        					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())
        					*(config.BTL_CHOICE_MIN_BANK_BALANCE-Model.housingMarketStats.getLongTermHPA()))
    				+ ", " + String.format("%.2f", me.getPrincipalPaidBack())
    				+ ", " + String.format("%.2f", me.getInterestPaidBack())
        			+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
        			+ ", "); 
    	} else {
    		Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
        			+ ", " + me.id + ", " + ", " + ", " + ", " + ", " + ", " 
        			+ ", " + String.format("%.2f", me.getBankBalance())
        			+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
        			+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
        			+ ", " + String.format("%.2f", me.getEquityPosition())
        			+ ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " + ", " 
        			+ "false" + ", " + "bb too far apart from desired bb"
        			+ ", " + String.format("%.2f", me.getBankBalance()) 
        			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
        					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())) 
        			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
        					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())
        					*config.BTL_CHOICE_MIN_BANK_BALANCE)
    				+ ", " + String.format("%.2f", me.getPrincipalPaidBack())
    				+ ", " + String.format("%.2f", me.getInterestPaidBack())
        			+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
        			+ ", "); 
    		
    	}
    }
    
    public void recordHousesTooExpensive(Household me) {
    	Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(", " + ", " + Model.getTime() 
		    	+ ", " + me.id 
		    	+ ", " + String.format("%.2f", me.getBankBalance())
		    	+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
		    	+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
		    	+ ", " + String.format("%.2f", me.getEquityPosition())
		    	+ ", " + ", "+ ", " + ", " + ", " + ", " + ", " + ", " + ", " 
		    	+ ", " + "false"
		    	+ ", " + "max price too small for houses on market"
		    	+ ", " + String.format("%.2f", me.getBankBalance()) 
    			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
    					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())) 
    			+ ", " 
				+ ", " + String.format("%.2f", me.getPrincipalPaidBack())
				+ ", " + String.format("%.2f", me.getInterestPaidBack())
    			+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
    			+ ", "); 
    }
    
    public void recordInvestmentDecision(
    		Household me, double equity, 
    		double leverage, double rentalYield,
    		double mortgageRate, double expectedEquityYield,
    		double pBuy, boolean bidOnTheHousingMarket) {
		Model.agentDecisionRecorder.decideBuyInvestmentProperty.println(Model.getTime() 
				+ ", " + me.id
				+ ", " + String.format("%.2f", me.getBankBalance())
				+ ", " + String.format("%.2f", me.returnMonthlyDisposableIncome())
				+ ", " + String.format("%.2f", me.getMonthlyGrossEmploymentIncome())
				+ ", " + String.format("%.2f", me.getEquityPosition())
				+ ", " + String.format("%.2f", Model.bank.getMaxMortgage(me, false, false))
				+ ", " + String.format("%.2f", equity)
				+ ", " + String.format("%.2f", leverage)
				+ ", " + String.format("%.6f", rentalYield)
				+ ", " + String.format("%.6f", mortgageRate)
				+ ", " + String.format("%.6f", expectedEquityYield)
				+ ", " + me.behaviour.getBTLCapGainCoefficient()
				+ ", " + String.format("%.6f", me.behaviour.getLongTermHPAExpectation())
				+ ", " + String.format("%.4f", pBuy)
				+ ", " + bidOnTheHousingMarket
				+ ", " + "calculated"
				+ ", " + String.format("%.2f", me.getBankBalance()) 
    			+ ", " + String.format("%.2f", data.Wealth.getDesiredBankBalance
    					(me.getAnnualGrossTotalIncome(), me.behaviour.getPropensityToSave())) 
    			+ ", " 
				+ ", " + String.format("%.2f", me.getPrincipalPaidBack())
				+ ", " + String.format("%.2f", me.getInterestPaidBack())
    			+ ", " + String.format("%.2f", Model.housingMarketStats.getHPI())
				+ ", ");     	
    }
        
    public void finish() {
        rentOrBuy.close();
        decideBuyInvestmentProperty.close();
        decideSellInvestmentProperty.close();
    }
}
