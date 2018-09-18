REPAIRED

Datasets.py - Functioning. Addresses rewired to read data from Researchhub folders.

RentalPrice.py - Functioning. Addresses rewired, some functions moved from Optimise.py, many repairs to make it work and plot the correct data. Decision needed, as result reported in the paper cannot be reproduced!

Optimise.py - Functioning. Addresses rewired, some functions moved to RentalPrice.py. Data at pdfRentalPrice.hd5 identified as the same as EHSinterview().

ZooplaExplorer.py - Functioning. New class to explore and play around with Zoopla data in general.

IncomeSavings.py - Partially functioning (read errors). Reads income and savings data from EHS. Savings data needs cleaning for this to work and be able to plot savings vs income.

IncomeDistByTenure.py - Functioning. Cleaned and renamed from Tenure.py. Reads income and tenure type data from EHS and plots a histogram of the income distribution for each tenure.

InitialSalePrice.py - Functioning, but doubts on assumptions used. Uses Zoopla and HPI data to project prices from the latest (assumed) sale price back to the initial listing price to compute initial mark-up.

SaleReprice.py - Functioning. Reads Zoopla data and plots probability of no price change per month on market, average price change per month on market and a probability distribution of price changes (independent of month on market).

----------------------------------------------------------------------------------------------------

REMOVED

temp.py - Copy of HousePriceIndex.py.

plottest.py - Unrelated file, tests 3D plotting with some randomly created data.

SaleRepriceTest.py - Irrelevant.

SaleReprice2MH.py - Irrelevant, almost copy of SaleRepriceTest.py.

Zoopla.py - Copy of methods included in Datasets.py.

Longitudinal.py - Irrelevant (only reads data from Understanding Society, which is not even present in any folder).

HousePriceIndex.py - Copy of methods included in Datasets.py.

Tools.py - Old copy of methods included in SaleReprice.py.

Tools_MHedit.py - Irrelevant, copy of Tools.py.

Tools2_MHedit.py - Irrelevant, copy of Tools.py.