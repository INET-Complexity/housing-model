package housing;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartUtilities;
import org.jfree.data.xy.XYSeries;

import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ChartGenerator;
import sim.util.media.chart.ScatterPlotGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;

/********************************************
 * Mason Graphic Interface for the housing market simulation.
 * 
 * @author daniel
 *
 ********************************************/
@SuppressWarnings("serial")
public class HousingMarketTestGUI extends GUIState implements Steppable {

    private javax.swing.JFrame myChartFrame;
    
    // Chart generators 
    private TimeSeriesChartGenerator
        bidOfferChart,
        affordabilityChart;

	ScatterPlotGenerator 
		housingChart,
		housePriceChart,
		bankBalanceChart,
		mortgageStatsChart,
		mortgagePhaseChart;
    
    // Plottable data
    private XYSeries
        bidSeries,
        offerSeries,
        affordabilitySeries;
    
    private double [][]    homelessData;
    private double [][]    rentingData;
    private double [][]    priceData;
    private double [][]    referencePriceData;
    private double [][]    bankBalData;
    private double [][]    referenceBankBalData;
    
    protected ArrayList<TimeSeriesPlot> timeSeriesPlots;
    
    /** Create an instance of MySecondModelGUI */
    HousingMarketTestGUI() { 
        super(new HousingMarketTest(1L));
        timeSeriesPlots = new ArrayList<TimeSeriesPlot>();
    }

    /** Called once, when the simulation is to begin. */
    @Override
    public void start() {
        super.start();
        
        housingChart.removeAllSeries();
        housePriceChart.removeAllSeries();
        bankBalanceChart.removeAllSeries();
        bidOfferChart.removeAllSeries();
        affordabilityChart.removeAllSeries();
        
        bidSeries = new XYSeries("Average bid price", false);
        offerSeries = new XYSeries("Average offer price", false);
        affordabilitySeries = new XYSeries("Average FTB mortgage/income ratio", false);
        homelessData = new double[2][HousingMarketTest.N/50];
        rentingData = new double[2][HousingMarketTest.N/50];
        priceData = new double[2][House.Config.N_QUALITY];
        referencePriceData = new double[2][House.Config.N_QUALITY];
        bankBalData = new double[2][HousingMarketTest.N/50];
        referenceBankBalData = new double[2][HousingMarketTest.N/50];

        housingChart.addSeries(homelessData, "Homeless", null);
        housingChart.addSeries(rentingData, "Renting", null);
        housePriceChart.addSeries(priceData, "House price", null);
        housePriceChart.addSeries(referencePriceData, "Reference price", null);
        bankBalanceChart.addSeries(bankBalData, "Bank balance", null);
        bankBalanceChart.addSeries(referenceBankBalData, "Reference bank balance", null);
        bidOfferChart.addSeries(bidSeries, null);        
        bidOfferChart.addSeries(offerSeries, null);
        affordabilityChart.addSeries(affordabilitySeries, null);
        
        mortgageStatsChart.addSeries(HousingMarketTest.bank.itv_distribution,"Income To Value", null);
        mortgageStatsChart.addSeries(HousingMarketTest.bank.ltv_distribution,"Loan To Value", null);
        mortgageStatsChart.addSeries(HousingMarketTest.bank.lti_distribution,"Loan To Income/10", null);

        mortgagePhaseChart.addSeries(HousingMarketTest.bank.approved_mortgages,"Mortgage Phase Diagram", null);

        int i;
        for(i=0; i<House.Config.N_QUALITY; ++i) {
        	priceData[0][i] = i;
        	referencePriceData[0][i] = i;
        	referencePriceData[1][i] = HousingMarket.referencePrice(i);
        }
        for(i = 0; i<HousingMarketTest.N-50; i += 50) {
        	homelessData[0][i/50] = HousingMarketTest.households[i].annualEmploymentIncome;
        	rentingData[0][i/50] = HousingMarketTest.households[i].annualEmploymentIncome;
        	bankBalData[0][i/50] = HousingMarketTest.households[i].annualEmploymentIncome;
        	referenceBankBalData[0][i/50] = HousingMarketTest.households[i].annualEmploymentIncome;
        	referenceBankBalData[1][i/50] = HousingMarketTest.grossFinancialWealth.inverseCumulativeProbability((i+0.5)/HousingMarketTest.N);
        }

        // Execute when each time-step is complete
        scheduleRepeatingImmediatelyAfter(this);
    }
    
