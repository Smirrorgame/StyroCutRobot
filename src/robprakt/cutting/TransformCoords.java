package robprakt.cutting;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import robCalibration.QR24;
import robprakt.graphics.Controller;
import robprakt.network.TCPClient;

/**
 * TransformCoords contains methods for describing coordinates in different coordinate systems.
 * It holds:
 * -> the transformation matrices needed to describe the relative position between robots,.
 * -> default positions for workspace and end-effectors
 * -> default orientation for workspace, cutting-tool and end-effectors
 * -> and more...
 * 
 * 
 * Dependencies:
 * -> Transformation matrices have to be set from external (after calibration):
 * 		-> cutterRobotToTrackingSystem
 * 		-> holderRobotToTrackingSystem
 * -> TCPClients are needed for communicating with robot servers
 * 		-> clientR1
 * 		-> clientR2
 * -> The midpoint of the workspace has to be from external
 * 		-> initialWorkspacePositionRelCutterRobot
 * 
 * @author DezzardHD
 */

public class TransformCoords {

	/**
	 * RealMatrix transforming coordinates from tracking-system to cutter-robot, so given coordinates
	 * are relative to cutter-robot's coordinate system.
	 * Is being set after calibration.
	 */
	public static RealMatrix cutterRobotToTrackingSystem;
	
	/**
	 * RealMatrix transforming coordinates from tracking-system to holder-robot, so given coordinates
	 * are relative to holder-robot's coordinate system.
	 * Is being set after calibration.
	 */
	public static RealMatrix holderRobotToTrackingSystem;
	
	/**
	 * RealVector 3D contains the initial workspace cutter position relative to cutter-robot, which is also
	 * the origin of the workspace.
	 * Is being set before calibration.
	 */
	public static RealVector initialWorkspacePositionRelCutterRobot;
	
	/**
	 * RealMatrix transforming coordinates from holder-robot to cutter-robot, so given coordinates 
	 * are relative to holder-robot's coordinate system.
	 */
	private RealMatrix cutterRobotToHolderRobot;
	
	/**
	 * Orientation of the cutter-tool (the cutter) relative to cutter-robot's coordinate system.
	 */
	//TODO: Schneideklinge sollte parallel zur x-Achse des Schneideroboters sein.
	private final RealMatrix standardCutterToolOrientation =
		new Array2DRowRealMatrix(new double[][] {
			{0d,0d,1d},
			{0d,1d,0d},
			{-1d,0d,0d}});
	
	/**
	 * RealMatrix transforming coordinates from cutter to end-effector of cutter-robot, so given coordinates
	 * are relative to end-effector's coordinate system.
	 */
	//TODO: measure distance between end-effector coordinate system and 
	private final RealMatrix endeffectorToCutter = new Array2DRowRealMatrix(new double[][]{	{1,0,0,50},
																							{0,1,0,-10},
																							{0,0,1,20},
																							{0,0,0,1}});
	
	/**
	 * Rotational part of holder-robot's end-effector default position (initial position, so that the
	 * object is positioned in the workspace) relative to cutter-robot.
	 * Used to calculate the transformation between holder-robot's end-effector
	 * and the workspace-coordinate-system.
	 */
	//TODO: Die Orientierung des Endeffektors ist hardcoded. Ggf. ist sie fehlerhaft.
	//TODO: Die Position und Orientierung des Endeffektorkoordinatensystems ist bzgl. des letzten
	//TODO: Roboterarmgliedes nicht bekannt. Es wurde angenommen, der Ursprung des Systems auf der Flansch-
	//TODO: oberfläche liegt und die z-Achse deckungsgleich mit der z-Achse des letzten Gelenks ist.
	private static final RealMatrix rotPartOfHolderRobotsEndeffectorDefaultPoseRelCutterRobot =
		new Array2DRowRealMatrix(new double[][] {
													{1d,0d,0d},
													{0d,1d,0d},
													{0d,0d,1d}});
	
	/**
	 * Rotational part of workspace's default position (initial position) relative to cutter-robot.
	 * Used to calculate the transformation between holder-robot's end-effector
	 * and the workspace-coordinate-system.
	 */
	//TODO: Die Orientierung des Workspaces ist hardcoded. Ggf. ist sie fehlerhaft.
	private static final RealMatrix rotPartOfWorkspaceDefaultPoseRelCutterRobot =
		new Array2DRowRealMatrix(new double[][] {
													{1d,0d,0d},
													{0d,1d,0d},
													{0d,0d,1d}});
	
	/**
	 * The default pose of the holder-robot's end-effector relative to holder-robot.
	 * Gets initially calculated in constructor.
	 */
	private RealMatrix defaultPoseHoldersEndeffector;
	
	/**
	 * The default pose of the the workspace relative to holder-robot.
	 * Gets initially calculated in constructor.
	 */
	private RealMatrix defaultPoseWorkspace;
	
