package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.other.ConfigViewLock;

/**
 * 
 * 这个类只是简单地对ConfigViewLock提供了一层封装，功能还是一样，
 * 只是把ConfigViewLock做成了像OpenCloseSupport一样的模式，来为 一些类的同步机制提供服务。
 * 
 */
public class ConfigViewSupport {
	private ConfigViewLock lock = new ConfigViewLock();

	/**
	 * @see ConfigViewLock
	 */
	public void lockConfig() {
		lock.lockConfig();
	}

	/**
	 * @see ConfigViewLock
	 */
	public void lockView() {
		lock.lockView();
	}

	/**
	 * @see ConfigViewLock
	 */
	public void unlockConfig() {
		lock.unlockConfig();
	}

	/**
	 * @see ConfigViewLock
	 */
	public void unlockView() {
		lock.unlockView();
	}

}
