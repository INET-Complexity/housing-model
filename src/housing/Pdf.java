package housing;

import java.util.function.DoubleUnaryOperator;

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
		double dcp_dx;
		int i;

		inverseCDF = new double[CDF_SAMPLES];
		dx = (end-start)/(CDF_SAMPLES-1);
		x = start;
		cp = 0.0;
		targetcp = 0.0;
		inverseCDF[0] = 0.0;
		inverseCDF[CDF_SAMPLES-1] = 1.0;
		for(i=1; i<(CDF_SAMPLES-1); ++i) {
			targetcp += dx;
			while(cp < targetcp) {
				cp += p(x)*dx;
				x += dx;
			}
			dcp_dx = p(x - dx/2.0);
			x += (targetcp - cp)/dcp_dx;
			inverseCDF[i] = x;
		}
	}
	
	/***
	 * Sample from the PDF
	 * @return A random sample from the PDF
	 */
	public double nextDouble() {
		double uniform = Model.rand.nextDouble(); // uniform random sample on [0:1)
		int i = (int)(uniform/dx);
		double remainder = uniform - i*dx; 
		return((1.0-remainder)*inverseCDF[i] + remainder*inverseCDF[i+1]);
	}
	
	DoubleUnaryOperator	pdf;
	public double				start;
	public double				end;
	double []			inverseCDF;			// pre-computed equi-spaced points on the inverse CDF including 0 and 1
	double dx; 								// dx between samples
	static final int	CDF_SAMPLES = 100;	// number of sample	points on the CDF		
}
