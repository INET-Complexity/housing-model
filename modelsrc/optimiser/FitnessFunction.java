package optimiser;

public class FitnessFunction {

    public double getFitness(SimulationRecord simulationRecord) {
        double fitness = 1.0;
        double singleFitness;
        for  (Indicator indicator : simulationRecord.averageCoreIndicators) {
            singleFitness = (1.0 - normalisedDeviation(indicator.value,indicator.reference,indicator.deviation));
//            System.out.println("value of core indicator of type "+indicator.type+" is "+indicator.value);
//            System.out.println(indicator.type+" has fitness: "+singleFitness);
            fitness = fitness * singleFitness;

        }
        System.out.println();
//        System.out.println("Overall fitness score: "+fitness);
        return (1.0 - fitness);
    }

    public double normalisedDeviation(double value, double reference, double deviation) {
        if (Double.isNaN(value)) return 0.0; //TODO: deal with this!
        double difference = Math.abs(value - reference);
        if (difference>deviation) return 0.0;
//        double result = 1.0*difference/deviation;
        return (1.0*difference/deviation);
//            System.out.println("Nan!! with value "+value+" reference "+reference+" deviation "+deviation);
//        return result;
    }
}
