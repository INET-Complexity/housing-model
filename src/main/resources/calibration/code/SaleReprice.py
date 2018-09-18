# -*- coding: utf-8 -*-
"""
Defines several classes to study the reprice or price decrease behaviour of households trying to sell their houses. It
uses Zoopla data

@author: daniel, Adrian Carro
"""

import Datasets as ds
import numpy as np
from datetime import datetime
import matplotlib.pyplot as plt
import scipy.stats as stats
import math


class DiscountDistribution:
    """Class to collect and store the distribution of price discounts per month"""

    # Number of months on the market to consider as sample (bins in the x axis for building the pdf)
    x_size = 48  # 4 years
    # For every month in the sample, countNoChange stores the number of properties not experiencing a drop in price
    # between -90% and -0.2%
    countNoChange = np.zeros(x_size)
    # For every month in the sample, countTotal stores the number of properties in the data during that month
    countTotal = np.zeros(x_size)
    # For every month in the sample, changesByMonth stores a list of the logarithmic percent changes (in absolute value)
    # in price of every property experiencing a drop in price between -90% and -0.2%
    changesByMonth = [[] for i in range(x_size)]

    # Need to implement an __init__ method
    def __init__(self):
        pass

    # Record one listing with no change of price between start and end months
    def record_no_change(self, start, end):
        if end >= self.x_size:
            end = self.x_size - 1
        for month in range(start, end + 1):
            self.countNoChange[month] += 1
            self.countTotal[month] += 1

    # Record one listing  with a drop in price between -90% and -0.2% at month
    def record_change(self, start, month, percent):
        if -90 < percent < -0.2:
            self.record_no_change(start, month - 1)  # Record the listing as no change before month
            if month < self.x_size:  # Only record the change if month is within the sample
                self.countTotal[month] += 1
                self.changesByMonth[month].append(math.log(math.fabs(percent)))
        else:
            self.record_no_change(start, month)

    # Probability that price will not change in a given month (given that the property is still on the market)
    def probability_no_change(self):
        return np.divide(self.countNoChange, self.countTotal)

    # Probability that there will be no change per month, integrated over all months
    def probability_no_change_all_time(self):
        return self.probability_no_change().sum() / self.x_size

    # Get a list of all changes, i.e., changesByMonth in a single list instead of a list of lists
    def list_all_changes(self):
        return [x for month in self.changesByMonth for x in month]


class PropertyRecord:
    """Class to function as record of the most recent price, initial price and days on market for a given property"""
    current_price = 0
    initial_market_price = 0
    days_on_market = 0
    last_change_date = 0

    def __init__(self, initial_date, initial_price):
        self.current_price = initial_price
        self.initial_market_price = initial_price
        self.days_on_market = 0
        self.last_change_date = datetime.strptime(initial_date, "%Y-%m-%d")

    def update_price(self, date_string, price):
        new_date = datetime.strptime(date_string, "%Y-%m-%d")
        previous_days_on_market = self.days_on_market
        new_days_on_market = self.days_on_market + (new_date - self.last_change_date).days
        reduction = (price - self.current_price) * 100.0 / self.current_price
        # Previous equation: Discounts were computed as price difference between current and previous price over
        # initial price
        # reduction = (price - self.current_price) * 100.0 / self.initial_market_price
        self.current_price = price
        self.days_on_market = new_days_on_market
        self.last_change_date = new_date
        return previous_days_on_market, new_days_on_market, reduction


def plot_probability(mat):
    """Plot a matrix mat as a colour plot, used for plotting a pdf"""
    plt.figure(figsize=(10, 10))
    im = plt.imshow(mat, origin='low', cmap=plt.get_cmap("jet"))
    plt.colorbar(im, orientation='horizontal')
    plt.show()


def calculate_price_changes(filtered_zoopla_data):
    """Compute and return the discount distribution"""
    distribution = DiscountDistribution()
    dict_of_property_records = {}
    for index, row in filtered_zoopla_data.iterrows():
        # If listing is already at price_map...
        if row["LISTING ID"] in dict_of_property_records:
            # ...recover its PriceCalc object as last_record
            last_record = dict_of_property_records[row["LISTING ID"]]
            # ...store the PriceCalc object previous current_price as old_price
            old_price = last_record.current_price
            # ...update the PriceCalc object with the most recent information (day and price)
            prev_days_on_market, new_days_on_market, reduction = last_record.update_price(row["DAY"], row["PRICE"])
            # If price has not changed, then record the no change to the DiscountDistribution
            if old_price == row["PRICE"]:
                distribution.record_no_change(prev_days_on_market / 30, new_days_on_market / 30)
            # Otherwise, record the change to the DiscountDistribution
            else:
                distribution.record_change(prev_days_on_market / 30, new_days_on_market / 30, reduction)
        # Otherwise, add PriceCalc object of the listing to price_map
        else:
            dict_of_property_records[row["LISTING ID"]] = PropertyRecord(row["DAY"], row["PRICE"])
    return distribution


# Read and filter data from Zoopla
data = ds.ZooplaMatchedDaily()
chunk = data.read(200000)
filtered_chunk = chunk[(chunk["MARKET"] == "SALE") & (chunk["PRICE"] > 0)][["LISTING ID", "DAY", "PRICE"]]

# Compute probability distribution of price discounts
dist = calculate_price_changes(filtered_chunk)

# Plot probability of no change per month on market
print "Average probability of no change per month"
print dist.probability_no_change().sum() / dist.probability_no_change().size
print "Probability of no change per month"
print dist.probability_no_change()
plt.figure()
plt.plot(dist.probability_no_change())
plt.xlabel("Months on market")
plt.ylabel("Probability of no price change")

# Plot average price discount per month on market
mean, sd = stats.norm.fit(dist.list_all_changes())
monthlyMeans = [stats.norm.fit(dist.changesByMonth[i])[0] for i in range(dist.x_size)]
print "Best mean and standard deviation of percentage change per month given change"
print mean, sd
print "Monthly Means"
print monthlyMeans
plt.figure()
plt.plot(monthlyMeans)
plt.xlabel("Months on market")
plt.ylabel("Percent discount")

# Plot probability distribution of price discounts (independent of month on market)
curve = [stats.norm.pdf(i * 0.05, mean, sd) for i in range(-35, 100)]
plt.figure()
plt.hist(dist.list_all_changes(), bins=50, density=True, label="Data")
plt.plot([i * 0.05 for i in range(-35, 100)], curve, label="Normal fit")
plt.xlabel("Percent discount")
plt.ylabel("Probability")
plt.legend()

plt.show()
