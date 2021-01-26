package utilities;

import java.util.ArrayList;

/**
 *  Utility class that expands ArrayList with two new fields for binned data: the minimum edge of the first bin and the
 *  width of the bins. It also includes some methods for getting and setting these new fields, as well as a method to
 *  get the frequency/density/probability of the bin within which a certain value falls
 *
 *  @author daniel, Adrian Carro
 */
public class BinnedData<D> extends ArrayList<D> {

    //------------------//
    //----- Fields -----//
    //------------------//

    private double firstBinMin;     // Minimum edge of the first bin
    private double binWidth;        // Bin width

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    public BinnedData(double firstBinMin, double binWidth) {
        this.firstBinMin = firstBinMin;
        this.binWidth = binWidth;
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    public double getSupportLowerBound() { return firstBinMin; }

    public double getSupportUpperBound() { return firstBinMin + size()*binWidth; }

    public double getBinWidth() { return binWidth; }

    /**
     * Returns the frequency/density/probability of the bin at which the value val falls, with security to ensure values
     * below the minimum bin edge return the minimum bin and values above the maximum bin return the maximum bin
     */
    public D getBinAt(double val) {
        if (val < firstBinMin) return get(0);
        else if (val > getSupportUpperBound()) return get(size() - 1);
        else return get((int)((val - firstBinMin)/binWidth));
    }

    public void setBinWidth(double width) { binWidth = width; }

    public void setFirstBinMin(double min) { firstBinMin = min; }
}
