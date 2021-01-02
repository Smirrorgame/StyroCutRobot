package robCalibration;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import robprakt.Constants;
import robprakt.graphics.Controller;
import robprakt.network.TCPClient;


/**
 * QR24 contains methods for QR24-calibration-algorithm described in the paper "Non-orthogonal tool/flange and robot/world calibration".
 * Using the algorithm it is possible to calibrate a six degrees-of-freedom robot manipulator by means of an suitable tracking system.
 * To calculate the two wanted matrices where the first matrix transforms from the end-effector to the marker of the tracking system and
 * the second matrix transforms from the base of the robot to the tracking system's sensor, it is necessary to take at least two
 * measurements while each measurement contains the robot pose-matrix  and transformation matrix for transform from tracker sensor to marker.
 * 
 * ===
 * CONVENTIONS for this class:
 * continuously using 4x4 homogeneous matrices
 * unit of length: millimeter
 * 
 * ===
 * 
 * Fragen:
 * Kann man die selbst spezifizierten Matrizen für die Pose des Roboters für die Berechnung verwenden,
 * oder sollte man die Position vom Roboter nochmals abfragen? --> Möglicherweise ist die Matrix des Roboters
 * leicht anders als die, die man anfahren wollte. (praktisch ausprobieren)
 * 
 */
public class QR24 {
	
	/**
	 * GUI Controller used to send data to servers
	 */
	private Controller controller;
	
	//TODO: Setting number of measurements in GUI (has to be greater than 1). if so: change private to protected
	/**
	 * Number of measurements.
	 * Has to be greater than 1.
	 */
	private int numberOfMeasurements = Constants.DEFAULT_NUM_MEASUREMENTS;
	
	private double[][] initialMarkerPose = {{1,0,0,1000},{0,1,0,1250},{0,0,1,1300},{0,0,0,1}}; //TODO: set more useful initial value 
	
	//TODO: Setting localWorkspaceMidpoint dynamically e.g. in GUI
	/**
	 * Determines the midpoint of the local workspace.
	 * Used for defining space in which the calibration is done.
	 */
	private double[] localWorkspaceMidpoint = {1000.0,1250.0,1300.0};
	
	//TODO: Setting radiusWorkspace dynamically e.g. in GUI
	/**
	 * Defines radius of spherical workspace.
	 */
	private double radiusWorkspace = 500;
	
	/**
	 * List of specified robot pose matrices.
	 * Index i refers to the i-1 measurement.
	 */
	protected ArrayList<RealMatrix> poseMatrices = new ArrayList<RealMatrix>();
	
	/**
	 * List of measured pose-matrices of the marker.
	 * Index i refers to the i-1 measurement.
	 */
	protected ArrayList<RealMatrix> measuredPosesOfMarker = new ArrayList<RealMatrix>();
	
	/**
	 * The Constructor
	 * @param c the controller for sending Messages to Robots and Tracking System 
	 */
	public QR24 (Controller c){
		this.controller = c;
	}
	
	/**
	 * Setter for NumberOfMeasurments
	 * @param num count of measurements
	 * @return true if num greater than 1 and smaller than 501
	 */
	public boolean setNumberOfMeasurements(int num) {
		if(1<num && 501>num) {
			this.numberOfMeasurements = num;
			return true;
		}
		System.out.println("Number of measurements have to be greater than one and smaller than 501./nMaybe the initialization in [calibrationMenu] of num broke.");
		return false;
	}
	
