package housing;

import sim.display.ChartUtilities;
import sim.display.ChartUtilities.ProvidesDoubleDoubles;
import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ScatterPlotGenerator;
import sim.util.media.chart.ScatterPlotSeriesAttributes;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;

public class ModelGUI2 extends GUIState implements Steppable {

	private static final long serialVersionUID = -7604303608565084623L;

	// Chart generators
  
//	public ScatterPlotGenerator
//		housePriceChart
//		;
	
    public TimeSeriesChartGenerator 
    	marketStats
    	;
    
    public TimeSeriesAttributes 	
    	hpi,
     	daysOnMarket
//     	nHouseholds,
 //    	nPrivateHousing,
  //   	ownerOccupier
     	;
    
//    public ScatterPlotSeriesAttributes
//    	housePrices;
    
    
    /** Create an instance of the model and connect it to the GUI */
    ModelGUI2() { 
        super(new Model(1L));
    }

    
        
    /** Called once, to initialise display windows. */
    @Override
    public void init(Controller controller) {
        super.init(controller);
                
        marketStats = ChartUtilities.buildTimeSeriesChartGenerator(this, "Market statistics", "Time");
        marketStats.setYAxisLabel("Index");
        hpi = ChartUtilities.addSeries(marketStats, "House Price Index");
        daysOnMarket = ChartUtilities.addSeries(marketStats, "Days on market");

//        housePriceChart = ChartUtilities.buildScatterPlotGenerator(this, "House Prices", "Reference Price", "Modelled Price");
//        housePrices = ChartUtilities.addSeries(housePriceChart,"Modelled prices");
        
//        tenure = ChartUtilities.buildTimeSeriesChartGenerator(this, "Household tenure", "Time");
//        ownerOccupier = ChartUtilities.addSeries(tenure, "Owner Occupiers");
 //       nPrivateHousing = ChartUtilities.addSeries(tenure, "Private Rental");
  //      nHouseholds = ChartUtilities.addSeries(tenure, "Social housing");
    }

    /** Called once, when the simulation is to begin. */
    @Override
    public void start() {
        super.start();        
        marketStats.clearAllSeries();
        scheduleSeries();
    }

    public void load(final SimState state) {
    	super.load(state);
    	scheduleSeries();
    }
    
    void scheduleSeries() {
        ChartUtilities.scheduleSeries(this, hpi, new sim.util.Valuable() {
        	public double doubleValue() {return Model.housingMarket.housePriceIndex; }
        });
        ChartUtilities.scheduleSeries(this, daysOnMarket, new sim.util.Valuable() {
        	public double doubleValue() {return Model.housingMarket.averageDaysOnMarket/365.0; }
        });

//        ChartUtilities.scheduleSeries(this, housePrices, new ProvidesDoubleDoubles() {
//			public double[][] provide() {return Model.collectors.housingMarketStats.priceData();
//			}});    	
    }

    
    /** Called after each simulation step. */
    @Override
    public void step(SimState state) {
    }

    /** Called once, when the console quits. */
    @Override
   public void quit() {
        super.quit();
    }

    /** Mason puts this in the "Console" tab */
	@Override
	public Object getSimulationInspectedObject() {
		return state;
	}

    /**
     * Java entry point. This is what's called when we begin an execution
     * @param args
     */
    public static void main(String[] args) {
        // Create a console for the GUI
        Console console = new Console(new ModelGUI2());
        console.setVisible(true);
    }

}
