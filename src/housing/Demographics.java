package housing;

import java.util.Iterator;
import java.util.function.DoubleUnaryOperator;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Demographics {
		
	/***
	 * Add newly 'born' households to the model and remove households that 'die'
	 */
	public void step() {
		// --- birth
		int nBirths;
		if(Model.t < SPINUP_YEARS*12) {
			// --- still in spinup phase of simulation
			nBirths = (int)(spinupBirthRatePerHousehold.getEntry((int)(Model.t/12.0))*TARGET_POPULATION + 0.5);
		} else {
			// --- in projection phase of simulation
			nBirths = futureBirthRate(Model.t);
		}
		while(--nBirths >= 0) {
			Model.households.add(new Household(pdfAgeOfNewHousehold.nextDouble()));
		}
		
		// --- death
		Iterator<Household> iterator = Model.households.iterator();
		while(iterator.hasNext()) {
		    Household h = iterator.next();
			if(Model.rand.nextDouble() < probDeathGivenAge(h.age)) {
				// --- inheritance
				h.transferAllWealthTo(Model.households.get(Model.rand.nextInt(Model.households.size())));
				iterator.remove();
			}
		}
	}
	
	/***
	 * Calculates the birth rate over time so that at the end
	 * of spinup period we hit the target population and age distribution
	 */
	public static RealVector spinupBirthRate() {
		RealVector targetDemographic = new ArrayRealVector(SPINUP_YEARS);
		RealVector birthDist 		 = new ArrayRealVector(SPINUP_YEARS);
		RealMatrix M			 	 = new Array2DRowRealMatrix(SPINUP_YEARS, SPINUP_YEARS);
		RealMatrix timeStep 		 = new Array2DRowRealMatrix(5,5);
		double baseAge	= pdfAgeOfNewHousehold.start;
		
		// --- setup timestep matrix
		int i,j;
		for(i=0; i<SPINUP_YEARS; ++i) {
			for(j=0; j<SPINUP_YEARS; ++j) {
				if(i == j+1) {
					timeStep.setEntry(i,j,1.0-probDeathGivenAge(j + baseAge));
				} else {
					timeStep.setEntry(i,j,0.0);					
				}
			}
		}
		
		// --- setup aged birth distribution matrix
		RealVector agedBirthDist = new ArrayRealVector(birthDist);
		for(i=0; i<5; ++i) {
			M.setColumnVector(i, agedBirthDist);
			agedBirthDist = timeStep.operate(agedBirthDist);
		}
		
		DecompositionSolver solver = new LUDecomposition(M).getSolver();
		return(solver.solve(targetDemographic));
	}
	
	/****
	 * Birth rates into the future
	 * @param t	time (months) into the future
	 * @return number of births per month
	 */
	public static int futureBirthRate(int t) {
		return((int)(TARGET_POPULATION * 0.012 / 12.0));
	}
	
	/***
	 * Probability that a household 'dies' per month given age of the representative householder
	 * Death of a household may occur by marriage, death of single occupant, moving together
	 */
	public static double probDeathGivenAge(double age) {
		return(futureBirthRate(0)/TARGET_POPULATION);
	}
	
	/**
	 * Probability density by age of the representative household given that
	 * the household is newly formed.
	 * New households can be formed by, e.g., children leaving home,
	 * divorce, separation, people leaving an HMO.
	 */
	public static Pdf pdfAgeOfNewHousehold = new Pdf(18.0, 28.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			if(age>=18.0 && age<28.0) 
				return(0.1);
			return(0.0);
		}	
	});
	
	/**
	 * Target probability density of age of representative householder
	 * at time t=0
	 */
	public static Pdf pdfAge = new Pdf(18.0, 100.0, new DoubleUnaryOperator() {
		public double applyAsDouble(double age) {
			if(age > 18.0 && age < 50.0) {
				return(0.01125 + 0.0002734375*(age-18.0));
			}
			if(age <100.0) {
				return(0.02-0.0004*(age-50.0));
			}
			return(0.0);	
		}
	});
	
	public static final int TARGET_POPULATION = 5000;  	// target number of households
	public static final int SPINUP_YEARS = 80;			// number of years to spinup
	public static RealVector spinupBirthRatePerHousehold = spinupBirthRate(); // birth rate by year per household at year 0
}
