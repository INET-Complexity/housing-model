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
		final int givenMinCol = 0;
		final int givenMaxCol = 1;
		final int varMinCol = 2;
		final int varMaxCol = 3;
		final int probCol = 4;
		BinnedData<Pdf> data = new BinnedData<>(0.0, 0.0);
		BinnedData<BinnedDataDouble> pdfData = new BinnedData<>(0.0,0.0);
		BinnedDataDouble pdf;
		double pdfBinMin;
		double pdfBinWidth;
		double lastBinMin;
		
		Iterator<CSVRecord> records;
		try {
			Reader in = new FileReader(Model.config.DATA_INCOME_GIVEN_AGE);
			records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
			CSVRecord record;
			if (records.hasNext()) {
				record = records.next();
                data.setFirstBinMin(Double.valueOf(record.get(givenMinCol)));
			    data.setBinWidth(Double.valueOf(record.get(givenMaxCol)) - data.getSupportLowerBound());
			    pdfBinMin = Double.valueOf(record.get(varMinCol));
			    pdfBinWidth = Double.valueOf(record.get(varMaxCol)) - pdfBinMin;
			    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
			    pdfData.add(pdf);
			    pdf.add(Double.valueOf(record.get(probCol)));
			    lastBinMin = data.getSupportLowerBound();
			    while (records.hasNext()) {
			    	record = records.next();
			    	if (Double.valueOf(record.get(givenMinCol)) != lastBinMin) {
					    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
					    pdfData.add(pdf);
					    lastBinMin = Double.valueOf(record.get(givenMinCol));
			    	}
		    		pdf.add(Double.valueOf(record.get(probCol)));
			    }
				for (BinnedDataDouble d: pdfData) {
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
        // If boundAge is above maximum age bin, then maximum age bin is assigned
        else if (boundAge > lnIncomeGivenAge.getSupportUpperBound()) {
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
