package com.accela.tetrixgame.gamelogic.geometry;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.util.ObjectPool;

/**
 * 
 * 点
 * 
 */
public class Point {
	/**
	 * 点的横坐标
	 */
	public int x;
	/**
	 * 点的纵坐标
	 */
	public int y;

	private Point() {
		this(0, 0);
	}

	private Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void reset(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void copy(Point p) {
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
	public void moveTo(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 将点移动(xOffset, yOffset)指定的偏移量。
	 * 
	 * @param xOffset
	 * @param yOffset
	 */
	public void moveBy(int xOffset, int yOffset) {
		moveTo(x + xOffset, y + yOffset);
	}

	// //////////////////////////////////////////////////////////////////

	private static Point retrieve() {
		Point p = ObjectPool.retrieve(Point.class);
		if (null == p) {
			p = new Point();
		}

		return p;
	}

	public static Point create() {
		return create(0, 0);
	}

	public static Point create(int x, int y) {
		Point p = retrieve();
		p.reset(x, y);

		return p;
	}

	public static Point create(Point p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		return create(p.x, p.y);
	}

	public static void dispose(Point p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		ObjectPool.put(p);
	}
}
