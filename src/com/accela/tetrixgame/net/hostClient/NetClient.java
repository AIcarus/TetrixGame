package com.accela.tetrixgame.net.hostClient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.ConnectionCenter;
import com.accela.tetrixgame.conn.ConnectionDoesNotExistException;
import com.accela.tetrixgame.conn.ConnectionFullException;
import com.accela.tetrixgame.conn.FailedToCreateConnectionException;
import com.accela.tetrixgame.conn.connector.FailedToSendMessageException;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketConnectionCenter;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;

/**
 * 
 * 同TetrixNetHost
 * 
 */
public class NetClient {
	private ConnectionCenter connectionCenter;

	private static enum ClientState {
		CLOSED, CONNECTED, IDENTIFIED, ESTABLISHED, GAME_STARTED,

	}

	private ClientState state;

	public NetClient() {
		connectionCenter = SocketConnectionCenter.createInstance(true);

		state = ClientState.CLOSED;
	}

	// /////////////////////////第一阶段，连接host////////////////////////

	private ClientID hostID; // 无望检查host是否切断了和自己的连接

	private void connect(ClientID hostID) throws FailedException {
		if (null == hostID) {
			throw new NullPointerException("hostID should not be null");
		}
		if (state != ClientState.CLOSED) {
			throw new IllegalStateException(
					"illegal state: ClientState should be "
							+ ClientState.CLOSED);
		}

		this.hostID = hostID;

		if (connectionCenter.isOpen()) {
			try {
				connectionCenter.close();
			} catch (FailedToCloseException ex) {
				ex.printStackTrace();
			}
		}

		state = ClientState.CONNECTED;
		try {
			connectionCenter.open();

			connectionCenter.setMaxConnectionNum(-1);

			connectionCenter.openConnection(hostID);

		} catch (Exception ex) {
			state = ClientState.CLOSED;
			throw new FailedException(ex);
		}
		
		Common.LOG.info("NetClient connected: "+hostID);
	}

	// ///////////////////第二阶段，发送ClientIdentifyMessage///////////////////

	private void sendIdentification() throws FailedException {
		if (state != ClientState.CONNECTED && state != ClientState.IDENTIFIED) {
			throw new IllegalStateException(
					"illegal state: ClientState should be "
							+ ClientState.CONNECTED + " or "
							+ ClientState.IDENTIFIED);
		}

		state = ClientState.IDENTIFIED;
		try {
			connectionCenter.startToReceiveConnection();

			connectionCenter.sendMessage(hostID, new ClientIdentifyMessage(
					connectionCenter.getConnectionReceivingClientID()));
		} catch (Exception ex) {
			state = ClientState.CLOSED;
			throw new FailedException(ex);
		}
		Common.LOG.info("NetClient sent identification: "+connectionCenter.getConnectionReceivingClientID());
	}

	// ////////////////////第三阶段，等待Host命令，建立全连接///////////////

	private static final long CHECK_PERIOD = 1000;

	private Map<ClientID, ClientIdentifyMessage> clientMap = null;

	private Map<ClientID, ClientIdentifyMessage> getClientMap() {
		if (state == ClientState.CLOSED || state == ClientState.CONNECTED
				|| state == ClientState.IDENTIFIED) {
			throw new IllegalStateException("you can't get clientMap yet");
		}

		return clientMap;
	}

	private EstablishFullConnectionMessage tryGetMessage()
			throws FailedException {
		while (connectionCenter.hasMessageToRetrieve()) {
			RemotePackage pack = connectionCenter.retriveMessage();
			Common.LOG.info("NetClient waitToEstablishFullConnection() got packet: "+pack.getMessage());
			if (pack.getMessage() instanceof EstablishFullConnectionMessage) {
				RemotePackage.deleteObject(pack);
				return (EstablishFullConnectionMessage) pack.getMessage();
			}

			RemotePackage.deleteObject(pack);
		}

		if (!connectionCenter.hasConnectionOf(hostID)) {
			throw new FailedException("Host disconnected");
		}

		return null;
	}

