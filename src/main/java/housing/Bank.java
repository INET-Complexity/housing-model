package housing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**************************************************************************************************
 * Class to represent a mortgage-lender (i.e. a bank or building society), whose only function is
 * to approve/decline mortgage requests, so this is where mortgage-lending policy is encoded
 *
 * @author daniel, davidrpugh, Adrian Carro
 *
 *************************************************************************************************/
public class Bank {

    //------------------//
    //----- Fields -----//
    //------------------//

    // General fields
    private Config              config = Model.config;      // Passes the Model's configuration parameters object to a private field
    private CentralBank         centralBank;                // Connection to the central bank to ask for policy

    // Bank fields
    public HashSet<MortgageAgreement>   mortgages;                  // All unpaid mortgage contracts supplied by the bank
    private double                      interestSpread;             // Current mortgage interest spread above base rate (monthly rate*12)

    // LTI tracking fields
    private int                 nFTBMortOverSoftMaxLTI_Prospec; // Number of prospective new mortgages to first-time buyers over the soft maximum LTI studied this month
    private int                 nFTBMortOverSoftMaxLTI_New;     // Number of new mortgages to first-time buyers over the soft maximum LTI underwritten this month
    private int                 nFTBMortOverSoftMaxLTI_Acc;     // Number of mortgages to FTBs over the soft maximum LTI accumulated over (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private ArrayList<Integer>  nFTBMortOverSoftMaxLTI_List;    // List to store the number of new mortgages to FTBs over the soft maximum LTI for (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private int                 nFTBMortgages_Prospec;          // Total number of prospective new mortgages to first-time buyers studied this month
    private int                 nFTBMortgages_New;              // Total number of new mortgages to first-time buyers
    private int                 nFTBMortgages_Acc;              // Total number of mortgages to FTBs accumulated over (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private ArrayList<Integer>  nFTBMortgages_List;             // List to store the number of new mortgages to FTBs for (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private double              maxFracFTBProspecOverSoftMaxLTI;// Internal max fraction of FTB prospective mortgages over the soft maximum LTI in order to reach the target with actual mortgages
    private int                 nHMMortOverSoftMaxLTI_Prospec;  // Number of prospective new mortgages to home movers over the soft maximum LTI
    private int                 nHMMortOverSoftMaxLTI_New;      // Number of new mortgages to home movers over the soft maximum LTI
    private int                 nHMMortOverSoftMaxLTI_Acc;      // Number of mortgages to HMs over the soft maximum LTI accumulated over (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private ArrayList<Integer>  nHMMortOverSoftMaxLTI_List;     // List to store the number of new mortgages to HMs over the soft maximum LTI for (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private int                 nHMMortgages_Prospec;           // Total number of prospective new mortgages to home movers
    private int                 nHMMortgages_New;               // Total number of new mortgages to home movers
    private int                 nHMMortgages_Acc;               // Total number of mortgages to HMs accumulated over (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private ArrayList<Integer>  nHMMortgages_List;              // List to store the number of new mortgages to HMs for (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
    private double              maxFracHMProspecOverSoftMaxLTI; // Internal max fraction of HM prospective mortgages over the soft maximum LTI in order to reach the target with actual mortgages

    // Credit supply strategy fields
    private double              monthlyCreditSupply;        // Monthly supply of mortgage lending (pounds)
    private double              monthlyCreditSupplyOld;     // Previous value of monthly supply of mortgage lending (pounds)

    // LTV internal policy thresholds
    private double              firstTimeBuyerHardMaxLTV;   // Loan-To-Value hard maximum for first-time buyer mortgages
    private double              homeMoverHardMaxLTV;        // Loan-To-Value hard maximum for home mover mortgages
    private double              buyToLetHardMaxLTV;         // Loan-To-Value hard maximum for buy-to-let mortgages

    // LTI internal policy thresholds
    private double              firstTimeBuyerHardMaxLTI;   // Loan-To-Income hard maximum for first-time buyer mortgages
    private double              homeMoverHardMaxLTI;        // Loan-To-Income hard maximum for home mover mortgages

    // Affordability internal policy thresholds
    private double              hardMaxAffordability;       // Affordability hard maximum (monthly mortgage payment / household's monthly net employment income)

