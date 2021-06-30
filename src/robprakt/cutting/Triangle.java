package robprakt.cutting;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Triangle {
	
	private Vector3D[] vertices;
	private Vector3D normal;
	
	public Triangle(Vector3D v1,Vector3D v2,Vector3D v3) {
		vertices = new Vector3D[3];
		this.vertices[0] = v1;
		this.vertices[1] = v2;
		this.vertices[2] = v3;
		
		//Calculating normal of the triangle, assuming, that the order of the vertices is given for negative orientation.
		//Vertices given: A,B,C
		//normal = AB*BC
		Vector3D edge1 = this.vertices[1].subtract(this.vertices[0]);
		Vector3D edge2 = this.vertices[2].subtract(this.vertices[1]);
		this.normal = Vector3D.crossProduct(edge1, edge2).normalize();
	}
	
	public Vector3D[] getVertices() {
		return this.vertices;
	}
	
	public Vector3D getNormal() {
		return this.normal;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Vector3D v: vertices) {
			s+=v.toString()+"\n";
		}
		s+="\n";
		
		return s;
	}

}
