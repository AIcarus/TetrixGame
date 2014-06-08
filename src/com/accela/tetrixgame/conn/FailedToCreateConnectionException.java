package com.accela.tetrixgame.conn;

import com.accela.tetrixgame.conn.shared.IConstants;

public class FailedToCreateConnectionException extends Exception {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public FailedToCreateConnectionException() {
		super();
	}

	public FailedToCreateConnectionException(String message) {
		super(message);
	}

	public FailedToCreateConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedToCreateConnectionException(Throwable cause) {
		super(cause);
	}
}
