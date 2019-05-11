package housing;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.math3.random.MersenneTwister;

public class Demographics {

	//------------------//
	//----- Fields -----//
	//------------------//

	private MersenneTwister     prng;
    private double              firstBinMin = data.Demographics.getMonthlyAgeDistributionMinimum();
    private double              binWidth = data.Demographics.getMonthlyAgeDistributionBinWidth();
    private int []              householdsPerAgeBand = new int[data.Demographics.getMonthlyAgeDistributionSize()];
    private int []              birthsAndDeaths = new int[data.Demographics.getMonthlyAgeDistributionSize()];
    private double []           deathProbabilities = new double[data.Demographics.getMonthlyAgeDistributionSize() + 1];

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	public Demographics(MersenneTwister prng) { this.prng = prng; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Add newly born households to the model and remove households that die. Given a distribution of age (with monthly
     * age bins), the expected number of households per age band is used to compute how many new households are to be
     * added or removed from each age band.
     */
	public void step() {
        // Increase age of households and create a histogram with the number of households in each age band
        updateHouseholdsPerAgeBand();
        // Update the list of births (positive) and deaths (negative) to be implemented
        updateBirthsAndDeaths();
        // Implement births in each age bin by adding new households with random ages between the corresponding edges
        implementBirths();
        // Update death probability for each age bin, with probability 1 for households beyond the maximum bin edge
        updateDeathProbabilities();
        // Implement deaths according to the probabilities calculated above
        implementDeaths();
    }


    /**
     * After increasing the age of all currently existing households by one month, create a histogram with the actual
     * number of households in each age band. Note that there might be households older than the maximum bin edge, which
     * will be ignored here, but they will be assigned a death probability equal to one later on
     */
    private void updateHouseholdsPerAgeBand() {
        // The array must be reset to zero every time step
        Arrays.fill(householdsPerAgeBand, 0);
        for (Household h : Model.households) {
            // First, households age
            h.ageOneMonth();
            // Then find the bin at which the age of the household falls...
            int i = (int) ((h.getAge() - firstBinMin) / binWidth);
            // ...and increase the number of households in that bin by one (ignoring ages beyond the maximum edge)
            if (i < householdsPerAgeBand.length) householdsPerAgeBand[i]++;
        }
    }

    /**
     * Update the array of births (positive) and deaths (negative) to be implemented by comparing the actual number of
     * households with the expected number of households for each age bin
     */
    private void updateBirthsAndDeaths() {
        for (int i = 0; i < householdsPerAgeBand.length; i++) {
            birthsAndDeaths[i] = data.Demographics.getExpectedHouseholdsForAgeBand(i) - householdsPerAgeBand[i];
        }
    }

    /**
     * Implement births in each age bin by adding new households with random ages drawn from a uniform distribution
     * between the corresponding bin edges
     */
    private void implementBirths() {
        for (int i = 0; i < birthsAndDeaths.length; i++) {
            while (birthsAndDeaths[i] > 0) {
                double r = (prng.nextDouble() + i) * binWidth + firstBinMin;
                if (r >= firstBinMin + (i + 1) * binWidth) { // To correct for possible rounding errors
                    r = Math.nextDown(firstBinMin + (i + 1) * binWidth);
                }
                Model.households.add(new Household(prng, r));
                birthsAndDeaths[i]--;
            }
        }
    }

    /**
     * Update the death probability for each age bin, including an extra bin with death probability 1 for households
     * aging beyond the maximum bin edge
     */
    private void updateDeathProbabilities() {
        for (int i = 0; i < deathProbabilities.length - 1; i++) {
            if (birthsAndDeaths[i] < 0 && householdsPerAgeBand[i] > 0) {
                deathProbabilities[i] = -(double) birthsAndDeaths[i] / householdsPerAgeBand[i];
            } else {
                deathProbabilities[i] = 0.0;
            }
        }
        deathProbabilities[deathProbabilities.length - 1] = 1.0;
    }

    /**
     * Run through households implementing deaths according to the probability corresponding to the age band they belong
     * to, organising also the inheritance of their belongings
     */
    private void implementDeaths() {
        Iterator<Household> iterator = Model.households.iterator();
        while (iterator.hasNext()) {
            Household h = iterator.next();
            int i = (int)((h.getAge() - firstBinMin)/binWidth);
            if (prng.nextDouble() < deathProbabilities[i]) {
                iterator.remove();
                // Implement inheritance with a randomly chosen heir
                h.transferAllWealthTo(Model.households.get(prng.nextInt(Model.households.size())));
                // Update the death probability for the corresponding age band. This prevents killing more than strictly
                // necessary. Note that this will tend to underestimate the number of deaths and this, in its turn, lead
                // to a slight overpopulation
                birthsAndDeaths[i]++;
                householdsPerAgeBand[i]--;
                deathProbabilities[i] = -(double) birthsAndDeaths[i] / householdsPerAgeBand[i];
            }
        }
	}
}
