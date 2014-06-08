package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;

/**
 * 
 * 这个类提供绘制一个可以按照设定的重力来作抛体运动的 DoubleSpark
 * 
 */
public class FlyingSpark extends AbstractEffect {
	/**
	 * To be drawn in flight doubleSpark
	 */
	private DoubleSpark doubleSpark;
	/**
	 * Flight speed (including direction)
	 */
	private Velocity flyingVel;
	/**
	 * Gravity
	 */
	private double gravity;
	/**
	 * DoubleSpark drawn by the location of center x coordinate
	 */
	private double x;
	/**
	 * DoubleSpark drawn by the location of center y coordinate
	 */
	private double y;

	public FlyingSpark() {
		this(0, 0, 0, Color.WHITE, 0, 0, 0, Color.YELLOW, Velocity.create(), 0);
	}

	/**
	 * FlyingSpark create a new object. ObjectPool with the new process, as far
	 * as possible the object of reuse has been released.
	 * 
	 * @ Param outerSparkInitRadius The outer radius of the initial spark @
	 * Param outerSparkAcc Acceleration of the outer spark @ Param
	 * outerSparkInitVel Outer spark the initial velocity @ Param
	 * initialFlyingVel The initial speed @ Param gravity Acceleration due to
	 * gravity
	 */
	public FlyingSpark(double outerSparkInitRadius, double outerSparkAcc,
			double outerSparkInitVel, Velocity initialFlyingVel, double gravity) {
		this(outerSparkInitRadius / 2, outerSparkAcc * 2,
				outerSparkInitVel / 2, Color.WHITE, outerSparkInitRadius,
				outerSparkAcc, outerSparkInitVel, Color.YELLOW,
				initialFlyingVel, gravity);
	}

	/**
	 * Create a new object DoubleSpark. ObjectPool with the new process and, as
	 * far as possible the object of reuse has been released.
	 * 
	 * @ Param innerSparkInitRadius The initial radius of the inner spark @
	 * Param innerSparkAcc Inner sparks of acceleration @ Param
	 * innerSparkInitVel Inner spark of the initial velocity @ Param
	 * innerSparkColor Inner sparks of color @ Param outerSparkInitRadius The
	 * outer radius of the initial spark @ Param outerSparkAcc Acceleration of
	 * the outer spark @ Param outerSparkInitVel Outer spark the initial
	 * velocity @ Param outerSparkColor Outer color sparks @ Param
	 * initialFlyingVel The initial speed @ Param gravity Acceleration due to
	 * gravity
	 */
	public FlyingSpark(double innerSparkInitRadius, double innerSparkAcc,
			double innerSparkInitVel, Color innerSparkColor,
			double outerSparkInitRadius, double outerSparkAcc,
			double outerSparkInitVel, Color outerSparkColor,
			Velocity initialFlyingVel, double gravity) {
		this.initialize(innerSparkInitRadius, innerSparkAcc, innerSparkInitVel,
				innerSparkColor, outerSparkInitRadius, outerSparkAcc,
				outerSparkInitVel, outerSparkColor, initialFlyingVel, gravity);
	}

	/**
	 * Create a new object FlyingSpark. ObjectPool with the new process will
	 * first attempt in ObjectPool Remove an object DoubleSpark only when the
	 * object is not available for take out when it will be new.
	 * 
	 * @ Param innerSparkInitRadius The initial radius of the inner spark @
	 * Param innerSparkAcc Inner sparks of acceleration @ Param
	 * innerSparkInitVel Inner spark of the initial velocity @ Param
	 * innerSparkColor Inner sparks of color @ Param outerSparkInitRadius The
	 * outer radius of the initial spark @ Param outerSparkAcc Acceleration of
	 * the outer spark @ Param outerSparkInitVel Outer spark the initial
	 * velocity @ Param outerSparkColor Outer color sparks @ Param
	 * initialFlyingVel The initial speed @ Param gravity Acceleration due to
	 * gravity
	 */
	public void initialize(double innerSparkInitRadius, double innerSparkAcc,
			double innerSparkInitVel, Color innerSparkColor,
			double outerSparkInitRadius, double outerSparkAcc,
			double outerSparkInitVel, Color outerSparkColor,
			Velocity initialFlyingVel, double gravity) {
		this.gravity = gravity;

		if (null == flyingVel) {
			flyingVel = Velocity.create();
		}
		flyingVel.vx = initialFlyingVel.vx;
		flyingVel.vy = initialFlyingVel.vy;

		if (null == doubleSpark) {
			doubleSpark = new DoubleSpark(innerSparkInitRadius, innerSparkAcc,
					innerSparkInitVel, innerSparkColor, outerSparkInitRadius,
					outerSparkAcc, outerSparkInitVel, outerSparkColor,
					flyingVel.getAngle());
		} else {
			doubleSpark.initialize(innerSparkInitRadius, innerSparkAcc,
					innerSparkInitVel, innerSparkColor, outerSparkInitRadius,
					outerSparkAcc, outerSparkInitVel, outerSparkColor,
					flyingVel.getAngle());
		}

		this.setValid(true);
		x = 0;
		y = 0;

	}

	@Override
	protected void onTickImpl(long interpolation) {
		x += flyingVel.vx * interpolation;
		y += flyingVel.vy * interpolation;

		flyingVel.vy += gravity * interpolation;

		doubleSpark.onTick(interpolation);

		if (!doubleSpark.isValid()) {
			setValid(false);
		}
	}

	private Point p = new Point();

	@Override
	protected void paintImpl(Graphics g, final Point center, double scale) {
		p.move((int) (center.x + x * scale), (int) (center.y + y * scale));
		doubleSpark.paint(g, p, scale);
	}

	@Override
	protected void finalize() throws Throwable {
		if (flyingVel != null)
			;
		{
			Velocity.dispose(flyingVel);
		}

		super.finalize();
	}

}
