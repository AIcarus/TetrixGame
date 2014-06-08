package com.accela.tetrixgame.gamelogic.rectMover.herizontal;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 水平方向的左移代理。这个代理能负责方块在受控状态下 的向左移动。 方块向左移动的时候，使用正弦速度方式来加速，到达最大 速度时候，就停止加速。
 * 
 */
public class HerizontalLeftMovingProxy
{
	private double remainedMoveDistance = 0;

	public void update(Rectangle rect, Grid grid, HerizontalVelocityProxy vel,
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
		if (null == vel)
		{
			throw new NullPointerException("vel should not be null");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		vel.update(HerizontalStatus.LEFT, interpolation);

		double moveDistance = vel.getVelocity() * interpolation
				+ remainedMoveDistance;
		int actualMoveDistance = (int) Math.round(moveDistance);
		remainedMoveDistance = moveDistance - actualMoveDistance;

		rect.moveBy(actualMoveDistance, 0);

	}

	public void reset()
	{
		remainedMoveDistance = 0;
	}

}
