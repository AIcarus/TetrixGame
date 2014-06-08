package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.io.IOException;
import java.net.*;

import com.accela.tetrixgame.conn.connectionReceiver.ConnectionReceiver;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;

public class SocketConnectionReceiver extends ConnectionReceiver {
	private ServerSocket serverSocket;

	@Override
	protected void closeImpl() throws IOException {
		serverSocket.close();
		serverSocket = null;
	}

	@Override
	protected NewConnectionInfo detectingConnection() throws IOException {
		Socket client = serverSocket.accept();
		return new SocketNewConnectionInfo(client);
	}

	@Override
	protected void openImpl() throws IOException {
		InetAddress localhost = InetAddress.getLocalHost();
		serverSocket = new ServerSocket(getIdlePort(), 50, localhost);
	}
	
	public int getSocketServerPort() {
		ocs.lockMethod();

		try {
			ocs.ensureOpen();
			return serverSocket.getLocalPort();
		} finally {
			ocs.unlockMethod();
		}
	}

	private int getIdlePort() throws IOException {
		ServerSocket s = new ServerSocket(0);
		int port = s.getLocalPort();
		s.close();
		return port;
	}

	@Override
	protected ClientID getConnectionReceivingClientIDImpl() {
		try {
			return new SocketClientID(InetAddress.getLocalHost(),
					getSocketServerPort());
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			assert (false);
			throw new IllegalStateException(
					"The UnknowHostException should not occured.", ex);
		}
	}

	@Override
	protected ClientID getMyAddressImpl() {
		return new SocketClientID(serverSocket.getInetAddress(), serverSocket.getLocalPort());
	}

}
