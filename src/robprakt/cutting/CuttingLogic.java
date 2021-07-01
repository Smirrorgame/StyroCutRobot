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
	 * neutralPositionCutterRobot used for trajectory calculations
	 * contains position of neutral Position for cutting tool in homegeneous coordinates
	 * relative to workspace (is being set in constructor)
	 */
	private RealVector neutralPositionCutterRobot;
	
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
		double offset = 100d; // offset from the styrofoam cylinder (This is the gap between styrofoam and the cutting tool.)
		//defining neutral position relative to workspace
		double xValue = (-1d)*(this.radiusStyroCylinder + offset);
		double yValue = (-1d)*(this.radiusStyroCylinder + offset);
		double zValue = this.heightStyroHolder + this.heightStyroCylinder + offset;
		//creating vector relative to workspace
		RealVector neutralPositionVector = new ArrayRealVector(new double[] {xValue, yValue, zValue, 1});
		//saving neutral position relative to workspace in CuttingLogic
		this.neutralPositionCutterRobot = neutralPositionVector;
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
	
	//TODO: Derzeit ist die Entfernung von Schnittstücken nicht implementiert. Können wir vorerst weglassen,
	//TODO: , damit man es später leichter hat die Fehler zu finden. Falls es denn welche gibt. ^^
	
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
			
			//
			//PREPARE FIRST CUT
			//
			
			//calculate angle, so that the normal of the triangle points to negative y-axis (x-component of normal is zero)
			double angle = rotationAngle(tr);
			
			//calculate rotation matrix (used for end-effector pose calculation and transformation in of triangles in workspace)
			RealMatrix rotationMatrix = this.calcRotationMatrix(angle);
			
			//calculate pose matrix for holder robot
			RealMatrix poseMatrixHolderRobot = calcPoseHolderRobot(rotationMatrix);
			
			//move holder-robot into cutting position
			robotMovement.moveMinChange(poseMatrixHolderRobot, this.transformCoords.getClientR2());
			
			//rotate Triangle so you can calculate with rotated vertices and rotated normal
			Triangle rotTr = rotateTriangle(tr,rotationMatrix);
			
			//calculate vertices for trajectory for FIRST CUT
			RealVector[] vertices = calculateVerticesFirstCut(rotationMatrix, rotTr);
			
			//
			//EXECUTE FIRST CUT
			//
			
			//move from Neutral-Position to startPoint of the trajectory
			robotMovement.moveCutterP2P(this.neutralPositionCutterRobot,vertices[0]);
			
			//cut through styro-foam
			//start- to mid-point
			robotMovement.moveCutterP2P(vertices[0], vertices[1]);
			//mid- to end-point
			robotMovement.moveCutterP2P(vertices[1], vertices[2]);
			
			//
			//PREPARE FOR NEXT CUT
			//
			
			//move to neutral position (potentially over auxiliary-position) 
			this.moveToNeutralPosition(vertices[2]);
			
			//
			//PREPARE FOR SECOND CUT
			//
			
			//turn holder-robot by 180°
			//calculate angle, so that the normal of the triangle points to positive y-axis (x-component of normal is zero)
			//use angle that has been calculated before and add 180°
			angle = angle + Math.PI;
			
			//calculate rotation matrix (used for end-effector pose calculation and transformation in of triangles in workspace)
			rotationMatrix = this.calcRotationMatrix(angle);
			
			//calculate pose matrix for holder robot
			poseMatrixHolderRobot = calcPoseHolderRobot(rotationMatrix);
			
			//move holder-robot into cutting position
			robotMovement.moveMinChange(poseMatrixHolderRobot, this.transformCoords.getClientR2());
			
			//rotate Triangle so you can calculate with rotated vertices and rotated normal
			rotTr = rotateTriangle(tr,rotationMatrix);
			
			//calculate vertices for trajectory for FIRST CUT
			vertices = calculateVerticesFirstCut(rotationMatrix, rotTr);
			
			//
			//EXECUTE SECOND CUT
			//
			
			//move from Neutral-Position to startPoint of the trajectory
			robotMovement.moveCutterP2P(this.neutralPositionCutterRobot,vertices[0]);
			
			//cut through styro-foam
			//start- to mid-point
			robotMovement.moveCutterP2P(vertices[0], vertices[1]);
			//mid- to end-point
			robotMovement.moveCutterP2P(vertices[1], vertices[2]);
			
			//
			//PREPARE FOR NEXT CUT
			//
			
			//move to neutral position (potentially over auxiliary-position) 
			this.moveToNeutralPosition(vertices[2]);
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
	
	/**
	 * Calculates the angle between negative y-axis and normal of the triangle. (normal is projected on x-y-plane)
	 * @param tr
	 * @return angle between negative y-axis and normal of the triangle (normal is projected on x-y-plane)
	 */
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
	
	/**
	 * Calculates homogeneous rotation matrix for rotation around z-axis.
	 * @param angle
	 * @return homogeneous rotation matrix
	 */
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
	
	/**
	 * Rotates the triangle around the z-axis.
	 * @param tr is the input triangle
	 * @param rotationMatrix is the rotation matrix that is used to rotate vertices and normal
	 * @return rotated triangle
	 */
	private Triangle rotateTriangle(Triangle tr, RealMatrix rotationMatrix) {
		return 	new Triangle(new Vector3D(rotationMatrix.operate(tr.getVertices()[0].toArray())),
				new Vector3D(rotationMatrix.operate(tr.getVertices()[1].toArray())),
				new Vector3D(rotationMatrix.operate(tr.getVertices()[2].toArray())));
	}
	
	/**
	 * Calculates the "striking" points of the trajectory relative to workspace.
	 * @param rotationMatrix
	 * @param tr
	 * @return array of Vector3D of length 3 with start- mid- and end-point
	 */
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
		Vector3D[] pointsOfTriangle = tr.getVertices();
		
		Plane plane = new Plane(pointsOfTriangle[0], tr.getNormal(), Math.pow(10.0d, -10.0d));
		
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
		
		//FILTER
		//identifying intersections which can be used as a start point
		//filter out only relevant intersections that are positioned directly near the cylinder
		//if the intersection is not relevant, set it to null
		if(!(intersectTopLine.getZ() >= topLeft.getY() && intersectTopLine.getZ() <= topRight.getY()) && intersectTopLine != null) {
			intersectTopLine = null; //top-line
		}
		if(!(intersectLeftLine.getZ() >= bottomLeft.getZ() && intersectLeftLine.getZ() <= topLeft.getZ()) && intersectLeftLine != null) {
			intersectLeftLine = null; //left-line
		}
		if(!(intersectRightLine.getZ() >= bottomRight.getZ() && intersectRightLine.getZ() <= topRight.getZ()) && intersectRightLine != null) {
			intersectRightLine = null; //right-line
		}
		
		//start point of the trajectory
		Vector3D startPoint = null;
		
		//choose start point
		if(intersectTopLine != null && intersectLeftLine == null && intersectRightLine == null) {
			//just top-line
			startPoint = intersectTopLine;
		} else {
			if(intersectTopLine != null) {
				//top and left or right
				if(intersectLeftLine != null && intersectRightLine == null) {
					//top and left
					startPoint = intersectTopLine;
				}
				if(intersectLeftLine == null && intersectRightLine != null) {
					//top and right
					startPoint = intersectTopLine;
				}
			} else {
				if(intersectLeftLine != null && intersectRightLine == null) {
					//only left
					startPoint = intersectLeftLine;
				}
				if(intersectLeftLine == null && intersectRightLine != null) {
					//only right
					startPoint = intersectLeftLine;
				}
			}
		}
		
		//search for triangle vertex, that is the lowest
		Vector3D lowestTriangleVertex = pointsOfTriangle[0];
		if(pointsOfTriangle[1].getZ() <= lowestTriangleVertex.getZ()) {
			lowestTriangleVertex = pointsOfTriangle[1];
			if(pointsOfTriangle[2].getZ() <= lowestTriangleVertex.getZ()) {
				lowestTriangleVertex = pointsOfTriangle[2];
			}
		}
		
		//calculate midpoint (tool stops at midpoint - it's the lowest point of the triangle)
		//after midpoint the tool leaves in the positive or negative y-direction
		
		//project lowest vertex in shifted z-y-plane
		Vector3D midPoint = new Vector3D(-this.radiusStyroCylinder,lowestTriangleVertex.getY(),lowestTriangleVertex.getZ());
		
		//calculate end-point of trajectory
		//end-point is located on left or right line
		//determining which on which end-point is located by normal of triangle
		
		//triangle surface points to the left
		Vector3D endPoint = new Vector3D(midPoint.getX(),-this.radiusStyroCylinder,midPoint.getZ());
		if (tr.getNormal().getY() > 0) {
			//triangle surface points to the right
			endPoint = new Vector3D(midPoint.getX(),this.radiusStyroCylinder,midPoint.getZ());
		} else if(tr.getNormal().getY() == 0) {
			//triangle is parallel to x-y-plane
			//cuts horizontal from left to right, or from right to left
			endPoint = startPoint.getY() > 0 ? 	new Vector3D(midPoint.getX(),-this.radiusStyroCylinder,midPoint.getZ()) :
												new Vector3D(midPoint.getX(),this.radiusStyroCylinder,midPoint.getZ());
		}
		
		//converting points to homogeneous RealVectors
		RealVector v0 = new ArrayRealVector(new double[] {0.0d,0.0d,0.0d,1.0d,});
		RealVector v1 = new ArrayRealVector(new double[] {0.0d,0.0d,0.0d,1.0d,});
		RealVector v2 = new ArrayRealVector(new double[] {0.0d,0.0d,0.0d,1.0d,});
		v0.setSubVector(0, new ArrayRealVector(startPoint.toArray()));
		v1.setSubVector(0, new ArrayRealVector(midPoint.toArray()));
		v2.setSubVector(0, new ArrayRealVector(endPoint.toArray()));
		
		
		return new RealVector[] {v0,v1,v2};
	}
	
	/**
	 * Decides whether the cutter robot moves straight to neutral position,
	 * or first to auxiliary and then neutral position.
	 * Gets called after each cut through the styro-foam.
	 * @param lastPosition
	 */
	private void moveToNeutralPosition(RealVector lastPosition) {
		//move to neutral position
		//check if last position of cutter robot was on left or right side
		if(lastPosition.getEntry(1) < 0) {
			//y-component is negative --> move straight to neutral
			robotMovement.moveToNeutralPosition();
		} else {
			//y-component is positive --> move to auxiliary and then to neutral position
			robotMovement.moveToAuxiliaryPosition();
			robotMovement.moveToNeutralPosition();
		}
	}
}
