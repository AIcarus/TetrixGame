package com.accela.tetrixgame.net.hostClient;

public class FailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public FailedException() {
		super();
	}

	public FailedException(String message) {
		super(message);
	}

	public FailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedException(Throwable cause) {
		super(cause);
	}

}
