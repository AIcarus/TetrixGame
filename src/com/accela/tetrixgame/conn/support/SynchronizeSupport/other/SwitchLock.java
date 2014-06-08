package com.accela.tetrixgame.conn.support.SynchronizeSupport.other;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * SwitchLock用于为能够打开和关闭的组件提供同步机制。
 * 
 * 能够打开和关闭的组件有两种方法，一种是开关方法，如open、close 一种是普通的方法，这里用method1、method2....表示。有同步要求：
 * 1、开关方法只能串行执行 2、普通方法可以并行执行 3、开关方法被调用的时候，普通方法不能执行，即线程不能进入其中
 * 4、如果在普通方法被执行的时候，另外一个线程去执行开关方法，所有 进入普通方法的线程都会被一个InterruptedException赶出来。然后要
 * 求执行开关方法的线程会等这些线程都从普通方法中出来的时候才开始执 行开关方法。
 * 这里的锁可以满足上述要求。具体用法见示例类SwitchLockExample。
 * 
 * 使用时，要注意： 1、lockSwicth()...unlockSwitch()与lockMethod()...unlockMethod() 不能嵌套。
 * 2、unlockMethod()方法一定要放在finally字句中，并保证线程无论抛出什么 异常，都会调用unlockMethod()来释放锁
 * 
 * 
 * 
 */
public class SwitchLock {
	private ReentrantLock synLock = new ReentrantLock();

	private Condition methodCondition = synLock.newCondition();

	private Condition switchCondition = synLock.newCondition();

	/**
	 * 用来表示当前谁拥有锁。 如果lockState==0，表示锁无人拥有。 如果lockState>0，表示开关方法拥有锁。
	 * 如果lockState<0，表示普通方法拥有锁。
	 */
	private int lockState = 0;

	private boolean drivingMethodThreads = false;

	private ThreadRecorder threadRecorder = new ThreadRecorder();

	private ReentrantLock switchLock = new ReentrantLock();

	private boolean isLockIdle() {
		return 0 == lockState;
	}

	private boolean isLockOwnedBySwitch() {
		return lockState > 0;
	}

	private boolean isLockOwnedByMethod() {
		return lockState < 0;
	}

	private void takeMethodLock() {
		assert (lockState <= 0);
		lockState--;
	}

	private void returnMethodLock() {
		assert (lockState < 0);
		lockState++;

		if (lockState >= 0) {
			switchCondition.signalAll();
		}
	}

	private void takeSwitchLock() {
		assert (lockState >= 0);
		lockState++;
	}

	private void returnSwitchLock() {
		assert (lockState > 0);
		lockState--;

		if (lockState <= 0) {
			methodCondition.signalAll();
		}
	}

	public void lockSwitch() {
		switchLock.lock();

		synLock.lock();
		try {
			while (!grabSwitchLock()) {
				switchCondition.awaitUninterruptibly();
			}
		} finally {
			synLock.unlock();
		}

	}

	public void unlockSwitch() {
		synLock.lock();

		try {
			returnSwitchLock();
		} finally {
			synLock.unlock();
		}

		switchLock.unlock();
	}

	private boolean grabSwitchLock() {
		if (isLockIdle()) {
			assert (threadRecorder.isEmpty());
			takeSwitchLock();
			drivingMethodThreads = false;
			return true;
		} else if (isLockOwnedByMethod()) {
			assert (!drivingMethodThreads);
			drivingMethodThreads = true;
			driveOutMethodThreads();
			return false;
		} else if (isLockOwnedBySwitch()) {
			takeSwitchLock();
			assert (!drivingMethodThreads);
			return true;
		} else {
			assert (false);
			return false;
		}
	}

	private void driveOutMethodThreads() {
		assert (isLockOwnedByMethod());
		for (Thread thread : threadRecorder.keySet()) {
			assert (thread != null);
			if (thread != null) {
				thread.interrupt();
			}
		}
	}

	public void lockMethod() {
		synLock.lock();

		try {
			while (!grabMethodLock()) {
				methodCondition.awaitUninterruptibly();
			}

			threadRecorder.recordThread();
		} finally {
			synLock.unlock();
		}
	}

	public void unlockMethod() {
		synLock.lock();

		try {
			returnMethodLock();
		} finally {
			threadRecorder.disrecordThread();
			synLock.unlock();
		}
	}

	private boolean grabMethodLock() {
		if (drivingMethodThreads) {
			return false;
		}

		if (isLockIdle()) {
			assert (threadRecorder.isEmpty());
			takeMethodLock();
			return true;
		} else if (isLockOwnedByMethod()) {
			takeMethodLock();
			return true;
		} else if (isLockOwnedBySwitch()) {
			return false;
		} else {
			assert (false);
			return false;
		}
	}

}
