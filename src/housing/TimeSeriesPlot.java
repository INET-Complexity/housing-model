package housing;

import java.util.ArrayList;

import javax.swing.JTabbedPane;

import sim.util.media.chart.TimeSeriesChartGenerator;

public class TimeSeriesPlot {
	
	public TimeSeriesPlot(String title, String xAxisLabel, String yAxisLabel) {
		dataSeries = new ArrayList<DataRecorder>();
		chart = new TimeSeriesChartGenerator();
		
        chart.setTitle(title);
        chart.setYAxisLabel(yAxisLabel);
        chart.setXAxisLabel(xAxisLabel);
	}
	
	public void addToPane(JTabbedPane pane) {
        pane.addTab(chart.getTitle(), chart);
	}

	public TimeSeriesPlot addVariable(Object obj, String varName, String title) {
		return(addVariable(obj,varName,title,null));
	}
	
	public TimeSeriesPlot addVariable(Object obj, String varName, String title, DataRecorder.Transform t) {
		DataRecorder series = new DataRecorder(obj, varName, title, t);
		dataSeries.add(series);
		chart.addSeries(series, null);
		return(this);
	}
	
	public void recordValues(double t) {
		for(DataRecorder series : dataSeries) {
			series.record(t);
		}
	}
	
	TimeSeriesChartGenerator 	chart;
	ArrayList<DataRecorder>		dataSeries;
}
