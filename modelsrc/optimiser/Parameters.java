package optimiser;

import java.util.ArrayList;

public class Parameters {
//    public double P_INVESTOR = 0.04; 		// Prior probability of being (wanting to be) a property investor (should be 4%)
//    public double MIN_INVESTOR_PERCENTILE = 0.5; // minimum income percentile for a HH to be a BTL investor
//    public double FUNDAMENTALIST_CAP_GAIN_COEFF = 0.5;// weight that fundamentalists put on cap gain
//    public double TREND_CAP_GAIN_COEFF = 0.9;			// weight that trend-followers put on cap gain
//    public double P_FUNDAMENTALIST = 0.5; 			// probability that BTL investor is a fundamentalist (otherwise is a trend-follower)

    public Parameter P_INVESTOR =                      new Parameter(0.04,0.01, 0.1); 		// Prior probability of being (wanting to be) a property investor (should be 4%)
    //public Parameter MIN_INVESTOR_PERCENTILE =         new Parameter(0.5,0.3,0.8); // minimum income percentile for a HH to be a BTL investor
    public Parameter FUNDAMENTALIST_CAP_GAIN_COEFF =   new Parameter(0.5,0.1,0.9); // weight that fundamentalists put on cap gain
    public Parameter TREND_CAP_GAIN_COEFF =            new Parameter(0.9,0.1,0.95);			// weight that trend-followers put on cap gain
    public Parameter P_FUNDAMENTALIST =                new Parameter(0.5,0.1,0.8); 			// probability that BTL investor is a fundamentalist (otherwise is a trend-follower)

    public ArrayList<Parameter> p = new ArrayList<>();
    public int N_PARAMS = 0;

    public Parameters() {
        p.add(P_INVESTOR);
       // p.add(MIN_INVESTOR_PERCENTILE);
//        p.add(FUNDAMENTALIST_CAP_GAIN_COEFF);
//        p.add(TREND_CAP_GAIN_COEFF);
        p.add(P_FUNDAMENTALIST);
        N_PARAMS = p.size();
    }

    public void refreshParameters(SimulationSetting sim) {
        double[] newParams = sim.x;
        for (int i=0; i<newParams.length; i++) {
            p.get(i).value=newParams[i];
        }

        // TODO: fix these indices!
        P_INVESTOR=p.get(0);
//        MIN_INVESTOR_PERCENTILE = p.get(1);
//        FUNDAMENTALIST_CAP_GAIN_COEFF = p.get(2);
        //TREND_CAP_GAIN_COEFF = p.get(1);
        P_FUNDAMENTALIST = p.get(1);
    }

}
