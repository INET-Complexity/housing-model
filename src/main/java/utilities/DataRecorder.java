package utilities;

import java.lang.reflect.Field;

import org.jfree.data.xy.XYSeries;

/**
 * This class represents a set of data points in a timeseries.
 * Use this to record a value that you later want to plot a
 * timeseries of.
 * 
 * @author daniel
 *
 */
public class DataRecorder extends XYSeries {
	private static final long serialVersionUID = 6740853271859360767L;

	static public int MAX_DATA_LEN = 400;	// Maximum number of data points to remember
	static public int CHUNK_SIZE = 5;		// number of datapoints to delete at a time
	
	public interface Transform {
		double exec(double x);
	}

	/**
	 * Construct a new DataRecorder
	 * 
	 * @param o	The object that contains the value to record.
	 * @param varName The name of the variable to record
	 * @param title A description of the recorded value.
	 */
	public DataRecorder(Object o, String varName, String title) {
		this(o,varName,title,null);
	}
	
	/**
	 * Construct a new DataRecorder
	 * 
	 * @param o	The object that contains the value to record.
	 * @param varName The name of the variable to record
	 * @param title A description of the recorded value.
	 * @param t A transform that should be applied to the value before recording.
	 */
	public DataRecorder(Object o, String varName, String title, Transform t) {
		super(title, false);
		transform = t;
		obj = o;
		description = title;
		try {
			field = obj.getClass().getField(varName);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Take a sample of the value now.
	 * @param timestamp the timestamp to associate with this sample.
	 */
	public void record(double timestamp) {		
		try {
			if(transform == null) {
				add(timestamp, field.getDouble(obj) , true);
			} else {
				add(timestamp, transform.exec(field.getDouble(obj)) , true);				
			}
			if(getItemCount() > MAX_DATA_LEN) delete(0,CHUNK_SIZE);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Object 		obj;
	Field		field;
	String 		description;
	Transform	transform;
}
