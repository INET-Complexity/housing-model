package housing;

import org.apache.commons.math3.random.MersenneTwister;

import java.util.HashSet;


public class Construction implements IHouseOwner{

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

    void init() {
        housingStock = 0;
        onMarket.clear();
    }

    public void step() {
        // Initialise to zero the number of houses built this month
        nNewBuild = 0;
        // First update prices of properties put on the market on previous time steps and still unsold
        for(House h : onMarket) {
            Model.houseSaleMarket.updateOffer(h.getSaleRecord(), h.getSaleRecord().getPrice() * 0.95);
        }
        // Then, compute target housing stock dependent on current and target population
        int targetStock;
        if(Model.households.size() < config.TARGET_POPULATION) {
            targetStock = (int)(Model.households.size()*config.derivedParams.UK_HOUSES_PER_HOUSEHOLD);
        } else {
            targetStock = (int)(config.TARGET_POPULATION*config.derivedParams.UK_HOUSES_PER_HOUSEHOLD);
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
            // ...create a new house with a random quality and give it to a randomly chosen household using the
            // inheritance mechanism and assuming the reference price for houses of that quality as previous price
            newHouse = new House((int)(prng.nextDouble() * config.N_QUALITY));
            Model.households.get(prng.nextInt(Model.households.size())).inheritHouse(
                    newHouse, Model.housingMarketStats.getReferencePriceForQuality(newHouse.getQuality()));
            // ...and finally increase housing stocks, and decrease shortfall
            ++housingStock;
            --shortFall;
        }
    }

    @Override
    public void completeHouseSale(HouseOfferRecord sale) { onMarket.remove(sale.getHouse()); }

    @Override
    public void endOfLettingAgreement(House h, PaymentAgreement p) {
        System.out.println("Strange: a tenant is moving out of a house owned by the construction sector!");
    }

    @Override
    public void completeHouseLet(HouseOfferRecord sale, RentalAgreement rentalAgreement) {
        System.out.println("Strange: the construction sector is trying to let a house!");
    }

    //----- Getter/setter methods -----//

    public int getHousingStock() { return housingStock; }

    public int getnNewBuild() { return nNewBuild; }
}
