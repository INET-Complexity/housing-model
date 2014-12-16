package housing;

import ec.util.MersenneTwisterFast;

/**
 * Created by drpugh on 12/16/14.
 */
public class AdaptiveExpectations extends Expectations {

    private double speedOfAdjustment;

    /** Constructor **/
    public AdaptiveExpectations(MersenneTwisterFast rng)  {
        previousExpectation = 0.0;
        speedOfAdjustment = rng.nextDouble();
    }

    @Override
    /**
     * Simple implementation of adaptive expectations
     * @param currentValue: current value of the variable
     * @return the new expected value
     */
    public double formExpectation(double currentValue) {
        double newExpectation = (currentValue +
                speedOfAdjustment * forecastError(currentValue));
        previousExpectation = newExpectation;  // update previous expectation
        return newExpectation;
    }
}