package com.accela.tetrixgame.gamelogic.rectMover.herizontal;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;

/**
 * 
 * 水平方向的速度代理。管理速度和速度的变化。 这个速度按照正弦曲线变化，当达到最大值的时候，就会保持 最高速度不再增加。
 * 
 */
public class HerizontalVelocityProxy
{
	private long curAccTimeCount;

	private long maxAccTime;

	private double maxVelocity;

	public HerizontalVelocityProxy(long maxAccTime, double maxVelocity)
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

		this.maxAccTime = maxAccTime;
		this.maxVelocity = maxVelocity;

		reset();

		/*
		 * if(2*calMaxDistanceIfDriftToStop()>=IConstants.BLOCK_SIZE) { throw
		 * new IllegalArgumentException("max distance if drift to stop ("
		 * +calMaxDistanceIfDriftToStop() +") exceeds half of block size"); }
		 */
	}

	public void reset()
	{
		curAccTimeCount = 0;
	}

	public void update(HerizontalStatus status, long interpolation)
	{
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		if (HerizontalStatus.LEFT == status)
		{
			curAccTimeCount -= interpolation;
			if (curAccTimeCount < -maxAccTime)
			{
				curAccTimeCount = -maxAccTime;
			}
		} else if (HerizontalStatus.RIGHT == status)
		{
			curAccTimeCount += interpolation;
			if (curAccTimeCount > maxAccTime)
			{
				curAccTimeCount = maxAccTime;
			}
		} else if (HerizontalStatus.DRIFT == status)
		{
			if (curAccTimeCount > interpolation)
			{
				curAccTimeCount -= interpolation;
			} else if (curAccTimeCount < -interpolation)
			{
				curAccTimeCount += interpolation;
			} else
			{
				curAccTimeCount = 0;
			}

		} else
		{
			assert (false);
			throw new IllegalArgumentException("illegal status");
		}
	}

	public double getVelocity()
	{
		return maxVelocity
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

	public double currentDriftDistance()
	{
		return maxVelocity
				* (double) maxAccTime
				* 2
				/ Math.PI
				* (1 - Math.cos((double) curAccTimeCount / maxAccTime * Math.PI
						/ 2));
	}

	public double maxDriftDistance()
	{
		return maxVelocity * (double) maxAccTime * 2 / Math.PI;
	}

	public static enum VelocityDirection
	{
		LEFT, RIGHT, NONE,
	}

	public VelocityDirection getVelocityDirection()
	{
		if (curAccTimeCount > 0)
		{
			return VelocityDirection.RIGHT;
		} else if (curAccTimeCount < 0)
		{
			return VelocityDirection.LEFT;
		} else
		{
			return VelocityDirection.NONE;
		}

	}

}
