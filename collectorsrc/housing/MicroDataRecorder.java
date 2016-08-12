package housing;

import java.io.*;
import java.util.UUID;

import org.apache.commons.math3.linear.ArrayRealVector;

public class MicroDataRecorder {

	public static int salesFromOwnerOccupiers=0;
	public static int salesFromInvestors=0;
	public static int salesTotal=0;
	public static int inheritedHomes=0;

	public void start() throws FileNotFoundException, UnsupportedEncodingException {
		openNewFile();
	}

	public void openNewFile() {
//		String simID = Integer.toHexString(UUID.randomUUID().hashCode());
		try {
			if(Model.USING_LSTM) {
				String filename = "transactions-LSTM-" + Model.nSimulation + ".csv";
				outfile = new PrintWriter(new FileOutputStream(filename,false));

				outfile.println(
						"Timestamp, houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, " +
								"sellerId, sellerAge(years), sellerMonthlyPreTaxIncome, sellerMonthlyEmploymentIncome, sellerBankBalance, " +
								"yearsSinceBoughtHouse, valueOfPreviousHouse, ");
			} else {
				String filename = "transactions-benchmark-"+Model.nSimulation+".csv";
				outfile = new PrintWriter(new FileOutputStream(filename,false));
				outfile.println(
						"Timestamp, houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, " +
								"sellerId, sellerAge(years), sellerMonthlyPreTaxIncome, sellerMonthlyEmploymentIncome, sellerBankBalance, " +
								"yearsSinceBoughtHouse, valueOfPreviousHouse, ");
//				outfile.println(
//					"Timestamp, transactionType, houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, "+
//					"buyerId, buyerAge(years), buyerHasBTLGene, buyerMonthlyPreTaxIncome, buyerMonthlyEmploymentIncome, buyerBankBalance, buyerCapGainCoeff, "+
//					"mortgageDownpayment, firstTimeBuyerMortgage, buyToLetMortgage, "+
//					"sellerId, sellerAge(years), sellerHasBTLGene, sellerMonthlyPreTaxIncome, sellerMonthlyEmploymentIncome, sellerBankBalance, sellerCapGainCoeff");
			}


		} catch (FileNotFoundException e) {
			System.out.println("Error trying to write microdata!!");
			e.printStackTrace();
		}
	}
	
	public void recordSale(HouseBuyerRecord purchase, HouseSaleRecord sale, MortgageAgreement mortgage, HousingMarket market) {

		if(true) {
			if (!active) return;
			if (market instanceof HouseRentalMarket) return;
			if (!(sale.house.owner instanceof Household)) return; //construction
			if (sale.house.inherited) return; //inherited house
			Household seller = (Household) sale.house.owner;
			salesTotal+=1;
			if (seller.behaviour.isPropertyInvestor()) {salesFromInvestors+=1; return;}
			salesFromOwnerOccupiers+=1;
			if (Model.getTime()<1000) return;
			outfile.println(
					Model.getTime() + ", " +
					sale.house.id + ", " +
					sale.house.getQuality() + ", " +
					sale.initialListedPrice + ", " +
					sale.tInitialListing + ", " +
					sale.getPrice() + ", " +
					seller.id + ", " +
					seller.lifecycle.age + ", " +
					seller.getMonthlyPreTaxIncome() + ", " +
					seller.monthlyEmploymentIncome + ", " +
					seller.bankBalance + ", " +
					seller.monthsSinceLastPurchase*1.0/12.0 + ", " +
					seller.valueOfPreviousHome);
		} else {
			if(!active) return;
			outfile.print(
					Model.getTime()+", "
					);
			if(market instanceof HouseSaleMarket) {
				outfile.print("sale, ");
			} else {
				outfile.print("rental, ");
			}
			outfile.print(
					sale.house.id+", "+
					sale.house.getQuality()+", "+
					sale.initialListedPrice+", "+
					sale.tInitialListing+", "+
					sale.getPrice()+", "+
					purchase.buyer.id+", "+
					purchase.buyer.lifecycle.age+", "+
					purchase.buyer.behaviour.isPropertyInvestor()+", "+
					purchase.buyer.getMonthlyPreTaxIncome()+", "+
					purchase.buyer.monthlyEmploymentIncome+", "+
					purchase.buyer.bankBalance+", "+
					purchase.buyer.behaviour.BtLCapGainCoeff+", "
					);
			if(mortgage != null) {
				outfile.print(
						mortgage.downPayment+", "+
						mortgage.isFirstTimeBuyer+", "+
						mortgage.isBuyToLet+", "
						);
			} else {
				outfile.print("-1, false, false, ");
			}
			if(sale.house.owner instanceof Household) {
				Household seller = (Household)sale.house.owner;
				outfile.println(
						seller.id+", "+
						seller.lifecycle.age+", "+
						seller.behaviour.isPropertyInvestor()+", "+
						seller.getMonthlyPreTaxIncome()+", "+
						seller.monthlyEmploymentIncome+", "+
						seller.bankBalance+", "+
						seller.behaviour.BtLCapGainCoeff
						);
			} else {
				// must be construction sector
				outfile.println("-1, 0, false, 0, 0, 0, 0");
			}
		}
	}
	
	public void finish() {
		System.out.println("Sales from investors :"+salesFromInvestors);
		System.out.println("Sales from Owner Occupiers :"+salesFromOwnerOccupiers);
		System.out.println("Inherited homes :"+inheritedHomes);
		System.out.println("Sales total :"+salesTotal+" should be "+(salesFromInvestors+salesFromOwnerOccupiers));

		outfile.close();
	}
		
	public void endOfSim() {
		outfile.close();
		openNewFile();
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean isActive) {
		this.active = isActive;
		if(isActive) {
			try {
				Model.collectors.housingMarketStats.setActive(true);
				Model.collectors.rentalMarketStats.setActive(true);
				start();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//} else {
		//	finish();
		}

	}

	PrintWriter 	outfile;
	public boolean  active=false;
}
