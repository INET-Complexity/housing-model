# -*- coding: utf-8 -*-
"""
Class to study households' wealth distribution depending on income based on Wealth and Assets Survey data. This is the
code used to create file "GrossIncomeLiqWealthJointDist.csv".

@author: Adrian Carro
"""

import pandas as pd
import numpy as np


# Read Wealth and Assets Survey data for households
root = r""  # ADD HERE PATH TO WAS DATA FOLDER
chunk = pd.read_csv(root + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "DVTotGIRw3", "DVTotNIRw3",
                                                                        "DVGrsRentAmtAnnualw3_aggr",
                                                                        "DVNetRentAmtAnnualw3_aggr", "HFINWW3_sum",
                                                                        "HFINWNTW3_sum", "DVFNSValW3_aggr",
                                                                        "DVCACTvW3_aggr", "DVCASVVW3_aggr",
                                                                        "DVSaValW3_aggr", "DVCISAVW3_aggr",
                                                                        "DVCaCrValW3_aggr"})

# List of household variables currently used
# HFINWNTW3_sum               Household Net financial Wealth (financial assets minus financial liabilities)
# HFINWW3_sum                 Gross Financial Wealth (financial assets only )
# DVTotGIRw3                  Household Gross Annual (regular) income
# DVTotNIRw3                  Household Net Annual (regular) income
# DVGrsRentAmtAnnualw3_aggr   Household Gross annual income from rent
# DVNetRentAmtAnnualw3_aggr   Household Net annual income from rent

# Add column with weights
chunk.rename(columns={"w3xswgt": "Weight"}, inplace=True)
# Add column with total gross income, except rental income (gross)
chunk["GrossNonRentIncome"] = chunk["DVTotGIRw3"] - chunk["DVGrsRentAmtAnnualw3_aggr"]
# Add column with total net income, except rental income (net)
chunk["NetNonRentIncome"] = chunk["DVTotNIRw3"] - chunk["DVNetRentAmtAnnualw3_aggr"]
# Rename the different measures of financial wealth
chunk.rename(columns={"HFINWW3_sum": "GrossFinancialWealth"}, inplace=True)
chunk.rename(columns={"HFINWNTW3_sum": "NetFinancialWealth"}, inplace=True)
chunk["LiqFinancialWealth"] = chunk["DVFNSValW3_aggr"].astype(float) + chunk["DVCACTvW3_aggr"].astype(float)\
                           + chunk["DVCASVVW3_aggr"].astype(float) + chunk["DVSaValW3_aggr"].astype(float)\
                           + chunk["DVCISAVW3_aggr"].astype(float) + chunk["DVCaCrValW3_aggr"].astype(float)
# Filter down to keep only financial wealth and total annual gross employee income
chunk = chunk[["GrossNonRentIncome", "NetNonRentIncome", "GrossFinancialWealth", "NetFinancialWealth",
               "LiqFinancialWealth", "Weight"]]
# Filter out the 1% with highest GrossNonRentIncome and the 1% with lowest NetNonRentIncome
one_per_cent = int(round(len(chunk.index) / 100))
chunk_ord_by_gross = chunk.sort_values("GrossNonRentIncome")
chunk_ord_by_net = chunk.sort_values("NetNonRentIncome")
max_gross_income = chunk_ord_by_gross.iloc[-one_per_cent]["GrossNonRentIncome"]
min_net_income = chunk_ord_by_net.iloc[one_per_cent]["NetNonRentIncome"]
chunk = chunk[chunk["GrossNonRentIncome"] <= max_gross_income]
chunk = chunk[chunk["NetNonRentIncome"] >= min_net_income]
# For the sake of plotting in logarithmic scales, filter out any zero and negative values from all wealth columns
chunk = chunk[(chunk["GrossFinancialWealth"] > 0.0) & (chunk["NetFinancialWealth"] > 0.0)
              & (chunk["LiqFinancialWealth"] > 0.0)]


# Create a 2D histogram of the data with logarithmic income bins (no normalisation here as we want column normalisation,
# to be introduced when plotting or printing) and logarithmic wealth bins
income_bin_edges = np.linspace(np.log(min_net_income), np.log(max_gross_income), 26)
min_wealth = min(min(chunk["GrossFinancialWealth"]), min(chunk["NetFinancialWealth"]), min(chunk["LiqFinancialWealth"]))
max_wealth = max(max(chunk["GrossFinancialWealth"]), max(chunk["NetFinancialWealth"]), max(chunk["LiqFinancialWealth"]))
wealth_bin_edges = np.linspace(np.log(min_wealth), np.log(max_wealth), 21)

frequency_grossI_grossW, xBins, yBins = np.histogram2d(np.log(chunk["GrossNonRentIncome"].values),
                                                       np.log(chunk["GrossFinancialWealth"].values),
                                                       bins=[income_bin_edges, wealth_bin_edges],
                                                       weights=chunk["Weight"].values)
