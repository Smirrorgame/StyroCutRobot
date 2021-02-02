package robprakt.cutting;

import org.apache.commons.math3.linear.RealMatrix;

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
	 * neutralPosition is a homogeneous matrix containing pose for a neutral position of cutter-robot
	 * relative to cutter-robot
	 */
	private RealMatrix neutralPosition;
	
	/**
	 * auxiliaryPosition is a homogeneous matrix containing pose for an auxiliary position of cutter-robot
	 * relative to cutter-robot
	 */
	private RealMatrix auxiliaryPosition;
	
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
	}
	
	/**
	 *  Setter for the neutralPosition.
	 *  Expects pose-matrix relative to workspace and sets pose-matrix relative
	 *  to cutter-robot coordinate-system.
	 * @param homPoseMatrix4x4 contains homogeneous pose matrix of the neutral Position
	 * 						   relative to workspace-coordinate-system
	 */
	protected void setNeutralPosition(RealMatrix homPoseMatrix4x4) {
		//TODO: for Cutter-Tool
	}
	
	protected void setQuantizationStep(double stepValue) {
		if(1d < stepValue && stepValue > 100d) throw new IllegalArgumentException("quantizationStep must have a value between 1 and 100 mm");
		this.quantizationStep = stepValue;
	}
	
	protected boolean moveToNeutralPosition() {
		//TODO
		return true;
	}
	
	protected boolean moveToAuxiliaryPosition() {
		//TODO
		return true;
	}
	
	protected boolean moveStraightP2P(RealMatrix startpose, RealMatrix endpose) {
		//TODO
		return true;
	}
	
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
