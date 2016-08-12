package optimiser;

import housing.Model;

import java.util.*;

/**
Class Simplex provides a priority queue x containing all the simulation settings, ordered in DECREASING FITNESS.
 The first setting is the best and the last setting is the worst.

 @author rafa
 */
public class Simplex {

    static final double RHO = 0.1;
    static final double XI = 0.1;
    static final double PSI = 0.1;
    static final double SIGMA = 0.1;

    public int N;
    private TreeSet<SimulationSetting> simplex;

    /**
     * First constructor: to initialise a simplex for a new optimisation
     * It makes a convex simplex by taking the range for each parameter, and putting:
     * <p>
     * x[0] at the origin of the ranges
     * x[i] at the maximum of the range for parameter i, and the origin of the range for the rest
     * <p>
     * If there are N parameters, the simplex has N+1 points.
     *
     * @param optimiser the Optimiser class which will be using this simplex
     * @param params    the Parameters that will be the search space
     */
//    Simplex(Optimiser optimiser, Parameters params) {
//        // Construct a new simplex
//        // If n=params.size(), the simplex has N=n+1 points
//        // if a_i is the minimum value for parameter x_i, and
//        // b_i is the maximum value for parameter x_i, then
//        // the point x_i of the simplex is the point:
//        // x_i = (a_1, a_2, a_3, ... b_i, ... a_N+1)
//
//        this.optimiser = optimiser;
//        N = params.N_PARAMS;
//        p = params.p;
//
//        simplex = new TreeSet<>(SimulationSetting.comparator);
//
//        double[] x_1 = new double[N];
//
//        // First simulation setting is (a1, a2, a3, ... aN)
//        for (int j = 0; j < N; j++) {
//            x_1[j] = p.get(j).min;
//        }
//        simplex.add(new SimulationSetting(x_1));
//
//        // Rest of simulation settings as defined in the rule
//        for (int i = 1; i < N + 1; i++) {
//            double[] x_i = new double[N];
//            for (int j = 0; j < N; j++) {
//
//                if (j == i - 1) {
//                    x_i[j] = p.get(j).max;
//                } else {
//                    x_i[j] = p.get(j).min;
//                }
//            }
//
//            simplex.add(new SimulationSetting(x_i));
//        }
//
//        System.out.println("Simplex initialised with size " + simplex.size());
//        assert (simplex.size() == N + 1);
//    }

    Simplex(Optimiser optimiser, Parameters params) {
        // Construct a new simplex
        // If n=params.size(), the simplex has N=n+1 points
        // if a_i is the minimum value for parameter x_i, and
        // b_i is the maximum value for parameter x_i, then
        // the point x_i of the simplex is the point:
        // x_i = (a_1, a_2, a_3, ... b_i, ... a_N+1)

        this.optimiser = optimiser;
        N = params.N_PARAMS;
        p = params.p;

        simplex = new TreeSet<>(SimulationSetting.comparator);

        double[] x_1 = new double[N];

        // First simulation setting the mean of all parameters
        for (int j = 0; j < N; j++) {
            x_1[j] = p.get(j).mean;
        }
        simplex.add(new SimulationSetting(x_1));

        // Rest of simulation settings as defined in the rule
        for (int i = 1; i < N + 1; i++) {
            double[] x_i = new double[N];
            for (int j = 0; j < N; j++) {

                if (j == i - 1) {
                    x_i[j] = 0.5*(p.get(j).max + p.get(j).mean);
                } else {
                    x_i[j] = p.get(j).mean;
                }
            }

            simplex.add(new SimulationSetting(x_i));
        }

        System.out.println("Simplex initialised with size " + simplex.size());
        assert (simplex.size() == N + 1);
    }

    /**
     * Second constructor, to initialise a simplex by giving it an explicit list of coordinates.
     *
     * @param optimiser the Optimiser
     * @param params    the parameters
     * @param locations a list of locations for the vertices of the simplex
     */
    Simplex(Optimiser optimiser, Parameters params, double[][] locations) {
        // Use this constructor to create a simplex with given locations
        this.optimiser = optimiser;
        N = params.N_PARAMS;
        p = params.p;

        assert (locations.length == N + 1);

        simplex = new TreeSet<>(SimulationSetting.comparator);

        for (int i = 0; i < N + 1; i++) {
            simplex.add(new SimulationSetting(locations[i]));
        }
    }


    public double[] getMean() {
        double[] x_mean = new double[N];
        double[] x;

        for (SimulationSetting sim : simplex) {
            x = sim.x;
            for (int i = 0; i < N; i++) {
                x_mean[i] = x_mean[i] + x[i];
            }

        }

        for (int i = 0; i < N; i++) {
            x_mean[i] = 1.0 * x_mean[i] / N;
        }

        return x_mean;
    }


    public double[] reflect() {
        double[] x_R = new double[N];
        double[] x_last = simplex.last().x;
        double[] x_mean = getMean();
        for (int i = 0; i < N; i++) {
            x_R[i] = (1 + RHO) * x_mean[i] - RHO * x_last[i];
        }
        assert (simplex.size() == N + 1);
        return x_R;
    }

