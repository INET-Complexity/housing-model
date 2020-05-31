from typing import List

#**************************************************************************************************
#* Class to represent the government, whose only role in the current model is to collect taxes,
#* including both income tax and national insurance contributions
#*
#* @author daniel, Adrian Carro
#*
#*************************************************************************************************/
class Government:
    def __init__(self, Model):
        self.config = Model.config

    # Calculates the income tax due in one year for a given gross annual income, taking into account the dependence of
    # the personal allowance on gross annual income, but not accounting for married couple's allowance
    #
    # @param grossIncome Gross annual income in pounds
    # @return Annual income tax due in pounds
    def incomeTaxDue(self, grossIncome: float) -> float:
        # First, the personal allowance is computed, starting from its general value
        personalAllowance = self.config.GOVERNMENT_GENERAL_PERSONAL_ALLOWANCE
        # If gross annual income is above the income limit for personal allowance...
        if grossIncome > self.config.GOVERNMENT_INCOME_LIMIT_FOR_PERSONAL_ALLOWANCE:
            # ...then £1 is subtracted from the personal allowance for every £2 of income above this income limit, till
            # it reaches 0
            personalAllowance = max(personalAllowance - (grossIncome - self.config.GOVERNMENT_INCOME_LIMIT_FOR_PERSONAL_ALLOWANCE) / 2.0, 0.0)
        # Compute and return tax to be paid based on gross annual income and taking into account the computed personal
        # allowance
        return self.bandedPercentage(grossIncome, data.Government.tax.bands, data.Government.tax.rates, personalAllowance)

    # Calculate the class 1 National Insurance Contributions due on a given gross annual income (under PAYE)
    #
    # Note that, since the untaxed allowance for national insurance contributions is the same for every household, it
    # is already taken into account in the band thresholds, rather than as an untaxed allowance when calling the
    # bandedPercentage method, thus untaxedAllowance is set to zero in that call
    #
    # @param grossIncome Gross annual income in pounds
    # @return Annual class 1 NICs due
    def class1NICsDue(self, grossIncome: float) -> float:
        return self.bandedPercentage(grossIncome, data.Government.nationalInsurance.bands, data.Government.nationalInsurance.rates, 0.0)

    # Calculate a "banded percentage" on a value. A "banded percentage" is a way of calculating a non-linear function,
    # f(x), widely used by HMRC. The domain of values of f(x) is split into bands: from 0 to x1, from x1 to x2, etc.
    # Each band is associated with a percentage p1, p2, etc. The final value of f(x) is the sum of the percentages of
    # each band. So, for example, if x lies somewhere between x1 and x2, f(x) would be p1*x1 + p2*(x - x1)
    #
    # Note that bands are internally shifted to take into account any untaxed allowance. This is used take into account
    # the particular personal allowance of a given household for income tax purposes. Given that the untaxed allowance
    # for national insurance contributions is the same for every household, it is taken into account already in the
    # bands' thresholds, rather than as an untaxed allowance within the call to this method
    #
    # @param taxableIncome The value to apply the banded percentage to
    # @param bands An array holding the lower limit of each band
    # @param rates An array holding the percentage applicable to each band
    # @param untaxedAllowance Any untaxed allowance
    # @return The banded percentage of "taxableIncome"
    def bandedPercentage(self, taxableIncome: float, bands: List[float], rates: List[float], untaxedAllowance: float) -> float:
        # Set counters to zero
        i = 0
        lastRate = 0.0
        tax = 0.0
        # For each tax band, charge the relative rate (current band rate minus previous band rate) to any income above
        # the band threshold (being this threshold shifted by any given untaxed allowance)
        while i < bands.length and taxableIncome > (bands[i] + untaxedAllowance):
            tax += (taxableIncome - (bands[i] + untaxedAllowance)) * (rates[i] - lastRate)
            lastRate = rates[i]
            i += 1
        return tax
