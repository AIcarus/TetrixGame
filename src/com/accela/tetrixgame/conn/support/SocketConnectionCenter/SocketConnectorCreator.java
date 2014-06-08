package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import com.accela.tetrixgame.conn.connector.Connector;
import com.accela.tetrixgame.conn.connectorCreator.ConnectorCreator;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;

public class SocketConnectorCreator extends ConnectorCreator {
	@Override
	protected Connector createConnectorImpl(NewConnectionInfo info) {
		if (!(info instanceof SocketNewConnectionInfo)) {
			assert (false);
			throw new IllegalArgumentException(
					"The NewConnectionInfo should be a SocketNewConnectionInfo");
		}

		SocketNewConnectionInfo socketInfo = (SocketNewConnectionInfo) info;

		return new SocketConnector(socketInfo.getSocket());
	}

}
