package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;

/**
 * 
 * 因为烟花效果是基于3D坐标空间的，因此继承AbstractEffect来为烟花效果 提供支持
 * 
 */
public abstract class AbstractEffect3D extends AbstractEffect {
	private static Plane plane;

	public AbstractEffect3D() {
		Point3D orign = Point3D.create(0, 0, 0);
		Vector3D xAix = Vector3D.create(1, 0, 0);
		Vector3D yAix = Vector3D.create(0, 1, 0);

		plane = new Plane(orign, xAix, yAix);

		Point3D.dispose(orign);
		Vector3D.dispose(xAix);
		Vector3D.dispose(yAix);
	}

	@Override
	protected final void paintImpl(Graphics g, Point center, double scale) {
		paintImpl(g, plane, center, scale);
	}

	protected abstract void paintImpl(final Graphics g, final Plane plane,
			final Point center, double scale);

}
