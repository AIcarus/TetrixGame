package com.accela.tetrixgame.net.basic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.ConnectionCenter;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.RemotePackage;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketConnectionCenter;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyClosedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyOpenedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.net.hostClient.HostBroadcastMessage;
import com.accela.tetrixgame.net.hostClient.NetHost;

/**
 * 
 * 这个类专门用来接收TetrixNet的广播，识别网段上的Host
 * 
 */
public class HostBroadcastReceiver {
	private ConnectionCenter connectionCenter;

	private Set<ClientID> lanHosts;

	private HostBroadcastReceiverThread broadcastReceiver;

	private boolean open = false;

	public HostBroadcastReceiver() {
		connectionCenter = SocketConnectionCenter.createInstance(true);
		lanHosts = Collections.synchronizedSet(new HashSet<ClientID>());
	}

	public void open() throws FailedToOpenException {
		if (open) {
			throw new AlreadyOpenedException();
		}

		try {
			open = true;

			connectionCenter.open();

			connectionCenter.openBroadcastFunction();

			broadcastReceiver = new HostBroadcastReceiverThread();
			broadcastReceiver.start();

		} catch (FailedToOpenException ex) {
			open = false;
			throw ex;
		}
	}

	public void close() throws FailedToCloseException {
		if (!open) {
			throw new AlreadyClosedException();
		}

		try {
			open = false;

			connectionCenter.close();
		} finally {
			broadcastReceiver.interrupt();
		}

	}

	public boolean isOpen() {
		return open;
	}

	/**
	 * @return 从网段上识别到的Host
	 */
	public Set<ClientID> getHosts() {
		return new HashSet<ClientID>(lanHosts);
	}

	/**
	 * 
	 * 主管从网络上接收广播，更新lanHosts的线程
	 * 
	 */
	public class HostBroadcastReceiverThread extends Thread {
		public HostBroadcastReceiverThread() {
			super("HostBroadcastReceiver - HostBroadcastReceiverThread");
		}

		public void run() {
			while (isOpen() && connectionCenter.isOpen()) {
				try {
					synchronized (lanHosts) {
						lanHosts.clear();

						while (connectionCenter.hasBroadcastMessageToRetrieve()) {
							RemotePackage pack = connectionCenter
									.retriveBroadcastMessage();
							Common.LOG.info("HostBroadcastReceiver retrieved packet: "+pack.getMessage());
							HostBroadcastMessage message = null;
							if (pack.getMessage() instanceof HostBroadcastMessage) {
								message = (HostBroadcastMessage) pack
										.getMessage();
							}
							RemotePackage.deleteObject(pack);

							assert (message != null);

							if (message.getHostReceivingID() != null) {
								lanHosts.add(message.getHostReceivingID());
							}
						}
					}

					Thread.sleep(NetHost.HOST_BROADCAST_PERIOD);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

}
