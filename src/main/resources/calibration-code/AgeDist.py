# -*- coding: utf-8 -*-
"""
Class to study households' age distribution based on Wealth and Assets Survey data. This is the code used to create file
"Age9-Weighted.csv".

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np


# Read Wealth and Assets Survey data for households
root = r""
chunk = pd.read_csv(root + r"/WAS/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "HRPDVAge9W3", "HRPDVAge15w3"})
pd.set_option('display.max_columns', None)

# List of household variables currently used
# HRPDVAge9W3                 Age of HRP or partner [0-15, 16-24, 25-34, 35-44, 45-54, 55-64, 65-74, 75-84, 85+]
# HRPDVAge15w3                Age of HRP/Partner Banded (15) [0-16, 17-19, 20-24, 25-29, 30-34, 35-39, 40-44, 45-49,
#                             50-54, 55-59, 60-64, 65-69, 70-74, 75-79, 80+]

# Rename columns to be used
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
chunk.rename(columns={"HRPDVAge9W3": "Age9"}, inplace=True)
chunk.rename(columns={"HRPDVAge15w3": "Age15"}, inplace=True)

# Filter down to keep only columns of interest
chunk = chunk[["Age9", "Age15", "Weight"]]

# Map age buckets to middle of bucket value by creating the corresponding dictionary
chunk["Age9"] = chunk["Age9"].map({"16-24": 20, "25-34": 30, "35-44": 40, "45-54": 50, "55-64": 60, "65-74": 70,
                                   "75-84": 80, "85+": 90})
chunk["Age15"] = chunk["Age15"].map({"17-19": 17.5, "20-24": 22.5, "25-29": 27.5, "30-34": 32.5, "35-39": 37.5,
                                     "40-44": 42.5, "45-49": 47.5, "50-54": 52.5, "55-59": 57.5, "60-64": 62.5,
                                     "65-69": 67.5, "70-74": 72.5, "75-79": 77.5, "80+": 82.5})
age9_bin_edges = [15, 25, 35, 45, 55, 65, 75, 85, 95]
age15_bin_edges = [15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85]

# Create a histogram of the data
frequency9, xBins9 = np.histogram(chunk["Age9"].values, bins=age9_bin_edges, density=True,
                                  weights=chunk["Weight"].values)
frequency15, xBins15 = np.histogram(chunk["Age15"].values, bins=age15_bin_edges, density=True,
                                    weights=chunk["Weight"].values)

# Print distributions to file
with open("Age9-Weighted.csv", "w") as f:
    f.write("# Age (lower edge), Age (upper edge), Probability\n")
    for element, lowerEdge, upperEdge in zip(frequency9, xBins9[:-1], xBins9[1:]):
        f.write("{}, {}, {}\n".format(lowerEdge, upperEdge, element))
with open("Age15-Weighted.csv", "w") as f:
    f.write("# Age (lower edge), Age (upper edge), Probability\n")
    for element, lowerEdge, upperEdge in zip(frequency15, xBins15[:-1], xBins15[1:]):
        f.write("{}, {}, {}\n".format(lowerEdge, upperEdge, element))
