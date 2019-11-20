import random
from typing import List

class Construction(IHouseOwner):
    def __init__(self, prng):
        # Total number of houses in the whole model
        self.housingStock: int = 0
        self.onMarket: List[House] = []
        self.prng = prng
        # Number of houses built this month
        self.nNewBuild = 0
        self.config = Model.config
        self.Model = Model

    #-------------------#
    #----- Methods -----#
    #-------------------#

    def init(self) -> None:
        self.housingStock = 0
        self.onMarket.clear()

    def step(self) -> None:
        # Initialise to zero the number of houses built this month
        self.nNewBuild = 0
        # First update prices of properties put on the market on previous time steps and still unsold
        for h in self.onMarket:
            self.Model.houseSaleMarket.updateOffer(h.getSaleRecord(), h.getSaleRecord().getPrice() * 0.95)
        # Then, compute target housing stock dependent on current and target population
        if len(self.Model.households) < self.config.TARGET_POPULATION:
            targetStock = int(len(self.Model.households) * self.config.CONSTRUCTION_HOUSES_PER_HOUSEHOLD)
        else:
            targetStock = int(self.config.TARGET_POPULATION * self.config.CONSTRUCTION_HOUSES_PER_HOUSEHOLD)
        # ...compute the shortfall of houses
        shortFall = targetStock - self.housingStock
        # ...if shortfall is positive...
        if shortFall > 0:
            # ...add this shortfall to the number of houses built this month
            self.nNewBuild += shortFall
        # ...and while there is any positive shortfall...
        while shortFall > 0:
            # ...create a new house with a random quality and with the construction sector as the owner
            newHouse = House(int(random.random() * self.config.N_QUALITY))
            newHouse.owner = self
            # ...put the house for sale in the house sale market at the reference price for that quality
            self.Model.houseSaleMarket.offer(
                newHouse,
                self.Model.housingMarketStats.getReferencePriceForQuality(newHouse.getQuality()), False)
            # ...add the house to the portfolio of construction sector properties
            self.onMarket.append(newHouse)
            # ...and finally increase housing stocks, and decrease shortfall
            self.housingStock += 1
            shortFall -= 1

    def completeHouseSale(self, sale: HouseOfferRecord) -> None:
        self.onMarket.remove(sale.getHouse())

    def endOfLettingAgreement(self, h: House, p: PaymentAgreement) -> None:
        print("Strange: a tenant is moving out of a house owned by the construction sector!")

    def completeHouseLet(self, sale: HouseOfferRecord) -> None:
        print("Strange: the construction sector is trying to let a house!")

    #----- Getter/setter methods -----#

    def getHousingStock(self) -> int:
        return self.housingStock

    def getnNewBuild(self) -> int:
        return self.nNewBuild
}
