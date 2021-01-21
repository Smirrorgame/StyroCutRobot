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
		
		Vector3D edge1 = this.vertices[1].subtract(this.vertices[0]);
		Vector3D edge2 = this.vertices[1].subtract(this.vertices[0]);
		this.normal = Vector3D.crossProduct(edge1, edge2).normalize();
	}

}
