# -*- coding: utf-8 -*-
"""
Created on Thu Apr 30 17:31:30 2015

@author: daniel
"""

import Datasets as ds
import pandas as pd
import matplotlib.pyplot as plt

derived = ds.EHSinterview()
income = ds.EHSincome()
joined = pd.merge(derived,income, on='aacode', how='inner')
data = joined[['HYEARGRx','AmtSvng1b']]
plt.scatter(data['HYEARGRx'], data['AmtSvng1b'])