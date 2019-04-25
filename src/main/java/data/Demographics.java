package data;

import housing.Config;
import housing.Model;

import utilities.BinnedDataDouble;
import utilities.Pdf;

/**************************************************************************************************
 * Class to read and work with demographics data before passing it to the Demographics class. Note
 * that, throughout this class, we use "real population" to refer to actual numbers of individuals
 * while we leave the term "population" to refer to numbers of agents, i.e., numbers of households
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class Demographics {

    //------------------//
    //----- Fields -----//
    //------------------//

    private static Config config = Model.config; // Passes the Model's configuration parameters object to a private field

    private static BinnedDataDouble ageDistribution = new BinnedDataDouble(config.DATA_AGE_DISTRIBUTION);

    // Transform the original age distribution bins to monthly bins (linear assumption)
    public static BinnedDataDouble monthlyAgeDistribution = transformAgeDistributionToMonthly(ageDistribution);

    /**
     * Probability density by age of the representative householder given that the household is newly formed. New
     * households can be formed by, e.g., children leaving home, divorce, separation, people leaving an HMO. Roughly
     * calibrated against "The changing living arrangements of young adults in the UK" ONS Population Trends winter 2009
     */
//    private static BinnedDataDouble householdAgeAtBirth = new BinnedDataDouble(config.DATA_HOUSEHOLD_AGE_AT_BIRTH_PDF);
    private static BinnedDataDouble householdAgeAtBirth = new BinnedDataDouble(config.DATA_AGE_DISTRIBUTION);
    public static Pdf pdfHouseholdAgeAtBirth = new Pdf(householdAgeAtBirth, 800);

    /**
     * Probability that a household 'dies' per year given age of the representative householder
     * Death of a household may occur by marriage, death of single occupant, moving together.
     * As first order approx: we use female death rates, assuming singles live at home until marriage,
     * there is no divorce and the male always dies first
     */
    // Create a BinnedDataDouble object to keep bins and probabilities
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // Warning due to data array not being queried (other variables are used)
//    private static BinnedDataDouble probDeathGivenAgeData  = new BinnedDataDouble(config.DATA_DEATH_PROB_GIVEN_AGE);
    private static BinnedDataDouble probDeathGivenAgeData  = new BinnedDataDouble(config.DATA_AGE_DISTRIBUTION);

    // Once data on household age at birth and on death probabilities has been loaded, compute birth rate
    private static double birthRate = computeBirthRate();

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Compute an alternative age distribution with a bin per month from the original one assuming straight linear
     * behaviour between every two points
     */
    private static BinnedDataDouble transformAgeDistributionToMonthly(BinnedDataDouble ageDistribution) {
        // Declare and initialise the new monthly age distribution with the same minimum bin edge as the original age
        // distribution and one month as bin width
        BinnedDataDouble monthlyAgeDistribution = new BinnedDataDouble(ageDistribution.getSupportLowerBound(),
                1.0 / config.constants.MONTHS_IN_YEAR);
        // Declare instrumental variables to save space
        int minAge = (int)ageDistribution.getSupportLowerBound();
        int maxAge = (int)ageDistribution.getSupportUpperBound();

        // Compute original bin centers
        double [] binCenters = new double[ageDistribution.size()];
        for (int i = 0; i < ageDistribution.size(); i++) {
            binCenters[i] = minAge + ageDistribution.getBinWidth() / 2.0 + i * ageDistribution.getBinWidth();
        }

        // Compute monthly bin edges and centers
        double [] monthlyBinCenters = new double[(maxAge - minAge) * config.constants.MONTHS_IN_YEAR];
        for (int i = 0; i < (maxAge - minAge) * config.constants.MONTHS_IN_YEAR; i++) {
            monthlyBinCenters[i] = minAge + monthlyAgeDistribution.getBinWidth() / 2.0
                    + i * monthlyAgeDistribution.getBinWidth();
        }

        // Find, for each monthly bin center, in which bin of the original bin centers it falls...
        int [] whichBin = computeWhichBin(monthlyBinCenters, binCenters);
        // ...then for each case, find the corresponding slope and intercept
        double [][] slopesAndIntercepts = computeSlopesAndIntercepts(binCenters, ageDistribution);
        double [] slopes = slopesAndIntercepts[0];
        double [] intercepts = slopesAndIntercepts[1];

        // Use these slopes and intercepts to find a monthly density for each monthly center
        for (int i = 0; i < monthlyBinCenters.length; i++) {
            double density = slopes[whichBin[i]] * monthlyBinCenters[i] + intercepts[whichBin[i]];
            if (density > 0.0) {
                monthlyAgeDistribution.add(density);
            } else {
                monthlyAgeDistribution.add(0.0);
            }
        }

        // Finally, re-normalise these densities such that, when multiplied by the monthly bin width, they add up to 1
        double factor = 0.0;
        for (double density: monthlyAgeDistribution) factor += density;
        factor *= monthlyAgeDistribution.getBinWidth();
        for (int i = 0; i < monthlyAgeDistribution.size(); i++) {
            monthlyAgeDistribution.set(i, monthlyAgeDistribution.get(i) / factor);
        }

        return monthlyAgeDistribution;
    }

    /**
     * Compute slope and intercept for each of the straight lines formed by every two points of the original age
     * distribution
     */
    private static double [][] computeSlopesAndIntercepts(double [] binCenters, BinnedDataDouble ageDistribution) {
        // For each case, find the corresponding slope and intercept and add it to an array...
        double[] slopes = new double[binCenters.length + 1];
        double[] intercepts = new double[binCenters.length + 1];
        for (int i = 1; i < binCenters.length; i++) {
            slopes[i] = (ageDistribution.getBinAt(binCenters[i]) - ageDistribution.getBinAt(binCenters[i - 1]))
                    / (binCenters[i] - binCenters[i - 1]);
            intercepts[i] = ageDistribution.getBinAt(binCenters[i]) - slopes[i] * binCenters[i];
        }
        // ...including an extension of the initial and final slopes to the edges of the age distribution
        slopes[0] = slopes[1];
        intercepts[0] = intercepts[1];
        slopes[slopes.length - 1] = slopes[slopes.length - 2];
        intercepts[intercepts.length - 1] = intercepts[intercepts.length - 2];
        return new double[][]{slopes, intercepts};
    }

    /**
     * Compute the number of bin of longEdges in which each value of shortEdges falls, assigning 0 to shortEdges values
     * below the minimum longEdges, and length(longEdges) to values of shortEdges beyond the maximum longEdges
     */
    private static int [] computeWhichBin(double [] shortEdges, double [] longEdges) {
        int [] whichBin = new int[shortEdges.length];
        int i = 0;
        int j = 0;
        for (double threshold: longEdges) {
            while (j < shortEdges.length && shortEdges[j] < threshold) {
                whichBin[j] = i;
                j++;
            }
            i++;
        }
        // Short center beyond the maximum long center, receive length(longCenters) as bin number
        while (j < shortEdges.length) {
            whichBin[j] = i;
            j++;
        }
        return whichBin;
    }

    /**
     * Compute monthly birth rate as a fraction of the target population, i.e., the number of births divided by the
     * target population
     */
    private static double computeBirthRate() {
        // First, compute total number of households with ages between the minimum possible age at birth and the maximum
        // possible age at birth, which is also the last age for which death probability is zero (birth area)
        double sum1 = 0.0;
        for (int i = (int)householdAgeAtBirth.getSupportLowerBound()*config.constants.MONTHS_IN_YEAR;
             i <= (int)householdAgeAtBirth.getSupportUpperBound()*config.constants.MONTHS_IN_YEAR - 2;
             i++) {
            sum1 += (householdAgeAtBirth.getSupportUpperBound()*config.constants.MONTHS_IN_YEAR - 1 - i)
                    * probHouseholdAgeAtBirthPerMonth(i);
        }
        // Second, compute total number of households with ages between the minimum age at which death probability is
        // non-zero and the maximum possible age, from which death probability is one (death area)
        double sum2 = 0.0;
        for (int j = (int)(probDeathGivenAgeData.getSupportLowerBound()*config.constants.MONTHS_IN_YEAR + 1);
             j <= (int)(probDeathGivenAgeData.getSupportUpperBound()*config.constants.MONTHS_IN_YEAR - 1); j++) {
            double product = 1;
            for (int i = (int)(probDeathGivenAgeData.getSupportLowerBound()*config.constants.MONTHS_IN_YEAR);
                 i <= j; i++) {
                product *= (1.0 - probDeathGivenAge(((double) i) / config.constants.MONTHS_IN_YEAR)
                        / config.constants.MONTHS_IN_YEAR);
            }
            sum2 += product;
        }
        return 1.0/(1 + sum1 + sum2);
    }

    /**
     * Method that gives, for a given age in years, its corresponding probability of death
     *
     * @param ageInYears Age in years (double)
     * @return probability Probability of death for the given age in years (double)
     */
    public static double probDeathGivenAge(double ageInYears) {
        if (ageInYears < probDeathGivenAgeData.getSupportLowerBound()) {
            return 0.0;
        } else if (ageInYears >= probDeathGivenAgeData.getSupportUpperBound()) {
            return config.constants.MONTHS_IN_YEAR;
        } else {
            return probDeathGivenAgeData.getBinAt(ageInYears);
        }
    }

    /**
     * Probability that a new household would be assigned a given age (in months)
     *
     * @param ageInMonths Age in months (int)
     */
    private static double probHouseholdAgeAtBirthPerMonth(int ageInMonths) {
        if (ageInMonths / config.constants.MONTHS_IN_YEAR < householdAgeAtBirth.getSupportLowerBound()
                || ageInMonths / config.constants.MONTHS_IN_YEAR > householdAgeAtBirth.getSupportUpperBound()) {
            return 0.0;
        } else {
            return householdAgeAtBirth.getBinAt((double) ageInMonths / config.constants.MONTHS_IN_YEAR)
                    / (config.constants.MONTHS_IN_YEAR * householdAgeAtBirth.getBinWidth());
        }
    }

    //----- Getter/setter methods -----//

    public static double getBirthRate() { return birthRate; }
}