	/**
	 * Setter for InitialMarkerPose
	 * @param matrix3x4 is a matrix send by tracking-system
	 */
	public void setInitialMarkerPose(double[] matrix3x4){
		ArrayList<Double> doubleList = new ArrayList<Double>(matrix3x4.length);
		for (double i : matrix3x4)
		{
		    doubleList.add(i);
		}
		if(matrix3x4.length != 12) System.out.println("[QR24] Setting localWorkspaceMidpoint wasn't successful, cause matrix is corrupted.");
		doubleList.add(0.0);
		doubleList.add(0.0);
		doubleList.add(0.0);
		doubleList.add(1.0);
		System.out.println(doubleList);
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 4; col++) {
				System.out.println("test: " + row + col);
				
				this.initialMarkerPose[row][col] = doubleList.get(3*row+col);
			}
		}
	}
	
	public void setLocalWorkspaceMidpoint(double[] matrix3x4) {
		if(matrix3x4.length != 12) System.out.println("[QR24] Setting localWorkspaceMidpoint wasn't successful, cause matrix is corrupted.");
		this.localWorkspaceMidpoint[0] = matrix3x4[3];
		this.localWorkspaceMidpoint[1] = matrix3x4[4+3];
		this.localWorkspaceMidpoint[2] = matrix3x4[4+4+3];
	}
	
	/**
	 * Creates random homogeneous robot pose matrices for a spherical limited workspace.
	 * Values of translational part is limited through sphere with radiusWorkspace.
	 * Rotational part is limited in a way, so that the tracking system is always able to
	 * have direct line of sight to the marker on the robot's end effector.
	 * Stores pose matrices into poseMatrices-list-
	 * 
	 * @param basicOrientationOfMarker	Pose of the marker at localWorkspaceMidpoint, where markers-plane
	 * 									is parallel to sensors-plane (has to be configured manually).Rotational
	 * 									part is used to define orientation of the marker.
	 */
	//TODO: using SET-UP configuration to manually configure basicOrientationOfMarker regarding the orientation of tracking system and marker
	private void createRobotPoseHomMatrices(RealMatrix basicOrientationOfMarker) {
		
		Random random = new Random();
		double alpha_x;	//angle for rotation around x-axis
		double beta_y;	//angle for rotation around y-axis
		double gamma_z;	//angle for rotation around z-axis
		double phase = Math.PI*(21/45);	//phase of 84°, in radians
		for(int cnt = 1; cnt <= numberOfMeasurements; cnt++) {
		
			//ROTATIONAL PART
			//creating random angle-value between -84 and 84 degrees
			alpha_x = ((0.5-random.nextDouble())*2)*phase;
			beta_y = ((0.5-random.nextDouble())*2)*phase;
			gamma_z = ((0.5-random.nextDouble())*2)*phase;
			//extrinsic rotation around fixed axis (Rx-Ry-Rz)
			double a11 = Math.cos(beta_y)*Math.cos(gamma_z);
			double a12 = -Math.cos(beta_y)*Math.sin(gamma_z);
			double a13 = Math.sin(beta_y);
			double a21 = Math.cos(alpha_x)*Math.sin(gamma_z) + Math.cos(gamma_z)*Math.sin(alpha_x)*Math.sin(beta_y);
			double a22 = Math.cos(alpha_x)*Math.cos(gamma_z) - Math.sin(alpha_x)*Math.sin(beta_y)*Math.sin(gamma_z);
			double a23 = -Math.cos(beta_y)*Math.sin(alpha_x);
			double a31 = Math.sin(alpha_x)*Math.sin(gamma_z) - Math.cos(alpha_x)*Math.cos(gamma_z)*Math.sin(beta_y);
			double a32 = Math.cos(gamma_z)*Math.sin(alpha_x) + Math.cos(alpha_x)*Math.sin(beta_y)*Math.sin(gamma_z);
			double a33 = Math.cos(alpha_x)*Math.cos(beta_y);

			//creating pose matrix
			double[][] robPoseMatrixData = {{a11,a12,a13,0},{a21,a22,a23,0},{a31,a32,a33,0},{0d,0d,0d,1d}};
			RealMatrix robPoseMatrix = new Array2DRowRealMatrix (robPoseMatrixData);
			//changing orientation regarding the basic orientation of the marker relative to the tracking sensor
			//TODO: Validate if this is the correct order of multiplication
			robPoseMatrix = robPoseMatrix.multiply(basicOrientationOfMarker);
			
			//TRANSLATIONAL PART
			//generating random values inside a sphere defined by radiusWorkspace
			robPoseMatrix.setEntry(0, 3, ((0.5-random.nextDouble())*2)*radiusWorkspace);
			robPoseMatrix.setEntry(1, 3, ((0.5-random.nextDouble())*2)*radiusWorkspace);
			robPoseMatrix.setEntry(2, 3, ((0.5-random.nextDouble())*2)*radiusWorkspace);
			
			//add generated pose matrix to list
			poseMatrices.add(robPoseMatrix);
		}
	}
	
	public boolean measuring(TCPClient clientRob) throws InterruptedException {
		RealMatrix robPoseMatrix;
		
		//create random pose matrices
		createRobotPoseHomMatrices(new Array2DRowRealMatrix(initialMarkerPose));
		
		//get TCP-client for the tracking-system
		TCPClient clientTS = controller.getClientTS();
		for(int cnt = 0; cnt < numberOfMeasurements; cnt++) {
			robPoseMatrix = poseMatrices.get(cnt);
			
			String data = "MoveMinChangeRowWiseStatus" 	+ " " + robPoseMatrix.getEntry(0,0) + " " + robPoseMatrix.getEntry(0, 1) + " " + robPoseMatrix.getEntry(0,2) + " " + robPoseMatrix.getEntry(0, 3)
														+ " " + robPoseMatrix.getEntry(1,0) + " " + robPoseMatrix.getEntry(1, 1) + " " + robPoseMatrix.getEntry(1,2) + " " + robPoseMatrix.getEntry(1, 3)
														+ " " + robPoseMatrix.getEntry(2,0) + " " + robPoseMatrix.getEntry(2, 1) + " " + robPoseMatrix.getEntry(2,2) + " " + robPoseMatrix.getEntry(2, 3)
														+ " " + " righty"; //TODO: Is "righty correct?"
			//send command to robot
			controller.send(data, clientRob);
			//wait a certain amount of time, till robot reaches pose
			try {
				TimeUnit.MILLISECONDS.sleep((long) (2*radiusWorkspace/(Constants.MAX_COMPOSITE_SPEED*Constants.MAX_ALLOWED_SPEED_RATIO)));
			} catch (InterruptedException e) {
				System.out.println("[QR24] While waiting for the robot to reach pose, the thread has been interrupted.");
			}
			
			//TODO: create initial setup with robot, so it only sends MATRIXROWWISE data
			//TODO: extract values from the tracking system response and add them to the list
			//TODO: create sendToTrackingSystem Method or use the controllers send method directly
			controller.send("CM_NEXTVALUE",clientTS);
			double[] trackingData = Constants.convertPoseDataToDoubleArray(controller.response(clientTS));
			
			//create RealMatrix out off the data that was send by tracking-system
			double[][] trackingData2DArray = {{trackingData[0],trackingData[1],trackingData[2],trackingData[3]},{trackingData[4],trackingData[5],trackingData[6],trackingData[7]},{trackingData[8],trackingData[9],trackingData[10],trackingData[11]},{0,0,0,1}};
			RealMatrix m = new Array2DRowRealMatrix (trackingData2DArray);
			//adding measured matrix to list
			this.measuredPosesOfMarker.add(m);
		}	
		//TODO: return something useful...
		return true;
	}
	
	public void start() {
		
		double degrees = Math.toRadians(45);

		double[][] m = {
				{Math.cos(degrees), -Math.sin(degrees), 0d, 0d},
				{Math.sin(degrees), Math.cos(degrees), 0d, 50d},
//				{0, -1, 0, 0},
//				{1, 0, 0, 50},
				{0d, 0d, 1d, 0d},
				{0d, 0d, 0d, 1d}};
		
		double[][] x = {
				{1d, 0d, 0d, 200d},
				{0d, 1d, 0d, -50d},
				{0d, 0d, 1d, 100d},
				{0d, 0d, 0d, 1d}};
		
		double degrees2 = Math.toRadians(45);
		
		double[][] y = {
				{Math.cos(degrees2), -Math.sin(degrees2), 0d, 20d},
				{Math.sin(degrees2), Math.cos(degrees2), 0d, 0d},
				{0d, 0d, 1d, 10d},
				{0d, 0d, 0d, 1d}};
		
		double[][] n = {
				{1d, 0d, 0d, 180d},
				{0d, 1d, 0d, 0d},
				{0d, 0d, 1d, 90d},
				{0d, 0d, 0d, 1d}};
		
		RealMatrix M = new Array2DRowRealMatrix(m);
		RealMatrix X = new Array2DRowRealMatrix(x);
		RealMatrix Y = new Array2DRowRealMatrix(y);
		RealMatrix N = new Array2DRowRealMatrix(n);
		
		
		int measures = 1; 
		RealMatrix A = new Array2DRowRealMatrix(12*measures, 24*measures);
		
		// A Matrix Aufbauen
		A.setSubMatrix(createAEntry(M, N).getData(), 0, 0);
		RealVector B = new ArrayRealVector(12*measures);
		B.setSubVector(0, createBEntry(M));
		
		System.out.println("M_invers");print(new LUDecomposition(M).getSolver().getInverse());
		System.out.println("M = ");print(M);
		System.out.println("X = ");print(X);
		System.out.println("Y = ");print(Y);
		System.out.println("N = ");print(N);
		System.out.println("A = ");print(A);
		System.out.println("B = ");System.out.println(B);
		System.out.println();
		
		System.out.println("MX=YN:");
		print(M.multiply(X));
		print(Y.multiply(N));
		
		DecompositionSolver solver = new QRDecomposition(A).getSolver();
		RealVector Elements = solver.solve(B);
		System.out.println("Elements:");
		System.out.println(Elements);
		
		RealVector xVals = Elements.getSubVector(0, 12);
		RealVector yVals = Elements.getSubVector(12, 12);
		
		System.out.println(xVals);
		System.out.println(yVals);
		
	}
	
	private RealMatrix createAEntry(RealMatrix m,RealMatrix n) {
		
		RealMatrix RM = getRot(m);
		
		RM = new LUDecomposition(RM).getSolver().getInverse();
		RealMatrix N = n.copy();
		RealMatrix Ai = new Array2DRowRealMatrix(12,24);
		RealMatrix Z = new Array2DRowRealMatrix(3,3);
		RealMatrix Identity12 = MatrixUtils.createRealIdentityMatrix(12);
		
		// erste Spalte
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(0, 0)).getData(), 0, 0);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(0, 1)).getData(), 3, 0);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(0, 2)).getData(), 6, 0);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(0, 3)).getData(), 9, 0);
		                                                   
		//zweite Spalte
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(1, 0)).getData(), 0, 3);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(1, 1)).getData(), 3, 3);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(1, 2)).getData(), 6, 3);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(1, 3)).getData(), 9, 3);

		//dritte spalte
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(2, 0)).getData(), 0, 6);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(2, 1)).getData(), 3, 6);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(2, 2)).getData(), 6, 6);
		Ai.setSubMatrix(RM.scalarMultiply(N.getEntry(2, 3)).getData(), 9, 6);
		
		//vierte Spalte
		Ai.setSubMatrix(Z.getData(), 0, 9);
		Ai.setSubMatrix(Z.getData(), 3, 9);
		Ai.setSubMatrix(Z.getData(), 6, 9);
		Ai.setSubMatrix(RM.getData(), 9, 9);
		
		//fünfte Spalte
		Ai.setSubMatrix(Identity12.scalarMultiply(-1d).getData(), 0, 12);
		return Ai;
	}
	
	private RealVector createBEntry(RealMatrix m) {
		RealMatrix bi = new Array2DRowRealMatrix(1,12);
		
		RealMatrix negT_M = getTrans(new LUDecomposition(m).getSolver().getInverse()).scalarMultiply(-1d).transpose();
		bi.setSubMatrix(negT_M.getData(), 0, 9);
		
		return bi.getRowVector(0);
	}
	
	private RealMatrix getRot(RealMatrix mat) {
		return mat.copy().getSubMatrix(
				new int[] {0,1,2}, new int[] {0,1,2});
	}
	
	private RealMatrix getTrans(RealMatrix mat) {
		return mat.copy().getSubMatrix(new int[] {0,1,2}, new int[] {3});
	}

	public void print(RealMatrix m) {
		
		double[][] arr = m.getData();
		
		for (int row = 0; row < arr.length; row++)//Cycles through rows
		{
		  for (int col = 0; col < arr[row].length; col++)//Cycles through columns
		  {
		    System.out.print(arr[row][col]+" "); //change the %5d to however much space you want
		  }
		  System.out.println(); //Makes a new row
		}
		System.out.println();
		//This allows you to print the array as matrix
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
