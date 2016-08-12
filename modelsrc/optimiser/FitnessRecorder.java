package optimiser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class FitnessRecorder {
    public void start() throws FileNotFoundException, UnsupportedEncodingException {
        // --- open files for core indicators
        simulationsRun = new PrintWriter("simulationsRun.csv", "UTF-8");
    }

    public void init() {
        try {
            outfile = new PrintWriter("simulationsRun.csv", "UTF-8");
            //TODO: generate a string from the Parameters class
            outfile.println("X1,X2,Fitness,Best1,Best2,BestFitness");

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("Writing to output failed!!!");
            e.printStackTrace();
        }
    }

    public void write(SimulationSetting simSetting, SimulationSetting best) {
        assert(simSetting.evaluated);
        assert(best.evaluated);
        System.out.println("Writing");

        double[] parameters = simSetting.x;
        for (double x: parameters) {
            outfile.print(x+",");
        }
        outfile.print(simSetting.fitness+",");

        for (double x: best.x) {
            outfile.print(x+",");
        }
        outfile.print(best.fitness);
        outfile.println();
    }

    public void finish() {
        if(outfile != null) outfile.close();
    }



    PrintWriter simulationsRun;
    PrintWriter 	outfile;
}
