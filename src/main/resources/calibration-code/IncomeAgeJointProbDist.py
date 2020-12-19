# -*- coding: utf-8 -*-
"""
Class to study households' income distribution depending on their age based on Wealth and Assets Survey data. This is
the code used to create file "AgeGrossIncomeJointDist.csv".

@author: Adrian Carro
"""

import pandas as pd
import numpy as np


# Read Wealth and Assets Survey data for households
root = r""  # ADD HERE PATH TO WAS DATA FOLDER
chunk = pd.read_csv(root + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "DVTotGIRw3", "DVTotNIRw3",
                                                                        "DVGrsRentAmtAnnualw3_aggr",
                                                                        "DVNetRentAmtAnnualw3_aggr", "HRPDVAge9W3"})

# List of household variables currently used
# DVTotGIRw3                  Household Gross Annual (regular) income
# DVTotNIRw3                  Household Net Annual (regular) income
# DVGrsRentAmtAnnualw3_aggr   Household Gross annual income from rent
# DVNetRentAmtAnnualw3_aggr   Household Net annual income from rent
# HRPDVAge9W3                 Age of HRP or partner [0-15, 16-24, 25-34, 35-44, 45-54, 55-64, 65-74, 75-84, 85+]

# List of other household variables of possible interest
# HRPDVAge15w3                Age of HRP/Partner Banded (15) [0-16, 17-19, 20-24, 25-29, 30-34, 35-39, 40-44, 45-49,
#                             50-54, 55-59, 60-64, 65-69, 70-74, 75-79, 80+]

# Add column with weights
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
# Add column with total gross income, except rental income (gross)
chunk["GrossNonRentIncome"] = chunk["DVTotGIRw3"] - chunk["DVGrsRentAmtAnnualw3_aggr"]
# Add column with total net income, except rental income (net)
chunk["NetNonRentIncome"] = chunk["DVTotNIRw3"] - chunk["DVNetRentAmtAnnualw3_aggr"]
# Rename column with age as "Age"
chunk.rename(columns={"HRPDVAge9W3": "Age"}, inplace=True)
# Filter down to keep only columns of interest
chunk = chunk[["Age", "GrossNonRentIncome", "NetNonRentIncome", "Weight"]]
# Filter out the 1% with highest GrossNonRentIncome and the 1% with lowest NetNonRentIncome
one_per_cent = int(round(len(chunk.index) / 100))
chunk_ord_by_gross = chunk.sort_values("GrossNonRentIncome")
chunk_ord_by_net = chunk.sort_values("NetNonRentIncome")
max_gross_income = chunk_ord_by_gross.iloc[-one_per_cent]["GrossNonRentIncome"]
min_net_income = chunk_ord_by_net.iloc[one_per_cent]["NetNonRentIncome"]
chunk = chunk[chunk["GrossNonRentIncome"] <= max_gross_income]
chunk = chunk[chunk["NetNonRentIncome"] >= min_net_income]
# Map age buckets to middle of bucket value by creating the corresponding dictionary
chunk["Age"] = chunk["Age"].map({"16-24": 20, "25-34": 30, "35-44": 40, "45-54": 50, "55-64": 60, "65-74": 70,
                                 "75-84": 80, "85+": 90})

# Create a 2D histogram of the data with logarithmic income bins (no normalisation here as we want column normalisation,
# to be introduced when plotting or printing)
income_bin_edges = np.linspace(np.log(min_net_income), np.log(max_gross_income), 26)
age_bin_edges = [15, 25, 35, 45, 55, 65, 75, 85, 95]
frequency_gross = np.histogram2d(chunk["Age"].values, np.log(chunk["GrossNonRentIncome"].values),
                                 bins=[age_bin_edges, income_bin_edges], normed=True, weights=chunk["Weight"].values)[0]
frequency_net = np.histogram2d(chunk["Age"].values, np.log(chunk["NetNonRentIncome"].values),
                               bins=[age_bin_edges, income_bin_edges], normed=True, weights=chunk["Weight"].values)[0]

# Print joint distributions to files
with open("AgeGrossIncomeJointDist.csv", "w") as f:
    f.write("# Age (lower edge), Age (upper edge), Log Gross Income (lower edge), Log Gross Income (upper edge), "
            "Probability\n")
    for line, ageLowerEdge, ageUpperEdge in zip(frequency_gross, age_bin_edges[:-1], age_bin_edges[1:]):
        for element, incomeLowerEdge, incomeUpperEdge in zip(line, income_bin_edges[:-1], income_bin_edges[1:]):
            f.write("{}, {}, {}, {}, {}\n".format(ageLowerEdge, ageUpperEdge, incomeLowerEdge, incomeUpperEdge,
                                                  element / sum(line)))
with open("AgeNetIncomeJointDist.csv", "w") as f:
    f.write("# Age (lower edge), Age (upper edge), Log Net Income (lower edge), Log Net Income (upper edge), "
            "Probability\n")
    for line, ageLowerEdge, ageUpperEdge in zip(frequency_net, age_bin_edges[:-1], age_bin_edges[1:]):
        for element, incomeLowerEdge, incomeUpperEdge in zip(line, income_bin_edges[:-1], income_bin_edges[1:]):
            f.write("{}, {}, {}, {}, {}\n".format(ageLowerEdge, ageUpperEdge, incomeLowerEdge, incomeUpperEdge,
                                                  element / sum(line)))
