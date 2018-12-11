# -*- coding: utf-8 -*-
"""
Created on Tue May  5 23:17:22 2015

@author: daniel
"""

import Datasets as ds
import matplotlib.pyplot as plt
from datetime import datetime

#import random

data = ds.ZooplaMatchedRegistry()
chunk = data.read(25000)
filteredchunk = chunk[chunk["MARKET"]=="SALE"][['LAND REGISTRY UID','CREATED','INITIAL PRICE']][chunk['PRICE']>0]
#population, xbins, ybins = np.histogram2d(filteredchunk['CREATED'],filteredchunk['INITIAL PRICE'],bins=[50,50])
plt.figure()
plt.hist2d(filteredchunk['CREATED'],filteredchunk['INITIAL PRICE'],bins=[50,50])
plt.colorbar()
plt.show()

plt.figure()
plt.hist(filteredchunk[[d.startswith("2008-10") for d in filteredchunk['CREATED']]]['INITIAL PRICE'], bins=50)
#for row in filteredchunk.values:
    
