package eu.crisis_economics.abm.markets.housing;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import sim.display.Console;
import sim.display.Controller;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ChartGenerator;
import sim.util.media.chart.ScatterPlotGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;
import eu.crisis_economics.abm.simulation.SimulationGui;

/********************************************
 * Mason Graphic Interface for the housing market simulation.
 * 
 * @author daniel
 *
 ********************************************/
@SuppressWarnings("serial")
public class HousingMarketTestGUI extends SimulationGui implements Steppable {

    private javax.swing.JFrame myChartFrame;
    
    // Chart generators 
    private TimeSeriesChartGenerator 
        marketStats,
        nSalesChart,
        bidOfferChart,
        affordabilityChart;

	ScatterPlotGenerator 
		housingChart,
		housePriceChart,
		bankBalanceChart;
    
    // Plottable data
    private XYSeries 
        housePriceIndexSeries,
        soldPriceToOLPSeries,
        daysOnMarketSeries,
        nSalesSeries,
        bidSeries,
        offerSeries,
        affordabilitySeries;
    
    private double [][]    homelessData;
    private double [][]    rentingData;
    private double [][]    priceData;
    private double [][]    referencePriceData;
    private double [][]    bankBalData;
    private double [][]    referenceBankBalData;
     
    
    /** Create an instance of MySecondModelGUI */
    HousingMarketTestGUI() { 
        super(new HousingMarketTest(1L));
    }

