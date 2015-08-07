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
		
	/***
	 * Add newly 'born' households to the model and remove households that 'die'
	 */
	public void step() {
		// --- birth
		int nBirths;
		if(Model.t < spinupYears*12) {
			// --- still in spinup phase of simulation
			nBirths = (int)(spinupBirthRatePerHousehold.getEntry((int)(Model.t/12.0))*data.Demographics.TARGET_POPULATION/12.0 + 0.5);
		} else {
			// --- in projection phase of simulation
			nBirths = (int)(data.Demographics.futureBirthRate(Model.t)/12.0 + 0.5);
		}
		while(--nBirths >= 0) {
			Model.households.add(new Household(data.Demographics.pdfHouseholdAgeAtBirth.nextDouble()));
		}
		
		// --- death
		Iterator<Household> iterator = Model.households.iterator();
		while(iterator.hasNext()) {
		    Household h = iterator.next();
			if(Model.rand.nextDouble() < data.Demographics.probDeathGivenAge(h.lifecycle.age)/12.0) {
				// --- inheritance
				iterator.remove();
				h.transferAllWealthTo(Model.households.get(Model.rand.nextInt(Model.households.size())));
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
		double baseAge	= data.Demographics.pdfHouseholdAgeAtBirth.getSupportMin();
		int i,j;
		
		// --- setup vectors
		for(i=0; i<spinupYears; ++i) {
			birthDist.setEntry(i, data.Demographics.pdfHouseholdAgeAtBirth.density(baseAge+i));
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
	
	public static int spinupYears = (int)Math.ceil(data.Demographics.pdfAge.getSupportMax()-data.Demographics.pdfAge.getSupportMin());			// number of years to spinup
	public static RealVector spinupBirthRatePerHousehold = spinupBirthRate(); // birth rate per year by year per household-at-year-0
}
