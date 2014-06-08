package com.accela.tetrixgame.conn;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.broadcaster.Broadcaster;
import com.accela.tetrixgame.conn.broadcaster.FailedToBroadcastMessageException;
import com.accela.tetrixgame.conn.connectionLauncher.ConnectionLauncher;
import com.accela.tetrixgame.conn.connectionLauncher.FailedToLaunchConnectionException;
import com.accela.tetrixgame.conn.connectionReceiver.ConnectionReceiver;
import com.accela.tetrixgame.conn.connector.Connector;
import com.accela.tetrixgame.conn.connector.FailedToSendMessageException;
import com.accela.tetrixgame.conn.connectorCreator.ConnectorCreator;
import com.accela.tetrixgame.conn.connectorCreator.FailedToCreateConnectorException;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.ClosableBlockingQueue;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.IOpenClosable;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.OpenCloseSupport;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.TargetMethod;

/**
 * ConnectionCenter模块的入口
 * 
 * 连接中心。管理网络信息交互，连接等等。 你可以把连接中心看成两大部分，一部分管理和指定Client的连接以及信息发送、接受，
 * 另一部分管理对于广播的发送和接收。连接中心支持多线程。
 * 
 * 前半部分的功能： 1、打开和关闭对Client的连接的监听。如果打开，那么当有Client试图连接到这里时，就会建立和
 * 它的连接，这个连接ConnectionCenter会帮你管理。关闭后，新的Client不再能发起到这里的连接。
 * 2、发起到其他Client的连接。当连接成功时，就会生成一个连接，ConnectionCenter会帮助你管理。
 * 3、当你已经建立和某个Client的连接后，你就可以向它发送信息了。需要使用ClientID指出给谁发送 信息。
 * 4、各个Client向你发送的信息会被自动储存起来，你可以取出来。 5、你可以关闭和某个Client的连接
 * 
 * 后半部分的功能： 1、打开和关闭广播功能。当你打开的时候，广播信息才会被接收和存储，同时你才可以发送广播。 2、你可以取出接收到的广播信息
 * 3、你可以发出广播
 * 
 * 其他功能： 1、打开ConnectionCenter。你需要先打开ConnectionCenter，然后才能够使用它
 * 2、关闭ConnectionCenter。一旦关闭，多数方法将不能够使用，线程将被退出。但是你可以重新启动ConnectionCenter。
 * 
 * 一些注意事项： 1、如果你打开接收信息的功能，不论是Client发送过来的信息，还是广播信息，都会被存储，如果你不把它们取出来，它们将永远不会
 * 被释放，越堆越多。这有可能造成内存溢出。 2、尽量不要继承ConnectionCenter，即使继承，也不要添加新的方法，以免新添加的方法扰乱同步和
 * 快关机制
 * 
 * 注意，ConnectionCenter被设计成为可开关类（具体定义见com.accela.tetrixgame.synchronizeSupport.
 * standardSupport.OpenCloseSupport）
 * 如果你继承这个类，在添加新的方法的时候，请参照相关的可开关类的设计标准。ConnectionCenter所使用的OpenCloseSupport对象
 * 已经被设计成protected类型，可供使用。
 * 
 * ConnectionCenter被设计成独立于具体的网络技术，你需要使用一种具体的网络技术
 * 来实现后，才能使用。默认的使用Socket技术的连接中心是类SocketConnectionCenter。 你可以直接使用。
 * 
 * //TODO 同步方面还是有一些问题，比如如果一个要个Connector 发信息的函数在检出Connector后，另一个函数把这个Connector
 * 关闭了。尽管各个组件都是同步的，在它们上面建立的许多函数未 必能同步，不过异常机制可以保证这种情况会通过异常来返回
 * 
 */
