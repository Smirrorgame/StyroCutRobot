package robCalibration;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import robprakt.graphics.Controller;

/**
 * Class contains methods for generating synthesized measuring-data in a robot-manipulator with end-effector, tracking-system with marker setup.
 * It can be used to check calibration algorithms like QR24 (paper: Non-orthogonal tool/flange and robot/world calibration).
 * 
 * By calling the function generateTestData(int numberOfMeasurements) it is possible to get a certain amount of measuring-data, inclusive the solution for
 * the calibration algorithm.
 * 
 * Checkout the paper "Non-orthogonal tool/flange and robot/world calibration" by Floris Ernst to understand the naming scheme used in my Code better.
 * https://www.rob.uni-luebeck.de/publikationen_downloads/ermm_12.pdf-b46951f770e1e17036ee6b18fe686a1b.pdf
 * 
 * @author DezzardHD
 *
 */
public class testDataGenerator {

	/**
	 * Position of the robot's coordinate system. x,y,z
	 */
	private final RealVector robotPosition = new ArrayRealVector(new double[] {0,0,0});
	
	/**
	 * Orientation of the tracking's coordinate system relative to robot's coordinate system.
	 */
	private final RealMatrix robotOrientation = new Array2DRowRealMatrix(new double[][] {{1,0,0},{0,1,0},{0,0,1}});
	
	/**
	 * Position of the tracking's coordinate System x,y,z
	 */
	private final RealVector trackingPosition = new ArrayRealVector(new double[] {10,20,30});
	
	/**
	 * Orientation of the tracking's coordinate system relative to robot's coordinate system.
	 *
	 */
	//TODO:has to be a right-hand-coordinate-system?!
	private final RealMatrix trackingOrientation = new Array2DRowRealMatrix(new double[][] {{-1,0,0},{0,0,-1},{0,-1,0}});
	
	/**
	 * End-effector's position relative to robot (variable)
	 */
	private RealVector endeffectorPosition;
	
	/**
	 * Marker's position relative to tracking-system (variable)
	 */
	private RealVector markerPosition;
	
	/**
	 * Marker's orientation relative to tracking-system. (variable)
	 */
	private RealMatrix markerOrientation;
	
	/**
	 * Position of the marker relative to the end-effector x,y,z
	 */
	private final RealVector markersPositionRelativeToEndeffector = new ArrayRealVector(new double[] {10,10,10});
	
	/**
	 * Orientation of end-effector and marker relative to robot-coordinate system. (Convention, that they have the same orientation. no necessity) (variable)
	 */
	private RealMatrix endeffectorAndMarkerOrientation;
	
	/**
	 * Contains the transformation-matrix used for converting from robot- to tracking-system-coordinate-system. (variable)
	 */
	private RealMatrix robotToTrackingTransformationMatrix;

	/**
	 * list of pose-matrices of robot (robot to endeffector)
	 */
	private ArrayList<RealMatrix> listM = new ArrayList<RealMatrix>();
	
	/**
	 * list of end-effector to marker matrices
	 */
	private ArrayList<RealMatrix> listX = new ArrayList<RealMatrix>();
	
	/**
	 * list of robot to tracking-system matrices
	 */
	private ArrayList<RealMatrix> listY = new ArrayList<RealMatrix>();
	
	/**
	 * list of tracking-system to marker matrices
	 */
	 private ArrayList<RealMatrix> listN = new ArrayList<RealMatrix>();
	
	/**
	 * Random object for generating random numbers
	 */
	private final Random random = new Random();
	
	/**
	 * Constructor of the testDataGenerator
	 */
	public testDataGenerator() {
		
	}
	
	
	/* ***********
	 * upper logic
	 * ***********/
	/**
	 * Generates a specified amount measuring-data quadruple and returns them as as a list of lists.
	 * 
	 * list index - list with transformation-matrix:
	 * 0 - M (robot to end-effector)
	 * 1 - X (end-effector to marker)
	 * 2 - Y (robot to tracking-system)
	 * 3 - N (tracking-system to marker)
	 * 
	 * The first matrix in each list is for the first measurement.
	 * The second matrix in each list is for the second measurement, and so on...
	 * 
	 * @param numberOfMeasurements defines how many measurements should be taken
	 * @return ArrayList of ArrayLists containing measuring-data
	 */
	public ArrayList<ArrayList<RealMatrix>> generateTestData(int numberOfMeasurements) {
		ArrayList<ArrayList<RealMatrix>> measurementList = new ArrayList<ArrayList<RealMatrix>>();
		
		for(int cnt = 0; cnt < numberOfMeasurements; cnt++) {
			addMeasurement();
		}
		
		createNoise();
		
		measurementList.add(listM);
		measurementList.add(listX);
		measurementList.add(listY);
		measurementList.add(listN);
		
		return measurementList;
	}
	
