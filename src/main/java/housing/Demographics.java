package housing;

import java.util.Iterator;

import org.apache.commons.math3.random.MersenneTwister;

public class Demographics {

	//------------------//
	//----- Fields -----//
	//------------------//

	private Config	        config = Model.config; // Passes the Model's configuration parameters object to a private field
	private MersenneTwister rand = Model.rand; // Passes the Model's random number generator to a private field

    //-------------------//
    //----- Methods -----//
    //-------------------//

	/***
	 * Add newly 'born' households to the model and remove households that 'die'
	 */
	public void step() {
	    // Birth
		int nBirths;
        nBirths = (int)(config.TARGET_POPULATION*data.Demographics.futureBirthRate(Model.getTime())/config.constants.MONTHS_IN_YEAR + 0.5);
		while(--nBirths >= 0) {
            Model.households.add(new Household(data.Demographics.pdfHouseholdAgeAtBirth.nextDouble()));
		}
		// Death
		double pDeath;
		Iterator<Household> iterator = Model.households.iterator();
		while(iterator.hasNext()) {
		    Household h = iterator.next();
		    pDeath = data.Demographics.probDeathGivenAge(h.getAge())/config.constants.MONTHS_IN_YEAR;
			if(rand.nextDouble() < pDeath) {
				// Inheritance
				iterator.remove();
				h.transferAllWealthTo(Model.households.get(rand.nextInt(Model.households.size())));
			}
		}
	}
}
