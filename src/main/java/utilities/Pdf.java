package utilities;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.math3.random.MersenneTwister;

import housing.Model;

// import java.util.function.DoubleUnaryOperator; // not compatible with Java 1.7

/****
 * Represents an arbitrarily shaped, 1-dimensional Probability Density Function.
 * Supply a DoubleUnaryOperator class that returns the probability density for
 * a given value.
 * 
 * @author daniel
 *
 */
public class Pdf implements Serializable {
	private static final long serialVersionUID = 1558047422758004631L;

	/***
	 * Read the pdf from a binned .csv file. The format should be as specified in
	 * BinnedDataDouble.
	 * @param filename
	 * @throws IOException 
	 */
	public Pdf(String filename) {
		try {
			BinnedDataDouble data = new BinnedDataDouble(filename);
			setPdf(data);
		} catch (IOException e) {
			System.out.println("Problem loading data from file while initialising Pdf");
			System.out.println("filename = "+System.getProperty("user.dir")+"/"+filename);
			e.printStackTrace();			
		}
	}

    public Pdf(String filename, int NSamples) {
        try {
            BinnedDataDouble data = new BinnedDataDouble(filename);
            setPdf(data, NSamples);
        } catch (IOException e) {
            System.out.println("Problem loading data from file while initialising Pdf");
            System.out.println("filename = "+System.getProperty("user.dir")+"/"+filename);
            e.printStackTrace();
        }
    }
	
	public Pdf(final BinnedDataDouble data) {
		setPdf(data);
	}
	
	/**
	 * @param ipdf functional class whose apply function returns the probability density at that point
	 * (should be defined on the interval [istart,iend) )
	 * @param istart the value below which probability is assumed to be zero
	 * @param iend   the value above which probability is assumed to be zero
	 */
	public Pdf(double istart, double iend, DoubleUnaryOperator ipdf) {
		this(istart, iend, ipdf, DEFAULT_CDF_SAMPLES);
	}

	/**
	 * @param ipdf functional class whose apply function returns the probability density at that point
	 * @param istart the value below which probability is assumed to be zero
	 * @param iend   the value above which probability is assumed to be zero
	 * @param NSamples the number of samples of the PDF to take in order to build the CDF
	 */
	public Pdf(double istart, double iend, DoubleUnaryOperator ipdf, int NSamples) {
		pdf = ipdf;
		start = istart;
		end = iend;
		nSamples = NSamples;
		initInverseCDF();
	}
	
	public void setPdf(final BinnedDataDouble data) {
		pdf = new DoubleUnaryOperator() {
			public double applyAsDouble(double operand) {
				return data.getBinAt(operand)/data.getBinWidth();
			}};
		start = data.getSupportLowerBound();
		end = data.getSupportUpperBound();
		nSamples = DEFAULT_CDF_SAMPLES;
		initInverseCDF();		
	}

	public void setPdf(final BinnedDataDouble data, int NSamples) {
		pdf = new DoubleUnaryOperator() {
			public double applyAsDouble(double operand) {
				return data.getBinAt(operand)/data.getBinWidth();
			}};
		start = data.getSupportLowerBound();
		end = data.getSupportUpperBound();
		nSamples = NSamples;
		initInverseCDF();
	}
	
	public double getSupportLowerBound() {
		return start;
	}

	public double getSupportUpperBound() {
		return end;
	}

	/***
	 * Get probability density P(x)
	 * @param x 
	 * @return P(x)
	 */
	public double density(double x) {
		if(x<start || x>=end) return(0.0);
		return(pdf.applyAsDouble(x));
	}
	
	public double inverseCumulativeProbability(double p) {
		if(p < 0.0 || p>=1.0) throw(new IllegalArgumentException("p must be in the interval [0,1)"));
		int i = (int)(p*(nSamples-1));
		double remainder = p*(nSamples-1) - i;
		return((1.0-remainder)*inverseCDF[i] + remainder*inverseCDF[i+1]);
	}
	
	
	/***
	 * integrates "pdf" over "INTEGRATION_STEPS" steps, starting at
	 * start + dx/2 and going up to end - dx/2, recording the values
	 * of x at which the cumulative probability hits quantiles.
	 */
	private void initInverseCDF() {
		double cp;		// cumulative proability
		double targetcp;// target cumulative probability
		double x;		// x in P(x)
		int INTEGRATION_STEPS = 2048;
		double dcp_dx;
		int i;

		inverseCDF = new double[nSamples];
		dx = (end-start)/INTEGRATION_STEPS;
		x = start + dx/2.0;
		cp = 0.0;
		dcp_dx = 0.0;
		inverseCDF[0] = start;
		inverseCDF[nSamples-1] = end;
		for(i=1; i<(nSamples-1); ++i) {
			targetcp = i/(nSamples-1.0);
			while(cp < targetcp && x < end) {
				dcp_dx = density(x);
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
		return(inverseCumulativeProbability(rand.nextDouble()));
//		double uniform = rand.nextDouble(); // uniform random sample on [0:1)
//		int i = (int)(uniform*(nSamples-1));
//		double remainder = uniform*(nSamples-1.0) - i;
//		return((1.0-remainder)*inverseCDF[i] + remainder*inverseCDF[i+1]);
	}

	private MersenneTwister	rand = Model.rand;	// Passes the Model's random number generator to a private field
	DoubleUnaryOperator				pdf;				// function that gives the pdf
	public double					start;				// lowest value of x that has a non-zero probability
	public double					end;				// highest value of x that has a non-zero probability
	double []						inverseCDF;			// pre-computed equi-spaced points on the inverse CDF including 0 and 1
	double 							dx;					// dx between samples
	int								nSamples;			// number of sample	points on the CDF
	static final int				DEFAULT_CDF_SAMPLES = 100;
}
