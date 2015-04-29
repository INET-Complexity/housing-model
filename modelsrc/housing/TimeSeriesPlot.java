package housing;

import java.util.ArrayList;

import javax.swing.JTabbedPane;

import sim.util.media.chart.TimeSeriesChartGenerator;

/**
 * This is a convenient wrapper for plotting time series 
 * @author daniel
 *
 */
public class TimeSeriesPlot {
	/**
	 * Constructor
	 * @param title Title of the plot
	 * @param xAxisLabel Label for the x axis
	 * @param yAxisLabel Label for the y axis
	 */
	public TimeSeriesPlot(String title, String xAxisLabel, String yAxisLabel) {
		dataSeries = new ArrayList<DataRecorder>(4);
		chart = new TimeSeriesChartGenerator();
		
        chart.setTitle(title);
        chart.setYAxisLabel(yAxisLabel);
        chart.setXAxisLabel(xAxisLabel);
	}
	
	/**
	 * Add this plot to a pane
	 * @param pane
	 */
	public void addToPane(JTabbedPane pane) {
        pane.addTab(chart.getTitle(), chart);
	}

	/**
	 * Add a variable to this plot
	 * @param obj The object that contains the variable to plot
	 * @param varName The name of the variable to plot
	 * @param title A description of the variable
	 * @return this
	 */
	public TimeSeriesPlot addVariable(Object obj, String varName, String title) {
		return(addVariable(obj,varName,title,null));
	}
	
	/**
	 * Add a variable to this plot.
	 * @param obj The object that contains the variable to plot
	 * @param varName The name of the variable to plot
	 * @param title A description of the variable
	 * @param t A transform to apply to the variable before plotting
	 * @return this
	 */
	public TimeSeriesPlot addVariable(Object obj, String varName, String title, DataRecorder.Transform t) {
		DataRecorder series = new DataRecorder(obj, varName, title, t);
		dataSeries.add(series);
		chart.addSeries(series, null);
		return(this);
	}
	
	/**
	 * Sample all values that are on this plot now.
	 * @param t The timestamp to associate with this sample.
	 */
	public void recordValues(double t) {
		for(DataRecorder series : dataSeries) {
			series.record(t);
		}
	}
	
	TimeSeriesChartGenerator 	chart;
	ArrayList<DataRecorder>		dataSeries;
}
