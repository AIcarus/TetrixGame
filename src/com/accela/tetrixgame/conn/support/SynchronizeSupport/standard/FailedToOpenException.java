package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.shared.IConstants;

/**
 * 
 * 这个异常应IOpenClosable接口而设计，表示在打开方法中遇到了异常。 使得关闭失败。
 * 
 */
public class FailedToOpenException extends Exception {
	private static final long serialVersionUID = IConstants.SERIAL_VERSION_UID;

	public FailedToOpenException() {
		super();
	}

	public FailedToOpenException(String message) {
		super(message);
	}

	public FailedToOpenException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedToOpenException(Throwable cause) {
		super(cause);
	}

}
