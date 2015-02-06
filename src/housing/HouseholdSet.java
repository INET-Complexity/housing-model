package housing;

import java.util.ArrayList;



@SuppressWarnings("serial")
public class HouseholdSet extends ArrayList<Household> {
	static public class Config {
		public int N = 5000; // initial number of households
	}

	public HouseholdSet() {
		super();
		config = new Config();
		this.ensureCapacity(config.N);
	}
	
	public void start() {
		int j;
		for(j = 0; j<config.N; ++j) { // create households
			add(new Household());
		}		
	}
	
	Config config;
}
