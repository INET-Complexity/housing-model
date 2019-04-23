package data;

import housing.Model;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import utilities.BinnedData;
import utilities.BinnedDataDouble;
import utilities.Pdf;

/**************************************************************************************************
 * Class to read and work with income data before passing it to the Household class. Note that we
 * use here income to refer to gross employment income.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class EmploymentIncome {

    //------------------//
    //----- Fields -----//
    //------------------//

    /***
     * Calibrated against LCFS 2012 data
     */
    static private BinnedData<Pdf> lnIncomeGivenAge = loadGrossEmploymentIncomePDFGivenAge();

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Read data from file Model.config.DATA_INCOME_GIVEN_AGE and return it as a binnedData pdf of gross employment
     * income conditional on household age. Note that we are dealing here with logarithmic incomes.
     */
	static private BinnedData<Pdf> loadGrossEmploymentIncomePDFGivenAge() {
		final int ageMinCol = 0;
		final int ageMaxCol = 1;
		final int incomeMinCol = 2;
		final int incomeMaxCol = 3;
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
			Reader in = new FileReader(Model.config.DATA_INCOME_GIVEN_AGE);
            // Pass reader to CSVFormat parser, which will use first line (header) to set column names
			records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
			CSVRecord record;
            // Read through records
			if (records.hasNext()) {
				record = records.next();
                // Use the first record to set the first (age) bin minimum and the (age) bin width...
                data.setFirstBinMin(Double.valueOf(record.get(ageMinCol)));
			    data.setBinWidth(Double.valueOf(record.get(ageMaxCol)) - data.getSupportLowerBound());
			    // ...as well as the first (income) bin minimum and the (income) bin width...
			    pdfBinMin = Double.valueOf(record.get(incomeMinCol));
			    pdfBinWidth = Double.valueOf(record.get(incomeMaxCol)) - pdfBinMin;
			    // ...income bin minimum and width are used to create a BinnedDataDouble object and add it to the
                // container of BinnedDataDouble objects
			    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
			    pdfData.add(pdf);
                // ...then the probability is actually added to the array of values within the BinnedDataDouble object
                pdf.add(Double.valueOf(record.get(probCol)));
			    lastBinMin = data.getSupportLowerBound();
			    // ...finally, iterate over the rest of the records
			    while (records.hasNext()) {
			    	record = records.next();
			    	// ...whenever the age bin changes, a new BinnedDataDouble object must be opened
			    	if (Double.valueOf(record.get(ageMinCol)) != lastBinMin) {
					    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
					    pdfData.add(pdf);
					    lastBinMin = Double.valueOf(record.get(ageMinCol));
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
			System.out.println("Error loading data for income given age in data.EmploymentIncome");
			e.printStackTrace();
		}
		return data;
	}

    /**
     * Find household annual gross income given age and income percentile
     */
    static public double getAnnualGrossEmploymentIncome(double boundAge, double incomePercentile) {
        // If boundAge is below minimum age bin, then minimum age bin is assigned
        if (boundAge < lnIncomeGivenAge.getSupportLowerBound()) {
            boundAge = lnIncomeGivenAge.getSupportLowerBound();
        }
        // If boundAge is above or equal to the maximum age bin, then maximum age bin is assigned, minus small amount
        else if (boundAge >= lnIncomeGivenAge.getSupportUpperBound()) {
            boundAge = lnIncomeGivenAge.getSupportUpperBound() - 1e-7;
        }
        // Assign gross annual income according to the determined boundAge
        double income = Math.exp(lnIncomeGivenAge.getBinAt(boundAge).inverseCumulativeProbability(incomePercentile));
        // Impose a minimum income equivalent to the minimum government annual income support
        if (income < Model.config.GOVERNMENT_MONTHLY_INCOME_SUPPORT*Model.config.constants.MONTHS_IN_YEAR) {
            income = Model.config.GOVERNMENT_MONTHLY_INCOME_SUPPORT*Model.config.constants.MONTHS_IN_YEAR;
        }
        return income;
    }
}
