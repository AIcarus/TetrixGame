package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.net.Socket;

import com.accela.tetrixgame.conn.shared.NewConnectionInfo;

public class SocketNewConnectionInfo extends NewConnectionInfo {
	private Socket socket;

	public SocketNewConnectionInfo(Socket socket) {
		if (null == socket) {
			throw new NullPointerException();
		}

		this.socket = socket;
	}

	public Socket getSocket() {
		return socket;
	}

}
