package com.accela.tetrixgame.util;

/**
 * 
 * 这个类是一个计时器，你需要不断的刷新它，它就会自动记录刷新的时候的系统当前时间。
 * 你还可以通过它获得这次刷新与上次刷新的时间间隔。另外，你可以设定一个时间间隔，每
 * 隔这个时间间隔，TimerCounter会自动把蜂鸣位beep设为true。
 * 
 * 当然，TimeCounter工作的基础是你不断刷新它。
 * 
 */
public class TimeCounter {
	private long curTimeCount;

	private long interval;

	private long beepInterval;

	private long beepCount;

	private boolean beep;

	/**
	 * 以蜂鸣的时间间隔为参数，创建一个TimeCounter。 TimeCounter每隔beepInterval就会把beep设为true，
	 * 即发出蜂鸣。
	 */
	public TimeCounter(long beepInterval) {
		if (beepInterval <= 0) {
			throw new IllegalArgumentException(
					"beepInterval should be positive");
		}

		this.beepInterval = beepInterval;
		beepCount = 0;
		beep = false;

		curTimeCount = 0;
		interval = -1;
	}

	/**
	 * 新建一个不使用蜂鸣功能的TimeCounter
	 */
	public TimeCounter() {
		this(1000);
	}

	/**
	 * 指定interpolation（时间间隔）的刷新。第一次刷新将作为计时起点， 此时将忽略 interpolation
	 */
	public void update(long interpolation) {
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should not negtive");
		}

		interval = interpolation;
		curTimeCount += interval;

		beepCount += interval;
		if (beepCount >= beepInterval) {
			beep = true;
			beepCount = 0;
		}

	}

	/**
	 * @return 这次刷新和上次刷新的时间间隔
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * @return TimeCounter是否正在发出蜂鸣
	 */
	public boolean isBeep() {
		return beep;
	}

	/**
	 * 将蜂鸣置位false。TimeCounter一旦将beep位设为true， 只有你调用clearBeep方法后，在能吧beep为设为false，
	 * 以便鉴别下次蜂鸣
	 */
	public void clearBeep() {
		beep = false;
	}

	public long getBeepInterval() {
		return beepInterval;
	}

	/**
	 * @return 得到TimeCounter这次刷新时，到第一次刷新的时间间隔。
	 */
	public long getCurTimeCount() {
		return curTimeCount;
	}

}
