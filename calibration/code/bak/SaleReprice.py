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
    zoopla = zp.Zoopla()

    def __init__(self):
    #self.priceChange = self.zoopla.matchedDaily.get_chunk(10000000) #[["CREATED","DELETED","PRICE","PRICE CHANGE"]]
      
     self.priceChange = self.zoopla.matchedCollated.get_chunk(200000000)

pd.set_option('display.max_columns',1000)
pd.set_option('display.max_rows',1000)
decision = SaleRepriceDecision()
#print decision.priceChange[decision.priceChange["ADDRESS 1"]=="Broadcarr House"]

print decision.priceChange[decision.priceChange[decision.priceChange.columns[0]]==402]

#print decision.priceChange
