# -*- coding: utf-8 -*-
"""
Class to count the number of listings of each type in the Zoopla data.

@author: Adrian Carro
"""

import numpy as np
import pandas as pd


# Read data, filtering according to the following columns
# - MARKET: Indicates if the listing is SALE or RENT
# - CREATED: Listing creation date
# - DELETED: Listing deletion date
# - PRICE UPDATES: Number of times that the asking price was updated
root = r""  # ADD HERE PATH TO ZOOPLA DATA FOLDER
chunk_size = 10000
n_rent_listings_non_null_dates = 0
n_sale_listings_non_null_dates = 0
for chunk in pd.read_csv(root + r"\Zoopla\New Zoopla\B Raw Listings (collation).csv", chunksize=chunk_size,
                         usecols=["MARKET", "CREATED", "DELETED"],
                         dtype={"MARKET": str, "CREATED": str, "DELETED": str},
                         engine="c"):
    # Keep only listings with non-null values in the required columns
    chunk = chunk[(np.invert(pd.isnull(chunk["CREATED"])) & np.invert(pd.isnull(chunk["DELETED"])))]
    # Keep separate sale and rent listings
    chunk_rent = chunk[chunk["MARKET"] == "RENT"]
    chunk_sale = chunk[chunk["MARKET"] == "SALE"]
    # Add sizes to counters
    n_rent_listings_non_null_dates += len(chunk_rent.index)
    n_sale_listings_non_null_dates += len(chunk_sale.index)

with open("ListingCounter.csv", "w") as f:
    f.write("Number of rent listings with non-null dates = {}\n".format(
        n_rent_listings_non_null_dates))
    f.write("Number of sale listings with non-null dates = {}\n".format(
        n_sale_listings_non_null_dates))
