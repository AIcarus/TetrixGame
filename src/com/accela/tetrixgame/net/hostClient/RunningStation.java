package com.accela.tetrixgame.net.hostClient;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.accela.tetrixgame.conn.ConnectionCenter;
import com.accela.tetrixgame.conn.ConnectionDoesNotExistException;
import com.accela.tetrixgame.conn.connector.FailedToSendMessageException;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.ObjectStreams.HPObjectOutputStream;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyClosedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyOpenedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;
import com.accela.tetrixgame.net.basic.Engine;
import com.accela.tetrixgame.net.basic.EngineMirror;
import com.accela.tetrixgame.net.basic.UpdateMessage;

/**
 * 
 * 当在网络上玩Tetrix游戏的时候。网络工作的模式就是一个玩家的机子上会有一个
 * TetrixEngine实例和多个TetrixEngineMirror实例，TetrixEngine实例用来进行
 * 这个玩家的游戏，而TetrixEngineMirror用来播放其他玩家的游戏。另一方面，
 * TetrixEngine产生的游戏信息需要发送到网上去，而TetrixEngineMirror播放游戏
 * 时所需的信息又需要从网上取过来。这整个就构成了一个运行中的网络模式下的Tetrix 游戏的模型。
 * 
 * TetrixRunningStation就是实现这个模型的类。
 * 
 */
public class RunningStation {
	/**
	 * 用于连接这个TetrixRunningStation和统一场网络游戏中的其他主机上的TetrixRunningStation
	 * 的连接中心。通过这个对象来发送TetrixEngine产生的UpdateMessage，以及获得用于
	 * TetrixEgineMirror的播放的UpdateMessage。
	 */
	private ConnectionCenter connectionCenter;
	/**
	 * 存放一场网络Tetrix游戏中的那个TetrixEngine
	 */
	private Engine engine;
	/**
	 * 存放一场网络Tetrix游戏中的多个TetrixEngineMirror，每个TetrixEngineMirror
	 * 都和一个远程主机对应，因此用ClientID作为键
	 */
	private Map<ClientID, EngineMirror> mirrorMap;
	/**
	 * 记录这个TetrixRunningStation和哪些客户连接。用集合来 保证查询的常数时间
	 */
	private Set<ClientID> clients; // 用集合来保证查询速度

	private Map<ClientID, Integer> clientErrCount;
	/**
	 * 记录的信息同clients，是不过用List来装载以保证顺序性。
	 */
	private List<ClientID> clientList; // 用链表来保证次序的一定
	/**
	 * 如果有客户中途连接中断，就会被记录在这个集合中
	 */
	private Set<ClientID> invalidClients; // 用集合保证contains的速度

	private EngineSenderThread senderThread;

	private MirrorReceiverThread receiverThread;

	private boolean open = false;

	/**
	 * 是否这个TetrixRunningStation只有一个人参与。如果这样，TetrixRunningStation
	 * 就会关掉多余的线程，来提高速度。
	 */
	private boolean singlePlayer = false; // TODO 别忘了检查此时的线程数是否正确
	/**
	 * TetrixRunningStation也需要定时刷新，刷新周期同TetrixEngineMirror
	 */
	public static final long UPDATE_INTERVAL = Engine.UPDATE_INTERVAL;
	/**
	 * 记录TetrixRunningStation中TetrixEngine实例初始化所使用的TetrixGameAttributes
	 */
	private GameAttributes attr;
	/**
	 * 是否在收到EnforceDisconnectMessage后，就自动与相应Client断开连接
	 */
	private boolean enforceDisconnectable = false;

