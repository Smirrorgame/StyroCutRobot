package robprakt.cutting;

import java.util.ArrayList;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import robprakt.network.TCPClient;

/**
 * RobotMovement contains methods for calculating basic trajectories
 * like a straight and sends commands to robot to move the robot to the specified position.
 * 
 * @author DezzardHD
 */
public class RobotMovement {
	
	private TransformCoords transformCoords;
	
	/**
	 * neutralPosition is a homogeneous vector containing neutral position of cutter-robot
	 * relative to cutter-robot
	 */
	private RealVector neutralPosition;
	
	/**
	 * auxiliaryPosition is a homogeneous vector containing an auxiliary position of cutter-robot
	 * relative to cutter-robot
	 */
	private RealVector auxiliaryPosition;
	
	/**
	 * quantizationStep sets the point to point width for calculating points on a trajectory
	 * smaller values might lead to longer processing times
	 * larger values might lead to bigger deviations from the trajectory
	 */
	private double quantizationStep;
	
	
	//===========================
	//==========METHODS==========
	//===========================
	
	//===========SETUP===========
	
	/**
	 * Constructor of RobotMovement.
	 * @param transformCoords TransformCoords used for communicating with robots
	 */
	public RobotMovement(TransformCoords transformCoords) {
		this.transformCoords = transformCoords;
		setQuantizationStep(10d);
	}
	
	/**
	 *  Setter for the neutralPosition of the cutter-robots end-effector.
	 *  Expects homogeneous position-vector relative to workspace and sets position vector relative
	 *  to cutter-robot coordinate-system.
	 * @param homPosVector4x1 contains homogeneous position vector with the neutral Position
	 * 						   relative to workspace-coordinate-system
	 */
	protected void setNeutralPosition(RealVector homPosVector4x1) {
		this.neutralPosition = transformCoords.getTrajectoryMatrixForCuttersEndeffector(homPosVector4x1).getColumnVector(3);
	}
	
	/**
	 *  Setter for the auxiliaryPosition of the cutter-robots end-effector.
	 *  Expects homogeneous position-vector relative to workspace and sets position vector relative
	 *  to cutter-robot coordinate-system.
	 * @param homPosVector4x1 contains homogeneous position vector with the auxiliary position
	 * 						   relative to workspace-coordinate-system
	 */
	protected void setAuxiliaryPosition(RealVector homPosVector4x1) {
		this.auxiliaryPosition = transformCoords.getTrajectoryMatrixForCuttersEndeffector(homPosVector4x1).getColumnVector(3);
	}
	
	
	//=======MOVEMENT-LOGIC======
	
	protected boolean moveStraightP2P(RealMatrix startpose, RealMatrix endpose) {
		//TODO
		return true;
	}
	
	protected boolean moveToNeutralPosition() {
		//TODO
		return true;
	}
	
	protected boolean moveToAuxiliaryPosition() {
		//TODO
		return true;
	}
	
	
	//=========TRAJECTORY========
	
	/**
	 * Calculates the points on a trajectory that are being passed on to the robot to move to.
	 * @param 	homStartPos homogeneous RealVector containing start position
	 * @param 	homEndPos homogeneous RealVector containing end position
	 * @return 	ArrayList with RealVectors containing the points on the trajectory. The
	 * 			start position is contained in the first element, and the end position in
	 * 			the last element of the list.
	 */
	private ArrayList<RealVector> calcP2PTrajectoryForStraight(RealVector homStartPos, RealVector homEndPos){
		//generating vector
		RealVector directionVector = homEndPos.getSubVector(0, 3).subtract(homStartPos.getSubVector(0, 3));
		//norm vector according to the quantizationStep value
		directionVector.mapDivideToSelf(directionVector.getNorm());
		directionVector.mapMultiplyToSelf(this.quantizationStep);
		//TODO: FINISH METHOD
	}
	
	/**
	 * Sets the discretization value for the trajectory-calculations.
	 * Method is called in constructor of RobotMovement.
	 * @param stepValue
	 */
	//TODO: Maybe add an option the the cutting menu to manually set this value.
	private void setQuantizationStep(double stepValue) {
		if(1d < stepValue && stepValue > 100d) throw new IllegalArgumentException("quantizationStep must have a value between 1 and 100 mm");
		this.quantizationStep = stepValue;
	}
	
	
	//=======COMMUNICATION=======
	
	/**
	 * Sets the pose of an robot specified in a 3x4 or 4x4 homogeneous matrix.
	 * @param 	poseMatrix 3x4 or 4x4 homogeneous matrix containing pose data 
	 * 			that is relative to the robot that should be moved
	 * @param 	client the client used for connecting to robot
	 * @return 	true if position and configuration are valid
	 */
	protected boolean moveMinChange(RealMatrix poseMatrix, TCPClient client) {
		String command = "MoveMinChangeRowWiseStatus" 	+ " " + poseMatrix.getEntry(0,0) + " " + poseMatrix.getEntry(0,1) + " " + poseMatrix.getEntry(0,2) + " " + poseMatrix.getEntry(0,3)
														+ " " + poseMatrix.getEntry(1,0) + " " + poseMatrix.getEntry(1,1) + " " + poseMatrix.getEntry(1,2) + " " + poseMatrix.getEntry(1,3)
														+ " " + poseMatrix.getEntry(2,0) + " " + poseMatrix.getEntry(2,1) + " " + poseMatrix.getEntry(2,2) + " " + poseMatrix.getEntry(2,3)
														+ " " + "noflip lefty"; //TODO: ggf. sollte man die Parameter noflip lefty etc. noch sinniger bestimmen.
		transformCoords.send(command, client);
		// if the robot accepts the command, it returns true
		return "true".equals((transformCoords.response(client)));
	}
}
