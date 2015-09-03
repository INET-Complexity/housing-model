package housing;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

//import org.jfree.chart.ChartUtilities;
//import org.jfree.data.xy.XYSeries;

import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ScatterPlotGenerator;
import sim.util.media.chart.ScatterPlotSeriesAttributes;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;
import sim.display.ChartUtilities;
import utilities.DataRecorder;
import utilities.TimeSeriesPlot;

/********************************************
 * Interface to the Mason Graphic Interface for the housing market simulation.
 * This object contains the "main" function, which crates a ModelGUI instance.
 * This, in turn, creates an instance of the Model class, which actually does
 * the simulation.
 * 
 * @author daniel
 *
 ********************************************/
@SuppressWarnings("serial")
public class ModelGUI extends GUIState implements Steppable {

    private javax.swing.JFrame myChartFrame;
    
    // Chart generators
    
    public ChartUtilities marketStats;
    
	public ScatterPlotGenerator
	//	housingChart,
		housePriceChart;
	//	bankBalanceChart,
	//	mortgageStatsChart,
	//	mortgagePhaseChart;
	
//	public TimeSeriesChartGenerator
//		marketStats;
            
 //   public TimeSeriesAttributes 	
//	hpi,
//    nBuyers;
// 	daysOnMarket;

    protected ArrayList<TimeSeriesPlot> timeSeriesPlots;
    
    /** Create an instance of MySecondModelGUI */
    ModelGUI() { 
        super(new Model(1L));
        timeSeriesPlots = new ArrayList<TimeSeriesPlot>();
    }

    
    public void load(final SimState state) {
    	super.load(state);
    	
 //       ChartUtilities.scheduleSeries(this, hpi, new sim.util.Valuable() {
  //      	public double doubleValue() {return Model.housingMarket.housePriceIndex; }});

 //   	ChartUtilities.scheduleSeries(this, nBuyers, new sim.util.Valuable() {
  //  		public double doubleValue() {return Collectors.housingMarketStats.getnBuyers();}});

 //       ChartUtilities.scheduleSeries(this, daysOnMarket, new sim.util.Valuable() {
  //      	public double doubleValue() {return Model.housingMarket.averageDaysOnMarket; }});

    	// Execute when each time-step is complete
        scheduleRepeatingImmediatelyAfter(this);
        
    }
        
    /** Called once, to initialise display windows. */
    @Override
    public void init(Controller controller) {
        super.init(controller);
        
        
        
        myChartFrame = new JFrame("My Graphs");
        controller.registerFrame(myChartFrame);
        
  //      marketStats = ChartUtilities.buildTimeSeriesChartGenerator(this, "Market statistics", "Time");
  //      marketStats.setYAxisLabel("Index");
  //      nBuyers = ChartUtilities.addSeries(marketStats, "no. of buyers");
   //     hpi = ChartUtilities.addSeries(marketStats, "House Price Index");
    //    daysOnMarket = ChartUtilities.addSeries(marketStats, "Days on market");
        // Create a tab interface
        JTabbedPane newTabPane = new JTabbedPane();
  //      housingChart = makeScatterPlot(newTabPane, "Housing stats", "Probability", "Household Income");
        housePriceChart = makeScatterPlot(newTabPane, "House prices", "Modelled Price", "Reference Price");
//        bankBalanceChart = makeScatterPlot(newTabPane, "Bank balances", "Balance", "Income");
//        mortgageStatsChart = makeScatterPlot(newTabPane, "Mortgage stats", "Frequency", "Ratio");
 //       mortgagePhaseChart = makeScatterPlot(newTabPane, "Mortgage phase",  "Down-payment/Income", "Loan to Income ratio");
  //      mortgagePhaseChart.setXAxisRange(0.0, 8.0);
   //     mortgagePhaseChart.setYAxisRange(0.0, 8.0);
                
        timeSeriesPlots.add(
        		new TimeSeriesPlot("Market Statistics","Time (years)","Value")
        			.addVariable(Model.housingMarket,"housePriceIndex", "HPI")
        			.addVariable(Model.housingMarket,"averageDaysOnMarket", "Years on market", new DataRecorder.Transform() {
        				public double exec(double x) {return(Math.min(x/360.0, 1.0));}
        			})
        			.addVariable(Model.collectors.housingMarketStats, "nBuyers", "Buyers (1000s)", new DataRecorder.Transform() {
        				public double exec(double x) {return(x/1000.0);}
        			})
//        			.addVariable(HousingMarketTest.housingMarket.diagnostics,"averageSoldPriceToOLP", "Sold Price/List price")
        			.addVariable(Model.collectors.creditSupply,"affordability", "Affordability (Mortgage-payment/income)")
    	);

        timeSeriesPlots.add(
        		new TimeSeriesPlot("Bid/Offer quantities","Time (years)","Number")
        			.addVariable(Model.collectors.housingMarketStats,"nSales", "Transactions")
        			.addVariable(Model.collectors.housingMarketStats,"nSellers", "Sellers")
        			.addVariable(Model.collectors.housingMarketStats,"nBuyers", "Buyers")
    	);
        
        timeSeriesPlots.add(
        		new TimeSeriesPlot("Bid/Offer Prices","Time (years)","Price")
        			.addVariable(Model.collectors.housingMarketStats,"averageBidPrice", "Average Bid Price")
        			.addVariable(Model.collectors.housingMarketStats,"averageOfferPrice", "Average Offer Price")        			
        );

//        timeSeriesPlots.add(
//        		new TimeSeriesPlot("Affordability","Time (years)","mortgage payment/income")
//        			.addVariable(HousingMarketTest.bank.diagnostics,"affordability", "Affordability")
//    	);

 //       timeSeriesPlots.add(
  //      		new TimeSeriesPlot("Tenure quantities","Time (years)","number")
   //     			.addVariable(Collectors.householdStats,"nHomeless", "Social-Housing")
    //    			.addVariable(Collectors.householdStats,"nNonOwner", "Non Owners")
     //   			.addVariable(Collectors.householdStats,"nEmpty", "Empty Houses")
      //  			.addVariable(Collectors.householdStats,"nHouseholds", "Total")
    //	);
        
        for(TimeSeriesPlot plot : timeSeriesPlots) {
        	plot.addToPane(newTabPane);
        }
        
        myChartFrame.add(newTabPane);
        
        myChartFrame.pack();
    }

