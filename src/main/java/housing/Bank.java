package housing;

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
    private Config                      config = Model.config; // Passes the Model's configuration parameters object to a private field
    private CentralBank                 centralBank; // Connection to the central bank to ask for policy

    // Bank fields
    public HashSet<MortgageAgreement>   mortgages; // All unpaid mortgage contracts supplied by the bank
    public double                       interestSpread; // Current mortgage interest spread above base rate (monthly rate*12)
    private double                      monthlyPaymentFactor; // Monthly payment as a fraction of the principal for non-BTL mortgages
    private double                      monthlyPaymentFactorBTL; // Monthly payment as a fraction of the principal for BTL (interest-only) mortgages

    // Credit supply strategy fields
    private double                      supplyTarget; // Target supply of mortgage lending (pounds)
    private double                      supplyVal; // Monthly supply of mortgage loans (pounds)
    private int                         nOOMortgagesOverLTI; // Number of mortgages for owner-occupying that go over the LTI cap this time step
    private int                         nOOMortgages; // Total number of mortgages for owner-occupying

    // LTV internal policy thresholds
    private double                      firstTimeBuyerLTVLimit; // Loan-To-Value upper limit for first-time buyer mortgages
    private double                      ownerOccupierLTVLimit; // Loan-To-Value upper limit for owner-occupying mortgages
    private double                      buyToLetLTVLimit; // Loan-To-Value upper limit for buy-to-let mortgages

    // LTI internal policy thresholds
    private double                      firstTimeBuyerLTILimit; // Loan-To-Income internal upper limit for first-time buyer mortgages
    private double                      ownerOccupierLTILimit; // Loan-To-Income internal upper limit for owner-occupying mortgages

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public Bank(CentralBank centralBank) {
        this.centralBank = centralBank;
        mortgages = new HashSet<>();
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    void init() {
        mortgages.clear();
        setMortgageInterestRate(config.BANK_INITIAL_RATE); // Central Bank must already be initiated at this point!
        resetMonthlyCounters();
        // Setup initial LTV internal policy thresholds
        firstTimeBuyerLTVLimit = config.BANK_MAX_FTB_LTV;
        ownerOccupierLTVLimit = config.BANK_MAX_OO_LTV;
        buyToLetLTVLimit = config.BANK_MAX_BTL_LTV;
        // Setup initial LTI internal policy thresholds
        firstTimeBuyerLTILimit = config.BANK_MAX_FTB_LTI;
        ownerOccupierLTILimit = config.BANK_MAX_OO_LTI;
    }

    /**
     * Redo all necessary monthly calculations and reset counters.
     *
     * @param totalPopulation Current population in the model, needed to scale the target amount of credit
     */
    public void step(int totalPopulation) {
        supplyTarget = config.BANK_CREDIT_SUPPLY_TARGET * totalPopulation;
        setMortgageInterestRate(recalculateInterestRate());
        resetMonthlyCounters();
    }

    /**
     *  Reset counters for the next month.
     */
    private void resetMonthlyCounters() {
        supplyVal = 0.0;
        nOOMortgagesOverLTI = 0;
        nOOMortgages = 0;
    }

    /**
     * Calculate the mortgage interest rate for next month based on the rate for this month and the resulting demand for
     * credit, which in this model is equivalent to supply. This assumes a linear relationship between interest rate and
     * the excess demand for credit over the target level, and aims to bring this excess demand to zero at a speed
     * proportional to the excess.
     */
    private double recalculateInterestRate() {
        // TODO: Remove 1/2 factor and invert parameter BANK_D_DEMAND_D_INTEREST to turn division into product
        double rate = getMortgageInterestRate() + 0.5 * (supplyVal - supplyTarget) / config.BANK_D_DEMAND_D_INTEREST;
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
        recalculateMonthlyPaymentFactor();
    }

    /**
     * Compute the monthly payment factor, i.e., the monthly payment on a mortgage as a fraction of the mortgage
     * principal, for both BTL (interest-only) and non-BTL mortgages.
     */
    private void recalculateMonthlyPaymentFactor() {
        double r = getMortgageInterestRate() / config.constants.MONTHS_IN_YEAR;
        monthlyPaymentFactor = r / (1.0 - Math.pow(1.0 + r, -config.derivedParams.N_PAYMENTS));
        monthlyPaymentFactorBTL = r;
    }

    /**
     * Get the monthly payment factor, i.e., the monthly payment on a mortgage as a fraction of the mortgage principal.
     */
    private double getMonthlyPaymentFactor(boolean isHome) {
        if (isHome) {
            return monthlyPaymentFactor; // Monthly payment factor to pay off the principal in N_PAYMENTS
        } else {
            return monthlyPaymentFactorBTL; // Monthly payment factor for interest-only mortgages
        }
    }

    /**
     * Arrange a mortgage contract and get a MortgageAgreement object, which is added to the Bank's HashSet of mortgages
     * and entered into CreditSupply statistics.
     *
     * @param h The household requesting the mortgage
     * @param housePrice The price of the house that household h wants to buy
     * @param isHome True if household h plans to live in the house (non-BTL mortgage), False otherwise
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
            supplyVal += approval.principal;
            // ...update various statistics at CreditSupply
            Model.creditSupply.recordLoan(h, approval);
            // ... count the number of non-BTL mortgages over the LTI limit
            if (isHome) {
                ++nOOMortgages;
                if (approval.principal > h.getAnnualGrossEmploymentIncome()
                        * centralBank.getLoanToIncomeLimit(h.isFirstTimeBuyer())) {
                    ++nOOMortgagesOverLTI;
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
     * @param isHome True if household h plans to live in the house (non-BTL mortgage), False otherwise
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

        /*
         * Constraints specific to non-BTL mortgages
         */

        if (isHome) {
            // Affordability constraint: it sets a maximum value for the monthly mortgage payment divided by the
            // household's monthly net employment income
            double affordable_principal = config.CENTRAL_BANK_AFFORDABILITY_COEFF * h.getMonthlyNetEmploymentIncome()
                    / getMonthlyPaymentFactor(true);
            approval.principal = Math.min(approval.principal, affordable_principal);
            // Loan-To-Income (LTI) constraint: it sets a maximum value for the principal divided by the household's
            // annual gross employment income
            double lti_principal = h.getAnnualGrossEmploymentIncome() * getLoanToIncomeLimit(h.isFirstTimeBuyer());
            approval.principal = Math.min(approval.principal, lti_principal);

        /*
         * Constraints specific to BTL mortgages
         */

        } else {
            // Interest Coverage Ratio (ICR) constraint: it sets a minimum value for the expected annual rental income
            // divided by the annual interest expenses
            double icr_principal = Model.rentalMarketStats.getExpAvFlowYield() * housePrice
                    / (centralBank.getInterestCoverRatioLimit() * getMortgageInterestRate());
            approval.principal = Math.min(approval.principal, icr_principal);
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

        approval.monthlyPayment = approval.principal * getMonthlyPaymentFactor(isHome);
        approval.nPayments = config.derivedParams.N_PAYMENTS;
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
     * leverage.
     *
     * @param h The household applying for the mortgage
     * @param isHome True if household h plans to live in the house (non-BTL mortgage), False otherwise
     * @return A double with the maximum house price that this mortgage-lender is willing to approve a mortgage for
     */
    double getMaxMortgagePrice(Household h, boolean isHome) {
        // First, maximise leverage by maximising the down-payment, thus using all the liquid wealth of the household
        // (except 1 cent to avoid rounding errors), with no home equity added, as home-movers always sell their homes
        // before bidding for new ones
        double max_downpayment = h.getBankBalance() - 0.01;

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
            // household's monthly net employment income
            double affordable_max_price = max_downpayment + config.CENTRAL_BANK_AFFORDABILITY_COEFF
                    * h.getMonthlyNetEmploymentIncome() / getMonthlyPaymentFactor(true);
            max_price = Math.min(max_price, affordable_max_price);
            // Loan-To-Income (LTI) constraint: it sets a maximum value for the principal divided by the household's
            // annual gross employment income
            double lti_max_price = max_downpayment + h.getAnnualGrossEmploymentIncome()
                    * getLoanToIncomeLimit(h.isFirstTimeBuyer());
            max_price = Math.min(max_price, lti_max_price);

        /*
         * Constraints specific to BTL mortgages
         */

        } else {
            // Interest Coverage Ratio (ICR) constraint: it sets a minimum value for the expected annual rental income
            // divided by the annual interest expenses
            double icr_max_price = max_downpayment / (1.0 - Model.rentalMarketStats.getExpAvFlowYield()
                    / (centralBank.getInterestCoverRatioLimit() * getMortgageInterestRate()));
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
     * Get the Loan-To-Value ratio limit applicable by this private bank to a given household. Note that this limit is
     * self-imposed by the private bank.
     *
     * @param isFirstTimeBuyer True if the household is a first-time buyer
     * @param isHome True if the mortgage is to buy a home for the household (non-BTL mortgage)
     * @return The Loan-To-Value ratio limit applicable to the given household
     */
    private double getLoanToValueLimit(boolean isFirstTimeBuyer, boolean isHome) {
        if(isHome) {
            if(isFirstTimeBuyer) {
                return firstTimeBuyerLTVLimit;
            } else {
                return ownerOccupierLTVLimit;
            }
        }
        return buyToLetLTVLimit;
    }

    /**
     * Get the Loan-To-Income ratio limit applicable by this private bank to a given household. Note that Loan-To-Income
     * constraints apply only to non-BTL applicants. The private bank always imposes its own (hard) limit. Apart from
     * this, it also imposes the Central Bank regulated limit, which allows for a certain fraction of residential loans
     * (mortgages for owner-occupying) to go over it (and thus it is considered here a soft limit).
     *
     * @param isFirstTimeBuyer true if the household is a first-time buyer
     * @return The Loan-To-Income ratio limit applicable to the given household
     */
    private double getLoanToIncomeLimit(boolean isFirstTimeBuyer) {
        double limit;
        // First compute the private bank self-imposed (hard) limit, which applies always
        if (isFirstTimeBuyer) {
            limit = firstTimeBuyerLTILimit;
        } else {
            limit = ownerOccupierLTILimit;
        }
        // If the fraction of non-BTL mortgages already underwritten over the Central Bank LTI limit exceeds a certain
        // maximum (regulated also by the Central Bank)...
        if ((nOOMortgagesOverLTI + 1.0)/(nOOMortgages + 1.0) > centralBank.getMaxFractionOverLTILimit()) {
            // ... then compare the Central Bank LTI (soft) limit and that of the private bank (hard) and choose the smallest
            limit = Math.min(limit, centralBank.getLoanToIncomeLimit(isFirstTimeBuyer));
        }
        return limit;
    }
}
