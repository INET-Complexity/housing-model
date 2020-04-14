# -*- coding: utf-8 -*-
"""
Class to study households' bid-up behaviour in both rental and sales markets, based on Zoopla data. In particular, this
code finds the average price increase per price update by computing (“PRICE”/”INITIAL PRICE”)^(1/”PRICE UPDATES) for all
houses with a positive “PRICE CHANGE”. Finally, this code also outputs the distribution of the number of bid-ups, which
is then used for independently output calibrating the DAYS_UNDER_OFFER parameter.

@author: Adrian Carro
"""

import pandas as pd
import numpy as np
import time


root = r""  # ADD HERE PATH TO WAS DATA FOLDER

# For proper column printing
pd.set_option('display.max_rows', None)
pd.set_option("display.max_columns", None)

start = time.time()
# Read data
chunk_size = 10000
filtered_data = pd.DataFrame()
i = 1
for chunk in pd.read_csv(root + r"\New Zoopla\B Raw Listings (collation).csv", chunksize=chunk_size,
                         usecols=["MARKET", "INITIAL PRICE", "PRICE", "PRICE UPDATES"],
                         dtype={"MARKET": str, "INITIAL PRICE": str, "PRICE": str, "PRICE UPDATES": str},
                         engine="c"):
    # Clean and create new required columns
    chunk = chunk[np.invert(pd.isnull(chunk["INITIAL PRICE"]))]
    chunk = chunk[np.invert(pd.isnull(chunk["PRICE"]))]
    chunk["PRICE UPDATES"].fillna(0, inplace=True)
    chunk = chunk.astype({"INITIAL PRICE": float, "PRICE": float, "PRICE UPDATES": int})
    chunk = chunk[chunk["INITIAL PRICE"] > 0.0]
    chunk = chunk[chunk["PRICE"] >= chunk["INITIAL PRICE"]]
    chunk = chunk[chunk["PRICE UPDATES"] > 0.0]
    chunk["AVERAGE BIDUP"] = np.power(chunk["PRICE"] / chunk["INITIAL PRICE"], 1.0 / chunk["PRICE UPDATES"])
    chunk["AVERAGE BIDUP"] = chunk["PRICE"]/chunk["INITIAL PRICE"]
    chunk = chunk[chunk["AVERAGE BIDUP"] < 2.0]

    filtered_data = pd.concat([filtered_data, chunk])
    if i % 10 == 0:
        print("Reading {} finished".format(i))
    i += 1
end = time.time()
print("Finished reading and processing in {} seconds".format(end - start))

# Print results
print("Number of data points: {}".format(len(filtered_data)))
print("Mean")
print(filtered_data[["AVERAGE BIDUP"]].mean())
print("Mode")
print(filtered_data[["AVERAGE BIDUP"]].mode())
print("Median")
print(filtered_data[["AVERAGE BIDUP"]].median())

# Compute and print distribution of the number of bidups
hist, bins = np.histogram(filtered_data["PRICE UPDATES"], bins=np.linspace(0, 15, 16), density=True)
print("Distribution of the number of bidups, from 0 to 15")
print(hist)
