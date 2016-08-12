package optimiser;

import housing.HouseholdBehaviour;
import housing.Model;

public class Optimiser {
    public static final boolean USING_TA=false;
    public OptimiserState state = OptimiserState.INITIALISING;
    public SimulationSetting nextSim = null;
    public SimulationSetting best;
    public static FitnessRecorder recorder = new FitnessRecorder();
    int nSimulations=0;


    public enum OptimiserState {
        INITIALISING, REFLECT, EXPAND, OUT_CONTRACT,
        IN_CONTRACT, SHRINK, END
    }

    public Optimiser(Parameters params) {
        // Construct starting simplex
        simplex = new Simplex(this, params);
        state=OptimiserState.INITIALISING;
        System.out.println("Optimiser initialised!");

        recorder.init();

        // Get parameters for the first simulation!
        nextSim=simplex.getNextSim();
        Model.parameters.refreshParameters(nextSim);
        HouseholdBehaviour.resetParameters();
    }


    public void endOfSim(SimulationRecord sim) {
        // process the result of the simulation that just ended
        nSimulations +=1;
        lastFitness = fitnessFn.getFitness(sim);
        lastSim= new SimulationSetting(nextSim.x,lastFitness);

        System.out.println("Simulation number: "+ Model.getnSimulation()+", state "+state+", fitness: "+lastFitness);

        if(nSimulations>=Model.getN_SIMS()) state = OptimiserState.END;
        switch (state) {

            case INITIALISING:
                // We are still initialising, so we add the simulation we just run to the simplex
                simplex.addToSimplex(lastSim);
                // Record the best so far
                best=simplex.getBest();

                if (!simplex.upToDate()){
                    // If we're not done, we continue initialising
                    nextSim = simplex.getNextSim();
                } else {
                    // Move on to reflect
                    state = OptimiserState.REFLECT;
                    nextSim.setParams(simplex.reflect());
                }
                break;

            case REFLECT:
                reflect=lastSim;
                if (reflect.fitness < simplex.get_f_1()) {
                    // Reflect was optimal! Let's try expanding as well
                    System.out.println("Optimal reflection! Let's expand.");
                    state = OptimiserState.EXPAND;
                    nextSim.setParams(simplex.expand());
                } else {
                    if (reflect.fitness < simplex.get_f_n()) {
                        // Reflect made some minor improvement, let's add it to the simplex and reflect again
                        x_star=reflect;
                        simplex.replaceWorst(x_star);

                        System.out.println("Reflect made decent improvement. Reflect again");
                        assert(state==OptimiserState.REFLECT);
                        nextSim.setParams(simplex.reflect());
                    } else {
                        if (reflect.fitness < simplex.get_f_nplus1()) {
                            System.out.println("Reflect made a minor improvement. Out-contract.");
                            // Reflect was not too bad... let's try to contract out
                            state = OptimiserState.OUT_CONTRACT;
                            nextSim.setParams(simplex.outContract());
                        } else {
                            System.out.println("Reflect was bad. In-contract.");
                            // Reflect was bad. Let's try to contract in
                            state = OptimiserState.IN_CONTRACT;
                            nextSim.setParams(simplex.inContract());
                        }
                    }

                }

                break;

            case EXPAND:
                expand=lastSim;
                if (expand.fitness < reflect.fitness) {
                    x_star=expand;
                    System.out.println("Expand was successful");
                } else {
                    x_star=reflect;
                    System.out.println("Reflect was successful");
                }
                simplex.replaceWorst(x_star);

                // The next simulation will be a reflect
                nextSim.setParams(simplex.reflect());
                state=OptimiserState.REFLECT;
                break;


            case OUT_CONTRACT:
                out_contract = lastSim;

                if (out_contract.fitness < simplex.get_f_nplus1()) {
                    // Out-contract was somewhat OK, let's add it in and start again with a new reflect
                    x_star=out_contract;
                    simplex.replaceWorst(x_star);
                    state=OptimiserState.REFLECT;
                } else {
                    // Everything failed, there were no improvements. Shrink!
                    state=OptimiserState.SHRINK;
                    simplex.shrink();
                    nextSim=simplex.getNextSim();
                }
                break;


            case IN_CONTRACT:
                in_contract = lastSim;

                if (in_contract.fitness < simplex.get_f_nplus1()) {
                    // In-contract was somewhat OK, let's add it in and start again with a new reflect
                    x_star=in_contract;
                    simplex.replaceWorst(x_star);
                    state=OptimiserState.REFLECT;
                    nextSim.setParams(simplex.reflect());
                } else {
                    // Everything failed, there were no improvements. Shrink!
                    state=OptimiserState.SHRINK;
                    simplex.shrink();
                    nextSim=simplex.getNextSim();
                }
                break;

            case SHRINK:
                // We are still updating the simplex after it shrank, so we add the simulation we just run to the simplex
                simplex.addToSimplex(lastSim);
                if (!simplex.upToDate()) {
                    // We are not done updating the new, shrank simplex
                    nextSim = simplex.getNextSim();
                } else {
                    // We are done updating the new simplex. Let's start again with a new reflect
                    state=OptimiserState.REFLECT;
                    nextSim.setParams(simplex.reflect());
                }
                break;

            case END:
                recorder.finish();
                System.out.println("Hallelujah!");
        }

        System.out.println("Best fitness so far:"+best.fitness);
        if (simplex.get_f_1()<best.fitness) {
            best=simplex.getBest();
            System.out.println("Improvement!! new best fitness: "+best.fitness);
        }
        recorder.write(lastSim,best);


    }

    double lastFitness;

    FitnessFunction fitnessFn = new FitnessFunction();

    Simplex simplex;
    SimulationSetting lastSim;
    SimulationSetting reflect;
    SimulationSetting expand;
    SimulationSetting out_contract;
    SimulationSetting in_contract;
    SimulationSetting x_star;
}
