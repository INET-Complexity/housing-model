# -*- coding: utf-8 -*-
"""
Class to study households' income distribution, for validation purposes, based on Wealth and Assets Survey data.

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np


# Read Wealth and Assets Survey data for households
root = r""  # ADD HERE PATH TO WAS DATA FOLDER
chunk = pd.read_csv(root + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "DVTotGIRw3", "DVTotNIRw3",
                                                                        "DVGrsRentAmtAnnualw3_aggr",
                                                                        "DVNetRentAmtAnnualw3_aggr"})

# List of household variables currently used
# DVTotGIRw3                  Household Gross Annual (regular) income
# DVTotNIRw3                  Household Net Annual (regular) income
# DVGrsRentAmtAnnualw3_aggr   Household Gross Annual income from rent
# DVNetRentAmtAnnualw3_aggr   Household Net Annual income from rent

# Rename columns to be used and add all necessary extra columns
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
chunk.rename(columns={"DVTotGIRw3": "GrossTotalIncome"}, inplace=True)
chunk.rename(columns={"DVTotNIRw3": "NetTotalIncome"}, inplace=True)
chunk.rename(columns={"DVGrsRentAmtAnnualw3_aggr": "GrossRentalIncome"}, inplace=True)
chunk.rename(columns={"DVNetRentAmtAnnualw3_aggr": "NetRentalIncome"}, inplace=True)
chunk["GrossNonRentIncome"] = chunk["GrossTotalIncome"] - chunk["GrossRentalIncome"]
chunk["NetNonRentIncome"] = chunk["NetTotalIncome"] - chunk["NetRentalIncome"]

# Filter down to keep only columns of interest
chunk = chunk[["GrossTotalIncome", "NetTotalIncome", "GrossRentalIncome", "NetRentalIncome",
               "GrossNonRentIncome", "NetNonRentIncome", "Weight"]]

# Filter out the 1% with highest GrossTotalIncome and the 1% with lowest NetTotalIncome
one_per_cent = int(round(len(chunk.index) / 100))
chunk_ord_by_net = chunk.sort_values("NetTotalIncome")
chunk_ord_by_gross = chunk.sort_values("GrossTotalIncome")
min_net_total_income = chunk_ord_by_net.iloc[one_per_cent]["NetTotalIncome"]
max_gross_total_income = chunk_ord_by_gross.iloc[-one_per_cent]["GrossTotalIncome"]
chunk = chunk[chunk["NetTotalIncome"] >= min_net_total_income]
chunk = chunk[chunk["GrossTotalIncome"] <= max_gross_total_income]

# Create logarithmic income bins
min_income_bin = 4.0
max_income_bin = 12.25
number_of_bins = int(max_income_bin - min_income_bin)*4 + 1
income_bin_edges = np.linspace(min_income_bin, max_income_bin, number_of_bins)

# Histogram data and print results to files
for name in ["GrossTotalIncome", "NetTotalIncome",
             "GrossRentalIncome", "NetRentalIncome",
             "GrossNonRentIncome", "NetNonRentIncome"]:
    frequency = np.histogram(np.log(chunk[chunk[name] > 0.0][name].values), bins=income_bin_edges,
                             density=True, weights=chunk[chunk[name] > 0.0]["Weight"].values)[0]
    with open(name + "-Weighted.csv", "w") as f:
        f.write("# " + name + " (lower edge), " + name + " (upper edge), Probability\n")
        for element, lowerEdge, upperEdge in zip(frequency, income_bin_edges[:-1], income_bin_edges[1:]):
            f.write("{}, {}, {}\n".format(lowerEdge, upperEdge, element))
