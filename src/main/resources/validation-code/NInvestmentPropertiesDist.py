# -*- coding: utf-8 -*-
"""
Class to compare the distribution of investment properties among BTL investors resulting from the model against those
reported on the DCLG Private Landlords Survey from 2010 and the BoE NMG Survey from 2015.

@author: Adrian Carro
"""

# Imports
from __future__ import division
import numpy as np
import matplotlib.pyplot as plt


def readData(file_name):
    _bin_edges = []
    _bin_heights = []
    with open(file_name, "r") as f:
        # Read bins and edges
        for line in f:
            if line[0] != "#":
                _bin_edges.append(float(line.split(",")[0]))
                _bin_heights.append(float(line.split(",")[2]))
        # Add last bin edge, with last bin being artificially assigned a width equal to 10
        _bin_edges.append(_bin_edges[-1] + 10.0)
    # Compute bin widths
    _bin_widths = [(b - a) for a, b in zip(_bin_edges[:-1], _bin_edges[1:])]
    return dict(bin_edges=_bin_edges, bin_heights=_bin_heights, bin_widths=_bin_widths)


def readResults(file_name, _start_time, _end_time):
    """Read micro-data from file_name, structured on a separate line per year. In particular, read from start_year until
    end_year, both inclusive"""
    # Read list of number of investment properties per investor household
    n_investment_properties = []
    with open(file_name, "r") as f:
        for line in f:
            if _start_time <= int(line.split(',')[0]) <= _end_time:
                for column in line.split(',')[1:]:
                    # Keep only households with more than one house, i.e., active BTL investors only
                    if int(column) > 1:
                        n_investment_properties.append(int(column) - 1)
    # Create bin edges, widths and histogram data
    _bin_edges = np.arange(0.0, max(n_investment_properties) + 1.0, 1.0)
    _bin_widths = [(b - a) for a, b in zip(_bin_edges[:-1], _bin_edges[1:])]
    _bin_heights = np.histogram(n_investment_properties, bins=_bin_edges)[0]
    _bin_heights = [element / sum(_bin_heights) for element in _bin_heights]  # Normalise from frequencies to fractions
    return dict(bin_edges=_bin_edges, bin_heights=_bin_heights, bin_widths=_bin_widths)


# Set control variables and addresses for data and results
start_time = 1000
end_time = 2000
rootData = r""  # ADD PATH TO BTL BEHAVIOUR DATA FOLDER
rootResults = r""  # ADD HERE PATH TO RESULTS FOLDER

# Read data distributions
data_DCLG = readData(rootData + r"/DCLG-PLS-BTLHousesPerBTLHousehold.csv")
data_NMG = readData(rootData + r"/BoE-NMG-BTLHousesPerBTLHousehold.csv")

# Read model results
results = readResults(rootResults + r"/test/NHousesOwned-run1.csv", start_time, end_time)

# Plot data and model resulta
plt.bar(data_DCLG["bin_edges"][:-1], height=data_DCLG["bin_heights"], width=data_DCLG["bin_widths"], align="edge",
        label="DCLG data", alpha=0.5, color="b")
plt.bar(data_NMG["bin_edges"][:-1], height=data_NMG["bin_heights"], width=data_NMG["bin_widths"], align="edge",
        label="NMG data", alpha=0.5, color="g")
plt.bar(results["bin_edges"][:-1], height=results["bin_heights"], width=results["bin_widths"], align="edge",
        label="Model results", alpha=0.5, color="r")

# Final plot details
# plt.xlim(min(bin_edges), max(bin_edges))
plt.legend()
plt.title("Distribution of the # of investment properties per BTL household")
plt.show()
