# -*- coding: utf-8 -*-
"""
Class to extract different averages from Zoopla data. This is the code used to create the file "ZooplaAverages.csv",
which is then used within "ZooplaSalesSummaryCreator.py" in order to create "ZooplaSalesSummary.csv". This latter, in
turn, is used within "InitialSalePrice.py" in order to create the file "InitialSaleMarkUpDist.csv".

@author: Adrian Carro
"""

from __future__ import division
from datetime import timedelta
import numpy as np
import pandas as pd
import time


def date_range(_date1, _date2, delta):
    for n in range(0, int((_date2 - _date1).days) + 1, delta):
        yield _date1 + timedelta(n)


# For proper column printing
pd.set_option('display.max_rows', None)
pd.set_option("display.max_columns", None)

start = time.time()

# Read data, filtering according to the following columns
# - MARKET: Indicates if the listing is SALE or RENT
# - CREATED: Listing creation date
# - INITIAL PRICE: First asking price stipulated on the listing
# - LATEST SOLD: Latest date that the listing had a status of SOLD
# - PRICE: Latest asking price stipulated on the listing
root = r""  # ADD HERE PATH TO ZOOPLA DATA FOLDER
chunk_size = 10000
filtered_data = pd.DataFrame()
i = 1
for chunk in pd.read_csv(root + r"\New Zoopla\B Raw Listings (collation).csv", chunksize=chunk_size,
                         usecols=["MARKET", "CREATED", "LATEST SOLD", "INITIAL PRICE", "PRICE"],
                         dtype={"MARKET": str, "CREATED": str, "LATEST SOLD": str, "INITIAL PRICE": str, "PRICE": str},
                         parse_dates=["CREATED", "LATEST SOLD"]):
    # Keep only sale listings
    chunk = chunk[chunk["MARKET"] == "SALE"]
    # Keep only listings with non-null values in the required columns
    chunk = chunk[(np.invert(pd.isnull(chunk["INITIAL PRICE"])) & np.invert(pd.isnull(chunk["PRICE"]))
                   & np.invert(pd.isnull(chunk["CREATED"])) & np.invert(pd.isnull(chunk["LATEST SOLD"])))]
    # Convert float columns to this type
    chunk = chunk.astype({"INITIAL PRICE": float, "PRICE": float})
    # Keep only listings with initial and final prices between £50k and £5m
    chunk = chunk[(chunk["INITIAL PRICE"].values > 50000) & (chunk["INITIAL PRICE"].values < 5000000)
                  & (chunk["PRICE"].values > 50000) & (chunk["PRICE"].values < 5000000)]
    # Add column with time (days) on market
    chunk["DAYS ON MARKET"] = [(row["LATEST SOLD"] - row["CREATED"]).days for index, row in chunk.iterrows()]
    # Keep only listings with latest sold date greater than created date, or strictly positive days on market
    chunk = chunk[chunk["DAYS ON MARKET"] > 0]
    # Add filtered chunk to total filtered_data data frame
    filtered_data = pd.concat([filtered_data, chunk])
    if i % 10 == 0:
        print("Reading {} finished".format(i))
    i += 1
end = time.time()
print("Finished reading and processing in {} seconds".format(end - start))

# Find average price and average time (days) on market for properties sold at each date
min_date = min(filtered_data["CREATED"].min(), filtered_data["LATEST SOLD"].min())
max_date = max(filtered_data["CREATED"].max(), filtered_data["LATEST SOLD"].max())
average_initial_price = []
number_of_created = []
average_sale_price = []
average_days_on_market = []
number_of_sales = []
dom = pd.DataFrame()
dom2 = pd.DataFrame()
i = 0
# for date1, date2 in zip(date_range(min_date, max_date, 28),
#                         date_range(min_date + timedelta(27), max_date + timedelta(28), 28)):
for date1, date2 in zip(date_range(min_date, max_date, 7),
                        date_range(min_date + timedelta(6), max_date + timedelta(7), 7)):
    dom = filtered_data[(filtered_data["LATEST SOLD"] >= date1) & (filtered_data["LATEST SOLD"] <= date2)]
    dom2 = filtered_data[(filtered_data["CREATED"] >= date1) & (filtered_data["CREATED"] <= date2)]
    if len(dom.index) > 0:
        average_sale_price.append(sum(dom["PRICE"].values) / len(dom["PRICE"].values))
        average_days_on_market.append(sum(dom["DAYS ON MARKET"].values) / len(dom["DAYS ON MARKET"].values))
        number_of_sales.append(len(dom["PRICE"].values))
    else:
        if len(average_sale_price) > 0:
            average_sale_price.append(average_sale_price[-1])
            average_days_on_market.append(average_days_on_market[-1])
        else:
            average_sale_price.append(0)
            average_days_on_market.append(0)
        number_of_sales.append(0)
    if len(dom2.index) > 0:
        average_initial_price.append(sum(dom2["INITIAL PRICE"].values) / len(dom2["INITIAL PRICE"].values))
        number_of_created.append(len(dom2["INITIAL PRICE"].values))
    else:
        if len(average_initial_price) > 0:
            average_initial_price.append(average_initial_price[-1])
        else:
            average_initial_price.append(0)
        number_of_created.append(0)
end2 = time.time()
print("Finished processing of averages in {} seconds".format(end2 - end))

# Write results to file
with open("ZooplaAverages.csv", "w") as f:
    f.write("# Date, Av Sale Price, Av Days Spent On Market, Number Of Sales, Av Initial Price, Number Of Creations\n")
    for date, avSalePrice, avDaysOnMarket, nSales, avInitialPrice, nCreated in zip(date_range(min_date, max_date, 28),
                                                                                   average_sale_price,
                                                                                   average_days_on_market,
                                                                                   number_of_sales,
                                                                                   average_initial_price,
                                                                                   number_of_created):
        for day in date_range(date, date + timedelta(27), 1):
            ts = pd.to_datetime(str(day))
            f.write("{}, {}, {}, {}, {}, {}\n".format(ts.strftime("%Y-%m-%d"),
                                                      avSalePrice, avDaysOnMarket, nSales, avInitialPrice, nCreated))
