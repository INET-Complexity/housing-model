package housing;

/**
 * Created by drpugh on 12/16/14.
 */
public class ExtrapolativeExpectations implements IExpectations {

    double SPEED_OF_ADJUSTMENT = 0.95;

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
     * Simple implementation of extrapolative expectations
     * @param currentValue: current value of the variable
     * @param previousValue: the previous value of the variable
     * @return the new expected value
     */
    public double formExpectation(double... args) {
        double currentValue = args[0];
        double previousValue = args[1];

        double newExpectation = (SPEED_OF_ADJUSTMENT * currentValue +
                (1 - SPEED_OF_ADJUSTMENT) * previousValue);
        return newExpectation;
    }

}
