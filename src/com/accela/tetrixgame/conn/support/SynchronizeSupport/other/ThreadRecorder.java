package com.accela.tetrixgame.conn.support.SynchronizeSupport.other;

import java.util.*;

/**
 * 
 * 用来给SwitchLock记录线程。能够记录一个线程被记录的次数。
 * 
 */
class ThreadRecorder {
	private Map<Thread, ThreadCount> recordedThreads = new HashMap<Thread, ThreadCount>();

	public void recordThread() {
		Thread t = Thread.currentThread();
		ThreadCount count = recordedThreads.get(t);
		if (null == count) {
			recordedThreads.put(t, ThreadCount.newObject(1));
		} else {
			count.increaseCount();
		}
	}

	public void disrecordThread() {
		Thread t = Thread.currentThread();
		ThreadCount count = recordedThreads.get(t);

		assert (count != null);
		if (null == count) {
			return;
		}

		count.decreaseCount();
		if (count.getCount() <= 0) {
			recordedThreads.remove(t);
		}

	}

	public boolean isEmpty() {
		return recordedThreads.isEmpty();
	}

	public Set<Thread> keySet() {
		return recordedThreads.keySet();
	}

	/**
	 * 
	 * 用来记录一个线程重复记录的次数
	 * 
	 */
	private static class ThreadCount {
		private int count = 0;

		private static Stack<ThreadCount> buffer = new Stack<ThreadCount>();

		public static ThreadCount newObject(int initCount) {
			if (buffer.isEmpty()) {
				return new ThreadCount(initCount);
			} else {
				ThreadCount old = buffer.pop();
				assert (old != null);
				old.count = initCount;
				return old;
			}
		}

		private ThreadCount(int initCount) {
			assert (initCount >= 0);
			this.count = initCount;
		}

		public void increaseCount() {
			count++;
		}

		public void decreaseCount() {
			count--;
			assert (count >= 0);
		}

		public int getCount() {
			return count;
		}
	}

}
