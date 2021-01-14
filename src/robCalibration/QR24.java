package robCalibration;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

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

/*
 * TODO:
 * Unterschied zwischen LU- und QR-Decomposition ermitteln.
 * Standard Algorithmus nutzen, statt den korrupten
 */
public class QR24 {
	
	/**
	 * GUI Controller used to send data to servers
	 */
	private Controller controller;
	
	/**
	 * Number of measurements.
	 * Has to be greater than 1.
	 */
	private int numberOfMeasurements = Constants.DEFAULT_NUM_MEASUREMENTS;
	
	private double[][] initialMarkerPose = {{1,0,0,1000},{0,1,0,1250},{0,0,1,1300},{0,0,0,1}}; //TODO: set more useful initial value 

	/**
	 * Determines the midpoint of the local workspace.
	 * Used for defining space in which the calibration is done.
	 */
//	private double[] localWorkspaceMidpoint = {1250.0,1250.0,1300.0};
	private double[] localWorkspaceMidpoint = {0.0,200.0,40.0};
	
	/**
	 * Defines radius of spherical workspace.
	 */
	private double radiusWorkspace = 50;
	
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
		for (double i : matrix3x4) {
		    doubleList.add(i);
		}
		if(matrix3x4.length != 12) System.out.println("[QR24] Setting localWorkspaceMidpoint wasn't successful, cause matrix is corrupted.");
		doubleList.add(0.0d);
		doubleList.add(0.0d);
		doubleList.add(0.0d);
		doubleList.add(1.0d);
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 4; col++) {
				this.initialMarkerPose[row][col] = doubleList.get(4*row+col);
			}
		}
	}
	
	/**
	 * this method sets the local workspace midpoint coordinates to the translational values of the given Matrix
	 * @param matrix3x4 the matrix with the new local workspace midpoint ccordinates
	 */
	public void setLocalWorkspaceMidpoint(double[] matrix3x4) {
		if(matrix3x4.length != 12) System.out.println("[QR24] Setting localWorkspaceMidpoint wasn't successful, because matrix is corrupted.");
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
		double phase = Math.toRadians(15);//Math.PI*(21d/45d);	//phase of 84°, in radians
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
			robPoseMatrix.setEntry(0, 3, ((0.5-random.nextDouble())*2)*radiusWorkspace + localWorkspaceMidpoint[0]);
			robPoseMatrix.setEntry(1, 3, ((0.5-random.nextDouble())*2)*radiusWorkspace + localWorkspaceMidpoint[1]);
			robPoseMatrix.setEntry(2, 3, ((0.5-random.nextDouble())*2)*radiusWorkspace + localWorkspaceMidpoint[2]);
			//add generated pose matrix to list
			poseMatrices.add(robPoseMatrix);
		}
	}
	
	/**
	 * This method sets the robot to random orientations respective to the local
	 * workspace midpoint and the workspace radius. This orientation will then be
	 * measured again and also the orientation of the tracker will be measured.
	 * Those orientations will then be added to a set of measurements. 
	 *  
	 * @param clientRob the robot client which is currently being measured on
	 * @return true if measurement was successful
	 * @throws InterruptedException when the Thread has been interrupted
	 */
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
														+ " " + "noflip lefty"; //TODO: Is "righty correct?"
			//send command to robot
			controller.send(data, clientRob);
			controller.response(clientRob);
