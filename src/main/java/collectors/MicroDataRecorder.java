package collectors;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MicroDataRecorder {

    //------------------//
    //----- Fields -----//
    //------------------//

    private String          outputFolder;

    private PrintWriter     outfileEmploymentIncome;
    private PrintWriter     outfileRentalIncome;
    private PrintWriter     outfileBankBalance;
    private PrintWriter     outfileHousingWealth;
    private PrintWriter     outfileNHousesOwned;
    private PrintWriter     outfileAge;
    private PrintWriter     outfileSavingRate;

    private int             timeToStartMicroPrinting = 996;
    private int             freqOfMicroPrinting = 12;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public MicroDataRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunSingleVariableFiles(int nRun, boolean recordEmploymentIncome, boolean recordRentalIncome,
                                                 boolean recordBankBalance, boolean recordHousingWealth,
                                                 boolean recordNHousesOwned, boolean recordAge,
                                                 boolean recordSavingRate) {
        if (recordEmploymentIncome) {
            try {
                outfileEmploymentIncome = new PrintWriter(outputFolder + "MonthlyGrossEmploymentIncome-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordRentalIncome) {
            try {
                outfileRentalIncome = new PrintWriter(outputFolder + "MonthlyGrossRentalIncome-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordBankBalance) {
            try {
                outfileBankBalance = new PrintWriter(outputFolder + "BankBalance-run" + nRun
                        + ".csv", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (recordHousingWealth) {
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
        if (recordAge) {
            try {
                outfileAge = new PrintWriter(outputFolder + "Age-run" + nRun
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

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordEmploymentIncome, boolean recordRentalIncome,
                                               boolean recordBankBalance, boolean recordHousingWealth,
                                               boolean recordNHousesOwned, boolean recordAge,
                                               boolean recordSavingRate) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            if (recordEmploymentIncome) {
                if (time != timeToStartMicroPrinting) {
                    outfileEmploymentIncome.println("");
                }
                outfileEmploymentIncome.print(time);
            }
            if (recordRentalIncome) {
                if (time != timeToStartMicroPrinting) {
                    outfileRentalIncome.println("");
                }
                outfileRentalIncome.print(time);
            }
            if (recordBankBalance) {
                if (time != timeToStartMicroPrinting) {
                    outfileBankBalance.println("");
                }
                outfileBankBalance.print(time);
            }
            if (recordHousingWealth) {
                if (time != timeToStartMicroPrinting) {
                    outfileHousingWealth.println("");
                }
                outfileHousingWealth.print(time);
            }
            if (recordNHousesOwned) {
                if (time != timeToStartMicroPrinting) {
                    outfileNHousesOwned.println("");
                }
                outfileNHousesOwned.print(time);
            }
            if (recordAge) {
                if (time != timeToStartMicroPrinting) {
                    outfileAge.println("");
                }
                outfileAge.print(time);
            }
            if (recordSavingRate) {
                if (time != timeToStartMicroPrinting) {
                    outfileSavingRate.println("");
                }
                outfileSavingRate.print(time);
            }
        }
    }

    void recordEmploymentIncome(int time, double monthlyGrossEmploymentIncome) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileEmploymentIncome.format(", %.2f", monthlyGrossEmploymentIncome);
        }
    }

    void recordRentalIncome(int time, double monthlyGrossRentalIncome) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileRentalIncome.format(", %.2f", monthlyGrossRentalIncome);
        }
    }

    void recordBankBalance(int time, double bankBalance) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileBankBalance.format(", %.2f", bankBalance);
        }
    }

    void recordHousingWealth(int time, double housingWealth) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileHousingWealth.format(", %2f", housingWealth);
        }
    }

    void recordNHousesOwned(int time, int nHousesOwned) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileNHousesOwned.format(", %d", nHousesOwned);
        }
    }

    void recordAge(int time, double age) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileAge.format(", %.2f", age);
        }
    }

    void recordSavingRate(int time, double savingRate) {
        if (time % freqOfMicroPrinting == 0 && time >= timeToStartMicroPrinting) {
            outfileSavingRate.format(", %.4f", savingRate);
        }
    }

    public void finishRun(boolean recordEmploymentIncome, boolean recordRentalIncome, boolean recordBankBalance,
                          boolean recordHousingWealth, boolean recordNHousesOwned, boolean recordAge,
                          boolean recordSavingRate) {
        if (recordEmploymentIncome) {
            outfileEmploymentIncome.close();
        }
        if (recordRentalIncome) {
            outfileRentalIncome.close();
        }
        if (recordBankBalance) {
            outfileBankBalance.close();
        }
        if (recordHousingWealth) {
            outfileHousingWealth.close();
        }
        if (recordNHousesOwned) {
            outfileNHousesOwned.close();
        }
        if (recordAge) {
            outfileAge.close();
        }
        if (recordSavingRate) {
            outfileSavingRate.close();
        }
    }
}
