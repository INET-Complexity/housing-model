package housing;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.lang.Integer;

/************************************************
 * Class to encapsulate all the configuration parameters
 * of the model
 * It also contains all methods needed to read these
 * parameter values from a configuration properties file
 *
 * @author Adrian Carro
 * @since 20/02/2017
 *
 ************************************************/
public class Config {
    // General model control parameters
    int N_STEPS;				            // Simulation duration in time steps
    int TIME_TO_START_RECORDING;	        // Time steps before recording statistics (initialisation time)
    int N_SIMS; 					        // Number of simulations to run (monte-carlo)
    boolean recordCoreIndicators;		    // True to write time series for each core indicator
    boolean recordMicroData;			    // True to write micro data for each transaction made
    // House parameters
    public int N_QUALITY;                          // Number of quality bands for houses
    // Housing market parameters
    int DAYS_UNDER_OFFER;                   // Time (in days) that a house remains under offer
    double BIDUP = 1.0075;                  // Smallest proportional increase in price that can cause a gazump
    int HPI_LENGTH = 15;                    // Number of months to record HPI
    // Demographic parameters
    int TARGET_POPULATION;                  // Target number of households
    boolean SPINUP;                         // TODO: Unclear parameter related to the creation of the population

    // Finally, create object containing all derived parameters
    Config.DerivedParams derivedParams = new DerivedParams();

    /**
     * Class to contain all parameters which are not read from the configuration (.properties) file, but derived,
     * instead, from these configuration parameters
     */
    public class DerivedParams {
        double MONTHS_UNDER_OFFER = DAYS_UNDER_OFFER/30.0;  // Time (in months) that a house remains under offer
        // TODO: Clarify where does this 0.2 come from
        double T = 0.02*TARGET_POPULATION;                  // Characteristic number of data-points over which to average market statistics
        double E = Math.exp(-1.0/T);                        // Decay constant for averaging days on market (in transactions)
        double G = Math.exp(-N_QUALITY/T);                  // Decay constant for averageListPrice averaging (in transactions)
    }

    /**
     * Empty constructor, mainly used for copying local instances of the Model Config instance into other classes
     */
    public Config () {}

    /**
     * Constructor with full initialization, used only for the original Model Config instance
     */
    public Config (String configFileName) {
        getConfigValues(configFileName);
    }

    /**
     * Method to read configuration parameters from a configuration (.properties) file
     * @param   configFileName    String with name of configuration (.properties) file (address inside source folder)
     */
    public void getConfigValues(String configFileName) {
        // Try-with-resources statement
        try (FileReader fileReader = new FileReader(configFileName)) {
            Properties prop = new Properties();
            prop.load(fileReader);
            // Run through all the fields of the Class using reflection
            for (Field field : this.getClass().getDeclaredFields()) {
                System.out.println(field.getName());
                // For int fields, parse the int with appropriate exception handling
                if (field.getType().toString().equals("int")) {
                    try {
                        field.set(this, Integer.parseInt(prop.getProperty(field.getName())));
                    } catch (NumberFormatException nfe) {
                        System.out.println("Exception " + nfe + " while trying to parse the field " +
                                field.getName() + " for an integer");
                        nfe.printStackTrace();
                    } catch (IllegalAccessException iae) {
                        System.out.println("Exception " + iae + " while trying to set the field " +
                                field.getName());
                        iae.printStackTrace();
                    }
                // For int fields, parse the int with appropriate exception handling
                } else if (field.getType().toString().equals("boolean")) {
                    try {
                        if (prop.getProperty(field.getName()).equals("true") ||
                                prop.getProperty(field.getName()).equals("false")) {
                            field.set(this, Boolean.parseBoolean(prop.getProperty(field.getName())));
                        } else {
                            throw new BooleanFormatException("For input string \"" + prop.getProperty(field.getName()) +
                                    "\"");
                        }
                    } catch (BooleanFormatException bfe) {
                        System.out.println("Exception " + bfe + " while trying to parse the field " +
                                field.getName() + " for a boolean");
                        bfe.printStackTrace();
                    } catch (IllegalAccessException iae) {
                        System.out.println("Exception " + iae + " while trying to set the field " +
                                field.getName());
                        iae.printStackTrace();
                    }
                }
            }
        } catch (IOException ioe) {
            System.out.println("Exception " + ioe + " while trying to read file '" + configFileName + "'");
            ioe.printStackTrace();
        }
    }

    /**
     * Equivalent to NumberFormatException for detecting problems when parsing for boolean values
     */
    public class BooleanFormatException extends RuntimeException {
        public BooleanFormatException() { super(); }
        public BooleanFormatException(String message) { super(message); }
        public BooleanFormatException(Throwable cause) { super(cause); }
        public BooleanFormatException(String message, Throwable cause) { super(message, cause); }
    }
}
