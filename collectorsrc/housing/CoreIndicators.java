package housing;

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
public class CoreIndicators {
	
	/***
	 * @return Owner-occupier mortgage LTV ratio (mean above the median)
	 */
	public double getOwnerOccupierLTVMeanAboveMedian() {
		int i,j;
		double top, bottom;

		i = 0;
		j = Collectors.creditSupply.ltv_distribution
		
		Collectors.creditSupply.ltv_distribution[][]
	}
	

}
