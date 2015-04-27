package housing;

/************************************************
 * Class representing a house.
 * Use this to represent the intrinsic properties of the house.
 * 
 * @author daniel
 *
 ************************************************/
public class House implements Comparable<House> {
	
	static public class Config {
		public static int N_QUALITY = 48; // number of quality bands		
	}
	
	public House() {
		id = ++id_pool;	
		resident = null;
		owner = null;
		quality = (int)(Model.rand.nextDouble()*Config.N_QUALITY);
	}
			
	public int 				quality;
	public IHouseOwner  	owner;
	public Household		resident;
	public int				id;
	static int 				id_pool = 0;
	
	@Override
	public int compareTo(House o) {
		return((int)Math.signum(id-o.id));
	}
	
}
