package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;

/**
 * 
 * This class used to draw a two-tier SinleSpark nested together inside and
 * outside of the spark. Both inside and outside layer of the radius of the
 * sparks are a constant change of acceleration. When the inner radius of the
 * spark to reach or exceed the outside Layer when the radius of the spark, it
 * will make the results invalid.
 * 
 */
public class DoubleSpark extends AbstractEffect {
	/**
	 * Sparks the inner object
	 */
	private SingleSpark innerSpark;
	/**
	 * Sparks outer object
	 */
	private SingleSpark outerSpark;

	/**
	 * SingleSpark from the outer lining of the center point to the direction of
	 * the center of SingleSpark, arc system, swing coordinate system
	 */
	private double angle;

	public DoubleSpark() {
		this(0, 0, 0, Color.WHITE, 0, 0, 0, Color.YELLOW, 0);
	}

	/**
	 * DoubleSpark create a new object. ObjectPool with the new process and, as
	 * far as possible the object of reuse has been released.
	 * 
	 * @param outerSparkInitRadius
	 *            The outer radius of the initial spark
	 * @param outerSparkAcc
	 *            Acceleration of the outer spark
	 * @param outerSparkInitVel
	 *            Initial velocity of the outer spark
	 * @param angle
	 *            SingleSpark from the outer lining of the center point to the
	 *            center of a circle SingleSpark the direction angle
	 */
	public DoubleSpark(double outerSparkInitRadius, double outerSparkAcc,
			double outerSparkInitVel, double angle) {
		this(outerSparkInitRadius / 2, outerSparkAcc * 2,
				outerSparkInitVel / 2, Color.WHITE, outerSparkInitRadius,
				outerSparkAcc, outerSparkInitVel, Color.YELLOW, angle);
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
	 * velocity @ Param outerSparkColor Outer color sparks @ Param angle
	 * SingleSpark from the outer lining of the center point to the center of a
	 * circle SingleSpark the direction angle
	 */
	public DoubleSpark(double innerSparkInitRadius, double innerSparkAcc,
			double innerSparkInitVel, Color innerSparkColor,
			double outerSparkInitRadius, double outerSparkAcc,
			double outerSparkInitVel, Color outerSparkColor, double angle) {
		this.initialize(innerSparkInitRadius, innerSparkAcc, innerSparkInitVel,
				innerSparkColor, outerSparkInitRadius, outerSparkAcc,
				outerSparkInitVel, outerSparkColor, angle);
	}

	/**
	 * 初始化一个DoubleSpark对象。新建过程与ObjectPool配合，会先尝试从ObjectPool中
	 * 取出两个SingleSpark对象，只有当没有可供取出的对象的时候，才会新建它们。
	 * 
	 * @param innerSparkInitRadius
	 *            内层火花的初始半径
	 * @param innerSparkAcc
	 *            内层火花的加速度
	 * @param innerSparkInitVel
	 *            内层火花的初始速度
	 * @param innerSparkColor
	 *            内层火花的颜色
	 * @param outerSparkInitRadius
	 *            外层火花的初始半径
	 * @param outerSparkAcc
	 *            外层火花的加速度
	 * @param outerSparkInitVel
	 *            外层火花的初始速度
	 * @param outerSparkColor
	 *            外层火花的颜色
	 * @param angle
	 *            从外层的SingleSpark的圆心指向内层的SingleSpark的圆心的方向角
	 */
	public void initialize(double innerSparkInitRadius, double innerSparkAcc,
			double innerSparkInitVel, Color innerSparkColor,
			double outerSparkInitRadius, double outerSparkAcc,
			double outerSparkInitVel, Color outerSparkColor, double angle) {
		if (null == innerSpark) {
			innerSpark = new SingleSpark(innerSparkInitRadius, innerSparkAcc,
					innerSparkInitVel, innerSparkColor);
		} else {
			innerSpark.initialize(innerSparkInitRadius, innerSparkAcc,
					innerSparkInitVel, innerSparkColor);
		}

		if (null == outerSpark) {
			outerSpark = new SingleSpark(outerSparkInitRadius, outerSparkAcc,
					outerSparkInitVel, outerSparkColor);
		} else {
			outerSpark.initialize(outerSparkInitRadius, outerSparkAcc,
					outerSparkInitVel, outerSparkColor);
		}

		this.angle = angle;

		this.setValid(true);
	}

	protected void onTickImpl(long interpolation) {
		innerSpark.onTick(interpolation);
		outerSpark.onTick(interpolation);

		if (innerSpark.getRadius() > outerSpark.getRadius()) {
			innerSpark.setValid(false);
			outerSpark.setValid(false);
			this.setValid(false);
		}
	}

	protected void paintImpl(Graphics g, final Point center, double scale) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}
		if (null == center) {
			throw new NullPointerException("center should not be null");
		}

		outerSpark.paint(g, center, scale);

		Point innerCenter = new Point(center);
		double length = (outerSpark.getRadius() - innerSpark.getRadius())
				* scale;
		innerCenter.move((int) (center.x + length * Math.cos(angle)),
				(int) (center.y + length * Math.sin(angle)));

		innerSpark.paint(g, innerCenter, scale);
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public SingleSpark getInnerSpark() {
		return innerSpark;
	}

	public SingleSpark getOuterSpark() {
		return outerSpark;
	}

}
