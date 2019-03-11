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
    private PrintWriter outfileMonthlyGrossTotalIncome;
    private PrintWriter outfileMonthlyGrossEmploymentIncome;
    private PrintWriter outfileMonthlyGrossRentalIncome;
    private PrintWriter outfileDebt;
    private PrintWriter outfileConsumption;
    private PrintWriter outfileBTL;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public MicroDataRecorder(String outputFolder) { this.outputFolder = outputFolder; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void openSingleRunSingleVariableFiles(int nRun, boolean recordBankBalance, boolean recordInitTotalWealth,
                                                 boolean recordNHousesOwned, boolean recordSavingRate,
                                                 boolean recordMonthlyGrossTotalIncome, boolean recordMonthlyGrossEmploymentIncome,
                                                 boolean recordMonthlyGrossRentalIncome, boolean recordDebt,
                                                 boolean recordConsumption, boolean recordBTL) {
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
        if(recordMonthlyGrossTotalIncome) {
        	try {
        		outfileMonthlyGrossTotalIncome = new PrintWriter(outputFolder + 
        				"MonthlyGrossTotalIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordMonthlyGrossEmploymentIncome) {
        	try {
        		outfileMonthlyGrossEmploymentIncome = new PrintWriter(outputFolder + 
        				"MonthlyGrossEmploymentIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordMonthlyGrossRentalIncome) {
        	try {
        		outfileMonthlyGrossRentalIncome = new PrintWriter(outputFolder + 
        				"MonthlyGrossRentalIncome-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordDebt) {
        	try {
        		outfileDebt = new PrintWriter(outputFolder + 
        				"Debt-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordConsumption) {
        	try {
        		outfileConsumption = new PrintWriter(outputFolder + 
        				"Consumption-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
        if(recordBTL) {
        	try {
        		outfileBTL = new PrintWriter(outputFolder + 
        				"isBTL-run" + nRun + ".csv", "UTF-8");
        	} catch(FileNotFoundException | UnsupportedEncodingException e) {
        		e.printStackTrace();
        	}
        }
    }

    void timeStampSingleRunSingleVariableFiles(int time, boolean recordBankBalance, boolean recordHousingWealth,
                                               boolean recordNHousesOwned, boolean recordSavingRate, 
                                               boolean recordMonthlyGrossTotalIncome, boolean recordMonthlyGrossEmploymentIncome,
                                               boolean recordMonthlyGrossRentalIncome, boolean recordDebt,
                                               boolean recordConsumption, boolean recordBTL) {
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
            if (recordMonthlyGrossTotalIncome) {
            	if(time!=0) {
            		outfileMonthlyGrossTotalIncome.println("");
            	}
            	outfileMonthlyGrossTotalIncome.print(time);
            }
            if (recordMonthlyGrossEmploymentIncome) {
            	if(time!=0) {
            		outfileMonthlyGrossEmploymentIncome.println("");
            	}
            	outfileMonthlyGrossEmploymentIncome.print(time);
            }
            if (recordMonthlyGrossRentalIncome) {
            	if(time!=0) {
            		outfileMonthlyGrossRentalIncome.println("");
            	}
            	outfileMonthlyGrossRentalIncome.print(time);
            }
            if (recordDebt) {
            	if (time != 0) {
            		outfileDebt.println("");
            	}
            	outfileDebt.print(time);
            }
            if (recordConsumption) {
            	if (time != 0) {
            		outfileConsumption.println("");
            	}
            	outfileConsumption.print(time);
            }
            if (recordBTL) {
            	if (time != 0) {
            		outfileBTL.println("");
            	}
            	outfileBTL.print(time);
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
    
    void recordMonthlyGrossTotalIncome(int time, double monthlyGrossTotalIncome) {
    	if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
    		outfileMonthlyGrossTotalIncome.print(", " + monthlyGrossTotalIncome);
    	}
    }
    
    void recordMonthlyGrossEmploymentIncome(int time, double monthlyGrossEmploymentIncome) {
    	if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
    		outfileMonthlyGrossEmploymentIncome.print(", " + monthlyGrossEmploymentIncome);
    	}
    }
    
    void recordMonthlyGrossRentalIncome(int time, double monthlyGrossRentalIncome) {
    	if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
    		outfileMonthlyGrossRentalIncome.print(", " + monthlyGrossRentalIncome);
    	}
    }
    
    void recordDebt(int time, double debt) {
    	if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
    		outfileDebt.print(", " + debt);
    	}
    }
    
    void recordConsumption(int time, double consumption) {
    	if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
    		outfileConsumption.print(", " + consumption);
    	}
    }
    
    void recordBTL(int time, boolean isBTL) {
    	if (time % Model.config.microDataRecordIntervall == 0 && time >= Model.config.TIME_TO_START_RECORDING) {
    		if(isBTL)outfileBTL.print(", " + 1);
    		else outfileBTL.print(", " + 0);
    	}
    }

	public void finishRun(boolean recordBankBalance, boolean recordHousingWealth, boolean recordNHousesOwned,
                          boolean recordSavingRate, boolean recordMonthlyGrossTotalIncome, boolean recordMonthlyGrossEmploymentIncome,
                          boolean recordMonthlyGrossRentalIncome, boolean recordDebt, boolean recordConsumption,
                          boolean recordBTL) {
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
        if (recordMonthlyGrossTotalIncome) {
        	outfileMonthlyGrossTotalIncome.close();
        }
        if (recordMonthlyGrossEmploymentIncome) {
        	outfileMonthlyGrossEmploymentIncome.close();
        }
        if (recordMonthlyGrossRentalIncome) {
        	outfileMonthlyGrossRentalIncome.close();
        }
        if (recordDebt) {
        	outfileDebt.close();
        }
        if (recordConsumption) {
        	outfileConsumption.close();
        }
        if (recordBTL) {
        	outfileBTL.close();
        }
	}
}
