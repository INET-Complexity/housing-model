package housing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Consumer;

/*******************************************************
 * Class that represents market for houses for-sale.
 * 
 * @author daniel
 *
 *****************************************************/
public class HouseSaleMarket extends HousingMarket {
		
	public HouseSaleMarket() {
		offersPY = new PriorityQueue2D<>(new HousingMarketRecord.PYComparator());
	}
	
	@Override
	public void init() {
		super.init();
		if(offersPY != null) offersPY.clear();
	}
		
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
		HouseSaleRecord hsr = super.offer(house, price);
		offersPY.add(hsr);
		house.putForSale(hsr);
		return(hsr);
	}
	
	@Override
	public void removeOffer(HouseSaleRecord hsr) {
		super.removeOffer(hsr);
		offersPY.remove(hsr);
		hsr.house.resetSaleRecord();
	}
	
	@Override
	public void updateOffer(HouseSaleRecord hsr, double newPrice) {
		offersPY.remove(hsr);
		super.updateOffer(hsr, newPrice);
		offersPY.add(hsr);
	}
	
	@Override
	protected HouseSaleRecord getBestOffer(HouseBuyerRecord bid) {
		if(bid.getClass() == HouseBuyerRecord.class) { // OO buyer (quality driven)
			return super.getBestOffer(bid);
		} else { // BTL buyer (yield driven)
			return (HouseSaleRecord)offersPY.peek(bid);
		}
	}
	/*
	@Override
	protected void clearMatches() {
		offersPY.checkConsistency();
		super.clearMatches();
		// sync offersPY with offersPQ to remove cleared offers
		HousingMarketRecord offer;
		Iterator<HousingMarketRecord> pyOffer = offersPY.iterator();
		offersPY.checkConsistency();
		while(pyOffer.hasNext()) {
			offer = pyOffer.next();
			if(!offersPQ.contains(offer)) pyOffer.remove();
		}
	}
		*/
	
	public Iterator<HousingMarketRecord> offersIterator() {
		final PriorityQueue2D<HousingMarketRecord>.Iter underlyingIterator = (PriorityQueue2D<HousingMarketRecord>.Iter)super.offersIterator();
		return(new Iterator<HousingMarketRecord>() {
			@Override
			public boolean hasNext() {
				return underlyingIterator.hasNext();
			}
			@Override
			public HousingMarketRecord next() {
				return underlyingIterator.next();
			}
			@Override
			public void remove() {
				underlyingIterator.remove();
				if(underlyingIterator.last != null) HouseSaleMarket.this.offersPY.remove(underlyingIterator.last);
			}
		});
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

	protected PriorityQueue2D<HousingMarketRecord>	offersPY;	

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
