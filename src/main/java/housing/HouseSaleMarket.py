from typing import Iterator

#******************************************************
# Class that represents market for houses for-sale.
# 
# @author daniel, Adrian Carro
#
#****************************************************/
class HouseSaleMarket(HousingMarket):
    def __init__(self, Model):
        super().__init__()
        self.Model = Model
        self.config = Model.config
        self.offersPY = PriorityQueue2D(HousingMarketRecord.PYComparator())

    def init() -> None:
        super().init()
        self.offersPY.clear()

    # This method deals with doing all the stuff necessary whenever a house gets sold.
    def completeTransaction(self, purchase: HouseBidderRecord, sale: HouseOfferRecord) -> None:
        # TODO: Revise if it makes sense to have recordTransaction as a separate method from recordSale
        self.Model.housingMarketStats.recordTransaction(sale)
        sale.getHouse().saleRecord = None
        buyer: Household = purchase.getBidder()
        if buyer == sale.getHouse().owner:
            return  # TODO: Shouldn't this if be the first line in this method?
        sale.getHouse().owner.completeHouseSale(sale)
        buyer.completeHousePurchase(sale)
        self.Model.housingMarketStats.recordSale(purchase, sale)
        sale.getHouse().owner = buyer

    def offer(self, house: House, price: float, BTLOffer: bool) -> HouseOfferRecord:
        hsr: HouseOfferRecord = super().offer(house, price, BTLOffer)
        self.offersPY.add(hsr)
        house.putForSale(hsr)
        return hsr

    def removeOffer(self, hsr: HouseOfferRecord) -> None:
        super().removeOffer(hsr)
        self.offersPY.remove(hsr)
        hsr.getHouse().resetSaleRecord()

    def updateOffer(self, hsr: HouseOfferRecord, newPrice: float) -> None:
            self.offersPY.remove(hsr)
            super.updateOffer(hsr, newPrice)
            self.offersPY.add(hsr)

    # This method overrides the main simulation step in order to sort the price-yield priorities.
    def clearMarket(self) -> None:
        # Before any use, priorities must be sorted by filling in the uncoveredElements TreeSet at the corresponding
        # PriorityQueue2D. In particular, we sort here the price-yield priorities
        self.offersPY.sortPriorities()
        # Then continue with the normal HousingMarket clearMarket mechanism
        super().clearMarket()

    def getBestOffer(self, bid: HouseBidderRecord) -> HouseOfferRecord:
        if bid.isBTLBid():  # BTL bidder (yield driven)
            bestOffer : HouseOfferRecord = HouseOfferRecord(offersPY.peek(bid))
            if bestOffer is not None:
                minDownpayment: float = bestOffer.getPrice() * (
                    1.0 - self.Model.rentalMarketStats.getExpAvFlowYield() /
                    (self.Model.centralBank.getInterestCoverRatioLimit(False) *
                     self.config.CENTRAL_BANK_BTL_STRESSED_INTEREST))
                if bid.getBidder().getBankBalance() >= minDownpayment:
                    return bestOffer
            return None
        else: # must be OO buyer (quality driven)
            return super().getBestOffer(bid)

    # Overrides corresponding method at HousingMarket in order to remove successfully matched and cleared offers from
    # the offersPY queue
    #
    # @param record Iterator over the HousingMarketRecord objects contained in offersPQ
    # @param offer Offer to remove from queues
    def removeOfferFromQueues(record: Iterator[HousingMarketRecord], offer: HouseOfferRecord) -> None:
        record.remove()
        self.offersPY.remove(offer)

    #******************************************
    # Make a bid on the market as a Buy-to-let investor
    #  (i.e. make an offer on a (yet to be decided) house).
    # 
    # @param buyer The household that is making the bid.
    # @param maxPrice The maximum price that the household is willing to pay.
    #*****************************************/
    def BTLbid(buyer: Household, maxPrice: float) -> None:
        self.bids.add(HouseBidderRecord(buyer, maxPrice, True))
