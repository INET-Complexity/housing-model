package housing;

import java.util.Iterator;

import org.apache.commons.math3.random.MersenneTwister;

public class Demographics {

	//------------------//
	//----- Fields -----//
	//------------------//

	private Config	            config = Model.config; // Passes the Model's configuration parameters object to a private field
	private MersenneTwister     prng;

	public Demographics(MersenneTwister prng) {
	    this.prng = prng;
    }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
	 * Add newly 'born' households to the model and remove households that 'die'
	 */
	public void step() {
        // Birth: Add households in proportion to target population and monthly birth rate of first-time-buyers
        // TODO: Shouldn't this include also new renters? Review the whole method...
        int nBirths = (int)(config.TARGET_POPULATION*config.FUTURE_BIRTH_RATE/config.constants.MONTHS_IN_YEAR
                + 0.5);
        while(nBirths-- > 0) {
            Model.households.add(new Household(prng, data.Demographics.pdfHouseholdAgeAtBirth.nextDouble()));
        }
        // Death: Kill households with a probability dependent on their age and organise inheritance
        double pDeath;
        // TODO: ATTENTION ---> fudge parameter so that population approaches the target value
        //double multFactor = (double)region.households.size()/region.getTargetPopulation();
        double multFactor = 0.05;
        Iterator<Household> iterator = Model.households.iterator();
        while(iterator.hasNext()) {
            Household h = iterator.next();
            pDeath = data.Demographics.probDeathGivenAge(h.getAge())/config.constants.MONTHS_IN_YEAR;
            if(prng.nextDouble() < pDeath*multFactor) {
                iterator.remove();
                // Inheritance
                h.transferAllWealthTo(Model.households.get(prng.nextInt(Model.households.size())));
            }
        }
	}
}
