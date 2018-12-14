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
    private PrintWriter outfileInitTotalWealth;
    private PrintWriter outfileNHousesOwned;
    private PrintWriter outfileSavingRate;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public MicroDataRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunSingleVariableFiles(int nRun, boolean recordBankBalance, boolean recordInitTotalWealth,
                                                 boolean recordNHousesOwned, boolean recordSavingRate) {
        if (recordBankBalance) {
            try {
                outfileBankBalance = new PrintWriter(outputFolder + "BankBalance-run" + nRun + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordInitTotalWealth) {
            try {
                outfileInitTotalWealth = new PrintWriter(outputFolder + "InitialTotalWealth-run" + nRun + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordNHousesOwned) {
            try {
                outfileNHousesOwned = new PrintWriter(outputFolder + "NHousesOwned-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordSavingRate) {
            try {
                outfileSavingRate = new PrintWriter(outputFolder + "SavingRate-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordBankBalance, boolean recordInitTotalWealth,
                                               boolean recordNHousesOwned, boolean recordSavingRate) {
        if (time % 100 == 0) {
            if (recordBankBalance) {
                if (time != 0) {
                    outfileBankBalance.println("");
                }
                outfileBankBalance.print(time);
            }
            if (recordInitTotalWealth) {
                if (time != 0) {
                    outfileInitTotalWealth.println("");
                }
                outfileInitTotalWealth.print(time);
            }
            if (recordNHousesOwned) {
                if (time != 0) {
                    outfileNHousesOwned.println("");
                }
                outfileNHousesOwned.print(time);
            }
            if (recordSavingRate) {
                if (time != 0) {
                    outfileSavingRate.println("");
                }
                outfileSavingRate.print(time);
            }
        }
    }
	
	void recordBankBalance(int time, double bankBalance) {
        if (time % 100 == 0) {
            outfileBankBalance.print(", " + bankBalance);
        }
	}

    void recordInitTotalWealth(int time, double initTotalWealth) {
        if (time % 100 == 0) {
            outfileInitTotalWealth.print(", " + initTotalWealth);
        }
    }

    void recordNHousesOwned(int time, int nHousesOwned) {
        if (time % 100 == 0) {
            outfileNHousesOwned.print(", " + nHousesOwned);
        }
    }

    void recordSavingRate(int time, double savingRate) {
        if (time % 100 == 0) {
            outfileSavingRate.print(", " + savingRate);
        }
    }

	public void finishRun(boolean recordBankBalance, boolean recordInitTotalWealth, boolean recordNHousesOwned,
                          boolean recordSavingRate) {
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
        if (recordInitTotalWealth) {
            outfileInitTotalWealth.close();
        }
        if (recordNHousesOwned) {
            outfileNHousesOwned.close();
        }
        if (recordSavingRate) {
            outfileSavingRate.close();
        }
	}
}
