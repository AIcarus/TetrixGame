package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.util.ObjectPool;

public class Point2D {
	/**
	 * 点的横坐标
	 */
	public double x;
	/**
	 * 点的纵坐标
	 */
	public double y;

	private Point2D() {
		this(0, 0);
	}

	private Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void reset(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void copy(Point2D p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		this.x = p.x;
		this.y = p.y;
	}

	/**
	 * 将点移动到(x,y)处
	 * 
	 * @param x
	 * @param y
	 */
	public void moveTo(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 将点移动(xOffset, yOffset)指定的偏移量。
	 * 
	 * @param xOffset
	 * @param yOffset
	 */
	public void moveBy(double xOffset, double yOffset) {
		moveTo(x + xOffset, y + yOffset);
	}

	// //////////////////////////////////////////////////////////////////

	private static Point2D retrieve() {
		Point2D p = ObjectPool.retrieve(Point2D.class);
		if (null == p) {
			p = new Point2D();
		}

		return p;
	}

	public static Point2D create() {
		return create(0, 0);
	}

	public static Point2D create(double x, double y) {
		Point2D p = retrieve();
		p.reset(x, y);

		return p;
	}

	public static Point2D create(Point2D p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		return create(p.x, p.y);
	}

	public static void dispose(Point2D p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		ObjectPool.put(p);
	}

}
