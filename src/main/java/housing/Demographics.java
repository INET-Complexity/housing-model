package housing;

import java.util.Iterator;

import org.apache.commons.math3.random.MersenneTwister;

public class Demographics {

	//------------------//
	//----- Fields -----//
	//------------------//

	private Config	            config = Model.config; // Passes the Model's configuration parameters object to a private field
	private MersenneTwister     prng;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	public Demographics(MersenneTwister prng) { this.prng = prng; }

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Adds newly "born" households to the model and removes households that "die".
     */
	public void step() {
        // Birth: Add new households at a rate compatible with the age at birth distribution, the probability of
        // death dependent on age, and the target population
        int nBirths = (int) (config.TARGET_POPULATION * data.Demographics.getBirthRate() + prng.nextDouble());
        // Finally, add the households, with random ages drawn from the corresponding distribution
        while (nBirths-- > 0) {
            Model.households.add(new Household(prng));
        }
        // Death: Kill households with a probability dependent on their age and organise inheritance
        double pDeath;
        Iterator<Household> iterator = Model.households.iterator();
        while (iterator.hasNext()) {
            Household h = iterator.next();
            pDeath = data.Demographics.probDeathGivenAge(h.getAge())/config.constants.MONTHS_IN_YEAR;
            if (prng.nextDouble() < pDeath) {
                iterator.remove();
                // Inheritance
                h.transferAllWealthTo(Model.households.get(prng.nextInt(Model.households.size())));
            }
        }
	}
}
