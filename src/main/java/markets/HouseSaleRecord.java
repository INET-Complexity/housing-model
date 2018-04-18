package markets;

import housing.House;
import housing.Model;

import java.util.ArrayList;

/**************************************************************************************************
 * Class to encapsulate information on a house that is for sale. It can be though of as the record
 * a estate agent would keep about each of the properties managed
 *
 * @author daniel, Adrian Carro
 *
 *************************************************************************************************/
public class HouseSaleRecord extends HousingMarketRecord {
	private static final long serialVersionUID = 8626260055548234106L;

	//------------------//
	//----- Fields -----//
	//------------------//

    public House house;
    ArrayList<HouseBuyerRecord>     matchedBids;
    public double                   initialListedPrice;
    public int                      tInitialListing; // Time of initial listing
    private double                  houseSpecificYield;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	/**
	 * Construct a new record
	 * 
	 * @param h The house that is for sale
	 * @param price The initial list price for the house
	 */
	public HouseSaleRecord(House h, double price) {
		super(price);
		house = h;
		initialListedPrice = price;
		tInitialListing = Model.getTime();
		matchedBids = new ArrayList<>(8); // TODO: Check if this initial size of 8 is good enough or can be improved
        recalculateHouseSpecificYield(price);
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

    /**
     * Expected gross rental yield for this particular property, obtained by multiplying the average flow gross rental
     * yield for houses of this quality in this particular region by the average sale price for houses of this quality
     * in this region and dividing by the actual listed price of this property
     */
    private void recalculateHouseSpecificYield(double price) {
        int q = house.getQuality();
        if (price > 0) {
            houseSpecificYield = Model.rentalMarketStats.getAvFlowYieldForQuality(q)
                    *Model.housingMarketStats.getExpAvSalePriceForQuality(q)
                    /price;
        }
    }

    /**
     * Record the match of the offer of this property with a bid
     *
     * @param bid The bid being matched to the offer
     */
    void matchWith(HouseBuyerRecord bid) { matchedBids.add(bid); }

    //----- Getter/setter methods -----//

    /**
     * Quality of this property
     */
    @Override
	public int getQuality() { return house.getQuality(); }
	
	/**
	 * Expected gross yield for this particular house, based on the current average flow yield and the actual listed
     * price for the house, and taking into account both the quality and the expected occupancy levels
	 */
	@Override
	public double getYield() { return houseSpecificYield; }

    /**
     * Set the listed price for this property
     *
     * @param newPrice The new listed price for this property
     * @param auth Authority to change the price
     */
	public void setPrice(double newPrice, HousingMarket.Authority auth) {
		super.setPrice(newPrice, auth);
        recalculateHouseSpecificYield(newPrice);
	}
}