frequency_grossI_netW, xBins, yBins = np.histogram2d(np.log(chunk["GrossNonRentIncome"].values),
                                                     np.log(chunk["NetFinancialWealth"].values),
                                                     bins=[income_bin_edges, wealth_bin_edges],
                                                     weights=chunk["Weight"].values)
frequency_grossI_liqW, xBins, yBins = np.histogram2d(np.log(chunk["GrossNonRentIncome"].values),
                                                     np.log(chunk["LiqFinancialWealth"].values),
                                                     bins=[income_bin_edges, wealth_bin_edges],
                                                     weights=chunk["Weight"].values)
frequency_netI_grossW, xBins, yBins = np.histogram2d(np.log(chunk["NetNonRentIncome"].values),
                                                     np.log(chunk["GrossFinancialWealth"].values),
                                                     bins=[income_bin_edges, wealth_bin_edges],
                                                     weights=chunk["Weight"].values)
frequency_netI_netW, xBins, yBins = np.histogram2d(np.log(chunk["NetNonRentIncome"].values),
                                                   np.log(chunk["NetFinancialWealth"].values),
                                                   bins=[income_bin_edges, wealth_bin_edges],
                                                   weights=chunk["Weight"].values)
frequency_netI_liqW, xBins, yBins = np.histogram2d(np.log(chunk["NetNonRentIncome"].values),
                                                   np.log(chunk["LiqFinancialWealth"].values),
                                                   bins=[income_bin_edges, wealth_bin_edges],
                                                   weights=chunk["Weight"].values)

# Print joint distributions to files
with open("GrossIncomeGrossWealthJointDist.csv", "w") as f:
    f.write("# Log Gross Income (lower edge), Log Gross Income (upper edge), Log Gross Wealth (lower edge), "
            "Log Gross Wealth (upper edge), Probability\n")
    for line, incomeLowerEdge, incomeUpperEdge in zip(frequency_grossI_grossW, xBins[:-1], xBins[1:]):
        for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
            f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                  element / sum(line)))
with open("GrossIncomeNetWealthJointDist.csv", "w") as f:
    f.write("# Log Gross Income (lower edge), Log Gross Income (upper edge), Log Net Wealth (lower edge), "
            "Log Net Wealth (upper edge), Probability\n")
    for line, incomeLowerEdge, incomeUpperEdge in zip(frequency_grossI_netW, xBins[:-1], xBins[1:]):
        for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
            f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                  element / sum(line)))
with open("GrossIncomeLiqWealthJointDist.csv", "w") as f:
    f.write("# Log Gross Income (lower edge), Log Gross Income (upper edge), Log Liq Wealth (lower edge), "
            "Log Liq Wealth (upper edge), Probability\n")
    for line, incomeLowerEdge, incomeUpperEdge in zip(frequency_grossI_liqW, xBins[:-1], xBins[1:]):
        for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
            f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                  element / sum(line)))
with open("NetIncomeGrossWealthJointDist.csv", "w") as f:
    f.write("# Log Net Income (lower edge), Log Net Income (upper edge), Log Gross Wealth (lower edge), "
            "Log Gross Wealth (upper edge), Probability\n")
    for line, incomeLowerEdge, incomeUpperEdge in zip(frequency_netI_grossW, xBins[:-1], xBins[1:]):
        if sum(line) != 0:
            for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
                f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                      element / sum(line)))
        else:
            for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
                f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                      0.0))
with open("NetIncomeNetWealthJointDist.csv", "w") as f:
    f.write("# Log Net Income (lower edge), Log Net Income (upper edge), Log Net Wealth (lower edge), "
            "Log Net Wealth (upper edge), Probability\n")
    for line, incomeLowerEdge, incomeUpperEdge in zip(frequency_netI_netW, xBins[:-1], xBins[1:]):
        if sum(line) != 0:
            for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
                f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                      element / sum(line)))
        else:
            for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
                f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge,
                                                      wealthUpperEdge,
                                                      0.0))
with open("NetIncomeLiqWealthJointDist.csv", "w") as f:
    f.write("# Log Net Income (lower edge), Log Net Income (upper edge), Log Liq Wealth (lower edge), "
            "Log Liq Wealth (upper edge), Probability\n")
    for line, incomeLowerEdge, incomeUpperEdge in zip(frequency_netI_liqW, xBins[:-1], xBins[1:]):
        if sum(line) != 0:
            for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
                f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge, wealthUpperEdge,
                                                      element / sum(line)))
        else:
            for element, wealthLowerEdge, wealthUpperEdge in zip(line, yBins[:-1], yBins[1:]):
                f.write("{}, {}, {}, {}, {}\n".format(incomeLowerEdge, incomeUpperEdge, wealthLowerEdge,
                                                      wealthUpperEdge,
                                                      0.0))
