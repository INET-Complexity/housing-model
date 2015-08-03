package housing;

public abstract class HousingMarketRecord {
	
	public int getQuality() {
		return 0;
	}

	public double getYeild() {
		return 0.0;
	}
	
	abstract public int getId();

	double price;
	
	public static class QPComparator implements PriorityQueue2D.XYComparator<HousingMarketRecord> {
		@Override
		public int XCompare(HousingMarketRecord arg0, HousingMarketRecord arg1) {
			double diff = arg0.price - arg1.price;
			if(diff == 0.0) {
				diff = arg0.getId() - arg1.getId();
			}
			return (int)Math.signum(diff);
		}

		@Override
		public int YCompare(HousingMarketRecord arg0, HousingMarketRecord arg1) {
			int diff = arg0.getQuality() - arg1.getQuality();
			if(diff == 0) {
				diff = arg0.getId() - arg1.getId();
			}
			return Integer.signum(diff);
		}
	}
	public static class QYComparator implements PriorityQueue2D.XYComparator<HousingMarketRecord> {
		@Override
		public int XCompare(HousingMarketRecord arg0, HousingMarketRecord arg1) {
			double diff = arg0.price - arg1.price;
			if(diff == 0.0) {
				diff = arg0.getId() - arg1.getId();
			}
			return (int)Math.signum(diff);
		}

		@Override
		public int YCompare(HousingMarketRecord arg0, HousingMarketRecord arg1) {
			double diff = arg0.getYeild() - arg1.getYeild();
			if(diff == 0.0) {
				diff = arg0.getId() - arg1.getId();
			}
			return (int)Math.signum(diff);
		}
	}
}