    /** Called once, when the simulation is to begin. */
    @Override
    public void start() {
        super.start();

  //      marketStats.clearAllSeries(); /// NOTE THIS ISN'T IN LOAD(...)
  //  	ChartUtilities.scheduleSeries(this, nBuyers, new sim.util.Valuable() {
  //  		public double doubleValue() {return Collectors.housingMarketStats.getnBuyers();}});
        
   //     ChartUtilities.scheduleSeries(this, hpi, new sim.util.Valuable() {
    //    	public double doubleValue() {return Model.housingMarket.housePriceIndex; }});
        
      //  ChartUtilities.scheduleSeries(this, daysOnMarket, new sim.util.Valuable() {
        //	public double doubleValue() {return Model.housingMarket.averageDaysOnMarket; }});
        
        addSeries(housePriceChart, "Modelled prices", Model.collectors.housingMarketStats.priceData);
    //    addSeries(mortgageStatsChart, "Owner-occupier LTV distribution", Collectors.creditSupply.oo_ltv_distribution);
     //   addSeries(mortgageStatsChart, "Owner-occupier LTI distribution (x0.1)", Collectors.creditSupply.oo_lti_distribution);
      //  addSeries(mortgagePhaseChart, "Approved mortgages", Collectors.creditSupply.approved_mortgages);
        
        housePriceChart.addSeries(Model.collectors.housingMarketStats.referencePriceData, "Reference price", null);

        // Execute when each time-step is complete
        scheduleRepeatingImmediatelyAfter(this);

    }

    
    /** Called after each simulation step. */
    @Override
    public void step(SimState state) {
        Model myModel = (Model)state;
        double t = myModel.schedule.getTime()/12.0;
        
        for(TimeSeriesPlot plot : timeSeriesPlots) {
        	plot.recordValues(t);
        }
        
        Model.collectors.step();
    }
        
    
    
    /** Add titles and labels to charts. */

    private ScatterPlotGenerator makeScatterPlot(JTabbedPane pane, String title, String xAxis, String yAxis) {
    	ScatterPlotGenerator chart = ChartUtilities.buildScatterPlotGenerator(title, xAxis, yAxis);
        pane.addTab(title, chart);
        return(chart);
    }

    private void addSeries(ScatterPlotGenerator chart, String title, final double[][] data) {
    	ScatterPlotSeriesAttributes attributes = ChartUtilities.addSeries(chart, title);
        ChartUtilities.scheduleSeries(this, attributes, new sim.display.ChartUtilities.ProvidesDoubleDoubles() {
        	public double[][] provide() {
        		return(data);
        	}
        });
    }

    /***
    private TimeSeriesChartGenerator makeTimeSeriesChart(JTabbedPane pane, String title, String yAxis) {
    	TimeSeriesChartGenerator chart = ChartUtilities.buildTimeSeriesChartGenerator(title, yAxis);
        pane.addTab(title, chart);
        return(chart);
    }


    private void addSeries(TimeSeriesChartGenerator chart, String title, Valuable val) {
    	TimeSeriesAttributes attributes = ChartUtilities.addSeries(chart, title);
        ChartUtilities.scheduleSeries(this, attributes, val);
    }
     ***/
    
    /** Called once, when the console quits. */
    @Override
   public void quit() {
        super.quit();
        myChartFrame.dispose();
    }

    /**
     * Java entry point. This is what's called when we begin an execution
     * @param args
     */
    public static void main(String[] args) {
        // Create a console for the GUI
        Console console = new Console(new ModelGUI());
        console.setVisible(true);
    }

    ////////////////////////////////////////////////////////////////////
    // Console stuff
    ////////////////////////////////////////////////////////////////////
    /**
     * MASON stuff
     */
	@Override
	public Object getSimulationInspectedObject() {
		return state;
	}

}
