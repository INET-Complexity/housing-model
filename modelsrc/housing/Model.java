package housing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;

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
		households = new ArrayList<Household>(data.Demographics.TARGET_POPULATION*2);
		housingMarket = new HouseSaleMarket();
		rentalMarket = new HouseRentalMarket();
		collectors = new Collectors();
		recorder = new Recorder();
		rand = new MersenneTwister(seed);
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

//		housingMarket.bid(households.get(0), 100.0);
//		housingMarket.offer(new House(), 90.0);
//		housingMarket.clearMarket();
		
		construction.step();

		
		for(Household h : households) h.preSaleClearingStep();
		Collectors.housingMarketStats.record();
		housingMarket.clearMarket();
		for(Household h : households) h.preRentalClearingStep();
//		housingMarket.clearBuyToLetMarket();
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

	public static int N_STEPS = 12000; // timesteps
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
	public static MersenneTwister			rand;
	
	public static Collectors		collectors;// = new Collectors();
	public static Recorder			recorder; // records info to file
	public boolean recordCoreIndicators = false;
	
	public static int	nSimulation; // number of simulations run
	public static int	t; // time (months)
//	public static LogNormalDistribution grossFinancialWealth;		// household wealth in bank balances and investments

	/*** proxy class to allow us to work with apache.commons distributions */
	public static class MersenneTwister extends MersenneTwisterFast implements RandomGenerator {
		public MersenneTwister(long seed) {super(seed);}
		public void setSeed(int arg0) {
			super.setSeed((long)arg0);
		}		
	}

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
