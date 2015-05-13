package housing;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

/**
 * This is the root object of the simulation. Upon creation it creates
 * and initialises all the agents in the model.
 * 
 * @author daniel
 *
 **/
@SuppressWarnings("serial")
public class Model extends SimState implements Steppable {

	public Model(long seed) {
		super(seed);
		centralBank = new CentralBank();
		bank = new Bank(centralBank);
		government = new Government();
		construction = new Construction();
		demographics = new Demographics();
		households = new ArrayList<Household>(Demographics.TARGET_POPULATION*2);
		housingMarket = new HouseSaleMarket();
		rentalMarket = new HouseRentalMarket();
		collectors = new Collectors();
		rand = new MersenneTwisterFast(seed);
	}

	/**
	 * This method is called before the simualtion starts. It schedules this
	 * object to be stepped at each timestep and initialises the agents.
	 */
	public void start() {
		super.start();
        scheduleRepeat = schedule.scheduleRepeating(this);
		initialise();
		t = 0;
	}
	
	public void stop() {
		scheduleRepeat.stop();
	}

	/**
	 * This is the main time-step of the whole simulation. Everything starts
	 * here.
	 */
	public void step(SimState simulationStateNow) {
		if (schedule.getTime() >= N_STEPS) simulationStateNow.kill();
		
		demographics.step();
		construction.step();
		for(Household h : households) h.preHouseSaleStep();
		Collectors.housingMarketStats.record();
		housingMarket.clearMarket();
		for(Household h : households) h.preHouseLettingStep();
		housingMarket.clearBuyToLetMarket();
		Collectors.rentalMarketStats.record();
		rentalMarket.clearMarket();
        bank.step();
        t += 1;
	}
	
	/**
	 * Cleans up after a simulation ends.
	 */
	public void finish() {
		super.finish();
	}
	
	
	////////////////////////////////////////////////////////////////////////
	// Initialisation
	////////////////////////////////////////////////////////////////////////
	/**
	 */
	protected void initialise() {
	}

	////////////////////////////////////////////////////////////////////////

	public static int N_STEPS = 50000; // timesteps
	public Stoppable scheduleRepeat;

	public static CentralBank		centralBank;
	public static Bank 				bank;
	public static Government		government;
	public static Construction		construction;
	public static HouseSaleMarket 	housingMarket;
	public static HouseRentalMarket	rentalMarket;
	public static ArrayList<Household>	households;
	public static Demographics		demographics;
	public static MersenneTwisterFast			rand;
	
	public static Collectors		collectors;// = new Collectors();
	
	public static int	t; // time (months)
//	public static LogNormalDistribution grossFinancialWealth;		// household wealth in bank balances and investments


	////////////////////////////////////////////////////////////////////////
	// Getters/setters for MASON console
	////////////////////////////////////////////////////////////////////////
	
	public Collectors getCollectors() {
		return(collectors);
	}
	public String nameCollectors() {return("Diagnostics");}
	
	public static int getN_STEPS() {
		return N_STEPS;
	}

	public static void setN_STEPS(int n_STEPS) {
		N_STEPS = n_STEPS;
	}
	public String nameN_STEPS() {return("Number of timesteps");}

}
