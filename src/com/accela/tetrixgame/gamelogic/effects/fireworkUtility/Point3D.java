package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.util.ObjectPool;

public class Point3D {
	public double x;

	public double y;

	public double z;

	private Point3D() {
		this(0, 0, 0);
	}

	private Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void toPoint2D(Point2D result, Plane plane) {
		if (null == plane) {
			throw new NullPointerException("plane");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		Point3D orign = Point3D.create();
		plane.getOrign(orign);
		Vector3D orignToThis = Vector3D.create(orign, this);
		Vector3D aix = Vector3D.create();

		plane.getXAix(aix);
		result.x = Vector3D.innerProduct(orignToThis, aix);
		plane.getYAix(aix);
		result.y = Vector3D.innerProduct(orignToThis, aix);

		Point3D.dispose(orign);
		Vector3D.dispose(orignToThis);
		Vector3D.dispose(aix);
	}

	public void reset(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void copy(Point3D p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		this.x = p.x;
		this.y = p.y;
		this.z = p.z;

	}

	public void moveTo(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void moveBy(double xOffset, double yOffset, double zOffset) {
		moveTo(x + xOffset, y + yOffset, z + zOffset);
	}

	public void moveBy(Vector3D v) {
		if (null == v) {
			throw new NullPointerException("v should not be null");
		}

		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
	}

	// ////////////////////////////////////////////////////////////////////

	private static Point3D retrieve() {
		Point3D p = ObjectPool.retrieve(Point3D.class);
		if (null == p) {
			p = new Point3D();
		}

		return p;
	}

	public static Point3D create() {
		return create(0, 0, 0);
	}

	public static Point3D create(double x, double y, double z) {
		Point3D p = retrieve();
		p.reset(x, y, z);

		return p;
	}

	public static Point3D create(Point3D p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		return create(p.x, p.y, p.z);
	}

	public static void dispose(Point3D p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}

		ObjectPool.put(p);
	}

}
