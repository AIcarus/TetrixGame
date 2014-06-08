package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.util.ObjectPool;

public class Vector2D {
	public double x;

	public double y;

	private Vector2D() {
		this(0, 0);
	}

	private Vector2D(double x, double y) {
		reset(x, y);
	}

	public void copy(Vector2D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		reset(v.x, v.y);
	}

	public double getAngleFromXToThis() {
		if (x > 0) {
			return Math.atan(y / x);
		} else if (x < 0) {
			return Math.PI + Math.atan(y / x);
		} else {
			if (y > 0) {
				return Math.PI / 2;
			} else if (y < 0) {
				return -Math.PI / 2;
			} else {
				return 0;
			}
		}

	}

	public void reset(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getLength() {
		return Math.pow(x * x + y * y, 0.5);
	}

	public void toUnitVector(Vector2D result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		double length = getLength();

		result.x = this.x / length;
		result.y = this.y / length;
	}

	// 是否垂直
	public boolean isPerpendicular(Vector2D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		return Math.abs(innerProduct(this, v)) < 1e-6;
	}

	// 是否平行
	public boolean isParallel(Vector2D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		double check1 = x * v.y - y * v.x;

		if ((Math.abs(check1) < 1e-6)) {
			return true;
		} else {
			return false;
		}
	}

	public static void mutiply(Vector2D result, Vector2D v, double factor) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		result.x = v.x * factor;
		result.y = v.y * factor;
	}

	public static void addition(Vector2D result, Vector2D v1, Vector2D v2) {
		if (null == v1) {
			throw new NullPointerException("v1 should not be null");
		}
		if (null == v2) {
			throw new NullPointerException("v2 should not be null");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.x = v1.x + v2.x;
		result.y = v1.y + v2.y;
	}

	/**
	 * 向量v2-v1
	 */
	public static void subtract(Vector2D result, Vector2D v1, Vector2D v2) {
		if (null == v1) {
			throw new NullPointerException("v1 should not be null");
		}
		if (null == v2) {
			throw new NullPointerException("v2 should not be null");
		}
		if (null == result) {
			throw new NullPointerException("result shoud not be null");
		}

		result.x = v2.x - v1.x;
		result.y = v2.y = v1.y;
	}

	public static double innerProduct(Vector2D v1, Vector2D v2) {
		if (null == v1) {
			throw new NullPointerException("v1 should not be null");
		}
		if (null == v2) {
			throw new NullPointerException("v2 should not be null");
		}

		return v1.x * v2.x + v1.y * v2.y;
	}

	// ///////////////////////////////////////////////////////////////////////

	private static Vector2D retrieve() {
		Vector2D v = ObjectPool.retrieve(Vector2D.class);
		if (null == v) {
			v = new Vector2D();
		}

		return v;
	}

	public static Vector2D create() {
		return create(0, 0);
	}

	public static Vector2D create(double x, double y) {
		Vector2D v = retrieve();
		v.reset(x, y);

		return v;
	}

	public static Vector2D create(Vector2D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		return create(v.x, v.y);
	}

	public static Vector2D create(Vector2D a, Vector2D b) {
		return create(b.x - a.x, b.y - a.y);
	}

	public static Vector2D create(Point2D p1, Point2D p2) {
		if (null == p1) {
			throw new NullPointerException("p1 should not be null");
		}
		if (null == p2) {
			throw new NullPointerException("p2 should not be null");
		}

		return create(p2.x - p1.x, p2.y - p1.y);
	}

	public static void dispose(Vector2D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		ObjectPool.put(v);
	}
}
