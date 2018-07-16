package utilities;

import java.util.ArrayList;

public class BinnedData<DATA> extends ArrayList<DATA> {
	
	public BinnedData(double firstBinMin, double binWidth) {
		this.firstBinMin = firstBinMin;
		this.binWidth = binWidth;
	}
	
	public double getSupportLowerBound() {
		return(firstBinMin);
	}

	public double getSupportUpperBound() {
		return(firstBinMin + size()*binWidth);
	}

	public double getBinWidth() {
		return(binWidth);
	}


	public DATA getBinAt(double val) {
		return(get((int)((val-firstBinMin)/binWidth)));
	}
	
	public void setBinWidth(double width) {
		binWidth = width;
	}
	
	public void setFirstBinMin(double min) {
		firstBinMin = min;
	}
	
	public double firstBinMin;
	public double binWidth;
}
