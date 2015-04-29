package housing;

public class Collectors {
		
	public void step() {
		creditSupply.step();
	}
	
	public static CreditSupply		creditSupply 	= new CreditSupply();
	public static CoreIndicators	coreIndicators 	= new CoreIndicators();
}