	private void waitToEstablishFullConnection() throws InterruptedException,
			FailedException {
		if (state != ClientState.IDENTIFIED) {
			throw new IllegalStateException(
					"illegal state: ClientState should be "
							+ ClientState.IDENTIFIED);
		}

		// 获取EstablishFullConnectionMessage
		EstablishFullConnectionMessage message = null;
		int sendIdentifyMessageFailCount = 0;
		while (true) {
			// 重复发送身份信息，避免Host漏掉自己
			try {
				Common.LOG.info("NetClient waitToEstablishFullConnection(fail="+sendIdentifyMessageFailCount+") re-sending identification: "+connectionCenter.getConnectionReceivingClientID());
				connectionCenter.sendMessage(hostID, new ClientIdentifyMessage(
						connectionCenter.getConnectionReceivingClientID()));

			} catch (ConnectionDoesNotExistException ex) {
				sendIdentifyMessageFailCount += 3;
				ex.printStackTrace();
			} catch (FailedToSendMessageException ex) {
				sendIdentifyMessageFailCount++;
				ex.printStackTrace();
			}

			// 如果频繁发送信息不成功，则认为连接已经断开
			if (sendIdentifyMessageFailCount >= 3) {
				try {
					this.connectionCenter.closeConnection(hostID);
				} catch (FailedToCloseException ex) {
					ex.printStackTrace();
				}
				throw new FailedException("Host disconnected");
			}

			try {
				message = tryGetMessage();
			} catch (FailedException ex) {
				state = ClientState.CLOSED;
				throw ex;
			}

			if (message != null) {
				break;
			}

			Thread.sleep(CHECK_PERIOD);
		}
		Common.LOG.info("NetClient waitToEstablishFullConnection() got EstablishFullConnectionMessage: "+message);

		// 建立全连接
		assert (message.getClientIdentifyMessages() != null);
		this.clientMap = new ConcurrentHashMap<ClientID, ClientIdentifyMessage>();
		// TODO 这里有个很大的问题，TetrixNetClient收到的clientID与实际上对应的机子连接过来时的clientID可能不符,
		// TODO 从而导致ClientIdentifyMessage对不上号。
		for (ClientIdentifyMessage idMessage : message
				.getClientIdentifyMessages()) {
			assert (idMessage != null);
			assert (idMessage.getClientReceivingID() != null);

			clientMap.put(idMessage.getClientReceivingID(), idMessage);
		}

		boolean foundSelf = false;
		for (ClientIdentifyMessage idMessage : message
				.getClientIdentifyMessages()) {
			assert (idMessage != null);
			assert (idMessage.getClientReceivingID() != null);

			if (!foundSelf) {
				if (idMessage.getClientReceivingID().equals(
						connectionCenter.getConnectionReceivingClientID())) {
					foundSelf = true;
				}
				continue;
			}

			final ClientID client = idMessage.getClientReceivingID();
			assert (!connectionCenter.hasConnectionOf(client));

			boolean succ = false;
			int retryCount = 0;
			Exception excep = null;
			while (!succ && retryCount <= 3) {
				try {
					connectionCenter.openConnection(client);
					succ = true;
				} catch (ConnectionFullException ex) {
					succ = false;
					excep = ex;

					ex.printStackTrace();
					assert (false);
				} catch (FailedToCreateConnectionException ex) {
					succ = false;
					excep = ex;
				}

				retryCount++;
			}

			if (!succ) {
				assert (excep != null);
				excep.printStackTrace();
			}
		}

		// 返回全连接建立完成的消息
		try {
			connectionCenter.sendMessage(hostID,
					new EstablishFullConnectionFinishedMessage());
		} catch (Exception ex) {
			state = ClientState.CLOSED;
			throw new FailedException(ex);
		}

		// 推进状态
		state = ClientState.ESTABLISHED;
	}

	// /////////////////////////第四阶段，等待开始游戏//////////////////////////

	private RunningStation waitToStartGame() throws FailedException {

		state = ClientState.GAME_STARTED;
		try {

			// 取得游戏开始消息
			GameStartMessage message = null;
			while (null == message) {
				RemotePackage pack = connectionCenter.retriveMessage();
				if (pack.getMessage() instanceof GameStartMessage) {
					message = (GameStartMessage) pack.getMessage();
				}

				RemotePackage.deleteObject(pack);
			}

			connectionCenter.stopToReceiveConnection();

			// 清理connectionCenter
			while (connectionCenter.hasMessageToRetrieve()) {
				RemotePackage.deleteObject(connectionCenter.retriveMessage());
			}
			while (connectionCenter.hasBroadcastMessageToRetrieve()) {
				RemotePackage.deleteObject(connectionCenter
						.retriveBroadcastMessage());
			}

			// 建立游戏
			RunningStation station = new RunningStation(
					message.getAttr(), connectionCenter);

			// 脱离对connectionCenter的控制
			connectionCenter = null;

			return station;

		} catch (Exception ex) {
			state = ClientState.CLOSED;
			throw new FailedException(ex);
		}
	}

	// ////////////////////////////中途退出////////////////////////////

	private void close() throws FailedToCloseException {
		if (null == connectionCenter) {
			throw new IllegalStateException(
					"you can't invoke this method after you have started game");
		}

		state = ClientState.CLOSED;
		connectionCenter.close();
	}

	// /////////////////////////////整合后的方法////////////////////////

	/**
	 * 与指定的TetrixNetHost连接上，并等待游戏开始。这个方法会阻塞直到游戏开始。
	 * 
	 * 返回的TetrixRunningStation对象就是游戏实体。
	 */
	public RunningStation launchGame(ClientID hostID)
			throws FailedException {
		connect(hostID);
		sendIdentification();
		try {
			waitToEstablishFullConnection();
		} catch (InterruptedException ex) {
			state = ClientState.CLOSED;
			throw new FailedException(ex);
		}
		return waitToStartGame();
	}

	/**
	 * 中断当前任务，退出。
	 */
	public void abort() throws FailedToCloseException {
		close();
	}

	public Set<ClientID> getClients() {
		return new HashSet<ClientID>(getClientMap().keySet());
	}

	public ClientIdentifyMessage getClientInfo(ClientID client) {
		if (null == client) {
			throw new NullPointerException("client should not be null");
		}

		return clientMap.get(client);
	}

	public ClientID getHostID() {
		return hostID;
	}

}
