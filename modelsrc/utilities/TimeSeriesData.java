package utilities;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class TimeSeriesData extends ArrayList<Double> {
	private static final long serialVersionUID = 3375301779145102517L;

	// TODO: Given that this whole class is not used, check if we can simply remove it

	public TimeSeriesData(String filename, int field) throws IOException {
		Reader in = new FileReader(filename);
		Iterator<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in).iterator();
		CSVRecord record;
		while(records.hasNext()) {
			record = records.next();
			add(Double.valueOf(record.get(field)));
		}
	}

}
