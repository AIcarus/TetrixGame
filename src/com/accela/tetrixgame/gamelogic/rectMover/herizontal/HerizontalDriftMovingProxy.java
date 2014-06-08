package com.accela.tetrixgame.gamelogic.rectMover.herizontal;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 水平方向自由滑动的移动代理。 这个移动代理负责水平方向的在不是控制的情况下的自由滑动。 方块在滑动的时候，如果距离下一个网格较远，就会继续加速，
 * 如果距离下一个网格较近，就会减速。如果经过一个网格，那么 就会自动吸附并停靠在其上。
 * 
 */
public class HerizontalDriftMovingProxy
{
	private static enum DriftState
	{
		STEADY_STATE, LEFT_ACCELERATE_STATE, RIGHT_ACCELERATE_STATE, SLOW_DOWN_STATE,
	}

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
		if (!rect.isProportionalToGrid(grid))
		{
			throw new IllegalArgumentException(
					"rect's width and height should be proportional to grid");
		}

		DriftState state = calculateDriftState(rect, grid, vel);
		if (DriftState.STEADY_STATE == state)
		{
			performSteadyStateDrift(rect, grid, vel, interpolation);
		} else if (DriftState.LEFT_ACCELERATE_STATE == state)
		{
			performLeftAccelerateStateDrift(rect, grid, vel, interpolation);
		} else if (DriftState.RIGHT_ACCELERATE_STATE == state)
		{
			performRightAccelerateStateDrift(rect, grid, vel, interpolation);
		} else if (DriftState.SLOW_DOWN_STATE == state)
		{
			performSlowDownStateDrift(rect, grid, vel, interpolation);
		} else
		{
			assert (false);
			throw new IllegalStateException(
					"this statement should never be reached");
		}

	}

	public void reset()
	{
		this.hasEnterSlowDownState = false;
		this.remainedMoveDistance = 0;
	}

	private boolean hasEnterSlowDownState = false;

	private DriftState calculateDriftState(Rectangle rect, Grid grid,
			HerizontalVelocityProxy vel)
	{
		if (grid.isOnVerticalGridLine(rect))
		{
			return DriftState.STEADY_STATE;
		}

		if (HerizontalVelocityProxy.VelocityDirection.NONE == vel.getVelocityDirection())
		{
			hasEnterSlowDownState = false;
			Point lu = Point.create();
			rect.getPoint(lu);
			final int xInGrid = grid.xCoordinateInGrid(lu);
			Point.dispose(lu);
			if (xInGrid <= grid.getGridWidth() / 2)
			{
				return DriftState.LEFT_ACCELERATE_STATE;
			} else
			{
				return DriftState.RIGHT_ACCELERATE_STATE;
			}
		} else if (hasEnterSlowDownState)
		{
			return DriftState.SLOW_DOWN_STATE;
		} else if (HerizontalVelocityProxy.VelocityDirection.LEFT == vel.getVelocityDirection())
		{
			Point lu = Point.create();
			rect.getPoint(lu);
			final int xInGrid = grid.xCoordinateInGrid(lu);
			Point.dispose(lu);
			final double driftDistance = vel.currentDriftDistance();

			if (xInGrid <= driftDistance)
			{
				hasEnterSlowDownState = true;
				return DriftState.SLOW_DOWN_STATE;
			} else
			{
				return DriftState.LEFT_ACCELERATE_STATE;
			}
		} else if (HerizontalVelocityProxy.VelocityDirection.RIGHT == vel.getVelocityDirection())
		{
			Point lu = Point.create();
			rect.getPoint(lu);
			final int xInGrid = grid.xCoordinateInGrid(lu);
			Point.dispose(lu);
			final double driftDistance = vel.currentDriftDistance();

			if (grid.getGridWidth() - xInGrid <= driftDistance)
			{
				hasEnterSlowDownState = true;
				return DriftState.SLOW_DOWN_STATE;
			} else
			{
				return DriftState.RIGHT_ACCELERATE_STATE;
			}
		} else
		{
			assert (false);
			throw new IllegalStateException(
					"this statement should never be reached!");
		}

	}

	private void performSteadyStateDrift(Rectangle rect, Grid grid,
			HerizontalVelocityProxy vel, long interpolation)
	{
		assert (grid.isOnVerticalGridLine(rect));
		vel.reset();
	}

	private void performLeftAccelerateStateDrift(Rectangle rect, Grid grid,
			HerizontalVelocityProxy vel, long interpolation)
	{
		vel.update(HerizontalStatus.LEFT, interpolation);
		performMovement(rect, grid, vel, interpolation);
	}

	private void performRightAccelerateStateDrift(Rectangle rect, Grid grid,
			HerizontalVelocityProxy vel, long interpolation)
	{
		vel.update(HerizontalStatus.RIGHT, interpolation);
		performMovement(rect, grid, vel, interpolation);
	}

	private void performSlowDownStateDrift(Rectangle rect, Grid grid,
			HerizontalVelocityProxy vel, long interpolation)
	{
		vel.update(HerizontalStatus.DRIFT, interpolation);
		performMovement(rect, grid, vel, interpolation);
	}

	private double remainedMoveDistance = 0;

	private void performMovement(Rectangle rect, Grid grid,
			HerizontalVelocityProxy vel, long interpolation)
	{
		double moveDistance = vel.getVelocity() * interpolation
				+ remainedMoveDistance;
		int actualMoveDistance = (int) Math.round(moveDistance);
		remainedMoveDistance = moveDistance - actualMoveDistance;

		if (rect.isInSameGridIfMoveBy(actualMoveDistance, 0, grid))
		{
			rect.moveBy(actualMoveDistance, 0);
		} else
		{
			if (actualMoveDistance > 0)
			{
				Point lu = Point.create();
				rect.getPoint(lu);
				final int xInGrid = grid.xCoordinateInGrid(lu);
				Point.dispose(lu);
				rect.moveBy(grid.getGridWidth() - xInGrid, 0);

				assert (grid.isOnVerticalGridLine(rect));
			} else if (actualMoveDistance < 0)
			{
				Point lu = Point.create();
				rect.getPoint(lu);
				final int xInGrid = grid.xCoordinateInGrid(lu);
				Point.dispose(lu);
				rect.moveBy(-xInGrid, 0);

				assert (grid.isOnVerticalGridLine(rect));
			} else
			{
				assert (false);
				throw new IllegalStateException(
						"this statement should neven be reached");
			}

		}

	}

}
