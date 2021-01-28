package cutting;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import robprakt.Constants;

/**
 * TransformCoords contains methods describing coordinates in different coordinate systems.
 * It holds the transformation matrices needed to describe the relative position between robots and more.
 * @author DezzardHD
 */

public class TransformCoords {

	/**
	 * RealMatrix transforming coordinates from tracking-system to cutter-robot, so given coordinates
	 * are relative to cutter-robot's coordinate system.
	 * Gets set after calibration.
	 */
	private static RealMatrix cutterRobotToTrackingSystem;
	
	/**
	 * RealMatrix transforming coordinates from tracking-system to holder-robot, so given coordinates
	 * are relative to holder-robot's coordinate system.
	 * Gets set after calibration.
	 */
	private static RealMatrix holderRobotToTrackingSystem;
	
	/**
	 * RealMatrix transforming coordinates from cutter to end-effector of cutter-robot, so given coordinates
	 * are relative to end-effector's coordinate system.
	 */
	//TODO: measure distance between end-effector coordinate system and 
	private final static RealMatrix endeffectorToCutter = new Array2DRowRealMatrix(new double[][]{{1,0,0,50},{0,1,0,-10},{0,0,1,20},{0,0,0,1}});
	
	/**
	 * RealMatrix transforming coordinates from workspace to end-effector of holder-robot, so given coordinates
	 * are relative to end-effector's coordinate system.
	 */
	//TODO: set orientation so it's the same as of system R1 and setWorkspaceMinpoint (position) defined in menu --> end-effector has to be moved for this
	//TODO: Ermittele die genaue Position des Endeffektorkoordinatensystems vom Adept Viper S850 --> ggf. muss endeffectorToWorkspace angepasst werden
	private RealMatrix endeffectorToWorkspace = new Array2DRowRealMatrix(new double[][] {{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}});
	
	
	
	
	/**
	 * Setter for the transformation matrix (cutter-robot to tracking-system).
	 * @param matrix4x4
	 */
	public static void setCutterRobotToTrackinSystem(RealMatrix matrix4x4) {
		cutterRobotToTrackingSystem = matrix4x4;
	}
	
	/**
	 * Setter for the transformation matrix (holder-robot to tracking-system).
	 * @param matrix4x4
	 */
	public static void setHolderRobotToTrackinSystem(RealMatrix matrix4x4) {
		holderRobotToTrackingSystem = matrix4x4;
	}
}
