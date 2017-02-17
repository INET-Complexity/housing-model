package housing;

import java.util.Iterator;
// import java.util.function.DoubleUnaryOperator; // not compatible with Java 1.7

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import utilities.DoubleUnaryOperator;
import utilities.Pdf;

public class Demographics {
	public static final int TARGET_POPULATION = 10000;  	// target number of households
	public static final boolean SPINUP = false;

	/***
	 * Add newly 'born' households to the model and remove households that 'die'
	 */
	@SuppressWarnings("unused")
	public void step() {
		// --- birth
		int nBirths;
		if(SPINUP && Model.getTime() < spinupYears*12) {
			// --- still in spinup phase of simulation
			nBirths = (int)(spinupBirthRatePerHousehold.getEntry((int)(Model.getTime()/12.0))*TARGET_POPULATION/12.0 + 0.5);
			while(--nBirths >= 0) {
				Model.households.add(new Household(data.Demographics.pdfSpinupHouseholdAgeAtBirth.nextDouble()));
			}
		} else {
			// --- in projection phase of simulation
			nBirths = (int)(TARGET_POPULATION*data.Demographics.futureBirthRate(Model.getTime())/12.0 + 0.5);
			while(--nBirths >= 0) {
				Model.households.add(new Household(data.Demographics.pdfHouseholdAgeAtBirth.nextDouble()));
			}
		}
		
		// --- death
		double pDeath;
		Iterator<Household> iterator = Model.households.iterator();
//	    double pMult = 1.0;
//	    if(Model.getTime() > spinupYears*12) pMult = Model.households.size()/TARGET_POPULATION;
		while(iterator.hasNext()) {
		    Household h = iterator.next();
		    pDeath = data.Demographics.probDeathGivenAge(h.lifecycle.age)/12.0;
			if(rand.nextDouble() < pDeath) {
				// --- inheritance
				iterator.remove();
				h.transferAllWealthTo(Model.households.get(rand.nextInt(Model.households.size())));
			}
		}
	}
	
	/***
	 * Calculates the birth rate over time so that at the end
	 * of spinup period we hit the target population and age distribution
	 */
	public static RealVector spinupBirthRate() {
		RealVector targetDemographic = new ArrayRealVector(spinupYears);
		RealVector birthDist 		 = new ArrayRealVector(spinupYears);
		RealMatrix M			 	 = new Array2DRowRealMatrix(spinupYears, spinupYears);
		RealMatrix timeStep 		 = new Array2DRowRealMatrix(spinupYears, spinupYears);
		double baseAge	= data.Demographics.pdfSpinupHouseholdAgeAtBirth.getSupportLowerBound();
		int i,j;
		
		// --- setup vectors
		for(i=0; i<spinupYears; ++i) {
			birthDist.setEntry(i, data.Demographics.pdfSpinupHouseholdAgeAtBirth.density(baseAge+i));
			targetDemographic.setEntry(i,data.Demographics.pdfAge.density(baseAge+i));
		}
		
		// --- setup timestep matrix
		for(i=0; i<spinupYears; ++i) {
			for(j=0; j<spinupYears; ++j) {
				if(i == j+1) {
					timeStep.setEntry(i,j,1.0-data.Demographics.probDeathGivenAge(j + baseAge));
				} else {
					timeStep.setEntry(i,j,0.0);					
				}
			}
		}
		
		// --- setup aged birth distribution matrix
		for(i=0; i<spinupYears; ++i) {
			M.setColumnVector(i, birthDist);
			birthDist = timeStep.operate(birthDist);
		}
		
		DecompositionSolver solver = new LUDecomposition(M).getSolver();
		return(solver.solve(targetDemographic));
	}

	private Model.MersenneTwister	rand = Model.rand;	// Passes the Model's random number generator to a private field
	public static int spinupYears = (int)Math.ceil(data.Demographics.pdfAge.getSupportUpperBound()-data.Demographics.pdfAge.getSupportLowerBound());			// number of years to spinup
	public static RealVector spinupBirthRatePerHousehold = spinupBirthRate(); // birth rate per year by year per household-at-year-0
}
