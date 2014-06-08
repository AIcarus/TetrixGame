package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

public class Plane {
	/**
	 * 该平面的原点
	 */
	private Point3D point = Point3D.create(); // 原点
	/**
	 * 该平面的x轴单位向量
	 */
	private Vector3D xAix = Vector3D.create(); // x轴的单位向量
	/**
	 * 该平面的y轴单位向量
	 */
	private Vector3D yAix = Vector3D.create(); // y轴的单位向量

	private void construct(Point3D point, Vector3D xAix, Vector3D yAix) {
		assert (point != null);
		assert (xAix != null);
		assert (yAix != null);

		this.point.copy(point);
		xAix.toUnitVector(this.xAix);
		yAix.toUnitVector(this.yAix);
	}

	public Plane(Point3D point, Vector3D xAix, Vector3D yAix) {
		if (null == point) {
			throw new NullPointerException("point should not be null");
		}
		if (null == xAix) {
			throw new NullPointerException("xAix should not be null");
		}
		if (null == yAix) {
			throw new NullPointerException("yAix should not be null");
		}

		construct(point, xAix, yAix);
	}

	public void getNormalVector(Vector3D result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		Vector3D.outerProduct(result, xAix, yAix);
	}

	public void getOrign(Point3D result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.copy(point);
	}

	public double getX() {
		return point.x;
	}

	public double getY() {
		return point.y;
	}

	public void setOrign(Point3D point) {
		if (null == point) {
			throw new NullPointerException("point should not be null");
		}

		this.point.copy(point);
	}

	public void getXAix(Vector3D result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.copy(xAix);
	}

	public void setXAix(Vector3D aix) {
		if (null == aix) {
			throw new NullPointerException("aix should not be null");
		}

		aix.toUnitVector(this.xAix);
	}

	public void getYAix(Vector3D result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.copy(yAix);
	}

	public void setYAix(Vector3D aix) {
		if (null == aix) {
			throw new NullPointerException("aix should not be null");
		}

		aix.toUnitVector(this.yAix);
	}

	public void moveTo(double x, double y, double z) {
		this.point.x = x;
		this.point.y = y;
		this.point.z = z;
	}

	public void moveTo(Point3D point) {
		if (null == point) {
			throw new NullPointerException("point should not be null");
		}

		this.point.copy(point);
	}

}