	/**
	 * 新建一个TetrixRunningStation实例
	 * 
	 * @param attr
	 *            初始化TetrixEngine和所有TetrixEngineMirror所用的TetrixGameAttributes
	 * @param connectionCenter
	 *            为获得与其他客户的连接而从外界传入的连接中心实例。
	 *            TetrixRunningStation通过查看connectionCenter中持有的连接来判断与谁建立游戏。
	 */
	public RunningStation(GameAttributes attr,
			ConnectionCenter connectionCenter) {
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}
		if (null == connectionCenter) {
			throw new NullPointerException(
					"connectionCenter should not be null");
		}
		if (!connectionCenter.isOpen()) {
			throw new IllegalArgumentException(
					"connectionCenter should be open");
		}
		if (connectionCenter.isReceivingConnection()) {
			throw new IllegalArgumentException(
					"connectionCenter should not be receivingConnections");
		}
		if (connectionCenter.isBroadcastFunctionOpen()) {
			throw new IllegalArgumentException(
					"connectionCenter's broadcast function should not be open");
		}

		this.attr = attr;

		// 接管connectionCenter
		this.connectionCenter = connectionCenter;

		// 生成客户列表
		List<ClientID> connectedClients = connectionCenter
				.getConnectedClientIDs();
		for (ClientID c : connectedClients) {
			assert (c != null);
		}
		clients = new HashSet<ClientID>(connectedClients);
		clients = Collections.unmodifiableSet(clients);
		assert (clients.size() == connectedClients.size());

		clientList = Collections.unmodifiableList(new LinkedList<ClientID>(
				clients));

		clientErrCount = new HashMap<ClientID, Integer>();

		// 初始化本地引擎和镜像引擎
		engine = new Engine(attr);

		mirrorMap = new HashMap<ClientID, EngineMirror>();
		for (ClientID c : clients) {
			assert (c != null);
			if (mirrorMap.containsKey(c)) {
				throw new IllegalArgumentException(
						"clients should not contain duplicated elements");
			}

			mirrorMap.put(c, new EngineMirror(attr));
		}
		mirrorMap = Collections.unmodifiableMap(mirrorMap);

		invalidClients = new HashSet<ClientID>();

