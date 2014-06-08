package com.accela.tetrixgame.gamelogic.rectMover.vertical;

import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 竖直方向的移动代理。这个代理使用VerticalVelocityProxy和
 * VerticalGravityVelocityProxy代理速度，并且能够处理方块 的移动。
 * 
 */
public class VerticalMovingProxy
{
	private VerticalVelocityProxy vel;

	private VerticalGravityVelocityProxy gVel;

	private boolean useGravityVelocityProxy;

	@SuppressWarnings("unused")
	private VerticalMovingProxy()
	{

	}

	public VerticalMovingProxy(long maxAccTime, double normalVelocity,
			double maxVelocity, double gravity)
	{
		if (maxAccTime <= 0)
		{
			throw new IllegalArgumentException("maxAccTime should be positive");
		}
		if (normalVelocity < 0)
		{
			throw new IllegalArgumentException(
					"normalVelocity should be nonnegtive");
		}
		if (maxVelocity < 0)
		{
			throw new IllegalArgumentException(
					"maxVelocity should be nonnegtive");
		}
		if (normalVelocity > maxVelocity)
		{
			throw new IllegalArgumentException(
					"normalVelocity should not be greater than maxVelocity");
		}

		vel = new VerticalVelocityProxy(maxAccTime, normalVelocity, maxVelocity);
		gVel = new VerticalGravityVelocityProxy(0, gravity);
	}

	public void update(VerticalStatus status, Rectangle rect, Grid grid,
			long interpolation)
	{
		if (null == rect)
		{
			throw new NullPointerException("rect should not be null");
		}
		if (null == grid)
		{
			throw new NullPointerException("grid should not be null");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}
		if (!rect.isProportionalToGrid(grid))
		{
			throw new IllegalArgumentException(
					"rect's width and height should be proportional to grid");
		}

		if (useGravityVelocityProxy)
		{
			gVel.update(interpolation);
			performMovementWithGravity(rect, grid, gVel, interpolation);

		} else
		{

			if (VerticalStatus.DRIFT == status)
			{
				vel.update(VerticalStatus.DRIFT, interpolation);
				performMovement(rect, grid, vel, interpolation);
			} else if (VerticalStatus.DOWN_ACC == status)
			{
				vel.update(VerticalStatus.DOWN_ACC, interpolation);
				performMovement(rect, grid, vel, interpolation);
			} else
			{
				throw new IllegalArgumentException("illegal status");
			}
		}

	}

	private double remainedMoveDistance = 0;

	private void performMovement(Rectangle rect, Grid grid,
			VerticalVelocityProxy vel, long interpolation)
	{
		double moveDistance = vel.getVelocity() * interpolation
				+ remainedMoveDistance;
		int actualMoveDistance = (int) Math.round(moveDistance);
		remainedMoveDistance = moveDistance - actualMoveDistance;

		rect.moveBy(0, actualMoveDistance);

	}

	private void performMovementWithGravity(Rectangle rect, Grid grid,
			VerticalGravityVelocityProxy vel, long interpolation)
	{
		double moveDistance = vel.getVelocity() * interpolation
				+ remainedMoveDistance;
		int actualMoveDistance = (int) Math.round(moveDistance);
		remainedMoveDistance = moveDistance - actualMoveDistance;

		rect.moveBy(0, actualMoveDistance);
	}

	public void reset()
	{
		vel.reset();
		gVel.reset(0);
		remainedMoveDistance = 0;
		useGravityVelocityProxy = false;
	}

	public void switchToGravity(double initVel)
	{
		gVel.reset(initVel);
		useGravityVelocityProxy = true;
	}

	public void switchToNormal()
	{
		useGravityVelocityProxy = false;
	}

	public double getVelocity()
	{
		if (!useGravityVelocityProxy)
		{
			return vel.getVelocity();
		} else
		{
			return gVel.getVelocity();
		}
	}

}
