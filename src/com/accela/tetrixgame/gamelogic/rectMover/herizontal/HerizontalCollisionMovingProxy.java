package com.accela.tetrixgame.gamelogic.rectMover.herizontal;

import java.util.*;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 方块的水平移动代理。 水平移动代理负责将方块按照正弦曲线速度运动，并且自动吸附网格，以及碰撞检测。
 * 
 */
public class HerizontalCollisionMovingProxy
{
	private HerizontalMovingProxy proxy;

	@SuppressWarnings("unused")
	private HerizontalCollisionMovingProxy()
	{
	}

	/**
	 * 新建一个水平移动代理对象
	 * 
	 * @param maxAccTime
	 *            最大加速时间
	 * @param maxVelocity
	 *            最大速度
	 */
	public HerizontalCollisionMovingProxy(long maxAccTime, double maxVelocity)
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

		this.proxy = new HerizontalMovingProxy(maxAccTime, maxVelocity);

	}

	/**
	 * 每个时钟周期调用此更新函数，更新方块的速度和位置
	 * 
	 * @param status
	 *            此时方块的移动状态
	 * @param block
	 *            被移动的方块对象
	 * @param blockContainer
	 *            方块容器
	 * @param interpolation
	 *            这次调用和上一次调用的时间差值
	 * @return
	 */
	public boolean update(HerizontalStatus status, Rectangle rect,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int rightBound, long interpolation)
	{
		// 检查传入参数
		if (null == rect)
		{
			throw new NullPointerException("block should not be null or empty");
		}
		if (null == otherRects)
		{
			throw new NullPointerException("blockContainer should not be null");
		}
		if (null == grid)
		{
			throw new NullPointerException("grid should not be null");
		}
		if (!rect.isProportionalToGrid(grid))
		{
			throw new IllegalArgumentException(
					"rect's width and height should be proportional to grid");
		}
		if (rightBound <= 0)
		{
			throw new IllegalArgumentException("rightBound should not be null");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		// 重新创建一个Rectangle，不直接移动传入参数的rect
		Rectangle copyRect = Rectangle.create(rect);

		// 移动block
		if (HerizontalStatus.LEFT == status)
		{
			proxy.update(HerizontalStatus.LEFT, copyRect, grid, interpolation);
		} else if (HerizontalStatus.RIGHT == status)
		{
			proxy.update(HerizontalStatus.RIGHT, copyRect, grid, interpolation);
		} else if (HerizontalStatus.DRIFT == status)
		{
			proxy.update(HerizontalStatus.DRIFT, copyRect, grid, interpolation);
		} else
		{
			throw new IllegalArgumentException("illegal status");
		}
		assert (copyRect.y == rect.y);

		// 考虑碰撞，计算移动距离
		int leftMost = 0;
		int rightMost = rightBound - rect.getWidth();
		for (Rectangle element : otherRects)
		{
			if (null == element || element == rect)
			{
				continue;
			}
			assert (rect.x + rect.getWidth() <= rightBound);

			if (Rectangle.isAOverlapWithHerizontalExtensionOfB(element, rect))
			{
				if (element.isLeftOf(rect))
				{
					leftMost = Math.max(leftMost, element.x
							+ element.getWidth());
				}
				if (element.isRightOf(rect))
				{
					rightMost = Math.min(rightMost, element.x
							- element.getWidth());
				}
			}
		}

		int moveDis = copyRect.x - rect.x;
		moveDis = Math.min(moveDis, rightMost - rect.x);
		moveDis = Math.max(moveDis, leftMost - rect.x);
		boolean hasCollision = moveDis != copyRect.x - rect.x;

		// 实施移动
		rect.moveBy(moveDis, 0);

		Rectangle.dispose(copyRect);
		return hasCollision;

	}

	/**
	 * 重置水平移动代理，回复到新建时的状态
	 */
	public void reset()
	{
		proxy.reset();
	}

	public double getVelocity()
	{
		return proxy.getVelocity();
	}

}
