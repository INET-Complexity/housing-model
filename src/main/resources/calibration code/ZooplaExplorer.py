# -*- coding: utf-8 -*-
"""
Classes to explore Zoopla data

@author: Adrian Carro
"""

import pandas as pd
import Datasets as ds
import numpy as np


decision = ds.ZooplaMatchedAggregated().read(100)
pd.set_option('display.max_columns', None)

# data = ds.ZooplaMatchedDaily()
data = ds.ZooplaRawCollated()
chunk = data.read(500)
filteredChunk = chunk[(chunk["CREATED"] != chunk["LATEST SOLD"]) & (np.invert(pd.isnull(chunk["LATEST SOLD"])))]
# filteredChunk = chunk[np.invert(pd.isnull(chunk["LATEST SOLD"]))]


# filtered_chunk = chunk[chunk["MARKET"]=="SALE"][['LISTING ID','DAY','PRICE']][chunk['PRICE']>0]
pd.set_option('display.max_rows', None)
pd.set_option("display.max_columns", None)
# print decision[['LISTING ID', 'CREATED', 'DELETED']]
print len(filteredChunk)
print filteredChunk
