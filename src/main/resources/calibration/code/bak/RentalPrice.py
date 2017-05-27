# -*- coding: utf-8 -*-
"""
Created on Tue Apr 14 15:44:04 2015

@author: daniel
"""
#import sys
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import numpy as np
#from pandas import Series, DataFrame

import Datasets as ds

###### RENTAL BID PRICE DECISION
class RentalPriceDecision:
    'Decision on how much to spend on rent'
    
    incomeField = "hhincx" # "HYEARGRx"
    renterData = pd.DataFrame()
    population = []
    xbins = []
    ybins = []
    xaxis = []
    yaxis = []
    popdf = pd.DataFrame()
    
    # extract data from file
    def __init__(self):
        rawData = ds.EHSinterview()
        
        incomeRent = rawData.loc[:,[self.incomeField,"rentwkx","bedrqx"]]   # Filter for fields of interest
        self.renterData = incomeRent[incomeRent["rentwkx"]>0] # filter out non renters
   #     self.renterData = self.renterData[self.renterData["bedrqx"]==4] # only consider one-bed
        # split the data into 2D histogram data
        self.population, self.xbins, self.ybins = np.histogram2d(
            np.log(self.renterData["rentwkx"].values),
            np.log(self.renterData[self.incomeField].values),
            bins=[40,30])
        self.xaxis = (np.array(self.xbins[1:]) + np.array(self.xbins[:-1]))/2.0
        self.yaxis = (np.array(self.ybins[1:]) + np.array(self.ybins[:-1]))/2.0
        
        self.popdf = pd.DataFrame(
            data=np.zeros(((len(self.xbins)-1)*(len(self.ybins)-1),3)),
            columns = ['rental price', 'income', 'p'])
        i = 0
        totalPop = self.population.sum()
        for param in range(1,len(self.ybins)):
            for out in range(1,len(self.xbins)):
                self.popdf.iloc[i,0] = (self.xbins[out] + self.xbins[out-1])/2.0
                self.popdf.iloc[i,1] = (self.ybins[param] + self.ybins[param-1])/2.0
                self.popdf.iloc[i,2] = self.population[out-1,param-1]*1.0/totalPop
                i += 1
                
    # plot
    def plotProbability(self):
        plt.figure(figsize=(10, 10))
#        im = plt.imshow(self.normalised(), origin='low', cmap=cm.jet)
        im = plt.imshow(self.columnNormalised(), origin='low', cmap=cm.jet)
        plt.colorbar(im, orientation='horizontal')
        plt.show()
        
    def plotAverage(self):
        averageRent = np.dot(self.xbins[0:40],self.columnNormalise(self.population))
        plt.plot(averageRent)
        
    def columnNormalised(self):
        col_sums = self.population.sum(axis=0)
        probability = self.population / col_sums[np.newaxis,:]
        return(probability)
        
    def normalised(self):
        return(self.population * 1.0 / self.population.sum())

rentPrice = RentalPriceDecision()
rentPrice.plotProbability()
#store = pd.HDFStore("pdfRentalPrice.hd5")
#store.append('data', pdf)
#store.close()
#rentPrice.renterData.replace(to_replace=".*more.*", value=100000, inplace=True, regex=True)
#print rentPrice.renterData
