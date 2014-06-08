package com.accela.tetrixgame.gamelogic.rectMover.herizontal;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 水平方向的移动代理，根据方块的移动状态，左移、右移和自由滑动， 处理方块的移动和网格吸附。
 * 
 * 其下属的HerizontalDriftMovingProxy、HerizontalLeftMovingProxy、
 * 和HerizontalRightMovingProxy分别负责左移、右移和自由滑动。
 * 
 */
public class HerizontalMovingProxy
{
	private HerizontalVelocityProxy vel;

	private HerizontalLeftMovingProxy leftMove;

	private HerizontalRightMovingProxy rightMove;

	private HerizontalDriftMovingProxy driftMove;

	@SuppressWarnings("unused")
	private HerizontalMovingProxy()
	{

	}

	public HerizontalMovingProxy(long maxAccTime, double maxVelocity)
	{
		if (maxAccTime <= 0)
		{
			throw new IllegalArgumentException("maxAccTime should be positive");
		}
		if (maxVelocity < 0)
		{
			throw new IllegalArgumentException(
					"maxVelocity should be nonnegtive");
		}

		vel = new HerizontalVelocityProxy(maxAccTime, maxVelocity);
		leftMove = new HerizontalLeftMovingProxy();
		rightMove = new HerizontalRightMovingProxy();
		driftMove = new HerizontalDriftMovingProxy();
	}

	public void update(HerizontalStatus status, Rectangle rect, Grid grid,
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

		if (HerizontalStatus.LEFT == status)
		{
			leftMove.update(rect, grid, vel, interpolation);
			rightMove.reset();
			driftMove.reset();
		} else if (HerizontalStatus.RIGHT == status)
		{
			rightMove.update(rect, grid, vel, interpolation);
			leftMove.reset();
			driftMove.reset();
		} else if (HerizontalStatus.DRIFT == status)
		{
			driftMove.update(rect, grid, vel, interpolation);
			leftMove.reset();
			rightMove.reset();
		} else
		{
			assert (false);
			throw new IllegalStateException(
					"this statement should never be reached");
		}
	}

	public double getVelocity()
	{
		return vel.getVelocity();
	}

	public void reset()
	{
		vel.reset();
		leftMove.reset();
		rightMove.reset();
		driftMove.reset();
	}

}
