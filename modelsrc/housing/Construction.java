package housing;


public class Construction implements IHouseOwner {

	public Construction() {
		housesPerHousehold = 4100.0/5000.0;
		housingStock = 0;
//		for(j = 0; j<Nh; ++j) { // setup houses
//			houses[j] = new House();
//			houses[j].quality = (int)(House.Config.N_QUALITY*j*1.0/Nh); // roughly same number of houses in each quality band
//		}	
	}
	
	public void step() {
		int targetStock = (int)(Model.households.size()*housesPerHousehold);
		int shortFall = targetStock - housingStock;
		House newBuild;
		double price;
		while(shortFall > 0) {
			newBuild = new House();
			newBuild.owner = this;
			++housingStock;
			price = HousingMarket.referencePrice(newBuild.quality);
			Model.housingMarket.offer(newBuild, price);
			--shortFall;
		}
	}
	
	@Override
	public void completeHousePurchase(HouseSaleRecord sale) {
		// TODO Auto-generated method stub

	}

	@Override
	public void completeHouseSale(HouseSaleRecord sale) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endOfLettingAgreement(House h) {
		// TODO Auto-generated method stub

	}

	@Override
	public void completeHouseLet(House house) {
		// TODO Auto-generated method stub		
	}

	public double housesPerHousehold; 	// target number of houses per household
	public int housingStock;			// total number of houses built
}
