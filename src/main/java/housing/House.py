import numpy as np

#*************************************************************************************************
# Class to represent a house with all its intrinsic characteristics.
#
# @author daniel, Adrian Carro
#
#************************************************************************************************/
class House:
    id_pool = 0

    # Creates a house of quality quality in region region
    #
    # @param quality Quality band characterizing the house
    def __init__(self, quality: int):
        self.id: int = House.id_pool
        House.id_pool += 1
        self.owner: IHouseOwner = None
        self.resident: Household = None
        self.quality: int = quality

        self.saleRecord: HouseOfferRecord = None
        self.rentalRecord: HouseOfferRecord = None

    #-------------------#
    #----- Methods -----#
    #-------------------#

    def isOnMarket(self) -> bool:
        return self.saleRecord is not None

    def getSaleRecord(self) -> HouseOfferRecord:
        return self.saleRecord

    def getRentalRecord(self) -> HouseOfferRecord:
        return self.rentalRecord

    def isOnRentalMarket(self) -> bool:
        return self.rentalRecord is not None

    def putForSale(self, saleRecord: HouseOfferRecord) -> None:
        self.saleRecord = saleRecord

    def resetSaleRecord(self) -> None:
        self.saleRecord = None

    def putForRent(rentalRecord: HouseOfferRecord) -> None:
        self.rentalRecord = rentalRecord

    def resetRentalRecord(self) -> None:
        self.rentalRecord = None

    def getQuality(self) -> int:
        return self.quality

    def compareTo(House o) -> int:
        return np.sign(self.id - o.id)
