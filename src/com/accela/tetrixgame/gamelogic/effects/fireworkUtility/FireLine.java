package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * 
 * 一条烟花线，即烟花效果中，满天飞的亮线中的一条
 * 
 */
public class FireLine extends AbstractEffect3D {
	private Vector3D vel = Vector3D.create();

	private Point3D headPos = Point3D.create();

	private double gravity;

	private int length;

	private Color mainColor;

	private Color subColor;

	private int rightBound;

	private int downBound;

	private boolean exploded = false;

	private boolean velChangedSign = false;

	private FireLineBall ball = new FireLineBall();

	public FireLine(Vector3D vel, Point3D headPos, double gravity, int length,
			Color mainColor, Color subColor, int rightBound, int downBound) {
		initialize(vel, headPos, gravity, length, mainColor, subColor,
				rightBound, downBound);
	}

	public void initialize(Vector3D vel, Point3D headPos, double gravity,
			int length, Color mainColor, Color subColor, int rightBound,
			int downBound) {
		if (null == vel) {
			throw new NullPointerException("vel should not be null");
		}
		if (null == headPos) {
			throw new NullPointerException("headPos should not be null");
		}
		if (null == mainColor) {
			throw new NullPointerException("mainColor should not be null");
		}
		if (null == subColor) {
			throw new NullPointerException("subColor should not be null");
		}
		if (length < 0) {
			throw new IllegalArgumentException("length should not be negtive");
		}
		if (rightBound <= 0) {
			throw new IllegalArgumentException("rightBound should be positive");
		}
		if (downBound <= 0) {
			throw new IllegalArgumentException("downBound should be positive");
		}

		this.vel.copy(vel);
		this.headPos.copy(headPos);
		this.gravity = gravity;
		this.length = length;
		this.mainColor = mainColor;
		this.subColor = subColor;
		this.rightBound = rightBound;
		this.downBound = downBound;

		ball.reset();
		this.exploded = false;
		this.velChangedSign = false;

		this.setValid(true);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		Vector3D.dispose(vel);
		Point3D.dispose(headPos);
	}

	@Override
	protected void onTickImpl(long interpolation) {
		if (!exploded) {
			final int BACK_OFF = 4;

			if (headPos.x >= rightBound || headPos.y >= downBound) {
				// 这个判断起作用的前提，是FireLine在new Plane(0,0,0)这样的平面上投影作画
				if (headPos.x >= rightBound) {
					headPos.x = rightBound - BACK_OFF;
				}
				if (headPos.y >= downBound) {
					headPos.y = downBound - BACK_OFF;
				}

				this.exploded = true;
				return;
			}

			if (headPos.x <= 0 || headPos.y <= 0) {
				// 这个判断起作用的前提，是FireLine在new Plane(0,0,0)这样的平面上投影作画
				if (headPos.x <= 0) {
					headPos.x = BACK_OFF;
				}
				if (headPos.y <= 0) {
					headPos.y = BACK_OFF;
				}

				this.exploded = true;
				return;
			}

			if (velChangedSign) {
				this.exploded = true;
				return;
			}

			headPos.moveBy(vel.x * interpolation, vel.y * interpolation, vel.z
					* interpolation);
			double velY = vel.y;
			vel.y += gravity * interpolation;
			if (velY * vel.y <= 0 && velY != vel.y) {
				velChangedSign = true;
			}

		} else {
			if (!ball.isValid()) {
				this.setValid(false);
				return;
			}

			ball.onTick(interpolation);
		}
	}

