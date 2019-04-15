package collectors;

import housing.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;

import static java.lang.Double.NaN;

/**************************************************************************************************
 * Class to write agent level output to files
 *
 * @author Ruben Tarne
 * 
 * TODO: I have to consider naming the rows and columns (agent_id?), and in General, many variables
 * are still missing.
 * - income
 * - worth of the houses the hh own
 * - consumption
 * - credit on their balance sheet
 * - decision taken to sell home?
 * - decision taken to sell investment properties?
 * 
 * TODO: formula for the length of the array at initialisation is not satisfying, yet.
 * Maybe I can include a method in order to remove the columns at the end with only NaN values.
 *
 *************************************************************************************************/

public class AgentDataRecorder{
	
    //------------------//
    //----- Fields -----//
    //------------------//	

	private String 						outputFolderCopy;
	private Config                     	config = Model.config; // Passes the Model's configuration parameters object to a private field
	private int							oldestID;				
	
	PrintWriter							bankBalance;
	PrintWriter							age;
	PrintWriter 						totalWealth;
	PrintWriter							housingNetWealth;
	PrintWriter							consumption;
	PrintWriter							annualGrossTotalIncome;
	PrintWriter							monthlyDisposableIncome;
	PrintWriter							desiredBankBalance;
	PrintWriter							BTL;
	PrintWriter							SH;
	PrintWriter							annualGrossEmploymentIncome;
	PrintWriter							debt;
	PrintWriter							consumptionWealth;
	


	
	
	
    //------------------------//
    //----- Constructors -----//
    //------------------------//
	