	/**
	 * Adding measuring-data for a random end-effector pose to the lists.
	 */
	private void addMeasurement() {
		RealMatrix Y = createYMatrix();
		RealMatrix X = createXMatrix();
		RealMatrix M = createMMatrix();
		RealMatrix N = createNMatrix(M,Y);

		listY.add(Y);
		listX.add(X);
		listM.add(M);
		listN.add(N);
	}
	
	/**
	 * Adding measuring noise to the N-matrix 
	 */
	private void createNoise() {
		for(RealMatrix matrix : listN) {
			matrix = rotationalNoise(matrix);
			matrix = translationalNoise(matrix);
		}
	}
	
	/**
	 * Adding noise to the rotational part of the matrix.
	 * @param matrix RealMatrix as homogeneous 4x4 matrix
	 * @return matrix with noise on rotational part
	 */
	private RealMatrix rotationalNoise(RealMatrix matrix) {
		//creating phases for noise for each possible rotation in the range of 1 degree
		double x = ((0.5-random.nextDouble())*2)*Math.PI/360;
		double y = ((0.5-random.nextDouble())*2)*Math.PI/360;
		double z = ((0.5-random.nextDouble())*2)*Math.PI/360;
		//extrinsic rotation
		RealMatrix rotationMatrix = new Array2DRowRealMatrix(new double[][] {
		{									   Math.cos(y)*Math.cos(z),										 -Math.cos(y)*Math.sin(z),				Math.sin(y)},
		{Math.cos(x)*Math.sin(z) + Math.cos(z)*Math.sin(x)*Math.sin(y), Math.cos(x)*Math.cos(z) - Math.sin(x)*Math.sin(y)*Math.sin(z), -Math.cos(y)*Math.sin(x)},
		{Math.sin(x)*Math.sin(z) - Math.cos(x)*Math.cos(z)*Math.sin(y), Math.cos(z)*Math.sin(x) + Math.cos(x)*Math.sin(y)*Math.sin(z),  Math.cos(x)*Math.cos(y)}});
		matrix.setSubMatrix(rotationMatrix.multiply(matrix.getSubMatrix(0, 2, 0, 2)).getData(), 0, 0);
		
		return matrix;
	}
	
	/**
	 * Adding noise to the translational part of the matrix.
	 * @param matrix RealMatrix as homogeneous 4x4 matrix
	 * @return matrix with noise on translational part
	 */
	private RealMatrix translationalNoise(RealMatrix matrix) {
		//adding a value between +-1 to the translational part of the matrix
		matrix.setEntry(0, 3, matrix.getEntry(0, 3) + 2*(0.5-random.nextDouble()));
		matrix.setEntry(1, 3, matrix.getEntry(1, 3) + 2*(0.5-random.nextDouble()));
		matrix.setEntry(2, 3, matrix.getEntry(2, 3) + 2*(0.5-random.nextDouble()));

		return matrix;
	}

