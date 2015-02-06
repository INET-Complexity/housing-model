package housing;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class HousingStock extends ArrayList<House> {
	static public class Config {
		public int N = 4100; // initial number of households
	}

	public HousingStock() {
		super();
		config = new Config();
		ensureCapacity(config.N);
	}

	public void start() {
		int j;
		House newHouse;
		
		for(j = 0; j<config.N; ++j) { // create households
			newHouse = new House();
			newHouse.quality = (int)(House.Config.N_QUALITY*j*1.0/config.N); // roughly same number of houses in each quality band
			add(newHouse);
		}
	}
	
	Config config;

	
}
