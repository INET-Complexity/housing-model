package housing;

import java.io.Serializable;

/**************************************************************************************************
 * Class to represent a house with all its intrinsic characteristics.
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class House implements Comparable<House>, Serializable {
    private static final long serialVersionUID = 4538336934216907799L;

    //------------------//
    //----- Fields -----//
    //------------------//

    private static int 	id_pool = 0;

    public IHouseOwner  owner;
    public Household    resident;
    public int          id;

    HouseSaleRecord     saleRecord;
    HouseSaleRecord     rentalRecord;

    private int         quality;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

    /**
     * Creates a house of quality quality in region region
     *
     * @param quality Quality band characterizing the house
     */
	public House(int quality) {
		this.id = ++id_pool;
        this.owner = null;
        this.resident = null;
		this.quality = quality;
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

	boolean isOnMarket() { return saleRecord != null; }

	HouseSaleRecord getSaleRecord() { return saleRecord; }

	HouseSaleRecord getRentalRecord() { return rentalRecord; }

	boolean isOnRentalMarket() { return rentalRecord != null; }
    void putForSale(HouseSaleRecord saleRecord) { this.saleRecord = saleRecord; }

	void resetSaleRecord() { saleRecord = null; }
    void putForRent(HouseSaleRecord rentalRecord) { this.rentalRecord = rentalRecord; }

	void resetRentalRecord() { rentalRecord = null; }

	public int getQuality() { return quality; }

	@Override
	public int compareTo(House o) { return((int)Math.signum(id-o.id)); }
	
}
