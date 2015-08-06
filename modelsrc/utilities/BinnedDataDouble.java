package utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;


public class BinnedDataDouble extends BinnedData<Double> {
	private static final long serialVersionUID = -2940041481582439332L;

	/***
	 * Loads data from a .csv file. The file should be in the format
	 * 
	 * bin min, min max, value
	 * 
	 * The first row should be the titles of the columns.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public BinnedDataDouble(String filename) throws IOException {
		super(0.0,0.0);
		Reader in = new FileReader(filename);
		Iterator<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
		CSVRecord record;
		if(records.hasNext()) {
			record = records.next();
		    this.setFirstBinMin(Double.valueOf(record.get(0)));
		    this.setBinWidth(Double.valueOf(record.get(1))-firstBinMin);
		    add(Double.valueOf(record.get(2)));
			while(records.hasNext()) {
				record = records.next();
			    add(Double.valueOf(record.get(2)));
			}
		}
	}
	
	public BinnedDataDouble(double firstBinMin, double binWidth) {
		super(firstBinMin, binWidth);
	}
}
