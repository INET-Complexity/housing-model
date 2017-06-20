package utilities;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.rank.Median;

public class MeanAboveMedian implements UnivariateStatistic {

	@Override
	public UnivariateStatistic copy() {
		return new MeanAboveMedian();
	}

	@Override
	public double evaluate(double[] arg0) throws MathIllegalArgumentException {
		double median = (new Median()).evaluate(arg0);
		double totalAboveMedian = 0.0;
		int countAboveMedian = 0;
		for(double val : arg0) {
			if(val > median) {
				totalAboveMedian += val;
				++countAboveMedian;
			}
		}
		return(totalAboveMedian/countAboveMedian);
	}

	@Override
	public double evaluate(double[] data, int begin, int length)
			throws MathIllegalArgumentException {
		double median = (new Median()).evaluate(data,begin,length);
		double totalAboveMedian = 0.0;
		int countAboveMedian = 0;
		for(int i=begin; i<begin+length; ++i) {
			if(data[i] > median) {
				totalAboveMedian += data[i];
				++countAboveMedian;
			}
		}
		return(totalAboveMedian/countAboveMedian);
	}

}
