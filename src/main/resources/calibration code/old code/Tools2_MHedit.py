# -*- coding: utf-8 -*-
"""
Created on Thu Apr 16 16:32:30 2015

@author: 326052
"""

import Datasets as ds
import pandas as pd
import numpy as np
from datetime import datetime
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import math
import pylab as pyl

savedOutput1 = 0
savedOutput2 = 0
hist = 0

class DiscountDistribution:
    ysize = 200
    dist = np.zeros((400,ysize))
    yOrigin = 200
    yUnit = 0.1

    def add(self, start, end, percent):
        for day in range(start, end):
            self.addPoint(day,percent)

    def addPoint(self, day, percent):
        yindex = percent/self.yUnit + self.yOrigin
#        if(yindex > 399): yindex = 399
#        if(yindex < 0): yindex = 0
        if(day < 400 and yindex < self.ysize and yindex >= 0):
            self.dist[day, yindex] += 1


class PriceCalc():
    currentprice = 0
    initialmarketprice = 0
    daysonmarket = 0
    lastChange = 0
    
    def __init__(self, initialDate, initialPrice):
        self.currentprice = initialPrice
        self.initialmarketprice = initialPrice
        self.daysonmarket = 0
        self.lastChange = self.dateconvert(initialDate)
        
    def dateconvert(self, dateString):
        if type(dateString) is str:
            return(datetime.strptime(dateString, "%Y-%m-%d"))      
        else:
            return(datetime.strptime("1900-01-01", "%Y-%m-%d"))
                              
    def add(self, dateString, price):
        newDate = self.dateconvert(dateString)
        startDays = self.daysonmarket
        endDays = self.daysonmarket + (newDate - self.lastChange).days
        reduction = (self.currentprice - self.initialmarketprice)*100.0/self.initialmarketprice
        self.currentprice = price
        self.daysonmarket = endDays
        self.lastChange = newDate
        
        
        
#        for month in range(0,120):
#            if math.trunc(endDays/30.0-1.0)==month: 
#                if reduction !=0.0:
#                    self.changeind = 1
#                else:
#                    self.changeind =0
#            else:
#                self.changeind = 0
#                
#        changeind = self.changeind
#                    
               
        return(startDays, endDays, reduction)
        
    def timemarket(self, dateString):
        marketdate = self.dateconvert(dateString)
 
        return(marketdate)
        
        

def plotProbability(mat):
    plt.figure(figsize=(10, 10))
    im = plt.imshow(mat, origin='low', cmap=cm.jet)
    plt.colorbar(im, orientation='horizontal')
    plt.show()

distribution = DiscountDistribution()


def ZooplaPriceChanges():
    total = 0
    pSame = 0
    priceMap = {}
#    distribution = DiscountDistribution()    
    data = ds.ZooplaMatchedDaily()
    #    store = pd.HDFStore('rawDaily.hd5',mode='w')
    #    for chunk in data.parser:
    chunk = data.read(100000)
    chunk.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)
    filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DAY','PRICE']][chunk['PRICE']>0]
    change = []
    changeprice = []
    nochange = []
    for row in filteredchunk.values:
        if row[0] in priceMap:
            if(priceMap[row[0]].currentprice == row[2]):
                # no change
                nochange.append(priceMap[row[0]].daysonmarket/30)
            else:
                change.append(priceMap[row[0]].daysonmarket/30)
                changeprice.append([priceMap[row[0]].daysonmarket/30, -(priceMap[row[0]].currentprice-row[2])/row[2]*100])
          
                
            startDay, endDay, percent = priceMap[row[0]].add(row[1],row[2])
            distribution.add(startDay, endDay, percent)
        else:
            priceMap[row[0]] = PriceCalc(row[1],row[2])
               
    # now get deletion dates
    delData = ds.ZooplaMatchedCollated()
#    for chunk in delData.parser:
    chunk = delData.read(100000)
    chunk.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)
#    filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DELETED','LATEST SOLD']]
    
#    filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DELETED']]
    
    filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','CREATED','DELETED']][chunk["LATEST SOLD"].isnull()]
        
#    filteredchunk = filteredchunk2[filteredchunk2['']==2952]
    
 
    for row in filteredchunk.values:
       if row[0] in priceMap:
           if(priceMap[row[0]].currentprice == priceMap[row[0]].initialmarketprice):
               pSame += 1
           total += 1

    print pSame, total, pSame*1.0/total
    

    global timeonmarket

    for row in filteredchunk.values:
       if row[0] in priceMap:
           startDay, endDay, percent = priceMap[row[0]].add(row[1],0)
           distribution.add(startDay, endDay, percent)
           priceMap.pop(row[0])
           
           timeonmarket = filteredchunk[row[2]].marketdate(row[2]) - filteredchunk[row[1]].marketdate(row[1])

           
    print len(priceMap)

    global savedOutput1
    global savedOutput2
    global savedOutput3
    savedOutput1 = nochange
    savedOutput2 = change
    savedOutput3 = changeprice
    plotProbability(distribution.dist)
    
    global hist
    global n, n1, n2, nprice, df
 
 #   hist = np.histogram(savedOutput1)
    

    n1, bins1, patches1 = pyl.hist(savedOutput1,bins=range(min(savedOutput1), max(savedOutput1) + 1, 1))
    
    n2, bins2, patches2 = pyl.hist(savedOutput2,bins=range(min(savedOutput2), max(savedOutput2) + 1, 1))
    
    dist, binsa, binsb = np.histogram2d([x[0] for x in savedOutput3], [x[1] for x in savedOutput3], range=[[0,30],[-30,0]], bins=[30,20])
    
# plt.imshow(dist)

    
    n = n2/(n1+n2)
  
    return(n, n1, n2)
    
   
# plt.imshow(dist)




 #       print filteredchunk.dtypes
#        print filteredchunk
#        store.append('df',filteredchunk)
#    store.close()

#delData = ds.ZooplaMatchedCollated()
#for chunk in delData.parser:
#   chunk.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)
#   filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DELETED']]
#   for row in filteredchunk.values:
#       print row
    
ZooplaPriceChanges()
#data = pd.io.pytables.read_hdf('test.hd5','df')
#print data

        
        