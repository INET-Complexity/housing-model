package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import housing.Config;
import housing.Model;

public class MicroDataRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String 		outputFolder;

    private PrintWriter outfileBankBalance;
    private PrintWriter outfileHousingWealth;
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
                outfileBankBalance = new PrintWriter(outputFolder + "BankBalance-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordInitTotalWealth) {
            try {
                outfileHousingWealth = new PrintWriter(outputFolder + "HousingWealth-run" + nRun
                        + ".csv", "UTF-8");
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

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordBankBalance, boolean recordHousingWealth,
                                               boolean recordNHousesOwned, boolean recordSavingRate) {
        if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
            if (recordBankBalance) {
                if (time != 0) {
                    outfileBankBalance.println("");
                }
                outfileBankBalance.print(time);
            }
            if (recordHousingWealth) {
                if (time != 0) {
                    outfileHousingWealth.println("");
                }
                outfileHousingWealth.print(time);
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
        if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
            outfileBankBalance.print(", " + bankBalance);
        }
	}

    void recordHousingWealth(int time, double housingWealth) {
        if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
            outfileHousingWealth.print(", " + housingWealth);
        }
    }

    void recordNHousesOwned(int time, int nHousesOwned) {
        if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
            outfileNHousesOwned.print(", " + nHousesOwned);
        }
    }

    void recordSavingRate(int time, double savingRate) {
        if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
            outfileSavingRate.print(", " + savingRate);
        }
    }

	public void finishRun(boolean recordBankBalance, boolean recordHousingWealth, boolean recordNHousesOwned,
                          boolean recordSavingRate) {
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
        if (recordHousingWealth) {
            outfileHousingWealth.close();
        }
        if (recordNHousesOwned) {
            outfileNHousesOwned.close();
        }
        if (recordSavingRate) {
            outfileSavingRate.close();
        }
	}
}
