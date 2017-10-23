package collectors;

import housing.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class MicroDataRecorder {

    public MicroDataRecorder(String outputFolder) {
        outputFolderCopy = outputFolder;
    }

	public void start() throws FileNotFoundException, UnsupportedEncodingException {
		openNewFile();
	}

	public void openNewFile() {
//		String simID = Integer.toHexString(UUID.randomUUID().hashCode());
		try {
			outfile = new PrintWriter(outputFolderCopy + "transactions-"+ Model.nSimulation+".csv", "UTF-8");
			outfile.println(
					"Timestamp, transactionType, houseId, houseQuality, initialListedPrice, timeFirstOffered, transactionPrice, "+
					"buyerId, buyerAge(years), buyerHasBTLGene, buyerMonthlyPreTaxIncome, buyerMonthlyEmploymentIncome, buyerBankBalance, buyerCapGainCoeff, "+
					"mortgageDownpayment, firstTimeBuyerMortgage, buyToLetMortgage, "+
					"sellerId, sellerAge(years), sellerHasBTLGene, sellerMonthlyPreTaxIncome, sellerMonthlyEmploymentIncome, sellerBankBalance, sellerCapGainCoeff");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void recordSale(HouseBuyerRecord purchase, HouseSaleRecord sale, MortgageAgreement mortgage, HousingMarket market) {
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
    			purchase.buyer.getAge()+", "+
    			purchase.buyer.behaviour.isPropertyInvestor()+", "+
    			purchase.buyer.getMonthlyPreTaxIncome()+", "+
    			purchase.buyer.monthlyEmploymentIncome+", "+
    			purchase.buyer.getBankBalance()+", "+
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
					seller.getAge()+", "+
					seller.behaviour.isPropertyInvestor()+", "+
					seller.getMonthlyPreTaxIncome()+", "+
					seller.monthlyEmploymentIncome+", "+
					seller.getBankBalance()+", "+
					seller.behaviour.BtLCapGainCoeff
					);			
		} else {
			// must be construction sector
			outfile.println("-1, 0, false, 0, 0, 0, 0");
		}
	}
	
	public void finish() {
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
		} else {
			//finish();
		}

	}

	PrintWriter 	outfile;
	public boolean  active=false;
	private String outputFolderCopy;
}
