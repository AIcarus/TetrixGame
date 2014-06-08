package com.accela.tetrixgame.gamelogic.rectMover.vertical;

/**
 * 
 * 有重力作用的竖直方向速度代理。 这个代理控制的速度，在重力的 作用下，均匀变化。
 * 
 */
public class VerticalGravityVelocityProxy
{
	/**
	 * 重力加速度
	 */
	private double acceleration;
	/**
	 * 当前的速度
	 */
	private double velocity;

	@SuppressWarnings("unused")
	private VerticalGravityVelocityProxy()
	{

	}

	/**
	 * 创建一个竖直方向速度代理
	 * 
	 * @param acceleration
	 *            指定的重力加速度
	 * @param initialVelocity
	 *            指定的初始速度
	 */
	public VerticalGravityVelocityProxy(double initialVelocity,
			double acceleration)
	{
		this.acceleration = acceleration;
		this.velocity = initialVelocity;
	}

	/**
	 * 重置
	 * 
	 * @param acceleration
	 *            指定的重力加速度
	 * @param initialVelocity
	 *            指定的初始速度
	 */
	public void reset(double initialVelocity)
	{
		this.velocity = initialVelocity;
	}

	/**
	 * 每个时钟周期的更新
	 * 
	 * @param interpolation
	 *            这次调用和上次调用的时间差值
	 */
	public void update(long interpolation)
	{
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		velocity += acceleration * interpolation;
	}

	public double getVelocity()
	{
		return velocity;
	}

	public double getAcceleration()
	{
		return acceleration;
	}

}
