# -*- coding: utf-8 -*-
"""
Created on Wed Apr 29 13:29:06 2015

@author: daniel
"""

import Datasets as ds
import numpy as np
import matplotlib.pyplot as plt


rawData = ds.EHSinterview()
data = rawData[['Prevten','agehrpx','lenresb','tenure4','HYEARGRx']][(rawData['Prevten'] >0) & (rawData['Prevten'] < 9)]
data["ageAtMove"] = data['agehrpx'] - data['lenresb']
data['tenChange'] = ((data['Prevten'] < 5) & (data['Prevten']>1))*10.0 + ((data['Prevten']>4) & (data['Prevten']<7))*20.0 + (data['Prevten']>6)*30.0 + data['tenure4']
formationData = data[['ageAtMove']][data['tenChange']<10]
formationRentData = data[['ageAtMove']][data['tenChange']==3]
moverData = data[['ageAtMove']][(data['tenChange']>10) & (data['tenChange']<20)]
rentownData = data[['ageAtMove']][data['tenChange'] == 31]
fluxes = data[['tenChange']]
incomeFormRent = data[['HYEARGRx']][data['tenChange']==3]
incomeFormSoc = data[['HYEARGRx']][data['tenChange']==2]
incomeFormOwn = data[['HYEARGRx']][data['tenChange']==1]

#hist, bins = np.histogram(ftbData)
#plt.hist(fluxes.values, bins=33)
#plt.hist(incomeFormRent.values, bins=20)
plt.hist(incomeFormSoc.values,incomeFormRent.values, incomeFormOwn.values], bins=20, stacked=True)
#plt.hist(incomeFormOwn.values, bins=20)
#plt.hist(rentownData.values, bins=20)
