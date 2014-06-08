package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.shared.IConstants;

/**
 * 
 * 这个异常是一个免检异常。 它表示组件的状态有问题。比如你在关闭了一个组件后，又试图关闭它一次， 就会抛出这个异常
 * 
 */
public class AlreadyClosedException extends IllegalStateException {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public AlreadyClosedException() {
		super();
	}

	public AlreadyClosedException(String message) {
		super(message);
	}

	public AlreadyClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyClosedException(Throwable cause) {
		super(cause);
	}

}
