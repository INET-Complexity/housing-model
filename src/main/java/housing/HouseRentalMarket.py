#*************************************************************************************************
# Class to represent the rental market
#
# @author daniel, Adrian Carro
#
#************************************************************************************************/
class HouseRentalMarket(HousingMarket):
    def completeTransaction(self, purchase: HouseBidderRecord, sale: HouseOfferRecord) -> None:
        self.Model.rentalMarketStats.recordTransaction(sale)
        sale.getHouse().rentalRecord = None
        purchase.getBidder().completeHouseRental(sale)
        sale.getHouse().owner.completeHouseLet(sale)
        self.Model.rentalMarketStats.recordSale(purchase, sale)

    def offer(self, house: House, price: float, BTLOffer: bool) -> HouseOfferRecord:
        if house.isOnMarket():
            print("Got offer on rental market of house already on sale market")
        hsr: HouseOfferRecord = super().offer(house, price, False)
        house.putForRent(hsr)
        return hsr

    def removeOffer(hsr: HouseOfferRecord) -> None:
        super().removeOffer(hsr)
        hsr.getHouse().resetRentalRecord()
