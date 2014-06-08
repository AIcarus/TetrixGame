package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.net.Socket;

public class SocketConnectionCenterUtilities {
	/**
	 * 检查socket是否已经打开
	 */
	public static boolean checkOpened(Socket socket) {
		if (null == socket) {
			return false;
		}

		return (socket.isBound() && !socket.isClosed() && socket.isConnected()
				&& !socket.isInputShutdown() && !socket.isOutputShutdown());
	}

}
