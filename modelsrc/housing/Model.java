package housing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import optimiser.Optimiser;
import optimiser.Parameters;
import optimiser.SimulationRecord;
import org.apache.commons.math3.random.RandomGenerator;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import LSTM.Predictor;

/**
 * This is the root object of the simulation. Upon creation it creates
 * and initialises all the agents in the model.
 * 
 * @author daniel
 *
 **/
@SuppressWarnings("serial")
public class Model extends SimState implements Steppable {

	public static int N_STEPS = 400; // timesteps
	public static int N_SIMS = 20; // number of simulations to run (monte-carlo)
	public boolean recordCoreIndicators = false; // set to true to write core indicators to a file
	public boolean recordMicroData = false; // set to true to write micro transaction data to a file
	public static boolean USING_LSTM=true;
	public static boolean USING_SELLING_CDF = false;
	public static boolean STATIC_PICTURE=false;
	public static boolean USING_SIMPLEX=true;
	public static boolean simplexDone = false;

	public Model(long seed) {
		super(seed);
		government = new Government();
		demographics = new Demographics();
		recorder = new Recorder();
		transactionRecorder = new MicroDataRecorder();
		staticPictureRecorder = new StaticPictureRecorder();
		rand = new MersenneTwister(seed);
		centralBank = new CentralBank();
		mBank = new Bank();
		mConstruction = new Construction();
		mHouseholds = new ArrayList<Household>(Demographics.TARGET_POPULATION*2);
		housingMarket = mHousingMarket = new HouseSaleMarket();
		rentalMarket = mRentalMarket = new HouseRentalMarket();
		mCollectors = new Collectors();
		nSimulation = 0;

		if(USING_LSTM) predictorLSTM = new Predictor();
		else predictorLSTM=null;

		if(USING_SIMPLEX) {
			parameters = new Parameters();
			optimiser = new Optimiser(parameters);
		} else {
			parameters=null;
			optimiser=null;
		}

		setupStatics();
		init();
	}
	
	@Override
	public void awakeFromCheckpoint() {
		super.awakeFromCheckpoint();
		setupStatics();
	}
	
	protected void setupStatics() {
//		centralBank = mCentralBank;
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
		if(!monteCarloCheckpoint.equals("")) {//changed this from != ""
			File f = new File(monteCarloCheckpoint);
			readFromCheckpoint(f);
		}
	}

	/**
	 * This method is called before the simualtion starts. It schedules this
	 * object to be stepped at each timestep and initialises the agents.
	 */
	public void start() {
		setRecordCoreIndicators(recordCoreIndicators);
		setRecordMicroData(recordMicroData);
		setStaticRecorder(STATIC_PICTURE);
		super.start();
        scheduleRepeat = schedule.scheduleRepeating(this);

        if(!monteCarloCheckpoint.equals("")) {//changed from != ""
        	File f = new File(monteCarloCheckpoint);
        	readFromCheckpoint(f);
        }
			// recorder.start();
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

			if(recordCoreIndicators) recorder.endOfSim();
			if(recordMicroData) transactionRecorder.endOfSim();

			// Last simulation using simplex, must finish recording properly.
			if(USING_SIMPLEX) {
				if (simplexDone) {
					if (recordCoreIndicators) recorder.finish();
					if (recordMicroData) transactionRecorder.finish();
					simulationStateNow.kill();
					return;
				} else {
					endSimplexSimulation();
				}
			}
			// This was the last simulation, end.
			if (nSimulation >= N_SIMS) {
				// this was the last simulation
				if(recordCoreIndicators) recorder.finish();
				if(recordMicroData) transactionRecorder.finish();
				simulationStateNow.kill();
				return;
			}
			init();
			start();
		}
		modelStep();
		if(recordCoreIndicators) recorder.step();
//		if(this.getTime() % 1200) this.writeToCheckpoint("file");
		collectors.step();
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
		if(recordCoreIndicators) recorder.finish();
		if(recordMicroData) transactionRecorder.finish();
		if(STATIC_PICTURE) staticPictureRecorder.finish();
	}
	
	/*** @return simulated time in months */
	static public int getTime() {
		return(Model.root.t);
	}

	static public int getMonth() {
		return(Model.root.t%12 + 1);
	}

	public void endSimplexSimulation() {
		// Write the core indicators to the SimulationRecord object
		SimulationRecord simRecord = new SimulationRecord(getCoreIndicators());
		// Signal the Optimiser to compute fitness function, update parameter values, and store the results
		optimiser.endOfSim(simRecord);
		if (optimiser.state== Optimiser.OptimiserState.END) {
			simplexDone = true;
			return;
		}
		// Refresh parameters
		parameters.refreshParameters(optimiser.nextSim);
		HouseholdBehaviour.resetParameters();
	}

	////////////////////////////////////////////////////////////////////////

	public Stoppable scheduleRepeat;

	// non-statics for serialization
	public ArrayList<Household>    	mHouseholds;
	public Bank						mBank;
//	public CentralBank				mCentralBank;
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
	public static MicroDataRecorder transactionRecorder;
	public static StaticPictureRecorder staticPictureRecorder;

	public static Predictor			predictorLSTM;

	public static Parameters		parameters;
	public static Optimiser			optimiser;

	public static int	nSimulation; // number of simulations run
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
	public static int getnSimulation() { return nSimulation;}

	public static void setN_SIMS(int n_SIMS) {
		N_SIMS = n_SIMS;
	}
	public String nameN_SIMS() {return("Number of monte-carlo runs");}

	String monteCarloCheckpoint = "";
	
	
	public String getMonteCarloCheckpoint() {
		return monteCarloCheckpoint;
	}

	public void setMonteCarloCheckpoint(String monteCarloCheckpoint) {
		this.monteCarloCheckpoint = monteCarloCheckpoint;
	}

	public boolean isRecordCoreIndicators() {
		return recordCoreIndicators;
	}

	public void setRecordCoreIndicators(boolean recordCoreIndicators) {
		this.recordCoreIndicators = recordCoreIndicators;
		if(recordCoreIndicators) {
			collectors.coreIndicators.setActive(true);
			collectors.creditSupply.setActive(true);
			collectors.householdStats.setActive(true);
			collectors.housingMarketStats.setActive(true);
			collectors.rentalMarketStats.setActive(true);
			try {
				recorder.start();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// complain
				e.printStackTrace();
			}
		//} else {
		//	recorder.finish();
		}
	}
	public String nameRecordCoreIndicators() {return("Record core indicators");}

	public boolean isRecordMicroData() {
		return transactionRecorder.isActive();
	}

	public void setRecordMicroData(boolean record) {
		transactionRecorder.setActive(record);
	}
	public void setStaticRecorder(boolean record) {staticPictureRecorder.setActive(record);}
	public String nameRecordMicroData() {return("Record micro data");}


}
