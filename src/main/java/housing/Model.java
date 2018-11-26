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
 * Note that the seed for random number generation is set from the config file.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/

public class Model {

    //------------------//
    //----- Fields -----//
    //------------------//

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
    public static MesoDataRecorder      mesoDataRecorder;
    public static int	                nSimulation; // To keep track of the simulation number
    public static int	                t; // To keep track of time (in months)

    static Government		            government;

    private static MersenneTwister      prng;
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
        mesoDataRecorder = new collectors.MesoDataRecorder(outputFolder);
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

        // Open files for writing multiple runs results
        recorder.openMultiRunFiles(config.recordCoreIndicators);

        // Perform config.N_SIMS simulations
		for (nSimulation = 1; nSimulation <= config.N_SIMS; nSimulation += 1) {

            // For each simulation, open files for writing single-run results
            recorder.openSingleRunFiles(nSimulation);
            if (config.recordMicroData) { transactionRecorder.openSingleRunFiles(nSimulation); }
            if (config.recordBankBalance) {
                mesoDataRecorder.openSingleRunSingleVariableFiles(nSimulation, config.recordBankBalance,
                        config.recordNInvestmentProperties);
            }

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
                    System.out.println("Simulation: " + nSimulation + ", time: " + t);
                }
            }

			// Finish each simulation within the recorders (closing single-run files, changing line in multi-run files)
            recorder.finishRun(config.recordCoreIndicators);
            if (config.recordMicroData) transactionRecorder.finishRun();
            if (config.recordBankBalance) {
                mesoDataRecorder.finishRun(config.recordBankBalance, config.recordNInvestmentProperties);
            }
		}

        // After the last simulation, clean up
        recorder.finish(config.recordCoreIndicators);

        //Stop the program when finished
		System.exit(0);
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
	static public int getTime() { return t; }

    /**
     * @return Current month of the simulation
     */
	static public int getMonth() { return t%12 + 1; }

    public MersenneTwister getPrng() { return prng; }
}
