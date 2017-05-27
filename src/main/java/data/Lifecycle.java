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

public class Lifecycle {
	
	static BinnedData<Pdf> loadIncomePDFGivenAge() {
		final int givenMinCol = 0;
		final int givenMaxCol = 1;
		final int varMinCol = 2;
		final int varMaxCol = 3;
		final int probCol = 4;
		BinnedData<Pdf> data = new BinnedData<Pdf>(0.0, 0.0);
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
			if(records.hasNext()) {
				record = records.next();
			    data.setFirstBinMin(Double.valueOf(record.get(givenMinCol)));
			    data.setBinWidth(Double.valueOf(record.get(givenMaxCol))-data.getSupportLowerBound());
			    pdfBinMin = Double.valueOf(record.get(varMinCol));
			    pdfBinWidth = Double.valueOf(record.get(varMaxCol)) - pdfBinMin;
			    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
			    pdfData.add(pdf);
			    pdf.add(Double.valueOf(record.get(probCol)));
			
			    lastBinMin = data.getSupportLowerBound();
			    while(records.hasNext()) {
			    	record = records.next();
			    	if(Double.valueOf(record.get(givenMinCol)) != lastBinMin) {
					    pdf = new BinnedDataDouble(pdfBinMin, pdfBinWidth);
					    pdfData.add(pdf);
					    lastBinMin = Double.valueOf(record.get(givenMinCol));
			    	}
		    		pdf.add(Double.valueOf(record.get(probCol)));
			    }
				for(BinnedDataDouble d : pdfData) {
					data.add(new Pdf(d));
				}
			}
		} catch (IOException e) {
			System.out.println("Error loading data for income given age in data.Lifecycle");
			e.printStackTrace();
		}

		/*
		Pdf d;
		for(double age=data.getSupportMin(); age < data.getSupportMax(); age+= data.getBinWidth()) {
			d = data.getBinAt(age);
			for(double lnIncome = d.start; lnIncome < d.end; lnIncome += 0.5) {
				System.out.println(age+"\t"+lnIncome+"\t"+d.density(lnIncome));
			}
		}
*/
		return(data);
	}
	
	/***
	 * Calibrated against LCFS 2012 data
	 */
	public static BinnedData<Pdf> lnIncomeGivenAge = loadIncomePDFGivenAge();
}
