package housing;

import ec.util.MersenneTwisterFast;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * Simulates the housing market.
 * 
 * @author daniel
 *
 */
@SuppressWarnings("serial")
public class Model extends SimState implements Steppable {

	public Model(long seed) {
		super(seed);
		firm = new Firm(this);
	}

	public void start() {
		super.start();
        schedule.scheduleRepeating(this);
        households.start();
//        houses.start();
		initialise();
		t=0;
	}
	
	public void step(SimState simulationStateNow) {
		int j;
        if (schedule.getTime() >= N_STEPS) simulationStateNow.kill();
		
		for(j = 0; j<households.config.N; ++j) households.get(j).preHouseSaleStep();
		housingMarket.clearMarket();
		for(j = 0; j<households.config.N; ++j) households.get(j).preHouseLettingStep();
		housingMarket.clearBuyToLetMarket();
		rentalMarket.clearMarket();
		t++;
	}
	
	public void finish() {
		super.finish();
	}
	
	
	////////////////////////////////////////////////////////////////////////
	// Initialisation
	////////////////////////////////////////////////////////////////////////
	static void initialise() {		
		/***
		final double RENTERS = 0.32; // proportion of population who rent
		final double OWNERS = 0.32;  // proportion of population outright home-owners (no mortgage)
		final double INFLATION = 0.5; // average house-price inflation over last 25 years (not verified)
//		final double INCOME_SUPPORT = 113.7*52.0; // married couple lower earnings from income support Source: www.nidirect.gov.uk
		
		int i, j, p, n;
		double price;
//		LogNormalDistribution incomeDistribution;		// Annual household post-tax income
//		LogNormalDistribution buyToLetDistribution; 	// No. of houses owned by buy-to-let investors (ARLA review and index 2014)
		
//		incomeDistribution 	  = new LogNormalDistribution(Household.Config.INCOME_LOG_MEDIAN, Household.Config.INCOME_SHAPE);
//		residenceDistribution = new LogNormalDistribution(Math.log(190000), 0.568); // Source: ONS, Wealth in Great Britain wave 3
//		buyToLetDistribution  = new LogNormalDistribution(Math.log(3.44), 1.050); 	// Source: ARLA report Q2 2014
		grossFinancialWealth  = new LogNormalDistribution(Math.log(9500), 2.259); 	// Source: ONS Wealth in Great Britain table 5.8
				
		i = houses.config.N-1;
		for(j = households.config.N-1; j>=0 && i > RENTERS*houses.config.N; --j) { // assign houses to homeowners with sigma function probability
			if(1.0/(1.0+Math.exp((j*1.0/households.config.N - RENTERS)/0.04)) < rand.nextDouble()) {
	//			houses.get(i).owner = households.get(j);
	//			houses.get(i).resident = households.get(j);
	//			households.get(j).home = houses.get(i);
				if(rand.nextDouble() < OWNERS/(1.0-RENTERS)) {
					// household owns house outright
	//				households.get(j).completeHousePurchase(new HouseSaleRecord(houses.get(i), 0));
	//				households.get(j).housePayments.get(houses.get(i)).nPayments = 0;
				} else {
					// household is still paying off mortgage
					p = (int)(bank.config.N_PAYMENTS*rand.nextDouble()); // number of payments outstanding
					price = HousingMarket.referencePrice(houses.get(i).quality)/(Math.pow(1.0+INFLATION,Math.floor((bank.config.N_PAYMENTS-p)/12.0)));
					if(price > bank.getMaxMortgage(households.get(j), true)) {
						price = bank.getMaxMortgage(households.get(j), true);
					}
					households.get(j).completeHousePurchase(new HouseSaleRecord(houses.get(i), price));
					households.get(j).housePayments.get(houses.get(i)).nPayments = p;
				}
				--i;
			}
		}

		while(i>=0) { // assign buyToLets
			do {
				j = (int)(rand.nextDouble()*households.config.N);
			} while(!households.get(j).isHomeowner());
//			n = (int)(buyToLetDistribution.sample() + 0.5); // number of houses owned
			n = (int)(Math.exp(rand.nextGaussian()*buyToLetDistribution.getShape() + buyToLetDistribution.getScale()) + 0.5); // number of houses owned
			while(n>0 && i>=0) { 			
				houses.get(i).owner = households.get(j);
				houses.get(i).resident = null;
				p = (int)(bank.config.N_PAYMENTS*rand.nextDouble()); // number of payments outstanding
				price = Math.min(
						HousingMarket.referencePrice(houses.get(i).quality)
						/(Math.pow(1.0+INFLATION,Math.floor((bank.config.N_PAYMENTS-p)/12.0))),
						bank.getMaxMortgage(households.get(j), false)
						);
				households.get(j).completeHousePurchase(new HouseSaleRecord(houses.get(i), price));	
				--i;
				--n;
			}
			households.get(j).desiredPropertyInvestmentFraction = 123.4; // temporary flag for later
		}

		for(j = 0; j<households.config.N; ++j) { // setup financial wealth
			households.get(j).bankBalance = grossFinancialWealth.inverseCumulativeProbability((j+0.5)/households.config.N);
//			System.out.println(households[j].monthlyPersonalIncome*12+" "+households[j].bankBalance/households[j].monthlyPersonalIncome);
			if(households.get(j).isPropertyInvestor()) {
				price = households.get(j).getPropertyInvestmentValuation();
				households.get(j).setDesiredPropertyInvestmentFraction(price/(price + households.get(j).bankBalance));
//				System.out.println(households[j].desiredPropertyInvestmentFraction + " " + households[j].getDesiredPropertyInvestmentValue()+" "+households[j].getPropertyInvestmentValuation());
			}
		}

		
		for(j = 0; j<households.config.N; ++j) { // homeless bid on rental market
			if(households.get(j).isHomeless()) rentalMarket.bid(households.get(j), households.get(j).desiredRent());
		}
		rentalMarket.clearMarket();				
		***/
	}

