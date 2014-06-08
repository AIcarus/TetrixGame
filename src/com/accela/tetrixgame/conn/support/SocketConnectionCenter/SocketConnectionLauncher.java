package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.io.IOException;
import java.net.Socket;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.connectionLauncher.ConnectionLauncher;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;

public class SocketConnectionLauncher extends ConnectionLauncher {

	@Override
	protected NewConnectionInfo launchConnectionImpl(ClientID clientID)
			throws IOException {
		if (!(clientID instanceof SocketClientID)) {
			assert (false);
			throw new IllegalArgumentException(
					"The clientID should be a SocketClientID");
		}
		
		SocketClientID socketClientID = (SocketClientID) clientID;
		Socket server = new Socket(socketClientID.getAddress(),
				socketClientID.getPort());
		Common.LOG.info("SocketConnectionLauncher opened connection to: "+socketClientID);
		return new SocketNewConnectionInfo(server);
	}

}
