# -*- coding: utf-8 -*-
"""
Class to study the initial ask price by households when they bring their properties to the market. This is the code used
to create the file "InitialSaleMarkUpDist.csv". It uses a summary of Zoopla sales data created with the code at
"ZooplaSalesSummaryCreator.py". Note that this latter code uses a back-projected equilibrium initial price for each
property, based on its actual sale price and the evolution of the HPI between the initial listing date and the listing
removal date. In this way, the markup is defined as the actual initial listing price divided by this back-projected
price.

@author: Adrian Carro
"""

from __future__ import division
import pandas as pd
import numpy as np
import scipy.stats as stats


# For proper column printing
pd.set_option('display.max_rows', None)
pd.set_option("display.max_columns", None)

# Read local Zoopla data summary
filtered_data = pd.read_csv(r"./ZooplaSalesSummary.csv", usecols=["MARKUP", "LOG PRICE MARKUP"])
# Convert float columns to this type
filtered_data = filtered_data.astype({"MARKUP": float, "LOG PRICE MARKUP": float})
# Keep only listings with reasonable mark-ups (coherent mark-ups and log mark-ups are selected here simultaneously)
filtered_data = filtered_data[(filtered_data["MARKUP"] > 0.5) & (filtered_data["MARKUP"] < 2.0)]

# Create logarithmic bins to accommodate the log mark-ups, histogram the data and find the log heights of the histogram
myLogBinEdges = np.linspace(np.log(0.5), np.log(2.0), 61, endpoint=True)
hist, bins = np.histogram(filtered_data["LOG PRICE MARKUP"], bins=myLogBinEdges, density=True)
log_hist = np.log(hist)
myLogBinCentres = (myLogBinEdges[:-1] + myLogBinEdges[1:]) / 2.0

# Create linear bins to accommodate the linear mark-ups and histogram the data
myLinBinEdges = np.linspace(0.5, 2.0, 61, endpoint=True)
histLin, binsLin = np.histogram(filtered_data["MARKUP"], bins=myLinBinEdges, density=True)

# Fit, separately, the positive and negative branches of the log-log histogram and plot the results
slope1, intercept1, rvalue1, pvalue1, stderr1 = stats.linregress(myLogBinCentres[myLogBinCentres < 0.0],
                                                                 log_hist[myLogBinCentres < 0.0])
slope2, intercept2, rvalue2, pvalue2, stderr2 = stats.linregress(myLogBinCentres[myLogBinCentres > 0.0],
                                                                 log_hist[myLogBinCentres > 0.0])

# Write statistical and fit information to file...
with open("InitialSalePriceLog.txt", "w") as f:
    f.write("Mean of original individual mark-ups = {}\n".format(filtered_data["MARKUP"].mean()))
    f.write("   ... and log mark-ups = {}\n".format(filtered_data["LOG PRICE MARKUP"].mean()))
    f.write("Std of original individual mark-ups = {}\n".format(filtered_data["MARKUP"].std()))
    f.write("   ... and log mark-ups = {}\n".format(filtered_data["LOG PRICE MARKUP"].std()))
    f.write("For negative values:\nSlope = {}, Intercept = {}\n".format(slope1, intercept1))
    f.write("For positive values:\nSlope = {}, Intercept = {}\n".format(slope2, intercept2))

# Normalise probabilities (in order to write probabilities instead of probability densities)
hist = [x/sum(hist) for x in hist]
# Write probability of log mark-up bins to file
with open("InitialSaleMarkUpDist.csv", "w") as f:
    f.write("# Log Price Mark-Up (lower edge), Log Price Mark-Up (upper edge), Probability\n")
    for lowerEdge, upperEdge, prob in zip(myLogBinEdges[:-1], myLogBinEdges[1:], hist):
        f.write("{}, {}, {}\n".format(lowerEdge, upperEdge, prob))

# Normalise probabilities (in order to write probabilities instead of probability densities)
histLin = [x/sum(histLin) for x in histLin]
# Write probability of lin mark-up bins to file
with open("InitialSaleMarkUpDistLin.csv", "w") as f:
    f.write("# Price Mark-Up (lower edge), Price Mark-Up (upper edge), Probability\n")
    for lowerEdge, upperEdge, prob in zip(myLinBinEdges[:-1], myLinBinEdges[1:], histLin):
        f.write("{}, {}, {}\n".format(lowerEdge, upperEdge, prob))
