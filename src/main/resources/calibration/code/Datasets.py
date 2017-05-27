# -*- coding: utf-8 -*-
"""
Created on Thu Apr 16 16:07:48 2015

@author: 326052
"""
import pandas as pd
from datetime import datetime
from dateutil import relativedelta

class Dataset(pd.DataFrame):
    def __init__(self, filename):
        if(filename.endswith('csv')) :
            pd.DataFrame.__init__(self, data=pd.read_csv(filename))
        elif(filename.endswith('dta')):
            pd.DataFrame.__init__(self, data=pd.read_stata(filename))

class LargeDataset():
    'object for reading large files'
    CHUNK = 4096
    parser=0
    def __init__(self, filename, skip=0):
        if(filename.endswith('csv')) :
            self.parser = pd.read_csv(filename, chunksize=self.CHUNK, skiprows=range(1,skip))
        elif(filename.endswith('dta')):
            self.parser = pd.read_stata(filename, chunksize=self.CHUNK,header=0, skiprows=skip)
            
    def read(self, lines):
        return(self.parser.read(lines))
        
#    def readAll(columns):
#        data = pd.DataFrame()
        

# rawDaily columns     [u'LISTING ID', u'DAY', u'DIFF', u'ADDRESS', u'PRICE', u'MARKET', u'STATUS', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# rawCollated columns  [u'LISTING ID', u'ADDRESS', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# matchedDaily columns [u'LISTING ID', u'DAY', u'DIFF', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'PRICE', u'MARKET', u'STATUS', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# matchedCollated cols [u'LISTING ID', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# matchedAggregated    [u'LISTING ID', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BUILDER', u'DATE BUILT', u'PERIOD BUILT', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'HAS CLOAKROOM', u'HAS CONSERVATORY', u'CENTRAL HEATING FUEL', u'FLOORS', u'HAS GARAGE', u'HAS PARKING', u'HAS DRIVEWAY']
# matchedRegistry      [u'LISTING ID', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BUILDER', u'DATE BUILT', u'PERIOD BUILT', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'HAS CLOAKROOM', u'HAS CONSERVATORY', u'CENTRAL HEATING FUEL', u'FLOORS', u'HAS GARAGE', u'HAS PARKING', u'HAS DRIVEWAY', u'LAND REGISTRY UID', u'LAND REGISTRY PRICE', u'LAND REGISTRY DATE', u'LAND REGISTRY DAYS']
# status.unique = ['TO RENT', 'RENTED', 'FOR SALE', 'SALE UNDER OFFER', 'SOLD', 'RENT UNDER OFFER']

######################################################################
# Zoopla
######################################################################


class ZooplaBase(LargeDataset):
    def read(self, lines):
        data = self.parser.read(lines)
        data.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)
        return(data)    

class ZooplaRawDaily(ZooplaBase):
    def __init__(self): ZooplaBase.__init__(self,"Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\A Raw Listings (daily).csv")
        
class ZooplaRawCollated(ZooplaBase):
    def __init__(self,skip=0): ZooplaBase.__init__(self,"Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\B Raw Listings (collation).csv",skip)

class ZooplaMatchedDaily(ZooplaBase):
    def __init__(self,skip=0): ZooplaBase.__init__(self,"Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\C Matched Listings (daily).csv",skip)

class ZooplaMatchedCollated(ZooplaBase):
    def __init__(self): ZooplaBase.__init__(self,"Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\D Matched Listings (collation).csv")

class ZooplaMatchedAggregated(ZooplaBase):
    def __init__(self): ZooplaBase.__init__(self,"Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\E Matched Listings (aggregation).csv")

class ZooplaMatchedRegistry(ZooplaBase):
    def __init__(self): ZooplaBase.__init__(self,"Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\F Matched Listings (registry).csv")


######################################################################
# English Housing Survey
######################################################################
class EHSinterview(Dataset):
    def __init__(self):
        Dataset.__init__(self,"Y:\Housing ABM\Data\EHS\UKDA-7512-stata11\stata11\derived\interviewfs12.dta")
        self.replace(to_replace=".*more$", value=100000, inplace=True, regex=True)
    
######################################################################
# PSD
######################################################################
class PSD(pd.io.parsers.TextFileReader):
    'Product Sales Data'
    
    filename='filename here'
    def __init__(self):
        pd.io.parsers.TextFileReader.__init__(self,data=pd.read_csv(PSD.filename, chunksize=100))
    

######################################################################
# Nationwide and Halifax house price indeces
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

class Nationwide(QuarterlyTable):
    'Nationwide House Price Appreciation table (Non-seasonally adjusted)'
    def __init__(self):
        QuarterlyTable.__init__(self,  102, pd.read_excel("/home/daniel/data/datasets/HPI/NationwideHPI.xls").iloc[5:,28])

class NationwideSeasonal(QuarterlyTable):
    'Nationwide House Price Appreciation table (Seasonally adjusted)'
    def __init__(self):
        QuarterlyTable.__init__(self, 102, pd.read_excel("/home/daniel/data/datasets/HPI/NationwideHPISeasonal.xls").iloc[5:,13])

class HalifaxSeasonal(QuarterlyTable):
    'Halifax seasonal House Price Appreciation (seasonally adjusted)'
    def __init__(self):
        QuarterlyTable.__init__(self,  68, pd.read_excel("/home/daniel/data/datasets/HPI/HalifaxHPI.xls", sheetname="All (SA) Quarters").iloc[5:,25])

class HPISeasonal():
    'Seasonal house price index, average of Nationwide and Halifax'
    nationwide = pd.Series()
    halifax = pd.Series()

    def __init__(self):
        self.nationwide = NationwideSeasonal()
        self.halifax = HalifaxSeasonal()
    
    def HPI(self):
        offset = self.nationwide.offset - self.halifax.offset
        size = self.halifax.size
        return(QuarterlyTable(self.halifax.offset,(self.nationwide[offset:offset+size] + self.halifax[:])/2.0))

    # Annualised ouse price appreciation
    def HPA(self):
        hpa1 = self.nationwide.annualGrowthData(12)
        hpa2 = self.halifax.annualGrowthData(12)
        offset = hpa1.offset - hpa2.offset
        size = hpa2.size
        return(QuarterlyTable(hpa2.offset,(hpa1[offset:offset+size].values + hpa2[:size-1].values)/2.0))

########################
# Katie's monthly HPI data
########################

class HPIMonthly(Dataset):
    def __init__(self):
        Dataset.__init__(self,"Y:\Housing ABM\Data\House price data\HPIMonthly.csv")
        self['DateObj'] = [datetime.strptime(x,"%b-%y") for x in self['Date']]

    # return interpolated HPI on a given day        
    def HPIat(self,date):
        datebefore = datetime(date.year, date.month, 1)
        dateafter = datebefore + relativedelta.relativedelta(months=1)
        frac = date.day*1.0/(dateafter-datebefore).days
        return((1.0-frac)*(self[self['DateObj'] == datebefore]['HPI']).values[0] + frac*(self[self['DateObj'] == dateafter]['HPI']).values[0])