	@Override
	protected void paintImpl(Graphics g, Plane plane, Point center, double scale) {
		if (!exploded) {
			Point2D start = Point2D.create();
			headPos.toPoint2D(start, plane);

			Vector2D startToEnd = Vector2D.create();
			vel.toVector2D(startToEnd, plane);

			double arc = Math.PI + startToEnd.getAngleFromXToThis();

			int length = 0;
			if (vel.getLength() != 0) {
				length = (int) (this.length * (startToEnd.getLength() / vel
						.getLength()));
			} else {
				length = 0;
			}

			g.setColor(mainColor);
			paintLine(g, start, length, arc, true, center, scale);
			g.setColor(subColor);
			paintLine(g, start, 2.0 / 3 * length, arc, true, center, scale);

			if (Math.abs(startToEnd.y) < Math.abs(startToEnd.x)) {
				start.moveBy(0, 2);
				g.setColor(mainColor);
				paintLine(g, start, 2.0 / 3 * length, arc, false, center, scale);
				g.setColor(subColor);
				paintLine(g, start, 1.0 / 3 * 2.0 / 3 * length, arc, false,
						center, scale);

				start.moveBy(0, -4);
				g.setColor(mainColor);
				paintLine(g, start, 2.0 / 3 * length, arc, false, center, scale);
				g.setColor(subColor);
				paintLine(g, start, 1.0 / 3 * 2.0 / 3 * length, arc, false,
						center, scale);
			} else {
				start.moveBy(2, 0);
				g.setColor(mainColor);
				paintLine(g, start, 2.0 / 3 * length, arc, false, center, scale);
				g.setColor(subColor);
				paintLine(g, start, 1.0 / 3 * 2.0 / 3 * length, arc, false,
						center, scale);

				start.moveBy(-4, 0);
				g.setColor(mainColor);
				paintLine(g, start, 2.0 / 3 * length, arc, false, center, scale);
				g.setColor(subColor);
				paintLine(g, start, 1.0 / 3 * 2.0 / 3 * length, arc, false,
						center, scale);
			}

			Point2D.dispose(start);
			Vector2D.dispose(startToEnd);
		} else {
			ball.paint(g, center.x + (int) (headPos.x * scale), center.y
					+ (int) (headPos.y * scale), scale);
		}
	}

	private void paintLine(Graphics g, Point2D start, double length,
			double arc, boolean stroked, Point center, double scale) {
		Point2D startPoint = Point2D.create(start);
		Point2D endPoint = Point2D.create(
				startPoint.x + length * Math.cos(arc), startPoint.y + length
						* Math.sin(arc));

		startPoint.x = center.x + (int) (startPoint.x * scale);
		startPoint.y = center.y + (int) (startPoint.y * scale);

		endPoint.x = center.x + (int) (endPoint.x * scale);
		endPoint.y = center.y + (int) (endPoint.y * scale);

		if (!stroked) {
			g.drawLine((int) startPoint.x, (int) startPoint.y,
					(int) endPoint.x, (int) endPoint.y);

		} else {

			if (Math.abs(Math.sin(arc)) < Math.abs(Math.cos(arc))) {
				g.drawLine((int) startPoint.x, (int) startPoint.y,
						(int) endPoint.x, (int) endPoint.y);

				startPoint.moveBy(0, 1);
				endPoint.moveBy(0, 1);
				g.drawLine((int) startPoint.x, (int) startPoint.y,
						(int) endPoint.x, (int) endPoint.y);

				startPoint.moveBy(0, -2);
				endPoint.moveBy(0, -2);
				g.drawLine((int) startPoint.x, (int) startPoint.y,
						(int) endPoint.x, (int) endPoint.y);

			} else {
				g.drawLine((int) startPoint.x, (int) startPoint.y,
						(int) endPoint.x, (int) endPoint.y);

				startPoint.moveBy(1, 0);
				endPoint.moveBy(1, 0);
				g.drawLine((int) startPoint.x, (int) startPoint.y,
						(int) endPoint.x, (int) endPoint.y);

				startPoint.moveBy(-2, 0);
				endPoint.moveBy(-2, 0);
				g.drawLine((int) startPoint.x, (int) startPoint.y,
						(int) endPoint.x, (int) endPoint.y);

			}
		}

		Point2D.dispose(startPoint);
		Point2D.dispose(endPoint);
	}

}
