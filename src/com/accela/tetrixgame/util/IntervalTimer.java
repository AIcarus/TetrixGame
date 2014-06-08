package com.accela.tetrixgame.util;

/**
 * 
 * 每次刷新，这个时钟可以通过getInterval方法告诉你，这次刷新与上次刷新的时间间隔
 * 
 */
public class IntervalTimer {
	private long curSystemTimeCount;

	private long startSystemTime;

	private long interval;

	/**
	 * 以蜂鸣的时间间隔为参数，创建一个TimeCounter。 TimeCounter每隔beepInterval就会把beep设为true，
	 * 即发出蜂鸣。
	 */
	public IntervalTimer() {
		curSystemTimeCount = -1;
		interval = -1;
		startSystemTime = -1;
	}

	/**
	 * 刷新TimeCounter
	 */
	public void update() {
		if (curSystemTimeCount < 0) {
			interval = -1;
			curSystemTimeCount = System.currentTimeMillis();
			startSystemTime = curSystemTimeCount;
		} else {
			interval = System.currentTimeMillis() - curSystemTimeCount;
			curSystemTimeCount += interval;
		}

	}

	/**
	 * @return 这次刷新和上次刷新的时间间隔
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @return 得到TimeCounter这次刷新时，到第一次刷新的时间间隔。
	 */
	public long getCurTimeCount() {
		long curTimeCount = curSystemTimeCount - startSystemTime;
		assert (curTimeCount >= 0);
		return curTimeCount;
	}

}
