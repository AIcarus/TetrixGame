package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.shared.IConstants;

/**
 * 
 * 这个异常应IOpenClosable接口而设计，表示在关闭方法中遇到了异常。 使得关闭失败。
 * 
 */
public class FailedToCloseException extends Exception {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public FailedToCloseException() {
		super();
	}

	public FailedToCloseException(String message) {
		super(message);
	}

	public FailedToCloseException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedToCloseException(Throwable cause) {
		super(cause);
	}

}
