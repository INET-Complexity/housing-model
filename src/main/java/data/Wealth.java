package data;

import housing.Model;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import utilities.BinnedData;
import utilities.BinnedDataDouble;
import utilities.Pdf;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**************************************************************************************************
 * Class to read and work with wealth data before passing it to the Household class.
 *
 * @author Adrian Carro
 *
 *************************************************************************************************/
public class Wealth {

    //------------------//
    //----- Fields -----//
    //------------------//

    static private BinnedData<Pdf> lnWealthGivenLnIncome = loadLnWealthPDFGivenLnIncome();

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Read data from file Model.config.DATA_WEALTH_GIVEN_INCOME and return it as a binnedData pdf of (log) wealth
     * conditional on household (log) income.
     */
	static private BinnedData<Pdf> loadLnWealthPDFGivenLnIncome() {
		final int incomeMinCol = 0;
		final int incomeMaxCol = 1;
		final int wealthMinCol = 2;
		final int wealthMaxCol = 3;
		final int probCol = 4;
		BinnedData<Pdf> data = new BinnedData<>(0.0, 0.0);
		BinnedData<BinnedDataDouble> pdfData = new BinnedData<>(0.0,0.0);
		BinnedDataDouble pdf;
		double pdfBinMin;
		double pdfBinWidth;
		double lastBinMin;
		
		Iterator<CSVRecord> records;
		try {
            // Open a file reader
			Reader in = new FileReader(Model.config.DATA_WEALTH_GIVEN_INCOME);
            // Pass reader to CSVFormat parser, which will use first line (header) to set column names
			records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
			CSVRecord record;
            // Read through records
			if (records.hasNext()) {
				record = records.next();
                // Use the first record to set the first (income) bin minimum and the (income) bin width...
                data.setFirstBinMin(Double.valueOf(record.get(incomeMinCol)));
			    data.setBinWidth(Double.valueOf(record.get(incomeMaxCol)) - data.getSupportLowerBound());
			    // ...as well as the first (income) bin minimum and the (income) bin width...
			    pdfBinMin = Double.valueOf(record.get(wealthMinCol));
			    pdfBinWidth = Double.valueOf(record.get(wealthMaxCol)) - pdfBinMin;
			    // ...wealth bin minimum and width are used to create a BinnedDataDouble object and add it to the
                // container of BinnedDataDouble objects
			    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
			    pdfData.add(pdf);
                // ...then the probability is actually added to the array of values within the BinnedDataDouble object
                pdf.add(Double.valueOf(record.get(probCol)));
			    lastBinMin = data.getSupportLowerBound();
			    // ...finally, iterate over the rest of the records
			    while (records.hasNext()) {
			    	record = records.next();
			    	// ...whenever the income bin changes, a new BinnedDataDouble object must be opened
			    	if (Double.valueOf(record.get(incomeMinCol)) != lastBinMin) {
					    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
					    pdfData.add(pdf);
					    lastBinMin = Double.valueOf(record.get(incomeMinCol));
			    	}
			    	// ...continue adding values to the previous BinnedDataDouble object
		    		pdf.add(Double.valueOf(record.get(probCol)));
			    }
			    // Finally, iterate over the BinnedDataDoubles in the container...
				for (BinnedDataDouble d: pdfData) {
			        // ...turning them into Pdf objects and adding them to the general data container
					data.add(new Pdf(d));
				}
			}
		} catch (IOException e) {
			System.out.println("Error loading data for wealth given income in data.Wealth");
			e.printStackTrace();
		}
		return data;
	}

    /**
     * Minimum bank balance each household is willing to have at the end of the month for the whole population to match
     * the wealth distribution obtained from the Wealth and Assets Survey. This desired bank balance will be then used
     * to determine non-essential consumption.
     *
     * @param annualGrossTotalIncome Household annual gross total income
     * @param propensityToSave Household propensity to save
     */
    static public double getDesiredBankBalance(double annualGrossTotalIncome, double propensityToSave) {
        double lnAnnualGrossTotalIncome = Math.log(annualGrossTotalIncome);
        // If lnAnnualGrossTotalIncome is below minimum income bin, then minimum income bin is assigned
        if (lnAnnualGrossTotalIncome < lnWealthGivenLnIncome.getSupportLowerBound()) {
            lnAnnualGrossTotalIncome = lnWealthGivenLnIncome.getSupportLowerBound();
        }
        // If lnAnnualGrossTotalIncome is above maximum income bin, then maximum income bin is assigned
        else if (lnAnnualGrossTotalIncome > lnWealthGivenLnIncome.getSupportUpperBound()) {
            lnAnnualGrossTotalIncome = lnWealthGivenLnIncome.getSupportUpperBound() - 1e-7;
        }
        // Assign gross wealth (desired bank balance) according to the determined annualGrossTotalIncome
        return Math.exp(lnWealthGivenLnIncome.getBinAt(lnAnnualGrossTotalIncome).inverseCumulativeProbability(propensityToSave));
    }
}
