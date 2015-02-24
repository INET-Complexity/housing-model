package housing;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.csv.*;

public class DataTable {
	public DataTable(String filename) throws IOException {
		super();
		Reader in = new FileReader(filename);
		data = new CSVParser(in, CSVFormat.EXCEL.withHeader());//CSVFormat.DEFAULT.parse(in);
		
		for(String header : data.getHeaderMap().keySet()) {
			System.out.print(header+", ");
		}
		System.out.println("");		
		System.out.println("");
		
		System.out.println("");
		for (CSVRecord record : data) {
			System.out.println(record.get("0 - 15"));
			for(String val : record) {
				System.out.print(val+", ");
			}
			System.out.println("");
		}
		
	}
	
	CSVParser data;
}
