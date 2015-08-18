package housing;

import utilities.MeanAboveMedian;

/***
 * This is a "Collector" class in the model-collector-observer architecture.
 * This class collects the information contained in the Bank of England
 * "Core Indicator" set for LTV and DTI limits, as set out in the Bank
 * of England's draft policy statement "The Financial policy committee's
 * power over housing tools" Feb.2015
 * 
 * Please see Table A for a list of these indicators and notes on their
 * definition.
 * 
 * @author daniel
 *
 */
public class CoreIndicators extends CollectorBase {
	
	public void step() {
	}
	
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		Collectors.creditSupply.setActive(active);
		Collectors.housingMarketStats.setActive(active);
		Collectors.householdStats.setActive(active);
	}
	
	public double getOwnerOccupierLTIMeanAboveMedian() {
		return(Collectors.creditSupply.oo_lti.apply(new MeanAboveMedian()));
	}
	public String desOwnerOccupierLTIMeanAboveMedian() {
		return("Owner-occupier mortage LTI ratio (mean above the median)");
	}
	public String nameOwnerOccupierLTIMeanAboveMedian() {
		return("Owner-occupier mortage LTI ratio");
	}

	public double getOwnerOccupierLTVMeanAboveMedian() {
		return(Collectors.creditSupply.oo_ltv.apply(new MeanAboveMedian()));
	}
	public String desOwnerOccupierLTVMeanAboveMedian() {
		return("Owner-occupier mortage LTV ratio (mean above the median)");
	}
	public String nameOwnerOccupierLTVMeanAboveMedian() {
		return("Owner-occupier mortage LTV ratio (%)");
	}

	public double getBuyToLetLTVMean() {
		return(Collectors.creditSupply.btl_ltv.getMean());
	}
	public String desBuyToLetLTVMean() {
		return("Buy-to-let loan-to-value ratio (mean)");
	}
	public String nameBuyToLetLTVMean() {
		return("Buy-to-let LTV ratio");
	}
	
	public double getHouseholdCreditGrowth() {
		return(Collectors.creditSupply.netCreditGrowth*12.0*100.0);
	}
	public String desHouseholdCreditGrowth() {
		return("Household credit growth (net, annualised, as a proportion of credit in previous step)");
	}
	public String nameHouseholdCreditGrowth() {
		return("Household credit growth (%)");
	}
	
	public double getDebtToIncome() {
		return(100.0*(Collectors.creditSupply.totalBTLCredit + Collectors.creditSupply.totalOOCredit)/Collectors.householdStats.totalAnnualIncome);
	}
	public String desDebtToIncome() {
		return("Household mortgage debt to income ratio (%)");
	}
	public String nameDebtToIncome() {
		return("Household mortgage debt to income ratio (%)");
	}
	
	/***
	 * 
	 */
	public double getOODebtToIncome() {
		return(100.0*Collectors.creditSupply.totalOOCredit/Collectors.householdStats.totalAnnualIncome);
	}
	public String desOODebtToIncome() {
		return("Household debt to income ratio (owner-occupier mortgages only) (%)");
	}
	public String nameOODebtToIncome() {
		return("Owner-occupier mortgage debt to income ratio (%)");
	}
	
	/***
	 * 
	 */
	public int getMortgageApprovals() {
		return((int)(Collectors.creditSupply.nApprovedMortgages*UK_HOUSEHOLDS/Model.households.size()));
	}
	public String desMortgageApprovals() {
		return("Number of mortgage approvals per month (scaled for 26.5 million households)");
	}
	public String nameMortgageApprovals() {
		return("Mortgage approvals");
	}
	
	
	public int getHousingTransactions() {
		return((int)(Collectors.housingMarketStats.nSales*UK_HOUSEHOLDS/Model.households.size()));
	}
	public String desHousingTransactions() {
		return("Number of houses bought/sold per month (scaled for 26.5 million households)");
	}
	public String nameHousingTransactions() {
		return("Housing Transactions");
	}
	
	public int getAdvancesToFTBs() {
		return((int)(Collectors.creditSupply.nFTBMortgages*UK_HOUSEHOLDS/Model.households.size()));
	}
	public String desAdvancesToFTBs() {
		return("Number of advances to first-time-buyers (scaled for 26.5 million households)");
	}
	public String nameAdvancesToFTBs() {
		return("Advances to first-time-buyers");
	}

	public int getAdvancesToBTL() {
		return((int)(Collectors.creditSupply.nBTLMortgages*UK_HOUSEHOLDS/Model.households.size()));
	}
	public String desAdvancesToBTL() {
		return("Number of advances to buy-to-let purchasers (scaled for 26.5 million households)");
	}
	public String nameAdvancesToBTL() {
		return("Advances to buy-to-let purchasers");
	}

	public int getAdvancesToHomeMovers() {
		return(getMortgageApprovals() - getAdvancesToFTBs() - getAdvancesToBTL());
	}
	public String desAdvancesToHomeMovers() {
		return("Number of advances to home-movers (scaled for 26.5 million households)");
	}
	public String nameAdvancesToHomeMovers() {
		return("Advances to home-movers");
	}

	public double getPriceToIncome() {
		return(Model.housingMarket.housePriceIndex*data.HouseSaleMarket.HPI_REFERENCE*Model.households.size()/Collectors.householdStats.totalAnnualIncome);
	}
	public String desPriceToIncome() {
		return("House price to household disposable income ratio");
	}
	public String namePriceToIncome() {
		return("House price to household disposable income ratio");
	}
	
	public double getRentalYield() {
		return(100.0*Collectors.householdStats.rentalYield);
	}
	public String desRentalYield() {
		return("Average gross annual yield on occupied rental properties");
	}
	public String nameRentalYield() {
		return("Rental Yield (%)");
	}

	public double getHousePriceGrowth() {
		return(100.0*Collectors.housingMarketStats.getHPA());
	}
	public String desHousePriceGrowth() {
		return("Growth of house price index (year on year)");
	}
	public String nameHousePriceGrowth() {
		return("Annual house price growth (%)");
	}

	public double getInterestRateSpread() {
		return(100.0*Model.bank.interestSpread);
	}
	public String desInterestRateSpread() {
		return("Spread between mortgage-lender interest rate and bank base-rate");
	}
	public String nameInterestRateSpread() {
		return("Interest Rate Spread (%)");
	}


	////////////////////////////////////////////////////////////////////////////////////////
	// Array tools
	////////////////////////////////////////////////////////////////////////////////////////
	

	/***
	 * 
	 * @param d 	The frequency distribution. Doesn't need to be normalised.
	 * @return 		The index in which the median of the given distribution
	 * lies. If all values are zero the result is 0
	 */
	/*
	protected int median(double [] d) {
		// count up from bottom and down from the top
		// median is where they meet in the middle
		if(d.length < 2) return(0);
		int top = d.length;
		int bottom = 0;
		double total = d[0];
		
		while(top > bottom) {
			if(total < 0.0) {
				total += d[++bottom];				
			} else {
				total -= d[--top];				
			}
		}
		return(bottom);
	}
	*/
	
	/***
	 * 
	 * @param d 	The frequency distribution on equally spaced bins. Doesn't need to be normalised.
	 * @return 		The mean index of the values in d that lie above the median. The distribution
	 * is assumed to consist of bars, centred around the index (like a bar-graph or histogram).
	 * 
	 */
	/*
	protected double meanAboveMedian(double [] d) {
		if(d.length == 0) return(0.0);
		double total;
		double mean;
		int i = median(d);

		total = d[i]*0.5;
		mean = (i+0.25)*total;
		while(++i < d.length) {
			total += d[i];
			mean += i*d[i];
		}
		return(mean/total);
	}
*/
	/*
	protected double mean(double [] d) {
		int i = 0;
		double mean = 0.0;
		double total = 0.0;
		
		while(++i < d.length) {
			mean += i*d[i];
			total += d[i];
		}
		return(mean/total);
	}
	*/
	
	/***
	 * 
	 * @param d			An array of values
	 * @param index 	A non-integer index on d
	 * @return 			The value of d, linearly interpolated between indices
	 */
	/*
	protected double linearInterpolate(double [] d, double index) {
		int 	i = (int)index;
		double 	frac = index - i;
		return((1.0-frac)*d[i] + frac*d[i+1]);		
	}
*/
	static final double UK_HOUSEHOLDS = 26.5e6; // approx number of households in UK
}
