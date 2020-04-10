package collectors;

import housing.Model;
import housing.Household;
import housing.MortgageAgreement;

import java.util.ArrayList;

/**************************************************************************************************
 * Class to record mortgage data
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class CreditSupply {

    //------------------//
    //----- Fields -----//
    //------------------//

    private ArrayList<ArrayList<Double>>    oo_ltv;                 // To store individual mortgages' LTV values for owner-occupiers (FTB + HM)
    private ArrayList<ArrayList<Double>>    btl_ltv;                // To store individual mortgages' LTV values for buy-to-let investors
    private ArrayList<ArrayList<Double>>    oo_lti;                 // To store individual mortgages' LTI values for owner-occupiers (FTB + HM)
    private int                             rollingWindow;          // Size of the window to compute rolling averages for LTV and LTI
    private int                             currentTime;            // To store locally the current time and thus avoid repeated calls to Model
    private double                          interestRate;           // To record interest rate and give it to recorder object for file writing
    private double                          totalBTLCredit = 0.0;   // Buy to let mortgage credit
    private double                          totalOOCredit = 0.0;    // Owner-occupier mortgage credit
    private double                          netCreditGrowth;        // Rate of change of credit per month as percentage
    private int                             nApprovedMortgages;     // total number of new mortgages
    private int                             nFTBMortgages;          // Total number of new first time buyer mortgages
    private int                             nFTBMortgagesToBTL;     // Total number of new first time buyer mortgages to households with the BTL gene
    private int                             nBTLMortgages;          // Total number of new buy to let mortgages
    private double                          newCreditToHM;          // Total amount (principals) of new home mover mortgages
    private double                          newCreditToFTB;         // Total amount (principals) of new first time buyer mortgages
    private double                          newCreditToBTL;         // Total amount (principals) of new buy-to-let mortgages

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public CreditSupply(int targetPopulation, int rollingWindow) {
        this.rollingWindow = rollingWindow;
        oo_ltv = new ArrayList<>(this.rollingWindow);
        btl_ltv = new ArrayList<>(this.rollingWindow);
        oo_lti = new ArrayList<>(this.rollingWindow);
        for (int i = 0; i < this.rollingWindow; i++) {
            // Initial capacity designed for efficiency up to around 5% of households getting a new mortgage per month
            oo_ltv.add(new ArrayList<>((int)(targetPopulation * 0.05)));
            btl_ltv.add(new ArrayList<>((int)(targetPopulation * 0.01)));
            oo_lti.add(new ArrayList<>((int)(targetPopulation * 0.05)));
        }
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public void init() {
        for (int i = 0; i < rollingWindow; i++) {
            oo_ltv.get(i).clear();
            btl_ltv.get(i).clear();
            oo_lti.get(i).clear();
        }
    }

    public void preClearingResetCounters(int currentTime) {
        this.currentTime = currentTime;
        nApprovedMortgages = 0;
        nFTBMortgages = 0;
        nFTBMortgagesToBTL = 0;
        nBTLMortgages = 0;
        newCreditToHM = 0.0;
        newCreditToFTB = 0.0;
        newCreditToBTL = 0.0;
        oo_ltv.get(this.currentTime % rollingWindow).clear();
        btl_ltv.get(this.currentTime % rollingWindow).clear();
        oo_lti.get(this.currentTime % rollingWindow).clear();
    }

    /**
     * Collect information for this time step
     */
    public void postClearingRecord() {
        interestRate = Model.bank.getMortgageInterestRate();
        double oldTotalCredit = totalOOCredit + totalBTLCredit;
        totalOOCredit = 0.0;
        totalBTLCredit = 0.0;
        for(MortgageAgreement m : Model.bank.mortgages) {
            if(m.isBuyToLet) {
                totalBTLCredit += m.principal;
            } else {
                totalOOCredit += m.principal;
            }
        }
        if (oldTotalCredit > 0.0) {
            netCreditGrowth = (totalOOCredit + totalBTLCredit - oldTotalCredit) / oldTotalCredit;
        } else {
            netCreditGrowth = 0;
        }
    }

    /**
     * Record information for a newly issued mortgage
     * @param h Household being awarded the loan
     * @param approval Mortgage agreement
     */
    public void recordLoan(Household h, MortgageAgreement approval) {
        nApprovedMortgages += 1;
        if (approval.isFirstTimeBuyer) {
            nFTBMortgages += 1;
            newCreditToFTB += approval.principal;
            oo_ltv.get(currentTime % rollingWindow).add(100.0 * approval.principal
                    / (approval.principal + approval.downPayment));
            oo_lti.get(currentTime % rollingWindow).add(approval.principal / h.getAnnualGrossEmploymentIncome());
            if (h.behaviour.isPropertyInvestor()) {
                nFTBMortgagesToBTL += 1;
            }
        } else if (approval.isBuyToLet) {
            nBTLMortgages += 1;
            newCreditToBTL += approval.principal;
            btl_ltv.get(currentTime % rollingWindow).add(100.0 * approval.principal
                    / (approval.principal + approval.downPayment));
        } else {
            newCreditToHM += approval.principal;
            oo_ltv.get(currentTime % rollingWindow).add(100.0 * approval.principal
                    / (approval.principal + approval.downPayment));
            oo_lti.get(currentTime % rollingWindow).add(approval.principal / h.getAnnualGrossEmploymentIncome());
        }
    }

    //----- Getter/setter methods -----//

    ArrayList<ArrayList<Double>> getOO_ltv() { return oo_ltv; }

    ArrayList<ArrayList<Double>> getBTL_ltv() { return btl_ltv; }

    ArrayList<ArrayList<Double>> getOO_lti() { return oo_lti; }

    double getInterestRate() { return interestRate; }

    int getnRegisteredMortgages() { return Model.bank.mortgages.size(); }

    int getnApprovedMortgages() { return nApprovedMortgages; }

    int getnFTBMortgages() { return nFTBMortgages; }

    int getnFTBMortgagesToBTL() { return nFTBMortgagesToBTL; }

    int getnHMMortgages() { return nApprovedMortgages - nFTBMortgages - nBTLMortgages; }

    int getnBTLMortgages() { return nBTLMortgages; }

    double getTotalBTLCredit() { return totalBTLCredit; }

    double getTotalOOCredit() { return totalOOCredit; }

    double getNetCreditGrowth() { return netCreditGrowth; }

    double getNewCreditToFTB() { return newCreditToFTB; }

    double getNewCreditToHM() { return newCreditToHM; }

    double getNewCreditToBTL() { return newCreditToBTL; }

    double getNewCreditTotal() { return newCreditToFTB + newCreditToHM + newCreditToBTL; }
}
