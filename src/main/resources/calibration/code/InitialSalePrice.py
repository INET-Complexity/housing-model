# -*- coding: utf-8 -*-
"""
Created on Tue May  5 23:17:22 2015

@author: daniel
"""

import Datasets as ds
import matplotlib.pyplot as plt
from datetime import datetime
import numpy as np
import scipy.stats as ss

#import random

hpi = ds.HPIMonthly()

def backProjectedPrice(backDate, date, price):
    currHPI = hpi.HPIat(date)
    backHPI = hpi.HPIat(backDate)
    return(price*backHPI/currHPI)

def markup(row):
    return(row['INITIAL PRICE']/backProjectedPrice(datetime.strptime(row['CREATED'], "%Y-%m-%d"),datetime.strptime(row['LATEST SOLD'], "%Y-%m-%d"),row['PRICE']))


def averageDaysOnMarket(data, date):
    dom = [(datetime.strptime(row[1],"%Y-%m-%d") - datetime.strptime(row[0],"%Y-%m-%d")).days for row in data[data['LATEST SOLD']==date][['CREATED','LATEST SOLD']].values]
    return(sum(dom)/len(dom))

data = ds.ZooplaRawCollated() # 2008-11-06
#data = ds.ZooplaRawCollated(2000000) # 2009-09-30
#data = ds.ZooplaRawCollated(3900000) # 2010-04-27
#data = ds.ZooplaRawCollated(4000000) # 2010-05-07?
chunk = data.read(200000)
#filteredchunk = chunk[(chunk["MARKET"]=="SALE") & (chunk['INITIAL PRICE'].values>0) & (chunk['INITIAL PRICE'].values<10000000)][['LAND REGISTRY UID','CREATED','INITIAL PRICE','LATEST SOLD']]
filteredchunk = chunk[(chunk["MARKET"]=="SALE") & (chunk['INITIAL PRICE'].values>0) & (chunk['INITIAL PRICE'].values<10000000)][['CREATED','INITIAL PRICE','LATEST SOLD','PRICE']]
date = datetime.strptime("2008-10-11", "%Y-%m-%d")
refdate = datetime.strptime("1900-01-01", "%Y-%m-%d")

soldListings = chunk[(chunk["MARKET"]=="SALE") & (chunk['INITIAL PRICE'].values>0) & (chunk['INITIAL PRICE'].values<10000000) & (chunk['STATUS']=="SOLD")]
#soldListings['StartDate'] = [datetime.strptime(x,"%Y-%m-%d") for x in soldListings['CREATED']]
#soldListings['EndDate'] = [datetime.strptime(x,"%Y-%m-%d") for x in soldListings['LATEST SOLD']]


#plt.figure()
#plt.hist(np.divide(soldListings['PRICE CHANGE'].values*0.1,soldListings['INITIAL PRICE'].values), bins=50, range=(-0.05,-0.01))
#population, xbins, ybins = np.histogram2d(filteredchunk['CREATED'],filteredchunk['INITIAL PRICE'],bins=[50,50])
#plt.figure()
#plt.hist2d(filteredchunk[(filteredchunk['DAY'] < 40500) & (filteredchunk['DAY']>39800)]['DAY'].values,np.log(filteredchunk[(filteredchunk['DAY'] < 40500) & (filteredchunk['DAY']>39800)]['INITIAL PRICE'].values),bins=[50,50])
#plt.colorbar()
#plt.show()

#plt.figure()
#plt.hist(np.log(filteredchunk['INITIAL PRICE'].values), bins=50)

markupOnBackProjection = soldListings.apply(markup,axis=1)
plt.figure()
plt.hist(markupOnBackProjection.values,bins=50,range=(0.90,1.1))
plt.figure()
plt.hist(markupOnBackProjection.values,bins=50,range=(0.90,0.999))
lower = markupOnBackProjection[(markupOnBackProjection < 0.999)  & (markupOnBackProjection > 0.1)].values
mean = np.mean(lower)
sd = np.std(lower)
prob = lower.size*1.0/markupOnBackProjection.values.size
expfit = ss.expon.fit(-1.0*lower)
print "lower mean = ",mean
print "loer stdev = ",sd
print "lower prob = ",prob
print "exponential fit (location, scale) = ",expfit
plt.figure()
plt.hist(markupOnBackProjection.values,bins=50,range=(1.001,1.15))
upper = markupOnBackProjection[(markupOnBackProjection > 1.001)  & (markupOnBackProjection < 2.0)].values
mean = np.mean(upper)
sd = np.std(upper)
prob = upper.size*1.0/markupOnBackProjection.values.size
expfit = ss.expon.fit(upper)
print "upper mean = ",mean
print "upper stdev = ",sd
print "upper prob = ",prob
print "exponential fit (location, scale) = ",expfit

#plt.figure()
#plt.hist2d([(datetime.strptime(d, "%Y-%m-%d")-refdate).days for d in soldListings['LATEST SOLD'].unique()],[averageDaysOnMarket(soldListings,d) for d in soldListings['LATEST SOLD'].unique()],bins=(50,50))
#for row in filteredchunk.values:
    
