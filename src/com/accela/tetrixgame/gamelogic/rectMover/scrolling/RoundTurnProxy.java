package com.accela.tetrixgame.gamelogic.rectMover.scrolling;

import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 做圆弧运动的移动代理
 * 
 */
public class RoundTurnProxy
{
	private double radius;

	private double vel;

	private double initAngle;

	private double totalAngle = 0;

	private double curAngle;

	private boolean clockWise;

	private boolean hasFinished = false;

	@SuppressWarnings("unused")
	private RoundTurnProxy()
	{

	}

	public RoundTurnProxy(double vel, double radius, double initAngle,
			double totalAngle, boolean clockWise)
	{
		if (vel <= 0)
		{
			throw new IllegalArgumentException("vel should be positive");
		}
		if (radius <= 0)
		{
			throw new IllegalArgumentException("radius should be positive");
		}
		if (totalAngle <= 0)
		{
			throw new IllegalArgumentException("totalAngle should be positive");
		}

		this.radius = radius;
		this.vel = vel;
		this.initAngle = initAngle;
		this.totalAngle = totalAngle;
		this.clockWise = clockWise;
		this.curAngle = initAngle;
	}

	private double xRemain = 0;
	private double yRemain = 0;

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

		// 刷新当前旋转的角度
		double dertAngle = vel / radius * interpolation;
		dertAngle = Math.min(dertAngle, totalAngle
				- Math.abs((curAngle - initAngle)));
		if (Math.abs(curAngle - initAngle) >= totalAngle)
		{
			hasFinished = true;
			dertAngle = 0;
		}
		if (!clockWise)
		{
			dertAngle = -dertAngle;
		}
		curAngle += dertAngle;

		// 刷新位移
		double dertX = getFlipRadius(curAngle) * Math.cos(curAngle)
				- getFlipRadius(curAngle - dertAngle)
				* Math.cos(curAngle - dertAngle);
		double dertY = getFlipRadius(curAngle) * Math.sin(curAngle)
				- getFlipRadius(curAngle - dertAngle)
				* Math.sin(curAngle - dertAngle);

		int actualDertX = (int) Math.round(dertX + xRemain);
		xRemain = (dertX + xRemain) - actualDertX;
		int actualDertY = (int) Math.round(dertY + yRemain);
		yRemain = (dertY + yRemain) - actualDertY;

		rect.moveBy(actualDertX, actualDertY);

	}

	private double getFlipRadius(double curAngle)
	{
		return radius;// -0.3*radius*Math.sin(Math.PI*(Math.abs(curAngle-initAngle))/totalAngle);
	}

	public void reset()
	{
		curAngle = 0;
		hasFinished = false;
		curAngle = initAngle;
		xRemain = 0;
		yRemain = 0;
	}

	public double getVelocity()
	{
		return vel;
	}

	public boolean hasFinished()
	{
		return hasFinished;
	}

}
