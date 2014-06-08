package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import java.awt.Color;
import java.awt.Graphics;

/**
 * 
 * FireLine爆炸后，会生成一个不断扩大的小圆圈，就是这个了。
 * 
 */
public class FireLineBall {
	private double radius;

	private double finalRadius = 15 - 10 * Math.random();

	private static final double VEL = 0.1;

	private static final Color color = new Color(0, 183, 239);

	private boolean valid;

	public FireLineBall() {
		reset();
	}

	public void reset() {
		radius = 0;
		valid = true;
	}

	public void onTick(long interpolation) {
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should not be negtive");
		}

		if (radius >= finalRadius) {
			valid = false;
			return;
		}

		radius += VEL * interpolation;
	}

	public boolean isValid() {
		return valid;
	}

	public void paint(Graphics g, double x, double y, double scale) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}
		if (scale <= 0) {
			throw new IllegalArgumentException("scale should be positive");
		}

		g.setColor(color);
		g.drawOval((int) (x - radius * scale), (int) (y - radius * scale),
				(int) (2 * radius * scale), (int) (2 * radius * scale));
	}

}
