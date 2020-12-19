# -*- coding: utf-8 -*-
"""
Class to study the per month probability of a price decrease for properties on the market (rental and sales depending on
user choice), based on Zoopla data. This code creates a file with two columns: the time on the market from initial
listing to removal and the number of price updates for each property in the Zoopla database. This can be then used to
compute an initial probability of price update per month on the market. However, since the code here does only consider
properties with a non-null time on the market and a non-null number of price updates, this probability needs to be
re-scaled as

Real prob. = # properties with a null * 0.0 + # properties with no null * initial prob. / total # of properties,

where # properties with a null is the number of properties with a null time on market and/or number of price updates and
# properties with no null is the number rof properties with a non-null time on market and a non-null number of price
updates.

@author: Adrian Carro
"""

import numpy as np
from datetime import datetime
import pandas as pd


market = "RENT"  # Must be "RENT" or "SALE"
root = r""  # ADD HERE PATH TO ZOOPLA DATA FOLDER

# Read data, filtering according to the following columns
# - MARKET: Indicates if the listing is SALE or RENT
# - CREATED: Listing creation date
# - DELETED: Listing deletion date
# - PRICE CHANGE: Difference between the first and latest asking prices
# - PRICE UPDATES: Number of times that the asking price was updated
chunk_size = 10000
filtered_data = pd.DataFrame()
for chunk in pd.read_csv(root + r"\New Zoopla\B Raw Listings (collation).csv", chunksize=chunk_size,
                         usecols=["MARKET", "CREATED", "DELETED", "PRICE CHANGE", "PRICE UPDATES"],
                         dtype={"MARKET": str, "CREATED": str, "DELETED": str, "PRICE CHANGE": str,
                                "PRICE UPDATES": str},
                         engine="c"):
    # Keep only sale listings
    chunk = chunk[chunk["MARKET"] == market]
    # Keep only listings with non-null values in the required columns
    chunk = chunk[(np.invert(pd.isnull(chunk["CREATED"])) & np.invert(pd.isnull(chunk["DELETED"]))
                   & np.invert(pd.isnull(chunk["PRICE CHANGE"])) & np.invert(pd.isnull(chunk["PRICE UPDATES"])))]
    # Convert numerical columns to their respective types
    chunk = chunk.astype({"PRICE CHANGE": float})
    chunk = chunk.astype({"PRICE UPDATES": int})
    # Keep only listings with negative price change
    chunk = chunk[chunk["PRICE CHANGE"] < 0.0]
    # Add filtered chunk to total filtered_data data frame
    filtered_data = pd.concat([filtered_data, chunk])

with open(market[0] + market[1:].lower() + "RepriceProbability.csv", "w") as f:
    f.write("Days On Market, Price Updates")
    for index, row in filtered_data.iterrows():
        daysOnMarket = (datetime.strptime(row["DELETED"], "%Y-%m-%d")
                        - datetime.strptime(row["CREATED"], "%Y-%m-%d")).days
        f.write("\n{}, {}".format(daysOnMarket, row["PRICE UPDATES"]))
