package com.accela.tetrixgame.conn.connectionReceiver;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.ClosableBlockingQueue;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.IOpenClosable;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.OpenCloseSupport;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.TargetMethod;

/**
 * 
 * 我假定的新建一个连接的流程是 1、ConnectionReceiver不断监听是否有Client连接
 * 2、当监听到Client连接后，生成NewConnectionInfo 3、通过NewConnectionInfo创建一个Connector
 * 4、这个Connector代表和Client的连接，可以互相通信
 * 
 * ConnectionReceiver就是用来监听是否有Client连接的。它被设 计成独立与具体网络技术。在具体的网络技术中，需要用它的子类
 * 来完成实际的功能。
 * 
 * 这个类的继承者将会用具体的网络技术实现它。子类会实现监听Client 的功能，当监听到Client的连接请求并且子类要接受连接的时候时，子
 * 类需要生成NewConnectionInfo，然后调用acceptConnection方
 * 法。方法的返回值是一个可以用来给本地发送信息的IMessageSender。
 * 此后根据网络技术的不同，子类可能需要将IMessageSender对象传给Client。 到这里，这个类就已经完成其全部预设的功能了。
 * 
 * 注意，ConnectionReceiver被设计成为可开关类（具体定义见com.accela.tetrixgame.synchronizeSupport.
 * standardSupport.OpenCloseSupport） 如果你继承这个类，在添加新的方法的时候，请参照相关的可开关类的设计标准。
 * ConnectionReceiver所使用的OpenCloseSupport对象 已经被设计成protected类型，可供使用。
 * 
 */
public abstract class ConnectionReceiver implements IOpenClosable {
	/**
	 * 存放新连接信息的队列
	 */
	private ClosableBlockingQueue<NewConnectionInfo> infoQueue = new ClosableBlockingQueue<NewConnectionInfo>();
	/**
	 * 监听Client的连接的线程
	 */
	private DetectingThread detectingThread;
	/**
	 * 为了使ConnectionReceiver满足可开关类的设计要求，而使用的同步机制实现帮手
	 */
	protected OpenCloseSupport ocs;

	public ConnectionReceiver() {
		ocs = new OpenCloseSupport(new OpenMethod(), new CloseMethod());
	}

	/**
	 * 
	 * 根据具体的网络技术实现的监听Client的连接的方法。当有Client要求连接
	 * 的时候，返回NewConnectionInfo。如果此时没有Client连接，应该引起线 程阻塞。
	 * 
	 * @return 当有Client要求连接的时候，返回NewConnectionInfo，记录建立连接的必要信息
	 * 
	 * @throws Exception
	 */
	protected abstract NewConnectionInfo detectingConnection() throws Exception;

	/**
	 * 
	 * 取出NewConnectionInfo，如果没有，则阻塞直到有。
	 * 如果ConnectionReceiver已经被关闭，则当NewConnectionInfo还没有被
	 * 取光的时候，还可以调用这个方法，当NewConnectionInfo已经被取光后，
	 * 再调用这个方法将会抛出AlreadyClosedException。
	 */
	public NewConnectionInfo retriveNewConnectionInfo() {
		ocs.lockMethod();
		try {
			if (!ocs.isOpen() && !hasNewConnectionInfoToRetrieve()) {
				ocs.ensureOpen();
			}

			return infoQueue.dequeue();
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 检测是还有NewConnectionInfo没有取出
	 */
	public boolean hasNewConnectionInfoToRetrieve() {
		return !infoQueue.isEmpty();
	}

	@Override
	public boolean isOpen() {
		return ocs.isOpen();
	}

	@Override
	public void open() throws FailedToOpenException {
		ocs.open();
	}
	
	
	public ClientID getMyAddress(){
		ocs.ensureOpen();
		return getMyAddressImpl();
	}
	
	protected abstract ClientID getMyAddressImpl();

	protected abstract void openImpl() throws Exception;

	@Override
	public void close() throws FailedToCloseException {
		ocs.close();
	}

	protected abstract void closeImpl() throws Exception;

	private class OpenMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			if (detectingThread != null) {
				if (detectingThread.isAlive()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}

			openImpl();
			infoQueue.open();
			detectingThread = new DetectingThread();
			detectingThread.start();

		}

	}

	private class CloseMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			try {
				closeImpl();
			} finally {
				infoQueue.close();
			}

		}

	}

	/**
	 * @return 返回一个ClientID，这个ClientID指明如何连接
	 *         到ConnectionReceiver。在ConnectionCenter中的
	 *         openConnection方法中使用这个ClientID将会连接到
	 *         ConnectionReceiver上，从而获得与相应ConnectionCenter 的连接。
	 */
	public ClientID getConnectionReceivingClientID() {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			ClientID clientID = getConnectionReceivingClientIDImpl();

			assert (clientID != null);
			return clientID;
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * getConnectionReceivingClientID()的实现方法，保证只在打开的情况下调用
	 */
	protected abstract ClientID getConnectionReceivingClientIDImpl();

	// //////////////////////////////////////////////////////////////////////

	/**
	 * 监听Client连接，并接受连接的线程
	 */
	private class DetectingThread extends Thread {
		public DetectingThread() {
			super("ConnectionReceiver - DetectingThread");
		}

		public void run() {
			while (ocs.isOpen()) {
				try {
					NewConnectionInfo info = null;

					info = detectingConnection();
					if (null == info) {
						assert (false);
						throw new NullPointerException(
								"detectingConnection() should not return null");
					}
					infoQueue.enqueue(info);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

}
