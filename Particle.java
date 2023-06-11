package net.ddns.x444556;

public class Particle {
	public Vector3 Position;
	public Vector3 Speed;
	public Vector3 borderStart, borderEnd;
	public Vector3 minSpeed, maxSpeed;
	public long spawnedMs;
	public long respawnMs = 1000 * 5; // 5 sec
	public boolean disableRespawn = true;
	
	public Particle(Vector3 borderStart, Vector3 borderEnd, Vector3 minSpeed, Vector3 maxSpeed) {
		this.borderEnd = borderEnd;
		this.borderStart = borderStart;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		Position = new Vector3(Math.random() * (borderEnd.X - borderStart.X) + borderStart.X,
				Math.random() * (borderEnd.Y - borderStart.Y) + borderStart.Y,
				Math.random() * (borderEnd.Z - borderStart.Z) + borderStart.Z);
		Speed = new Vector3(Math.random() * (maxSpeed.X - minSpeed.X) + minSpeed.X,
				Math.random() * (maxSpeed.Y - minSpeed.Y) + minSpeed.Y,
				Math.random() * (maxSpeed.Z - minSpeed.Z) + minSpeed.Z);
		spawnedMs = System.currentTimeMillis();
		respawnMs += Math.random() * 6000 - 3000;
	}
	
	public void update(double deltaTime) {
		if(!disableRespawn && spawnedMs + respawnMs <= System.currentTimeMillis()) {
			Position = new Vector3(Math.random() * (borderEnd.X - borderStart.X) + borderStart.X,
					Math.random() * (borderEnd.Y - borderStart.Y) + borderStart.Y,
					Math.random() * (borderEnd.Z - borderStart.Z) + borderStart.Z);
			Speed = new Vector3(Math.random() * (maxSpeed.X - minSpeed.X) + minSpeed.X,
					Math.random() * (maxSpeed.Y - minSpeed.Y) + minSpeed.Y,
					Math.random() * (maxSpeed.Z - minSpeed.Z) + minSpeed.Z);
			spawnedMs = System.currentTimeMillis();
			respawnMs += Math.random() * 6000 - 3000;
		}
		else {
			Position.X += Speed.X * (deltaTime / 1000000000.0);
			Position.Y += Speed.Y * (deltaTime / 1000000000.0);
			Position.Z += Speed.Z * (deltaTime / 1000000000.0);
			
			if(Position.X <= borderStart.X) {
				Position.X = borderStart.X;
				Speed.X = -Speed.X;
			}
			if(Position.Y <= borderStart.Y) {
				Position.Y = borderStart.Y;
				Speed.Y = -Speed.Y;
			}
			if(Position.Z <= borderStart.Z) {
				Position.Z = borderStart.Z;
				Speed.Z = -Speed.Z;
			}
			
			if(Position.X >= borderEnd.X) {
				Position.X = borderEnd.X;
				Speed.X = -Speed.X;
			}
			if(Position.Y >= borderEnd.Y) {
				Position.Y = borderEnd.Y;
				Speed.Y = -Speed.Y;
			}
			if(Position.Z >= borderEnd.Z) {
				Position.Z = borderEnd.Z;
				Speed.Z = -Speed.Z;
			}
		}
	}
	public double getSpeed() {
		return Math.sqrt(Speed.X * Speed.X + Speed.Y * Speed.Y + Speed.Z * Speed.Z);
	}
}
