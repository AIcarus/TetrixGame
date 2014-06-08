package com.accela.tetrixgame.conn.support.SynchronizeSupport.other;

/**
 * 
 * 为了清楚地展示ConfigViewLock如何使用而编写的示例类
 * 
 */
public class ConfigViewLockExample {
	private ConfigViewLock lock = new ConfigViewLock();

	private int data = 0;

	/**
	 * 这个方法会修改类的内部数据，是config方法。 因此用lockConfig()...unlockConfig()保护。
	 * 这个方法，以及decrease()方法两者一起只能串行执行。而且这个方法绝对 不会和getData()同时执行
	 */
	public void increase() {
		lock.lockConfig();
		try {
			data++;
		} finally {
			lock.unlockConfig();
		}
	}

	/**
	 * 这个方法会修改类的内部数据，是config方法。 因此用lockConfig()...unlockConfig()保护
	 * 这个方法，以及icrease()方法两者一起只能串行执行。而且这个方法绝对 不会和getData()同时执行
	 */
	public void decrease() {
		lock.lockConfig();
		try {
			data--;
		} finally {
			lock.unlockConfig();
		}
	}

	/**
	 * 这个方法不会修改类的内部数据，是view方法。 因此用lockView()...unlockView()保护。
	 * 这个方法可以并行执行。但绝对不会与increase()、decrease()同时 执行。
	 */
	public int getData() {
		lock.lockView();
		try {
			return data;
		} finally {
			lock.unlockView();
		}
	}

}