    // ICR internal policy thresholds
    private double              hardMinICR;                 // ICR hard minimum for the ratio of expected rental yield over interest monthly payment

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public Bank(CentralBank centralBank) {
        this.centralBank = centralBank;
        mortgages = new HashSet<>();
        nFTBMortOverSoftMaxLTI_List = new ArrayList<>();
        nFTBMortgages_List = new ArrayList<>();
        nHMMortOverSoftMaxLTI_List = new ArrayList<>();
        nHMMortgages_List = new ArrayList<>();
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    void init() {
        mortgages.clear();
        initLTICounters();
        setMortgageInterestRate(config.BANK_INITIAL_RATE); // Central Bank must already be initiated at this point!
        resetMonthlyCounters();
        monthlyCreditSupplyOld = config.BANK_INITIAL_CREDIT_SUPPLY * config.TARGET_POPULATION;
        // Setup initial LTV internal policy thresholds
        firstTimeBuyerHardMaxLTV = config.BANK_LTV_HARD_MAX_FTB;
        homeMoverHardMaxLTV = config.BANK_LTV_HARD_MAX_HM;
        buyToLetHardMaxLTV = config.BANK_LTV_HARD_MAX_BTL;
        // Setup initial LTI internal policy thresholds
        firstTimeBuyerHardMaxLTI = config.BANK_LTI_HARD_MAX_FTB;
        homeMoverHardMaxLTI = config.BANK_LTI_HARD_MAX_HM;
        // Set initial affordability internal policy thresholds
        hardMaxAffordability = config.BANK_AFFORDABILITY_HARD_MAX;
        // Set initial ICR internal policy thresholds
        hardMinICR = config.BANK_ICR_HARD_MIN;
    }

    private void initLTICounters() {
        nFTBMortOverSoftMaxLTI_Prospec = 0;
        nFTBMortOverSoftMaxLTI_New = 0;
        nFTBMortOverSoftMaxLTI_Acc = 0;
        nFTBMortOverSoftMaxLTI_List.clear();
        nFTBMortOverSoftMaxLTI_List.addAll(Collections.nCopies(centralBank.getMonthsToCheckLTI() - 1, 0));
        nFTBMortgages_Prospec= 0;
        nFTBMortgages_New = 0;
        nFTBMortgages_Acc = 0;
        nFTBMortgages_List.clear();
        nFTBMortgages_List.addAll(Collections.nCopies(centralBank.getMonthsToCheckLTI() - 1, 0));
        maxFracFTBProspecOverSoftMaxLTI = centralBank.getFirstTimeBuyerMaxFracOverSoftMaxLTI();
        nHMMortOverSoftMaxLTI_Prospec = 0;
        nHMMortOverSoftMaxLTI_New = 0;
        nHMMortOverSoftMaxLTI_Acc = 0;
        nHMMortOverSoftMaxLTI_List.clear();
        nHMMortOverSoftMaxLTI_List.addAll(Collections.nCopies(centralBank.getMonthsToCheckLTI() - 1, 0));
        nHMMortgages_Prospec = 0;
        nHMMortgages_New = 0;
        nHMMortgages_Acc = 0;
        nHMMortgages_List.clear();
        nHMMortgages_List.addAll(Collections.nCopies(centralBank.getMonthsToCheckLTI() - 1, 0));
        maxFracHMProspecOverSoftMaxLTI = centralBank.getHomeMoverMaxFracOverSoftMaxLTI();
    }

    /**
     * Redo all necessary monthly calculations and reset counters.
     *
     * @param totalPopulation Current population in the model, needed to scale the target amount of credit
     */
    public void step(int totalPopulation) {
        setMortgageInterestRate(recalculateInterestRate(totalPopulation));
        resetMonthlyCounters();
    }

    /**
     *  Reset counters for the next month.
     */
    private void resetMonthlyCounters() {
        // Reset the maximum fractions of prospective mortgages over their soft LTI limits, with a single result in case
        // the maximum fractions of actual mortgages are the same
        if (centralBank.getFirstTimeBuyerMaxFracOverSoftMaxLTI() == centralBank.getHomeMoverMaxFracOverSoftMaxLTI()) {
            maxFracFTBProspecOverSoftMaxLTI = getNextMaxFracProspecOverSoftMaxLTI(
                    (nFTBMortOverSoftMaxLTI_Acc + nHMMortOverSoftMaxLTI_Acc),
                    (nFTBMortOverSoftMaxLTI_New + nHMMortOverSoftMaxLTI_New), (nFTBMortgages_Acc + nHMMortgages_Acc),
                    (nFTBMortgages_New + nHMMortgages_New), centralBank.getHomeMoverMaxFracOverSoftMaxLTI());
        } else {
            maxFracFTBProspecOverSoftMaxLTI = getNextMaxFracProspecOverSoftMaxLTI( nFTBMortOverSoftMaxLTI_Acc,
                    nFTBMortOverSoftMaxLTI_New, nFTBMortgages_Acc, nFTBMortgages_New,
                    centralBank.getFirstTimeBuyerMaxFracOverSoftMaxLTI());
            maxFracHMProspecOverSoftMaxLTI = getNextMaxFracProspecOverSoftMaxLTI(nHMMortOverSoftMaxLTI_Acc,
                    nHMMortOverSoftMaxLTI_New, nHMMortgages_Acc, nHMMortgages_New,
                    centralBank.getHomeMoverMaxFracOverSoftMaxLTI());
        }
        // Reset to zero the monthly credit supply counter
        monthlyCreditSupply = 0.0;
        // Reset moving counter of first-time buyer mortgages over their soft maximum LTI
        nFTBMortOverSoftMaxLTI_Prospec = 0;
        nFTBMortOverSoftMaxLTI_Acc -= nFTBMortOverSoftMaxLTI_List.remove(0); // Remove oldest month from list and subtract it from accumulated sum
        nFTBMortOverSoftMaxLTI_Acc += nFTBMortOverSoftMaxLTI_New; // Add most recent month to accumulated sum
        nFTBMortOverSoftMaxLTI_List.add(nFTBMortOverSoftMaxLTI_New); // Add most recent month to list
        nFTBMortOverSoftMaxLTI_New = 0; // Reset new mortgages counter to zero for next time step
        // Reset moving counter of first-time buyer mortgages
        nFTBMortgages_Prospec= 0;
        nFTBMortgages_Acc -= nFTBMortgages_List.remove(0); // Remove oldest month from list and subtract it from accumulated sum
        nFTBMortgages_Acc += nFTBMortgages_New; // Add most recent month to accumulated sum
        nFTBMortgages_List.add(nFTBMortgages_New); // Add most recent month to list
        nFTBMortgages_New = 0; // Reset new mortgages counter to zero for next time step
        // Reset moving counter of home mover mortgages over their soft maximum LTI
        nHMMortOverSoftMaxLTI_Prospec = 0;
        nHMMortOverSoftMaxLTI_Acc -= nHMMortOverSoftMaxLTI_List.remove(0); // Remove oldest month from list and subtract it from accumulated sum
        nHMMortOverSoftMaxLTI_Acc += nHMMortOverSoftMaxLTI_New; // Add most recent month to accumulated sum
        nHMMortOverSoftMaxLTI_List.add(nHMMortOverSoftMaxLTI_New); // Add most recent month to list
        nHMMortOverSoftMaxLTI_New = 0; // Reset new mortgages counter to zero for next time step
        // Reset moving counter of home mover mortgages
        nHMMortgages_Prospec = 0;
        nHMMortgages_Acc -= nHMMortgages_List.remove(0); // Remove oldest month from list and subtract it from accumulated sum
        nHMMortgages_Acc += nHMMortgages_New; // Add most recent month to accumulated sum
        nHMMortgages_List.add(nHMMortgages_New); // Add most recent month to list
        nHMMortgages_New = 0; // Reset new mortgages counter to zero for next time step
    }

    /**
     * Plan next month's fraction of approval in principle letters over the soft limit so as to match the target
     * fraction of actual mortgages over the soft limit
     */
    private double getNextMaxFracProspecOverSoftMaxLTI(int nMortOverSoftMaxLTI_Acc, int nMortOverSoftMaxLTI_New,
                                                       int nMortgages_Acc, int nMortgages_New,
                                                       double centralBankLimit) {
        // First compute the current fraction of actual mortgages over the corresponding soft LTI limit
        double actualFraction;
        if (nMortgages_Acc + nMortgages_New > 0) {
            actualFraction = (double) (nMortOverSoftMaxLTI_Acc + nMortOverSoftMaxLTI_New)
                    / (nMortgages_Acc + nMortgages_New);
        } else {
            actualFraction = 0;
        }
        // Then plan next month's fraction of approval in principle letters over the soft limit as the distance between
        // the actual fraction of mortgages over the soft limit and the target fraction
        if (actualFraction > centralBankLimit) {
            return 0.0;
        } else {
            return centralBankLimit - actualFraction;
        }
    }

    /**
     * Calculate the mortgage interest rate for next month based on the rate for this month and the resulting demand for
     * credit, which in this model is equivalent to supply. This assumes a linear relationship between interest rate and
     * the excess demand for credit over the target level, and aims to bring this excess demand to zero at a speed
     * proportional to the excess.
     */
    private double recalculateInterestRate(int totalPopulation) {
        double rate = getMortgageInterestRate()
                + config.BANK_D_INTEREST_D_DEMAND * (monthlyCreditSupply - monthlyCreditSupplyOld) / totalPopulation;
        monthlyCreditSupplyOld = monthlyCreditSupply; // After using the current value of the supply of credit, store it as old value for next month
        if (rate < centralBank.getBaseRate()) rate = centralBank.getBaseRate();
        return rate;
    }

    /**
     * Get the interest rate on mortgages.
     */
    public double getMortgageInterestRate() { return centralBank.getBaseRate() + interestSpread; }


    /**
     * Set the interest rate on mortgages.
     */
    private void setMortgageInterestRate(double rate) {
        interestSpread = rate - centralBank.getBaseRate();
    }

    /**
     * Get the monthly payment factor, i.e., the monthly payment on a mortgage as a fraction of the mortgage principal.
     * This takes into account age-based restrictions for non-BTL mortgages via the number of payments.
     */
    private double getMonthlyPaymentFactor(boolean isHome, double age) {
        double r = getMortgageInterestRate() / config.constants.MONTHS_IN_YEAR;
        // For non-BTL purchases, compute payment factor to pay off the principal in the agreed number of payments,
        // coherent with any mortgage length age-based restrictions
        if (isHome) {
            if (getNPayments(true, age) > 0) {
                return r / (1.0 - Math.pow(1.0 + r, -getNPayments(true, age)));
            } else {
                throw new RuntimeException("Trying to find monthly payment factor for a zero payments mortgage");
            }
        // For BTL purchases, compute interest-only payment factor (age-based restrictions applied elsewhere)
        } else {
            return r;
        }
    }

    /**
     * Compute the number of payments, taking into account differentiated age-based restrictions for BTL and non-BTL
     * bids. In particular, BTL mortgages always have full maturity, but they can only be approved before the household
     * reaches the age limit. On the contrary, non-BTL mortgages start seeing their maturities reduced before the age
     * limit, in such a way that the full amount is repaid by the time the household reaches this limit.
     */
    private int getNPayments(boolean isHome, double age) {
        // For non-BTL purchases, any mortgage principal must be repaid when the household turns 65
        if (isHome) {
            if (age <= config.BANK_AGE_LIMIT - config.MORTGAGE_DURATION_YEARS) {
                return config.MORTGAGE_DURATION_YEARS * config.constants.MONTHS_IN_YEAR;
            } else if (age <= config.BANK_AGE_LIMIT) {
                return (int) ((config.BANK_AGE_LIMIT - age) * config.constants.MONTHS_IN_YEAR);
            } else {
                return 0;
            }
        // For BTL purchases, a mortgage can only be approved before the household turns 65
        } else {
            if (age <= config.BANK_AGE_LIMIT) {
                return config.MORTGAGE_DURATION_YEARS * config.constants.MONTHS_IN_YEAR;
            } else {
                return 0;
            }
        }
    }

    /**
     * Arrange a mortgage contract and get a MortgageAgreement object, which is added to the Bank's HashSet of mortgages
     * and entered into CreditSupply statistics.
     *
     * @param h The household requesting the mortgage
     * @param housePrice The price of the house that household h wants to buy
     * @param isHome True if household h plans to live in the house (non-BTL mortgage)
     * @return The MortgageApproval object
     */
    MortgageAgreement requestLoan(Household h, double housePrice, double desiredDownPayment, boolean isHome) {
        // Request the mortgage and create the MortgageAgreement object with all the required parameters
        MortgageAgreement approval = requestApproval(h, housePrice, desiredDownPayment, isHome);
        // If this is an actual mortgage, i.e., with a non-zero principal...
        if (approval.principal > 0.0) {
            // ...add it to the Bank's HashSet of mortgages
            mortgages.add(approval);
            // ...add the principal to the new supply/demand of credit
            monthlyCreditSupply += approval.principal;
            // ...update various statistics at CreditSupply
            Model.creditSupply.recordLoan(h, approval);
            // ... count the number of non-BTL mortgages over the soft LTI limit imposed by the Central Bank...
            if (isHome) {
                // ...differentiating between first-time buyers
                if (h.isFirstTimeBuyer()) {
                    ++nFTBMortgages_New;
                    if (approval.principal > h.getAnnualGrossEmploymentIncome()
                            * centralBank.getFirstTimeBuyerSoftMaxLTI()) {
                        ++nFTBMortOverSoftMaxLTI_New;
                    }
                // ...and home movers
                } else {
                    ++nHMMortgages_New;
                    if (approval.principal > h.getAnnualGrossEmploymentIncome()
                            * centralBank.getHomeMoverSoftMaxLTI()) {
                        ++nHMMortOverSoftMaxLTI_New;
                    }
                }
            }
        }
        return approval;
    }

    /**
     * Request a mortgage approval without actually signing a mortgage contract, i.e., the returned
     * MortgageAgreement object is not added to the Bank's HashSet of mortgages nor entered into CreditSupply
     * statistics. This is useful for households to explore the details of the best available mortgage contract before
     * deciding whether to actually go ahead and sign it.
     *
     * @param h The household requesting the mortgage
     * @param housePrice The price of the house that household h wants to buy
     * @param isHome True if household h plans to live in the house (non-BTL mortgage)
     * @return The MortgageApproval object
     */
    MortgageAgreement requestApproval(Household h, double housePrice, double desiredDownPayment, boolean isHome) {
        // Create a MortgageAgreement object to store and return the new mortgage data
        MortgageAgreement approval = new MortgageAgreement(h, !isHome);

        /*
         * Constraints for all mortgages
         */

        // Loan-To-Value (LTV) constraint: it sets a maximum value for the ratio of the principal divided by the house
        // price
        approval.principal = housePrice * getLoanToValueLimit(h.isFirstTimeBuyer(), isHome);

        if (getNPayments(isHome, h.getAge()) > 0) {

            /*
             * Constraints specific to non-BTL mortgages
             */

            if (isHome) {
                // Affordability constraint: it sets a maximum value for the monthly mortgage payment divided by the
                // household's monthly gross employment income
                double affordable_principal = getHardMaxAffordability() * h.getMonthlyGrossEmploymentIncome()
                        / getMonthlyPaymentFactor(true, h.getAge());
                if (getMonthlyPaymentFactor(true, h.getAge()) == 1.0) affordable_principal = 0.0;
                approval.principal = Math.min(approval.principal, affordable_principal);
                // Loan-To-Income (LTI) constraint: it sets a maximum value for the principal divided by the household's
                // annual gross employment income. The specific LTI limit used is that offered to the household in the
                // approval in principle letter issued by the bank
                double lti_principal = h.getAnnualGrossEmploymentIncome() * h.getPersistentLTILimit();
                approval.principal = Math.min(approval.principal, lti_principal);

                /*
                 * Constraints specific to BTL mortgages
                 */

            } else {
                // Interest Coverage Ratio (ICR) constraint: it sets a minimum value for the expected annual rental
                // income divided by the annual interest expenses
                double icr_principal = Model.rentalMarketStats.getExpAvFlowYield() * housePrice
                        / (getHardMinICR() * getMortgageInterestRate());
                approval.principal = Math.min(approval.principal, icr_principal);
            }

            // If number of payments is zero, then no principal is approved, purchase must be paid outright
        } else {
            approval.principal = 0.0;
        }

        /*
         * Compute the down-payment
         */

        // Start by assuming the minimum possible down-payment, i.e., that resulting from the above maximisation of the
        // principal available to the household, given its chosen house price
        approval.downPayment = housePrice - approval.principal;
        // Determine the liquid wealth of the household, with no home equity added, as home-movers always sell their
        // homes before bidding for new ones
        double liquidWealth = h.getBankBalance();
        // Ensure desired down-payment is between zero and the house price, capped also by the household's liquid wealth
        if (desiredDownPayment < 0.0) desiredDownPayment = 0.0;
        if (desiredDownPayment > housePrice) desiredDownPayment = housePrice;
        if (desiredDownPayment > liquidWealth) desiredDownPayment = liquidWealth;
        // If the desired down-payment is larger than the initially assumed minimum possible down-payment, then set the
        // down-payment to the desired value and update the principal accordingly
        if (desiredDownPayment > approval.downPayment) {
            approval.downPayment = desiredDownPayment;
            approval.principal = housePrice - desiredDownPayment;
        }

        /*
         * Set the rest of the variables of the MortgageAgreement object
         */

        if (getNPayments(isHome, h.getAge()) > 0) {
            approval.monthlyPayment = approval.principal * getMonthlyPaymentFactor(isHome, h.getAge());
        } else {
            approval.monthlyPayment = 0.0;
        }
        approval.nPayments = getNPayments(isHome, h.getAge());
        approval.monthlyInterestRate = getMortgageInterestRate() / config.constants.MONTHS_IN_YEAR;
        approval.purchasePrice = approval.principal + approval.downPayment;
        // Throw error and stop program if requested mortgage has down-payment larger than household's liquid wealth
        if (approval.downPayment > liquidWealth) {
            System.out.println("Error at Bank.requestApproval(), down-payment larger than household's bank balance: "
                    + "downpayment = " + approval.downPayment + ", bank balance = " + liquidWealth);
            System.exit(0);
        }

        return approval;
    }

    /**
     * Find, for a given household, the maximum house price that this mortgage-lender is willing to approve a mortgage
     * for. That is, this method assumes the household will use its total liquid wealth as deposit, thus maximising
     * leverage. Regarding the application of the hard/soft LTI limit, the specific limit used is that offered to the
     * household in the approval in principle letter issued by the bank, which is stored by the household.
     *
     * @param h The household applying for the mortgage
     * @param isHome True if household h plans to live in the house (non-BTL mortgage)
     * @return A double with the maximum house price that this mortgage-lender is willing to approve a mortgage for
     */
    double getMaxMortgagePrice(Household h, boolean isHome) {
        // First, maximise leverage by maximising the down-payment, thus using all the liquid wealth of the household
        // (except 1 cent to avoid rounding errors), with no home equity added, as home-movers always sell their homes
        // before bidding for new ones
        double max_downpayment = h.getBankBalance() - 0.01;

        // If number of payments is zero, then no principal is approved, purchase must be paid outright
        if (getNPayments(isHome, h.getAge()) == 0) return max_downpayment;

        /*
         * Constraints for all mortgages
         */

        // Loan-To-Value (LTV) constraint: it sets a maximum value for the ratio of the principal divided by the house
        // price, thus setting a maximum house price given a fixed (maximised) down-payment
        double max_price = max_downpayment / (1.0 - getLoanToValueLimit(h.isFirstTimeBuyer(), isHome));

        /*
         * Constraints specific to non-BTL mortgages
         */

        if (isHome) {
            // Affordability constraint: it sets a maximum value for the monthly mortgage payment divided by the
            // household's monthly gross employment income
            double affordable_max_price = max_downpayment + getHardMaxAffordability()
                    * h.getMonthlyGrossEmploymentIncome() / getMonthlyPaymentFactor(true, h.getAge());
            if (getMonthlyPaymentFactor(true, h.getAge()) == 1.0) affordable_max_price = max_downpayment;
            max_price = Math.min(max_price, affordable_max_price);
            // Loan-To-Income (LTI) constraint: it sets a maximum value for the principal divided by the household's
            // annual gross employment income. The specific LTI limit used is that offered to the household in the
            // approval in principle letter issued by the bank
            double lti_max_price = max_downpayment + h.getAnnualGrossEmploymentIncome()
                    * h.getPersistentLTILimit();
            max_price = Math.min(max_price, lti_max_price);

        /*
         * Constraints specific to BTL mortgages
         */

        } else {
            // Interest Coverage Ratio (ICR) constraint: it sets a minimum value for the expected annual rental income
            // divided by the annual interest expenses
            double icr_max_price = max_downpayment / (1.0 - Model.rentalMarketStats.getExpAvFlowYield()
                    / (getHardMinICR() * getMortgageInterestRate()));
            // When the rental yield is larger than the interest rate times the ICR, then the ICR does never constrain
            if (icr_max_price < 0.0) icr_max_price = Double.POSITIVE_INFINITY;
            max_price = Math.min(max_price,  icr_max_price);
        }

        return max_price;
    }

    /**
     * End a mortgage contract by removing it from the Bank's HashSet of mortgages
     *
     * @param mortgage The MortgageAgreement object to be removed
     */
    void endMortgageContract(MortgageAgreement mortgage) { mortgages.remove(mortgage); }

    //----- Mortgage policy methods -----//

    /**
     * Get the Loan-To-Value ratio limit currently applicable to a given type of household (first-time buyer, home mover
     * or buy-to-let investor). Note that this limit is defined as the minimum between the private bank self-imposed
     * internal policy limit and the central bank mandatory policy limit.
     *
     * @param isFirstTimeBuyer True if the household is a first-time buyer
     * @param isHome True if the mortgage is to buy a home for the household (non-BTL mortgage)
     * @return The Loan-To-Value ratio limit applicable to this type of household
     */
    private double getLoanToValueLimit(boolean isFirstTimeBuyer, boolean isHome) {
        if (isHome) {
            if (isFirstTimeBuyer) {
                return Math.min(firstTimeBuyerHardMaxLTV, centralBank.getFirstTimeBuyerHardMaxLTV());
            } else {
                return Math.min(homeMoverHardMaxLTV, centralBank.getHomeMoverHardMaxLTV());
            }
        }
        return Math.min(buyToLetHardMaxLTV, centralBank.getBuyToLetHardMaxLTV());
    }

    /**
     * Get the Loan-To-Income ratio limit currently applicable to a given type of household, whether first-time buyer or
     * home mover (that is, this constraint is applicable only to non-BTL mortgages). The private bank always imposes
     * its own internal hard limit. Apart from this, it also imposes the Central Bank regulated soft limit, which allows
     * for a certain fraction of non-BTL loans to go over this soft limit. This fraction of mortgages allowed to exceed
     * the soft limit is measure on a rolling basis with a window of (CENTRAL_BANK_LTI_MONTHS_TO_CHECK - 1) months
     * previous to the current one plus the current one.
     *
     * @param isFirstTimeBuyer True if the household is a first-time buyer
     * @return The Loan-To-Income ratio limit currently applicable to this type of household
     */
    double getLoanToIncomeLimit(boolean isFirstTimeBuyer) {

        // If the maximum fractions of mortgages over their soft LTI limits allowed by the Central Bank for FTBs and HMs
        // are the same, then the quota is shared by FTBs and HMs, instead of having separate quotas
        if (centralBank.getFirstTimeBuyerMaxFracOverSoftMaxLTI() == centralBank.getHomeMoverMaxFracOverSoftMaxLTI()) {
            // If this mortgage could bring the fraction of mortgages (FTB + HM) underwritten over the Central Bank LTI
            // soft limit to exceed the maximum fraction established by the Central Bank (same for FTB and HM)...
            if (((double)(nFTBMortOverSoftMaxLTI_Prospec + nHMMortOverSoftMaxLTI_Prospec + 1)
                    / (nFTBMortgages_Prospec + nHMMortgages_Prospec + 1) > maxFracFTBProspecOverSoftMaxLTI)) {
                // ... then use the minimum between the Central Bank soft limit and the private bank hard limit, for
                // either first-time buyers or home movers
                if (isFirstTimeBuyer) {
                    return Math.min(firstTimeBuyerHardMaxLTI, centralBank.getFirstTimeBuyerSoftMaxLTI());
                } else {
                    return Math.min(homeMoverHardMaxLTI, centralBank.getHomeMoverSoftMaxLTI());
                }
            // ...otherwise...
            } else {
                // ...simply use the private bank self-imposed hard maximum, for either first-time buyers or home movers
                if (isFirstTimeBuyer) {
                    return firstTimeBuyerHardMaxLTI;
                } else {
                    return homeMoverHardMaxLTI;
                }
            }
        // Otherwise, FTBs and HMs keep separate quotas for mortgages over their respective LTI limits
        } else {
            // For first-time buyers...
            if (isFirstTimeBuyer) {
                // ...if this mortgage could bring the fraction of mortgages underwritten over the Central Bank LTI soft
                // limit to exceed the maximum fraction established by the Central Bank...
                if (((double)(nFTBMortOverSoftMaxLTI_Prospec + 1) / (nFTBMortgages_Prospec + 1)
                        > maxFracFTBProspecOverSoftMaxLTI)) {
                    // ... then use the minimum between the Central Bank soft limit and the private bank hard limit
                    return Math.min(firstTimeBuyerHardMaxLTI, centralBank.getFirstTimeBuyerSoftMaxLTI());
                    // ...otherwise...
                } else {
                    // ...simply use the private bank self-imposed hard maximum
                    return firstTimeBuyerHardMaxLTI;
                }
                // For home movers...
            } else {
                // ...if this mortgage could bring the fraction of mortgages underwritten over the Central Bank LTI soft
                // limit to exceed the maximum fraction established by the Central Bank...
                if (((double)(nHMMortOverSoftMaxLTI_Prospec + 1) / (nHMMortgages_Prospec + 1)
                        > maxFracHMProspecOverSoftMaxLTI)) {
                    // ... then use the minimum between the Central Bank soft limit and the private bank hard limit
                    return Math.min(homeMoverHardMaxLTI, centralBank.getHomeMoverSoftMaxLTI());
                    // ...otherwise...
                } else {
                    // ...simply use the private bank self-imposed hard maximum
                    return homeMoverHardMaxLTI;
                }
            }
        }
    }

    /**
     * Get the most constraining affordability limit, between the private and the central bank policies
     */
    private double getHardMaxAffordability() {
        return Math.min(hardMaxAffordability, centralBank.getHardMaxAffordability());
    }

    /**
     * Get the most constraining Interest Coverage Ratio limit, between the private and the central bank policies
     */
    private double getHardMinICR() { return Math.max(hardMinICR, centralBank.getHardMinICR()); }

    public double getInterestSpread() { return interestSpread; }

    /**
     * This method notifies the bank that the household is accepting the approval in principle letter offered and thus
     * sending a bid to the sales market. The purpose is for the bank to take this information into account for
     * computing the number of approval in principle letters (and thus potential mortgages) over the central bank soft
     * LTI limit it has offered so far this month.
     */
    void acceptApprovalInPrincipleLetter(double max_downpayment, double price, double annualGrossEmploymentIncome,
                                         boolean isFTB) {
        // Count the number of prospective new (non-BTL) mortgages over the soft LTI limit imposed by the Central
        // Bank, differentiating between first-time buyers and home-movers
        if (isFTB) {
            ++Model.bank.nFTBMortgages_Prospec;
            if (price > max_downpayment
                    + annualGrossEmploymentIncome * Model.centralBank.getFirstTimeBuyerSoftMaxLTI()) {
                ++Model.bank.nFTBMortOverSoftMaxLTI_Prospec;
            }
        // ...and home movers
        } else {
            ++Model.bank.nHMMortgages_Prospec;
            if (price > max_downpayment
                    + annualGrossEmploymentIncome * Model.centralBank.getHomeMoverSoftMaxLTI()) {
                ++Model.bank.nHMMortOverSoftMaxLTI_Prospec;
            }
        }
    }
}
