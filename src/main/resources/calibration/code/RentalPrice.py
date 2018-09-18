# -*- coding: utf-8 -*-
"""
Class to study households' decision on how much to spend on rent based on English Housing Survey data.

@author: daniel, Adrian Carro
"""

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import Datasets as ds
import Optimise as opt


def column_normalise(matrix):
    """Function to normalise to 1 the sum of the values of the columns of a matrix given as entry"""
    col_sums = matrix.sum(axis=0)
    col_sums = np.array([x if x > 0 else 1.0 for x in col_sums])  # Solve for zeros (replace by 1 for normalisation)
    probability = matrix / col_sums[np.newaxis, :]
    return probability


class RentalPriceDecision:
    """
    Decision on how much to spend on rent. This class brings in English Housing Survey data from the Datasets class,
    filters the data and creates the corresponding histograms.
    """
    # Choose income field between:
    # - Hhincx, EHS Basic Income (annual net household income (HRP + Partner) including savings). The EHS Basic Income
    # refers to the annual net income of the Household Reference Person (HRP) and any partner from wages, pensions,
    # other private sources, savings and state benefits. Amounts are net i.e. after the deduction of Tax and National
    # Insurance where applicable. This income variable does not include any housing related benefits/allowances.
    # - HYEARGRx, household gross annual income (inc. income from all adult household members). An extension of the
    # gross income of the HRP and any partner. This variable represents the household gross income of ALL adults living
    # within the household.
    incomeField = "hhincx"  # "HYEARGRx"
    # incomeField = "HYEARGRx"  # "hhincx"
    renterData = pd.DataFrame()
    population = []
    xbins = []
    ybins = []

    def __init__(self):
        # Bring raw data from Datasets class reader for the English Housing Survey data
        raw_data = ds.EHSinterview()
        # Filter for field rentwkx, total weekly rent payable (rent plus housing benefit)
        income_rent = raw_data.loc[:, [self.incomeField, "rentwkx"]]
        # Filter out non renters and unreasonably large weekly rent values
        self.renterData = income_rent[(income_rent["rentwkx"] > 0) & (income_rent["rentwkx"] < 50000)]
        # Filter out strings at rentwkx column
        self.renterData = self.renterData[self.renterData["rentwkx"].apply(lambda x: not isinstance(x, str))]
        # Cast rentwkx column values as numpy float64 type
        self.renterData = self.renterData.astype({"rentwkx": np.float64})
        # Split the data into a 2D histogram with logarithmic bins (no normalisation here as we want column
        # normalisation, to be introduced when plotting)
        self.population, self.xbins, self.ybins = np.histogram2d(np.log(self.renterData[self.incomeField].values),
                                                                 np.log(self.renterData["rentwkx"].values),
                                                                 bins=[30, 30])
        # Transpose the matrix as histogram2d returns a list of columns instead of a list of rows
        self.population = self.population.T

    # Plot actual values as (net annual income, weekly rent) points
    def plot_values(self):
        plt.scatter(self.renterData[self.incomeField], self.renterData["rentwkx"])
        plt.xlabel("Annual net household income")
        plt.ylabel("Weekly rent")
        plt.xscale("log")
        plt.yscale("log")
        plt.tight_layout()

    # Colour plot of the pdf
    def plot_probability(self):
        # im = plt.imshow(column_normalise(self.population), origin='low', cmap=plt.get_cmap("jet"), aspect="auto",
        #                 extent=[np.min(self.xbins), np.max(self.xbins), np.min(self.ybins), np.max(self.ybins)])
        im = plt.imshow(self.population, origin='low', cmap=plt.get_cmap("jet"), aspect="auto",
                        extent=[np.min(self.xbins), np.max(self.xbins), np.min(self.ybins), np.max(self.ybins)])
        xlocs, xlabels = plt.xticks()
        plt.xticks(xlocs[1:-1], ['%.2E' % np.exp(x) for x in xlocs[1:-1]])
        ylocs, ylabels = plt.yticks()
        plt.yticks(ylocs[:-1], ['%.2E' % np.exp(x) for x in ylocs[:-1]])
        plt.colorbar(im, orientation='horizontal')
        plt.xlabel("Annual net household income")
        plt.ylabel("Weekly rent")
        plt.tight_layout()

    # Plot average weekly rent for each net income bin
    def plot_average(self):
        # Centers of the weekly rent bins
        weekly_rent_bin_centers = np.array(self.ybins[1:] + self.ybins[:-1])/2.0
        # Centers of the net income bins
        net_income_bin_centers = np.array(self.xbins[1:] + self.xbins[:-1])/2.0
        # Array with the average weekly rent for each income bin (dot product of the weekly rent array times the column
        # normalised probability density of the 2D histogram)
        average_rent_per_income_bin = np.dot(weekly_rent_bin_centers, column_normalise(self.population))
        # plt.plot(net_income_bin_centers, average_rent_per_income_bin)
        # plt.xlabel("ln(Annual net household income)")
        # plt.ylabel("ln(Weekly rent)")
        plt.plot(np.exp(net_income_bin_centers) / 12.0, np.exp(average_rent_per_income_bin) * 4.0)
        xmin = min(np.exp(net_income_bin_centers) / 12.0)
        xmax = max(np.exp(net_income_bin_centers) / 12.0)
        plt.plot(np.arange(xmin, xmax), [(x/3.0) for x in np.arange(xmin, xmax)])
        plt.xlabel("Monthly net household income")
        plt.ylabel("Monthly rent")
        plt.tight_layout()

    # Plot average weekly rent for each net income bin + original data + linear fit + piecewise linear fit
    def plot_average_and_values(self):
        # Centers of the weekly rent bins
        weekly_rent_bin_centers = np.array(self.ybins[1:] + self.ybins[:-1])/2.0
        # Centers of the net income bins
        net_income_bin_centers = np.array(self.xbins[1:] + self.xbins[:-1])/2.0
        # Array with the average weekly rent for each income bin (dot product of the weekly rent array times the column
        # normalised probability density of the 2D histogram)
        average_rent_per_income_bin = np.dot(weekly_rent_bin_centers, column_normalise(self.population))
        # Plot data
        plt.scatter(self.renterData[self.incomeField] / 12.0, self.renterData["rentwkx"] * 4.0, c="blue", label="Data")
        # Plot average rent for every income bin
        plt.plot(np.exp(net_income_bin_centers) / 12.0, np.exp(average_rent_per_income_bin) * 4.0, c="green", label="Av. rent per income bin")
        # plot 1/3 slope line
        xmin = min(np.exp(net_income_bin_centers) / 12.0)
        xmax = max(np.exp(net_income_bin_centers) / 12.0)
        plt.plot(np.arange(xmin, xmax), [(x/3.0) for x in np.arange(xmin, xmax)], c="orange", label="x/3 linear fit")
        plt.xlabel("Monthly net household income")
        plt.ylabel("Monthly rent")
        plt.tight_layout()


