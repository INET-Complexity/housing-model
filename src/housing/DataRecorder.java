package housing;

import java.lang.reflect.Field;

import org.jfree.data.xy.XYSeries;

@SuppressWarnings("serial")
public class DataRecorder extends XYSeries {
	
	static public int MAX_DATA_LEN = 400;	// Maximum number of data points to remember
	static public int CHUNK_SIZE = 5;		// number of datapoints to delete at a time
	
	static interface Transform {
		public double exec(double x);
	}

	public DataRecorder(Object o, String varName, String title) {
		this(o,varName,title,null);
	}
	
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
