# -*- coding: utf-8 -*-
"""
Class to study households' financial wealth distribution, for validation purposes, based on Wealth and Assets Survey
data.

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np


# Read Wealth and Assets Survey data for households
root = r""  # ADD HERE PATH TO WAS DATA FOLDER
chunk = pd.read_csv(root + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "HFINWW3_sum", "HFINWNTW3_sum",
                                                                        "DVFNSValW3_aggr", "DVCACTvW3_aggr",
                                                                        "DVCASVVW3_aggr", "DVSaValW3_aggr",
                                                                        "DVCISAVW3_aggr", "DVCaCrValW3_aggr"})

# List of household variables currently used
# HFINWNTW3_sum               Household Net financial Wealth (financial assets minus financial liabilities)
# HFINWW3_sum                 Gross Financial Wealth (financial assets only)

# Rename the different measures of financial wealth
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
chunk.rename(columns={"HFINWW3_sum": "GrossFinancialWealth"}, inplace=True)
chunk.rename(columns={"HFINWNTW3_sum": "NetFinancialWealth"}, inplace=True)
chunk["LiqFinancialWealth"] = chunk["DVFNSValW3_aggr"].astype(float) + chunk["DVCACTvW3_aggr"].astype(float)\
                           + chunk["DVCASVVW3_aggr"].astype(float) + chunk["DVSaValW3_aggr"].astype(float)\
                           + chunk["DVCISAVW3_aggr"].astype(float) + chunk["DVCaCrValW3_aggr"].astype(float)
# Filter down to keep only financial wealth and total annual gross employee income
chunk = chunk[["GrossFinancialWealth", "NetFinancialWealth", "LiqFinancialWealth", "Weight"]]
# For the sake of using logarithmic scales, filter out any zero and negative values from all wealth columns
chunk = chunk[(chunk["GrossFinancialWealth"] > 0.0) & (chunk["NetFinancialWealth"] > 0.0)
              & (chunk["LiqFinancialWealth"] > 0.0)]

# Create a histogram of the data with logarithmic wealth bins
min_wealth = min(min(chunk["GrossFinancialWealth"]), min(chunk["NetFinancialWealth"]), min(chunk["LiqFinancialWealth"]))
max_wealth = max(max(chunk["GrossFinancialWealth"]), max(chunk["NetFinancialWealth"]), max(chunk["LiqFinancialWealth"]))
wealth_bin_edges = np.linspace(np.log(min_wealth), np.log(max_wealth), 21)
frequency_grossW = np.histogram(np.log(chunk["GrossFinancialWealth"].values), bins=wealth_bin_edges,
                                density=True, weights=chunk["Weight"].values)[0]
frequency_netW = np.histogram(np.log(chunk["NetFinancialWealth"].values), bins=wealth_bin_edges, density=True,
                              weights=chunk["Weight"].values)[0]
frequency_liqW = np.histogram(np.log(chunk["LiqFinancialWealth"].values), bins=wealth_bin_edges, density=True,
                              weights=chunk["Weight"].values)[0]

# Print joint distributions to files
with open("GrossWealthDist-Weighted.csv", "w") as f:
    f.write("# Log Gross Wealth (lower edge), Log Gross Wealth (upper edge), Probability\n")
    for element, wealthLowerEdge, wealthUpperEdge in zip(frequency_grossW, wealth_bin_edges[:-1], wealth_bin_edges[1:]):
        f.write("{}, {}, {}\n".format(wealthLowerEdge, wealthUpperEdge, element))
with open("NetWealthDist-Weighted.csv", "w") as f:
    f.write("# Log Net Wealth (lower edge), Log Net Wealth (upper edge), Probability\n")
    for element, wealthLowerEdge, wealthUpperEdge in zip(frequency_netW, wealth_bin_edges[:-1], wealth_bin_edges[1:]):
        f.write("{}, {}, {}\n".format(wealthLowerEdge, wealthUpperEdge, element))
with open("LiqWealthDist-Weighted.csv", "w") as f:
    f.write("# Log Liq Wealth (lower edge), Log Liq Wealth (upper edge), Probability\n")
    for element, wealthLowerEdge, wealthUpperEdge in zip(frequency_liqW, wealth_bin_edges[:-1], wealth_bin_edges[1:]):
        f.write("{}, {}, {}\n".format(wealthLowerEdge, wealthUpperEdge, element))
