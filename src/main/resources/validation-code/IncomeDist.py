# -*- coding: utf-8 -*-
"""
Class to study households' income distribution, for validation purposes, based on Wealth and Assets Survey data.

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


# Set control variables and addresses. Note that available variables to print and plot are "GrossTotalIncome",
# "NetTotalIncome", "GrossRentalIncome", "NetRentalIncome", "GrossNonRentIncome" and "NetNonRentIncome"
printResults = False
plotResults = True
start_time = 1000
end_time = 2000
min_log_income_bin_edge = 4.0
max_log_income_bin_edge = 12.25
variableToPlot = "GrossNonRentIncome"
rootData = r""  # ADD HERE PATH TO WAS DATA FOLDER
rootResults = r""  # ADD HERE PATH TO RESULTS FOLDER

# Read Wealth and Assets Survey data for households
chunk = pd.read_csv(rootData + r"/was_wave_3_hhold_eul_final.dta", usecols={"w3xswgt", "DVTotGIRw3", "DVTotNIRw3",
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

# If printing data to files is required, histogram data and print results
if printResults:
    number_of_bins = int(max_log_income_bin_edge - min_log_income_bin_edge) * 4 + 2
    income_bin_edges = np.linspace(min_log_income_bin_edge, max_log_income_bin_edge, number_of_bins)
    income_bin_widths = [b - a for a, b in zip(income_bin_edges[:-1], income_bin_edges[1:])]
    for name in ["GrossTotalIncome", "NetTotalIncome",
                 "GrossRentalIncome", "NetRentalIncome",
                 "GrossNonRentIncome", "NetNonRentIncome"]:
        frequency = np.histogram(np.log(chunk[chunk[name] > 0.0][name].values), bins=income_bin_edges,
                                 density=True, weights=chunk[chunk[name] > 0.0]["Weight"].values)[0]
        with open(name + "-Weighted.csv", "w") as f:
            f.write("# " + name + " (lower edge), " + name + " (upper edge), Probability\n")
            for element, lowerEdge, upperEdge in zip(frequency, income_bin_edges[:-1], income_bin_edges[1:]):
                f.write("{}, {}, {}\n".format(lowerEdge, upperEdge, element))

# If plotting data and results is required, read model results, histogram data and results and plot them
if plotResults:
    # Define bin edges and widths
    number_of_bins = int(max_log_income_bin_edge - min_log_income_bin_edge) * 4 + 2
    income_bin_edges = np.logspace(min_log_income_bin_edge, max_log_income_bin_edge, number_of_bins, base=np.e)
    income_bin_widths = [b - a for a, b in zip(income_bin_edges[:-1], income_bin_edges[1:])]
    # Read model results
    results = readResults(rootResults + r"/test/MonthlyGrossEmploymentIncome-run1.csv", start_time, end_time)
    # Histogram model results
    model_hist = np.histogram([12.0 * x for x in results if x > 0.0], bins=income_bin_edges, density=False)[0]
    model_hist = model_hist / sum(model_hist)
    # Histogram data from WAS
    WAS_hist = np.histogram(chunk[chunk[variableToPlot] > 0.0][variableToPlot].values, bins=income_bin_edges,
                            density=False, weights=chunk[chunk[variableToPlot] > 0.0]["Weight"].values)[0]
    WAS_hist = WAS_hist / sum(WAS_hist)
    # Plot both model results and data from WAS
    plt.bar(income_bin_edges[:-1], height=model_hist, width=income_bin_widths, align="edge",
            label="Model results", alpha=0.5, color="b")
    plt.bar(income_bin_edges[:-1], height=WAS_hist, width=income_bin_widths, align="edge",
            label="WAS data", alpha=0.5, color="r")
    # Final plot details
    plt.gca().set_xscale("log")
    plt.xlabel("Income")
    plt.ylabel("Frequency (fraction of cases)")
    plt.legend()
    plt.title("Distribution of {}".format(variableToPlot))
    plt.show()