	/* **************************************************************
	 * creating X-matrix (transformation from end-effector to marker)
	 * **************************************************************/
	/**
	 * creating X-matrix by combining rotational and translational part 
	 * @return RealMatrix X-matrix as homogeneous 4x4 matrix
	 */
	private RealMatrix createXMatrix() {
		//rotational part (coordinate system of end-effector and marker have the same orientation relative to robot coordinate system)
		//therefore no rotation needed
		RealMatrix rotMat = new Array2DRowRealMatrix(new double[][] {{1,0,0},{0,1,0},{0,0,1}});
		//position of marker relative to end-effector is a fixed value
		RealVector posVec = markersPositionRelativeToEndeffector;
		
		//creating homogeneous Matrix
		return this.createHomMatrix(rotMat, posVec);
	}
	
	
	/* ****************************************************************
	 * creating Y-matrix (transformation from robot to tracking-system)
	 * ****************************************************************/
	/**
	 * creating Y-matrix by combining rotational and translational part
	 * @return RealMatrix Y-matrix as homogeneous 4x4 matrix
	 */
	private RealMatrix createYMatrix() {
		// tracking-system's orientation is a matrix relative to robot coordinate system, and it's values are fixed
		RealMatrix rotMat = trackingOrientation;
		// tracking-system's position are relative to robot coordinate system, and they are fixed values
		RealVector posVec = trackingPosition;
		
		//creating homogeneous Matrix
		return this.createHomMatrix(rotMat, posVec);
	}
	
	
	/* *************************************************************
	 * creating M-matrix (transformation from robot to end-effector)
	 * *************************************************************/
	/**
	 * creating M-matrix by generating random rotational matrix and translational vector and then combining them
	 * @return RealMatrix M-matrix as homogeneous 4x4 matrix
	 */
	private RealMatrix createMMatrix() {
		//creating random transformation-matrix for transformation from robot to end-effector,
		//so it simulates different poses of the robot
		RealMatrix rotMat = genRandomRightHandCoordsSystem();
		this.endeffectorAndMarkerOrientation = rotMat;
		RealVector posVec = genRandomPositionEndEffector();
		this.endeffectorPosition = posVec;
		
		//creating homogeneous Matrix
		return this.createHomMatrix(rotMat, posVec);
	}
	
	/**
	 * generates random right-hand-coordinate-system for end-effector and marker
	 * RELATIVE TO ROBOT-COORDINATE-SYSTEM
	 * @return RealMatrix containing random rotation-matrix for end-effector and marker
	 */
	private RealMatrix genRandomRightHandCoordsSystem() {
		double x = random.nextDouble()*2*Math.PI;
		double y = random.nextDouble()*2*Math.PI;
		double z = random.nextDouble()*2*Math.PI;
		//extrinsic rotation for random orientation matrix (right)
		RealMatrix rotationMatrix = new Array2DRowRealMatrix(new double[][] {
		{									   Math.cos(y)*Math.cos(z),										 -Math.cos(y)*Math.sin(z),				Math.sin(y)},
		{Math.cos(x)*Math.sin(z) + Math.cos(z)*Math.sin(x)*Math.sin(y), Math.cos(x)*Math.cos(z) - Math.sin(x)*Math.sin(y)*Math.sin(z), -Math.cos(y)*Math.sin(x)},
		{Math.sin(x)*Math.sin(z) - Math.cos(x)*Math.cos(z)*Math.sin(y), Math.cos(z)*Math.sin(x) + Math.cos(x)*Math.sin(y)*Math.sin(z),  Math.cos(x)*Math.cos(y)}});
		return rotationMatrix;
	}
	
	/**
	 * generates random position of the end-effector in a certain area
	 * RELATIVE TO ROBOT-COORDINATE-SYSTEM
	 * @return RealVector contains position of end-effector
	 */
	private RealVector genRandomPositionEndEffector() {
		//TODO: modify if position does not suit your mood
		return new ArrayRealVector(new double[] {	random.nextDouble()*trackingPosition.getEntry(0),
													random.nextDouble()*trackingPosition.getEntry(1),
													random.nextDouble()*trackingPosition.getEntry(2)});
	}
	
	
	/* *****************************************************************
	 * creating N-matrix (transformation from tracking-system to marker)
	 * *****************************************************************/
	/**
	 * creates homogeneous 4x4 N-matrix by combining rotational and translational part
	 * @param m the M-matrix (used for transformation from robot to end-effector)
	 * @param y the Y-matrix (used for transformation from robot to tracking-system)
	 */
	private RealMatrix createNMatrix(RealMatrix m, RealMatrix y) {
		RealMatrix rotMat = genRotMatOfN(y);
		RealVector posVec = genPosVecOfN(m,y);
		
		return this.createHomMatrix(rotMat, posVec);
	}
	