public class ConnectionCenter implements IOpenClosable {
	/**
	 * ConnectionCenter的组件之一，负责创建Connector
	 */
	private ConnectorCreator connectorCreator;
	/**
	 * ConnectionCenter的组件之一，负责接收外界的连接
	 */
	private ConnectionReceiver connectionReceiver;
	/**
	 * ConnectionCenter的组件之一，负责发起连接
	 */
	private ConnectionLauncher connectionLauncher;
	/**
	 * ConenctionCenter的组件之一，负责广播功能
	 */
	private Broadcaster broadcaster;
	/**
	 * 用来存储ConnectionCenter的组件之一——Connector（Connector用来管理一个连接)。
	 * 这个数据结构用所存储的Connector的所连接的客户的ClientID来作为键
	 */
	private Map<ClientID, Connector> connectors = new ConcurrentHashMap<ClientID, Connector>();
	/**
	 * receivingThread是由线程池分配的，用来管理它的Future对象保存在这里，
	 * 并通过对应Connector所连接的客户的ClientID来作为键，以求和所服务的 Connector一一对应。
	 */
	private Map<ClientID, Future<?>> receivingThreads = new ConcurrentHashMap<ClientID, Future<?>>();
	/**
	 * 存储和管理receivingThread的线程池
	 */
	private static final ExecutorService RECEIVING_THREAD_POOL = Executors
			.newCachedThreadPool();
	/**
	 * 从Connector收到的信息将会装入这个队列，先收到的信息先装入
	 */
	private ClosableBlockingQueue<RemotePackage> receivingQueue = new ClosableBlockingQueue<RemotePackage>();
	/**
	 * receiveingQueue的最大大小，如果接受的信息过多， 则自动移除最早接收的信息
	 */
	private int receivingQueueMaxSize = 150;
	/**
	 * 一个不断根据ConnectionReceiver提供的信息，建立新的连接的线程
	 */
	private ConnectionReceiverThread connectionReceiverThread;
	/**
	 * 一个不断将broadcaster中收到的信息装入broadcasterReceivingQueue的线程
	 */
	private BroadcasterReceivingThread broadcasterReceivingThread;
	/**
	 * broadcaster中收到的信息将会被装入这个队列
	 */
	private ClosableBlockingQueue<RemotePackage> broadcasterReceivingQueue = new ClosableBlockingQueue<RemotePackage>();
	/**
	 * broadcasterReceivingQueue的大小上限，如果接受的信息过多， 则自动移除最早接收的信息
	 */
	private int broadcasterReceivingQueueMaxSize = 150;
	/**
	 * 负责自动清除被关闭的Connector的线程
	 */
	private ConnectorSweeperThread connectorSweeperThread;
	/**
	 * 为了使增加和删除Connector的方法同步满足同步需求，需要使用这个锁
	 */
	private ReentrantLock addRemoveConnectorLock = new ReentrantLock();
	/**
	 * 为了使ConnectionCenter满足可开关类的设计要求，而使用的同步机制实现帮手
	 */
	protected OpenCloseSupport ocs;
	/**
	 * 设定最大连接数的限制，为负数表示不限制连接数。为了方便计算连接数，
	 * 计算连接数的时候，直接使用connectors.size()，即把那些已经断开，但 是还未被清除的连接也算上了。
	 */
	private int maxConnectionNum = -1; // TODO 限制连接数的功能还没有测试

	/**
	 * 利用ConnectionCenterInitializer新建ConnectionCenter
	 * 
	 * @param initializer
	 */
	public ConnectionCenter(ConnectionCenterInitializer initializer) {
		if (null == initializer) {
			throw new NullPointerException("initializer should not be null");
		}

		ocs = new OpenCloseSupport(new OpenMethod(), new CloseMethod());
		construct(initializer);

	}

	public ClientID getMyAddress() {
		return this.connectionReceiver.getMyAddress();
	}

