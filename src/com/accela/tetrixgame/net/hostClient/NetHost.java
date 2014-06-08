package com.accela.tetrixgame.net.hostClient;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.ConnectionCenter;
import com.accela.tetrixgame.conn.ConnectionDoesNotExistException;
import com.accela.tetrixgame.conn.connector.FailedToSendMessageException;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketConnectionCenter;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.gamelogic.GameAttributes;

/**
 * 
 * 现在介绍Tetrix游戏的网络模式设计： Tetrix游戏中，先要有一个玩家建立游戏主机，即TetrixNetHost。
 * 游戏主机不断广播以表明自己的存在，并告知其他人如何连接自己。 然后，会有其他玩家在其他计算机上建立客户机，即TetrixNetClient，
 * 游戏客户机会连接的指定的游戏主机上。 一段时间后，就形成了一个主机为中心，多个客户机连接其上的网络。
 * 然后主机向客户机发送建立全连接的请求，整个网络开始建立全连接。 这个过程实际上就是，各个客户机之间建立全连接。最后整个网络，
 * 包括主机和所有客户机，成为以及全连接网络。 当客户机完成自己的全连接建立任务后，就会通知主机。当主机得知
 * 所有的客户机都已经完成任务后，就会开始游戏，并通知客户机也开始 游戏。此时主机和客户机已经没有任何区别，它们都会各自生成一个
 * TetrixRunningStation实例，代表自己机子上的游戏实体。外界得到
 * 这个TetrixRunningStation实例，用图形界面把它表示出来，就成了 一场玩家可以看到的进行中的游戏。
 * 
 * 建立一场Tetrix游戏，需要TetrixNetHost与TetrixNetClient 进行许多步的交互，这个类的代码已经从上到下被分成了许多段，
 * 每一段都已经写明了自己负责的交互步骤。类最后的public方法 是将这些步骤经过整合后的到的方便使用的方法。
 * 
 */
public class NetHost {
	// WARNING
	// 一个ConnectionCenter除了receivingConnectionClientID以外，实际上可能产生多种ClientID

	private ConnectionCenter connectionCenter;

	private Map<ClientID, ClientIdentifyMessage> clientMap = new ConcurrentHashMap<ClientID, ClientIdentifyMessage>();

	private boolean singlePlayer;

	private static enum HostState {
		CLOSED, WAITING, ESTABLISHING, ESTABLISH_FINISHED, GAME_STARTED
	}

	private HostState state;

	public NetHost() {
		connectionCenter = SocketConnectionCenter.createInstance(true);

		state = HostState.CLOSED;
	}

	// /////////////////////////第一阶段，等待和接收客户连接//////////////////////////

	private ClientReceivingThread clientReceivingThread;

	public static final int MAXIMUM_OF_PLAYER_NUM = 17;

	public static final long HOST_BROADCAST_PERIOD = 1000;

	private HostBroadcastMessage hostBroadcastMessage;

	private void startWaiting(int maxPlayerNum) throws FailedException {
		if (maxPlayerNum <= 0 || maxPlayerNum > MAXIMUM_OF_PLAYER_NUM) {
			throw new IllegalArgumentException(
					"illegal maxPlayerNum, too big or too small: "
							+ maxPlayerNum);
		}

		if (maxPlayerNum == 1) {
			singlePlayer = true;
		} else {
			singlePlayer = false;
		}

		if (state != HostState.CLOSED) {
			throw new IllegalStateException(
					"illegal state: HostState should be " + HostState.CLOSED);
		}

		if (connectionCenter.isOpen()) {
			try {
				connectionCenter.close();
			} catch (FailedToCloseException ex) {
				ex.printStackTrace();
			}
		}

		state = HostState.WAITING;

		try {
			connectionCenter.open();

			connectionCenter.setMaxConnectionNum(maxPlayerNum - 1);

			connectionCenter.startToReceiveConnection();

			connectionCenter.openBroadcastFunction();
			Common.LOG.info("TetrixNetHost: start to broadcast message");
			hostBroadcastMessage = new HostBroadcastMessage(
					connectionCenter.getConnectionReceivingClientID());

			connectionCenter.broadcastMessage(hostBroadcastMessage,
					HOST_BROADCAST_PERIOD);
			writeMyAddress();

			// 启动接收ClientIdentifyMessage的线程
			clientReceivingThread = new ClientReceivingThread();
			clientReceivingThread.start();

		} catch (Exception ex) {
			state = HostState.CLOSED;
			throw new FailedException(ex);
		}

	}

	/**
	 * Write my address in a local file, which can be seen by BOTs. So that they can join the game, 
	 * even if cannot receive the broadcasts
	 * @throws IOException 
	 */
	private void writeMyAddress() throws IOException {
		File hostAddr=Common.HOST_ADDRESS;
		PrintWriter out=null;
		try{
			out=new PrintWriter(hostAddr);
			out.println(this.getMyAddress().serializeToString());
		}finally{
			if(out!=null){
				out.close();
			}
		}
	}