	/**
	 * RealMatrix transforming coordinates from workspace to end-effector of holder-robot, so given coordinates
	 * are relative to end-effector's coordinate system.
	 */
	//TODO: Möglicherweise nicht notwendig, weil der Workspace absolut im Raum ist und sich nicht mit dem Endeffektor
	//TODO: mitdrehen soll. --> ggf. Berechnung auch aus Konstruktor entfernen (erstmal aber drinne lassen.)
	private RealMatrix endeffectorToWorkspace;

	/**
	 * Client for connection to cutter-robot server.
	 */
	private TCPClient clientR1;
	
	/**
	 * Client for connection to holder-robot server.
	 */
	private TCPClient clientR2;
	

	//===========================
	//==========METHODS==========
	//===========================
	
	//===========SETUP===========
	
	/**
	 * Constructor sets clients for connecting to the robot-server.
	 * Initially the transformation matrix between the robots get calculated.
	 * Initially the transformation matrix between the holder-robots end-effector and the workspace is calculated.
	 * @param clientR1 client for connecting to cutter-robot
	 * @param clientR2 client for connecting to holder-robot
	 */
	public TransformCoords(TCPClient clientR1, TCPClient clientR2) {
		if(clientR1 == null || clientR2 == null) throw new NullPointerException(
				"[TransformCoords] At least one TCP-Client is not initialized.");
		if(TransformCoords.initialWorkspacePositionRelCutterRobot == null) throw new NullPointerException(
				"[TransformCoords] Workspace position is not defined.");
		this.clientR1 = clientR1;
		this.clientR2 = clientR2;
		calcCutterRobotToHolderRobot();
		calcDefaultPoseHoldersEndeffector();
		calcDefaultPoseWorkspace();
		calcEndeffectorToWorkspace();
	}
	
	/**
	 * Calculates the transformation matrix transforming from holder-robot to cutter-robot
	 * using cutterRobotToTrackingSystem and holderRobotToTrackingSystem matrices.
	 * Is being called in constructor of TransformCoords.
	 */
	private void calcCutterRobotToHolderRobot() {
		if(cutterRobotToTrackingSystem == null || cutterRobotToTrackingSystem == null) throw new NullPointerException(
				"[TransformCoords] Calculation of transformation matrix from robot1 to robot2 failed,"
				+ " cause at least one matrix was null.\nDid the calibration succeeded?");
		cutterRobotToHolderRobot = cutterRobotToTrackingSystem.multiply(createInversTransformationMatrix(holderRobotToTrackingSystem));
	}
	
	/**
	 * Calculates the default holder-robots end-effector pose relative to holder-robot.
	 * Is being called in constructor of TransformCoords.
	 */
	private void calcDefaultPoseHoldersEndeffector() {
		// create transformation matrix cutterRobotToHolderRobotsEndeffector relative to cutter-robot
		RealMatrix cutterRobotToHolderRobotsEndeffector = new Array2DRowRealMatrix(4,4);
		cutterRobotToHolderRobotsEndeffector.setSubMatrix(rotPartOfHolderRobotsEndeffectorDefaultPoseRelCutterRobot.getData(), 0, 0);
		RealVector homVec = new ArrayRealVector(new double[] {0,0,0,1});
		homVec.setSubVector(0, initialWorkspacePositionRelCutterRobot);
		cutterRobotToHolderRobotsEndeffector.setColumn(3, homVec.toArray());
		
		// transformation so it's relative to holder-robot
		this.defaultPoseHoldersEndeffector = this.createInversTransformationMatrix(cutterRobotToHolderRobot).multiply(cutterRobotToHolderRobotsEndeffector);
	}

	/**
	 * Calculates the default workspace pose relative to holder-robot.
	 * Is being called in constructor of TransformCoords.
	 */
	private void calcDefaultPoseWorkspace() {
		// create transformation matrix cutterRobotToWorkspace relative to cutter-robot
		RealMatrix cutterRobotToWorkspace = new Array2DRowRealMatrix(4,4);
		cutterRobotToWorkspace.setSubMatrix(rotPartOfWorkspaceDefaultPoseRelCutterRobot.getData(), 0, 0);
		RealVector homVec = new ArrayRealVector(new double[] {0,0,0,1});
		homVec.setSubVector(0, initialWorkspacePositionRelCutterRobot);
		cutterRobotToWorkspace.setColumn(3, homVec.toArray());
		
		// transformation so it's relative to holder-robot
		this.defaultPoseWorkspace = this.createInversTransformationMatrix(cutterRobotToHolderRobot).multiply(cutterRobotToWorkspace);
	}

	
	/**
	 * Calculates the transformation matrix transforming coordinates from workspace to end-effector.
	 *
	 */
	private void calcEndeffectorToWorkspace() {
		// create transformation matrix endeffectorToWorkspace
		this.endeffectorToWorkspace = this.createInversTransformationMatrix(defaultPoseHoldersEndeffector).multiply(defaultPoseWorkspace);
	}
	
