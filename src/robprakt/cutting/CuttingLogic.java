package robprakt.cutting;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
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
	
	/**
	 * The radius of the styrofoam cylinder in millimeters.
	 * Currently assuming to have 200mm for the radius.
	 */
	private final double radiusStyroCylinder = 200;
	
	/**
	 * The height of the styrofoam cylinder in millimeters.
	 * Currently assuming to have 400mm for the height.
	 */
	private final double heightStyroCylinder = 400;
	
	/**
	 * The height of the styrofoam holder in millimeters.
	 * Currently assuming to have 13mm as a radius.
	 */
	//TODO: Möglicherweise muss man noch einen weiteren Offset definieren, falls das Endeffektorkoordinatensystem
	//TODO: nicht so wie derzeit erwartet auf der Flanschoberfläche liegt, sondern noch verschobnen ist.
	//TODO: Genau genommen muss man den Offset von der Lage des Workspaces abhängig machen, allerdings 
	//TODO: sind das Endeffektorkoordinatensystem und das Workspacekoordinatensystem derzeit von der Position her identisch.
	private final double heightStyroHolder = 13;
	
	/**
	 * The height that is needed to attach the styro-foam on the holder.
	 * Is needed to prevent collision of cutting tool with attachment gear.
	 */
	private final double assemblyZoneHeight = 30;
	
	/**
	 * List contains triangles from the STL file, that are describing the model.
	 * The number of triangles determines the number of cuts to completely cut the object.
	 */
	//TODO: Die Triangle müssen nach dem laden hier gespeichert werden.
	private ArrayList<Triangle> triangles;
	
	/**
	 * Accuracy defines a tolerance for different calculations.
	 */
	private final double accuracy = Math.pow(10, -5);
	
	
	//===========================
	//==========METHODS==========
	//===========================
	
	//===========SETUP===========
	
	/**
	 * Constructor of CuttingLogic creates TransformCoords and RobotMovement instances.
	 * @param clientR1 client for connecting to cutter-robot
	 * @param clientR2 client for connecting to holder-robot
	 */
	public CuttingLogic(TCPClient clientR1, TCPClient clientR2) {
		this.transformCoords = new TransformCoords(clientR1, clientR2);
		this.robotMovement = new RobotMovement(transformCoords);
		setNeutralPosition();
		setAuxiliaryPosition();
	}
	
	/**
	 * Calculates the neutral position of the cutter-robots end-effector
	 * and saves it in robotMovement.
	 * Is being called in constructor of CuttingLogic.
	 */
	private void setNeutralPosition() {
		double offset = 100; // offset from the styrofoam cylinder (This is the gap between styrofoam and the cutting tool.)
		//defining neutral position relative to workspace
		double xValue = (-1d)*(this.radiusStyroCylinder + offset);
		double yValue = (-1d)*(this.radiusStyroCylinder + offset);
		double zValue = this.heightStyroHolder + this.heightStyroCylinder + offset;
		//creating vector relative to workspace
		RealVector neutralPositionVector = new ArrayRealVector(new double[] {xValue, yValue, zValue, 1});
		//saving neutral position in robotMovement
		robotMovement.setNeutralPosition(neutralPositionVector);
		}
	
	/**
	 * Calculates the auxiliary position of the cutter-robots end-effector
	 * and saves it in robotMovement.
	 * Is being called in constructor of CuttingLogic.
	 */
	private void setAuxiliaryPosition() {
		double offset = 100; // offset from the styrofoam cylinder
		//defining neutral position relative to workspace
		double xValue = (-1d)*(this.radiusStyroCylinder + offset);
		double yValue = this.radiusStyroCylinder + offset;
		double zValue = this.heightStyroHolder + this.heightStyroCylinder + offset;
		//creating vector relative to workspace
		RealVector auxiliaryPositionVector = new ArrayRealVector(new double[] {xValue, yValue, zValue, 1});
		//saving neutral position in robotMovement
		robotMovement.setAuxiliaryPosition(auxiliaryPositionVector);
		}
	

	//==========CUTTING==========
	
	/**
	 * Starts the cutting process.
	 * @throws Exception if the holder robot couldn't move into default pose.
	 */
	public void cut() throws Exception {
		//set cutting status to active
		this.isCuttingActive = true;
		
		//move robots into initial position
		moveHolderRobotToDefaultPose();
		robotMovement.moveToNeutralPosition();
		
		//gradually calculate and execute cuts for each triangle
		for(int cnt = 0; cnt < this.triangles.size(); cnt++) {
			Triangle tr = triangles.get(cnt);
			
			//
			// Every calculation is relative to the workspace.
			// Transformation into other coordinate systems is handled in TransformCoords and RobotMovement.
			//
			
			/*
			 *The idea is, that the initial pose of the holder-robot's end-effector
			 *is the reference pose matrix. That means, that initially the model to be cut
			 *is not rotated, cause the holder-robot is still in it's initial position.
			 *The holder-robot's rotate around the z-axis //TODO when a triangle has to be aligned
			 *so it can be cut by cutter-robot.
			 *So the algorithm is not recursive. You always start with the reference pose.
			 */
			
			//calculate angle, so that the normal of the triangle points to negative y-axis (x-component of normal is zero)
			double angle = rotationAngle(tr);
			
			//calculate rotation matrix (used for end-effector pose calculation and transformation in of triangles in workspace)
			RealMatrix rotationMatrix = this.calcRotationMatrix(angle);
			
			//calculate pose matrix for holder robot
			RealMatrix poseMatrixHolderRobot = calcPoseHolderRobot(rotationMatrix);
			
			//move holder-robot into cutting position
			robotMovement.moveMinChange(poseMatrixHolderRobot, this.transformCoords.getClientR2());
			
			//calculate vertices for trajectory for FIRST CUT
			RealVector[] vertices = calculateVerticesFirstCut(rotationMatrix, tr);
		}
		
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
	
	
	private double rotationAngle(Triangle tr) {
		
		Vector3D normal = tr.getNormal();
		//check if normal is parallel to z-axis
		if(Math.abs(normal.getX())<accuracy && Math.abs(normal.getY())<accuracy) {
			Vector2D negativeYaxis = new Vector2D(0.0d, -1.0d);
			Vector2D normal2D = new Vector2D(normal.getX(),normal.getY());

			return Vector2D.angle(negativeYaxis, normal2D);
		}
		return 0;
	}
	
	private RealMatrix calcRotationMatrix(double angle) {
		return new Array2DRowRealMatrix(new double[][] {
			{1.0d,0.0d,0.0d,0.0d},
			{0.0d,Math.cos(angle),-Math.sin(angle),0.0d},
			{0.0d,Math.sin(angle),Math.cos(angle),0.0d},
			{0.0d,0.0d,0.0d,1.0d}});
	}
	
	/**
	 * Calculates the pose of the holder-robot. Pose matrix is relative to holder-robot.
	 * Uses initial pose matrix of holder-robot as a reference pose. Pose matrix is rotated by a certain angle
	 * around z axis of the end-effector coordinate system.
	 * @param rotationMatrix for rotation around z-axis with a certain angle
	 * @return rotated pose matrix of holder-robot's end-effector relative to holder-robot
	 */
	private RealMatrix calcPoseHolderRobot(RealMatrix rotationMatrix) {
		//get initial end-effector pose of holder-robot
		RealMatrix initialPoseHolderRobot = this.transformCoords.getDefaultPoseHoldersEndeffector();
		
		//TODO: Als Rotationsachse wurde hier die z-Achse vom Endeffektor gewählt, allerdings wissen wir nicht genau, wie die
		//TODO: Koordinatensysteme zum Endeffektor positioniert/orientiert sind. Möglicherweise muss man die Rotationsmatrix in den Raum
		//TODO: des Halterungskoordinatensystems transformieren
		
		//calculate new pose-matrix for holder-robot's end-effector
		return initialPoseHolderRobot.multiply(rotationMatrix);
	}
	
	private RealVector[] calculateVerticesFirstCut(RealMatrix rotationMatrix, Triangle tr){
		//defining vertices that surround the projection of the cylinder on the shifted yz-plane
		/*
		 *assuming workspace-coordinate-system is positioned on the flange //TODO
		 */
		Vector3D bottomLeft = new Vector3D(-this.radiusStyroCylinder,-this.radiusStyroCylinder,this.heightStyroHolder+this.assemblyZoneHeight);
		Vector3D bottomRight = new Vector3D(-this.radiusStyroCylinder,this.radiusStyroCylinder,this.heightStyroHolder+this.assemblyZoneHeight);
		Vector3D topLeft = new Vector3D(-this.radiusStyroCylinder,-this.radiusStyroCylinder,this.heightStyroCylinder+this.heightStyroHolder+this.assemblyZoneHeight);
		Vector3D topRight= new Vector3D(-this.radiusStyroCylinder,this.radiusStyroCylinder,this.heightStyroCylinder+this.heightStyroHolder+this.assemblyZoneHeight);
		
		//defining triangle plane
		Vector3D[] pointOfTriangle = tr.getVertices();
		Plane plane = new Plane(pointOfTriangle[0], tr.getNormal(), Math.pow(10.0d, -10.0d));
		
		//defining lines for start- and end-points of a trajectory for a cut
		Line leftLine = new Line(bottomLeft,topLeft,Math.pow(10.0d, -10.0d));
		Line rightLine = new Line(bottomRight,topRight,Math.pow(10.0d, -10.0d));
		Line topLine = new Line(topLeft,topRight,Math.pow(10.0d, -10.0d));
		//Line bottomLine = new Line(bottomLeft,bottomRight,Math.pow(10.0d, -10.0d));

		//calculating intersections between plane and lines
		Vector3D intersectLeftLine = plane.intersection(leftLine);
		Vector3D intersectRightLine = plane.intersection(rightLine);
		Vector3D intersectTopLine = plane.intersection(topLine);
		//Vector3D intersectBottomLine = plane.intersection(bottomLine);
		
		//identifying intersections which can be used as a start point
		Vector3D startPoint = intersectLeftLine;
		
		//MIT NULL ARBEITEN!!!!!!!!!!!!!!!!!!!!!!
		
		if((intersectLeftLine.getZ() >= bottomLeft.getZ() && intersectLeftLine.getZ() <= topLeft.getZ())) {
			if((intersectRightLine.getZ() >= bottomRight.getZ() && intersectRightLine.getZ() <= topRight.getZ())) {
				//intersections on the left and right side
				//choose intersection with highest z-component as a starting point
				startPoint = (intersectLeftLine.getZ() >= intersectRightLine.getZ()) ? intersectLeftLine : intersectRightLine;
			}
		}
		
		if((intersectTopLine.getZ() >= topLeft.getY() && intersectLeftLine.getZ() <= t.getY())) {
			if(!(intersectLeftLine.getZ() >= bottomLeft.getZ() && intersectLeftLine.getZ() <= topLeft.getZ())) {
				if(!(intersectRightLine.getZ() >= bottomRight.getZ() && intersectRightLine.getZ() <= topRight.getZ())) {
					//intersection only on top line
					startPoint = intersectTopLine;
				}
			}
		}
		
		
		if(!(intersectTopLine.getZ() >= topLeft.getY() && intersectLeftLine.getZ() <= t.getY())) {
			if(!(intersectLeftLine.getZ() >= bottomLeft.getZ() && intersectLeftLine.getZ() <= topLeft.getZ())) {
				
			}
		}
		return null;
	}
}
