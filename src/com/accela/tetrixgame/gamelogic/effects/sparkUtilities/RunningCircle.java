package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;

public class RunningCircle extends AbstractEffect {
	private double radius;

	private double finalRadius = 200 - 100 * Math.random();

	private double vel = 0;

	private static final double ACC = 0.001;

	private static final double INIT_VEL = 0.2;

	private static final Color color = new Color(255, 255, 255);// new
	// Color(153,
	// 217, 234);

	private Point center = new Point();

	public RunningCircle(Point center) {
		initialize(center);
	}

	public void initialize(Point center) {
		if (null == center) {
			throw new NullPointerException("center should not be null");
		}

		radius = 0;
		vel = INIT_VEL;
		this.center.x = center.x;
		this.center.y = center.y;

		this.setValid(true);
	}

	@Override
	protected void onTickImpl(long interpolation) {
		if (radius >= finalRadius) {
			this.setValid(false);
			return;
		}

		radius += vel * interpolation;
		vel += ACC * interpolation;

	}

	@Override
	protected void paintImpl(Graphics g, Point center, double scale) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}

		g.setColor(color);
		g.drawOval(center.x + (int) ((this.center.x - radius) * scale),
				center.y + (int) ((this.center.y - radius) * scale),
				(int) (2 * radius * scale), (int) (2 * radius * scale));

	}
}
