package housing;

import sim.engine.SimState;
import sim.engine.Steppable;

@SuppressWarnings("serial")
public class Firm {
	
	public Firm(Model m) {
		model = m;
	}
	
	public void start() {
		model.schedule.scheduleOnceIn(1.0, new Steppable() {
			public void step(SimState s) {payEmployees();}
		});
	}
	
	public void payEmployees() {
	//	model.schedule.scheduleOnceIn(1.0, new Steppable() {
	//		public void step(SimState s) {payEmployees();}
	//	});
	}
	
	Model model;
}