    public double[] expand() {
        double[] x_E = new double[N];
        double[] x_last = simplex.last().x;
        double[] x_mean = getMean();
        for (int i = 0; i < N; i++) {
            x_E[i] = (1 + RHO * XI) * x_mean[i] - RHO * XI * x_last[i];
        }
        assert (simplex.size() == N + 1);
        return x_E;

    }

    public double[] outContract() {
        double[] x_O = new double[N];
        double[] x_last = simplex.last().x;
        double[] x_mean = getMean();
        for (int i = 0; i < N; i++) {
            x_O[i] = (1 + PSI * RHO) * x_mean[i] - PSI * RHO * x_last[i];
        }
        assert (simplex.size() == N + 1);
        return x_O;
    }

    public double[] inContract() {
        double[] x_I = new double[N];
        double[] x_last = simplex.last().x;
        double[] x_mean = getMean();
        for (int i = 0; i < N; i++) {
            x_I[i] = (1 - PSI * RHO) * x_mean[i] + PSI * RHO * x_last[i];
        }
        assert (simplex.size() == N + 1);
        return x_I;

    }

    public void shrink() {
        assert (simplex.size() == N + 1);
        System.out.println("Shrink!");

        // Look at the best vertex, towards which the others will shrink, but do NOT remove it
        double[] x_best = simplex.first().x;

        // Create temporary storage for the other settings. Note that their fitness scores will be lost.
        double[][] store = new double[N][N];

        // Now, iterate through all the other vertices, remove them, and store temporarily
        for (int i = 0; i < N; i++) {
            // Remove the worst element and store it
            store[i] = simplex.pollLast().x;
        }

        // Now, the simplex only has one point left: the best vertex
        assert (simplex.size() == 1);

        // Shrink the old vertices one at a time, and add them back into the simplex (without fitness values)
        for (int i = 0; i < N; i++) {
            double[] shrank_coordinates = new double[N];
            for (int j = 0; j < N; j++) {
                shrank_coordinates[j] = x_best[j] - SIGMA * (store[i][j] - x_best[j]);
            }
            simplex.add(new SimulationSetting(shrank_coordinates));
        }

        assert (simplex.size() == N + 1);

    }


    public SimulationSetting getNextSim() {
        if (!simplex.last().evaluated) return simplex.pollLast();
        else {
            System.out.println("Error! The last simulation in the simplex has already been evaluated.");
            return null;
        }
    }

    /**
     * Is the simplex up to date?
     *
     * @return true if all vertices in the simplex have been evaluated
     */
    public boolean upToDate() {
        int evaluated = 0;
        int total = 0;
        for (SimulationSetting sim : simplex) {
            if (sim.evaluated) evaluated += 1;
            total += 1;
        }

        System.out.println("Filling in simplex. Progress: [" + evaluated + "/" + total + "]");

        for (SimulationSetting sim : simplex) {
            if (!sim.evaluated) {
                return false;
            }
        }
        return true;
    }

    public void addToSimplex(SimulationSetting simWithFitness) {
        int currentSize = simplex.size();
        simplex.add(simWithFitness);
        assert (simplex.size() == currentSize + 1);
    }

    public SimulationSetting getBest() {
        return simplex.first();
    }
    public double get_f_1() {
        return simplex.first().fitness;
    }

    public double get_f_n() {
        Iterator<SimulationSetting> descendingIterator = simplex.descendingIterator();
        descendingIterator.next(); // Last element
        return descendingIterator.next().fitness; // Second to last element
    }

    public double get_f_nplus1() {
        return simplex.last().fitness;
    }

    public void replaceWorst(SimulationSetting x_star) {
        simplex.pollLast();
        simplex.add(x_star);
        assert (simplex.size() == N + 1);
    }

    public int getSize() {
        return simplex.size();
    }

    //TODO
//    public double[][] getTranslatedLocations() {
//        // First, decide on a random direction to translate using Marsaglia method (1972)
//        double[] direction = getRandomDirection();
//
//        // Now, get a random distance along which to move
//        double length = Model.rand.nextDouble();
//
//    }

    double[] getRandomDirection() {
        double[] gaussians = new double[N + 1];
        // Get a set of N+1 Gaussian draws
        for (int i = 0; i < N + 1; i++) {
            gaussians[i] = Model.rand.nextGaussian();
        }

        // Get the norm of the vector formed by them
        double norm = 0;
        for (int i = 0; i < N + 1; i++) {
            norm += gaussians[i] * gaussians[i];
        }
        norm = 1.0 / Math.sqrt(norm);

        // The random direction is given by the normalised vector
        double[] direction = new double[N + 1];
        for (int i = 0; i < N + 1; i++) {
            direction[i] = 1.0 * gaussians[i] / norm;
        }
        return direction;
    }






    Optimiser optimiser;
    ArrayList<Parameter> p;


}