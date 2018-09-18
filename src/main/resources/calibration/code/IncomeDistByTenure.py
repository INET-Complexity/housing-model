# -*- coding: utf-8 -*-
"""
Code to read English Housing Survey data about income for different tenures and plot a histogram of it.

@author: daniel, Adrian Carro
"""

import Datasets as ds
import matplotlib.pyplot as plt

# Read data
rawData = ds.EHSinterview()
# Data filtering according to the following fields:
# - Prevten: Previous Tenure (new household; owned outright; buying with a mortgage; owned, unknown if outright or
#   mortgage; council; housing association; privately rented; rented, landlord unknown; household reference person
#   resident 3 years or more; does not apply; no answer)
# - agehrpx: Age of HRP (no answer; does not apply; 95 or over)
# - lenresb: Length of residence (less than 1 year; one year; two years; 3-4 years; 5-9 years; 10-19 years;
#   20-29 years; 30+ years; does not apply; no answer)
# - tenure4: Tenure Group 4 (no answer; owners; social sector; private renters; does not apply)
# - HYEARGRx: Household gross annual income (inc. income from all adult household members). An extension of the gross
#   income of the HRP and any partner. This variable represents the household gross income of ALL adults living within
#   the household (£100,000 or more)
#
# First, select only these 5 columns, and only rows where Prevten does not assume the values "does not apply",
# "no answer" nor "household reference person resident 3 years or more"
data = rawData[['Prevten', 'agehrpx', 'lenresb', 'tenure4', 'HYEARGRx']][(rawData['Prevten'] != "does not apply") &
                                                                         (rawData['Prevten'] != "no answer") &
                                                                         (rawData['Prevten'] != "household reference "
                                                                                                "person resident 3 "
                                                                                                "years or more")]
# data = rawData[['Prevten', 'agehrpx', 'lenresb', 'tenure4', 'HYEARGRx']][(rawData['Prevten'] > 0) &
#                                                                          (rawData['Prevten'] < 9)]
# Replace the label "£100,000 or more" with the value 100000
data["HYEARGRx"].replace(to_replace=".*more$", value=100000, inplace=True, regex=True)

# TODO: Old and devoid of much sense code, to be kept for now and decided upon later
# # Transform labels at lenresb column into (average) values, filtering out "does not apply" and "no answer"
# data = data[(data["lenresb"] != "does not apply") & (data["lenresb"] != "no answer")]
# dictionary = {"less than 1 year": 0.5,
#               "one year": 1,
#               "two years": 2,
#               "3-4 years": 3.5,
#               "5-9 years": 7,
#               "10-19 years": 15,
#               "20-29 years": 25,
#               "30+ years": 30}
# data["lenresb"].replace(dictionary, inplace=True)
#
# # Transform "95 or over" label at agehrpx into 95
# data["agehrpx"].replace({"95 or over": 95}, inplace=True)
#
# # Add new column with age of Household Repr. Person at moment of last move (current age - length of residence)
# data["ageAtMove"] = data['agehrpx'] - data['lenresb']
#
# # Transform labels at lenresb column into original value codes
# dictionary = {"new household": 1,
#               "owned outright": 2,
#               "buying with a mortgage": 3,
#               "owned, unknown if outright or mortgage": 4,
#               "council": 5,
#               "housing association": 6,
#               "privately rented": 7,
#               "rented, landlord unknown": 8}
# data["Prevten"].replace(dictionary, inplace=True)
#
# # Transform labels at tenure4 column into original value codes
# dictionary = {"no answer": -8,
#               "owners": 1,
#               "social sector": 2,
#               "private renters": 3,
#               "does not apply": -9}
# data["tenure4"].replace(dictionary, inplace=True)
#
# # Some absolute nonsense calculation
# data['tenChange'] = ((data['Prevten'] < 5) & (data['Prevten'] > 1)) * 10.0 \
#                     + ((data['Prevten'] > 4) & (data['Prevten'] < 7)) * 20.0 + (data['Prevten'] > 6) * 30.0 \
#                     + data['tenure4']
#
# # Some absolute nonsense filtering of ageAtMove data
# formationData = data[['ageAtMove']][data['tenChange'] < 10]
# formationRentData = data[['ageAtMove']][data['tenChange'] == 3]  # This selects private renters
# moverData = data[['ageAtMove']][(data['tenChange'] > 10) & (data['tenChange'] < 20)]
# rentownData = data[['ageAtMove']][data['tenChange'] == 31]
# fluxes = data[['tenChange']]

# Selecting income values for private renters
incomeFormRent = data[["HYEARGRx"]][data["tenure4"] == "private renters"]
# incomeFormRent = data[['HYEARGRx']][data['tenure4'] == 3]  # Old version
# Selecting income values for socially housed households
incomeFormSoc = data[["HYEARGRx"]][data["tenure4"] == "social sector"]
# incomeFormSoc = data[['HYEARGRx']][data['tenure4'] == 2]  # Old version
# Selecting income values for owners
incomeFormOwn = data[["HYEARGRx"]][data["tenure4"] == "owners"]
# incomeFormOwn = data[['HYEARGRx']][data['tenure4'] == 1]  # Old version

# Plot histogram of data
plt.figure(figsize=(9, 6))
plt.hist([incomeFormSoc.values, incomeFormRent.values, incomeFormOwn.values], bins=20, stacked=False, density=True,
         label=["Social housing", "Private renters", "Owners"], rwidth=0.9)
plt.xlabel("Annual net household income")
plt.ylabel("Density")
plt.legend()
plt.tight_layout()
plt.show()
