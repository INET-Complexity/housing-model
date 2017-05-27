# -*- coding: utf-8 -*-
"""
Created on Tue Apr 21 15:48:46 2015

@author: daniel
"""
#from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from matplotlib import pyplot as plt
import scipy.optimize as opt
import pandas as pd

class Solver:
    data = []
    func = 0
    optParams = []
    residuals = []
    xvals = []
    totalResidual = 0.0
    
    def __init__(self,d,f):
        self.data = d
        self.func = f
        self.optParams = []
        
    def err(self,params):
        outFirst = self.data.iat[0,0]
        e = 0.0    
        for row in self.data.values:
            if(row[0] == outFirst):
                x = row[1:-1]
                y = self.func(params,x)
            e += (y-row[0])*(y-row[0])*row[2]
        return(e)

    def calcResiduals(self):
        outFirst = self.data.iat[0,0]
        e = -1.0
        for row in self.data.values:
            if(row[0] == outFirst):
                x = row[1:-1]
                y = self.func(self.optParams,x)
                if(e >= 0.0):
                    self.residuals.append(e)
                    self.totalResidual += e
                    self.xvals.extend(x)
                e = 0.0
            e += (y-row[0])*(y-row[0])*row[2]

    def optimize(self, firstGuessParams):
        self.optParams = opt.fmin(self.err,firstGuessParams)
        self.calcResiduals()
        return(self.optParams)
    
    # only works in 2d
    def plotPDF(self):
        plt.scatter(self.data.iloc[:,1].values, data.iloc[:,0].values, c=data.iloc[:,2].values)  

    # only works in 2d
    def plotFunc(self, params):
        xvals = [x/10.0 for x in range(80,115)]
        yvals = [self.func(params,[x]) for x in xvals]
        plt.plot(xvals,yvals)

    def plotResiduals(self):
        plt.plot(self.xvals,np.array(self.residuals)*50.0)

def linearFunc(params, x):
    return(params[0]*x[0] + params[1])

def dogLegFunc(params, x):
    if(x[0] < params[0]):
        return(params[1])
    return(params[2]*(x[0]-params[0]) + params[1])

data = pd.io.pytables.read_hdf("pdfRentalPrice.hd5",'data')
mySolver = Solver(data, dogLegFunc)
params = mySolver.optimize([10.0, 4.5, 0.2])
print params
print mySolver.totalResidual
#mySolver.plotPDF()
mySolver.plotFunc(params)
mySolver.plotResiduals()
