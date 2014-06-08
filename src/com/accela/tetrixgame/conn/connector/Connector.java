package com.accela.tetrixgame.conn.connector;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.ClosableBlockingQueue;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.IOpenClosable;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.OpenCloseSupport;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.TargetMethod;

/**
 * 
 * 连接器。 当与客户建立了一个连接后，这个类就代表这个连接。 外界是用sendMessage和retriveMessage来通过连接 器发送和接受信息。
 * 
 * 注意，Connector被设计成为可开关类（具体定义见com.accela.tetrixgame.synchronizeSupport.standardSupport.
 * OpenCloseSupport）
 * 如果你继承这个类，在添加新的方法的时候，请参照相关的可开关类的设计标准。Connector所使用的OpenCloseSupport对象
 * 已经被设计成protected类型，可供使用。
 * 
 */
public abstract class Connector implements Comparable<Connector>, IOpenClosable {
	/**
	 * 当Connector从Client收到一个对象时，这个对象将会被放入到receivingQueue中
	 */
	private ClosableBlockingQueue<RemotePackage> receivingQueue = new ClosableBlockingQueue<RemotePackage>();
	/**
	 * 用来新建Connector的线程的线程池
	 */
	private static final ExecutorService THREAD_POOL = Executors
			.newCachedThreadPool();
	/**
	 * 执行ReceivingThreadTask任务的线程的Future对象，用来控制该线程
	 */
	private Future<?> receivingThread;
	/**
	 * 记录这个Connector所连接的远程端的ClientID
	 */
	private ClientID storedClientID;
	/**
	 * 为了使Connector满足可开关类的设计要求，而使用的同步机制实现帮手
	 */
	protected OpenCloseSupport ocs;

	public Connector() {
		ocs = new OpenCloseSupport(new OpenMethod(), new CloseMethod());
	}

	/**
	 * 这个锁用来保证Connector发送对象的时候，是一个一个发送的。
	 */
	private ReentrantLock sendLock = new ReentrantLock();