    /** Called after each simulation step. */
    @Override
    public void step(SimState state) {
    	int i, j, n, r;
        HousingMarketTest myModel = (HousingMarketTest)state;
        double t = myModel.schedule.getTime()/12.0;
        bidSeries.add(t, HousingMarketTest.averageBidPrice,true);
        offerSeries.add(t, HousingMarketTest.averageOfferPrice,true);
        affordabilitySeries.add(t,HousingMarketTest.bank.affordability, true);
        
        for(TimeSeriesPlot plot : timeSeriesPlots) {
        	plot.recordValues(t);
        }
        
        for(i = 0; i<HousingMarketTest.N-50; i += 50) {
        	n = 0;
        	r = 0;
        	for(j = 0; j<50; ++j) {
        		if(HousingMarketTest.households[i+j].isHomeless()) {
        			n++;
        		} else if(HousingMarketTest.households[i+j].isRenting()) {
        			r++;
        		}
        	}
        	homelessData[1][i/50] = n/50.0;
        	rentingData[1][i/50] = r/50.0;
        }
        housingChart.update();
        
        for(i=0; i<House.Config.N_QUALITY; ++i) {
        	priceData[1][i] = HousingMarketTest.housingMarket.averageSalePrice[i];
        }
        housePriceChart.update();
        
        for(i=0; i<HousingMarketTest.N-50; i+=50) {
        	bankBalData[1][i/50] = HousingMarketTest.households[i].bankBalance;
        }
        bankBalanceChart.update();        
        mortgageStatsChart.update();
        mortgagePhaseChart.update();
        for(i=0; i<=100; ++i) {
			HousingMarketTest.bank.ltv_distribution[1][i] *= HousingMarketTest.bank.config.STATS_DECAY;
			HousingMarketTest.bank.itv_distribution[1][i] *= HousingMarketTest.bank.config.STATS_DECAY;
			HousingMarketTest.bank.lti_distribution[1][i] *= HousingMarketTest.bank.config.STATS_DECAY;
        }
        
    }
    
    
    /** Called once, to initialise display windows. */
    @Override
    public void init(Controller controller) {
        super.init(controller);
        myChartFrame = new JFrame("My Graphs");
        
        // Create and label charts
        bidOfferChart = new TimeSeriesChartGenerator();
        affordabilityChart = new TimeSeriesChartGenerator();
        housingChart = new ScatterPlotGenerator();
        housePriceChart = new ScatterPlotGenerator();
        bankBalanceChart = new ScatterPlotGenerator();
        mortgageStatsChart = new ScatterPlotGenerator();
        mortgagePhaseChart = new ScatterPlotGenerator();
        
        this.makeChartLabels(housingChart, "Housing stats", "Probability", "Household Income");
        this.makeChartLabels(housePriceChart, "House prices", "Price", "Quality");
        this.makeChartLabels(bankBalanceChart, "Bank balances", "Balance", "Income");
        this.makeChartLabels(bidOfferChart, "Average Bid/Offer price", "Price", "Time");
        this.makeChartLabels(affordabilityChart, "FTB mortgage payment/income ratio", "Ratio", "Time");
        this.makeChartLabels(mortgageStatsChart, "Mortgage stats", "Frequency", "Ratio");
        this.makeChartLabels(mortgagePhaseChart, "Mortgage Phase", "Deposit/income", "Loan/income");
        
        mortgagePhaseChart.setXAxisRange(0.0, 8.0);
        mortgagePhaseChart.setYAxisRange(0.0, 8.0);
        controller.registerFrame(myChartFrame);
       
        // Create a tab interface
        JTabbedPane newTabPane = new JTabbedPane();
        newTabPane.addTab(housingChart.getTitle(), housingChart);
        newTabPane.addTab(housePriceChart.getTitle(), housePriceChart);
        newTabPane.addTab(bankBalanceChart.getTitle(), bankBalanceChart);
        newTabPane.addTab(bidOfferChart.getTitle(), bidOfferChart);
        newTabPane.addTab(affordabilityChart.getTitle(), affordabilityChart);
        newTabPane.addTab(mortgageStatsChart.getTitle(), mortgageStatsChart);
        newTabPane.addTab(mortgagePhaseChart.getTitle(), mortgagePhaseChart);

        timeSeriesPlots.add(
        		new TimeSeriesPlot("Market Statistics","Time (years)","")
        			.addVariable(HousingMarketTest.housingMarket,"housePriceIndex", "HPI")
        			.addVariable(HousingMarketTest.housingMarket,"averageSoldPriceToOLP", "Sold Price/List price")
        			.addVariable(HousingMarketTest.housingMarket,"averageDaysOnMarket", "Years on market", new DataRecorder.Transform() {
        				public double exec(double x) {return(Math.min(x/360.0, 1.0));}
        			})
    	);

        timeSeriesPlots.add(
        		new TimeSeriesPlot("Bid/Offer quantities","Time (years)","Number")
        			.addVariable(HousingMarketTest.housingMarket,"nSales", "Transactions")
        			.addVariable(HousingMarketTest.housingMarket,"nSellers", "Sellers")
        			.addVariable(HousingMarketTest.housingMarket,"nBuyers", "Buyers")
    	);
        
        /**
        timeSeriesPlots.add(
        		new TimeSeriesPlot("Bid/Offer Prices","Time (years)","Price")
        			.addVariable(HousingMarketTest,"averageBidPrice", "Average Bid Price")
        			.addVariable(HousingMarketTest,"averageOfferPrice", "Average Offer Price")        			
        );
        **/
        
        for(TimeSeriesPlot plot : timeSeriesPlots) {
        	plot.addToPane(newTabPane);
        }
        
        myChartFrame.add(newTabPane);
        
        myChartFrame.pack();
    }
    
