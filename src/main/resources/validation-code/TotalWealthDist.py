# -*- coding: utf-8 -*-
"""
Class to study households' total wealth (financial + housing) distribution, for validation purposes, based on Wealth and
Assets Survey data.

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
                                                                        "DVCISAVW3_aggr", "DVCaCrValW3_aggr",
                                                                        "HPROPWW3", "DVPropertyW3", "DVHValueW3",
                                                                        "DVHseValW3_sum", "DVBltValW3_sum"})

# List of household variables currently used
# HFINWW3_sum                   Gross Financial Wealth (financial assets only)
# HFINWNTW3_sum                 Household Net financial Wealth (financial assets minus financial liabilities)

# Rename the different measures of financial wealth
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
chunk.rename(columns={"HFINWW3_sum": "GrossFinancialWealth"}, inplace=True)
chunk.rename(columns={"HFINWNTW3_sum": "NetFinancialWealth"}, inplace=True)
chunk["LiqFinancialWealth"] = chunk["DVFNSValW3_aggr"].astype(float) + chunk["DVCACTvW3_aggr"].astype(float)\
                           + chunk["DVCASVVW3_aggr"].astype(float) + chunk["DVSaValW3_aggr"].astype(float)\
                           + chunk["DVCISAVW3_aggr"].astype(float) + chunk["DVCaCrValW3_aggr"].astype(float)
# Rename the different measures of housing wealth
chunk.rename(columns={"HPROPWW3": "NetPropertyWealth"}, inplace=True)
chunk.rename(columns={"DVPropertyW3": "GrossPropertyWealth"}, inplace=True)
chunk["GrossHousingWealth"] = chunk["DVHValueW3"].astype(float) + chunk["DVHseValW3_sum"].astype(float)\
                           + chunk["DVBltValW3_sum"].astype(float)
# Filter down to keep only these columns
chunk = chunk[["Weight", "NetFinancialWealth", "GrossFinancialWealth", "LiqFinancialWealth",
               "NetPropertyWealth", "GrossPropertyWealth", "GrossHousingWealth"]]

# Create wealth bins for histograms
min_wealth_bin = 2.0
max_wealth_bin = 16.0
wealth_bin_edges = np.linspace(min_wealth_bin, max_wealth_bin, 57)

# For each combination of housing wealth and financial wealth measures...
financial_wealth_measures = ["NetFinancialWealth", "GrossFinancialWealth", "LiqFinancialWealth"]
housing_wealth_measures = ["NetPropertyWealth", "GrossPropertyWealth", "GrossHousingWealth"]
for financial_wealth_measure in financial_wealth_measures:
    for housing_wealth_measure in housing_wealth_measures:
        # ...add total wealth column
        chunk["TotalWealth"] = chunk[financial_wealth_measure].astype(float)\
                               + chunk[housing_wealth_measure].astype(float)
        # For the sake of using logarithmic scales, filter out any zero and negative values
        temp_chunk = chunk[(chunk["TotalWealth"] > 0.0)]
        # ...create a histogram
        frequency = np.histogram(np.log(temp_chunk["TotalWealth"].values), bins=wealth_bin_edges, density=True,
                                 weights=temp_chunk["Weight"].values)[0]
        # ...and print the distribution to a file
        with open(financial_wealth_measure + "-" + housing_wealth_measure + "-Weighted.csv", "w") as f:
            f.write("# Log Total Wealth (" + financial_wealth_measure + " + " + housing_wealth_measure
                    + ") (lower edge), Log Total Wealth (" + financial_wealth_measure + " + " + housing_wealth_measure
                    + ") (upper edge), Probability\n")
            for element, wealthLowerEdge, wealthUpperEdge in zip(frequency, wealth_bin_edges[:-1],
                                                                 wealth_bin_edges[1:]):
                f.write("{}, {}, {}\n".format(wealthLowerEdge, wealthUpperEdge, element))
