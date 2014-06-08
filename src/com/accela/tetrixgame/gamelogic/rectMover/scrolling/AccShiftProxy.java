package com.accela.tetrixgame.gamelogic.rectMover.scrolling;

import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 水平加速移动的移动代理
 * 
 */
public class AccShiftProxy
{
	private double initS;

	private double curS;

	private double totalS;

	private boolean movingRight;

	private boolean hasFinished = false;

	private double acc;

	private double initV;

	private long curTime;

	private double remainedMoveDistance = 0;

	@SuppressWarnings("unused")
	private AccShiftProxy()
	{

	}

	public AccShiftProxy(double initS, double totalS, double maxVel,
			boolean movingRight)
	{
		if (initS < 0)
		{
			throw new IllegalArgumentException("initS should be non negtive");
		}
		if (totalS <= 0)
		{
			throw new IllegalArgumentException("totalS should be positive");
		}
		if (totalS < initS)
		{
			throw new IllegalArgumentException(
					"totalS should be greater than initS");
		}
		if (maxVel <= 0)
		{
			throw new IllegalArgumentException("maxVel should be positive");
		}

		this.initS = initS;
		this.curS = initS;
		this.totalS = totalS;
		this.movingRight = movingRight;
		this.acc = maxVel * maxVel / (2 * totalS);
		this.initV = Math.pow(2 * acc * initS, 0.5);
		this.curTime = 0;
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

		curTime += interpolation;

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

	public double getVelocity()
	{
		return initV + acc * curTime;
	}

	public boolean hasFinished()
	{
		return hasFinished;
	}

	public void reset()
	{
		curS = initS;
		curTime = 0;
		remainedMoveDistance = 0;
		hasFinished = false;
	}

}
