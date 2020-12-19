# -*- coding: utf-8 -*-
"""
Class to show that a single personal allowance produces a better fit of the net income of households as a function of
their gross income, based on Wealth and Assets Survey data.

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np


def getNetFromGross(net_income, allowance):
    """Implements tax bands and rates corresponding to the tax year 2011-2012"""
    if net_income <= allowance:
        return net_income
    else:
        net_income_without_allowance = net_income - allowance
        if net_income_without_allowance <= 35000:
            return allowance + net_income_without_allowance * (1 - 0.2)
        elif net_income_without_allowance <= 150000:
            return allowance + 35000 * (1 - 0.2) + (net_income_without_allowance - 35000) * (1 - 0.4)
        else:
            return allowance + 35000 * (1 - 0.2) + (150000 - 35000) * (1 - 0.4)\
                   + (net_income_without_allowance - 150000) * (1 - 0.5)


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

# Compute logarithmic difference between predicted and actual net income for a single personal allowance
singleAllowanceDiff = sum((np.log(getNetFromGross(g, 7475)) - np.log(n))**2
                          for g, n in zip(chunk["GrossTotalIncome"].values, chunk["NetTotalIncome"].values))
# Compute logarithmic difference between predicted and actual net income for a double personal allowance
doubleAllowanceDiff = sum((np.log(getNetFromGross(g, 2 * 7475)) - np.log(n))**2
                          for g, n in zip(chunk["GrossTotalIncome"].values, chunk["NetTotalIncome"].values))
# Print results to screen
print("Logarithmic difference between predicted and actual net income for a single personal allowance {:.2f}"
      .format(singleAllowanceDiff))
print("Logarithmic difference between predicted and actual net income for a double personal allowance {:.2f}"
      .format(doubleAllowanceDiff))
