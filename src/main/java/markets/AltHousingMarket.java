package markets;


import org.apache.commons.math3.random.MersenneTwister;

import java.util.*;

public class AltHousingMarket {

    private MersenneTwister prng;
    private List<HouseBuyerRecord> buyerRecords;
    private HashSet<HouseSaleRecord> sellerRecords;
    private HashMap<HouseSaleRecord, List<HouseBuyerRecord>> potentialMatchedRecords;

    AltHousingMarket(MersenneTwister prng) {
        this.prng = prng;
        buyerRecords = new ArrayList<>();
        sellerRecords = new HashSet<>();
        potentialMatchedRecords = new HashMap<>();
    }

    public void submitBid(Bid bid) {
        HouseBuyerRecord record = HouseBuyerRecord.from(bid);
        buyerRecords.add(record);
    }

    public void submitOffer(Offer offer) {
        HouseSaleRecord record = HouseSaleRecord.from(offer);
        sellerRecords.add(record);
    }

    private void findPotentialMatches() {
        for (HouseBuyerRecord buyerRecord : buyerRecords) {
            HashSet<HouseSaleRecord> affordableListings = new HashSet<>();
            for (HouseSaleRecord sellerRecord : sellerRecords) {
                if (sellerRecord.getPrice() <= buyerRecord.getPrice()) {
                    affordableListings.add(sellerRecord);
                }
            }
            HouseSaleRecord highestQualityAffordableListing = Collections.max(affordableListings, ???);
            HashSet<HouseBuyerRecord> existingMatchedRecords = potentialMatchedRecords.getOrDefault(highestQualityAffordableListing, new HashSet<>());
            existingMatchedRecords.add(buyerRecord);
        }
    }

    private HashMap<HouseBuyerRecord, HouseSaleRecord> finalizePotentialMatches() {
        HashMap<HouseBuyerRecord, HouseSaleRecord> finalizedMatchedRecords = new HashMap<>();
        for (HouseSaleRecord sellerRecord : sellerRecords) {
            List<HouseBuyerRecord> potentialBuyers = potentialMatchedRecords.getOrDefault(sellerRecord, new ArrayList<>());
            if (potentialBuyers.isEmpty()) {
                sellerRecords.remove(sellerRecord);  // TODO is this the correct behavior if seller has no potential buyers?
            } else if (potentialBuyers.size() == 1) {
                HouseBuyerRecord matchedBuyerRecord = potentialBuyers.remove(0);
                finalizedMatchedRecords.put(matchedBuyerRecord, sellerRecord);
                buyerRecords.remove(matchedBuyerRecord);
                sellerRecords.remove(sellerRecord);
            } else {
                int idx = prng.nextInt(potentialBuyers.size());
                HouseBuyerRecord matchedBuyerRecord = potentialBuyers.remove(idx);
                finalizedMatchedRecords.put(matchedBuyerRecord, sellerRecord);
                buyerRecords.remove(matchedBuyerRecord);
                sellerRecords.remove(sellerRecord);
            }
        }
        return finalizedMatchedRecords;
    }

}