	////////////////////////////////////////////////////////////////////////
	// Getters/setters for the console
	////////////////////////////////////////////////////////////////////////
	
	public Bank.Config getBankConfig() {
		return(bank.config);
	}

	public Household.Config getHouseholdConfig() {
		return(new Household.Config());
	}
	public String nameBankConfig() {return("Bank Configuration");}
	public String nameHouseholdConfig() {return("Household Configuration");}

	public Bank.Diagnostics getBankDiagnostics() {
		return(bank.diagnostics);
	}
	public String nameBankDiagnostics() {return("Bank Diagnostics");}
	
	public HouseSaleMarket.Diagnostics getMarketDiagnostics() {
		return(housingMarket.diagnostics);
	}
	public String nameMarketDiagnostics() {return("Housing Market Diagnostics");}

	public HouseRentalMarket.Diagnostics getRentalDiagnostics() {
		return(rentalMarket.diagnostics);
	}
	public String nameRentalDiagnostics() {return("Rental Market Diagnostics");}
	
	public static int getN() {
		return households.config.N;
	}
	public String nameN() {return("Number of households");}
	
	public static int getNh() {
		return Nh;
	}
	public String nameNh() {return("Number of houses");}

	public static int getN_STEPS() {
		return N_STEPS;
	}

	public static void setN_STEPS(int n_STEPS) {
		N_STEPS = n_STEPS;
	}

	public String nameN_STEPS() {return("Number of timesteps");}

	////////////////////////////////////////////////////////////////////////

//	public static final int N = 5000; // number of households
	public static final int Nh = 4100; // number of houses
	public static int N_STEPS = 50000; // timesteps

	public static Bank 				bank = new Bank();
	public static Firm				firm;
	public static Government		government = new Government();
	public static HouseSaleMarket 	housingMarket = new HouseSaleMarket();
	public static HouseRentalMarket	rentalMarket = new HouseRentalMarket();
//	public static Household 		households[] = new Household[N];
	public static HouseholdSet 		households = new HouseholdSet();
//	public static House 			houses[] = new House[Nh];
//	public static HousingStock		houses = new HousingStock();
	public static int 				t;
	public static MersenneTwisterFast			rand = new MersenneTwisterFast(1L);
	
//	public static LogNormalDistribution grossFinancialWealth;		// household wealth in bank balances and investments

}
