package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * ClosableBlockingQueue在BlockingQueue的基础上发展而来。 它被设计成可开关组件，拥有打开和关闭方法。
 * 
 * ClosableBlockingQueue与BlockingQueue的不同之处在于，ClosableBlockingQueue
 * 又有打开和关闭的方法。它是可开关组件，参见类OpenCloseSupport中的定义。你需要先打开ClosableBlockingQueue
 * 后才能使用。关闭后，所有阻塞的线程都会被赶出来，抛出AlreadyClosedException，并且多数方法将不能够在使用，
 * 且能够阻塞线程的方法一定不能够在使用。
 * 
 */
public class ClosableBlockingQueue<T> implements IOpenClosable {
	private LinkedBlockingQueue<T> queue; // 注意，队列的大小必须是无限大的，才能符合这个类的设计

	private OpenCloseSupport openCloseSupport;

	public ClosableBlockingQueue() {
		queue = new LinkedBlockingQueue<T>();

		openCloseSupport = new OpenCloseSupport(new OpenMethod(),
				new CloseMethod());
	}

	@Override
	public boolean isOpen() {
		return openCloseSupport.isOpen();
	}

	/**
	 * Inserts the specified element at the tail of this queue.
	 * 
	 * @param value
	 *            the inserted element
	 * @throws InterruptedException
	 * 
	 * @throws InterruptedException
	 * @throws NullPointerException
	 *             - if the specified element is null
	 */
	public void enqueue(T value) {
		if (null == value) {
			throw new NullPointerException("value == null");
		}

		openCloseSupport.lockMethod();

		try {
			openCloseSupport.ensureOpen();

			queue.put(value);
		} catch (InterruptedException ex) {
			// 因为队列大小无限，不可能发生等待的情况，因此这个异常不可能发生
			assert (false);
			ex.printStackTrace();
		} finally {
			openCloseSupport.unlockMethod();
		}
	}

	/**
	 * Retrieves and removes the head of this queue, waiting if necessary until
	 * an element becomes available.
	 * 
	 * @return the head of this queue
	 * 
	 * @throws InterruptedException
	 */
	public T dequeue() {
		openCloseSupport.lockMethod();

		try {
			if (!openCloseSupport.isOpen() && isEmpty()) {
				openCloseSupport.ensureOpen();
			}

			return queue.take();
		} catch (InterruptedException ex) {
			// 一般来说，这个异常应该是由这个类的关闭引发的。
			// 但是如果外界故意中断进入这个方法的线程，则这个
			// 异常就可能在不是这个类关闭的时候发生了
			throw new AlreadyClosedException(
					"there should be somebody who has closed "
							+ "ClosableBlockingQueue, or someone has interrupted "
							+ "the thread intentively");
		} finally {
			openCloseSupport.unlockMethod();
		}

	}

	/**
	 * Returns <tt>true</tt> if this queue contains no elements.
	 * 
	 * @return <tt>true</tt> if this queue contains no elements
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * clear all elements contained in the blocking queue
	 */
	public void clear() {
		openCloseSupport.lockMethod();

		try {
			openCloseSupport.ensureOpen();

			queue.clear();
		} finally {
			openCloseSupport.unlockMethod();
		}
	}

	/**
	 * return the number of the elements contained in the blocking queue
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * 关闭组件。注意关闭后会清空所有存储的对象
	 */
	@Override
	public void close() {
		try {
			openCloseSupport.close();
		} catch (FailedToCloseException ex) {
			assert (false);
			ex.printStackTrace();
		}

	}

	@Override
	public void open() {
		try {
			openCloseSupport.open();
		} catch (FailedToOpenException ex) {
			assert (false);
			ex.printStackTrace();
		}
	}

	private class OpenMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			queue.clear();
		}

	}

	private class CloseMethod implements TargetMethod {

		@Override
		public void run() throws Exception {

		}

	}

}
