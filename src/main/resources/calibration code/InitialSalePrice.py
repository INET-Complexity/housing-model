# -*- coding: utf-8 -*-
"""
Class to study the initial ask price by households when they bring their properties to the market. It uses data from
Katie Low's HPI and Zoopla data

@author: daniel, Adrian Carro
"""

import Datasets as ds
import matplotlib.pyplot as plt
from datetime import datetime
import numpy as np
import scipy.stats as ss
import pandas as pd


def back_projected_price(back_date, current_date, current_price):
    """
    Compute price at a previous date (back_date) by projecting back the HPI evolution over the price (current_price) at
    a more recent date (current_date)
    """
    if back_date == current_date:
        return current_price
    else:
        current_hpi = hpi.HPIat(current_date)
        back_hpi = hpi.HPIat(back_date)
        return current_price * back_hpi / current_hpi


def markup(row):
    """
    Function that acts on individual listings (rows) computing the markup of the initial listing ask price with respect
    to the theoretical price projected back from the latest listed price assuming the same behaviour as the HPI
    - CREATED: Listing creation date
    - INITIAL PRICE: First asking price stipulated on the listing
    - LATEST SOLD: Latest date that the listing had a status of SOLD
    - PRICE: Latest asking price stipulated on the listing
    """
    return (row["INITIAL PRICE"] / back_projected_price(datetime.strptime(row["CREATED"], "%Y-%m-%d"),
                                                        datetime.strptime(row["LATEST SOLD"], "%Y-%m-%d"),
                                                        row["PRICE"]))

# TODO: Old code to find average days on market
# def average_days_on_market(data, date):
#     dom = [(datetime.strptime(row[1], "%Y-%m-%d") - datetime.strptime(row[0], "%Y-%m-%d")).days for row in
#            data[data['LATEST SOLD'] == date][['CREATED', 'LATEST SOLD']].values]
#     return sum(dom) / len(dom)

# Read HPI monthly data
hpi = ds.HPIMonthly()

# Read Zoopla data
data = ds.ZooplaRawCollated()
chunk = data.read(500000)

# Filter data according to the following columns
# - MARKET: Indicates if the listing is SALE or RENT
# - CREATED: Listing creation date
# - INITIAL PRICE: First asking price stipulated on the listing
# - LATEST SOLD: Latest date that the listing had a status of SOLD
# - PRICE: Latest asking price stipulated on the listing
# First, keep only sale listings with a non-null initial price
chunk = chunk[(chunk["MARKET"] == "SALE") & (np.invert(pd.isnull(chunk["INITIAL PRICE"])))]
# Second, keep only listings with an initial price greater than zero but smaller than £10m
chunk = chunk[(chunk["INITIAL PRICE"].values > 0) & (chunk["INITIAL PRICE"].values < 10000000)]
# Third, keep only listings whose latest status is "SOLD"
chunk = chunk[chunk["STATUS"].values == "SOLD"]
# Fourth, keep only listings whose latest price is non-zero
chunk = chunk[chunk["PRICE"].values > 0]
# Finally, keep only "CREATED", "INITIAL PRICE", "LATEST SOLD", "PRICE", and "STATUS" columns
sold_listings = chunk[["CREATED", "INITIAL PRICE", "LATEST SOLD", "PRICE", "STATUS"]]

# Apply markup function to each row (listing)
markupOnBackProjection = sold_listings.apply(markup, axis=1)

# Plot distribution of mark-ups
plt.figure(figsize=(8, 6))
plt.hist(markupOnBackProjection.values, bins=50, range=(0.5, 2.0), density=True, label="Data")

# Select only mark-ups smaller than 1, that is, corresponding to properties with initial prices smaller than their
# back-projected prices, or rather, properties whose price increased more than the HPI
lower = markupOnBackProjection[(markupOnBackProjection < 0.999) & (markupOnBackProjection > 0.1)].values
# Some statistics about this lower selection
# mean = np.mean(lower)
# sd = np.std(lower)
# prob = lower.size * 1.0 / markupOnBackProjection.values.size
# expfit = ss.expon.fit(-1.0 * lower)
# print "lower mean = ", mean
# print "lower stdev = ", sd
# print "lower prob = ", prob
# print "exponential fit (location, scale) = ", expfit
# Plot exponential fit
# x = np.linspace(0.1, 0.999, 200)
# y = ss.expon.pdf(x, expfit[0], expfit[1])
# plt.plot(x, y)

# Select only mark-ups greater than 1, that is, corresponding to properties with initial prices larger than their
# back-projected prices, or rather, properties whose price increased less than the HPI
upper = markupOnBackProjection[(markupOnBackProjection > 1.01) & (markupOnBackProjection < 1.5)].values

# Some statistics about this upper selection
mean = np.mean(upper)
sd = np.std(upper)
prob = upper.size * 1.0 / markupOnBackProjection.values.size
# expfit = ss.expon.fit(upper, floc=1.0)
expfit = ss.expon.fit(upper)
print "upper mean = ", mean
print "upper stdev = ", sd
print "upper prob = ", prob
print "exponential fit (location, scale) = ", expfit
# Plot exponential fit
x = np.linspace(expfit[0], 2.0, 200)
y = ss.expon.pdf(x, expfit[0], expfit[1])
plt.plot(x, y, label="Exponential fit (loc, scale) = ({:6f}, {:6f})".format(expfit[0], expfit[1]))

# Show plots
plt.yscale("log")
plt.xlabel("Back-projected mark-ups")
plt.ylabel("Probability density")
plt.legend()
plt.show()

# TODO: Old code to study influence of the average days on market
# date = datetime.strptime("2008-10-11", "%Y-%m-%d")
# refdate = datetime.strptime("1900-01-01", "%Y-%m-%d")
#
# soldListings = chunk[
#     (chunk["MARKET"] == "SALE") & (chunk['INITIAL PRICE'].values > 0) & (chunk['INITIAL PRICE'].values < 10000000) & (
#         chunk['STATUS'] == "SOLD")]
# soldListings['StartDate'] = [datetime.strptime(x,"%Y-%m-%d") for x in soldListings['CREATED']]
# soldListings['EndDate'] = [datetime.strptime(x,"%Y-%m-%d") for x in soldListings['LATEST SOLD']]
# plt.figure()
# plt.hist(np.divide(soldListings['PRICE CHANGE'].values*0.1,soldListings['INITIAL PRICE'].values), bins=50,
#          range=(-0.05,-0.01))
# population, xbins, ybins = np.histogram2d(filteredchunk['CREATED'],filteredchunk['INITIAL PRICE'],bins=[50,50])
# plt.figure()
# plt.hist2d(filteredchunk[(filteredchunk['DAY'] < 40500) & (filteredchunk['DAY']>39800)]['DAY'].values,
#            np.log(filteredchunk[(filteredchunk['DAY'] < 40500)
#                                 & (filteredchunk['DAY']>39800)]['INITIAL PRICE'].values),bins=[50,50])
# plt.colorbar()
# plt.show()
# plt.figure()
# plt.hist(np.log(filteredchunk['INITIAL PRICE'].values), bins=50)
# plt.figure()
# plt.hist2d([(datetime.strptime(d, "%Y-%m-%d")-refdate).days for d in soldListings['LATEST SOLD'].unique()],
#            [averageDaysOnMarket(soldListings,d) for d in soldListings['LATEST SOLD'].unique()],bins=(50,50))
