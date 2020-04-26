# -*- coding: utf-8 -*-
"""
Class to study households' financial wealth distribution, for validation purposes, based on Wealth and Assets Survey
data.

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt


def readResults(file_name, _start_time, _end_time):
    """Read micro-data from file_name, structured on a separate line per year. In particular, read from start_year until
    end_year, both inclusive"""
    # Read list of float values, one per household
    data_float = []
    with open(file_name, "r") as _f:
        for line in _f:
            if _start_time <= int(line.split(',')[0]) <= _end_time:
                for column in line.split(',')[1:]:
                    data_float.append(float(column))
    return data_float


# Set control variables and addresses. Note that available variables to print and plot are "GrossFinancialWealth",
# "NetFinancialWealth", "LiqFinancialWealth"
printResults = False
plotResults = True
start_time = 1000
end_time = 2000
min_log_bin_edge = 0.0
max_log_bin_edge = 20.0
variableToPlot = "LiqFinancialWealth"
rootData = r""  # ADD HERE PATH TO WAS DATA FOLDER
rootResults = r""  # ADD HERE PATH TO RESULTS FOLDER

# Read Wealth and Assets Survey data for households
chunk = pd.read_csv(rootData + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "HFINWW3_sum", "HFINWNTW3_sum",
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

# Define bin edges and widths
number_of_bins = int(max_log_bin_edge - min_log_bin_edge) * 4 + 1
bin_edges = np.logspace(min_log_bin_edge, max_log_bin_edge, number_of_bins, base=np.e)
bin_widths = [b - a for a, b in zip(bin_edges[:-1], bin_edges[1:])]

# If printing data to files is required, histogram data and print results
if printResults:
    for variable in ["GrossFinancialWealth", "NetFinancialWealth", "LiqFinancialWealth"]:
        hist = np.histogram(chunk[variable].values, bins=bin_edges, density=True, weights=chunk["Weight"].values)[0]
        with open("{}-Weighted.csv".format(variable), "w") as f:
            f.write("# Log {} (lower edge), Log {} (upper edge), Probability\n".format(variable, variable))
            for element, wealthLowerEdge, wealthUpperEdge in zip(hist, bin_edges[:-1], bin_edges[1:]):
                f.write("{}, {}, {}\n".format(wealthLowerEdge, wealthUpperEdge, element))

# If plotting data and results is required, read model results, histogram data and results and plot them
if plotResults:
    # Read model results
    results = readResults(rootResults + r"/test/BankBalance-run1.csv", start_time, end_time)
    # Histogram model results
    model_hist = np.histogram([x for x in results if x > 0.0], bins=bin_edges, density=False)[0]
    model_hist = model_hist / sum(model_hist)
    # Histogram data from WAS
    WAS_hist = np.histogram(chunk[chunk[variableToPlot] > 0.0][variableToPlot].values, bins=bin_edges,
                            density=False, weights=chunk[chunk[variableToPlot] > 0.0]["Weight"].values)[0]
    WAS_hist = WAS_hist / sum(WAS_hist)
    # Plot both model results and data from WAS
    plt.bar(bin_edges[:-1], height=model_hist, width=bin_widths, align="edge", label="Model results", alpha=0.5,
            color="b")
    plt.bar(bin_edges[:-1], height=WAS_hist, width=bin_widths, align="edge", label="WAS data", alpha=0.5, color="r")
    # Final plot details
    plt.gca().set_xscale("log")
    plt.xlabel("Liquid Financial Wealth (Bank Balance)")
    plt.ylabel("Frequency (fraction of cases)")
    plt.legend()
    plt.title("Distribution of {}".format(variableToPlot))
    plt.show()
