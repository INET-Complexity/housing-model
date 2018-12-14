# -*- coding: utf-8 -*-
"""
Class to study households' saving behaviour based on English Housing Survey data.

@author: daniel, Adrian Carro
"""

import Datasets as ds
import pandas as pd
import matplotlib.pyplot as plt

# Read English Housing Survey interview data
derived = ds.EHSinterview()
# Read English Housing Survey income data
income = ds.EHSincome()
# Merge both Dataframes (join on aacode column, which must be present in both frames, and use intersection of keys)
joined = pd.merge(derived, income, on='aacode', how='inner')
# Keep only HYEARGRx and AmtSvng1b columns:
# - HYEARGRx: household gross annual income (inc. income from all adult household members). An extension of the gross
#   income of the HRP and any partner. This variable represents the household gross income of ALL adults living within
#   the household.
# - AmtSvng1b: Amount of savings/money invested.
#   Value = 1.0 Label = under £1,000
# 	Value = 2.0	Label = £1,000 to £2,999
# 	Value = 3.0	Label = £3,000 to £4,999
# 	Value = 4.0	Label = £5,000 to £5,999
# 	Value = 5.0	Label = £6,000 to £6,999
# 	Value = 6.0	Label = £7,000 to £7,999
# 	Value = 7.0	Label = £8,000 to £11,999
# 	Value = 8.0	Label = £12,000 to £15,999
# 	Value = 9.0	Label = £16,000 to £19,999
# 	Value = 10.0 Label = £20,000 to £29,999
# 	Value = 11.0 Label = £30,000 to £39,999
# 	Value = 12.0 Label = £40,000 to £49,999
# 	Value = 13.0 Label = £50,000 to £99,999
# 	Value = 14.0 Label = £100,000 to £149,999
# 	Value = 15.0 Label = £150,000 and over
# 	Value = -9.0 Label = does not apply
# 	Value = -8.0 Label = no answer
# TODO: Savings information format not correctly read!
data = joined[['HYEARGRx', 'AmtSvng1b']]
print data
exit()

plt.scatter(data['HYEARGRx'], data['AmtSvng1b'])
plt.show()
