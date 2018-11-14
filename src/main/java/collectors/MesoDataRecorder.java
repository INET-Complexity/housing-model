package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MesoDataRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String outputFolder;

    private PrintWriter outfileBankBalance;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public MesoDataRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunSingleVariableFiles(int nRun, boolean recordBankBalance) {
        if (recordBankBalance) {
            try {
                outfileBankBalance = new PrintWriter(outputFolder + "BankBalance-run" + nRun + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordBankBalance) {
        if (time % 100 == 0) {
            if (recordBankBalance) {
                if (time != 0) {
                    outfileBankBalance.println("");
                }
                outfileBankBalance.print(time);
            }
        }
    }
	
	void recordBankBalance(int time, double bankBalance) {
        if (time % 100 == 0) {
            outfileBankBalance.print(", " + bankBalance);
        }
	}

	public void finishRun(boolean recordBankBalance) {
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
	}
}
