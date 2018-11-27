package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MicroDataRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String outputFolder;

    private PrintWriter outfileBankBalance;
    private PrintWriter outfileNInvestmentProperties;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public MicroDataRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunSingleVariableFiles(int nRun, boolean recordBankBalance,
                                                 boolean recordNInvestmentProperties) {
        if (recordBankBalance) {
            try {
                outfileBankBalance = new PrintWriter(outputFolder + "BankBalance-run" + nRun + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordNInvestmentProperties) {
            try {
                outfileNInvestmentProperties = new PrintWriter(outputFolder + "NInvestmentProperties-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordBankBalance,
                                               boolean recordNInvestmentProperties) {
        if (time % 100 == 0) {
            if (recordBankBalance) {
                if (time != 0) {
                    outfileBankBalance.println("");
                }
                outfileBankBalance.print(time);
            }
            if (recordNInvestmentProperties) {
                if (time != 0) {
                    outfileNInvestmentProperties.println("");
                }
                outfileNInvestmentProperties.print(time);
            }
        }
    }
	
	void recordBankBalance(int time, double bankBalance) {
        if (time % 100 == 0) {
            outfileBankBalance.print(", " + bankBalance);
        }
	}

    void recordNInvestmentProperties(int time, int nInvestmentProperties) {
        if (time % 100 == 0 && nInvestmentProperties > 0) {
            outfileNInvestmentProperties.print(", " + nInvestmentProperties);
        }
    }

	public void finishRun(boolean recordBankBalance, boolean recordNInvestmentProperties) {
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
        if (recordNInvestmentProperties) {
            outfileNInvestmentProperties.close();
        }
	}
}
