package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * 
 * 使用延迟新建策略的对象输入流。即什么时候要 用输入流来读数据，什么时候才新建输入流。这 是为了避免新建输入流的时候线程卡死。
 * 
 * 
 */
public class DelayedObjectInputStream {
	private ObjectInputStream in;
	private InputStream inputStream;

	public DelayedObjectInputStream(InputStream inputStream) {
		if (null == inputStream) {
			throw new NullPointerException("inputStream should not be null");
		}
		this.inputStream = inputStream;
	}

	public Object readObject() throws IOException, ClassNotFoundException {
		if (null == in) {
			in = new ObjectInputStream(inputStream);
		}
		return in.readObject();
	}

	public void close() throws IOException {
		if (in != null) {
			in.close();
		} else {
			inputStream.close();
		}
	}

}