	//==========GETTER===========
	
	/**
	 * Getter returns the client for the connection to the cutter-robot.
	 * @return clientR1 for connecting to cutter-robot
	 */
	protected TCPClient getClientR1() {
		return this.clientR1;
	}
	
	/**
	 * Getter returns the client for the connection to the holder-robot.
	 * @return clientR2 for connecting to holder-robot
	 */
	protected TCPClient getClientR2() {
		return this.clientR2;
	}
	
	/**
	 * Getter for the defaultPoseHoldersEndeffector RELATIVE TO HOLDER-ROBOT.
	 * @return RealMatrix containing defaultPoseHoldersEndeffector relative to holder-robot.
	 */
	protected RealMatrix getDefaultPoseHoldersEndeffector() {
		return defaultPoseHoldersEndeffector;
	}
	
	//======TRANSFORMATIONS======
	
	/**
	 * Transforming homogeneous position vector relative to workspace to position vector relative to cutter-robot.
	 * Is being used for transforming trajectory data from workspace coordinate system to cutter-robot's coordinate system.
	 * Therefore cutter-robot can move to the end-effector in such a way, to follow the trajectory.
	 * @param homPosVec homogeneous RealVector containing position x,y,z relative to workspace
	 * @return homogeneous RealVector containing position relative to cutter-robot
	 */
	private RealVector transformPosWorkspaceToCutterRobot(RealVector homPosVec) {
		return this.cutterRobotToWorkspace().operate(homPosVec);
	}
	
	/**
	 * Creates transformation matrix cutterRobotToWorkspace transforming coordinates from workspace to cutter-robot.
	 * @return homogeneous transformation matrix -> cutterRobotToWorkspace
	 */
	private RealMatrix cutterRobotToWorkspace() {
		return cutterRobotToHolderRobot.multiply(this.defaultPoseWorkspace);
	}
	
	/**
	 * Transforming homogeneous position vector relative to cutter-robot to position vector relative to holder-robot.
	 * @param homPosVec homogeneous RealVector containing position relative to cutter-robot
	 * @return homogeneous RealVector containing position relative to holder-robot
	 */
	private RealVector transformPosCutterToHolderRobot(RealVector homPosVec) {
		return this.createInversTransformationMatrix(cutterRobotToHolderRobot).operate(homPosVec);
	}
	
	/**
	 * Calculates RealMatrix ready to send to cutter-robot, so the cutter-robots end-effector moves in a way, that the tool
	 * moves to the specified point in the workspace.
	 * @param homPosVec homogeneous RealVector containing position x,y,z relative to workspace
	 * @return 	RealMatrix containing homogeneous RealMatrix contains pose matrix relative to cutter-robot, so that the tool moves
	 * 			to the specified position (homPosVec).
	 */
	protected RealMatrix getTrajectoryMatrixForCuttersEndeffector(RealVector homPosVec) {
		// describe trajectory position relative to cutter-robot
		RealVector posRelCutRobot = this.transformPosWorkspaceToCutterRobot(homPosVec);
		// describe trajectory position relative to cutter-robot's end-effector
		RealMatrix currentCutterRobotPose = this.measureCutterRobotPose();
		RealVector posRelCutRobotEndeffector = this.createInversTransformationMatrix(currentCutterRobotPose).operate(posRelCutRobot);
		// considering the tool-offset by subtracting the offset from the posRelCutRobotEndeffector
		RealVector endeffectorPositionRelEndeffector = endeffectorToCutter.getColumnVector(3).getSubVector(0, 3).mapMultiply(-1d).add(posRelCutRobotEndeffector);
		// describe calculated end-effector position relative to cutter-robot
		// set position
		double x = endeffectorPositionRelEndeffector.getEntry(0);
		double y = endeffectorPositionRelEndeffector.getEntry(1);
		double z = endeffectorPositionRelEndeffector.getEntry(2);
		RealVector homEndeffectorPositionRelEndeffector = new ArrayRealVector(new double[] {x,y,z,1});
		RealVector homEndeffectorPositionRelCutterRobot = currentCutterRobotPose.operate(homEndeffectorPositionRelEndeffector);
		
		//create final pose matrix
		RealMatrix endeffectorPoseRelCutterRobot = new Array2DRowRealMatrix(4,4);
		//set predefined rotational part
		//TODO: Hier wird davon ausgegangen, dass die Orientierung von Tool und Endeffector gleich ist. Muss man beim
		//TODO: Anbringen des Schneiders darauf achten. Klinge ist gewissermaßen der verlängerte Arm des Roboters.
		endeffectorPoseRelCutterRobot.setSubMatrix(this.standardCutterToolOrientation.getData(), 0, 0);
		//set translational part
		endeffectorPoseRelCutterRobot.setColumn(3, homEndeffectorPositionRelCutterRobot.toArray());
		return endeffectorPoseRelCutterRobot;
	}
	
	
	//=======COMMUNICATION=======
	
