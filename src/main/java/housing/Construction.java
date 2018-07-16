package housing;

import org.apache.commons.math3.random.MersenneTwister;

import java.io.Serializable;
import java.util.HashSet;


public class Construction implements IHouseOwner, Serializable {

    //------------------//
    //----- Fields -----//
    //------------------//

    private int                         housingStock; // Total number of houses in the whole model
    private int                         nNewBuild; // Number of houses built this month

    private Config	                    config = Model.config; // Passes the Model's configuration parameters object to a private field
    private MersenneTwister             prng;
    private HashSet<House>              onMarket;

    //------------------------//
    //----- Constructors -----//
    //------------------------//

	public Construction(MersenneTwister prng) {
		housingStock = 0;
		onMarket = new HashSet<>();
		this.prng = prng;
	}

    //-------------------//
    //----- Methods -----//
    //-------------------//

	public void init() {
        housingStock = 0;
		onMarket.clear();
	}

	public void step() {
        // Initialise to zero the number of houses built this month
        nNewBuild = 0;
        // First update prices of properties put on the market on previous time steps and still unsold
        for(House h : onMarket) {
            Model.houseSaleMarket.updateOffer(h.getSaleRecord(), h.getSaleRecord().getPrice()*0.95);
        }
        // Then, compute target housing stock dependent on current and target population
        int targetStock;
        if(Model.households.size() < config.TARGET_POPULATION) {
            targetStock = (int)(Model.households.size()*config.CONSTRUCTION_HOUSES_PER_HOUSEHOLD);
        } else {
            targetStock = (int)(config.TARGET_POPULATION*config.CONSTRUCTION_HOUSES_PER_HOUSEHOLD);
        }
        // ...compute the shortfall of houses
        int shortFall = targetStock - housingStock;
        // ...if shortfall is positive...
        if (shortFall > 0) {
            // ...add this shortfall to the number of houses built this month
            nNewBuild += shortFall;
        }
        // ...and while there is any positive shortfall...
        House newHouse;
        while(shortFall > 0) {
            // ...create a new house with a random quality and with the construction sector as the owner
            newHouse = new House((int)(prng.nextDouble()*config.N_QUALITY));
            newHouse.owner = this;
            // ...put the house for sale in the house sale market at the reference price for that quality
            Model.houseSaleMarket.offer(newHouse,
                    Model.housingMarketStats.getReferencePriceForQuality(newHouse.getQuality()));
            // ...add the house to the portfolio of construction sector properties
            onMarket.add(newHouse);
            // ...and finally increase housing stocks, and decrease shortfall
            ++housingStock;
            --shortFall;
		}
	}

	@Override
	public void completeHouseSale(HouseSaleRecord sale) { onMarket.remove(sale.house); }

	@Override
	public void endOfLettingAgreement(House h, PaymentAgreement p) {
        System.out.println("Strange: a tenant is moving out of a house owned by the construction sector!");
	}

	@Override
	public void completeHouseLet(HouseSaleRecord sale) {
        System.out.println("Strange: the construction sector is trying to let a house!");
	}

    //----- Getter/setter methods -----//

    public int getHousingStock() { return housingStock; }

    public int getnNewBuild() { return nNewBuild; }
}
