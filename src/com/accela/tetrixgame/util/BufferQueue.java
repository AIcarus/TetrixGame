package com.accela.tetrixgame.util;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 给TetrixEngineMirror使用的缓冲队列。这个缓冲队列除了支持线程同步外，
 * 与普通队列的不同之处在于，如果这个队列中的对象被全部去完，那么外界再 通过出队取对象的时候返回null，当又有新的对象放入队列的时候，调用出队
 * 方法扔回返回null。只有当队列中的对象积累到bufferLength个，出队方法 才能够取出对象。
 * 
 * 这个工作模式就好像网上播放视频的方式。
 */
public class BufferQueue<T> {
	private int bufferLength;

	private Queue<T> queue = new LinkedList<T>();

	private ReentrantLock synLock = new ReentrantLock();

	private Condition putCondition = synLock.newCondition();

	private boolean waitingForBuffer = true;

	public BufferQueue(int bufferLength) {
		if (bufferLength <= 0) {
			throw new IllegalArgumentException(
					"bufferLength should be positive");
		}

		this.bufferLength = bufferLength;
	}

	public int getBufferLength() {
		return this.bufferLength;
	}

	public T dequeue() {
		synLock.lock();
		try {
			if (waitingForBuffer || queue.isEmpty()) {
				waitingForBuffer = true;
				return null;
			}

			T ret = queue.poll();
			assert (ret != null);

			if (queue.size() <= 0) {
				putCondition.signalAll();
			}

			return ret;
		} finally {
			synLock.unlock();
		}
	}

	public T peek() {
		synLock.lock();
		try {
			if (waitingForBuffer || queue.isEmpty()) {
				waitingForBuffer = true;
				return null;
			}

			T ret = queue.peek();
			assert (ret != null);

			return ret;
		} finally {
			synLock.unlock();
		}
	}

	public void enqueue(T element) {
		if (null == element) {
			throw new NullPointerException("element should not be null");
		}

		synLock.lock();

		try {
			queue.offer(element);

			if (queue.size() >= bufferLength && waitingForBuffer) {
				waitingForBuffer = false;
			}
		} finally {
			synLock.unlock();
		}
	}

	/**
	 * 将element入队。但是如果入队后队列的大小会超过sizeLimit，则会等待直到 队列的大小变为零。
	 * 
	 * @throws InterruptedException
	 */
	public void put(T element, int sizeLimit) throws InterruptedException {
		if (null == element) {
			throw new NullPointerException("element should not be null");
		}
		if (sizeLimit <= 0) {
			throw new IllegalArgumentException("sizeLimit should be positive");
		}
		if (sizeLimit < bufferLength) {
			throw new IllegalArgumentException(
					"sizeLimit should not be less than bufferLength");
		}

		synLock.lock();

		try {
			while (queue.size() >= sizeLimit) {
				putCondition.await();
			}

			enqueue(element);
		} finally {
			synLock.unlock();
		}
	}

	public int size() {
		return queue.size();
	}

}
