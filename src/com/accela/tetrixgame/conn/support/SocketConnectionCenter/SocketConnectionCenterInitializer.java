package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import com.accela.tetrixgame.conn.ConnectionCenterInitializer;
import com.accela.tetrixgame.conn.broadcaster.Broadcaster;
import com.accela.tetrixgame.conn.connectionLauncher.ConnectionLauncher;
import com.accela.tetrixgame.conn.connectionReceiver.ConnectionReceiver;
import com.accela.tetrixgame.conn.connectorCreator.ConnectorCreator;

public class SocketConnectionCenterInitializer extends
		ConnectionCenterInitializer {

	@Override
	public Broadcaster specifyBroadcaster() {
		return new SocketBroadcaster();
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
		return new SocketConnectorCreator();
	}

}
