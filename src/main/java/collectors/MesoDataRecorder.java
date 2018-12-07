package collectors;

import housing.*;

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

    void timeStampBankBalance(int time) {
        if (time != 0) { outfileBankBalance.println(""); }
        outfileBankBalance.print(time);
    }
	
	void recordBankBalance(double bankBalance) {
		outfileBankBalance.print(", " + bankBalance);
	}

	public void finishRun(boolean recordBankBalance) {
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
	}
}
