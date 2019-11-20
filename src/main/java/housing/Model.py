import os
import shutil
from time import gmtime, strftime
from typing import List

import collectors

#**************************************************************************************************
#* This is the root object of the simulation. Upon creation it creates and initialises all the
#* agents in the model.
#*
#* The project is prepared to be run with maven, and it takes the following command line input
#* arguments:
#*
#* -configFile <arg>    Configuration file to be used (address within project folder). By default,
#*                      'src/main/resources/config.properties' is used.
#* -outputFolder <arg>  Folder in which to collect all results (address within project folder). By
#*                      default, 'Results/<current date and time>/' is used. The folder will be
#*                      created if it does not exist.
#* -dev                 Removes security question before erasing the content inside output folder
#*                      (if the folder already exists).
#* -help                Print input arguments usage information.
#*
#* Note that the seed for random number generation is set from the config file.
#*
#* @author daniel, Adrian Carro
#*
#*************************************************************************************************/

class Model:

    #------------------------#
    #----- Constructors -----#
    #------------------------#

    #**
    #* @param configFileName String with the address of the configuration file
    #* @param outputFolder String with the address of the folder for storing results
    #*/
    def __init__(self, configFileName: str, outputFolder: str):
        self.config = Config(configFileName)
        self.prng = MersenneTwister(config.SEED)

        self.government = Government()
        self.demographics = Demographics(self.prng)
        self.construction = Construction(self.prng)
        self.centralBank = CentralBank()
        self.bank = Bank()
        self.households: List[Household] = []
        self.houseSaleMarket = HouseSaleMarket(self.prng)
        self.houseRentalMarket = HouseRentalMarket(self.prng)

        self.recorder = collectors.Recorder(outputFolder)
        self.transactionRecorder = collectors.MicroDataRecorder(outputFolder)
        self.creditSupply = collectors.CreditSupply(outputFolder)
        self.coreIndicators = collectors.CoreIndicators()
        self.householdStats = collectors.HouseholdStats()
        self.housingMarketStats = collectors.HousingMarketStats(self.houseSaleMarket)
        self.rentalMarketStats = collectors.RentalMarketStats(self.housingMarketStats, self.houseRentalMarket)
        # To keep track of the simulation number
        self.nSimulation = 0
        # To keep track of time (in months)
        self.t = 0

        #private static MersenneTwister      prng
        #private static Demographics		    demographics
        #private static Recorder             recorder
        #private static String               configFileName
        #private static String               outputFolder

    #-------------------#
    #----- Methods -----#
    #-------------------#
    def main(self) -> None:

        # Handle input arguments from command line
        self.handleInputArguments()

        # Create an instance of Model in order to initialise it (reading config file)
        model = Model(configFileName, outputFolder)

        # Start data recorders for output
        self.setupStatics()

        # Open files for writing multiple runs results
        self.recorder.openMultiRunFiles(config.recordCoreIndicators)

        # Perform config.N_SIMS simulations
        for nSimulation in range(1, self.config.N_SIMS + 1):
            # For each simulation, open files for writing single-run results
            self.recorder.openSingleRunFiles(nSimulation)

            # For each simulation, initialise both houseSaleMarket and houseRentalMarket variables (including HPI)
            self.init()

            # For each simulation, run config.N_STEPS time steps
            for t in range(config.N_STEPS):
                # Steps model and stores sale and rental markets bid and offer prices, and their averages, into their
                # respective variables
                self.modelStep()

                # Write results of this time step and run to both multi- and single-run files
                self.recorder.writeTimeStampResults(self.config.recordCoreIndicators, t)

                # Print time information to screen
                if t % 100 == 0:
                    print("Simulation: " + str(nSimulation) + ", time: " + str(t))

            # Finish each simulation within the recorders (closing single-run files, changing line in multi-run files)
            self.recorder.finishRun(self.config.recordCoreIndicators)
            # TODO: Check what this is actually doing and if it is necessary
            if config.recordMicroData:
                self.transactionRecorder.endOfSim()

        # After the last simulation, clean up
        self.recorder.finish(config.recordCoreIndicators)
        if self.config.recordMicroData:
            self.transactionRecorder.finish()

        #Stop the program when finished
        exit()

    def setupStatics(self) -> None:
        self.setRecordGeneral()
        self.setRecordCoreIndicators(self.config.recordCoreIndicators)
        self.setRecordMicroData(self.config.recordMicroData)

    def init(self) -> None:
        self.construction.init()
        self.houseSaleMarket.init()
        self.houseRentalMarket.init()
        self.bank.init()
        self.centralBank.init()
        self.housingMarketStats.init()
        self.rentalMarketStats.init()
        self.householdStats.init()
        self.households.clear()

    def modelStep(self) -> None:
        # Update population with births and deaths
        self.demographics.step()
        # Update number of houses
        self.construction.step()
        # Updates regional households consumption, housing decisions, and corresponding regional bids and offers
        for h in self.households:
            h.step()
        # Stores sale market bid and offer prices and averages before bids are matched by clearing the market
        self.housingMarketStats.preClearingRecord()
        # Clears sale market and updates the HPI
        self.houseSaleMarket.clearMarket()
        # Computes and stores several housing market statistics after bids are matched by clearing the market (such as HPI, HPA)
        self.housingMarketStats.postClearingRecord()
        # Stores rental market bid and offer prices and averages before bids are matched by clearing the market
        self.rentalMarketStats.preClearingRecord()
        # Clears rental market
        self.houseRentalMarket.clearMarket()
        # Computes and stores several rental market statistics after bids are matched by clearing the market (such as HPI, HPA)
        self.rentalMarketStats.postClearingRecord()
        # Stores household statistics after both regional markets have been cleared
        self.householdStats.record()
        # Update credit supply statistics # TODO: Check what this actually does and if it should go elsewhere!
        self.creditSupply.step()
        # Update bank and interest rate for new mortgages
        self.bank.step(Model.households.size())
        # Update central bank policies (currently empty!)
        self.centralBank.step(self.coreIndicators)

    # This method handles command line input arguments to
    # determine the address of the input config file and
    # the folder for outputs
    #
    # @param args String with the command line arguments
    def handleInputArguments(self) -> None:
        # "Configuration file to be used (address within project folder).
        configFile = "src/main/resources/config.properties"
        # Folder in which to collect all results (address within project
        # folder). By default, 'Results/<current date and time>/' is used. The
        # folder will be created if it does not exist.
        outputFolder = "Results/" + strftime("%Y-%m-%d-%H-%M-%S", gmtime()) + "/"
        # Removes security question before erasing the content inside output
        # folder (if the folder already exists).
        devBoolean = False

        # Check if outputFolder directory already exists
        os.makedirs(outputFolder, exist_ok=True)
        # TODO remove existing files
        #print("\nATTENTION:\n\nThe folder chosen for output, '" + outputFolder + "', already exists and " +
        #      "might contain relevant files.\nDo you still want to proceed and erase all content?")

        # Copy config file to output folder
        shutil.copyfile(configFile, outputFolder + 'config.properties')

    # @return Simulated time in months
    def getTime(self) -> int:
        return t

    # @return Current month of the simulation
    def getMonth(self) -> int:
        return t % 12 + 1

    def getPrng(self):
        return self.prng

    def setRecordGeneral(self) -> None:
        self.creditSupply.setActive(true)
        self.householdStats.setActive(true)
        self.housingMarketStats.setActive(true)
        self.rentalMarketStats.setActive(true)

    def setRecordCoreIndicators(recordCoreIndicators: bool) -> None:
        self.coreIndicators.setActive(recordCoreIndicators)

    def setRecordMicroData(record: bool) -> None:
        self.transactionRecorder.setActive(record)
