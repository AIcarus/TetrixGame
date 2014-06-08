package com.accela.tetrixgame.gamelogic.rectMover.scrolling;

import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 水平减速运动的移动代理
 * 
 */
public class DecShiftProxy
{
	private double initVel;

	private double totalS;

	private boolean movingRight;

	private double curS = 0;

	private double acc;

	private boolean hasFinished = false;

	private double remainedMoveDistance = 0;

	@SuppressWarnings("unused")
	private DecShiftProxy()
	{

	}

	public DecShiftProxy(double initVel, double endVel, double totalS,
			boolean movingRight)
	{
		if (initVel <= 0)
		{
			throw new IllegalArgumentException("initVel should be positive");
		}
		if (endVel < 0)
		{
			throw new IllegalArgumentException("endVel should be non negtive");
		}
		if (initVel < endVel)
		{
			throw new IllegalArgumentException(
					"initVel should be greater than endVel");
		}
		if (totalS <= 0)
		{
			throw new IllegalArgumentException("totalS should be positive");
		}

		this.initVel = initVel;
		this.totalS = totalS;
		this.movingRight = movingRight;
		this.curS = 0;
		this.acc = (initVel * initVel - endVel * endVel) / (2 * totalS);
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

		double dertS = getVelocity() * interpolation;
		dertS = Math.min(dertS, totalS - curS);
		if (curS >= totalS)
		{
			hasFinished = true;
			dertS = 0;
		}
		curS += dertS;

		int actualMoveDistance = (int) Math.round(dertS + remainedMoveDistance);
		remainedMoveDistance = (dertS + remainedMoveDistance)
				- actualMoveDistance;

		if (movingRight)
		{
			rect.moveBy(actualMoveDistance, 0);
		} else
		{
			rect.moveBy(-actualMoveDistance, 0);
		}

	}

	public void reset()
	{
		curS = 0;
		hasFinished = false;
		remainedMoveDistance = 0;
	}

	public double getVelocity()
	{
		double vSquare = initVel * initVel - 2 * acc * curS;
		if (vSquare >= 0)
		{
			return Math.pow(vSquare, 0.5);
		} else
		{
			return 0;
		}
	}

	public boolean hasFinished()
	{
		return hasFinished;
	}

}
