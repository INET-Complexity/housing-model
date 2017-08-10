package housing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import collectors.CoreIndicators;
import collectors.CreditSupply;
import collectors.HouseholdStats;
import collectors.HousingMarketStats;
import collectors.MicroDataRecorder;
import collectors.Recorder;
import org.apache.commons.math3.random.RandomGenerator;

import ec.util.MersenneTwisterFast;

/**
 * This is the root object of the simulation. Upon creation it creates
 * and initialises all the agents in the model.
 * 
 * @author daniel
 *
 **/
@SuppressWarnings("serial")
public class Model {

	////////////////////////////////////////////////////////////////////////

	/*
	 * ATTENTION: Seed for random number generation is set by calling the program with argument "-seed <your_seed>",
	 * where <your_seed> must be a positive integer. In the absence of this argument, seed is set from machine time.
	 */

	public static Config config;

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {

	    // Create an instance of Model in order to initialise it (reading config file)
	    new Model(1);

        // Perform config.N_SIMS simulations
		for (nSimulation = 1; nSimulation <= config.N_SIMS; nSimulation += 1) {

		    // For each simulation, initialise both housingMarket and rentalMarket variables (including HPI)
            init();

            // For each simulation, run config.N_STEPS time steps
			for (t = 0; t <= config.N_STEPS; t += 1) {

                /*
		         * Steps model and stores ownership and rental markets bid and offer prices, and their averages, into
		         * their respective variables
		         */
                modelStep();

                // TODO: More efficient to not check every time step but rather divide the external for into 2
                if (t>=config.TIME_TO_START_RECORDING) {
                    // Finds values of variables and records them to their respective files
                    if(config.recordCoreIndicators) recorder.step();
                }

                collectors.step();

                // Print time information to screen
                if (t % 100 == 0) {
                    System.out.println("Simulation: " + nSimulation + ", time: " + t);
                }
            }

			// Finish each simulation within the recorders
            // TODO: Check what this is actually doing and if it is necessary
            if(config.recordCoreIndicators) recorder.endOfSim();
            if(config.recordMicroData) transactionRecorder.endOfSim();
		}

        // After the last simulation, clean up
        if(config.recordCoreIndicators) recorder.finish();
        if(config.recordMicroData) transactionRecorder.finish();

        //Stop the program when finished.
		System.exit(0);
	}

	public Model(long seed) {
        // TODO: Check that random numbers are working properly!
		rand = new MersenneTwister(seed);
		config = new Config("src/main/resources/config.properties");
		//System.exit(0);

		government = new Government();
		demographics = new Demographics();
		recorder = new collectors.Recorder();
		transactionRecorder = new collectors.MicroDataRecorder();

		centralBank = new CentralBank();
		mBank = new Bank();
		mConstruction = new Construction();
		mHouseholds = new ArrayList<Household>(config.TARGET_POPULATION*2);
		housingMarket = mHousingMarket = new HouseSaleMarket();		// Variables of housingMarket are initialised (including HPI)
		rentalMarket = mRentalMarket = new HouseRentalMarket();		// Variables of rentalMarket are initialised (including HPI)
		mCollectors = new collectors.Collectors();
		nSimulation = 0;

		setupStatics();
		init();		// Variables of both housingMarket and rentalMarket are initialised again (including HPI)
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
		setRecordCoreIndicators(config.recordCoreIndicators);
		setRecordMicroData(config.recordMicroData);
	}

	public static void init() {
		construction.init();
		housingMarket.init();
		rentalMarket.init();
		bank.init();
		households.clear();
		collectors.init();
	}


	public static void modelStep() {
		demographics.step();
		construction.step();

		for(Household h : households) h.step();
        // Stores ownership market bid and offer prices, and their averages, into their respective variables
		collectors.housingMarketStats.record();
        // Clears market and updates the HPI
		housingMarket.clearMarket();
        // Stores rental market bid and offer prices, and their averages, into their respective variables
		collectors.rentalMarketStats.record();
		rentalMarket.clearMarket();
		bank.step();
		centralBank.step(getCoreIndicators());
	}


	/**
	 * @return simulated time in months
	 */
	static public int getTime() {
		return(Model.root.t);
	}

	static public int getMonth() {
		return(Model.root.t%12 + 1);
	}

	// non-statics for serialization
	public ArrayList<Household>    	mHouseholds;
	public Bank						mBank;
//	public CentralBank				mCentralBank;
	public Construction				mConstruction;
	public HouseSaleMarket			mHousingMarket;
	public HouseRentalMarket		mRentalMarket;
	public collectors.Collectors mCollectors;

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

	public static collectors.Collectors collectors;	// = new Collectors();
	public static Recorder recorder;	// records info to file
	public static MicroDataRecorder transactionRecorder;

	public static int	nSimulation;	// number of simulations run
	public static int	t;	// time (months)

	/**
	 * proxy class to allow us to work with apache.commons distributions
	 */
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

	public collectors.HousingMarketStats getHousingMarketStats() {
		return collectors.housingMarketStats;
	}

	public HousingMarketStats getRentalMarketStats() {
		return collectors.rentalMarketStats;
	}

	public static CoreIndicators getCoreIndicators() {
		return collectors.coreIndicators;
	}

	public HouseholdStats getHouseholdStats() {
		return collectors.householdStats;
	}

	public void setRecordCoreIndicators(boolean recordCoreIndicators) {
		this.config.recordCoreIndicators = recordCoreIndicators;
		if(recordCoreIndicators) {
			collectors.coreIndicators.setActive(true);
			collectors.creditSupply.setActive(true);
			collectors.householdStats.setActive(true);
			collectors.housingMarketStats.setActive(true);
			collectors.rentalMarketStats.setActive(true);
			try {
				recorder.start();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public String nameRecordCoreIndicators() {return("Record core indicators");}

	public boolean isRecordMicroData() {
		return transactionRecorder.isActive();
	}

	public void setRecordMicroData(boolean record) {
		transactionRecorder.setActive(record);
	}
	public String nameRecordMicroData() {return("Record micro data");}


}
