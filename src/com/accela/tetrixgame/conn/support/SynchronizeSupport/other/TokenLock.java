package com.accela.tetrixgame.conn.support.SynchronizeSupport.other;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 这种锁纯属本人自创，名字是我取的。 这个类新建时，内部就同时新建了numOfLocks把不同的锁，分别用数字0...numOfLocks-1
 * 表示。你可以用任何一把锁去包裹住一段代码，比如： <code>
 * TokenLock tokenLock=new TokenLock(3);
 * ...
 * ...
 * tokenLock.lock(0);
 * processing...
 * tokenLock.unlock(0);
 * ...
 * ...
 * tokenLock.lock(0);
 * processing...
 * tokenLock.unlock(0);
 * ...
 * ...
 * tokenLock.lock(1);
 * processing...
 * tokenLock.unlock(1);
 * ...
 * ...
 * tokenLock.lock(2);
 * processing...
 * tokenLock.unlock(2);
 * ...
 * ...
 * </code> 以上面为例，这个锁是这样工作的。如果0号锁被锁住（tokenLock.lock(0)），那么
 * 用其他的锁，如1号锁、2号锁等包住的代码段将无法进入（即tokenLock.lock(1)等会
 * 使线程等待）。但是被0号锁包住的代码段仍然可以重复进入，哪怕不是同一个线程。
 * 当0号锁被释放（tokenLock.unlock(0)）后，被各个锁阻拦住的线程就会竞争来进入 相应的代码段，最后又会有且仅有一个锁被锁住。
 * 
 * 总的来说，就是各个标号的锁去争抢一个令牌，谁获得了令牌，谁包住的代码段就可以 无限制地进入，但其他标号的锁所包住的代码段却不能进入。注意，一个标号的锁常常
 * 可以用来包住多个代码段。比如： ... ... tokenLock.lock(0); processing... tokenLock.unlock(0);
 * ... ... tokenLock.lock(0); processing... tokenLock.unlock(0); ... ...
 * tokenLock.lock(1); processing... tokenLock.unlock(1); ... ...
 * tokenLock.lock(1); processing... tokenLock.unlock(1); ... ...
 * 在这种情况下，TokenLock可以保证在同一时间，要么只有被0号锁包住的代码段在执行，
 * 要么只有被1号锁包住的代码段在执行。但两个被0号锁包住的代码段却可以同时执行，和 没有锁一样。事实上TokenLock就是为这种情况而设计的。
 */
public class TokenLock {
	private static final int INVALID_TOKEN_HOLDER = -1;

	private int whichLockHasToken = INVALID_TOKEN_HOLDER;
	private int numOfLocks = -1;

	private int count = 0; // 锁的进入次数

	private Object waitLock = new Object();

	private ReentrantLock syncLock = new ReentrantLock();;

	public TokenLock(int numOfLocks) {
		if (numOfLocks <= 0) {
			throw new IllegalArgumentException();
		}

		this.numOfLocks = numOfLocks;
	}

	public int getNumOfLocks() {
		return numOfLocks;
	}

	public void lock(int whichLockToLock) throws InterruptedException {
		if (whichLockToLock < 0) {
			throw new IllegalArgumentException();
		}

		while (!tryGetToken(whichLockToLock)) {
			synchronized (waitLock) {
				waitLock.wait();
			}
		}
	}

	/**
	 * 标号为whichLock的锁试图得到令牌。
	 * 
	 * @param whichLock
	 * @return 是否得到了令牌
	 */
	private boolean tryGetToken(int whichLock) {
		syncLock.lock();
		try {
			if (INVALID_TOKEN_HOLDER == whichLockHasToken) {
				// 令牌没人用，所以标号为whichLock的锁抢走令牌
				whichLockHasToken = whichLock;
				count = 1;
				return true;
			} else if (whichLock == whichLockHasToken) {
				// 标号为whichLock的锁已经拥有令牌了，重入该锁
				count++;
				return true;
			} else {
				// 令牌在除开whichLock以外的其他锁那里，得不到令牌
				return false;
			}
		} finally {
			syncLock.unlock();
		}
	}

	public void unlock(int whichLockToUnlock) {
		if (whichLockToUnlock < 0) {
			throw new IllegalArgumentException();
		}

		syncLock.lock();
		try {
			if (whichLockToUnlock != whichLockHasToken) {
				throw new IllegalStateException(
						"It is illogical for a lock who doesn't has the token to unlock!");
			}

			count--;
			assert (count >= 0);
			if (0 == count) {
				whichLockHasToken = INVALID_TOKEN_HOLDER;
				synchronized (waitLock) {
					waitLock.notifyAll();
				}
			}
		} finally {
			syncLock.unlock();
		}
	}

}
