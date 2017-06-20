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

    # plot
    def plotProbability(self):
        plt.figure(figsize=(10, 10))
        im = plt.imshow(self.columnNormalise(self.population), origin='low', cmap=cm.jet)
        plt.colorbar(im, orientation='horizontal')
        plt.show()
        
    def plotAverage(self):
        averageRent = np.dot(self.xbins[0:40],self.columnNormalise(self.population))
        plt.plot(averageRent)
        
    def columnNormalise(self,matrix):
        col_sums = matrix.sum(axis=0)
        probability = matrix / col_sums[np.newaxis,:]
        return(probability)        

rentPrice = RentalPriceDecision()
rentPrice.plotProbability()
#rentPrice.renterData.replace(to_replace=".*more.*", value=100000, inplace=True, regex=True)
#print rentPrice.renterData
