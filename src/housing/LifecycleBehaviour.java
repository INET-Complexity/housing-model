package housing;

import java.util.ArrayList;


public class LifecycleBehaviour {

	
	public void step() {
		for(Person p : people) {
			p.step();
		}		
	}
		
	ArrayList<Person> 		people; // ages in Months of people in the household
	ArrayList<Person> 		will; // beneficiaries on the last will and testament
}
