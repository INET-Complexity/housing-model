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
	
	public boolean isOnMarket() {
		return saleRecord == null;
	}
	
	public HouseSaleRecord getSaleRecord() {
		return saleRecord;
	}

	public HouseSaleRecord getRentalRecord() {
		return rentalRecord;
	}
	
	public boolean isOnRentalMarket() {
		return rentalRecord == null;
	}

	public void putForSale(HouseSaleRecord saleRecord) {
		this.saleRecord = saleRecord;
	}
	public void offSaleMarket() {
		saleRecord = null;
	}

	public void putForRent(HouseSaleRecord rentalRecord) {
		this.rentalRecord = rentalRecord;
	}
	public void offRentalMarket() {
		rentalRecord = null;
	}
			
	public int 				quality;
	public IHouseOwner  	owner;
	public Household		resident;
	public int				id;
	public HouseSaleRecord	saleRecord;
	public HouseSaleRecord	rentalRecord;
	
	static int 				id_pool = 0;
	
	@Override
	public int compareTo(House o) {
		return((int)Math.signum(id-o.id));
	}
	
}
