package com.accela.tetrixgame.gamelogic.rectMover.scrolling;

import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 处理一个方块的翻转动作的移动代理。
 * 
 */
public class ScrollProxy
{
	private UniformSpeedShiftProxy upShift;

	private RoundTurnProxy turn;

	private DecShiftProxy downShift;

	private boolean hasFinished = false;

	@SuppressWarnings("unused")
	private ScrollProxy()
	{

	}

	public ScrollProxy(double upInitS, double upTotalS, double upStillS,
			double maxVel, double roundRadius, double roundInitAngle,
			double downTotalS, boolean herizontalReverse)
	{
		upShift = new UniformSpeedShiftProxy(upInitS, upTotalS, upStillS,
				maxVel, !herizontalReverse);

		if (!herizontalReverse)
		{
			turn = new RoundTurnProxy(maxVel, roundRadius, roundInitAngle,
					normalize(-2 * roundInitAngle), true);
		} else
		{
			turn = new RoundTurnProxy(maxVel, roundRadius, roundInitAngle,
					normalize(2 * roundInitAngle), false);
		}

		downShift = new DecShiftProxy(maxVel, 0, downTotalS, herizontalReverse);
	}

	private double normalize(double angle)
	{
		while (angle >= 2 * Math.PI)
		{
			angle -= 2 * Math.PI;
		}
		while (angle < 0)
		{
			angle += 2 * Math.PI;
		}

		return angle;
	}

	public void reset()
	{
		upShift.reset();
		turn.reset();
		downShift.reset();
		hasFinished = false;
	}

	public void update(Rectangle rect, long interpolation)
	{
		if (null == rect)
		{
			throw new NullPointerException("rect should not be null");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should not be negtive");
		}

		if (!upShift.hasFinished())
		{
			upShift.update(rect, interpolation);
		}

		if (upShift.hasFinished() && !turn.hasFinished())
		{
			turn.update(rect, interpolation);
		}

		if (upShift.hasFinished() && turn.hasFinished()
				&& !downShift.hasFinished())
		{
			downShift.update(rect, interpolation);
		}

		if (upShift.hasFinished() && turn.hasFinished()
				&& downShift.hasFinished())
		{
			hasFinished = true;
		}

	}

	public boolean hasFinished()
	{
		return hasFinished;
	}

	public boolean hasHalfFinished()
	{
		if (hasFinished())
		{
			return true;
		} else
		{
			if (turn.hasFinished())
			{
				return true;
			} else
			{
				return false;
			}
		}

	}

}