def linear_func(parameters, x):
    """Linear function"""
    return parameters[0] * x + parameters[1]


def piecewise_func(parameters, x):
    """
    Defines the piecewise function
    f = { b                 if x < a
        { c*(x - a) + b     if x >= a
    where a, b and c are parameters given by parameters[0], [1] and [2] respectively
    """
    if x < parameters[0]:
        return parameters[1]
    return parameters[2] * (x - parameters[0]) + parameters[1]


# Plots the fitted function
def plot_piecewise_function(parameters):
    """Instrumental function to plot the piecewise function fit"""
    xvals = [x / 10.0 for x in range(50, 91)]
    yvals = [piecewise_func(parameters, x) for x in xvals]
    plt.plot(np.exp(xvals), np.exp(yvals), c="red", label="Piecewise function")


rentPrice = RentalPriceDecision()

# plt.figure(figsize=(7, 7))
# rentPrice.plot_values()
# plt.figure(figsize=(7, 7))
# rentPrice.plot_probability()
# plt.figure(figsize=(7, 7))
# rentPrice.plot_average()
# plt.figure(figsize=(7, 7))
rentPrice.plot_average_and_values()

# np.set_printoptions(threshold=np.nan)
# print rentPrice.renterData[rentPrice.renterData[rentPrice.incomeField] < 3000]
# print len(rentPrice.renterData[rentPrice.renterData[rentPrice.incomeField] < 3000])
# print rentPrice.renterData[rentPrice.renterData["rentwkx"] < 10]
# print len(rentPrice.renterData[rentPrice.renterData["rentwkx"] < 10])

# Read data
data = pd.read_hdf("pdfRentalPrice.hd5")
# Create new solver object passing the data and the particular functional form to use
mySolver = opt.Solver(data, piecewise_func)
# Transform weekly to monthly rent
data["rental price"] += np.log(4.0)
# Transform annual to monthly income
data["income"] -= np.log(12.0)
# Fit
params = mySolver.optimize([10.0, 4.5, 0.2])

# plt.figure(figsize=(9, 7))
# mySolver.plot_pdf()
# plt.figure(figsize=(9, 7))
plot_piecewise_function(params)
# plt.figure(figsize=(7, 7))
# mySolver.plot_residuals()
plt.legend()
plt.show()
