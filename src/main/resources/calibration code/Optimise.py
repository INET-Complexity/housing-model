# -*- coding: utf-8 -*-
"""
A few tool classes to solve an optimisation problem

@author: daniel, Adrian Carro
"""

import numpy as np
from matplotlib import pyplot as plt
import scipy.optimize as opt


class Solver:
    data = []
    func = 0
    optParams = []
    residuals = []
    xvals = []
    totalResidual = 0.0

    def __init__(self, d, f):
        self.data = d
        self.func = f
        self.optParams = []

    def err(self, parameters):
        out_first = self.data.iat[0, 0]
        e = 0.0
        for row in self.data.values:
            if row[0] == out_first:
                x = row[1:-1]
                y = self.func(parameters, x)
            e += (y - row[0]) * (y - row[0]) * row[2]
        return e

    def calc_residuals(self):
        out_first = self.data.iat[0, 0]
        e = -1.0
        for row in self.data.values:
            if row[0] == out_first:
                x = row[1:-1]
                y = self.func(self.optParams, x)
                if e >= 0.0:
                    self.residuals.append(e)
                    self.totalResidual += e
                    self.xvals.extend(x)
                e = 0.0
            e += (y - row[0]) * (y - row[0]) * row[2]

    def optimize(self, first_guess_params):
        self.optParams = opt.fmin(self.err, first_guess_params)
        self.calc_residuals()
        return self.optParams

    # Plots the 2D pdf as a scatter plot where points' colour represents the probability
    def plot_pdf(self):
        plt.scatter(self.data.iloc[:, 1].values, self.data.iloc[:, 0].values, c=self.data.iloc[:, 2].values)
        xlocs, xlabels = plt.xticks()
        plt.xticks(xlocs[:-1], ['%.2E' % np.exp(x) for x in xlocs[:-1]])
        ylocs, ylabels = plt.yticks()
        plt.yticks(ylocs[1:-1], ['%.2E' % np.exp(x) for x in ylocs[1:-1]])
        plt.xlabel("Weekly rent")
        plt.ylabel("Annual net household income")
        plt.tight_layout()

    # Plots the fitted function
    def plot_func(self, parameters):
        xvals = [x / 10.0 for x in range(80, 115)]
        yvals = [self.func(parameters, x) for x in xvals]
        plt.plot(xvals, yvals)

    # Plots the residuals only
    def plot_residuals(self):
        plt.plot(self.xvals, np.array(self.residuals) * 50.0)
