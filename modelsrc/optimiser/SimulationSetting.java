package optimiser;

import java.util.Comparator;

class SimulationSetting {

    static Comparator<SimulationSetting> comparator = new Comparator<SimulationSetting>() {
        @Override
        public int compare(SimulationSetting o1, SimulationSetting o2) {
//            System.out.println("calling compare");
            // The tree set will order the elements from smallest to largest
            // Therefore, we want:
            // low error < high error < unevaluated

//            if (o1.equals(o2)) return 0;

            if (!o1.evaluated) return 1; // If I'm not evaluated, put me last
            if (!o2.evaluated) return -1; // If he's not evaluated, put him last

            if (o1.fitness > o2.fitness) return 1; // If I'm higher error, I must go last
            else return -1; // Otherwise, I'm fittest so I go first
        }
    };

    double[] x;
    double fitness;
    boolean evaluated;

    SimulationSetting(double[] x) {
        this.x=x;
        this.fitness=0;
        this.evaluated=false;
    }

    SimulationSetting(double[] x, double fitness) {
        this.x=x;
        this.fitness=fitness;
        this.evaluated=true;
    }

    public void setFitness(double fitness) {
        this.fitness=fitness;
        evaluated=true;
    }

    public void setParams(double[] x) {
        this.x=x;
    }

    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        SimulationSetting that = (SimulationSetting) o;
//        if (Double.compare(that.fitness, fitness) != 0) return false;
//        if (evaluated != that.evaluated) return false;
//        if (x.length != that.x.length) return false;
//        for (int i=0; i<x.length; i++) {
//            if(Double.compare(x[i],that.x[i]) != 0) return false;
//        }
//        return true;
        return false;

    }

//    @Override
//    public int hashCode() {
//        System.out.println("Calling hashcode");
//        int result;
//        long temp;
//        result = Arrays.hashCode(x);
//        temp = Double.doubleToLongBits(fitness);
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        result = 31 * result + (evaluated ? 1 : 0);
//        return result;
//    }
}
