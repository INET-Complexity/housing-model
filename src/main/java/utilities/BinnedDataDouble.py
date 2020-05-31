from typing import Optional

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

#**
#*  Utility class to that expands BinnedData with a constructor that reads data from a source file
#*
#*  @author daniel, Adrian Carro
#*/
class BinnedDataDouble(BinnedData):
    #**
    #* Loads data from a .csv file. The file should be in the format {bin min, min max, value}, with as many initial
    #* rows as needed for comments but always marked with an initial "#" character
    #*
    #* @param filename Address of the file to read data from
    #*/
    def __init__(self,
                 filename: Optional[str] = None,
                 firstBinMin: Optional[float] = None,
                 binWidth: Optional[float] = None):
        if firstBinMin is not None:
            # This constructor creates a BinnedDataDouble object with a given first bin minimum and a given bin width, but
            # without reading any data. Thus, data is to be added manually via the add method of the ArrayList
            #
            # @param firstBinMin First bin minimum
            # @param binWidth Bin width
            super().__init__(firstBinMin, binWidth)
            return

        super().__init__(0.0,0.0)
        try:
            # Open file and buffered readers
            FileReader in = new FileReader(filename);
            BufferedReader buffReader = new BufferedReader(in);
            # Skip initial comment lines keeping mark of previous position to return to if line is not comment
            buffReader.mark(1000); # 1000 is just the number of characters that can be read while preserving the mark
            String line = buffReader.readLine();
            while (line.charAt(0) == '#'):
                buffReader.mark(1000);
                line = buffReader.readLine();
            buffReader.reset(); # Return to previous position (before reading the first line that was not a comment)
            # Pass advanced buffered reader to CSVFormat parser
            Iterator<CSVRecord> records = CSVFormat.EXCEL.parse(buffReader).iterator();
            CSVRecord record;
            # Read through records
            if(records.hasNext()):
                record = records.next();
                # Use the first record to set the first bin minimum and the bin width...
                this.setFirstBinMin(Double.valueOf(record.get(0)));
                this.setBinWidth(Double.valueOf(record.get(1))-firstBinMin);
                # ...before actually adding it to the array
                add(Double.valueOf(record.get(2)));
                while(records.hasNext()):
                    record = records.next();
                    # Next records are just added to the array
                    add(Double.valueOf(record.get(2)));
        except Exception as e:
            print("Problem while loading data from " + filename + " for creating a BinnedDataDouble object");
            print(e)
