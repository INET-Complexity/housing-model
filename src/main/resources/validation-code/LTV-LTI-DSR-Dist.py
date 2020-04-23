# -*- coding: utf-8 -*-
"""
Class to compare the LTV distributions resulting from the model against those observed on the PSD database.

@author: Adrian Carro
"""

from __future__ import division
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt


def readData(file_name):
    _bin_edges = []
    _bin_heights = []
    with open(file_name, "r") as f:
        # Read bins and edges
        for line in f:
            if line[0] != "#":
                _bin_edges.append(float(line.split(",")[0]))
                _bin_heights.append(float(line.split(",")[2]))
        # Add last bin edge, with last bin being artificially assigned a width equal to the previous bin
        _bin_edges.append(2.0 * _bin_edges[-1] - _bin_edges[-2])
    # Normalise heights from frequencies to fractions (not densities!)
    _bin_heights = [element / sum(_bin_heights) for element in _bin_heights]
    # Compute bin widths
    _bin_widths = [(b - a) for a, b in zip(_bin_edges[:-1], _bin_edges[1:])]
    return _bin_edges, _bin_heights, _bin_widths


# Set household type and addresses for data and results
variable = "DSR"  # Can only be "LTV", "LTI", "DSR
householdType = "HM"  # Can only be "FTB" or "HM"
rootData = r""  # ADD PATH TO PSD DATA FOLDER
rootResults = r""  # ADD HERE PATH TO RESULTS FOLDER

# Read PSD data distributions
bin_edges, bin_heights, bin_widths = readData(rootData + r"/" + variable + "_" + householdType + ".csv")

# Read model results, possible columns are:
# modelTime, houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, buyerId, buyerAge,
# buyerHasBTLGene, buyerMonthlyGrossTotalIncome, buyerMonthlyGrossEmploymentIncome, buyerMonthlyNetEmploymentIncome,
# buyerPostPurchaseBankBalance, buyerCapGainCoeff, mortgageDownpayment, mortgagePrincipal, mortgageMonthlyPayment,
# firstTimeBuyerMortgage, buyToLetMortgage, sellerId, sellerAge, sellerHasBTLGene, sellerMonthlyGrossTotalIncome,
# sellerMonthlyGrossEmploymentIncome, sellerPostPurchaseBankBalance, sellerCapGainCoeff
results = pd.read_csv(rootResults + "/test/SaleTransactions-run1.csv", skipinitialspace=True,
                      usecols={"mortgagePrincipal", "transactionPrice", "firstTimeBuyerMortgage", "buyToLetMortgage",
                               "buyerMonthlyGrossEmploymentIncome", "buyerMonthlyNetEmploymentIncome",
                               "buyerPostPurchaseBankBalance", "buyerHasBTLGene", "mortgageMonthlyPayment"})

# Select mortgages for the selected type of household and always with a principal larger than zero
if householdType == "FTB":
    results = results[results["firstTimeBuyerMortgage"] & results["mortgagePrincipal"] > 0.0]
elif householdType == "HM":
    results = results[~results["firstTimeBuyerMortgage"]
                      & ~results["buyToLetMortgage"]
                      & results["mortgagePrincipal"] > 0.0]

# Add column with LTV, LTI or DSR
if variable == "LTV":
    results[variable] = 100.0 * results["mortgagePrincipal"] / results["transactionPrice"]
elif variable == "LTI":
    results[variable] = results["mortgagePrincipal"] / (12.0 * results["buyerMonthlyGrossEmploymentIncome"])
elif variable == "DSR":
    results[variable] = results["mortgageMonthlyPayment"] / results["buyerMonthlyNetEmploymentIncome"]

# Histogram of model results
hist = np.histogram(results[variable], bins=bin_edges)[0]
hist = hist / sum(hist)

# Plot data
plt.bar(bin_edges[:-1], bin_heights, width=bin_widths, align="edge", alpha=0.5, label="PSD data")

# Plot model results
plt.bar(bin_edges[:-1], hist, width=bin_widths, align="edge", alpha=0.5, label="Model")
# plt.plot([a + b / 2.0 for a, b in zip(bin_edges, bin_widths)], hist, lw=2.0, c="r")

# Final plot details
plt.xlim(min(bin_edges), max(bin_edges))
plt.legend()
plt.title("{} - {}".format(variable, householdType))
plt.show()