	public AgentDataRecorder(String outputFolder) {
        outputFolderCopy = outputFolder;
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//
	
    
    // start the recorder and a new file and write into the new file
	public void openNewFiles(int run){
		if(config.recordAgentData) {
			try{
				//TODO insert all the new outputs
				bankBalance = new PrintWriter(outputFolderCopy + "agentData-bankBalance" + run + ".csv", "UTF-8");
				age = new PrintWriter(outputFolderCopy + "agentData-age" + run + ".csv", "UTF-8");
				totalWealth = new PrintWriter(outputFolderCopy + "agentData-totalNetWealth" + run + ".csv", "UTF-8");
				housingNetWealth = new PrintWriter(outputFolderCopy + "agentData-housingNetWealth" + run + ".csv", "UTF-8");
				consumption = new PrintWriter(outputFolderCopy + "agentData-consumption" + run + ".csv", "UTF-8");
				annualGrossTotalIncome = new PrintWriter(outputFolderCopy + "agentData-annualGrossTotalIncome" + run + ".csv", "UTF-8");
				monthlyDisposableIncome = new PrintWriter(outputFolderCopy + "agentData-monthlyDisposableIncome" + run + ".csv", "UTF-8");
				desiredBankBalance = new PrintWriter(outputFolderCopy + "agentData-desiredBankBalance" + run + ".csv", "UTF-8");
				BTL = new PrintWriter(outputFolderCopy + "agentData-BTL" + run + ".csv", "UTF-8");
				SH = new PrintWriter(outputFolderCopy + "agentData-SH" + run + ".csv", "UTF-8");
				annualGrossEmploymentIncome = new PrintWriter(outputFolderCopy + "agentData-annualGrossEmploymentIncome" + run + ".csv", "UTF-8");
				debt = new PrintWriter(outputFolderCopy + "agentData-debt" + run + ".csv", "UTF-8");
				consumptionWealth = new PrintWriter(outputFolderCopy + "agentData-consumptionWealth" + run + ".csv", "UTF-8");

			}
			catch (FileNotFoundException | UnsupportedEncodingException e){
				e.printStackTrace();
			}
		}
	}
    
	public void recordAgentData() {
		if (Model.getTime() >= config.TIME_TO_START_RECORDING) {

			// set the array size big enough to hold all future HHs (about 100000 for 10,000 HH and 6,000 periods);
			// 0.0015 is a fudge parameter to adjust the influence of more periods and target population.
			// Generally, for small intervalls, the array becomes too small
			//TODO This equation does not really provide a good initialisation size for changing varibles.
			int maxLivedHHs =  35000;//(int)((config.N_STEPS - config.TIME_TO_START_RECORDING)*config.TARGET_POPULATION*0.0035);

			// build the arrays for the variables to be extracted
			double[] arrayBankBalance = new double[maxLivedHHs];
			double[] arrayAge = new double[maxLivedHHs];
			double[] arrayTotalWealth = new double[maxLivedHHs];
			double[] arrayHousingNetWealth = new double[maxLivedHHs];
			double[] arrayConsumption = new double[maxLivedHHs];
			double[] arrayAnnualGrossTotalIncome = new double[maxLivedHHs];
			double[] arrayMonthlyDisposableIncome = new double[maxLivedHHs];
			double[] arrayDesiredBankBalance = new double[maxLivedHHs];
			double[] arrayBTL = new double[maxLivedHHs];
			double[] arraySH = new double[maxLivedHHs];
			double[] arrayAnnualGrossEmploymentIncome = new double[maxLivedHHs];
			double[] arrayDebt = new double[maxLivedHHs];
			double[] arrayConsumptionWealth = new double[maxLivedHHs];

			// fill arrays with NULL
			Arrays.fill(arrayBankBalance, NaN);
			Arrays.fill(arrayAge, NaN);
			Arrays.fill(arrayTotalWealth, NaN);
			Arrays.fill(arrayHousingNetWealth, NaN);
			Arrays.fill(arrayConsumption, NaN);
			Arrays.fill(arrayAnnualGrossTotalIncome, NaN);
			Arrays.fill(arrayMonthlyDisposableIncome, NaN);
			Arrays.fill(arrayDesiredBankBalance, NaN);
			Arrays.fill(arrayBTL, NaN);
			Arrays.fill(arraySH, NaN);
			Arrays.fill(arrayAnnualGrossEmploymentIncome, NaN);
			Arrays.fill(arrayDebt, NaN);
			Arrays.fill(arrayConsumptionWealth, NaN);

			// fill a 2-dimensional Array with the household data
			double[][] dataArray = dataToArray(arrayBankBalance, 
					arrayAge, 
					arrayTotalWealth, 
					arrayHousingNetWealth, 
					arrayConsumption,
					arrayAnnualGrossTotalIncome,
					arrayMonthlyDisposableIncome,
					arrayDesiredBankBalance,
					arrayBTL,
					arraySH,
					arrayAnnualGrossEmploymentIncome,
					arrayDebt,
					arrayConsumptionWealth
					);

			// convert the Array to a string and clean from brackets so it can easily 
			// be written into a csv-file
			String stringBankBalance = cleanString(dataArray[0]);
			String stringAge = cleanString(dataArray[1]);
			String stringTotalWealth = cleanString(dataArray[2]);
			String stringHousingNetWealth = cleanString(dataArray[3]);
			String stringConsumption = cleanString(dataArray[4]);
			String stringAnnualGrossTotalIncome = cleanString(dataArray[5]);
			String stringMonthlyDisposableIncome = cleanString(dataArray[6]);
			String stringDesiredBankBalance = cleanString(dataArray[7]);
			String stringBTL = cleanString(dataArray[8]);
			String stringSH = cleanString(dataArray[9]);
			String stringAnnualGrossEmploymentIncome = cleanString(dataArray[10]);
			String stringDebt = cleanString(dataArray[11]);
			String stringConsumptionWealth = cleanString(dataArray[12]);

			//write the clean string into the csv file
			bankBalance.println(Model.getTime() + ", " + stringBankBalance);
			age.println(Model.getTime() + ", " + stringAge);
			totalWealth.println(Model.getTime() + ", " + stringTotalWealth);
			housingNetWealth.println(Model.getTime() + ", " + stringHousingNetWealth);
			consumption.println(Model.getTime() + ", " + stringConsumption);
			annualGrossTotalIncome.println(Model.getTime() + ", " + stringAnnualGrossTotalIncome);
			monthlyDisposableIncome.println(Model.getTime() + ", " + stringMonthlyDisposableIncome);
			desiredBankBalance.println(Model.getTime() + ", " + stringDesiredBankBalance);
			BTL.println(Model.getTime() + ", " + stringBTL);
			SH.println(Model.getTime() + ", " + stringSH);
			annualGrossEmploymentIncome.println(Model.getTime() + ", " + stringAnnualGrossEmploymentIncome);
			debt.println(Model.getTime() + ", " + stringDebt);
			consumptionWealth.println(Model.getTime() + ", " + stringConsumptionWealth);
		}         	
	}
    
    // this is where the data from the simulation gets actually extracted
        private double[][] dataToArray(double[] input1, double[] input2, 
        		double[] input3, double[] input4, 
        		double[] input5,
        		double[] input6
        		, double[] input7, double[] input8,
        		double[] input9, double[] input10,
        		double[] input11, double[] input12, double[] input13
        		) {

        	// load the data into the 2-dimensional Array. 
        	double[][] dataArray = new double[][] {input1, input2, input3, input4, input5, input6
        		, input7, input8, input9, input10, input11, input12, input13
        	}; 

        	// put households into an array (from arrayList to array) at their "id-place" so that IDs not used 
        	// appear as "NaN" 
        	//Alternatively, when starting to record from later on, id is not used as positioner.
        	Iterator<Household> iterator = Model.households.iterator();
        	// find the agent with the lowest id_value and extract this value 
        	if (Model.t == config.TIME_TO_START_RECORDING) {
        		Household oldestHH = iterator.next();
        		oldestID = oldestHH.id;
        		//System.out.println("oldestID is: " + oldestID);
        	}

        	// use the id value so that the agent with the lowest id is recorded at the first position in the array
        	// TODO: this code leaves out the first value of the oldest HH and starts with the second id. 
        	// this does not seem to be a problem (one value is missing out of some million), but maybe I can fix that. 
        	while(iterator.hasNext()){
        		Household h = iterator.next();

        		//System.out.println("; and first HH's ID is: " + h.id);
        		double bankBalanceHH = dataArray[0][h.id-oldestID] = h.getBankBalance();
        		dataArray[1][h.id-oldestID] = h.getAge();
        		double equityPositionHH = dataArray[2][h.id-oldestID] = h.getEquityPosition();
        		dataArray[3][h.id-oldestID] = equityPositionHH - bankBalanceHH;
        		dataArray[4][h.id-oldestID] = (h.getConsumption()+config.GOVERNMENT_MONTHLY_INCOME_SUPPORT*config.ESSENTIAL_CONSUMPTION_FRACTION);
        		dataArray[5][h.id-oldestID] = h.getAnnualGrossTotalIncome();
        		dataArray[6][h.id-oldestID] = h.returnMonthlyDisposableIncome(); 
        		dataArray[7][h.id-oldestID] = data.Wealth.getDesiredBankBalance(h.getAnnualGrossTotalIncome(), h.behaviour.getPropensityToSave()); //dataArray[3][h.id-oldestID] = h.behaviour.getDesiredBankBalance(h.getAnnualGrossTotalIncome());
        		if(h.behaviour.isPropertyInvestor()) {
        			dataArray[8][h.id-oldestID] = 1;
        		}else {dataArray[8][h.id-oldestID] = 0;}
        		if(h.isInSocialHousing()) {
        			dataArray[9][h.id-oldestID] = 1;
        		}else { dataArray[9][h.id-oldestID] = 0;}
        		dataArray[10][h.id-oldestID] = h.getAnnualGrossEmploymentIncome();
        		dataArray[11][h.id-oldestID] = h.getTotalDebt();
        		dataArray[12][h.id-oldestID] = h.getFinancialWealthConsumption()+h.getHousingWealthConsumption()+h.getDebtConsumption();
        	}            
        	return dataArray;
        }
    
    
    String cleanString(double[] whatToMeasure) {
        //clean the output so it is compatible with csv format
        String whatToMeasureString;
        whatToMeasureString = Arrays.toString(whatToMeasure).replace("[" , "").replace("]" , "");
        return whatToMeasureString;
    }
    

    public void finish() {
        bankBalance.close();
        age.close();
        totalWealth.close();
        housingNetWealth.close();
        consumption.close();
        annualGrossTotalIncome.close();
        monthlyDisposableIncome.close();
        desiredBankBalance.close();
        BTL.close();
        SH.close();
        annualGrossEmploymentIncome.close();
        debt.close();
        consumptionWealth.close();
	}
}
