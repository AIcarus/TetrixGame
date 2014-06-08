package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import com.accela.tetrixgame.util.ObjectPool;

/**
 * 
 * Speed, the speed of the x-axis and y-axis velocity component
 * 
 */
public class Velocity {
	public double vx;

	public double vy;

	private Velocity() {
		vx = 0;
		vy = 0;
	}

	/**
	 * Return x-axis along the clockwise direction to the speed of the angle.
	 * When the rate of return of zero is zero.
	 */
	public double getAngle() {
		if (0 == vx && 0 == vy) {
			return 0;
		}

		return Math.asin(vy / (Math.pow(vx * vx + vy * vy, 0.5)));
	}

	/**
	 * Absolute speed through the point of view of size and speed of the speed
	 * of the object to create a.
	 */
	public static Velocity fromAbsolutVel(double vel, double angle) {
		Velocity v = ObjectPool.retrieve(Velocity.class);
		if (null == v) {
			v = new Velocity();
		}

		v.vx = Math.cos(angle) * vel;
		v.vy = Math.sin(angle) * vel;

		return v;
	}

	// ///////////////////////////////////////////////////////////////

	private static Velocity retrieve() {
		Velocity v = ObjectPool.retrieve(Velocity.class);
		if (null == v) {
			v = new Velocity();
		}

		return v;
	}

	public static Velocity create() {
		return create(0, 0);
	}

	public static Velocity create(double vx, double vy) {
		Velocity v = retrieve();
		v.vx = vx;
		v.vy = vy;

		return v;
	}

	public static void dispose(Velocity v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		ObjectPool.put(v);
	}

}