	/**
	 * 初始化方法，只在构造方法里调用
	 * 
	 * @param initializer
	 */
	private void construct(ConnectionCenterInitializer initializer) {
		assert (!ocs.isOpen());
		assert (initializer != null);

		ConnectorCreator connectorCreator = initializer
				.specifyConnectorCreator();
		if (null == connectorCreator) {
			throw new NullPointerException(
					"connectorCreator should not be null in ConnectionCenterInitializer");
		}
		this.connectorCreator = connectorCreator;

		ConnectionReceiver connectionReceiver = initializer
				.specifyConnectionReceiver();
		if (null == connectionReceiver) {
			throw new NullPointerException(
					"connectionReceiver should not be null in ConnectionCenterInitializer");
		}
		this.connectionReceiver = connectionReceiver;

		ConnectionLauncher connectionLauncher = initializer
				.specifyConnectionLauncher();
		if (null == connectionLauncher) {
			throw new NullPointerException(
					"connectionLauncher should not be null in ConnectionCenterInitializer");
		}
		this.connectionLauncher = connectionLauncher;

		Broadcaster broadcaster = initializer.specifyBroadcaster();
		if (null == broadcaster) {
			throw new NullPointerException(
					"broadcaster should not be null in ConnectionCenterInitializer");
		}
		this.broadcaster = broadcaster;

	}

