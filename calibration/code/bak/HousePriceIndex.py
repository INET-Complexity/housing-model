# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""
import pandas as pd
import matplotlib.pyplot as plt

######################################################################
class QuarterlyTable(pd.Series):
    'Representation of a column of numbers at quarterly time intervals'
    offset = 0
    # offset  - row number of first quarter of 2000
    # columns - column containing data
    def __init__(self, offset, dataSeries):
        pd.Series.__init__(self,data=dataSeries)
        self.offset = offset        

    def val(self, month, year):
        return(self.iloc[self.getRow(month, year)])

    def annualGrowth(self, month, year, lag):
        row = self.getRow(month, year)
        lag = lag/3
        return( (self.iloc[row]/
                 self.iloc[row-lag] - 1.0)*4.0/lag)

    def annualGrowthData(self, lag):
        lag = lag/3
        return(QuarterlyTable(self.offset-lag,(self[lag:].values/self[:self.size-lag].values)*4.0/lag))

    def getRow(self, month, year):
        row = (month-1)/3 + 4*(year-2000) + self.offset
        if(row<0): row = 0
        return(row)    

######################################################################
class Nationwide(QuarterlyTable):
    'Nationwide House Price Appreciation table (Non-seasonally adjusted)'
    def __init__(self):
        QuarterlyTable.__init__(self,  102, pd.read_excel("/home/daniel/data/datasets/HPI/NationwideHPI.xls").iloc[5:,28])

######################################################################
class NationwideSeasonal(QuarterlyTable):
    'Nationwide House Price Appreciation table (Seasonally adjusted)'
    def __init__(self):
        QuarterlyTable.__init__(self, 102, pd.read_excel("/home/daniel/data/datasets/HPI/NationwideHPISeasonal.xls").iloc[5:,13])

######################################################################
class HalifaxSeasonal(QuarterlyTable):
    'Halifax seasonal House Price Appreciation (seasonally adjusted)'
    def __init__(self):
        QuarterlyTable.__init__(self,  68, pd.read_excel("/home/daniel/data/datasets/HPI/HalifaxHPI.xls", sheetname="All (SA) Quarters").iloc[5:,25])

######################################################################
class HPISeasonal():
    nationwide = pd.Series()
    halifax = pd.Series()

    def __init__(self):
        self.nationwide = NationwideSeasonal()
        self.halifax = HalifaxSeasonal()
    
    def HPI(self):
        offset = self.nationwide.offset - self.halifax.offset
        size = self.halifax.size
        return(QuarterlyTable(self.halifax.offset,(self.nationwide[offset:offset+size] + self.halifax[:])/2.0))

    def HPA(self):
        hpa1 = self.nationwide.annualGrowthData(12)
        hpa2 = self.halifax.annualGrowthData(12)
        offset = hpa1.offset - hpa2.offset
        size = hpa2.size
        return(QuarterlyTable(hpa2.offset,(hpa1[offset:offset+size].values + hpa2[:size-1].values)/2.0))
        

hpi = HalifaxSeasonal()
hpa = hpi.annualGrowthData(12)
row = hpa.getRow(1,1990)
hpi2 = NationwideSeasonal()
hpa2 = hpi2.annualGrowthData(12)
row2 = hpa2.getRow(1,1990)
plt.plot(hpa[row:])
plt.plot(hpa2[row2:])

hpi3 = HPISeasonal()
hpa3 = hpi3.HPA()
row3 = hpa3.getRow(1,1990)
plt.plot(hpa3[row3:])
