# -*- coding: utf-8 -*-
"""
Class to study households' housing wealth distribution, for validation purposes, based on Wealth and Assets Survey
data.

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np


# Read Wealth and Assets Survey data for households
root = r""  # ADD HERE PATH TO WAS DATA FOLDER
chunk = pd.read_csv(root + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "HPROPWW3", "DVPropertyW3",
                                                                        "DVHValueW3", "DVHseValW3_sum",
                                                                        "DVBltValW3_sum"})

# List of household variables currently used
# HPROPWW3                      Total (net) property wealth (net, i.e., = DVPropertyW3 - HMORTGW3)
# DVPropertyW3                  Total (gross) property wealth (sum of all property values)
# DVHValueW3                    Value of main residence
# DVHseValW3_sum                Total value of other houses
# DVBltValW3_sum                Total value of buy to let houses

# List of other household variables of possible interest: values
# DVBldValW3_sum                Total value of buildings
# DVLUKValW3_sum                Total value of UK land
# DVLOSValW3_sum                Total value of overseas land
# DVOPrValW3_sum                Total value of other property
# Value of all non-residential property = (DVHseValW3_sum + DVBltValW3_sum + DVBldValW3_sum + DVLUKValW3_sum
# + DVLOSValW3_sum + DVOPrValW3_sum

# List of other household variables of possible interest: debts
# TotMortW3                     Total mortgage on main residence
# DVHseDebtW3_sum               Total debt houses not main residence
# DVBLtDebtW3_sum               Total debt buy to let houses
# DVBldDebtW3_sum               Total debt buildings
# DVLUKDebtW3_sum               Total debt UK land
# DVLOSDebtW3_sum               Total debt overseas land
# DVOPrDebtW3_sum               Total debt other property
# OthMortW3_sum                 Total property debt (debt on all non-residential property)
# HMORTGW3                      Value of all mortgages and amount owed


# Rename the different measures of housing wealth
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
chunk.rename(columns={"HPROPWW3": "NetPropertyWealth"}, inplace=True)
chunk.rename(columns={"DVPropertyW3": "GrossPropertyWealth"}, inplace=True)
chunk["GrossHousingWealth"] = chunk["DVHValueW3"].astype(float) + chunk["DVHseValW3_sum"].astype(float)\
                           + chunk["DVBltValW3_sum"].astype(float)

# Filter down to keep only housing wealth
chunk = chunk[["NetPropertyWealth", "GrossPropertyWealth", "GrossHousingWealth", "Weight"]]
# For the sake of using logarithmic scales, filter out any zero and negative values from all wealth columns
chunk = chunk[(chunk["NetPropertyWealth"] > 0.0) & (chunk["GrossPropertyWealth"] > 0.0) &
              (chunk["GrossHousingWealth"] > 0.0)]

# Create wealth bins for histograms
min_wealth_bin = 6.0
max_wealth_bin = 16.0
wealth_bin_edges = np.linspace(min_wealth_bin, max_wealth_bin, 41)

# For each of the measures of housing wealth...
wealth_measures = ["NetPropertyWealth", "GrossPropertyWealth", "GrossHousingWealth"]
for wealth_measure in wealth_measures:
    # ...create a histogram
    frequency = np.histogram(np.log(chunk[wealth_measure].values), bins=wealth_bin_edges, density=True,
                             weights=chunk["Weight"].values)[0]
    # ...and print the distribution to a file
    with open(wealth_measure + "-Weighted.csv", "w") as f:
        f.write("# Log " + wealth_measure + " (lower edge), Log " + wealth_measure + " (upper edge), Probability\n")
        for element, wealthLowerEdge, wealthUpperEdge in zip(frequency, wealth_bin_edges[:-1], wealth_bin_edges[1:]):
            f.write("{}, {}, {}\n".format(wealthLowerEdge, wealthUpperEdge, element))
