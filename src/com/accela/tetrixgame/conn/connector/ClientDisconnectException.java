package com.accela.tetrixgame.conn.connector;

/**
 * 
 * 这个异常表示所连接的远程端的客户断开连接。
 * 
 */
public class ClientDisconnectException extends Exception {
	private static final long serialVersionUID = com.accela.tetrixgame.conn.shared.IConstants.SERIAL_VERSION_UID;

	public ClientDisconnectException() {
		super();
	}

	public ClientDisconnectException(String message) {
		super(message);
	}

	public ClientDisconnectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientDisconnectException(Throwable cause) {
		super(cause);
	}

}
