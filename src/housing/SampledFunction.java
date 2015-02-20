package housing;

import java.util.Map;
import java.util.TreeMap;

/***********************************
 * Represents a function for which we have samples
 * at various points.
 * @author daniel
 *
 * @param <A>
 */
@SuppressWarnings("serial")
public class SampledFunction extends TreeMap<Double ,Double> {
	public SampledFunction(Double [][] init) {
		super();
		int i;
		for(i=0; i<init.length; ++i) {
			this.put(init[i][0], init[i][1]);
		}
	}
	
	/****
	 * 
	 * @param key
	 * @return the value of the sample immediately above key
	 */
	public Double getNextSample(Double key) {
		Map.Entry<Double,Double> result = this.ceilingEntry(key);
		if(result != null) {
			return(result.getValue());
		}
		return(0.0);
	}
	
	/*****
	 * Returns the definite integral of this function from zero to the
	 * value of key
	 * @param key
	 * @return
	 */
	public Double getCumulativeValue(Double key) {
		return(0.0);
	}
}
