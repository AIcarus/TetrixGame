package com.accela.tetrixgame.conn.broadcaster;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.ClosableBlockingQueue;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.IOpenClosable;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.OpenCloseSupport;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.TargetMethod;

/**
 * 
 * 这个类用来发现网络上的广播。 根据具体的网络技术，将被继承和实现。
 * 
 * 注意，Broadcaster被设计成为可开关类（具体定义见com.accela.tetrixgame.synchronizeSupport.standardSupport.
 * OpenCloseSupport）
 * 如果你继承这个类，在添加新的方法的时候，请参照相关的可开关类的设计标准。Broadcaster所使用的OpenCloseSupport对象
 * 已经被设计成protected类型，可供使用。
 * 
 */
public abstract class Broadcaster implements IOpenClosable {
	/**
	 * 收到的广播信息都会存储在这个队列中，外界取出
	 */
	private ClosableBlockingQueue<RemotePackage> receivingQueue = new ClosableBlockingQueue<RemotePackage>();

	private ReceivingThread receivingThread;
	/**
	 * 用来周期性地发送广播信息的定时器
	 */
	private Timer timer;

	private static final String TIMER_NAME = "Broadcaster - timer";
	/**
	 * 为了使Broadcaster满足可开关类的设计要求，而使用的同步机制实现帮手
	 */
	protected OpenCloseSupport ocs;

	public Broadcaster() {
		ocs = new OpenCloseSupport(new OpenMethod(), new CloseMethod());
	}

	/**
	 * 
	 * 从messageQueue中取出一个已经收到的对象。如果没有，这个方法会使线程阻塞直到有新的 收到的对象。
	 * 
	 * 如果Broadcaster已经被关闭，则当信息还没有被取光的时候，还可以调用这个方法，当信息
	 * 已经被取光后，再调用这个方法将会抛出AlreadyClosedException。
	 * 
	 * 返回的RemotePackage封装了指向信息发送者的ClientID和他发送的信息
	 */
	public RemotePackage retrieveMessage() {
		ocs.lockMethod();
		try {
			if (!ocs.isOpen() && !hasMessageToRetrieve()) {
				ocs.ensureOpen();
			}

			return receivingQueue.dequeue();
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 检测是否还有信息没有取出
	 */
	public boolean hasMessageToRetrieve() {
		return !receivingQueue.isEmpty();
	}

	/**
	 * 根据具体网络技术实现的接收广播信息的方法。如果此时没有可接收的信息，则应阻塞。
	 */
	protected abstract RemotePackage receiveMessageFromBroadcast()
			throws Exception;

	/**
	 * 将message广播一次
	 * 
	 * @throws FailedToBroadcastMessageException
	 */
	public void broadcastMessage(Object message)
			throws FailedToBroadcastMessageException {
		if (null == message) {
			throw new NullPointerException();
		}

		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			broadcastMessageImpl(message);
		} catch (Exception ex) {
			throw new FailedToBroadcastMessageException(ex);
		} finally {
			ocs.unlockMethod();
		}

	}

	/**
	 * 调用方法后会很快返回，然后内部线程会每隔period指定的毫秒 时间将message广播一次
	 */
	public void broadcastMessage(Object message, long period) {
		if (null == message) {
			throw new NullPointerException();
		}
		if (period <= 0) {
			throw new IllegalArgumentException();
		}

		ocs.lockMethod();

		try {
			ocs.ensureOpen();
			Common.LOG.info("Broadcaster: broadcastMessage() invoked, period="+period+", this.class="+this.getClass());
			timer.schedule(new BroadcastMessageTask(message), 0, period);
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 根据具体的网络技术，将message广播一次的方法，需要子类来实现
	 */
	protected abstract void broadcastMessageImpl(Object message)
			throws Exception;

	private ReentrantLock synLock = new ReentrantLock();

	/**
	 * 终止当前的所有重复广播信息的任务
	 * 
	 * @throws BroadcasterClosedException
	 * @throws InterruptedException
	 */
	public void cancelAllBroadcast() {
		ocs.lockMethod();
		synLock.lock();
		try {
			ocs.ensureOpen();

			timer.cancel();
			timer = new Timer(TIMER_NAME, true);
		} finally {
			synLock.unlock();
			ocs.unlockMethod();
		}
	}

	@Override
	public boolean isOpen() {
		return ocs.isOpen();
	}

	@Override
	public void open() throws FailedToOpenException {
		ocs.open();
	}

	protected abstract void openImpl() throws Exception;

	@Override
	public void close() throws FailedToCloseException {
		ocs.close();

	}

	/**
	 * 子类实现的停止接收广播信息的处理过程，这个方法会在close()中调用
	 */
	protected abstract void closeImpl() throws Exception;

	private class OpenMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			if (receivingThread != null) {
				if (receivingThread.isAlive()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}

			openImpl();
			receivingQueue.open();
			receivingThread = new ReceivingThread();
			receivingThread.start();
			timer = new Timer(TIMER_NAME, true);

		}

	}

	private class CloseMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			try {
				closeImpl();
			} finally {
				receivingQueue.close();
				timer.cancel();
			}

		}

	}

	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Broadcaster内的线程，它不断接收信息，并放入messageQueue
	 */
	private class ReceivingThread extends Thread {
		public ReceivingThread() {
			super("Broadcaster - ReceivingThread");
		}

		public void run() {
			while (ocs.isOpen()) {
				try {
					RemotePackage message = null;
					Common.LOG.info("Broadcaster: receiving thread run()");
					message = receiveMessageFromBroadcast();
					if (null == message) {
						assert (false);
						throw new NullPointerException(
								"receiveMessageFromBroadcast() should not return null");
					}
					receivingQueue.enqueue(message);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Broadcaster内Timer对象所使用的TimerTask，用来发出广播
	 */
	private class BroadcastMessageTask extends TimerTask {
		private Object message;

		public BroadcastMessageTask(Object message) {

			assert (message != null);
			if (null == message) {
				throw new NullPointerException("message should not be null");
			}
			this.message = message;
		}

		@Override
		public void run() {
			try {
				broadcastMessageImpl(message);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}