    /** Add titles and labels to charts. */
    private void makeChartLabels(
        ChartGenerator chart, 
        String chartTitle, String xAxisLabel, String yAxisLabel) {
        chart.setTitle(chartTitle);
        chart.setYAxisLabel(xAxisLabel);
        chart.setXAxisLabel(yAxisLabel);
    }
    
    /** Called once, when the console quits. */
    @Override
   public void quit() {
        super.quit();

  //     saveChart(housingChart,"/home/daniel/output/sim5housingType.png");
  //    saveChart(housePriceChart,"/home/daniel/output/sim5housePrices.png");
  //   saveChart(bankBalanceChart,"/home/daniel/output/sim5bankBalances.png");
        
        myChartFrame.dispose();
    }
        
    public void printSeries(XYSeries series) {
        int i;
        for(i=0; i< series.getItemCount(); ++i) {
        	System.out.println(series.getX(i) + " " + series.getY(i));
        }
    }
    
    public void saveChart(ChartGenerator chart, String filename) {
        File file = new File(filename);
        try {
			ChartUtilities.saveChartAsPNG(file, chart.getChart(), 800, 600);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    // Java entry point
    public static void main(String[] args) {
        // Create a console for the GUI
        Console console = new Console(new HousingMarketTestGUI());
        console.setVisible(true);
    }

    ////////////////////////////////////////////////////////////////////
    // Console stuff
    ////////////////////////////////////////////////////////////////////
	@Override
	public Object getSimulationInspectedObject() {
		return state;
	}

}
