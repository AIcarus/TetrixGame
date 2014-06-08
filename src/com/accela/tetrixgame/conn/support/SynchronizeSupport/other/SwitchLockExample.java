package com.accela.tetrixgame.conn.support.SynchronizeSupport.other;

import com.accela.tetrixgame.Common;

/**
 * 
 * 这个类为清晰地表达SiwthLock的使用方法而编写
 * 
 */
public class SwitchLockExample {
	private boolean open = false;

	private SwitchLock lock = new SwitchLock();

	public boolean isOpen() {
		return open;
	}

	/**
	 * 这是开关方法中的打开方法。应该用 lockSwitch()...unlockSwitch()保 护起来。
	 */
	public void open() {
		lock.lockSwitch();
		try {
			open = true;
		} finally {
			lock.unlockSwitch();
		}
	}

	/**
	 * 这是开关方法中的关闭方法。应该用 lockSwitch()...unlockSwitch()保 护起来。
	 */
	public void close() {
		lock.lockSwitch();
		try {
			open = false;
		} finally {
			lock.unlockSwitch();
		}
	}

	/**
	 * 这是一个普通方法，应该用 lockMethod()...unlockMethod()保护起来
	 */
	public void method1() throws InterruptedException {
		lock.lockMethod();
		try {
			Thread.sleep(1000);
		} finally {
			/*
			 * 这里请注意，将unlockMethod()方法放在finally字句中， 并用try将可能抛出异常的方法包起来，不仅是一种好的
			 * 变成习惯，而且对于switchLock是必须的。
			 * 
			 * 因为，lockSwitch()通过使进入普通方法的线程抛出异常 来将其赶出方法。
			 */
			lock.unlockMethod();
		}
	}

	/**
	 * 这是一个普通方法，应该用 lockMethod()...unlockMethod()保护起来
	 */
	public void method2() {
		lock.lockMethod();
		try {
			Common.LOG.info("method2 invoked!");
		} finally {
			lock.unlockMethod();
		}
	}

}
