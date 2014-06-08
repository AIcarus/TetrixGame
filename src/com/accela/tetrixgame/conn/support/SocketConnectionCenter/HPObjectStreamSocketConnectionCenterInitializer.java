package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import com.accela.tetrixgame.conn.ConnectionCenterInitializer;
import com.accela.tetrixgame.conn.broadcaster.Broadcaster;
import com.accela.tetrixgame.conn.connectionLauncher.ConnectionLauncher;
import com.accela.tetrixgame.conn.connectionReceiver.ConnectionReceiver;
import com.accela.tetrixgame.conn.connectorCreator.ConnectorCreator;

/**
 * 
 * 与SocketConnectionCenter完全相同，除了这个初始化器使用 HPObjectStreamSocketConnectorCreator以外
 * 
 */
public class HPObjectStreamSocketConnectionCenterInitializer extends
		ConnectionCenterInitializer {
	@Override
	public Broadcaster specifyBroadcaster() {
		return new HPObjectStreamSocketBroadcaster();
	}

	@Override
	public ConnectionLauncher specifyConnectionLauncher() {
		return new SocketConnectionLauncher();
	}

	@Override
	public ConnectionReceiver specifyConnectionReceiver() {
		return new SocketConnectionReceiver();
	}

	@Override
	public ConnectorCreator specifyConnectorCreator() {
		return new HPObjectStreamSocketConnectorCreator();
	}

}
