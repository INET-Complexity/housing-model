package housing;

import java.util.Random;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Simulates the housing market.
 * 
 * @author daniel
 *
 */
@SuppressWarnings("serial")
public class HousingMarketTest extends SimState implements Steppable {

	public HousingMarketTest(long seed) {
		super(seed);
	}

	public void start() {
		super.start();
        schedule.scheduleRepeating(this);
		initialise();
		t=0;
	}
	
	public void step(SimState simulationStateNow) {
		int j;
        if (schedule.getTime() >= N_STEPS) simulationStateNow.kill();
        
		for(j = 0; j<N; ++j) households[j].preHouseSaleStep();
		recordBidOffer(housingMarket);
		housingMarket.clearMarket();
		for(j = 0; j<N; ++j) households[j].preHouseLettingStep();
		housingMarket.clearBuyToLetMarket();
		rentalMarket.clearMarket();
		t++;
	}
	
	public void finish() {
		super.finish();
	}
	
	
	////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {

		int j;
		
		initialise();
		
		// ---- simulate
		// -------------
		for(t = 0; t<N_STEPS; ++t) {
			for(j = 0; j<N; ++j) households[j].preHouseSaleStep();

			
			housingMarket.clearMarket();
			
	//		System.out.println(housingMarket.nSales);
			
			for(j = 0; j<N; ++j) households[j].preHouseLettingStep();

			housingMarket.clearBuyToLetMarket();
			rentalMarket.clearMarket();

//			printMarketStats();
		printHPI();
	//		System.out.println(housingMarket.nSales + " " + rentalMarket.nSales + " " + housingMarket.housePriceIndex);

		}
		printHousePriceDist();
	}
	
	////////////////////////////////////////////////////////////////////////
	// Initialisation
	////////////////////////////////////////////////////////////////////////
	static void initialise() {		
		final double RENTERS = 0.32; // proportion of population who rent
		final double OWNERS = 0.32;  // proportion of population outright home-owners (no mortgage)
		final double INFLATION = 0.5; // average house-price inflation over last 25 years (not verified)
		final double INCOME_SUPPORT = 113.7*52.0/12.0; // married couple lower earnings from income support Source: www.nidirect.gov.uk
		
		int i, j, p, n;
		double price;
		LogNormalDistribution incomeDistribution;		// Annual household post-tax income
		LogNormalDistribution buyToLetDistribution; 	// No. of houses owned by buy-to-let investors
		
		incomeDistribution 	  = new LogNormalDistribution(Household.INCOME_LOG_MEDIAN, Household.INCOME_SHAPE);
//		residenceDistribution = new LogNormalDistribution(Math.log(190000), 0.568); // Source: ONS, Wealth in Great Britain wave 3
		buyToLetDistribution  = new LogNormalDistribution(Math.log(3.44), 1.050); 	// Source: ARLA report Q2 2014
		grossFinancialWealth  = new LogNormalDistribution(Math.log(9500), 2.259); 	// Source: ONS Wealth in Great Britain table 5.8
		
		for(j = 0; j<Nh; ++j) { // setup houses
			houses[j] = new House();
			houses[j].quality = (int)(House.N_QUALITY*j*1.0/Nh); // roughly same number of houses in each quality band
		}
		
		for(j = 0; j<N; ++j) { // setup households
			households[j] = new Household();
			households[j].monthlyIncome = incomeDistribution.inverseCumulativeProbability((j+0.5)/N)/12.0;
			if(households[j].monthlyIncome < INCOME_SUPPORT) households[j].monthlyIncome = INCOME_SUPPORT;
			households[j].bankBalance = 1e8; // Interim value to ensure liquidity during initialisation of housing
		}
		
		i = Nh-1;
		for(j = N-1; j>=0 && i > RENTERS*Nh; --j) { // assign houses to homeowners with sigma function probability
			if(1.0/(1.0+Math.exp((j*1.0/N - RENTERS)/0.04)) < Math.random()) {
				houses[i].owner = households[j];
				houses[i].resident = households[j];
				if(Math.random() < OWNERS/(1.0-RENTERS)) {
					// household owns house outright
					households[j].completeHousePurchase(new HouseSaleRecord(houses[i], 0));
					households[j].housePayments.get(houses[i]).nPayments = 0;
				} else {
					// household is still paying off mortgage
					p = (int)(Bank.N_PAYMENTS*Math.random()); // number of payments outstanding
					price = HousingMarket.referencePrice(houses[i].quality)/(Math.pow(1.0+INFLATION,Math.floor((Bank.N_PAYMENTS-p)/12.0)));
					if(price > bank.getMaxMortgage(households[j], true)) {
						price = bank.getMaxMortgage(households[j], true);
					}
					households[j].completeHousePurchase(new HouseSaleRecord(houses[i], price));
					households[j].housePayments.get(houses[i]).nPayments = p;
				}
				--i;
			}
		}

		while(i>=0) { // assign buyToLets
			do {
				j = (int)(Math.random()*N);
			} while(!households[j].isHomeowner());
			n = (int)(buyToLetDistribution.sample() + 0.5); // number of houses owned
			while(n>0 && i>=0) {				
				houses[i].owner = households[j];
				houses[i].resident = null;
				p = (int)(Bank.N_PAYMENTS*Math.random()); // number of payments outstanding
				price = Math.min(
						HousingMarket.referencePrice(houses[i].quality)
						/(Math.pow(1.0+INFLATION,Math.floor((Bank.N_PAYMENTS-p)/12.0))),
						bank.getMaxMortgage(households[j], false)
						);
				households[j].completeHousePurchase(new HouseSaleRecord(houses[i], price));		
				--i;
				--n;
			}
		}

		for(j = 0; j<N; ++j) { // setup financial wealth
			households[j].bankBalance = grossFinancialWealth.inverseCumulativeProbability((j+0.5)/N);
//			System.out.println(households[j].monthlyIncome*12+" "+households[j].bankBalance/households[j].monthlyIncome);
		}

		
		for(j = 0; j<N; ++j) { // homeless bid on rental market
			if(households[j].isHomeless()) rentalMarket.bid(households[j], households[j].desiredRent());
		}
		rentalMarket.clearMarket();				
	}
	
	////////////////////////////////////////////////////////////////////////
	// Monthly income against number of houses owned for each household.
	////////////////////////////////////////////////////////////////////////
	static void printHomeOwnershipData() {
		int i,j;
		for(j = 0; j<N; ++j) {
			i = 0;
			for(House h : households[j].housePayments.keySet()) {
				if(h.owner == households[j]) {
					i += 1;
				}
			}
			System.out.println(households[j].monthlyIncome + " " + i);
		}
	}
	
	////////////////////////////////////////////////////////////////////////
	// Quality of home against monthly income.
	////////////////////////////////////////////////////////////////////////
	static void printHomeQualityData() {
		int j;
		for(j = 0; j<N; ++j) {
			if(households[j].isHomeowner())
				System.out.println(households[j].home.quality+" "+households[j].monthlyIncome);
		}		
	}
	
	////////////////////////////////////////////////////////////////////////
	// average list price as a function of house quality
	////////////////////////////////////////////////////////////////////////
	static void printHousePriceDist() {
		int j;
		for(j = 0; j < House.N_QUALITY; ++j) {
			System.out.println(j+" "+housingMarket.averageSalePrice[j]);
		}
	}

	////////////////////////////////////////////////////////////////////////
	static void printMarketStats() {
		System.out.println(t/12.0+" "+housingMarket.housePriceIndex+" "+
				housingMarket.averageDaysOnMarket/360.0+" "+
				housingMarket.averageSoldPriceToOLP);		
	}
	
	////////////////////////////////////////////////////////////////////////
	// Just house price index
	////////////////////////////////////////////////////////////////////////
	static void printHPI() {
		System.out.println(housingMarket.housePriceIndex);		
	}
		
	////////////////////////////////////////////////////////////////////////
	// Average bid and average offer prices
	////////////////////////////////////////////////////////////////////////
	static void printBidOffer(HousingMarket market) {
		recordBidOffer(market);
		System.out.println(t + "\t" + averageBidPrice + "\t" + averageOfferPrice
				+ "\t" + housingMarket.averageSoldPriceToOLP + "\t" + housingMarket.averageDaysOnMarket);		
	}

	static void recordBidOffer(HousingMarket market) {
		averageBidPrice = 0.0;
		for(HouseBuyerRecord buyer : market.buyers) {
			averageBidPrice += buyer.price;
		}
		if(market.buyers.size() > 0) averageBidPrice /= market.buyers.size();

		averageOfferPrice = 0.0;
		for(HouseSaleRecord sale : market.onMarket.values()) {
			averageOfferPrice += sale.currentPrice;
		}
		if(market.onMarket.size() > 0) averageOfferPrice /= market.onMarket.size();
	}

	////////////////////////////////////////////////////////////////////////

	public static final int N = 5000; // number of households
	public static final int Nh = 4100; // number of houses
	public static final int N_STEPS = 1200; // timesteps

	public static Bank 				bank = new Bank();
	public static HouseSaleMarket 	housingMarket = new HouseSaleMarket();
	public static HouseRentalMarket	rentalMarket = new HouseRentalMarket();
	public static Household 		households[] = new Household[N];
	public static House 			houses[] = new House[Nh];
	public static int 				t;
	public static Random			rand = new Random();
	public static double			averageBidPrice;
	public static double			averageOfferPrice;
	
	public static LogNormalDistribution grossFinancialWealth;		// household wealth in bank balances and investments

}
