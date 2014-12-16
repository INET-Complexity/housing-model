package housing;

import ec.util.MersenneTwisterFast;

/**
 * Created by drpugh on 12/16/14.
 */
public class ExtrapolativeExpectations extends Expectations {

    private double speedOfAdjustment;

    /** Constructor **/
    public ExtrapolativeExpectations(MersenneTwisterFast rng)  {
        previousExpectation = 0.0;
        speedOfAdjustment = rng.nextDouble();
    }

    @Override
    /**
     * Simple implementation of extrapolative expectations
     * @param currentValue: current value of the variable
     * @param previousValue: the previous value of the variable
     * @return the new expected value
     */
    public double formExpectation(double currentValue) {
        double newExpectation = (speedOfAdjustment * currentValue +
                Math.pow(speedOfAdjustment, 2) * previousValue);
        previousExpectation = newExpectation;  // update previous expectation
        return newExpectation;
    }

}