package data;

import housing.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/************************************************
 * Class to read all government calibration data,
 * namely tax and national insurance bands and
 * rates, from external files
 *
 * @author Adrian Carro
 * @since 20/05/17
 *
 ************************************************/
public class Government {

    /** Declarations and initialisations **/

    public static BandsAndRates tax = readBandsAndRates(Model.config.DATA_TAX_RATES);
    public static BandsAndRates nationalInsurance = readBandsAndRates(Model.config.DATA_NATIONAL_INSURANCE_RATES);

    /**
     * Class to group bands and rates arrays in a single object, such that it can be returned from methods
     */
    public static class BandsAndRates {
        public Double[] bands = null;
        public Double[] rates = null;
    }

    /**
     * Method to read bands and rates from a file, to be used to read both tax and national insurance data
     * @param   fileName    String with name of file (address inside source folder)
     * @return  BandsAndRates object containing two arrays of Doubles, one with the bands and the other with the rates
     */
    public static BandsAndRates readBandsAndRates(String fileName) {
        BandsAndRates bandsAndRates = new BandsAndRates();
        List<Double> dummyBands = new ArrayList<>();
        List<Double> dummyRates = new ArrayList<>();
        // Try-with-resources statement
        try (BufferedReader buffReader = new BufferedReader(new FileReader(fileName))) {
            String line = buffReader.readLine();
            while (line != null) {
                if (line.charAt(0) != '#') {
                    try {
                        dummyBands.add(Double.parseDouble(line.split(",")[0]));
                    } catch (NumberFormatException nfe) {
                        System.out.println("Exception " + nfe + " while trying to parse " +
                                line.split(",")[0] + " for an double");
                        nfe.printStackTrace();
                    }
                    try {
                        dummyRates.add(Double.parseDouble(line.split(",")[1]));
                    } catch (NumberFormatException nfe) {
                        System.out.println("Exception " + nfe + " while trying to parse " +
                                line.split(",")[0] + " for an double");
                        nfe.printStackTrace();
                    }
                }
                line = buffReader.readLine();
            }
            bandsAndRates.bands = new Double[dummyBands.size()];
            bandsAndRates.rates = new Double[dummyRates.size()];
            bandsAndRates.bands = dummyBands.toArray(bandsAndRates.bands);
            bandsAndRates.rates = dummyRates.toArray(bandsAndRates.rates);

        } catch (IOException ioe) {
            System.out.println("Exception " + ioe + " while trying to read file '" + fileName + "'");
            ioe.printStackTrace();
        }
        return bandsAndRates;
    }
}
