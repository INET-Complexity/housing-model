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
        return(datetime.strptime(dateString, "%Y-%m-%d"))            
                              
    def add(self, dateString, price):
        newDate = self.dateconvert(dateString)
        startDays = self.daysonmarket
        endDays = self.daysonmarket + (newDate - self.lastChange).days
        reduction = (self.currentprice - self.initialmarketprice)*100.0/self.initialmarketprice
        self.currentprice = price
        self.daysonmarket = endDays
        self.lastChange = newDate
        return(startDays, endDays, reduction)

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
    for row in filteredchunk.values:
        if row[0] in priceMap:
            startDay, endDay, percent = priceMap[row[0]].add(row[1],row[2])
            distribution.add(startDay, endDay, percent)
        else:
            priceMap[row[0]] = PriceCalc(row[1],row[2])
               
    # now get deletion dates
    delData = ds.ZooplaMatchedCollated()
#    for chunk in delData.parser:
    chunk = delData.read(100000)
    chunk.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)
    filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DELETED']]
    for row in filteredchunk.values:
       if row[0] in priceMap:
           if(priceMap[row[0]].currentprice == priceMap[row[0]].initialmarketprice):
               pSame += 1
           total += 1
           startDay, endDay, percent = priceMap[row[0]].add(row[1],0)
           distribution.add(startDay, endDay, percent)
           priceMap.pop(row[0])
    print len(priceMap)
    print pSame, total, pSame*1.0/total
    plotProbability(distribution.dist)
                   
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