		// 初始状态是关闭
		open = false;

	}

	public int getGameFieldHeight() {
		return engine.getGameFieldHeight();
	}

	public int getGameFieldWidth() {
		return engine.getGameFieldWidth();
	}

	public void open() throws FailedToOpenException {
		if (open) {
			throw new AlreadyOpenedException();
		}

		try {
			open = true;

			// 启动工作线程
			if (clients.size() - invalidClients.size() > 0) {
				senderThread = new EngineSenderThread();
				receiverThread = new MirrorReceiverThread();

				senderThread.start();
				receiverThread.start();
			} else {
				singlePlayer = true;

				if (demoOut != null) {
					senderThread = new EngineSenderThread();
					senderThread.start();
				} else {
					senderThread = null;
				}

				receiverThread = null;

				connectionCenter.close();
			}

			// 启动背景音乐
			if (Boolean.parseBoolean(attr
					.get(GameAttributes.ENABLE_SOUND))) {
				SoundManager.loopMusic();
			}

			// 记录录像中的用于启动游戏的属性表
			if (demoOut != null) {
				demoOut.writeObject(attr);
			}

		} catch (Exception ex) {
			open = false;

			throw new FailedToOpenException(ex);
		}
	}

	// 会关闭传入的ConnectionCenter
	public void close() throws FailedToCloseException {
		if (!open) {
			throw new AlreadyClosedException();
		}

		try {
			open = false;

			// 关闭背景音乐
			SoundManager.stopMusic();

			if (connectionCenter.isOpen()) {
				connectionCenter.close(); // 别人传入的connectionCenter，这里把它终止掉了，有点不合理
			}

		} finally {
			if (senderThread != null) {
				senderThread.interrupt();
			}
			if (receiverThread != null) {
				receiverThread.interrupt();
			}
			if (demoOut != null) {
				try {
					demoOut.close();
				} catch (IOException ex) {
					throw new FailedToCloseException(ex);
				}
			}
		}

	}

	public boolean isOpen() {
		return open;
	}

	private HPObjectOutputStream demoOut = null;

	/**
	 * 通过这个方法，设定录像应该发送到的文件
	 */
	public void setDemoOutputStream(OutputStream out) {
		if (isOpen()) {
			throw new AlreadyOpenedException(
					"you should not set demo output stream when TetrixRunningStation is open");
		}

		if (out != null) {
			demoOut = new HPObjectOutputStream(new BufferedOutputStream(out));
		} else {
			if (demoOut != null) {
				try {
					demoOut.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					demoOut = null;
				}
			}
		}

	}

	/**
	 * @return 是否所有仍然连接着的客户都已经游戏结束了，并且本地的TetrixEngine也 已经游戏结束了。
	 */
	public boolean isAllGameOver() {
		if (isClientsAllGameOver() && engine.isGameOver()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isLocalGameOver() {
		return engine.isGameOver();
	}

	public boolean isClientsAllGameOver() {
		for (ClientID client : clients) {
			assert (client != null);
			if (invalidClients.contains(client)) {
				continue;
			}

			EngineMirror mirror = mirrorMap.get(client);
			assert (mirror != null);
			if (!mirrorMap.get(client).isGameOver()) {
				return false;
			}
		}

		return true;
	}

	public List<ClientID> getAllClients() {
		return clientList;
	}

	public boolean isClientValid(ClientID client) {
		if (null == client) {
			throw new NullPointerException("client should not be null");
		}
		if (!clients.contains(client)) {
			throw new IllegalArgumentException(
					"does not contains the specified client");
		}

		return !invalidClients.contains(client);
	}

	/**
	 * 当玩家按下了控制键的时候，传入控制键
	 * 
	 * @param keyCode
	 */
	public void keyPressed(int keyCode) {
		engine.keyPressed(keyCode);
	}

	/**
	 * 当玩家松开了控制键的时候，传入控制键
	 * 
	 * @param keyCode
	 */
	public void keyReleased(int keyCode) {
		engine.keyReleased(keyCode);
	}

	/**
	 * 刷新并绘制TetrixEngine
	 * 
	 * @param g
	 * @param orign
	 *            游戏绘制时的原点
	 * @param observer
	 * @param scale
	 *            游戏绘制出来的图像的缩放比例
	 */
	// TODO 别忘了update时，Thread.sleep时间算上刷新用掉的时间
	public void updateAndPaintEngine(Graphics g, Point orign,
			ImageObserver observer, double scale) {
		assert (engine != null);

		engine.updateAndPaint(g, orign, observer, scale);

		if (singlePlayer && null == demoOut) {
			// 单人模式的时候，直接删除所有UpdateMessage
			while (engine.hasMoreMessageToRetrieve()) {
				try {
					UpdateMessage.dispose(engine.retrieveUpdateMessage());
				} catch (InterruptedException ex) {

				}
			}
		}
	}

	/**
	 * 刷新并绘制指定的客户所对应的TetrixEngineMirror
	 * 
	 * @param clientID
	 *            被刷新的TetrixEngineMirror所对应的客户
	 * @param g
	 * @param orign
	 *            游戏绘制时的原点
	 * @param observer
	 * @param scale
	 *            游戏绘制出来的图像的缩放比例
	 */
	public void updateAndPaintMirror(ClientID clientID, Graphics g,
			Point orign, ImageObserver observer, double scale) {
		if (null == clientID) {
			throw new NullPointerException("clientID should not be null");
		}
		if (!mirrorMap.containsKey(clientID)) {
			throw new IllegalArgumentException("clientID is not contained");
		}

		EngineMirror mirror = mirrorMap.get(clientID);
		assert (mirror != null);

		mirror.updateAndPaint(g, orign, observer, scale);
	}

	/**
	 * 
	 * 这个线程把TetrixEngine中的UpdateMessage取出并发送给TetrixRunningStation 所连接的客户。
	 * 同时这个线程也负责记录录像。
	 * 
	 */
	private class EngineSenderThread extends Thread {
		public EngineSenderThread() {
			super(RunningStation.class.getSimpleName() + " - "
					+ EngineSenderThread.class.getSimpleName());
		}

		public void run() {
			while (open) {
				try {

					UpdateMessage message = engine.retrieveUpdateMessage();
					assert (message != null);

					// 将engine产生的数据发送给每个client
					if (!singlePlayer && connectionCenter.isOpen()) {
						for (ClientID client : clients) {
							assert (client != null);
							if (invalidClients.contains(client)) {
								continue;
							}

							try {
								connectionCenter.sendMessage(client, message);
							} catch (ConnectionDoesNotExistException ex) {
								clientDisconnected(client);
								ex.printStackTrace();
							} catch (FailedToSendMessageException ex) {
								recordClientError(client);
								if (clientErrCount.get(client) != null
										&& clientErrCount.get(client) >= 3) {
									clientDisconnected(client);
								}
								ex.printStackTrace();
							}

						}
					}

					// 将UpdateMessage记录入录像
					if (demoOut != null) {
						demoOut.writeObject(message);
					}

					// 释放UpdateMessage
					UpdateMessage.dispose(message);

				} catch (InterruptedException ex) {
					ex.printStackTrace();
				} catch (AlreadyClosedException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	/**
	 * 
	 * 这个线程从connectionCentter中取出从客户处发送过来的UpdateMessage，
	 * 并把它传递给对应的TetrixEngineMirror
	 * 
	 */
	private class MirrorReceiverThread extends Thread {
		public MirrorReceiverThread() {
			super(RunningStation.class.getSimpleName() + " - "
					+ MirrorReceiverThread.class.getSimpleName());
		}

		public void run() {
			while (open && connectionCenter.isOpen()) {
				try {
					// 从ConnectionCenter中取出UpdateMessage，发送给相应的Mirror
					RemotePackage pack = connectionCenter.retriveMessage();
					assert (pack != null);

					ClientID aimClient = pack.getClientID();
					assert (aimClient != null);

					EngineMirror mirror = mirrorMap.get(aimClient);
					assert (mirror != null);
					assert ((pack.getMessage() instanceof UpdateMessage) || (pack
							.getMessage() instanceof EnforceDisconnectMessage));
					if (mirror != null
							&& (pack.getMessage() instanceof UpdateMessage)) {
						mirror.putUpdateMessage((UpdateMessage) pack
								.getMessage());
					} else if (mirror != null
							&& (pack.getMessage() instanceof EnforceDisconnectMessage)) {
						if (enforceDisconnectable) {
							clientDisconnected(aimClient);
						}
					}

					RemotePackage.deleteObject(pack);
				} catch (AlreadyClosedException ex) {
					ex.printStackTrace();
				}
			}
		}

	}

	// 当向client发送信息的时候，如果client出错，那么就会记录出错次数
	private void recordClientError(ClientID client) {
		assert (client != null);

		Integer old = clientErrCount.get(client);
		if (null == old) {
			clientErrCount.put(client, 1);
		} else {
			clientErrCount.put(client, old + 1);
		}
	}

	// 将断开连接的客户记录，并且将其游戏
	private void clientDisconnected(ClientID client) {
		assert (client != null);

		if (connectionCenter.hasConnectionOf(client)) {
			try {
				connectionCenter.closeConnection(client);
			} catch (FailedToCloseException ex_inner) {
				ex_inner.printStackTrace();
			}
		}

		invalidClients.add(client);

		EngineMirror mirror = mirrorMap.get(client);
		assert (mirror != null);
		mirror.enforceGameOver();
	}

	public boolean isEnforceDisconnectable() {
		return enforceDisconnectable;
	}

	public void setEnforceDisconnectable(boolean enforceDisconnectable) {
		this.enforceDisconnectable = enforceDisconnectable;
	}

}
