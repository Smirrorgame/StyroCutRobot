package robprakt.cutting;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import robCalibration.ConsoleTable;
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
	 * relative to cutter-robot (tool-offset already considered)
	 */
	private RealVector neutralPosition;
	
	/**
	 * auxiliaryPosition is a homogeneous vector containing an auxiliary position of cutter-robot
	 * relative to cutter-robot (tool-offset already considered)
	 */
	private RealVector auxiliaryPosition;
	
	/**
	 * quantizationStep sets the point to point width for calculating points on a trajectory
	 * smaller values might lead to longer processing times
	 * larger values might lead to bigger deviations from the trajectory
	 */
	private double quantizationStep;
	
	//TODO: Nur für die manuell ausführbare Simulation benötigt.
	public static ArrayList<String> commandsDuringCutting = new ArrayList<String>();
	public static ArrayList<TCPClient> clientForEachCommand = new ArrayList<TCPClient>();
	
	
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
		setQuantizationStep(20d);
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
	
	/**
	 * Moves cutter-robots end-effector from a start-position to an end-position.
	 * @param startPosition (homogeneous) relative to workspace
	 * @param endPosition (homogeneous) relative to workspace
	 */
	protected void moveCutterP2P(RealVector startPosition, RealVector endPosition) {
		RealVector startPos = transformCoords.getTrajectoryMatrixForCuttersEndeffector(startPosition).getColumnVector(3);
		RealVector endPos = transformCoords.getTrajectoryMatrixForCuttersEndeffector(endPosition).getColumnVector(3);
		this.movementHandler(	this.calcP2PTrajectoryForStraight(startPos, endPos)
								,this.transformCoords.getClientR1());
	}
	
	/**
	 * Moves cutter-robots end-effector from the current position to the neutral position.
	 */
	protected void moveToNeutralPosition() {
		//get current position (homogeneous) of cutter-robots end-effector
		RealVector currentPosition = this.transformCoords.measureCutterRobotPose().getColumnVector(3);
		this.movementHandler(	this.calcP2PTrajectoryForStraight(currentPosition, this.neutralPosition)
								,this.transformCoords.getClientR1());
	}
	
	/**
	 * Moves cutter-robots end-effector from the current position to the auxiliary position.
	 */
	protected void moveToAuxiliaryPosition() {
		//get current position (homogeneous) of cutter-robots end-effector
		RealVector currentPosition = this.transformCoords.measureCutterRobotPose().getColumnVector(3);
		this.movementHandler(	this.calcP2PTrajectoryForStraight(currentPosition, this.auxiliaryPosition)
								,this.transformCoords.getClientR1());
	}
	
	/**
	 * Handles gradually moving the robot from point to point.
	 * @param positions to move to, which define a trajectory
	 * @param client for identifying which robot to communicate with
	 */
	private void movementHandler(ArrayList<RealVector> positions,TCPClient client) {
		RealMatrix poseMatrix3x4 = new Array2DRowRealMatrix(3,4);
		//setting the orientation of the end-effector depending on robot-type
		RealMatrix rotMat = (client.isEqual(this.transformCoords.getClientR1()))
				? TransformCoords.standardCutterToolOrientation
				: TransformCoords.rotPartOfHolderRobotsEndeffectorDefaultPoseRelCutterRobot;
		poseMatrix3x4.setSubMatrix(rotMat.getData(), 0, 0);
		//moving to each point with a certain precision
		for(RealVector pos : positions) {
			poseMatrix3x4.setColumnVector(3, pos);
			this.moveMinChange(poseMatrix3x4, client);
			this.comparePoses(poseMatrix3x4, client);
		}
	}
	
	/**
	 * Compares current pose matrix of a robot to the goal pose matrix.
	 * Program stays in this algorithm as long as the deviation to the goal pose
	 * is smaller than a defined threshold.
	 * @param poseMatrix3x4 goal-pose-matrix 3x4
	 * @param client used for identifying which robot to communicate with
	 */
	private void comparePoses(RealMatrix poseMatrix3x4, TCPClient client) {
		//defining deviation for the stop criterion
		double deviation = 0.1; //TODO: Abweichung ggf. vergrößern oder verringern
		double actualDeviation = this.quantizationStep; //setting 
		while(actualDeviation > deviation) {
			RealMatrix currentPose = client.isEqual(this.transformCoords.getClientR1()) ?
			this.transformCoords.measureCutterRobotPose().getSubMatrix(0, 2, 0, 3)
			: this.transformCoords.measureHolderRobotPose().getSubMatrix(0, 2, 0, 3);
			RealMatrix deviationPoseMatrix = poseMatrix3x4.subtract(currentPose);
			//get maximum deviation
			actualDeviation = this.maxDeviation(deviationPoseMatrix);
		}
	}
	
	/**
	 * Returns greatest double value (according to amount) of a 3x4 matrix.
	 * @param deviationPoseMatrix3x4
	 * @return maxDeviation maximum deviation found in matrix
	 */
	private double maxDeviation(RealMatrix deviationPoseMatrix3x4) {
		//TODO: bestenfalls sollte hier eine TimeOutException hinzugefügt werden.
		double maxDeviation = 0;
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 4; col++) {
				//get according to amount the greatest value of deviationPoseMatrix
				double nextValue = Math.abs(deviationPoseMatrix3x4.getEntry(row, col));
				maxDeviation = Math.max(maxDeviation, nextValue);
			}
		}
		return maxDeviation;
	}
	
	
	//=========TRAJECTORY========
	
	/**
	 * Calculates the points on a trajectory that are being passed on to the robot to move to.
	 * @param 	homStartPos homogeneous RealVector containing start position relative to cutter-robot
	 * @param 	homEndPos homogeneous RealVector containing end position relative to cutter-robot
	 * @return 	ArrayList with RealVectors containing the points (NOT homogeneous) relative to cutter-robot
	 * 			on the trajectory. The start position is the first element,
	 * 			and the end position is the last element of the list.
	 */
	private ArrayList<RealVector> calcP2PTrajectoryForStraight(RealVector homStartPos, RealVector homEndPos){
		//generating vector
		RealVector directionVector = homEndPos.getSubVector(0, 3).subtract(homStartPos.getSubVector(0, 3));
		//saving length/norm of vector, so later it can be used as stop criterion
		double distanceStartToEnd = directionVector.getSubVector(0, 3).getNorm();
		//norm vector according to the quantizationStep value
		directionVector.mapDivideToSelf(directionVector.getNorm());
		directionVector.mapMultiplyToSelf(this.quantizationStep);
		
		//calculate trajectory-points
		ArrayList<RealVector> trajectoryPoints = new ArrayList<RealVector>();
		//setting start position as first element of list
		trajectoryPoints.add(homStartPos.getSubVector(0, 3));
		RealVector currentPoint = homStartPos.getSubVector(0, 3);
		double currentLength = 0;
		while(currentLength < distanceStartToEnd) {
			trajectoryPoints.add(currentPoint);
			//calculate positions based on the last position in the list
			currentPoint = trajectoryPoints.get(trajectoryPoints.size() - 1).add(directionVector);
			//update length status (distance between start and current position)
			currentLength = currentLength + this.quantizationStep;
		}
		//adding end position as last element of list
		trajectoryPoints.add(homEndPos.getSubVector(0, 3));
		
		return trajectoryPoints;
	}
	
	/**
	 * Sets the discretization value for the trajectory-calculations.
	 * Method is called in constructor of RobotMovement.
	 * @param stepValue
	 */
	//TODO: Maybe add an option the the cutting menu to manually set this value.
	private void setQuantizationStep(double stepValue) {
		if(!(1d < stepValue && stepValue < 100d)) throw new IllegalArgumentException("quantizationStep must have a value between 1 and 100 mm");
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
		
		//saving commands when cutting process is active
		commandsDuringCutting.add(command);
		clientForEachCommand.add(client);
		
		// if the robot accepts the command, it returns true
		String response = (transformCoords.response(client)).trim(); //TODO: in simulation it is necessary to trim response, cause there are trailing whitespaces
		return "true".equals(response);
	}
}
