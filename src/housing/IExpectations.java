package housing;

/**
 * Created by drpugh on 12/16/14.
 */
public interface IExpectations {

    /** Computes the forecast error for a variable. **/
    public double forecastError(double currentValue, double previousExpectation);

    /** Computes the forecast error for a variable. **/
    public double formExpectation(double... args);

}
