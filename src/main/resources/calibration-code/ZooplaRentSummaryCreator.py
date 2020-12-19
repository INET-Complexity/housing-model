# -*- coding: utf-8 -*-
"""
Class to read, filter and store locally a summary of Zoopla rental data. In particular, this creates the file
"ZooplaRentSummary.csv", which is used later on by InitialRentPrice.py" for studying rental ask price mark-ups.

Note that the code below uses a back-projected equilibrium initial rental price for each property, based on its actual
final rental price and the evolution of the RPI between the initial listing date and the listing removal date. In this
way, the markup is defined as the actual initial listing price divided by this back-projected price.

@author: Adrian Carro
"""

from __future__ import division
from datetime import datetime
from dateutil import relativedelta
import os
import numpy as np
import pandas as pd
import time


# Compute interpolated RPI on a given day
def interpolate_rpi(date):
    date_before = datetime(date.year, date.month, 1)
    date_after = date_before + relativedelta.relativedelta(months=1)
    fraction_of_month = date.day * 1.0 / (date_after - date_before).days
    return ((1.0 - fraction_of_month) * rpi[rpi["DateObj"] == date_before]["RPI (England)"].values[0]
            + fraction_of_month * rpi[rpi["DateObj"] == date_after]["RPI (England)"].values[0])


# Root address for data
root = r""  # ADD HERE PATH TO ZOOPLA DATA FOLDER
rpiRoot = os.path.dirname(os.path.dirname(os.path.abspath(__file__))) + "/supporting-data/HPI and RPI"

start = time.time()

# For proper column printing
pd.set_option('display.max_rows', None)
pd.set_option("display.max_columns", None)

# Read RPI monthly data and create a pre-computed dictionary to return the interpolated RPI value for any given day
interpolated_rpi = dict()
rpi = pd.read_csv(rpiRoot + r"/RPIMonthly.csv")
rpi["DateObj"] = [datetime.strptime(x, "%b-%y") for x in rpi["Date"]]
start_date = datetime(2005, 1, 1)  # This saves computing time, as Zoopla data is only available from 2008
end_date = rpi["DateObj"].iloc[-1]
for single_date in (start_date + relativedelta.relativedelta(days=n) for n in range(int((end_date - start_date).days))):
    interpolated_rpi[single_date] = interpolate_rpi(single_date)

# Read Zoopla data, filtering according to the following columns
# - MARKET: Indicates if the listing is SALE or RENT
# - CREATED: Listing creation date
# - INITIAL PRICE: First asking price stipulated on the listing
# - LATEST RENTED: Latest date that the listing had a status of RENTED
# - PRICE: Latest asking price stipulated on the listing
chunk_size = 10000
filtered_data = pd.DataFrame()
i = 1
for chunk in pd.read_csv(root + r"\Zoopla\New Zoopla\B Raw Listings (collation).csv", chunksize=chunk_size,
                         usecols=["MARKET", "CREATED", "LATEST RENTED", "INITIAL PRICE", "PRICE"],
                         dtype={"MARKET": str, "CREATED": str, "LATEST RENTED": str, "INITIAL PRICE": str, "PRICE": str},
                         engine="c", parse_dates=["CREATED", "LATEST RENTED"]):
    # Keep only sale listings
    chunk = chunk[chunk["MARKET"] == "RENT"]
    # Keep only listings with non-null values in the required columns
    chunk = chunk[(np.invert(pd.isnull(chunk["INITIAL PRICE"])) & np.invert(pd.isnull(chunk["PRICE"]))
                   & np.invert(pd.isnull(chunk["CREATED"])) & np.invert(pd.isnull(chunk["LATEST RENTED"])))]
    # Convert float columns to this type
    chunk = chunk.astype({"INITIAL PRICE": float, "PRICE": float})
    # Keep only listings with an initial and a final price greater than zero
    chunk = chunk[(chunk["INITIAL PRICE"].values > 0) & (chunk["PRICE"].values > 0)]
    # Keep only listings with latest rented date later than created date
    chunk = chunk[chunk["CREATED"] < chunk["LATEST RENTED"]]

    # Filter out first month of data so as to be able to use it as previous month for average days on market later on.
    # Also, do not use data before 01-01-2005, as no RPI data before that
    chunk = chunk[(chunk["LATEST RENTED"] >= datetime(2005, 1, 1)) & (chunk["LATEST RENTED"] <= datetime(2015, 3, 15))]
    chunk = chunk[(chunk["CREATED"] >= datetime(2005, 1, 1)) & (chunk["CREATED"] <= datetime(2015, 3, 15))]
    # Add column with back projected prices
    chunk["BACK PROJECTED PRICE"] = [row["PRICE"] *
                                     interpolated_rpi[row["CREATED"]] / interpolated_rpi[row["LATEST RENTED"]]
                                     for index, row in chunk.iterrows()]
    # Add column with markup
    chunk["MARKUP"] = [row["INITIAL PRICE"] / row["BACK PROJECTED PRICE"] for index, row in chunk.iterrows()]
    # Add log columns
    chunk["LOG PRICE MARKUP"] = np.log(chunk["MARKUP"])
    # Add filtered chunk to total filtered_data data frame
    filtered_data = pd.concat([filtered_data, chunk])
    if i % 10 == 0:
        print("Reading {} finished".format(i))
    i += 1
end = time.time()
print("Finished reading and processing in {} seconds".format(end - start))

# Write to file
filtered_data.to_csv("./ZooplaRentSummary.csv", index=False, columns=["CREATED", "LATEST RENTED", "INITIAL PRICE",
                                                                      "PRICE", "BACK PROJECTED PRICE", "MARKUP",
                                                                      "LOG PRICE MARKUP"])
