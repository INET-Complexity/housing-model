from typing import List

from .House import House

#*************************************************************************************************
# This class encapsulates information on a house that is to be offered on the rental or the
# ownership housing market. One can think of it as the file an estate agent would have on each
# property managed.
#
#
# @author daniel, Adrian Carro
#
#************************************************************************************************/
class HouseOfferRecord(HousingMarketRecord):
    #------------------------#
    #----- Constructors -----#
    #------------------------#

    def __init__(self, Model, house: House, price: float, BTLOffer: bool):
        super().__init__(price)
        self.Model = Model
        self.house = house
        self.BTLOffer: bool = BTLOffer  # True if buy-to-let investor offering an investment property, false if homeowner offering home (Note that rental offers are all set to false)
        self.initialListedPrice: float = price
        # Time of initial listing
        self.tInitialListing: int = self.Model.getTime()
        self.matchedBids: List[HouseBidderRecord] = [] # TODO: Check if this initial size of 8 is good enough or can be improved
        self.houseSpecificYield: float = 0.0
        self.recalculateHouseSpecificYield(price)

    #-------------------#
    #----- Methods -----#
    #-------------------#

    #*
    # Expected gross rental yield for this particular property, obtained by multiplying the average flow gross rental
    # yield for houses of this quality in this particular region by the average sale price for houses of this quality
    # in this region and dividing by the actual listed price of this property
    #
    # @param price Updated price of the property
    #/
    def recalculateHouseSpecificYield(self, price: float) -> None:
        q: int = self.house.getQuality()
        if price > 0:
            self.houseSpecificYield = self.Model.rentalMarketStats.getAvFlowYieldForQuality(q) * self.Model.housingMarketStats.getExpAvSalePriceForQuality(q) / price

    # Record the match of the offer of this property with a bid
    #
    # @param bid The bid being matched to the offer
    def matchWith(self, bid: HouseBidderRecord) -> None:
        self.matchedBids.append(bid)

    #----- Getter/setter methods -----#

    # Quality of this property
    def getQuality(self) -> int:
        return self.house.getQuality()

    # Expected gross yield for this particular house, based on the current average flow yield and the actual listed
    # price for the house, and taking into account both the quality and the expected occupancy levels
    def getYield(self) -> float:
        return self.houseSpecificYield

    # Set the listed price for this property
    #
    # @param newPrice The new listed price for this property
    def setPrice(self, newPrice: float) -> None:
        super().setPrice(newPrice)
        self.recalculateHouseSpecificYield(newPrice)

    def getHouse(self) -> House:
        return self.house

    def getMatchedBids(self) -> List[HouseBidderRecord]:
        return self.matchedBids

    def getInitialListedPrice(self) -> float:
        return self.initialListedPrice

    def gettInitialListing(self) -> int:
        return self.tInitialListing

    def isBTLOffer(self) -> bool:
        return self.BTLOffer