	// /////////////////////////管理对Client连接的监听ConnectionReceiver///////////////////////////
	/**
	 * 开始监听Client的连接。 调用这个方法后， 如果有Client请求连接到ConenctioinCenter，就会被监听到，然后 建立连接。
	 * 
	 * @throws FailedToOpenException
	 *             如果打开的过程中遇到异常
	 */
	public void startToReceiveConnection() throws FailedToOpenException {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			connectionReceiver.open();

			connectionReceiverThread = new ConnectionReceiverThread(
					connectionReceiver);
			connectionReceiverThread.start();
		} finally {
			ocs.unlockMethod();
		}

	}

	/**
	 * 停止监听Client的连接，所有已经向本地发起连接但还没有建立连接的Client 在停止后不会被继续建立连接
	 * 
	 * @throws FailedToCloseException
	 *             如果关闭的过程中遇到异常
	 */
	public void stopToReceiveConnection() throws FailedToCloseException {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			connectionReceiver.close();
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * @return 是否正在监听Client的连接
	 */
	public boolean isReceivingConnection() {
		if (!ocs.isOpen()) {
			assert (!connectionReceiver.isOpen());
			return false;
		}

		return connectionReceiver.isOpen();
	}

	/**
	 * @return 如果其他客户需要连接到这个ConnectionCenter上，应该 通过这个方法返回的ClientID。
	 *         这个方法返回的实际上是监听连接的模块，所在的ClientID
	 */
	public ClientID getConnectionReceivingClientID() {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			return connectionReceiver.getConnectionReceivingClientID();
		} finally {
			ocs.unlockMethod();
		}

	}

	// ///////////////////////////发起连接ConnectionLauncher////////////////////////////

	/**
	 * 一条打开通向ClientID所指定的Client的连接。打开连接后就可以向这个Client发送信息了
	 * 
	 * @return 如果过去已经打开过这个连接，且没有关闭，则返回false，因为此种情况下这个方法
	 *         不会做什么事并返回false。如果没有可用的连接，则这个方法就会新建一个，并返回true
	 * 
	 * @throws 如果打开过程中出现异常
	 */
	public boolean openConnection(ClientID clientID)
			throws ConnectionFullException, FailedToCreateConnectionException {
		if (null == clientID) {
			throw new NullPointerException("clientID should not be null");
		}

		addRemoveConnectorLock.lock();
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			if (hasConnectionOf(clientID)) {
				return false;
			}

			createConnection(connectionLauncher.launchConnection(clientID));
			return true;
		} catch (FailedToCreateConnectorException ex) {
			throw new FailedToCreateConnectionException(ex);
		} catch (ConnectionFullException ex) {
			throw ex;
		} catch (FailedToLaunchConnectionException ex) {
			throw new FailedToCreateConnectionException(ex);
		} catch (FailedToOpenException ex) {
			throw new FailedToCreateConnectionException(ex);
		} finally {
			ocs.unlockMethod();
			addRemoveConnectorLock.unlock();
		}

	}

	// ////////////////////////////关闭连接////////////////////////////////

	/**
	 * 关闭通向ClientID所指定的Client的连接。会把缓冲区中的剩下的信息接收完毕后才停止。
	 * ClientID指定一个远程端的客户，本地有连接通向它，调用这个方法将关闭这个连接。
	 * 
	 * 如果所指定的连接，远程端的客户已经关闭了，那么返回false
	 */
	public boolean closeConnection(ClientID clientID)
			throws FailedToCloseException {
		if (null == clientID) {
			throw new NullPointerException();
		}

		addRemoveConnectorLock.lock();
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			if (!hasConnectionOf(clientID)) {
				return false;
			}

			destroyConnection(clientID);
			return true;
		} finally {
			ocs.unlockMethod();
			addRemoveConnectorLock.unlock();
		}

	}

	// //////////////////////////////发送和取出信息Connector///////////////////////////////

	/**
	 * 取出ConnectionCenter收到的信息，如果此时没有信息则会阻塞。
	 * 当ConnectionCenter已经关闭后，你可以继续调用这个方法来取 出剩余的信息。当没有剩余的信息时，再调用这个方法就会抛出
	 * AlreadyClosedException。
	 */
	public RemotePackage retriveMessage() {
		ocs.lockMethod();

		try {
			if (!ocs.isOpen() && !hasMessageToRetrieve()) // TODO
			// 这里在关闭后，还是有可能是的两条线程都通过，但只有一个消息包，从而造成一个线程阻塞
			{
				ocs.ensureOpen();
			}

			return receivingQueue.dequeue();
		} finally {
			ocs.unlockMethod();
		}

	}

	/**
	 * 返回retrieveMessage()此时是否还有可以取回的信息
	 */
	public boolean hasMessageToRetrieve() {
		return !receivingQueue.isEmpty();
	}

	/**
	 * 发送信息，RemotePackage中的ClientID将会指出信息应该发送给哪个Client。
	 * RemotePackage中的ClientID指向要发送给的远程端。
	 * 
	 * @throws FailedToSendMessageException
	 */
	public void sendMessage(RemotePackage remotePackage)
			throws FailedToSendMessageException {
		if (null == remotePackage) {
			throw new NullPointerException("remotePackage should not be null");
		}
		if (null == remotePackage.getClientID()) {
			throw new NullPointerException(
					"remotePackage.clientID should not be null");
		}
		if (null == remotePackage.getMessage()) {
			throw new NullPointerException(
					"remotePackage.message should not be null");
		}

		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			sendMessage(remotePackage.getClientID(), remotePackage.getMessage());

			RemotePackage.deleteObject(remotePackage);
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 发送信息
	 * 
	 * @param clientID
	 *            ClientID指向要发送给的远程端。指出信息将发送给哪个Client
	 * @param message
	 *            要发送的信息
	 */
	public void sendMessage(ClientID clientID, Object message)
			throws FailedToSendMessageException,
			ConnectionDoesNotExistException {
		if (null == clientID) {
			throw new NullPointerException("clientID should not be null");
		}
		if (null == message) {
			throw new NullPointerException("message should not be null");
		}
		if (!hasConnectionOf(clientID)) {
			throw new ConnectionDoesNotExistException(
					"this client specified by clientID is not connected");
		}

		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			Connector connector = connectors.get(clientID);
			if (null == connector) {
				throw new ConnectionDoesNotExistException(
						"this client specified by clientID is not connected");
			}

			connector.sendMessage(message);
		} finally {
			ocs.unlockMethod();
		}
	}

	// /////////////////////////查询ClientID////////////////////////////

	/**
	 * @return 是否已经和clientID指定的Client建立了连接，并且这个连接没有被关闭
	 */
	public boolean hasConnectionOf(ClientID clientID) {
		if (null == clientID) {
			return false;
		}
		if (!ocs.isOpen()) {
			assert (!connectors.containsKey(clientID) || !connectors.get(
					clientID).isOpen());
			return false;
		}

		return connectors.containsKey(clientID)
				&& connectors.get(clientID).isOpen();
	}

	/**
	 * @return 所有已经建立了连接，且连接没有被关闭的Client的ClientID
	 */
	public List<ClientID> getConnectedClientIDs() {
		if (!ocs.isOpen()) {
			return Collections.unmodifiableList(new LinkedList<ClientID>());
		}

		List<ClientID> list = new LinkedList<ClientID>();

		Set<ClientID> clientIDs = connectors.keySet();
		for (ClientID clientID : clientIDs) {
			assert (clientID != null);
			assert (connectors.get(clientID).getClientID() == null || connectors
					.get(clientID).getClientID().equals(clientID));
			if (clientID != null && hasConnectionOf(clientID)) {
				list.add(clientID);
			}
		}

		return Collections.unmodifiableList(list);
	}

	// ///////////////////////////////管理广播Broadcaster///////////////////////////////////

	/**
	 * 打开广播功能。只有打开广播功能后，才能够接收广播信息和发送广播信息
	 * 
	 * @throws FailedToOpenException
	 */
	public void openBroadcastFunction() throws FailedToOpenException {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			broadcaster.open();
			broadcasterReceivingThread = new BroadcasterReceivingThread(
					broadcaster);
			broadcasterReceivingThread.start();
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 关闭广播功能
	 * 
	 * @throws FailedToCloseException
	 */
	public void closeBroadcastFunction() throws FailedToCloseException {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			broadcaster.close();
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * @return 返回广播功能是否已经打开
	 */
	public boolean isBroadcastFunctionOpen() {
		if (!ocs.isOpen()) {
			assert (!broadcaster.isOpen());
			return false;
		}

		return broadcaster.isOpen();
	}

	/**
	 * 取出收到的广播信息，如果此时没有信息则会阻塞。 当ConnectionCenter已经关闭后，你可以继续调用这个方法来取
	 * 出剩余的信息。当没有剩余的信息时，再调用这个方法就会抛出 AlreadyClosedException。
	 */
	public RemotePackage retriveBroadcastMessage() {
		ocs.lockMethod();

		try {
			if (!ocs.isOpen() && !hasBroadcastMessageToRetrieve()) {
				ocs.ensureOpen();
			}

			return broadcasterReceivingQueue.dequeue();
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 返回retrieveBroadcastMessage()此时是否还有可以取回的信息
	 */
	public boolean hasBroadcastMessageToRetrieve() {
		return !broadcasterReceivingQueue.isEmpty();
	}

	/**
	 * 将message广播一次
	 * 
	 * @throws FailedToBroadcastMessageException
	 */
	public void broadcastMessage(Object message)
			throws FailedToBroadcastMessageException {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			broadcaster.broadcastMessage(message);
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 调用方法后会很快返回，然后内部线程会每隔period指定的毫秒 时间将message广播一次
	 */
	public void broadcastMessage(Object message, long period) {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			broadcaster.broadcastMessage(message, period);
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * 终止当前ConnectionCenter中所有的广播
	 */
	public void cancelAllBroadcast() {
		ocs.lockMethod();

		try {
			ocs.isOpen();

			broadcaster.cancelAllBroadcast();
		} finally {
			ocs.unlockMethod();
		}
	}

	// ///////////////////////////////开关ConnectionCenter//////////////////////////////////

	/**
	 * 返回ConnectionCenter是否已经打开
	 */
	@Override
	public boolean isOpen() {
		return ocs.isOpen();
	}

	/**
	 * 只有先打开ConnectionCenter，你才能使用它
	 * 
	 * @throws FialedToOpenException
	 */
	@Override
	public void open() throws FailedToOpenException {
		ocs.open();
	}

	/**
	 * 关闭ConnectionCenter。停止接收信息，停止广播，清空存储的信息和缓冲区， 关闭所有连接，关闭所有内部线程。
	 * 关闭时，所有在其他方法中阻塞的线程会被中断以便退出。关闭后，所有方法都不可用，除了
	 * retriveMessage()方法和retriveBroadcastMessage()方法。这两个方法可以继续使用，以便
	 * 取出ConnectionCenter中剩余的信息，但是如果信息取光了，则再调用这两个方法就会抛出
	 * FailedToCloseException异常。
	 * 
	 * @throws FailedToCloseException
	 */
	@Override
	public void close() throws FailedToCloseException {
		ocs.close();
	}

	private class OpenMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			for (Future<?> task : receivingThreads.values()) {
				assert (task != null);
				if (!task.isDone()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}
			if (connectionReceiverThread != null) {
				if (connectionReceiverThread.isAlive()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}
			if (broadcasterReceivingThread != null) {
				if (broadcasterReceivingThread.isAlive()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}
			if (connectorSweeperThread != null) {
				if (connectorSweeperThread.isAlive()) {
					throw new IllegalStateException(
							"the inner thread has not been exited yet, please wait");
				}
			}

			connectors.clear();
			receivingThreads.clear();
			receivingQueue.open();
			broadcasterReceivingQueue.open();
			connectorSweeperThread = new ConnectorSweeperThread();
			connectorSweeperThread.start();

		}

	}

	private class CloseMethod implements TargetMethod {

		@Override
		public void run() throws Exception {
			// 关闭组件
			if (connectionReceiver.isOpen()) {
				connectionReceiver.close();
			}
			if (broadcaster.isOpen()) {
				broadcaster.close();
			}

			// 关闭Connector
			for (ClientID client : connectors.keySet()) {
				assert (client != null);
				if (client != null) {
					uncheckedDestroyConnection(client);
				}
			}

			// 清空缓冲区
			connectors.clear();
			receivingQueue.close();
			broadcasterReceivingQueue.close();

		}

	}

	/**
	 * 返回ConnectionCenter用来存储从连接中接收到的信息包的缓冲区的最大容量。
	 * 如果接收到的信息包放不下，ConnectionCenter将自动移除最早接收到的包
	 */
	public int getMessageBufferMaxSize() {
		return receivingQueueMaxSize;
	}

	/**
	 * 设定ConnectionCenter用来存储从连接中接收到的信息包的缓冲区的最大容量。
	 * 如果接收到的信息包放不下，ConnectionCenter将自动移除最早接收到的包
	 */
	public void setMessageBufferMaxSize(int messageBufferMaxSize) {
		if (messageBufferMaxSize <= 0) {
			throw new IllegalArgumentException(
					"messageBufferMaxSize should be positive");
		}

		this.receivingQueueMaxSize = messageBufferMaxSize;
	}

	/**
	 * 返回ConnectionCenter能够保存的最大的接收到的广播信息包的数量， 当接收到过多的广播信息包的时候，最早接收的包会被自动移除
	 */
	public int getBroadcastMessageBufferMaxSize() {
		return broadcasterReceivingQueueMaxSize;
	}

	/**
	 * 设定ConnectionCenter能够保存的最大的接收到的广播信息包的数量， 当接收到过多的广播信息包的时候，最早接收的包会被自动移除
	 */
	public void setBroadcastMessageBufferMaxSize(
			int broadcastMessageBufferMaxSize) {
		if (broadcastMessageBufferMaxSize <= 0) {
			throw new IllegalArgumentException(
					"broadcastMessageBufferMaxSize should be positive");
		}

		this.broadcasterReceivingQueueMaxSize = broadcastMessageBufferMaxSize;
	}

	/**
	 * @return ConnectionCenter所允许的最大连接数
	 */
	public int getMaxConnectionNum() {
		return maxConnectionNum;
	}

	/**
	 * 设定最大连接数限制，对于已经连接上的客户，设置这个方法不能 将它们踢出。 将maxConnectionNum设定为为负数表示不限制连接数
	 */
	public void setMaxConnectionNum(int maxConnectionNum) {
		this.maxConnectionNum = maxConnectionNum;
	}

	// ///////////////////////////////////////////////////////////////////

	/**
	 * 新建一个Connector以及它的receivingThread。 启动它们，然后将它们加入相应的容器。
	 * 传入的NewConnectionInfo对象包含指向连接的远程端的连接信息。
	 * 
	 * 这个方法利用hasConnectionOf检测是否会建立重复的连接
	 * 
	 * @param info
	 *            新建这个Connector所必须的信息
	 * @return 新建的Connector
	 * 
	 */
	private void createConnection(NewConnectionInfo info)
			throws FailedToCreateConnectorException, FailedToOpenException,
			ConnectionFullException {
		if (null == info) {
			throw new NullPointerException("info should not be null");
		}

		addRemoveConnectorLock.lock();
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			Connector connector = connectorCreator.createConnector(info);
			if (hasConnectionOf(connector.getClientID())) {
				throw new IllegalArgumentException(
						"Trying to connect to a connected client: "
								+ connector.getClientID().toString());
			}
			Runnable threadTask = new ReceivingThreadTask(connector);

			connector.open();
			Future<?> future = RECEIVING_THREAD_POOL.submit(threadTask);

			connectors.put(connector.getClientID(), connector);
			Common.LOG.info("ConnectionCenter put connector: "
					+ connector.getClientID());
			receivingThreads.put(connector.getClientID(), future);

			// 检测是否已经超过了最大连接数限制
			// TODO 这个自动限制连接数的功能还没有测试过
			if (maxConnectionNum >= 0 && connectors.size() > maxConnectionNum) {
				try {
					uncheckedDestroyConnection(connector.getClientID());
				} catch (FailedToCloseException ex) {
					// there is nothing can do here
					System.err
							.println("Connection is full and failed to close the exceeded connection");
					ex.printStackTrace();
				}

				throw new ConnectionFullException(
						"Connection num is exceeding the limit: "
								+ maxConnectionNum);
			}

		} finally {
			ocs.unlockMethod();
			addRemoveConnectorLock.unlock();
		}
	}

	/**
	 * 关闭通向ClientID所指定的Client的连接。会把缓冲区中的剩下的信息接收完毕后才停止。
	 * ClientID指定一个远程端的客户，本地有连接通向它，调用这个方法将关闭这个连接。
	 * 
	 * 无论通过clientID所指定的Connector是否已经关闭，这个方法都能够删除它
	 */
	private void destroyConnection(ClientID clientID)
			throws FailedToCloseException {
		if (null == clientID) {
			throw new NullPointerException("clientID should not be null");
		}

		addRemoveConnectorLock.lock();
		ocs.lockMethod();

		try {
			ocs.ensureOpen();

			uncheckedDestroyConnection(clientID);

		} finally {
			ocs.unlockMethod();
			addRemoveConnectorLock.unlock();
		}

	}

	private void uncheckedDestroyConnection(ClientID clientID)
			throws FailedToCloseException {
		if (null == clientID) {
			throw new NullPointerException("clientID should not be null");
		}

		addRemoveConnectorLock.lock();
		try {
			Connector connector = connectors.remove(clientID);
			if (null == connector) {
				throw new ConnectionDoesNotExistException(
						"Trying to disconnect from a not connected client: "
								+ clientID.toString());
			}

			Future<?> future = receivingThreads.remove(clientID);
			assert (future != null);

			if (connector.isOpen()) {
				connector.close();
			}

		} finally {
			addRemoveConnectorLock.unlock();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 不断把Connector接收到的信息放入receivingQueue的线程，每个Connector都会对应一个，
	 * 命名为receivingThread。这个是它执行的任务
	 */
	private class ReceivingThreadTask implements Runnable {
		private Connector aimConnector;

		public ReceivingThreadTask(Connector aimConnector) {
			assert (aimConnector != null);
			if (null == aimConnector) {
				throw new NullPointerException(
						"aimConnector should not be null");
			}
			this.aimConnector = aimConnector;
		}

		public void run() {
			assert (aimConnector.isOpen());

			while (aimConnector.isOpen() || aimConnector.hasMessageToRetrieve()) {
				try {
					RemotePackage message = aimConnector.retrieveMessage();
					if (null == message) {
						assert (false);
						throw new NullPointerException();
					}

					assert (receivingQueueMaxSize >= 1);
					while (receivingQueue.size() > receivingQueueMaxSize - 1) {
						RemotePackage.deleteObject(receivingQueue.dequeue());
					}

					receivingQueue.enqueue(message);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	/**
	 * 
	 * 这个线程检测ConnectionCenter中存储的已经关闭的Connector，并删除之
	 * 
	 */
	private class ConnectorSweeperThread extends Thread {
		private static final long SWEEPING_INTERVAL = 5000;

		public ConnectorSweeperThread() {
			super("ConnectionCenter - ConnectorSweeperThread");
		}

		public void run() {
			while (ocs.isOpen()) {
				try {
					for (ClientID clientID : connectors.keySet()) {
						assert (clientID != null);
						assert (connectors.get(clientID).getClientID() == null || connectors
								.get(clientID).getClientID().equals(clientID));
						if (clientID != null
								&& !connectors.get(clientID).isOpen()) {
							destroyConnection(clientID);
							Common.LOG.info("ConnectionCenter - "
											+ "ConnectorSweeperThread has removed a connector: "
											+ clientID);

						}
					}

					Thread.sleep(SWEEPING_INTERVAL);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 一个不断根据ConnectionReceiver提供的信息，建立新的连接的线程
	 */
	private class ConnectionReceiverThread extends Thread {
		private ConnectionReceiver connectionReceiver;

		public ConnectionReceiverThread(ConnectionReceiver connectionReceiver) {
			super("ConnectionCenter - ConnectionReceiverThread");

			assert (connectionReceiver != null);
			if (null == connectionReceiver) {
				throw new NullPointerException(
						"connectionReceiver should not be null");
			}

			this.connectionReceiver = connectionReceiver;
		}

		public void run() {
			while (connectionReceiver.isOpen()
					|| connectionReceiver.hasNewConnectionInfoToRetrieve()) {
				try {
					NewConnectionInfo info = connectionReceiver
							.retriveNewConnectionInfo();
					if (null == info) {
						assert (false);
						throw new NullPointerException();
					}
					createConnection(info);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}

	}

	/**
	 * 一个不断将broadcaster中收到的信息装入broadcasterReceivingQueue的线程
	 */
	private class BroadcasterReceivingThread extends Thread {
		private Broadcaster broadcaster;

		public BroadcasterReceivingThread(Broadcaster broadcaster) {
			super("ConnectionCenter - BroadcasterReceivingThread");

			assert (broadcaster != null);
			if (null == broadcaster) {
				throw new NullPointerException("broadcaster should not be null");
			}

			this.broadcaster = broadcaster;
		}

		public void run() {
			while (broadcaster.isOpen() || broadcaster.hasMessageToRetrieve()) {
				try {
					RemotePackage message = broadcaster.retrieveMessage();
					if (null == message) {
						assert (false);
						throw new NullPointerException();
					}

					assert (broadcasterReceivingQueueMaxSize >= 1);
					while (broadcasterReceivingQueue.size() > broadcasterReceivingQueueMaxSize - 1) {
						RemotePackage.deleteObject(broadcasterReceivingQueue
								.dequeue());
					}

					broadcasterReceivingQueue.enqueue(message);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}

	}

	/**
	 * 该方法将ConnectionCenter内部组件暴露给类的继承者， 类的继承者应该只读取而不更改，否则可能引起错误
	 */
	protected ConnectorCreator getConnectorCreator() {
		return connectorCreator;
	}

	/**
	 * 该方法将ConnectionCenter内部组件暴露给类的继承者， 以便读取某些属性。 类的继承者应该只读取而不更改，否则可能引起错误。
	 */
	protected ConnectionReceiver getConnectionReceiver() {
		return connectionReceiver;
	}

	/**
	 * 该方法将ConnectionCenter内部组件暴露给类的继承者， 以便读取某些属性。 类的继承者应该只读取而不更改，否则可能引起错误。
	 */
	protected ConnectionLauncher getConnectionLauncher() {
		return connectionLauncher;
	}

	/**
	 * 该方法将ConnectionCenter内部组件暴露给类的继承者， 以便读取某些属性。 类的继承者应该只读取而不更改，否则可能引起错误。
	 */
	protected Broadcaster getBroadcaster() {
		return broadcaster;
	}

	/**
	 * 该方法将ConnectionCenter内部组件暴露给类的继承者， 以便读取某些属性。 类的继承者应该只读取而不更改，否则可能引起错误。
	 */
	protected Map<ClientID, Connector> getConnectors() {
		return Collections.unmodifiableMap(connectors);
	}

}

// WARNING: 注意不应该把任何线程改成守护线程，因为组件关闭后仍然有发送剩余信息等任务