	/**
	 * 将一个对象给Connector，要求它把这个对象发送给Client。 对象message将立即发送。
	 * 
	 * @param message
	 *            传给Connector的对象
	 * @throws FailedToSendMessageException
	 */
	public void sendMessage(Object message) throws FailedToSendMessageException {
		if (null == message) {
			throw new NullPointerException();
		}

		sendLock.lock();
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			sendMessageToClient(message);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new FailedToSendMessageException(ex);
		} finally {
			ocs.unlockMethod();
			sendLock.unlock();
		}
	}

	/**
	 * 这个方法将message对象发送给Client，根据具体网络技术不同应该有不同的实现
	 * 
	 * @throws Exception
	 */
	protected abstract void sendMessageToClient(Object message)
			throws Exception;

	/**
	 * 从Connector中取走它从它所连接的Client那里收到的对象。 多次调用这个方法，可以依次取出Client发送来的对象，先收
	 * 到的对象将先从这个方法返回。
	 * 
	 * 注意如果没有对象可以被取回，这个方法将会引起线程阻塞， 直到有对象可以被取回时，才返回这个对象。
	 * 
	 * 关闭后，如果有剩余信息没有取出，则仍然可以调用这个方法。
	 * 但是如果信息已经被取光，再调用这个方法就会抛出AlreadyClosedException。
	 * 
	 * return Connector所接收到的Client发送过来的对象。如果已经 取走了全部的对象，则返回null
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
	 * 
	 * 检测是否有等待取出的信息
	 * 
	 */
	public boolean hasMessageToRetrieve() {
		return !receivingQueue.isEmpty();
	}

	/**
	 * 根据具体网络技术实现的接收对象的方法 从Client那里收到一个对象，如果此时没有对象发送过来，
	 * 则调用该方法的线程进入被阻塞直到收到对方发送来的对象。 这个方法根据具体网络技术不同应该有不同的实现。
	 * 
	 * 如果所连接的远程端的客户断开连接，则本地这边应该抛出ClientDisconnectException。
	 * 当Connector接收到这个异常时将会自动关闭
	 * 
	 * @return Client发送来的一个对象
	 * @throws Exception
	 */
	protected abstract Object receiveMessageFromClient() throws Exception;

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

	protected abstract void closeImpl() throws Exception;

	private class OpenMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			if (receivingThread != null) {
				if (!receivingThread.isDone()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}

			storedClientID = null;
			openImpl();
			receivingQueue.open();
			receivingThread = THREAD_POOL.submit(new ReceivingThreadTask());
		}

	}

	private class CloseMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			try {
				closeImpl();
			} finally {
				storedClientID = null;
				receivingQueue.close();
			}

		}

	}

	/**
	 * 到这个Connector所连接的Client的ClientID。 这个方法将会调用getClientIDImpl()来得到ClientID
	 * 
	 * 注意返回的应该是这个Connector所连接的远程端的客户的ClientID， 而不是自己的ClientID
	 * 
	 * 如果这个Connector还没有打开，将返回null
	 */
	public ClientID getClientID() {
		if (!ocs.isOpen()) {
			return null;
		}

		if (null == storedClientID) {
			ocs.lockMethod();
			try {
				storedClientID = getClientIDImpl();
			} finally {
				ocs.unlockMethod();
			}

			assert (storedClientID != null);
			if (null == storedClientID) {
				throw new NullPointerException(
						"getClientIDImpl() should not return null");
			}
		}

		return storedClientID;
	}

	/**
	 * 返回这个Connector所连接的Client的ClientID，保证只在打开的情况下调用
	 */
	protected abstract ClientID getClientIDImpl();

	// /////////////////////////////////////////////////////////////////////

	/**
	 * Connector中的线程，命名为输入线程所执行的任务， 它负责不断接受Client发送过来的对象，并将其放入 receivingQueue
	 */
	private class ReceivingThreadTask implements Runnable {
		private int exceptionCount = 0;

		public void run() {
			boolean exitAndClose = false;

			while (ocs.isOpen()) {
				try {
					Object message = null;

					message = receiveMessageFromClient();
					if (null == message) {
						assert (false);
						throw new NullPointerException(
								"receiveMessageFromClient() should not return null");
					}
					receivingQueue.enqueue(RemotePackage.newObject(
							getClientID(), message));

					exceptionCount = 0;
				} catch (ClientDisconnectException ex) {
					exitAndClose = true;
					break;
				} catch (Exception ex) {
					exceptionCount++;
					if (exceptionCount > 3) {
						exitAndClose = true;
					}

					ex.printStackTrace();
					break;
				}
			}

			try {
				if (exitAndClose) {
					close();
				}
			} catch (FailedToCloseException ex) {
				ex.printStackTrace();
			}

		}
	}

	// ///////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Connector)) {
			return false;
		}

		Connector other = (Connector) obj;

		// get ClientIDs
		ClientID selfClientID = null;
		ClientID otherClientID = null;
		boolean selfHasClientID = true;
		boolean otherHasClientID = true;

		selfClientID = getClientID();

		if (selfClientID == null) {
			selfHasClientID = false;
		}

		otherClientID = other.getClientID();

		if (otherClientID == null) {
			otherHasClientID = false;
		}

		// compare ClientIDs
		if (selfHasClientID && otherHasClientID) {
			return selfClientID.equals(otherClientID);
		} else if (!selfHasClientID && otherHasClientID) {
			return false;
		} else if (selfHasClientID && !otherHasClientID) {
			return false;
		} else {
			return true;
		}

	}

	@Override
	public int hashCode() {
		// get ClientIDs
		ClientID selfClientID = null;
		boolean selfHasClientID = true;

		selfClientID = getClientID();

		if (selfClientID == null) {
			selfHasClientID = false;
		}

		// get hashCode
		if (selfHasClientID) {
			return selfClientID.hashCode();
		} else {
			return 0; // 所有没有ClientID的Connector被视为相等
		}

	}

	@Override
	public int compareTo(Connector other) {
		if (null == other) {
			return 1;
		}

		// get ClientIDs
		ClientID selfClientID = null;
		ClientID otherClientID = null;
		boolean selfHasClientID = true;
		boolean otherHasClientID = true;

		selfClientID = getClientID();

		if (selfClientID == null) {
			selfHasClientID = false;
		}

		otherClientID = other.getClientID();

		if (otherClientID == null) {
			otherHasClientID = false;
		}

		// compare CliendIDs
		if (selfHasClientID && otherHasClientID) {
			return selfClientID.compareTo(otherClientID);
		} else if (!selfHasClientID && otherHasClientID) {
			return -1;
		} else if (selfHasClientID && !otherHasClientID) {
			return 1;
		} else {
			return 0;
		}
	}

}
