package optimiser;

public class Indicator {
    public double reference;
    public double deviation;
    public double value;
    public CoreIndicator type;

    public enum CoreIndicator {
        OO_LTV, OO_LTI, BTL_LTV, CREDIT_GROWTH,
        OO_DEBT_TO_INCOME, MORTGAGE_APPROVALS, ADVANCES_TO_HOME_OWNERS,
        ADVANCES_TO_FTB, ADVANCES_TO_BTL, HOUSE_PRICE_GROWTH,
        PRICE_TO_INCOME, RENTAL_YIELD, INTEREST_RATE_SPREAD
    }

    public Indicator(double value, CoreIndicator type) {
        this.value=value;
        this.type = type;
        switch (type) {
            case OO_LTV:
                this.reference=90.6;
                this.deviation=90.8-81.6;
                break;
            case OO_LTI:
                this.reference=3.8;
                this.deviation=4.1-3.6;
                break;
            case BTL_LTV:
                this.reference=78.6;
                this.deviation=78.6-70.9;
                break;
            case CREDIT_GROWTH:
                this.reference=10.2;
                this.deviation=19.9+0.1;
                break;
            case OO_DEBT_TO_INCOME:
                this.reference=86.1;
                this.deviation=105.4-72.8;
                break;
            case MORTGAGE_APPROVALS:
                this.reference=97888;
                this.deviation=133617.0-26609;
                break;
            case ADVANCES_TO_HOME_OWNERS:
                this.reference=48985;
                this.deviation=93500.0-14300;
                break;
            case ADVANCES_TO_FTB:
                this.reference=39179;
                this.deviation=55800.0-8500;
                break;
            case ADVANCES_TO_BTL:
                this.reference=9903;
                this.deviation=16230-3603;
                break;
            case HOUSE_PRICE_GROWTH:
                this.reference=1.8;
                this.deviation=7+5.6;
                break;
            case PRICE_TO_INCOME:
                this.reference=3.3;
                this.deviation=5-2.4;
                break;
            case RENTAL_YIELD:
                this.reference=5.8;
                this.deviation=7.6-4.8;
                break;
            case INTEREST_RATE_SPREAD:
                this.reference=361;
                this.deviation=361-35;
                break;

        }

        this.deviation *= 10.0; // Increment the deviation so that fitness doesn't collapse to zero
    }

}