	/**
	 * send command to server suitable to TCPClient by TCPClient
	 * @param command to send to server
	 * @param client TCPClient for sending command to a specific server
	 */
	protected void send(String command, TCPClient client) {
		client.sendData(command);
	}
	
	/**
	 * receive message from server to TCPClient
	 * @param client contains the client that communicates with the server
	 * @return server response message
	 */
	protected String response(TCPClient client) {
		return client.receiveData();
	}
	
	/**
	 * Measures the current pose data of the cutter-robot's end-effector.
	 * @return 	currentCutterRobotEndeffectorPose RealMatrix containing homogeneous pose data of the cutter-robot's end-effector.
	 * 			relative to cutter-robot's coordinate-system
	 */
	private RealMatrix measureCutterRobotPose() {
		send("GetPositionHomRowWise", this.clientR1);
		return getHomRealMatrix(response(this.clientR1));
	}
	
	/**
	 * Measures the current pose data of the holder-robot's end-effector.
	 * @return 	currentHolderRobotEndeffectorPose RealMatrix containing homogeneous pose data of the holder-robot's end-effector.
	 * 			relative to holder-robot's coordinate-system
	 */
	private RealMatrix measureHolderRobotPose() {
		send("GetPositionHomRowWise", this.clientR2);
		return getHomRealMatrix(response(this.clientR2));
	}
	
	/**
	 * Returns RealMatrix containing pose data of the robot's end-effector
	 * @param positionHomRowWise String contains pose data of the robot's end-effector
	 * @return RealMatrix containing pose data of the robot's end-effector
	 */
	private static RealMatrix getHomRealMatrix(String positionHomRowWise) {
		String[] splitStringHomMatrix = positionHomRowWise.split("\s");
		double[] row1 = new double[4];
		double[] row2 = new double[4];
		double[] row3 = new double[4];
		ArrayList<double[]> rows = new ArrayList<double[]>();
		rows.add(row1);
		rows.add(row2);
		rows.add(row3);
		
		try
	    {
			for(int row = 0; row < 3; row++) {
				double[] currentRow = rows.get(row);
				//TODO: @Micha --> Werden die Werte beim Setzen in currentRow auch in die eizelnen rows gesetzt?
				//TODO: Eigentlich dürfte ja currentRow die Objekte von row1,2 & 3 erhalten, sodass das klappen dürfte.
				for(int col = 0; col < 4; col++) {
					currentRow[col] = Double.parseDouble(splitStringHomMatrix[row*4 + col]);
				}
			}
	    }
	    catch (NumberFormatException nfe)
	    {
	      System.out.println("[TransformCoords] An error occured while converting an string array (pose data) to double array.");
	    }
		double[][] matrix2dDoubleArray = new double[][] {rows.get(0),rows.get(1),rows.get(2),{0,0,0,1}};
		return new Array2DRowRealMatrix(matrix2dDoubleArray);
	}
	
	
	//==========GENERAL==========
	
	/**
	 * Creates the inverse transformation for a given homogeneous transformation matrix.
	 * @param homTransMat homogeneous transformation matrix
	 * @return invertedTransformationMatrix
	 */
	private RealMatrix createInversTransformationMatrix(RealMatrix homTransMat) {
		RealMatrix invertedRotationalPart = MatrixUtils.inverse(homTransMat.getSubMatrix(0, 2, 0, 2));
		RealVector invertedTranslationalPart = invertedRotationalPart.operate(homTransMat.getColumnVector(3).getSubVector(0, 3)).mapMultiply(-1d);
		RealMatrix invertedTransformationMatrix = new Array2DRowRealMatrix(4,4);
		invertedTransformationMatrix.setSubMatrix(invertedRotationalPart.getData(), 0, 0);
		invertedTransformationMatrix.setEntry(0, 3, invertedTranslationalPart.getEntry(0));
		invertedTransformationMatrix.setEntry(1, 3, invertedTranslationalPart.getEntry(1));
		invertedTransformationMatrix.setEntry(2, 3, invertedTranslationalPart.getEntry(2));
		invertedTransformationMatrix.setRow(3, new double[] {0,0,0,1});
		return invertedTransformationMatrix;
	}

	//TESTER-TESTER-TESTER-TESTER-TESTER-TESTER-TESTER-TESTER
	public static void main(String[] args) {
	
	}
	//TESTER-TESTER-TESTER-TESTER-TESTER-TESTER-TESTER-TESTER
	
}
