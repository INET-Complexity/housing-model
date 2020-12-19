# -*- coding: utf-8 -*-
"""
Class to read, filter and store locally a summary of Zoopla sales data. In particular, this creates the file
"ZooplaSalesSummary.csv", which is used later on by InitialSalePrice.py" for studying ask price mark-ups. In order to
compute some of these summary variables, this code reads from the file "ZooplaAverages.csv", created by the code at
"ZooplaAverager.py".

Note that the code below uses a back-projected equilibrium initial price for each property, based on its actual sale
price and the evolution of the HPI between the initial listing date and the listing removal date. In this way, the
markup is defined as the actual initial listing price divided by this back-projected price.

@author: daniel, Adrian Carro
"""

from __future__ import division
from datetime import datetime, timedelta
from dateutil import relativedelta
import os
import numpy as np
import pandas as pd
import time


# Compute interpolated HPI on a given day
def interpolate_hpi(date):
    date_before = datetime(date.year, date.month, 1)
    date_after = date_before + relativedelta.relativedelta(months=1)
    fraction_of_month = date.day * 1.0 / (date_after - date_before).days
    return ((1.0 - fraction_of_month) * hpi[hpi["DateObj"] == date_before]["HPI"].values[0]
            + fraction_of_month * hpi[hpi["DateObj"] == date_after]["HPI"].values[0])


def back_projected_price(back_date, current_date, current_price):
    """
    Compute price at a previous date (back_date) by projecting back the HPI evolution over the price (current_price) at
    a more recent date (current_date)
    """
    if back_date == current_date:
        return current_price
    else:
        current_hpi = interpolated_hpi[current_date]
        back_hpi = interpolated_hpi[back_date]
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


# Root address for data
root = r""  # ADD HERE PATH TO ZOOPLA DATA FOLDER
hpiRoot = os.path.dirname(os.path.dirname(os.path.abspath(__file__))) + "/supporting-data/HPI and RPI"

start = time.time()

# For proper column printing
pd.set_option('display.max_rows', None)
pd.set_option("display.max_columns", None)

# Read HPI monthly data and create a pre-computed dictionary to return the interpolated HPI value for any given day
interpolated_hpi = dict()
hpi = pd.read_csv(hpiRoot + r"/HPIMonthly.csv")
hpi["DateObj"] = [datetime.strptime(x, "%b-%y") for x in hpi["Date"]]
start_date = datetime(2005, 1, 1)  # This saves computing time, as Zoopla data is only available from 2008
end_date = hpi["DateObj"].iloc[-1]
for single_date in (start_date + relativedelta.relativedelta(days=n) for n in range(int((end_date - start_date).days))):
    interpolated_hpi[single_date] = interpolate_hpi(single_date)

# Read averages from file
averages = pd.read_csv(r"./ZooplaAverages.csv", parse_dates=["# Date"])

# Filter average data
averages = averages[(averages["# Date"] >= datetime(2003, 03, 15)) & (averages["# Date"] < datetime(2015, 03, 15))]

# Create dictionary for average days on market
avDaysOnMarket_dict = dict()
for index, row in averages.iterrows():
    avDaysOnMarket_dict[row["# Date"]] = row[" Av Days Spent On Market"]

# Read data, filtering according to the following columns
# - MARKET: Indicates if the listing is SALE or RENT
# - CREATED: Listing creation date
# - INITIAL PRICE: First asking price stipulated on the listing
# - LATEST SOLD: Latest date that the listing had a status of SOLD
# - PRICE: Latest asking price stipulated on the listing
chunk_size = 10000
filtered_data = pd.DataFrame()
i = 1
for chunk in pd.read_csv(root + r"\New Zoopla\B Raw Listings (collation).csv", chunksize=chunk_size,
                         usecols=["MARKET", "CREATED", "LATEST SOLD", "INITIAL PRICE", "PRICE"],
                         dtype={"MARKET": str, "CREATED": str, "LATEST SOLD": str, "INITIAL PRICE": str, "PRICE": str},
                         engine="c", parse_dates=["CREATED", "LATEST SOLD"]):
    # Keep only sale listings
    chunk = chunk[chunk["MARKET"] == "SALE"]
    # Keep only listings with non-null values in the required columns
    chunk = chunk[(np.invert(pd.isnull(chunk["INITIAL PRICE"])) & np.invert(pd.isnull(chunk["PRICE"]))
                   & np.invert(pd.isnull(chunk["CREATED"])) & np.invert(pd.isnull(chunk["LATEST SOLD"])))]
    # Convert float columns to this type
    chunk = chunk.astype({"INITIAL PRICE": float, "PRICE": float})
    # Keep only listings with an initial and a final price greater than zero
    chunk = chunk[(chunk["INITIAL PRICE"].values > 0) & (chunk["PRICE"].values > 0)]
    # Keep only listings with latest sold date later than created date
    chunk = chunk[chunk["CREATED"] < chunk["LATEST SOLD"]]

    # Filter out first month of data so as to be able to use it as previous month for average days on market later on
    chunk = chunk[(chunk["LATEST SOLD"] >= datetime(2003, 04, 15)) & (chunk["LATEST SOLD"] <= datetime(2015, 03, 15))]
    chunk = chunk[(chunk["CREATED"] >= datetime(2003, 04, 15)) & (chunk["CREATED"] <= datetime(2015, 03, 15))]
    # Keep only properties created on days when at least one property got sold, so as to have an average time on market
    chunk = chunk[chunk.CREATED.isin(avDaysOnMarket_dict.keys())]
    # Add column with back projected prices
    chunk["BACK PROJECTED PRICE"] = [row["PRICE"] *
                                     interpolated_hpi[row["CREATED"]] / interpolated_hpi[row["LATEST SOLD"]]
                                     for index, row in chunk.iterrows()]
    # Add column with markup
    chunk["MARKUP"] = [row["INITIAL PRICE"] / row["BACK PROJECTED PRICE"] for index, row in chunk.iterrows()]
    # Add column with average days on market the week before the sale
    chunk["AV DAYS ON MARKET AT CREATION"] = [avDaysOnMarket_dict[row["CREATED"] - timedelta(7)]
                                              for index, row in chunk.iterrows()]
    # Add log columns
    chunk["LOG AV DAYS ON MARKET AT CREATION"] = np.log(chunk["AV DAYS ON MARKET AT CREATION"])
    chunk["LOG PRICE MARKUP"] = np.log(chunk["MARKUP"])
    # Add filtered chunk to total filtered_data data frame
    filtered_data = pd.concat([filtered_data, chunk])
    if i % 10 == 0:
        print("Reading {} finished".format(i))
    i += 1
end = time.time()
print("Finished reading and processing in {} seconds".format(end - start))

# Write to file
filtered_data.to_csv("./ZooplaSalesSummary.csv", index=False, columns=["CREATED", "LATEST SOLD", "INITIAL PRICE",
                                                                       "PRICE", "BACK PROJECTED PRICE", "MARKUP",
                                                                       "AV DAYS ON MARKET AT CREATION",
                                                                       "LOG PRICE MARKUP",
                                                                       "LOG AV DAYS ON MARKET AT CREATION"])
