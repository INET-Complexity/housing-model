package housing;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
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
		recorder = new Recorder();
		rand = new MersenneTwisterFast(seed);
	}

	public void init() {
		construction.init();
		housingMarket.init();
		rentalMarket.init();
		bank.init();
		households.clear();
		t = 0;
	}

	/**
	 * This method is called before the simualtion starts. It schedules this
	 * object to be stepped at each timestep and initialises the agents.
	 */
	public void start() {
		super.start();
        scheduleRepeat = schedule.scheduleRepeating(this);
		nSimulation = 0;
		init();

		try {
			recorder.start();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		scheduleRepeat.stop();
	}

	/**
	 * This is the main time-step of the whole simulation. Everything starts
	 * here.
	 */
	public void step(SimState simulationStateNow) {
		if (schedule.getTime() >= N_STEPS*N_SIMS) simulationStateNow.kill();
		if(t >= N_STEPS) {
			// start new simulation
			nSimulation += 1;
			if (nSimulation >= N_SIMS) {
				// this was the last simulation
				recorder.finish();
				simulationStateNow.kill();
				return;
			}
			init();
		}
		modelStep();
		if(recordCoreIndicators) recorder.step();
	}

	public void modelStep() {
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
        centralBank.step(Collectors.getCoreIndicators());
        t += 1;		
	}
	
	
	/**
	 * Cleans up after a simulation ends.
	 */
	public void finish() {
		super.finish();
	}
	

	////////////////////////////////////////////////////////////////////////

	public static int N_STEPS = 1200; // timesteps
	public static int N_SIMS = 1; // number of simulations to run (monte-carlo) 


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
	public static Recorder			recorder; // records info to file
	public boolean recordCoreIndicators = true;
	
	public static int	nSimulation; // number of simulations run
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

	public static int getN_SIMS() {
		return N_SIMS;
	}

	public static void setN_SIMS(int n_SIMS) {
		N_SIMS = n_SIMS;
	}
	public String nameN_SIMS() {return("Number of monte-carlo runs");}

	public boolean isRecordCoreIndicators() {
		return recordCoreIndicators;
	}

	public void setRecordCoreIndicators(boolean recordCoreIndicators) {
		this.recordCoreIndicators = recordCoreIndicators;
	}
	public String nameRecordCoreIndicators() {return("Record core indicators");}



}
