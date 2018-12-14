Datasets.py - Defines classes Dataset, LargeDataset, ZooplaBase and its derivates for reading and storing Zoopla data (ZooplaRawDaily, ZooplaRawCollated, ZooplaMatchedDaily, ZooplaMatchedCollated, ZooplaMatchedAggregated, ZooplaMatchedRegistry). Apart from Zoopla related classes, this file also contains classes for English Housing Survey, Product Sales Data, and the same classes as defined at HousePriceIndex.py. Finally, it adds an extra class for reading HPI monthly data provided by Katie Low and stored at Housing ABM/Data/House price data/HPIMonthly.csv.

Zoopla.py - Defines the class Zoopla, which, at creation, reads from csv files all Zoopla data (from Y:Housing ABM/Data/Zoopla/New Zoopla).

HousePriceIndex.py - Defines classes QuarterlyTable, Nationwide, NationwideSeasonal, HalifaxSeasonal, HPISeasonal (tries to access local data at /home/daniel/...).

temp.py - Copy of HousePriceIndex.py.

Tools.py - Defines classes DiscountDistribution, PriceCalc and methods plotProbability, ZooplaPriceChanges.

Tools_MHedit.py - Small edits over Tools.py probably writen by Marc Hinterschweiger.

Tools2_MHedit.py - Small edits over Tools.py probably writen by Marc Hinterschweiger.

SaleReprice.py - Defines classes DiscountDistribution, PriceCalc and methods plotProbability (so far the same classes and methods as in Tools.py), CalculatePriceChanges (different from ZooplaPriceChanges method within Tools.py). Finally, several variables related with price changes are printed to screen and plotted.

SaleRepriceTest.py - This appears to have little or nothing to do with SaleReprice.py. It seems to define a class and some code to simply print data to screen, but doesn't do much.

SaleReprice2MH.py - This appears to be a small variation over SaleRepriceTest.py.

RentalPrice.py - A single class to study the decision on how much to spend on rent.

InitialSalePrice.py - Methods and statements to study initial sale price decision (its dependence on average prices, markup and average days on market).

----------------------------------------------------------------------------------------------------

CHANGES IN THESE FILES WITH RESPECT TO LATEST GITHUB VERSIONS

SaleReprice2MH.py - Absent at github.

Tools.py - Small but apparently relevant changes (additions) to code.

Tools2_MHedit.py - Absent at github.

Tools_MHedit.py - Absent at github.

Zoopla.py - Small change of addresses for accessing Zoopla data.

----------------------------------------------------------------------------------------------------

NEW FILES AT LATEST GITHUB VERSIONS

incomeSavings.py - Plots EHS survey data (presumably income vs savings).

Tenure.py - Plots EHS survey data (presumably a histogram of rental tenure lenghts).

Longitudinal.py - Reads a table from Understanding Society data.

plottest.py - Tests 3D plotting with some randomly created data.

Optimise.py - Reads data from pdfRentalPrice.hd5 and finds an optimal fit before printing and plotting the corresponding results.