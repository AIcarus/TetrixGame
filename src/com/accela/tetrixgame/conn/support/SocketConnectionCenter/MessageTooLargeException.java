package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import com.accela.tetrixgame.conn.shared.IConstants;

public class MessageTooLargeException extends Exception {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public MessageTooLargeException() {
		super();
	}

	public MessageTooLargeException(String message) {
		super(message);
	}

}
