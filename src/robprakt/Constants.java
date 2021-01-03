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
	final static public double MAX_ALLOWED_SPEED_RATIO = 25;
	
	/**
	 * default number of measurements
	 */
	final static public int DEFAULT_NUM_MEASUREMENTS = 50;
	
	public static final double[] convertPoseDataToDoubleArray(String data) {
		String[] dataStringArray = data.split(" ");
		double[] dataDoubleArray = {0,0,0,0,0,0,0,0,0,0,0,0,};
		try
	    {
			for(int counter = 0; counter < dataStringArray.length; counter++) {
				//TODO: REMOVE PRINT
				System.out.println("String to be converted: " + dataStringArray[counter] + "test");
				dataDoubleArray[counter] = Double.parseDouble(dataStringArray[counter]);
			}
	    }
	    catch (NumberFormatException nfe)
	    {
	      System.out.println("[Constants] An error occured while converting an String array to double array.");
	    }
		return dataDoubleArray;
	}
}
