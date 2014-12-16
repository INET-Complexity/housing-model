package housing;

import ec.util.MersenneTwisterFast;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by drpugh on 12/16/14.
 */
abstract class Expectations implements IExpectations {

    public double previousValue;
    public double previousExpectation;

    /**
     * Return the forecast error for a variable
     * @param currentValue: current value of the variable
     * @return the forecast error for the variable
     */
    @Override
    public double forecastError(double currentValue) {
        return currentValue - previousExpectation;
    }

    @Override
    /**
     * Simple implementation of adaptive expectations
     * @param currentValue: current value of the variable
     * @return the new expected value
     */
    public double formExpectation(double currentValue) {
        throw new NotImplementedException();
    }
}