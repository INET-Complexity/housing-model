import numpy as np

#**************************************************************************************************
#* This class encapsulates information on a household that has placed a bid for a house on the
#* rental or the ownership housing market. One can think of it as the file an estate agent would
#* have on a customer who wants to buy or rent a house.
#*
#* @author daniel, Adrian Carro
#*
#*************************************************************************************************/
class HouseBidderRecord(HousingMarketRecord):
    def __init__(self, h: Household, price: float, BTLBid: bool):
        super().__init__(price)
        # Household who is bidding to buy or rent a house
        self.bidder = h
        # True if the bid is for a buy-to-let property, false for a home bid (Note that rental bids are all set to false)
        self.BTLBid = BTLBid

    #----- Getter/setter methods -----#

    def getBidder(self) -> Household:
        return self.bidder

    def isBTLBid(self) -> bool:
        return self.BTLBid

    # TODO: Check if the abstract method in HousingMarketRecord class is actually needed, otherwise this could be removed
    def getQuality(self) -> int:
        return 0

# Class that implements a price comparator which solves the case of equal price by using the arguments' IDs.
class PComparator:
    def compare(HouseBidderRecord arg0, HouseBidderRecord arg1) -> int:
        diff = arg0.getPrice() - arg1.getPrice()
        if (diff == 0.0):
            diff = arg0.getId() - arg1.getId()
        return np.sign(diff)