	private Map<ClientID, ClientIdentifyMessage> getClientMap() {
		if (state == HostState.CLOSED) {
			throw new IllegalStateException("you can't get clientMap yet");
		}

		return this.clientMap;
	}

	private void disconnectClient(ClientID client) {
		if (state != HostState.WAITING) {
			throw new IllegalArgumentException(
					"illegal state: HostState should be " + HostState.WAITING);
		}
		if (null == client) {
			throw new NullPointerException("client should not be null");
		}

		try {
			connectionCenter.closeConnection(client);
			clientMap.remove(client);
		} catch (FailedToCloseException ex) {
			ex.printStackTrace();
		}
	}

	private class ClientReceivingThread extends Thread {
		public ClientReceivingThread() {
			super("TetrixNetHost - ClientReceivingThread");
		}

		public void run() {
			while (HostState.WAITING == state) {
				try {
					// 检测新加入的成员
					while (connectionCenter.hasMessageToRetrieve()) {
						RemotePackage pack = connectionCenter.retriveMessage();
						Common.LOG.info("NetHost received message: "+pack.getMessage());
						
						assert (pack.getClientID() != null);
						assert (pack.getMessage() instanceof ClientIdentifyMessage);

						if ((pack.getClientID() != null)
								&& (pack.getMessage() instanceof ClientIdentifyMessage)
								&& (connectionCenter.hasConnectionOf(pack
										.getClientID()))) {
							clientMap.put(pack.getClientID(),
									(ClientIdentifyMessage) pack.getMessage());
						}

						RemotePackage.deleteObject(pack);
					}

					// 清除已经退出的客户
					Set<ClientID> toBeRemoved = new HashSet<ClientID>();
					for (ClientID client : clientMap.keySet()) {
						assert (client != null);
						if (!connectionCenter.hasConnectionOf(client)) {
							toBeRemoved.add(client);
						}
					}
					for (ClientID client : toBeRemoved) {
						Object ret = clientMap.remove(client);
						assert (ret != null);
					}

					Thread.sleep(500);

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}
	}

	// /////////////////////////第二阶段，所有人建立全连接//////////////////////////

	private void stopWaitingAndClean() throws FailedToCloseException {
		connectionCenter.closeBroadcastFunction();
		connectionCenter.stopToReceiveConnection();

		clientReceivingThread.interrupt();

		while (connectionCenter.hasMessageToRetrieve()) {
			RemotePackage.deleteObject(connectionCenter.retriveMessage());
		}
		while (connectionCenter.hasBroadcastMessageToRetrieve()) {
			RemotePackage.deleteObject(connectionCenter
					.retriveBroadcastMessage());
		}

		Set<ClientID> toBeRemoved = new HashSet<ClientID>();
		for (ClientID client : clientMap.keySet()) {
			assert (client != null);
			if (!connectionCenter.hasConnectionOf(client)) {
				toBeRemoved.add(client);
			}
		}
		for (ClientID client : toBeRemoved) {
			Object ret = clientMap.remove(client);
			assert (ret != null);
		}

		for (ClientID client : connectionCenter.getConnectedClientIDs()) {
			assert (client != null);
			if (!clientMap.containsKey(client)) {
				try {
					connectionCenter.closeConnection(client);
				} catch (FailedToCloseException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void sendEstablishFullConnectionMessage() throws FailedException {
		if (state != HostState.WAITING) {
			throw new IllegalArgumentException(
					"illegal state: HostState should be " + HostState.WAITING);
		}

		state = HostState.ESTABLISHING;

		try {
			// 清理上一个状态
			stopWaitingAndClean();

			// 向各个客户发送进行全连接的请求
			for (ClientID client : clientMap.keySet()) {
				assert (client != null);

				boolean succ = false;
				int retryCount = 0;
				Exception excep = null;
				boolean isConnectionNotExisted = false;
				while (!succ && retryCount <= 3) {
					try {
						connectionCenter.sendMessage(client,
								new EstablishFullConnectionMessage(
										new LinkedList<ClientIdentifyMessage>(
												clientMap.values())));
						succ = true;
					} catch (FailedToSendMessageException ex) {
						succ = false;
						excep = ex;
					} catch (ConnectionDoesNotExistException ex) {
						succ = false;
						isConnectionNotExisted = true;
						excep = ex;
					}

					retryCount++;
				}

				if (!succ) {
					if (isConnectionNotExisted) {
						try {
							connectionCenter.closeConnection(client);
						} catch (FailedToCloseException ex) {
							ex.printStackTrace();
						}
					}

					assert (excep != null);
					excep.printStackTrace();
				}
			}

		} catch (Exception ex) {
			state = HostState.CLOSED;
			throw new FailedException(ex);
		}
	}

	// /////////////////////////第三阶段，等待直到建立连接//////////////////////////

	private static final long WAIT_TIME_OUT = 60 * 1000;

	private static final long CHECK_PERIOD = 1000;

	private Map<ClientID, Boolean> finishMap = new HashMap<ClientID, Boolean>();

	private boolean checkAllEstablished() {
		while (connectionCenter.hasMessageToRetrieve()) {
			RemotePackage pack = connectionCenter.retriveMessage();
			if (pack.getMessage() instanceof EstablishFullConnectionFinishedMessage) {
				finishMap.put(pack.getClientID(), Boolean.TRUE);
			}

			RemotePackage.deleteObject(pack);
		}

		for (ClientID client : clientMap.keySet()) {
			if (connectionCenter.hasConnectionOf(client)) {
				if (finishMap.get(client) == null
						|| !finishMap.get(client).equals(Boolean.TRUE)) {
					return false;
				}
			}
		}

		return true;
	}

	private void waitForEstablishFinished() throws InterruptedException {
		if (state != HostState.ESTABLISHING) {
			throw new IllegalArgumentException(
					"illegal state: HostState should be "
							+ HostState.ESTABLISHING);
		}

		long waitTime = 0;
		while (waitTime <= WAIT_TIME_OUT) {
			if (checkAllEstablished()) {
				break;
			}

			Thread.sleep(CHECK_PERIOD);
			waitTime += CHECK_PERIOD;
		}

		state = HostState.ESTABLISH_FINISHED;
	}

	// /////////////////////////第四阶段，开始游戏//////////////////////////

	private RunningStation startGame(GameAttributes attr)
			throws FailedException {
		if (null == attr) {
			attr = new GameAttributes();
		}
		if (state != HostState.ESTABLISH_FINISHED) {
			throw new IllegalArgumentException(
					"illegal state: HostState should be "
							+ HostState.ESTABLISH_FINISHED);
		}

		state = HostState.GAME_STARTED;
		try {
			// 向各个客户发送游戏开始消息
			for (ClientID client : clientMap.keySet()) {
				try {
					connectionCenter.sendMessage(client, new GameStartMessage(
							attr));
				} catch (FailedToSendMessageException ex) {
					ex.printStackTrace();
				} catch (ConnectionDoesNotExistException ex) {
					ex.printStackTrace();
				}
			}

			// 清理connectionCenter
			while (connectionCenter.hasMessageToRetrieve()) {
				RemotePackage.deleteObject(connectionCenter.retriveMessage());
			}
			while (connectionCenter.hasBroadcastMessageToRetrieve()) {
				RemotePackage.deleteObject(connectionCenter
						.retriveBroadcastMessage());
			}

			// 建立游戏
			RunningStation station = new RunningStation(attr,
					connectionCenter);

			// 脱离对connectionCenter的控制
			connectionCenter = null;

			return station;

		} catch (Exception ex) {
			state = HostState.CLOSED;
			throw new FailedException(ex);
		}

	}

	// /////////////////////////////中途退出/////////////////////////

	private void close() throws FailedToCloseException {
		if (null == connectionCenter) {
			throw new IllegalStateException(
					"you can't invoke this method after you have started game");
		}

		state = HostState.CLOSED;
		connectionCenter.close();
	}

	// /////////////////////////////整合后的方法////////////////////////

	/**
	 * 建立主机并且开始等待其他客户机连接。
	 */
	public void startToReceiveClients(int maxPlayerNum) throws FailedException {
		startWaiting(maxPlayerNum);
	}

	/**
	 * 踢出玩家
	 */
	public void kick(ClientID client) {
		disconnectClient(client);
	}

	/**
	 * 根据已经连接上的客户机，启动游戏。 返回的TetrixRunningStation对象就是游戏实体。
	 * 
	 * 这个方法会阻塞直到游戏建立完毕。
	 */
	public RunningStation launchGame(GameAttributes attr)
			throws FailedException {
		sendEstablishFullConnectionMessage();
		try {
			waitForEstablishFinished();
		} catch (InterruptedException ex) {
			state = HostState.CLOSED;
			throw new FailedException(ex);
		}

		return startGame(attr);
	}

	/**
	 * 中断当前任务，退出。
	 */
	public void abort() throws FailedToCloseException {
		close();
	}

	public boolean isSinglePlayer() {
		return singlePlayer;
	}

	public Set<ClientID> getClients() {
		if (state == HostState.CLOSED) {
			throw new IllegalStateException(
					"you should invoke startToReceiveClients(int) first");
		}

		return new HashSet<ClientID>(getClientMap().keySet());
	}

	public ClientIdentifyMessage getClientInfo(ClientID client) {
		if (null == client) {
			throw new NullPointerException("client should not be null");
		}

		return clientMap.get(client);
	}
	
	public ClientID getMyAddress(){
		return this.connectionCenter.getMyAddress();
	}

	/**
	 * @return 这个TretrixNetHost所广播的信息。
	 */
	public HostBroadcastMessage getHostBroadcastMessage() {
		if (state != HostState.WAITING) {
			throw new IllegalStateException(
					"you can only get hostBroadcastMessage when this host is waiting for clients to join it");
		}

		return this.hostBroadcastMessage;
	}

}
