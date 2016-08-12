package housing;

public class LSTM_input {
    public static int LSTM_SEQUENCES=18;
    public static int LSTM_FEATURES=3;
    static public int TIME_BEFORE_LSTM = 2000*12; //TODO: Let the model spin for 20 years in order to generate
    // long enough sequences for the LSTM


    public LSTM_input(Household me) {
        sequence_LSTM = new double[LSTM_SEQUENCES][LSTM_FEATURES];
        newLSTM_PredictionNeeded=true;
        last_sequence_pointer=0;
        this.me = me;
        annual_LSTM_prediction=0;

    }

    /**
     Update the sequence of features to pass to the LSTM
     */
    public void update() {
        if (!Model.USING_LSTM) return;
        System.arraycopy(sequence_LSTM,1,sequence_LSTM,0,LSTM_SEQUENCES-1);

        sequence_LSTM[LSTM_SEQUENCES-1][0]=me.valueOfPreviousHome;//value of house
        sequence_LSTM[LSTM_SEQUENCES-1][1]=me.monthlyEmploymentIncome*12.0;// income last year
        sequence_LSTM[LSTM_SEQUENCES-1][2]=(int)me.monthsSinceLastPurchase*1.0/12.0; // years since I bought house

        newLSTM_PredictionNeeded=true;

    }

    public double monthlySaleProbability() {
        if (Model.getTime()-me.timeOfBirth < TIME_BEFORE_LSTM) return 0;

        if (newLSTM_PredictionNeeded) {
            annual_LSTM_prediction=Model.predictorLSTM.predict(sequence_LSTM);
            newLSTM_PredictionNeeded=false;
        }

        return (annual_LSTM_prediction*1.0/12.0);
    }

    public void newHouseValue(double houseValue) {
        me.valueOfPreviousHome = houseValue;
        me.monthsSinceLastPurchase=0;
        newLSTM_PredictionNeeded=true;
    }

    public double[][] sequence_LSTM;
    public boolean newLSTM_PredictionNeeded;
    private int last_sequence_pointer;
    public Household me;
    private double annual_LSTM_prediction;


}