	/**
	 * creating rotational matrix of N-matrix
	 * First getting the rotational matrix by the end-effector (same as the marker for this setup), then extracting columns,
	 * then moving the extracted vector to the origin of the tracking-system's coordinate system, and then converting the resulting
	 * vector from robot- to tracking-coordinate-system
	 * Last the three vectors being combined to the wanted rotational matrix
	 * @param RealMatrix Y-matrix as homogeneous 4x4 matrix
	 * @return RealMatrix containing orientation of the marker's coordinate-system relative to tracking-system's coordinate system
	 */
	private RealMatrix genRotMatOfN(RealMatrix y) {
		RealMatrix rotMatRelTS = new Array2DRowRealMatrix(3,3);
		//extracting vectors from endeffectorAndMarkerOrientation to transform them separately into the tracking-system's coordinate-system
		//TODO: maybe it is possible to just multiply rotational part of robot to tracking-system transformation-matrix M * rotational matrix of endeffectorAndMarkerOrientation
		//add position of tracking-system relative to robot to vector, so the vector gets moved to the origin of the tracking-system's coordinate system
		RealVector movedXvectorOfRotationalMatrix = new ArrayRealVector(this.endeffectorAndMarkerOrientation.getColumn(0)).add(this.trackingPosition);
		rotMatRelTS.setColumn(0,transformCoordsFromRobotToTracking(y, this.createHomVector(movedXvectorOfRotationalMatrix)).getSubVector(0, 3).toArray());
		RealVector movedYvectorOfRotationalMatrix = new ArrayRealVector(this.endeffectorAndMarkerOrientation.getColumn(1)).add(this.trackingPosition);
		rotMatRelTS.setColumn(1,transformCoordsFromRobotToTracking(y, this.createHomVector(movedYvectorOfRotationalMatrix)).getSubVector(0, 3).toArray());
		RealVector movedZvectorOfRotationalMatrix = new ArrayRealVector(this.endeffectorAndMarkerOrientation.getColumn(2)).add(this.trackingPosition);
		rotMatRelTS.setColumn(2,transformCoordsFromRobotToTracking(y, this.createHomVector(movedZvectorOfRotationalMatrix)).getSubVector(0, 3).toArray());
		
		return rotMatRelTS;
	}
	
	/**
	 * generating position of the marker relative to the tracking-system's coordinate system
	 * @param m the M-matrix (used for transformation from robot to end-effector)
	 * @param y the Y-matrix (used for transformation from robot to tracking-system)
	 * @return RealVector containing position of the marker relative to the tracking-system's coordinate system
	 */
	private RealVector genPosVecOfN(RealMatrix m, RealMatrix y) {
		RealVector posRelativeToRobot = transformPosOfMarkerFromEndEffectorToRobot(m);
		RealVector posRelativeToTrackingSystem = transformCoordsFromRobotToTracking(y, posRelativeToRobot);
		return posRelativeToTrackingSystem;
	}
	
	/**
	 * Transformation of the marker's relative to end-effector to the marker's position relative to
	 * the robot's coordinate system
	 * @param m RealMatrix M-matrix as homogeneous 4x4 matrix
	 * @return RealVector containing the above mentioned position data
	 */
	private RealVector transformPosOfMarkerFromEndEffectorToRobot(RealMatrix m) {
		return m.operate(this.createHomVector(this.markersPositionRelativeToEndeffector));
	}
	
	/**
	 * Transformation of coordinates from robot to tracking-system.
	 * @param y RealMatrix Y-matrix as homogeneous 4x4 matrix
	 * @param posRelativeToRobot coordinates relative to robot's coordinate system
	 * @return RealVector containing coordinates relative to tracking's coordinate system
	 */
	private RealVector transformCoordsFromRobotToTracking(RealMatrix y, RealVector posRelativeToRobot) {
		return MatrixUtils.inverse(y).operate(posRelativeToRobot);
	}
	
	
	/* ************
	 * tool methods
	 * ************/
	/**
	 * create homogeneous matrix out of RealMatrix rotation-matrix and RealVector translational-vector
	 * @param rotMat 3x3 rotation-matrix
	 * @param posVec 3x1 translational-matrix
	 * @return RealMatrix containing homogeneous 4x4 matrix
	 */
	private RealMatrix createHomMatrix(RealMatrix rotMat, RealVector posVec) {
		RealMatrix homMat = new Array2DRowRealMatrix(4,4);
		homMat.setSubMatrix(rotMat.getData(), 0, 0);
		homMat.setEntry(0, 3, posVec.getEntry(0));
		homMat.setEntry(1, 3, posVec.getEntry(1));
		homMat.setEntry(2, 3, posVec.getEntry(2));
		homMat.setRow(3, new double[] {0,0,0,1});
		return homMat;
	}
	
	/**
	 * create homogeneous vector by adding a 1 in row 4 in an 3x1 vector
	 * @param v 3x1 vector
	 * @return homVec homogeneous 4x1 vector
	 */
	private RealVector createHomVector(RealVector v) {
		RealVector homVec = new ArrayRealVector(new double[] {v.getEntry(0),v.getEntry(1),v.getEntry(2),1});
		return homVec;
	}
}