package com.accela.tetrixgame.gamelogic.rectMover.vertical;

import java.util.Collection;

import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 竖直方向的方块移动代理。这个类负责方块在竖直方向上的速度变化、移动和碰撞。 竖直方向上方块有三种状态，正常下落、告诉高速下落和两者之间的过度状态。过度
 * 状态中的速度变化使用正弦曲线。
 * 
 */
public class VerticalCollisionMovingProxy
{
	/**
	 * 竖直方向移动代理
	 */
	private VerticalMovingProxy proxy;

	@SuppressWarnings("unused")
	private VerticalCollisionMovingProxy()
	{

	}

	/**
	 * 新建一个方块的竖直移动代理
	 * 
	 * @param maxAccTime
	 *            最大加速时间
	 * @param normalVelocity
	 *            正常下落速度
	 * @param maxVelocity
	 *            高速下落速度
	 */
	public VerticalCollisionMovingProxy(long maxAccTime, double normalVelocity,
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

		this.proxy = new VerticalMovingProxy(maxAccTime, normalVelocity,
				maxVelocity, gravity);
	}

	/**
	 * 每个时钟周期的更新
	 * 
	 * @param status
	 *            竖直方向的移动状态
	 * @param block
	 *            被移动的方块对象
	 * @param blockContainer
	 *            方块的容器
	 * @param interpolation
	 *            这次调用和上次调用的时间差值
	 * @return 是否发生了碰撞
	 */
	public boolean update(VerticalStatus status, Rectangle rect,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int downBound, long interpolation)
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
		if (downBound <= 0)
		{
			throw new IllegalArgumentException("downBound should not be null");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		// 备份block过去的位置
		Rectangle copyRect = Rectangle.create(rect);

		// 移动block
		if (VerticalStatus.DRIFT == status)
		{
			proxy.update(VerticalStatus.DRIFT, copyRect, grid, interpolation);
		} else if (VerticalStatus.DOWN_ACC == status)
		{
			proxy.update(VerticalStatus.DOWN_ACC, copyRect, grid, interpolation);
		} else
		{
			throw new IllegalArgumentException("illegal status");
		}
		assert (copyRect.x == rect.x);

		// 考虑碰撞，计算移动距离
		int downMost = downBound - rect.getHeight();
		for (Rectangle element : otherRects)
		{
			if (null == element || element == rect)
			{
				continue;
			}
			assert (rect.y + rect.getHeight() <= downBound);

			if (Rectangle.isAOverlapWithVerticalExtensionOfB(element, rect))
			{
				if (rect.isAbove(element))
				{
					downMost = Math.min(downMost, element.y - rect.getHeight());
				}
			}
		}

		int moveDis = copyRect.y - rect.y;
		moveDis = Math.min(moveDis, downMost - rect.y);
		boolean hasCollision = moveDis != copyRect.y - rect.y;

		// 实施移动
		rect.moveBy(0, moveDis);

		Rectangle.dispose(copyRect);
		return hasCollision;

	}

	/**
	 * 将竖直方向移动代理重置为新建状态
	 */
	public void reset()
	{
		proxy.reset();
	}

	public void switchToGravity(double initVel)
	{
		proxy.switchToGravity(initVel);
	}

	public void switchToNormal()
	{
		proxy.switchToNormal();
	}

	public double getVelocity()
	{
		return proxy.getVelocity();
	}

}