//			System.out.println("[QR24:measuring] "+controller.response(clientRob)+"\n at iteration "+cnt);
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
			String s = controller.response(clientTS);
			
			//TODO: Which values does the tracking-system send back to the client for FORMAT_MATRIXROWWISE? --> example from manual seems to be wrong
			double[] trackingData = Constants.convertPoseDataToDoubleArray(s, 2);
			
			//create RealMatrix out off the data that was send by tracking-system
			double[][] trackingData2DArray = {{trackingData[0],trackingData[1],trackingData[2],trackingData[3]},{trackingData[4],trackingData[5],trackingData[6],trackingData[7]},{trackingData[8],trackingData[9],trackingData[10],trackingData[11]},{0,0,0,1}};
			RealMatrix m = new Array2DRowRealMatrix (trackingData2DArray);
			//adding measured matrix to list
			this.measuredPosesOfMarker.add(m);
		}	
		return true;
	}
	
	/**
	 * This Method takes the measured matrices set M and N and creates a linear equation system to solve
	 * for matrices X and Y
	 * @param M consisting of Mi
	 * @param N consisting if Ni
	 * @return An array containing the matrix X and Y, leading with X 
	 * @throws Exception Error when there're no measurements
	 */
	public RealMatrix[] calibrate() throws Exception {
		
		// if there's not data measured throw an error
		if (poseMatrices.size()<=0 || measuredPosesOfMarker.size()<=0) {
			throw new Exception("Keine Messungen vorhanden!");
		}
		
		System.out.println("[Calibrate] poseMatrices:"+poseMatrices.size()+", Measured: "+measuredPosesOfMarker.size());
		
		// create A and B matrix/vector related to the number of measurements
		RealMatrix A = new Array2DRowRealMatrix(12*this.numberOfMeasurements, 24);
		RealVector B = new ArrayRealVector(12*this.numberOfMeasurements);
				
		// Here are the coefficientmatrix A and the solution vector B
		// generated from all given Measurements Mi and Ni
		for(int cnt=0;cnt<poseMatrices.size();cnt++) {
			A.setSubMatrix(createAEntry(poseMatrices.get(cnt), measuredPosesOfMarker.get(cnt)).getData(), cnt*12, 0);
			B.setSubVector(cnt*12, new ArrayRealVector(createBEntry(poseMatrices.get(cnt))));
		}
		
		// create solver for the linear equation system and solve it 
		DecompositionSolver solver = new QRDecomposition(A).getSolver();
		RealVector w = solver.solve(B);
		
		// generate Matrix X and Y from the solved vector
		RealMatrix Y = getFromW(w.getSubVector(12, 12));
		RealMatrix X = getFromW(w.getSubVector(0, 12));
		
		// orthonormalize the rotational part of the X and Y matrices
		RealMatrix XRot = new SingularValueDecomposition(getRot(X)).getV();
		RealMatrix YRot = new SingularValueDecomposition(getRot(Y)).getV();
		X.setSubMatrix(XRot.getData(), 0, 0);
		Y.setSubMatrix(YRot.getData(), 0, 0);
		
		// return the calculated X and Y matrices
		return new RealMatrix[] {X,Y};
	}
	
	/**
	 * Berechnet eine Transformationsmatrix anhand eines gegebenen Vektors 
	 * @param w Vektor mit Eintr�gen der Matrix
	 * @return die berechnete Matrix
	 */
	public RealMatrix getFromW(RealVector w) {
		RealMatrix M = new Array2DRowRealMatrix(4,4);
		
		M.setEntry(0, 0, w.getEntry(0));
		M.setEntry(1, 0, w.getEntry(1));
		M.setEntry(2, 0, w.getEntry(2));
		M.setEntry(0, 1, w.getEntry(3));
		M.setEntry(1, 1, w.getEntry(4));
		M.setEntry(2, 1, w.getEntry(5));
		M.setEntry(0, 2, w.getEntry(6));
		M.setEntry(1, 2, w.getEntry(7));
		M.setEntry(2, 2, w.getEntry(8));
		M.setEntry(0, 3, w.getEntry(9));
		M.setEntry(1, 3, w.getEntry(10));
		M.setEntry(2, 3, w.getEntry(11));
		M.setEntry(3, 3, 1);
		
		return M;
	}
		
	/**
	 * create A matrix for a single pair of measuring data M and N
	 * @param m pose matrix of robot
	 * @param n measured data by tracking system
	 * @return Ai matrix for a single pair of measuring data M and N
	 */
	private RealMatrix createAEntry(RealMatrix m,RealMatrix n) {
		
		RealMatrix rotM = getRot(m);
		
		RealMatrix N = n.copy();
		RealMatrix Ai = new Array2DRowRealMatrix(12,24); //12x24 matrix for one pair of measured matrices (M,N)
		RealMatrix Z = new Array2DRowRealMatrix(3,3); //3x3 zero-matrix
		RealMatrix Identity12 = MatrixUtils.createRealIdentityMatrix(12); //12x12 identity-matrix
		
		// first column
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(0, 0)).getData(), 0, 0);
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(1, 0)).getData(), 3, 0);
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(2, 0)).getData(), 6, 0);
		
		//TODO: in den letzten Zeilen die Werte mit t bestehend aus gewissen Nij multiplizieren
		double t1 = -(N.getEntry(0, 0)*N.getEntry(0, 3)+
				N.getEntry(1,0)*N.getEntry(1,3)+
				N.getEntry(2, 0)*N.getEntry(2, 3));
		
		Ai.setSubMatrix(rotM.scalarMultiply(t1).getData(), 9, 0);
		                                                   
		// second column
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(0, 1)).getData(), 0, 3);
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(1, 1)).getData(), 3, 3);
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(2, 1)).getData(), 6, 3);
		
		double t2 = -(N.getEntry(0, 1)*N.getEntry(0,3)+
				N.getEntry(1,1)*N.getEntry(1,3)+
				N.getEntry(2,1)*N.getEntry(2, 3));
		Ai.setSubMatrix(rotM.scalarMultiply(t2).getData(), 9, 3);

		// third column
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(0, 2)).getData(), 0, 6);
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(1, 2)).getData(), 3, 6);
		Ai.setSubMatrix(rotM.scalarMultiply(N.getEntry(2, 2)).getData(), 6, 6);
		
		double t3 = -(N.getEntry(0, 2)*N.getEntry(0, 3)+
				N.getEntry(1,2)*N.getEntry(1,3)+
				N.getEntry(2,2)*N.getEntry(2,3));
		Ai.setSubMatrix(rotM.scalarMultiply(t3).getData(), 9, 6);
		
		// fourth column
		Ai.setSubMatrix(Z.getData(), 0, 9);
		Ai.setSubMatrix(Z.getData(), 3, 9);
		Ai.setSubMatrix(Z.getData(), 6, 9);
		Ai.setSubMatrix(rotM.getData(), 9, 9);
		
		// fifth column
		Ai.setSubMatrix(Identity12.scalarMultiply(-1d).getData(), 0, 12);
		return Ai;
	}
	
	/**
	 * create B vector as an array of double values
	 * @param m RealMatrix contains matrix that we need to get the translational part of the matrix
	 * @return array of double values
	 */
	private double[] createBEntry(RealMatrix m) {
		double[] bi = {0,0,0,0,0,0,0,0,0,-m.getEntry(0, 3),-m.getEntry(1, 3),-m.getEntry(2, 3)};
		return bi;
	}
	
	/**
	 * Returns rotational part of a matrix
	 * @param mat matrix has to have at least 3 rows and 3 columns
	 * @return RealMatrix containing rotational part of mat
	 */
	private RealMatrix getRot(RealMatrix mat) {
		return mat.copy().getSubMatrix(
				new int[] {0,1,2}, new int[] {0,1,2});
	}
		
	/**
	 * prints the given matrix in console
	 * @param m the matrix to porint to console
	 */
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

	/**
	 * A more nice way to print the given matrix to the console
	 * @param rm the matrix to print
	 */
	public void printTable(RealMatrix rm) {
		int colDimNumber = rm.getColumnDimension();
		int rowDimNumber = rm.getRowDimension();
		ArrayList<String> headers = new ArrayList<String>();
		String[] alphabet = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
		for(int colDim = 0; colDim < colDimNumber; colDim++) {
			headers.add(alphabet[colDim]);
		}
		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		for(int rowDim = 0; rowDim < rowDimNumber; rowDim++) {
			double[] rowValues = rm.getRow(rowDim);
			ArrayList<String> rowArrayList = new ArrayList<String>();
			for(int colDim = 0; colDim < colDimNumber; colDim++) {
				rowArrayList.add(rowValues[colDim]+ "");
			}
			content.add(rowArrayList);
		}
		ConsoleTable ct = new ConsoleTable(headers,content);
		ct.printTable();
	}
	
	/**
	 * Berechnet die Transformation von Roboter zu Endeffektor (bzw. Marker) anhand 
	 * vordefinierter D-H-Parametergegebener und gegebener Winkel der Gelenke
	 * @param a1 winkel des Gelenks 1 in Grad
	 * @param a2 winkel des Gelenks 2 in Grad
	 * @param a3 winkel des Gelenks 3 in Grad
	 * @return
	 */
	public RealMatrix rob_H_M(double a1,double a2,double a3) {

		double theta1 = Math.toRadians(90);
		double theta2 = Math.toRadians(a1);
		double theta3 = Math.toRadians(90+a2);
		double theta4 = Math.toRadians(a3);

		double alpha1 = Math.toRadians(90);
		double alpha2 = Math.toRadians(90);
		double alpha3 = Math.toRadians(90);
		double alpha4 = Math.toRadians(0);
		
		double d1 = 0;
		double d2 = 100;
		double d3 = 0;
		double d4 = 150;
		
		double r1 = 0;
		double r2 = 0;
		double r3 = 0;
		double r4 = 0;
		
		double[][] rob_H_one = {
				{Math.cos(theta1), -Math.sin(theta1)*Math.cos(alpha1), Math.sin(theta1)*Math.sin(alpha1), r1*Math.cos(theta1)},
				{Math.sin(theta1), Math.cos(theta1)*Math.cos(alpha1), -Math.cos(theta1)*Math.sin(alpha1), r1*Math.sin(theta1)},
				{0d, Math.sin(alpha1), Math.cos(alpha1), d1},
				{0d, 0d, 0d, 1d}};
		
		double[][] one_H_two = {
				{Math.cos(theta2), -Math.sin(theta2)*Math.cos(alpha2), Math.sin(theta2)*Math.sin(alpha2), r2*Math.cos(theta2)},
				{Math.sin(theta2), Math.cos(theta2)*Math.cos(alpha2), -Math.cos(theta2)*Math.sin(alpha2), r2*Math.sin(theta2)},
				{0d, Math.sin(alpha2), Math.cos(alpha2), d2},
				{0d, 0d, 0d, 1d}};
		
		double[][] two_H_three = {
				{Math.cos(theta3), -Math.sin(theta3)*Math.cos(alpha3), Math.sin(theta3)*Math.sin(alpha3), r3*Math.cos(theta3)},
				{Math.sin(theta3), Math.cos(theta3)*Math.cos(alpha3), -Math.cos(theta3)*Math.sin(alpha3), r3*Math.sin(theta3)},
				{0d, Math.sin(alpha3), Math.cos(alpha3), d3},
				{0d, 0d, 0d, 1d}};
		
		double[][] three_H_marker = {
				{Math.cos(theta4), -Math.sin(theta4)*Math.cos(alpha4), Math.sin(theta4)*Math.sin(alpha4), r4*Math.cos(theta4)},
				{Math.sin(theta4), Math.cos(theta4)*Math.cos(alpha4), -Math.cos(theta4)*Math.sin(alpha4), r4*Math.sin(theta4)},
				{0d, Math.sin(alpha4), Math.cos(alpha4), d4},
				{0d, 0d, 0d, 1d}};
		
		RealMatrix ROB_H_ONE = new Array2DRowRealMatrix(rob_H_one);
		RealMatrix ONE_H_TWO = new Array2DRowRealMatrix(one_H_two);
		RealMatrix TWO_H_THREE = new Array2DRowRealMatrix(two_H_three);
		RealMatrix THREE_H_MARKER = new Array2DRowRealMatrix(three_H_marker);

		RealMatrix ROB_H_MARKER = ONE_H_TWO.multiply(TWO_H_THREE).multiply(THREE_H_MARKER);
		return ROB_H_MARKER;//ROB_H_ONE.multiply(ONE_H_TWO).multiply(TWO_H_THREE).multiply(THREE_H_MARKER);
	}
	
	
	
	
}
