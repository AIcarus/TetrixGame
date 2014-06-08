package com.accela.tetrixgame.gamelogic.rectMover.vertical;

import com.accela.tetrixgame.gamelogic.control.VerticalStatus;

/**
 * 
 * 竖直方向的速度代理。 这个代理控制下的速度可以保持在一个正常速度和一个高速度上。 两者之间的过度使用正弦曲线的速度变化。
 * 
 */
public class VerticalVelocityProxy
{
	private long curAccTimeCount;

	private long maxAccTime;

	private double maxVelocity;

	private double normalVelocity;

	@SuppressWarnings("unused")
	private VerticalVelocityProxy()
	{

	}

	public VerticalVelocityProxy(long maxAccTime, double normalVelocity,
			double maxVelocity)
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

		this.maxAccTime = maxAccTime;
		this.maxVelocity = maxVelocity;
		this.normalVelocity = normalVelocity;

		reset();
	}

	public void reset()
	{
		curAccTimeCount = 0;
	}

	public void update(VerticalStatus status, long interpolation)
	{
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}
		assert (curAccTimeCount >= 0);

		if (VerticalStatus.DOWN_ACC == status)
		{
			curAccTimeCount += interpolation;
			if (curAccTimeCount > maxAccTime)
			{
				curAccTimeCount = maxAccTime;
			}
		} else if (VerticalStatus.DRIFT == status)
		{
			if (curAccTimeCount > interpolation)
			{
				curAccTimeCount -= interpolation;
			} else if (curAccTimeCount >= 0 && curAccTimeCount <= interpolation)
			{
				curAccTimeCount = 0;
			} else
			{
				assert (false);
			}

		} else
		{
			throw new IllegalArgumentException("illegal parameter status");
		}
	}

	public double getVelocity()
	{
		return normalVelocity + (maxVelocity - normalVelocity)
				* Math.sin((double) curAccTimeCount / maxAccTime * Math.PI / 2);
	}

	public long getMaxAccTime()
	{
		return maxAccTime;
	}

	public double getMaxVelocity()
	{
		return maxVelocity;
	}

	public double getNormalVelocity()
	{
		return normalVelocity;
	}

}
