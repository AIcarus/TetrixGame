package com.accela.tetrixgame.gamelogic.effects.shake;

import java.awt.Graphics;
import java.awt.Point;

import com.accela.tetrixgame.gamelogic.effects.AbstractEffect;

/**
 * 
 * 这个类用来产生正弦振动。实际上，通过每个时钟周期的不断刷新， 你就可以通过getShakeOffset()方法获得按照正弦曲线变化的振动 偏移量。
 * 
 */
public class Shaker extends AbstractEffect {
	private int maxOffset;

	// 记录当期已经振动了多少周期
	private int curNumPeriod;

	// 记录总共能够振动的周期数
	private int totalNumPeriod;

	// 记录在当前周期中，已经经过了多少时间。
	private long curTimeCount;

	private long period;

	public Shaker() {
		this(0, 1000, 1);
	}

	public Shaker(int maxOffset, long period, int numPeriod) {
		initialize(maxOffset, period, numPeriod);
	}

	public void initialize(int maxOffset, long period, int numPeriod) {
		if (maxOffset < 0) {
			throw new IllegalArgumentException("maxOffset should be nonnegtive");
		}
		if (period <= 0) {
			throw new IllegalArgumentException("maxOffset should be positive");
		}
		if (numPeriod < 0) {
			throw new IllegalArgumentException("maxOffset should be nonnegtive");
		}

		this.maxOffset = maxOffset;
		this.period = period;
		this.curNumPeriod = numPeriod;
		this.totalNumPeriod = numPeriod;

		this.curTimeCount = 0;

		this.setValid(true);
	}

	@Override
	protected void onTickImpl(long interpolation) {
		if (curNumPeriod <= 0) {
			this.setValid(false);
			return;
		}

		curTimeCount += interpolation;
		if (curTimeCount >= period) {
			curTimeCount = 0;
			curNumPeriod--;
		}

	}

	public double getShakeOffset() {
		if (!isValid()) {
			assert (0 == curNumPeriod);
			return 0;
		}

		return maxOffset
				* (double) curNumPeriod
				/ (double) totalNumPeriod
				* Math.sin(2 * Math.PI * (double) curTimeCount
						/ (double) period);
	}

	@Override
	protected void paintImpl(Graphics g, Point center, double scale) {
		throw new UnsupportedOperationException(
				"shaker is not used to paint. it is used to get the shake offset");

	}

}
