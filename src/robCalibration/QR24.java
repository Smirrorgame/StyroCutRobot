package robCalibration;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

<<<<<<< HEAD
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
=======
>>>>>>> 1045c59188c23aa5d029e1c94a351f2dcf68c60a
import org.ejml.data.DMatrix4x4;
import org.ejml.data.DMatrixRBlock;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.fixed.CommonOps_DDF3;
import org.ejml.dense.fixed.CommonOps_DDF4;

import robprakt.Constants;


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
	
	//TODO: Setting number of measurements in GUI (has to be greater than 1). if so: change private to protected
	/**
	 * Number of measurements.
	 * Has to be greater than 1.
	 */
	private int numberOfMeasurements = 50;
	
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
			//TODO: Validate if this is the correct oder of multiplication
			robPoseMatrix = robPoseMatrix.multiply(basicOrientationOfMarker);
			
			//TRANSLATIONAL PART
			//generating random values inside a sphere defined by radiusWorkspace
			robPoseMatrix.setEntry(1, 4, ((0.5-random.nextDouble())*2)*radiusWorkspace);
			robPoseMatrix.setEntry(2, 4, ((0.5-random.nextDouble())*2)*radiusWorkspace);
			robPoseMatrix.setEntry(3, 4, ((0.5-random.nextDouble())*2)*radiusWorkspace);
			
			//add generated pose matrix to list
			poseMatrices.add(robPoseMatrix);
		}
	}
	
	

	private RealMatrix measuring() {
		RealMatrix robPoseMatrix;

		for(int cnt = 0; cnt < numberOfMeasurements; cnt++) {
			robPoseMatrix = poseMatrices.get(cnt);
			//TODO: replace sendToRobot with correct method to send commands to the robot
			sendToRobot("MoveMinChangeRowWiseStatus" 	+ " " + robPoseMatrix.getEntry(1,1) + " " + robPoseMatrix.getEntry(1, 2) + " " + robPoseMatrix.getEntry(1,3) + " " + robPoseMatrix.getEntry(1, 4)
														+ " " + robPoseMatrix.getEntry(2,1) + " " + robPoseMatrix.getEntry(2, 2) + " " + robPoseMatrix.getEntry(2,3) + " " + robPoseMatrix.getEntry(2, 4)
														+ " " + robPoseMatrix.getEntry(3,1) + " " + robPoseMatrix.getEntry(3, 2) + " " + robPoseMatrix.getEntry(3,3) + " " + robPoseMatrix.getEntry(3, 4)

														+ " " + " righty");
			//TODO: replace getRobSpeed with correct method to get the speed value of the robot (assuming value is given in percentage)
			TimeUnit.MILLISECONDS.sleep(2*radiusWorkspace/(Constants.MAX_COMPOSITE_SPEED*getRobSpeed));
			
			//TODO: create initial setup with robot, so it only sends MATRIXROWWISE data
			//TODO: extract values from the tracking system response and add them to the list
			sendToTrackingSystem("CM_NEXTVALUE")
		}
	}

	
}
