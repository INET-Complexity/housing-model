# -*- coding: utf-8 -*-
"""
Created on Tue Apr 14 15:44:04 2015

@author: daniel
"""
import pandas as pd

###### SALE PRICE RECONSIDER DECiSION
class Zoopla:
    CSIZE = 100
    rawDaily    = pd.read_csv("Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\A Raw Listings (daily).csv", chunksize=CSIZE)
    rawCollated = pd.read_csv("Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\B Raw Listings (collation).csv",chunksize=CSIZE)
    matchedDaily = pd.read_csv("Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\C Matched Listings (daily).csv",chunksize=CSIZE)
    matchedCollated = pd.read_csv("Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\D Matched Listings (collation).csv",chunksize=CSIZE)
    matchedAggregated = pd.read_csv("Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\E Matched Listings (aggregation).csv",chunksize=CSIZE)  
    matchedRegistry =  pd.read_csv("Y:\\Housing ABM\\Data\\Zoopla\\New Zoopla\\F Matched Listings (registry).csv",chunksize=CSIZE)
    IDField = '\xef\xbb\xbfLISTING ID'

# rawDaily columns     [u'LISTING ID', u'DAY', u'DIFF', u'ADDRESS', u'PRICE', u'MARKET', u'STATUS', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# rawCollated columns  [u'LISTING ID', u'ADDRESS', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# matchedDaily columns [u'LISTING ID', u'DAY', u'DIFF', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'PRICE', u'MARKET', u'STATUS', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# matchedCollated cols [u'LISTING ID', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'FLOORS']
# matchedAggregated    [u'LISTING ID', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BUILDER', u'DATE BUILT', u'PERIOD BUILT', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'HAS CLOAKROOM', u'HAS CONSERVATORY', u'CENTRAL HEATING FUEL', u'FLOORS', u'HAS GARAGE', u'HAS PARKING', u'HAS DRIVEWAY']
# matchedRegistry      [u'LISTING ID', u'ADDRESS 1', u'ADDRESS 2', u'ADDRESS 3', u'ADDRESS 4', u'ADDRESS 5', u'ADDRESS 6', u'POSTCODE', u'CREATED', u'DELETED', u'INITIAL PRICE', u'PRICE', u'PRICE CHANGE', u'PRICE UPDATES', u'MARKET', u'STATUS', u'STATUS UPDATES', u'LATEST FOR SALE', u'LATEST SALE UNDER OFFER', u'LATEST SOLD', u'LATEST TO RENT', u'LATEST RENT UNDER OFFER', u'LATEST RENTED', u'PROPERTY TYPE', u'TENURE', u'BUILDER', u'DATE BUILT', u'PERIOD BUILT', u'BEDROOMS', u'RECEPTIONS', u'BATHROOMS', u'HAS CLOAKROOM', u'HAS CONSERVATORY', u'CENTRAL HEATING FUEL', u'FLOORS', u'HAS GARAGE', u'HAS PARKING', u'HAS DRIVEWAY', u'LAND REGISTRY UID', u'LAND REGISTRY PRICE', u'LAND REGISTRY DATE', u'LAND REGISTRY DAYS']

#Y:\Housing ABM\Data\Zoopla\New Zoopla