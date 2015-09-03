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
		government = new Government();
		demographics = new Demographics();
		recorder = new Recorder();
		rand = new MersenneTwister(seed);

		mCentralBank = new CentralBank();
		mBank = new Bank();
		mConstruction = new Construction();
		mHouseholds = new ArrayList<Household>(Demographics.TARGET_POPULATION*2);
		housingMarket = mHousingMarket = new HouseSaleMarket();
		rentalMarket = mRentalMarket = new HouseRentalMarket();
		mCollectors = new Collectors();
		nSimulation = 0;

		setupStatics();
		init();
	}
	
	@Override
	public void awakeFromCheckpoint() {
		super.awakeFromCheckpoint();
		setupStatics();
	}
	
	protected void setupStatics() {
		centralBank = mCentralBank;
		bank = mBank;
		construction = mConstruction;
		households = mHouseholds;
		housingMarket = mHousingMarket;
		rentalMarket = mRentalMarket;
		collectors = mCollectors;
		root = this;
	}

	
	public void init() {
		construction.init();
		housingMarket.init();
		rentalMarket.init();
		bank.init();
		households.clear();
		collectors.init();
		t = 0;
	}

	/**
	 * This method is called before the simualtion starts. It schedules this
	 * object to be stepped at each timestep and initialises the agents.
	 */
	public void start() {
		super.start();
        scheduleRepeat = schedule.scheduleRepeating(this);

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
		
		for(Household h : households) h.step();
		collectors.housingMarketStats.record();
		housingMarket.clearMarket();
		collectors.rentalMarketStats.record();
		rentalMarket.clearMarket();
        bank.step();
        centralBank.step(getCoreIndicators());
        t += 1;        
	}
	
	
	/**
	 * Cleans up after a simulation ends.
	 */
	public void finish() {
		super.finish();
	}
	
	/*** @return simulated time in months */
	static public int getTime() {
		return(Model.root.t);
	}

	static public int getMonth() {
		return(Model.root.t%12 + 1);
	}

	////////////////////////////////////////////////////////////////////////

	public static int N_STEPS = 12000; // timesteps
	public static int N_SIMS = 1; // number of simulations to run (monte-carlo) 


	public Stoppable scheduleRepeat;

	// non-statics for serialization
	public ArrayList<Household>    	mHouseholds;
	public Bank						mBank;
	public CentralBank				mCentralBank;
	public Construction				mConstruction;
	public HouseSaleMarket			mHousingMarket;
	public HouseRentalMarket		mRentalMarket;
	public Collectors				mCollectors;
	
	public static CentralBank		centralBank;
	public static Bank 				bank;
	public static Government		government;
	public static Construction		construction;
	public static HouseSaleMarket 	housingMarket;
	public static HouseRentalMarket	rentalMarket;
	public static ArrayList<Household>	households;
	public static Demographics		demographics;
	public static MersenneTwister	rand;
	public static Model				root;
	
	public static Collectors		collectors;// = new Collectors();
	public static Recorder			recorder; // records info to file
	public boolean recordCoreIndicators = false;
	
	public int	nSimulation; // number of simulations run
	public int	t; // time (months)
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
	
	public CreditSupply getCreditSupply() {
		return collectors.creditSupply;
	}

	public HousingMarketStats getHousingMarketStats() {
		return collectors.housingMarketStats;
	}

	public HousingMarketStats getRentalMarketStats() {
		return collectors.rentalMarketStats;
	}

	public CoreIndicators getCoreIndicators() {
		return collectors.coreIndicators;
	}

	public HouseholdStats getHouseholdStats() {
		return collectors.householdStats;
	}	
	
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
