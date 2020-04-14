# -*- coding: utf-8 -*-
"""
Class to study the probability of a household becoming a buy-to-let investor depending on its income percentile, based
on Wealth and Assets Survey data. This is the code used to create the file "BTLProbabilityPerIncomePercentileBin.csv".

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
from scipy import stats

# List of household variables currently used
# DVTotGIRw3                  Household Gross Annual (regular) income
# DVTotNIRw3                  Household Net Annual (regular) income
# DVGrsRentAmtAnnualw3_aggr   Household Gross Annual income from rent
# DVNetRentAmtAnnualw3_aggr   Household Net Annual income from rent

# Read Wealth and Assets Survey data for households
root = r""  # ADD HERE PATH TO WAS DATA FOLDER
chunk = pd.read_csv(root + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "DVTotGIRw3", "DVTotNIRw3",
                                                                             "DVGrsRentAmtAnnualw3_aggr",
                                                                             "DVNetRentAmtAnnualw3_aggr"})
pd.set_option('display.max_columns', None)

# Rename columns to be used and add all necessary extra columns
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
chunk.rename(columns={"DVTotGIRw3": "GrossTotalIncome"}, inplace=True)
chunk.rename(columns={"DVTotNIRw3": "NetTotalIncome"}, inplace=True)
chunk.rename(columns={"DVGrsRentAmtAnnualw3_aggr": "GrossRentalIncome"}, inplace=True)
chunk.rename(columns={"DVNetRentAmtAnnualw3_aggr": "NetRentalIncome"}, inplace=True)
chunk["GrossNonRentIncome"] = chunk["GrossTotalIncome"] - chunk["GrossRentalIncome"]
chunk["NetNonRentIncome"] = chunk["NetTotalIncome"] - chunk["NetRentalIncome"]

# Filter out the 1% with highest GrossTotalIncome and the 1% with lowest NetTotalIncome
one_per_cent = int(round(len(chunk.index) / 100))
chunk_ord_by_net = chunk.sort_values("NetTotalIncome")
chunk_ord_by_gross = chunk.sort_values("GrossTotalIncome")
min_net_total_income = chunk_ord_by_net.iloc[one_per_cent]["NetTotalIncome"]
max_gross_total_income = chunk_ord_by_gross.iloc[-one_per_cent]["GrossTotalIncome"]
chunk = chunk[chunk["NetTotalIncome"] >= min_net_total_income]
chunk = chunk[chunk["GrossTotalIncome"] <= max_gross_total_income]

# Compute income percentiles (using gross non-rent income) of all households
chunk["GrossNonRentIncomePercentile"] = [stats.percentileofscore(chunk["GrossNonRentIncome"].values, x, "weak")
                                         for x in chunk["GrossNonRentIncome"]]

# Write to file probability of being a BTL investor for each percentile bin (of width 1%)
with open("BTLProbabilityPerIncomePercentileBin.csv", "w") as f:
    f.write("# Gross non-rental income percentile (lower edge), gross non-rental income percentile (upper edge), "
            "BTL probability\n")
    for a in range(100):
        n_total = len(chunk[(a < chunk["GrossNonRentIncomePercentile"])
                            & (chunk["GrossNonRentIncomePercentile"] <= a + 1.0)])
        n_BTL = len(chunk[(a < chunk["GrossNonRentIncomePercentile"])
                          & (chunk["GrossNonRentIncomePercentile"] <= a + 1.0)
                          & (chunk["GrossRentalIncome"] > 0.0)])
        f.write("{}, {}, {}\n".format(a/100, (a + 1)/100, n_BTL/n_total))
