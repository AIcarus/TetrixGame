package com.accela.tetrixgame.conn.broadcaster;

import com.accela.tetrixgame.conn.shared.IConstants;

public class FailedToBroadcastMessageException extends Exception {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public FailedToBroadcastMessageException() {
		super();
	}

	public FailedToBroadcastMessageException(String message) {
		super(message);
	}

	public FailedToBroadcastMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedToBroadcastMessageException(Throwable cause) {
		super(cause);
	}

}
