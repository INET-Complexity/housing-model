package housing;

import sim.engine.SimState;

public class ModelNoGUI extends SimState {
    static long seed = 3;
    static Model model;

    public ModelNoGUI(long seed) {
        super(seed);
        model = new Model(seed);
    }


    @Override
    public void start() {
        super.start();
        model.start();
        schedule.scheduleRepeating(model);
    }

    public static void main(String[] args) {
        doLoop(ModelNoGUI.class, args);
        //doLoop is a static method
        //already defined in SimState

        System.exit(0);//Stop the program when finished.
    }
}
