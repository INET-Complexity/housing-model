package housing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.Instant;

import collectors.*;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

/**************************************************************************************************
 * This is the root object of the simulation. Upon creation it creates and initialises all the
 * agents in the model.
 *
 * The project is prepared to be run with maven, and it takes the following command line input
 * arguments:
 *
 * -configFile <arg>    Configuration file to be used (address within project folder). By default,
 *                      'src/main/resources/config.properties' is used.
 * -outputFolder <arg>  Folder in which to collect all results (address within project folder). By
 *                      default, 'Results/<current date and time>/' is used. The folder will be
 *                      created if it does not exist.
 * -dev                 Removes security question before erasing the content inside output folder
 *                      (if the folder already exists).
 * -help                Print input arguments usage information.
 *
 * NEW AD-HOC PARAMETER ENTRIES
 *
 * -MARKET_AVERAGE_PRICE_DECAY <arg> (double) [default value 0.25]
 * -SALE_EPSILON <arg> (double) [default value 0.05]
 * -TARGET_POPULATION <arg> (int) [default value 10000]
 * -P_INVESTOR <arg> (double) [default value 0.16]
 * -MIN_INVESTOR_PERCENTILE <arg> (double) [default value 0.5]
 *
 * Note that the seed for random number generation is set from the config file.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/

@SuppressWarnings("serial")

public class Model {

    //------------------//
    //----- Fields -----//
    //------------------//

    // Ad-hoc variables for command line entry of parameters
    public static String MARKET_AVERAGE_PRICE_DECAY;
    public static String SALE_EPSILON;
    public static String TARGET_POPULATION;
    public static String P_INVESTOR;
    public static String MIN_INVESTOR_PERCENTILE;
    public static String SEED;
    public static String N_STEPS;
    public static String HPA_EXPECTATION_FACTOR;
    public static String HPA_YEARS_TO_CHECK;
    public static String derivedParams_G;
    public static String derivedParams_K;
    public static String derivedParams_KL;
    public static String TENANCY_LENGTH_AVERAGE;
    public static String HOLD_PERIOD;
    public static String DECISION_TO_SELL_ALPHA;
    public static String DECISION_TO_SELL_BETA;
    public static String DECISION_TO_SELL_HPC;
    public static String DECISION_TO_SELL_INTEREST;
    public static String BTL_CHOICE_INTENSITY;
    public static String DESIRED_RENT_INCOME_FRACTION;
    public static String PSYCHOLOGICAL_COST_OF_RENTING;
    public static String SENSITIVITY_RENT_OR_PURCHASE;

    public static Config                config;
    public static Construction		    construction;
    public static CentralBank		    centralBank;
    public static Bank 				    bank;
    public static HouseSaleMarket       houseSaleMarket;
    public static HouseRentalMarket     houseRentalMarket;
    public static ArrayList<Household>  households;
    public static CreditSupply          creditSupply;
    public static CoreIndicators        coreIndicators;
    public static HouseholdStats        householdStats;
    public static HousingMarketStats    housingMarketStats;
    public static RentalMarketStats     rentalMarketStats;
    public static MicroDataRecorder     transactionRecorder;
    public static int	                nSimulation; // To keep track of the simulation number
    public static int	                t; // To keep track of time (in months)

    static Government		            government;

    private static MersenneTwister prng;
    private static Demographics		    demographics;
    private static Recorder             recorder;
    private static String               configFileName;
    private static String               outputFolder;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /**
     * @param configFileName String with the address of the configuration file
     * @param outputFolder String with the address of the folder for storing results
     */
    public Model(String configFileName, String outputFolder) {
        // TODO: Check that random numbers are working properly!
        config = new Config(configFileName);
        prng = new MersenneTwister(config.SEED);

        government = new Government();
        demographics = new Demographics(prng);
        construction = new Construction(prng);
        centralBank = new CentralBank();
        bank = new Bank();
        households = new ArrayList<>(config.TARGET_POPULATION*2);
        houseSaleMarket = new HouseSaleMarket(prng);
        houseRentalMarket = new HouseRentalMarket(prng);

        recorder = new collectors.Recorder(outputFolder);
        transactionRecorder = new collectors.MicroDataRecorder(outputFolder);
        creditSupply = new collectors.CreditSupply(outputFolder);
        coreIndicators = new collectors.CoreIndicators();
        householdStats = new collectors.HouseholdStats();
        housingMarketStats = new collectors.HousingMarketStats(houseSaleMarket);
        rentalMarketStats = new collectors.RentalMarketStats(housingMarketStats, houseRentalMarket);

        nSimulation = 0;
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

	public static void main(String[] args) {

	    // Handle input arguments from command line
        handleInputArguments(args);

        // Create an instance of Model in order to initialise it (reading config file)
        new Model(configFileName, outputFolder);

        // Start data recorders for output
        setupStatics();

        // Open files for writing multiple runs results
        recorder.openMultiRunFiles(config.recordCoreIndicators);

        // Perform config.N_SIMS simulations
		for (nSimulation = 1; nSimulation <= config.N_SIMS; nSimulation += 1) {

            // For each simulation, open files for writing single-run results
            recorder.openSingleRunFiles(nSimulation);

		    // For each simulation, initialise both houseSaleMarket and houseRentalMarket variables (including HPI)
            init();

            // For each simulation, run config.N_STEPS time steps
			for (t = 0; t <= config.N_STEPS; t += 1) {

                // Steps model and stores sale and rental markets bid and offer prices, and their averages, into their
                // respective variables
                modelStep();

//                if (t >= config.TIME_TO_START_RECORDING) {
                    // Write results of this time step and run to both multi- and single-run files
                    recorder.writeTimeStampResults(config.recordCoreIndicators, t);
//                }

                // Print time information to screen
                if (t % 100 == 0) {

                    System.out.println("MARKET_AVERAGE_PRICE_DECAY " + MARKET_AVERAGE_PRICE_DECAY);
                    System.out.println("SALE_EPSILON " + SALE_EPSILON);
                    System.out.println("TARGET_POPULATION " + TARGET_POPULATION);
                    System.out.println("P_INVESTOR " + P_INVESTOR);
                    System.out.println("MIN_INVESTOR_PERCENTILE " + MIN_INVESTOR_PERCENTILE);
                    System.out.println("SEED " + SEED);
                    System.out.println("N_STEPS " + N_STEPS);
                    System.out.println("HPA_EXPECTATION_FACTOR " + HPA_EXPECTATION_FACTOR);
                    System.out.println("HPA_YEARS_TO_CHECK " + HPA_YEARS_TO_CHECK);
                    System.out.println("derivedParams_G " + derivedParams_G);
                    System.out.println("derivedParams_K " + derivedParams_K);
                    System.out.println("derivedParams_KL " + derivedParams_KL);
                    System.out.println("TENANCY_LENGTH_AVERAGE " + TENANCY_LENGTH_AVERAGE);
                    System.out.println("HOLD_PERIOD " + HOLD_PERIOD);
                    System.out.println("DECISION_TO_SELL_ALPHA " + DECISION_TO_SELL_ALPHA);
                    System.out.println("DECISION_TO_SELL_BETA " + DECISION_TO_SELL_BETA);
                    System.out.println("DECISION_TO_SELL_HPC " + DECISION_TO_SELL_HPC);
                    System.out.println("DECISION_TO_SELL_INTEREST " + DECISION_TO_SELL_INTEREST);
                    System.out.println("BTL_CHOICE_INTENSITY " + BTL_CHOICE_INTENSITY);
                    System.out.println("DESIRED_RENT_INCOME_FRACTION " + DESIRED_RENT_INCOME_FRACTION);
                    System.out.println("PSYCHOLOGICAL_COST_OF_RENTING " + PSYCHOLOGICAL_COST_OF_RENTING);
                    System.out.println("SENSITIVITY_RENT_OR_PURCHASE " + SENSITIVITY_RENT_OR_PURCHASE);

                    System.exit(0);

                    System.out.println("Simulation: " + nSimulation + ", time: " + t);
                }
            }

			// Finish each simulation within the recorders (closing single-run files, changing line in multi-run files)
            recorder.finishRun(config.recordCoreIndicators);
            // TODO: Check what this is actually doing and if it is necessary
            if(config.recordMicroData) transactionRecorder.endOfSim();
		}

        // After the last simulation, clean up
        recorder.finish(config.recordCoreIndicators);
        if(config.recordMicroData) transactionRecorder.finish();

        //Stop the program when finished
		System.exit(0);
	}

	private static void setupStatics() {
        setRecordGeneral();
		setRecordCoreIndicators(config.recordCoreIndicators);
		setRecordMicroData(config.recordMicroData);
	}

	private static void init() {
		construction.init();
		houseSaleMarket.init();
		houseRentalMarket.init();
		bank.init();
		centralBank.init();
        housingMarketStats.init();
        rentalMarketStats.init();
        householdStats.init();
        households.clear();
	}

	private static void modelStep() {
        // Update population with births and deaths
        demographics.step();
        // Update number of houses
        construction.step();
        // Updates regional households consumption, housing decisions, and corresponding regional bids and offers
		for(Household h : households) h.step();
        // Stores sale market bid and offer prices and averages before bids are matched by clearing the market
        housingMarketStats.preClearingRecord();
        // Clears sale market and updates the HPI
        houseSaleMarket.clearMarket();
        // Computes and stores several housing market statistics after bids are matched by clearing the market (such as HPI, HPA)
        housingMarketStats.postClearingRecord();
        // Stores rental market bid and offer prices and averages before bids are matched by clearing the market
        rentalMarketStats.preClearingRecord();
        // Clears rental market
        houseRentalMarket.clearMarket();
        // Computes and stores several rental market statistics after bids are matched by clearing the market (such as HPI, HPA)
        rentalMarketStats.postClearingRecord();
        // Stores household statistics after both regional markets have been cleared
        householdStats.record();
        // Update credit supply statistics // TODO: Check what this actually does and if it should go elsewhere!
        creditSupply.step();
		// Update bank and interest rate for new mortgages
		bank.step(Model.households.size());
        // Update central bank policies (currently empty!)
		centralBank.step(coreIndicators);
	}

    /**
     * This method handles command line input arguments to
     * determine the address of the input config file and
     * the folder for outputs
     *
     * @param args String with the command line arguments
     */
	private static void handleInputArguments(String[] args) {

        // Create Options object
        Options options = new Options();

        // Add configFile and outputFolder options
        options.addOption("configFile", true, "Configuration file to be used (address within " +
                "project folder). By default, 'src/main/resources/config.properties' is used.");
        options.addOption("outputFolder", true, "Folder in which to collect all results " +
                "(address within project folder). By default, 'Results/<current date and time>/' is used. The " +
                "folder will be created if it does not exist.");
        options.addOption("dev", false, "Removes security question before erasing the content" +
                "inside output folder (if the folder already exists).");
        options.addOption("help", false, "Print input arguments usage information.");

        // Ad-hoc options for parameter value entry on command line
        options.addOption("MARKET_AVERAGE_PRICE_DECAY", true, "MARKET_AVERAGE_PRICE_DECAY," +
                "default value is 0.25");
        options.addOption("SALE_EPSILON", true, "SALE_EPSILON, default value is 0.05");
        options.addOption("TARGET_POPULATION", true, "TARGET_POPULATION, default value is 10000");
        options.addOption("P_INVESTOR", true, "P_INVESTOR, default value is 0.16");
        options.addOption("MIN_INVESTOR_PERCENTILE", true, "MIN_INVESTOR_PERCENTILE, default" +
                "value is 0.5");
        options.addOption("SEED", true, "SEED, default value is 1");
        options.addOption("N_STEPS", true, "N_STEPS, default value is 3000");
        options.addOption("HPA_EXPECTATION_FACTOR", true, "HPA_EXPECTATION_FACTOR, default value" +
                " is 0.5");
        options.addOption("HPA_YEARS_TO_CHECK", true, "HPA_YEARS_TO_CHECK, default value is 1");
        options.addOption("derivedParams_G", true, "derivedParams_G, default value is 0.24");
        options.addOption("derivedParams_K", true, "derivedParams_K, default value is 0.9802");
        options.addOption("derivedParams_KL", true, "derivedParams_KL, default value is 0.9999");
        options.addOption("TENANCY_LENGTH_AVERAGE", true, "TENANCY_LENGTH_AVERAGE, default value" +
                " is 18");
        options.addOption("HOLD_PERIOD", true, "HOLD_PERIOD, default value is 11.0");
        options.addOption("DECISION_TO_SELL_ALPHA", true, "DECISION_TO_SELL_ALPHA, default value" +
                " is 4.0");
        options.addOption("DECISION_TO_SELL_BETA", true, "DECISION_TO_SELL_BETA, default value" +
                " is 5.0");
        options.addOption("DECISION_TO_SELL_HPC", true, "DECISION_TO_SELL_HPC, default value" +
                " is 0.05");
        options.addOption("DECISION_TO_SELL_INTEREST", true, "DECISION_TO_SELL_INTEREST, default" +
                " value is 0.03");
        options.addOption("BTL_CHOICE_INTENSITY", true, "BTL_CHOICE_INTENSITY, default" +
                " value is 50.0");
        options.addOption("DESIRED_RENT_INCOME_FRACTION", true, "DESIRED_RENT_INCOME_FRACTION," +
                " default value is 0.33");
        options.addOption("PSYCHOLOGICAL_COST_OF_RENTING", true, "PSYCHOLOGICAL_COST_OF_RENTING," +
                " default value is 0.0916666666667");
        options.addOption("SENSITIVITY_RENT_OR_PURCHASE", true, "SENSITIVITY_RENT_OR_PURCHASE," +
                " default value is 0.000285714285714");

        // Create help formatter in case it will be needed
        HelpFormatter formatter = new HelpFormatter();

        // Parse command line arguments and perform appropriate actions
        // Create a parser and a boolean variable for later control
        CommandLineParser parser = new DefaultParser();
        boolean devBoolean = false;
        try {
            // Parse command line arguments into a CommandLine instance
            CommandLine cmd = parser.parse(options, args);
            // Check if help argument has been passed
            if(cmd.hasOption("help")) {
                // If it has, then print formatted help to screen and stop program
                formatter.printHelp( "spatial-housing-model", options );
                System.exit(0);
            }
            // Check if dev argument has been passed
            if(cmd.hasOption("dev")) {
                // If it has, then activate boolean variable for later control
                devBoolean = true;
            }
            // Check if configFile argument has been passed
            if(cmd.hasOption("configFile")) {
                // If it has, then use its value to initialise the respective member variable
                configFileName = cmd.getOptionValue("configFile");
            } else {
                // If not, use the default value to initialise the respective member variable
                configFileName = "src/main/resources/config.properties";
            }
            // Check if outputFolder argument has been passed
            if(cmd.hasOption("outputFolder")) {
                // If it has, then use its value to initialise the respective member variable
                outputFolder = cmd.getOptionValue("outputFolder");
                // If outputFolder does not end with "/", add it
                if (!outputFolder.endsWith("/")) { outputFolder += "/"; }
            } else {
                // If not, use the default value to initialise the respective member variable
                outputFolder = "Results/" + Instant.now().toString().replace(":", "-") + "/";
            }
            // Ad-hoc parameter
            if(cmd.hasOption("MARKET_AVERAGE_PRICE_DECAY")) {
                // If it has, then store its value in a variable
                MARKET_AVERAGE_PRICE_DECAY = cmd.getOptionValue("MARKET_AVERAGE_PRICE_DECAY");
            } else {
                // If not, store the default value in a variable
                MARKET_AVERAGE_PRICE_DECAY = "0.25";
            }
            if(cmd.hasOption("SALE_EPSILON")) {
                // If it has, then store its value in a variable
                SALE_EPSILON = cmd.getOptionValue("SALE_EPSILON");
            } else {
                // If not, store the default value in a variable
                SALE_EPSILON = "0.05";
            }
            if(cmd.hasOption("TARGET_POPULATION")) {
                // If it has, then store its value in a variable
                TARGET_POPULATION = cmd.getOptionValue("TARGET_POPULATION");
            } else {
                // If not, store the default value in a variable
                TARGET_POPULATION = "10000";
            }
            if(cmd.hasOption("P_INVESTOR")) {
                // If it has, then store its value in a variable
                P_INVESTOR = cmd.getOptionValue("P_INVESTOR");
            } else {
                // If not, store the default value in a variable
                P_INVESTOR = "0.16";
            }
            if(cmd.hasOption("MIN_INVESTOR_PERCENTILE")) {
                // If it has, then store its value in a variable
                MIN_INVESTOR_PERCENTILE = cmd.getOptionValue("MIN_INVESTOR_PERCENTILE");
            } else {
                // If not, store the default value in a variable
                MIN_INVESTOR_PERCENTILE = "0.5";
            }
            if(cmd.hasOption("SEED")) {
                // If it has, then store its value in a variable
                SEED = cmd.getOptionValue("SEED");
            } else {
                // If not, store the default value in a variable
                SEED = "1";
            }
            if(cmd.hasOption("N_STEPS")) {
                // If it has, then store its value in a variable
                N_STEPS = cmd.getOptionValue("N_STEPS");
            } else {
                // If not, store the default value in a variable
                N_STEPS = "3000";
            }
            if(cmd.hasOption("HPA_EXPECTATION_FACTOR")) {
                // If it has, then store its value in a variable
                HPA_EXPECTATION_FACTOR = cmd.getOptionValue("HPA_EXPECTATION_FACTOR");
            } else {
                // If not, store the default value in a variable
                HPA_EXPECTATION_FACTOR = "0.5";
            }
            if(cmd.hasOption("HPA_YEARS_TO_CHECK")) {
                // If it has, then store its value in a variable
                HPA_YEARS_TO_CHECK = cmd.getOptionValue("HPA_YEARS_TO_CHECK");
            } else {
                // If not, store the default value in a variable
                HPA_YEARS_TO_CHECK = "1";
            }
            if(cmd.hasOption("derivedParams_G")) {
                // If it has, then store its value in a variable
                derivedParams_G = cmd.getOptionValue("derivedParams_G");
            } else {
                // If not, store the default value in a variable
                derivedParams_G = "0.24";
            }
            if(cmd.hasOption("derivedParams_K")) {
                // If it has, then store its value in a variable
                derivedParams_K = cmd.getOptionValue("derivedParams_K");
            } else {
                // If not, store the default value in a variable
                derivedParams_K = "0.9802";
            }
            if(cmd.hasOption("derivedParams_KL")) {
                // If it has, then store its value in a variable
                derivedParams_KL = cmd.getOptionValue("derivedParams_KL");
            } else {
                // If not, store the default value in a variable
                derivedParams_KL = "0.9999";
            }
            if(cmd.hasOption("TENANCY_LENGTH_AVERAGE")) {
                // If it has, then store its value in a variable
                TENANCY_LENGTH_AVERAGE = cmd.getOptionValue("TENANCY_LENGTH_AVERAGE");
            } else {
                // If not, store the default value in a variable
                TENANCY_LENGTH_AVERAGE = "18";
            }
            if(cmd.hasOption("HOLD_PERIOD")) {
                // If it has, then store its value in a variable
                HOLD_PERIOD = cmd.getOptionValue("HOLD_PERIOD");
            } else {
                // If not, store the default value in a variable
                HOLD_PERIOD = "11.0";
            }
            if(cmd.hasOption("DECISION_TO_SELL_ALPHA")) {
                // If it has, then store its value in a variable
                DECISION_TO_SELL_ALPHA = cmd.getOptionValue("DECISION_TO_SELL_ALPHA");
            } else {
                // If not, store the default value in a variable
                DECISION_TO_SELL_ALPHA = "4.0";
            }
            if(cmd.hasOption("DECISION_TO_SELL_BETA")) {
                // If it has, then store its value in a variable
                DECISION_TO_SELL_BETA = cmd.getOptionValue("DECISION_TO_SELL_BETA");
            } else {
                // If not, store the default value in a variable
                DECISION_TO_SELL_BETA = "5.0";
            }
            if(cmd.hasOption("DECISION_TO_SELL_HPC")) {
                // If it has, then store its value in a variable
                DECISION_TO_SELL_HPC = cmd.getOptionValue("DECISION_TO_SELL_HPC");
            } else {
                // If not, store the default value in a variable
                DECISION_TO_SELL_HPC = "0.05";
            }
            if(cmd.hasOption("DECISION_TO_SELL_INTEREST")) {
                // If it has, then store its value in a variable
                DECISION_TO_SELL_INTEREST = cmd.getOptionValue("DECISION_TO_SELL_INTEREST");
            } else {
                // If not, store the default value in a variable
                DECISION_TO_SELL_INTEREST = "0.03";
            }
            if(cmd.hasOption("BTL_CHOICE_INTENSITY")) {
                // If it has, then store its value in a variable
                BTL_CHOICE_INTENSITY = cmd.getOptionValue("BTL_CHOICE_INTENSITY");
            } else {
                // If not, store the default value in a variable
                BTL_CHOICE_INTENSITY = "50.0";
            }
            if(cmd.hasOption("DESIRED_RENT_INCOME_FRACTION")) {
                // If it has, then store its value in a variable
                DESIRED_RENT_INCOME_FRACTION = cmd.getOptionValue("DESIRED_RENT_INCOME_FRACTION");
            } else {
                // If not, store the default value in a variable
                DESIRED_RENT_INCOME_FRACTION = "0.33";
            }
            if(cmd.hasOption("PSYCHOLOGICAL_COST_OF_RENTING")) {
                // If it has, then store its value in a variable
                PSYCHOLOGICAL_COST_OF_RENTING = cmd.getOptionValue("PSYCHOLOGICAL_COST_OF_RENTING");
            } else {
                // If not, store the default value in a variable
                PSYCHOLOGICAL_COST_OF_RENTING = "0.0916666666667";
            }
            if(cmd.hasOption("SENSITIVITY_RENT_OR_PURCHASE")) {
                // If it has, then store its value in a variable
                SENSITIVITY_RENT_OR_PURCHASE = cmd.getOptionValue("SENSITIVITY_RENT_OR_PURCHASE");
            } else {
                // If not, store the default value in a variable
                SENSITIVITY_RENT_OR_PURCHASE = "0.000285714285714";
            }
        }
        catch(ParseException pex) {
            // Catch possible parsing errors
            System.err.println("Parsing failed. Reason: " + pex.getMessage());
            // And print input arguments usage information
            formatter.printHelp( "spatial-housing-model", options );
        }

        // Check if outputFolder directory already exists
        File f = new File(outputFolder);
        if (f.exists() && !devBoolean) {
            // If it does, try removing everything inside (with a warning that requests approval!)
            Scanner reader = new Scanner(System.in);
            System.out.println("\nATTENTION:\n\nThe folder chosen for output, '" + outputFolder + "', already exists and " +
                    "might contain relevant files.\nDo you still want to proceed and erase all content?");
            String reply = reader.next();
            if (!reply.equalsIgnoreCase("yes") && !reply.equalsIgnoreCase("y")) {
                // If user does not clearly reply "yes", then stop the program
                System.exit(0);
            } else {
                // Otherwise, try to erase everything inside the folder
                try {
                    FileUtils.cleanDirectory(f);
                } catch (IOException ioe) {
                    // Catch possible folder cleaning errors
                    System.err.println("Folder cleaning failed. Reason: " + ioe.getMessage());
                }
            }
        } else {
            // If it doesn't, simply create it
            f.mkdirs();
        }

        // Copy config file to output folder
        try {
            FileUtils.copyFileToDirectory(new File(configFileName), new File(outputFolder));
        } catch (IOException ioe) {
            System.err.println("Copying config file to output folder failed. Reason: " + ioe.getMessage());
        }
    }

	/**
	 * @return Simulated time in months
	 */
	static public int getTime() {
		return t;
	}

    /**
     * @return Current month of the simulation
     */
	static public int getMonth() {
		return t%12 + 1;
	}

    public MersenneTwister getPrng() {
        return prng;
    }

    private static void setRecordGeneral() {
        creditSupply.setActive(true);
        householdStats.setActive(true);
        housingMarketStats.setActive(true);
        rentalMarketStats.setActive(true);
    }

	private static void setRecordCoreIndicators(boolean recordCoreIndicators) {
	    coreIndicators.setActive(recordCoreIndicators);
	}

	private static void setRecordMicroData(boolean record) { transactionRecorder.setActive(record); }

}
