package robprakt.cutting;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import robprakt.network.TCPClient;

/**
 * Cutting logic implements the cutting process.
 * 
 * @author DezzardHD
 */
public class CuttingLogic {
	
	/**
	 * transformCoords used for transforming between different coordinate systems
	 */
	private TransformCoords transformCoords;
	
	/**
	 * robotMovement used for calculating trajectories and letting the robot move
	 */
	private RobotMovement robotMovement;
	
	/**
	 * isCuttingActive if the cutting process currently is running (true) or not (false)
	 */
	private static boolean isCuttingActive = false;
	//TODO: muss beim Start auf true und beim Ende auf false gesetzt werden
	
	
	//===========================
	//==========METHODS==========
	//===========================
	
	/**
	 * Constructor of CuttingLogic creates TransformCoords and RobotMovement instances.
	 * @param clientR1 client for connecting to cutter-robot
	 * @param clientR2 client for connecting to holder-robot
	 */
	public CuttingLogic(TCPClient clientR1, TCPClient clientR2) {
		this.transformCoords = new TransformCoords(clientR1, clientR2);
		this.robotMovement = new RobotMovement(transformCoords);
	}
	
	/**
	 * Starts the cutting process.
	 * @throws Exception if the holder robot couldn't move into default pose.
	 */
	public void cut() throws Exception {
		//set cutting status to active
		this.isCuttingActive = true;
		
		//move holder-robots end-effector into default pose 
		moveHolderRobotToDefaultPose();
		
		//TODO: Hier kommt die obere Logik des Cutting Prozesses rein.
		
		//set cutting status to not active
		this.isCuttingActive = false;
	}
	
	/**
	 * Returns true if cutting process has been started and still runs and false if the
	 * cutting process hasn't been started or has been finished.
	 * @return isCuttingActive
	 */
	public static boolean isCuttingActive() {
		return isCuttingActive;
	}
	
	/**
	 * Moves holder-robot, so its end-effector points upwards and the the end-effector's position equals
	 * the workspace midpoint defined in TransformCoords.
	 * @throws Exception if the holder robot couldn't move into default pose.
	 */
	private void moveHolderRobotToDefaultPose() throws Exception {
		if(!robotMovement.moveMinChange(transformCoords.getDefaultPoseHoldersEndeffector(), transformCoords.getClientR2()))
			throw new Exception("[CuttingLogic] Robot couldn't move into default pose.");
	}
	
}
