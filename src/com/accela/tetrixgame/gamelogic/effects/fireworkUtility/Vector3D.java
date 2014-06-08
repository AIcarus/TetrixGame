package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.util.ObjectPool;

public class Vector3D {
	public double x;

	public double y;

	public double z;

	private Vector3D() {
		this(0, 0, 0);
	}

	private Vector3D(double x, double y, double z) {
		reset(x, y, z);
	}

	public void toVector2D(Vector2D result, Plane plane) {
		if (null == plane) {
			throw new NullPointerException("plane");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		Vector3D temp = Vector3D.create();

		plane.getXAix(temp);
		result.x = innerProduct(this, temp);

		plane.getYAix(temp);
		result.y = innerProduct(this, temp);

		Vector3D.dispose(temp);
	}

	public void copy(Vector3D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		reset(v.x, v.y, v.z);
	}

	public double getLength() {
		return Math.pow(x * x + y * y + z * z, 0.5);
	}

	public void reset(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void toUnitVector(Vector3D result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		double length = getLength();

		result.x = this.x / length;
		result.y = this.y / length;
		result.z = this.z / length;
	}

	// 是否垂直
	public boolean isPerpendicular(Vector3D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		return Math.abs(innerProduct(this, v)) < 1e-6;
	}

	// 是否平行
	public boolean isParallel(Vector3D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		double check1 = x * v.y - y * v.x;
		double check2 = x * v.z - z * v.x;
		double check3 = z * v.y - y * v.z;

		if ((Math.abs(check1) < 1e-6) && (Math.abs(check2) < 1e-6)
				&& (Math.abs(check3) < 1e-6)) {
			return true;
		} else {
			return false;
		}
	}

	public static void addition(Vector3D result, Vector3D v1, Vector3D v2) {
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
		result.z = v1.z + v2.z;
	}

	/**
	 * 向量v2-v1
	 */
	public static void subtract(Vector3D result, Vector3D v1, Vector3D v2) {
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
		result.z = v2.z - v1.z;
	}

	public static void mutiply(Vector3D result, Vector3D v, double factor) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		result.x = v.x * factor;
		result.y = v.y * factor;
		result.z = v.z * factor;
	}

	public static double innerProduct(Vector3D v1, Vector3D v2) {
		if (null == v1) {
			throw new NullPointerException("v1 should not be null");
		}
		if (null == v2) {
			throw new NullPointerException("v2 should not be null");
		}

		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	public static void outerProduct(Vector3D result, Vector3D v1, Vector3D v2) {
		if (null == v1) {
			throw new NullPointerException("v1 should not be null");
		}
		if (null == v2) {
			throw new NullPointerException("v2 should not be null");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.x = v1.y * v2.z - v2.y * v1.z;
		result.y = v2.x * v1.z - v1.x * v2.z;
		result.z = v1.x * v2.y - v2.x * v1.y;
	}

	// ///////////////////////////////////////////////////////////////////////

	private static Vector3D retrieve() {
		Vector3D v = ObjectPool.retrieve(Vector3D.class);
		if (null == v) {
			v = new Vector3D();
		}

		return v;
	}

	public static Vector3D create() {
		return create(0, 0, 0);
	}

	public static Vector3D create(double x, double y, double z) {
		Vector3D v = retrieve();
		v.reset(x, y, z);

		return v;
	}

	public static Vector3D create(Vector3D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		return create(v.x, v.y, v.z);
	}

	public static Vector3D create(Vector3D a, Vector3D b) {
		return create(b.x - a.x, b.y - a.y, b.z - a.z);
	}

	public static Vector3D create(Point3D p1, Point3D p2) {
		if (null == p1) {
			throw new NullPointerException("p1 should not be null");
		}
		if (null == p2) {
			throw new NullPointerException("p2 should not be null");
		}

		return create(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
	}

	public static void dispose(Vector3D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		ObjectPool.put(v);
	}
}
