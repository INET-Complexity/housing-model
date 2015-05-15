package housing;

// import java.util.function.DoubleUnaryOperator; // not compatible with Java 1.7

/****
 * Represents an arbitrarily shaped, 1-dimensional Probability Density Function.
 * Supply a DoubleUnaryOperator class that returns the probability density for
 * a given value.
 * 
 * @author daniel
 *
 */
public class Pdf {
	/**
	 * 
	 * @param ipdf functional class whose apply function returns the probability density at that point
	 * @param istart the value below which probability is assumed to be zero
	 * @param iend   the value above which probability is assumed to be zero
	 */
	public Pdf(double istart, double iend, DoubleUnaryOperator ipdf) {
		pdf = ipdf;
		start = istart;
		end = iend;
		initInverseCDF();
	}
	
	/***
	 * Get probability density P(x)
	 * @param x 
	 * @return P(x)
	 */
	public double p(double x) {
		return(pdf.applyAsDouble(x));
	}
	
	
	public void initInverseCDF() {
		double cp;		// cumulative proability
		double targetcp;// target cumulative probability
		double x;		// x in P(x)
		int INTEGRATION_STEPS = 2048;
		double dcp_dx;
		int i;

		inverseCDF = new double[CDF_SAMPLES];
		dx = (end-start)/INTEGRATION_STEPS;
		x = start + dx/2.0;
		cp = 0.0;
		dcp_dx = 0.0;
		inverseCDF[0] = start;
		inverseCDF[CDF_SAMPLES-1] = end;
		for(i=1; i<(CDF_SAMPLES-1); ++i) {
			targetcp = i/(CDF_SAMPLES-1.0);
			while(cp < targetcp && x < end) {
				dcp_dx = p(x);
				cp += dcp_dx*dx;
				x += dx;
			}
			if(x < end) {
				x += (targetcp - cp)/dcp_dx;
				cp = targetcp;
			} else {
				x = end;
			}
			inverseCDF[i] = x;
		}
	}
	
	/***
	 * Sample from the PDF
	 * @return A random sample from the PDF
	 */
	public double nextDouble() {
		double uniform = Model.rand.nextDouble(); // uniform random sample on [0:1)
		int i = (int)(uniform*(CDF_SAMPLES-1));
		double remainder = uniform*(CDF_SAMPLES-1.0) - i;
		return((1.0-remainder)*inverseCDF[i] + remainder*inverseCDF[i+1]);
	}
	
	DoubleUnaryOperator	pdf;
	public double				start;
	public double				end;
	double []			inverseCDF;			// pre-computed equi-spaced points on the inverse CDF including 0 and 1
	double dx; 								// dx between samples
	static final int	CDF_SAMPLES = 100;	// number of sample	points on the CDF		
}
