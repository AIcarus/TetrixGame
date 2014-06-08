package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;

/**
 * 
 * This class used to draw a circle, its radius to a constant acceleration of
 * the expansion of
 * 
 */
public class SingleSpark extends AbstractEffect {
	/**
	 * Changes in the radius of
	 */
	private double radius;
	/**
	 * Constant acceleration
	 */
	private double acc;
	/**
	 * Changes in the speed of
	 */
	private double vel;
	/**
	 * Painted the color of the circle
	 */
	private Color color;

	public SingleSpark() {
		this(0, 0, 0, Color.WHITE);
	}

	/**
	 * Create a new SingleSpark
	 * 
	 * @ Param initRadius The initial radius @ Param acc Acceleration @ Param
	 * initVel Initial velocity @ Param color Color
	 */
	public SingleSpark(double initRadius, double acc, double initVel,
			Color color) {
		initialize(initRadius, acc, initVel, color);
	}

	/***
	 * Initialize a SingleSpark
	 * 
	 * @ Param initRadius The initial radius @ Param acc Acceleration @ Param
	 * initVel Initial velocity @ Param color Color
	 */
	public void initialize(double initRadius, double acc, double initVel,
			Color color) {
		if (null == color) {
			throw new NullPointerException("color should not be null");
		}

		this.radius = initRadius;
		this.acc = acc;
		this.vel = initVel;

		this.color = color;

		this.setValid(true);
	}

	protected void onTickImpl(long interpolation) {
		radius += vel * interpolation;
		vel += acc * interpolation;
	}

	protected void paintImpl(Graphics g, final Point center, double scale) {
		g.setColor(color);
		g.fillOval((int) (center.x - (int) (radius * scale)),
				(int) (center.y - (int) (radius * scale)),
				(int) ((radius * 2) * scale), (int) ((radius * 2) * scale));
	}

	public double getRadius() {
		return radius;
	}

	public double getAcc() {
		return acc;
	}

	public double getVel() {
		return vel;
	}

	public Color getColor() {
		return color;
	}

	@Override
	protected void setValid(boolean valid) {
		super.setValid(valid);
	}
}
