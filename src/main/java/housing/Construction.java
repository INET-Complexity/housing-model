package housing;

import java.io.Serializable;
import java.util.HashSet;


public class Construction implements IHouseOwner, Serializable {
	private static final long serialVersionUID = -6288390048595500248L;

	private Config	config = Model.config;	// Passes the Model's configuration parameters object to a private field

	public Construction() {
		housingStock = 0;
		onMarket = new HashSet<>();
//		for(j = 0; j<Nh; ++j) { // setup houses
//			houses[j] = new House();
//			houses[j].quality = (int)(House.Config.N_QUALITY*j*1.0/Nh); // roughly same number of houses in each quality band
//		}	
	}
	
	public void init() {
		housingStock = 0;
		onMarket.clear();
	}
	
	public void step() {
		int targetStock;
		if(Model.households.size() < config.TARGET_POPULATION) {
			targetStock = (int)(Model.households.size()*config.CONSTRUCTION_HOUSES_PER_HOUSEHOLD);
		} else {
			targetStock = (int)(config.TARGET_POPULATION*config.CONSTRUCTION_HOUSES_PER_HOUSEHOLD);
		}
		int shortFall = targetStock - housingStock;
		House newBuild;
		double price;
		for(House h : onMarket) {
			Model.houseSaleMarkets.updateOffer(h.getSaleRecord(), h.getSaleRecord().getPrice()*0.95);
		}
		while(shortFall > 0) {
			newBuild = new House();
			newBuild.owner = this;
			++housingStock;
			price = Model.houseSaleMarkets.referencePrice(newBuild.getQuality());
//			if(Model.rand.nextDouble() < 0.9) {
			Model.houseSaleMarkets.offer(newBuild, price);
			onMarket.add(newBuild);
//			} else {
//				Model.households.get(Model.rand.nextInt(Model.households.size())).inheritHouse(newBuild);
//			}
			--shortFall;
		}
	}

	@Override
	public void completeHouseSale(HouseSaleRecord sale) {
		onMarket.remove(sale.house);
	}

	@Override
	public void endOfLettingAgreement(House h, PaymentAgreement p) {
        System.out.println("Strange: a tenant is moving out of a house owned by the construction sector!");
		System.exit(0);

	}

	@Override
	public void completeHouseLet(HouseSaleRecord sale) {
        System.out.println("Strange: the construction sector is trying to let a house!");
        System.exit(0);
	}

	public int housingStock;			// total number of houses built
	HashSet<House> onMarket; 
}
