package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.shared.IConstants;

/**
 * 
 * 这个异常是一个免检异常。 它表示组件的状态有问题。比如你在打开了一个组件后，又试图打开它一次， 就会抛出这个异常
 * 
 */
public class AlreadyOpenedException extends IllegalStateException {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public AlreadyOpenedException() {
		super();
	}

	public AlreadyOpenedException(String message) {
		super(message);
	}

	public AlreadyOpenedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyOpenedException(Throwable cause) {
		super(cause);
	}

}