    /** Called once, when the simulation is to begin. */
    @Override
    public void start() {
        super.start();

        marketStats.removeAllSeries();
        nSalesChart.removeAllSeries();
        housingChart.removeAllSeries();
        housePriceChart.removeAllSeries();
        bankBalanceChart.removeAllSeries();
        bidOfferChart.removeAllSeries();
        affordabilityChart.removeAllSeries();
        
        housePriceIndexSeries = new XYSeries("House Price Index", false);
        soldPriceToOLPSeries = new XYSeries("Average Sold price/List Price", false);
        daysOnMarketSeries = new XYSeries("Average years on market", false);
        nSalesSeries = new XYSeries(nSalesChart.getTitle(), false);
        bidSeries = new XYSeries("Average bid price", false);
        offerSeries = new XYSeries("Average offer price", false);
        affordabilitySeries = new XYSeries("Average FTB mortgage/income ratio", false);
        homelessData = new double[2][HousingMarketTest.N/50];
        rentingData = new double[2][HousingMarketTest.N/50];
        priceData = new double[2][House.N_QUALITY];
        referencePriceData = new double[2][House.N_QUALITY];
        bankBalData = new double[2][HousingMarketTest.N/50];
        referenceBankBalData = new double[2][HousingMarketTest.N/50];

        marketStats.addSeries(housePriceIndexSeries, null);
        marketStats.addSeries(soldPriceToOLPSeries, null);
        marketStats.addSeries(daysOnMarketSeries, null);
        nSalesChart.addSeries(nSalesSeries, null);
        housingChart.addSeries(homelessData, "Homeless", null);
        housingChart.addSeries(rentingData, "Renting", null);
        housePriceChart.addSeries(priceData, "House price", null);
        housePriceChart.addSeries(referencePriceData, "Reference price", null);
        bankBalanceChart.addSeries(bankBalData, "Bank balance", null);
        bankBalanceChart.addSeries(referenceBankBalData, "Reference bank balance", null);
        bidOfferChart.addSeries(bidSeries, null);        
        bidOfferChart.addSeries(offerSeries, null);
        affordabilityChart.addSeries(affordabilitySeries, null);
        
        int i;
        for(i=0; i<House.N_QUALITY; ++i) {
        	priceData[0][i] = i;
        	referencePriceData[0][i] = i;
        	referencePriceData[1][i] = HousingMarket.referencePrice(i);
        }
        for(i = 0; i<HousingMarketTest.N-50; i += 50) {
        	homelessData[0][i/50] = HousingMarketTest.households[i].monthlyIncome*12.0;
        	rentingData[0][i/50] = HousingMarketTest.households[i].monthlyIncome*12.0;
        	bankBalData[0][i/50] = HousingMarketTest.households[i].monthlyIncome*12.0;
        	referenceBankBalData[0][i/50] = HousingMarketTest.households[i].monthlyIncome*12.0;
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
        housePriceIndexSeries.add(t, HousingMarketTest.housingMarket.housePriceIndex, true);
        soldPriceToOLPSeries.add(t, HousingMarketTest.housingMarket.averageSoldPriceToOLP, true);
        daysOnMarketSeries.add(t, Math.min(HousingMarketTest.housingMarket.averageDaysOnMarket/360.0,1.0), true);
        nSalesSeries.add(t, HousingMarketTest.housingMarket.nSales, true);
        bidSeries.add(t, HousingMarketTest.averageBidPrice,true);
        offerSeries.add(t, HousingMarketTest.averageOfferPrice,true);
        affordabilitySeries.add(t,HousingMarketTest.bank.affordability, true);
        
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
        
        for(i=0; i<House.N_QUALITY; ++i) {
        	priceData[1][i] = HousingMarketTest.housingMarket.averageSalePrice[i];
        }
        housePriceChart.update();
        
        for(i=0; i<HousingMarketTest.N-50; i+=50) {
        	bankBalData[1][i/50] = HousingMarketTest.households[i].bankBalance;
        }
        bankBalanceChart.update();
    }
    
    
    /** Called once, to initialise display windows. */
    @Override
    public void init(Controller controller) {
        super.init(controller);
        myChartFrame = new JFrame("My Graphs");
        
        // Create and label charts
        marketStats = new TimeSeriesChartGenerator();
        nSalesChart = new TimeSeriesChartGenerator();
        bidOfferChart = new TimeSeriesChartGenerator();
        affordabilityChart = new TimeSeriesChartGenerator();
        housingChart = new ScatterPlotGenerator();
        housePriceChart = new ScatterPlotGenerator();
        bankBalanceChart = new ScatterPlotGenerator();
        
        this.makeChartLabels(marketStats, "Market stats", "", "Time (years)");
        this.makeChartLabels(nSalesChart, "Number of sales/month", "Number", "Time (years)");
        this.makeChartLabels(housingChart, "Housing stats", "Probability", "Household Income");
        this.makeChartLabels(housePriceChart, "House prices", "Price", "Quality");
        this.makeChartLabels(bankBalanceChart, "Bank balances", "Balance", "Income");
        this.makeChartLabels(bidOfferChart, "Average Bid/Offer price", "Price", "Time");
        this.makeChartLabels(affordabilityChart, "FTB mortgage payment/income ratio", "Ratio", "Time");
        
        controller.registerFrame(myChartFrame);
       
        // Create a tab interface
        JTabbedPane newTabPane = new JTabbedPane();
        newTabPane.addTab(marketStats.getTitle(), marketStats);
        newTabPane.addTab(nSalesChart.getTitle(), nSalesChart);
        newTabPane.addTab(housingChart.getTitle(), housingChart);
        newTabPane.addTab(housePriceChart.getTitle(), housePriceChart);
        newTabPane.addTab(bankBalanceChart.getTitle(), bankBalanceChart);
        newTabPane.addTab(bidOfferChart.getTitle(), bidOfferChart);
        newTabPane.addTab(affordabilityChart.getTitle(), affordabilityChart);
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

//        saveChart(marketStats,"./output/sim4marketStats.png");
 //       saveChart(housingChart,"./output/sim4housingType.png");
  //      saveChart(housePriceChart,"./output/sim4housePrices.png");
   //     saveChart(bankBalanceChart,"./output/sim4bankBalances.png");
    //    saveChart(nSalesChart,"./output/sim4salesPerMonth.png");
        
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
}
