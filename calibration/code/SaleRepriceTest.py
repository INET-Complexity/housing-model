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

import Zoopla as zp

class SaleRepriceDecision:
    'Decision on how much to reduce the price of a house if unsold for one month'
    priceChange = pd.DataFrame()

    def __init__(self):
        self.priceChange = zp.Zoopla.matchedAggregated.get_chunk(100)
        #self.priceChange.rename(columns={'\xef\xbb\xbfLISTING ID':'LISTING ID'},inplace=True)

pd.set_option('display.max_columns',1000)
pd.set_option('display.max_rows',1000)
decision = SaleRepriceDecision()
#print decision.priceChange.columns
print decision.priceChange[[zp.Zoopla.IDField,'CREATED','DELETED']]
