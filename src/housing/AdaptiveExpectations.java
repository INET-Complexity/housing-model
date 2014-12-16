package housing;

/**
 * Created by drpugh on 12/16/14.
 */
public class AdaptiveExpectations implements IExpectations {

    double SPEED_OF_ADJUSTMENT = 0.5;

    /**
     * Return the forecast error for a variable
     * @param currentValue: current value of the variable
     * @param previousExpectation: previous expected value of the variable
     * @return the forecast error for the variable
     */
    @Override
    public double forecastError(double currentValue,
                                double previousExpectation) {
        return currentValue - previousExpectation;
    }

    @Override
    /**
     * Simple implementation of adaptive expectations
     * @param currentValue: current value of the variable
     * @param previousExpectation: the previous expected value of the variable
     * @return the new expected value
     */
    public double formExpectation(double... args) {
        double currentValue = args[0];
        double previousExpectation = args[1];

        double newExpectation = (previousExpectation +
                SPEED_OF_ADJUSTMENT * forecastError(currentValue, previousExpectation));
        return newExpectation;
    }
}
