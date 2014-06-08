package com.accela.tetrixgame.conn.support.SynchronizeSupport.other;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * 这个类提供对修改类的方法和类的只读方法提供同步支持。
 * 
 * 在编写类的时候，经常有这样的需求：类中有一些方法，它们有更改类的内部数据
 * 的功能，而另一些方法只有查看类的数据的功能。比如HashMap中，put、remove、 clear等方法会改变类的数据，而get方法只会查看类的数据。
 * 
 * 这里将这些修改类的方法叫做config方法，将只会查看类的内部数据而不会修改它
 * 的方法叫做view方法。当多线程的时候，你可能会希望，config方法全部串行执行，
 * view方法之间可以并行执行。并且，当config方法执行的时候，view方法不能执行， 当view方法执行的时候，config方法不能执行。
 * 
 * 这个类就是为了提供上述的同步机制而设计的。你应该把修改类的内容的方法中的全
 * 部代码放在lockConfig()...unlockConfig()之间，而把类的只读方法中的全部代码
 * 放在lockView()...unlockView()之间。这样便可以使得类中的这两种方法达到上述的 同步要求。
 * 
 * 当然，更稳妥的方式是用finally字句将unlockXXX()方法包起来，以保证一定会释放锁。
 * 
 * 注意，lockConfig()...unlockConfig()，以及lockView()...unlockView()不能嵌套， 否则会出现死锁。
 * 
 * 具体例子见示例类ConfigViewLockExample
 * 
 */
public class ConfigViewLock {
	private ReentrantLock synLock = new ReentrantLock();

	private Condition viewCondition = synLock.newCondition();

	private Condition configCondition = synLock.newCondition();

	/**
	 * 用来表示当前谁拥有锁。 如果lockState==0，表示锁无人拥有。 如果lockState>0，表示config方法拥有锁。
	 * 如果lockState<0，表示view方法拥有锁。
	 */
	private int lockState = 0;

	private ReentrantLock configLock = new ReentrantLock();

	private boolean isLockIdle() {
		return 0 == lockState;
	}

	private boolean isLockOwnedByConfig() {
		return lockState > 0;
	}

	private boolean isLockOwnedByView() {
		return lockState < 0;
	}

	private void takeViewLock() {
		assert (lockState <= 0);
		lockState--;
	}

	private void returnViewLock() {
		assert (lockState < 0);
		lockState++;

		if (lockState >= 0) {
			configCondition.signalAll();
		}
	}

	private void takeConfigLock() {
		assert (lockState >= 0);
		lockState++;
	}

	private void returnConfigLock() {
		assert (lockState > 0);
		lockState--;

		if (lockState <= 0) {
			viewCondition.signalAll();
		}
	}

	/**
	 * 这个方法在会修改类的内部数据的方法的入口处调用
	 */
	public void lockConfig() {
		configLock.lock();

		synLock.lock();
		try {
			while (!grabConfigLock()) {
				configCondition.awaitUninterruptibly();
			}
		} finally {
			synLock.unlock();
		}

	}

	/**
	 * 这个方法在会修改类的内部数据的方法的结尾处调用
	 */
	public void unlockConfig() {
		synLock.lock();

		try {
			returnConfigLock();
		} finally {
			synLock.unlock();
		}

		configLock.unlock();
	}

	private boolean grabConfigLock() {
		if (isLockIdle()) {
			takeConfigLock();
			return true;
		} else if (isLockOwnedByView()) {
			return false;
		} else if (isLockOwnedByConfig()) {
			takeConfigLock();
			return true;
		} else {
			assert (false);
			return false;
		}
	}

	/**
	 * 这个方法在只读方法（不修改类的内部数据的方法）的入口处调用
	 */
	public void lockView() {
		synLock.lock();

		try {
			while (!grabViewLock()) {
				viewCondition.awaitUninterruptibly();
			}
		} finally {
			synLock.unlock();
		}
	}

	/**
	 * 这个方法在只读方法（不修改类的内部数据的方法）的结尾处调用
	 */
	public void unlockView() {
		synLock.lock();

		try {
			returnViewLock();
		} finally {
			synLock.unlock();
		}
	}

	private boolean grabViewLock() {
		if (isLockIdle()) {
			takeViewLock();
			return true;
		} else if (isLockOwnedByView()) {
			takeViewLock();
			return true;
		} else if (isLockOwnedByConfig()) {
			return false;
		} else {
			assert (false);
			return false;
		}
	}

}
