package net.ddns.x444556;

public class Vector3 {
	public double X, Y, Z;
	
	public Vector3() {
		X = 0;
		Y = 0;
		Z = 0;
	}
	public Vector3(double x, double y, double z) {
		X = x;
		Y = y;
		Z = z;
	}
	
	public double MaxSpeed() {
		return Math.sqrt(X * X + Y * Y + Z * Z);
	}
}
