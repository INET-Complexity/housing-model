# -*- coding: utf-8 -*-
"""
Created on Tue Apr 14 15:44:04 2015

@author: daniel
"""

import Datasets as ds
import pandas as pd
import numpy as np
from datetime import datetime
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import pylab as pyl
import scipy.stats as stats
import math

savedOutput1 = 0
savedOutput2 = 0
hist = 0

class DiscountDistribution:
    'Distribution of price changes by month given that price changes in that month and prior prob of change'
    xsize = 48 # number of bins on the time axis
    countNoChange = np.zeros(xsize)
    countTotal = np.zeros(xsize)    
    yOrigin = 100
    yUnit = 0.05
    changesByMonth = [[] for i in range(xsize)]

    # record no change between start and end months
    def noChange(self, start, end):
        if(end >= self.xsize): end = self.xsize-1
        for month in range(start, end+1):
            self.countNoChange[month] += 1  
            self.countTotal[month] += 1

    # add a single sample to the conditional distribution
    def addChange(self, start, month, percent):
        if(percent < -0.2 and percent > -90):
            self.noChange(start, month-1)
            if(month < self.xsize):
                self.countTotal[month] += 1
                self.changesByMonth[month].append(math.log(math.fabs(percent)))
        else:
            self.noChange(start,month)

    # probability that price will not change in a given month (given that it is still on the market)
    def probNoChange(self):
        return(np.divide(self.countNoChange,self.countTotal));

    # probability that there will be no change per month, integrated over all months 
    def probNoChangeAllTime(self):
        return(self.probNoChange().sum()/self.xsize)

    def allChanges(self):
        return([i for month in self.changesByMonth for i in month])

class PriceCalc:
    'Record current price, initial price, days on market for a listing'
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
#        reduction = (price - self.initialmarketprice)*100.0/self.initialmarketprice
        reduction = (price - self.currentprice)*100.0/self.initialmarketprice
        self.currentprice = price
        self.daysonmarket = endDays
        self.lastChange = newDate
        return(startDays, endDays, reduction)

def plotProbability(mat):
    plt.figure(figsize=(10, 10))
    im = plt.imshow(mat, origin='low', cmap=cm.jet)
    plt.colorbar(im, orientation='horizontal')
    plt.show()


def CalculatePriceChanges():
    distribution = DiscountDistribution()
    priceMap = {}
#    data = ds.ZooplaMatchedDaily(2000000) # during rising housing market
    data = ds.ZooplaMatchedDaily() # at bottom of housing market
    chunk = data.read(500000)
    chunk.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)
    filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DAY','PRICE']][chunk['PRICE']>0]
    for row in filteredchunk.values:
        # row: LISTING ID   DAY   PRICE
        listingid = row[0]
        if listingid in priceMap:
            lastRecord = priceMap[listingid]
            oldPrice = lastRecord.currentprice
            startDay, endDay, percent = lastRecord.add(row[1],row[2])
            if(oldPrice == row[2]): # no price change
                distribution.noChange(startDay/30, endDay/30)
            else:   # price has changed
                distribution.addChange(startDay/30, endDay/30, percent)
        else:
            priceMap[listingid] = PriceCalc(row[1],row[2])               
    return(distribution)           
   
dist = CalculatePriceChanges()

print "Average probability of no change per month"
print dist.probNoChange().sum()/dist.probNoChange().size
print "Probability of no change per month"
print dist.probNoChange()
plt.figure()
plt.plot(dist.probNoChange())

mean, sd = stats.norm.fit(dist.allChanges())
monthlyMeans = [stats.norm.fit(dist.changesByMonth[i])[0] for i in range(dist.xsize)]
print "Best mean and standard deviation of percentage change per month given change"
print mean,sd
print "Monthly Means"
print monthlyMeans
plt.figure()
plt.plot(monthlyMeans)
curve = [stats.norm.pdf(i*0.05,mean,sd) for i in range(-35,100)]

plt.figure()
plt.plot([i*0.05 for i in range(-35,100)],curve)
plt.hist(dist.allChanges(),bins=50,normed=True)
