package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.FlyingSpark;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.Velocity;

public class Spark extends AbstractEffect {
	private FlyingSpark flyingSpark;

	private Point center;

	private long totalDelay = 0;

	private long delayCounter = 0;

	private static final Color outerColor = Color.YELLOW;

	public Spark(Point center, SparkAttributes attr) {
		initialize(center, attr);
	}

	public void initialize(Point center, SparkAttributes attr) {
		if (null == center) {
			throw new NullPointerException("center should not be null");
		}
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}
		this.center = new Point(center);

		double randomFactor = Math.random() * 0.4 + 1 - 0.2;

		if (null == flyingSpark) {
			flyingSpark = new FlyingSpark(
					Integer.parseInt(attr
							.get(GameAttributes.INNER_SPARK_RAIDUS))
							* randomFactor,
					Double.parseDouble(attr
							.get(GameAttributes.INNER_SPARK_ACC))
							* randomFactor,
					Double.parseDouble(attr
							.get(GameAttributes.INNER_SPARK_VEL))
							* randomFactor,
					Color.WHITE,
					Integer.parseInt(attr
							.get(GameAttributes.OUTER_SPARK_RAIDUS))
							* randomFactor,
					Double.parseDouble(attr
							.get(GameAttributes.OUTER_SPARK_ACC))
							* randomFactor,
					Double.parseDouble(attr
							.get(GameAttributes.OUTER_SPARK_VEL))
							* randomFactor,
					getColor(),
					Velocity.fromAbsolutVel(
							Double.parseDouble(attr
									.get(GameAttributes.SPARK_VEL))
									* randomFactor, Math.random() * Math.PI * 2),
					Double.parseDouble(attr
							.get(GameAttributes.SPARK_GRAVITY))
							* randomFactor);
		} else {
			flyingSpark
					.initialize(
							Integer.parseInt(attr
									.get(GameAttributes.INNER_SPARK_RAIDUS))
									* randomFactor,
							Double.parseDouble(attr
									.get(GameAttributes.INNER_SPARK_ACC))
									* randomFactor,
							Double.parseDouble(attr
									.get(GameAttributes.INNER_SPARK_VEL))
									* randomFactor,
							Color.WHITE,
							Integer.parseInt(attr
									.get(GameAttributes.OUTER_SPARK_RAIDUS))
									* randomFactor,
							Double.parseDouble(attr
									.get(GameAttributes.OUTER_SPARK_ACC))
									* randomFactor,
							Double.parseDouble(attr
									.get(GameAttributes.OUTER_SPARK_VEL))
									* randomFactor,
							getColor(),
							Velocity.fromAbsolutVel(
									Double.parseDouble(attr
											.get(GameAttributes.SPARK_VEL))
											* randomFactor, Math.random()
											* Math.PI * 2),
							Double.parseDouble(attr
									.get(GameAttributes.SPARK_GRAVITY))
									* randomFactor);
		}

		this.setValid(true);

		delayCounter = 0;

	}

	private Color getColor() {
		if (Math.random() >= 0.7) {
			return new Color(255, (int) (180 + Math.random() * (255 - 180)), 0);
		} else {
			return outerColor;
		}
	}

	@Override
	protected void onTickImpl(long interpolation) {
		if (delayCounter < totalDelay) {
			delayCounter += interpolation;
			return;
		}

		flyingSpark.onTick(interpolation);

		if (!flyingSpark.isValid()) {
			this.setValid(false);
		}
	}

	private Point paintCenter = new Point();

	@Override
	protected void paintImpl(Graphics g, final Point center, double scale) {
		if (delayCounter < totalDelay) {
			return;
		}

		paintCenter.x = (int) (center.x + this.center.x * scale);
		paintCenter.y = (int) (center.y + this.center.y * scale);

		flyingSpark.paint(g, paintCenter, scale);
	}

	public long getTotalDelay() {
		return totalDelay;
	}

	public void setTotalDelay(long totalDelay) {
		if (totalDelay < 0) {
			throw new IllegalArgumentException("totalDelay should not be null");
		}

		this.totalDelay = totalDelay;
	}

	public long getDelayCounter() {
		return delayCounter;
	}

	public Point getCenter() {
		return center;
	}

}
