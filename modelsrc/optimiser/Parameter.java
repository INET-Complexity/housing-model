package optimiser;

public class Parameter {
    public double value;
    public double min;
    public double max;
    public double mean;

    public Parameter(double value, double min, double max) {
        this.value=value;
        this.min=min;
        this.max=max;
        this.mean = 0.5*(max+min);
    }


}
