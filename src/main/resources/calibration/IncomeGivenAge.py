#! usr/bin/dev python

from __future__ import division
import numpy as np

# This simply transforms the old IncomeGivenAge.csv file (now renamed as
# IncomeGivenAge_Old.csv) to avoid multiplying and dividing by 52, operation
# which remains unexplained at this point

# Names of files to open
inputFileName = './IncomeGivenAge_Old.csv'
outputFileName = './IncomeGivenAge.csv'

# Read data from file
with open(inputFileName, 'r') as inF, open(outputFileName, 'w') as outF:
    outF.write(inF.next())
    for line in inF:
        newLine = line.split(",")[0] + "," + line.split(",")[1] + ","
        newLine += str(float(line.split(",")[2]) + np.log(52)) + ","
        newLine += str(float(line.split(",")[3]) + np.log(52)) + ","
        newLine += line.split(",")[4]
        outF.write(newLine)
