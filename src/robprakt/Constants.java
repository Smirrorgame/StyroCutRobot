package robprakt;

public class Constants {
	
	/**
	 * width of window
	 */
	final static public int mainFrameWidth = 800;
	
	/**
	 * height of window
	 */
	final static public int mainFrameHeight = 600;
	
	/**
	 * composite speed of Adept Viper S850 in mm/s
	 */
	final static public double MAX_COMPOSITE_SPEED = 7600; 
	
	/**
	 * maximum allowed speed in labor in percentage
	 */
	final static public double MAX_ALLOWED_SPEED_RATIO = 0.25;
	
	/**
	 * default number of measurements
	 */
	final static public int DEFAULT_NUM_MEASUREMENTS = 50;
	
	/**
	 * default position of the workspace
	 */
	final static public double[] DEFAULT_LOCAL_WORKSPACE_MIDPOINT = {0.0,200.0,40.0};
	
	public static final double[] convertPoseDataToDoubleArray(String data, int index) {
		String[] dataStringArray = data.split(" ");
		double[] dataDoubleArray = new double[12];
		try
	    {
			for(int counter = index; counter < dataDoubleArray.length; counter++) {
				dataDoubleArray[counter-index] = Double.parseDouble(dataStringArray[counter]);
			}
	    }
	    catch (NumberFormatException nfe)
	    {
	      System.out.println("[Constants] An error occured while converting an String array to double array.");
	    }
		return dataDoubleArray;
	}
}
