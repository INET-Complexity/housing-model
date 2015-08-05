package housing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/*******************************************************
 * Class that represents market for houses for-sale.
 * 
 * @author daniel
 *
 *****************************************************/
public class HouseSaleMarket extends HousingMarket {
		
	/**
	 * This method deals with doing all the stuff necessary whenever a house gets sold.
	 */
	public void completeTransaction(HouseBuyerRecord purchase, HouseSaleRecord sale) {
		super.completeTransaction(purchase, sale);
		sale.house.saleRecord = null;
		Household buyer = purchase.buyer;		
		if(buyer == sale.house.owner) return;
		sale.house.owner.completeHouseSale(sale);
		buyer.completeHousePurchase(sale);
		sale.house.owner = buyer;
		Collectors.housingMarketStats.recordSale(purchase, sale);
	}

	@Override
	public HouseSaleRecord offer(House house, double price) {
		if(house.isOnRentalMarket()) {
			System.out.println("Got offer on sale market of house already on rental market");			
		}

		HouseSaleRecord hsr = super.offer(house, price);
		house.putForSale(hsr);
		return(hsr);
	}
	
	@Override
	public void removeOffer(HouseSaleRecord hsr) {
		super.removeOffer(hsr);
		hsr.house.resetSaleRecord();
	}
		
	/*******************************************
	 * Make a bid on the market as a Buy-to-let investor
	 *  (i.e. make an offer on a (yet to be decided) house).
	 * 
	 * @param buyer The household that is making the bid.
	 * @param price The price that the household is willing to pay.
	 ******************************************/
	public void BTLbid(Household buyer, double price) {
		bids.add(new BTLBuyerRecord(buyer, price));
	}

	/**
	 * Buy to let investors get randomly offered the chance to buy houses that
	 * are still on the market after non-investors have been cleared.
	 */
	/***
	public void clearBuyToLetMarket() {
		HouseBuyerRecord buyer;
		HouseSaleRecord  seller;
		ArrayList<HouseBuyerRecord>	potentialBuyers;
		int i;
		
		// --- create set of sellers, sorted by price then quality
		TreeSet<HouseSaleRecord> sellers = new TreeSet<HouseSaleRecord>(new HouseSaleRecord.PriceComparator());
		for(HouseSaleRecord sale : onMarket.values()) {
			sellers.add(sale);
		}

		Iterator<HouseSaleRecord>  saleIt = sellers.iterator();
		potentialBuyers = new ArrayList<HouseBuyerRecord>();
		while(saleIt.hasNext()) {
			seller = saleIt.next();
			// --- construct collection of buyers that can afford this house
			while(!buyers.isEmpty() && buyers.peek().price >= seller.price) {
				potentialBuyers.add(buyers.poll());
			}
			
			// --- choose potential buyer at random
			if(!potentialBuyers.isEmpty()) {
				i = (int)(Model.rand.nextDouble()*potentialBuyers.size());
				buyer = potentialBuyers.get(i);
				if(buyer.buyer != seller.house.owner && 
						buyer.buyer.decideToBuyBuyToLet(seller.house, seller.price)) {
					removeOffer(seller.house);
					completeTransaction(buyer, seller);
					potentialBuyers.remove(buyer);
					saleIt.remove();
				}
			}
		}
		buyers.clear();
	}
	***/
}
