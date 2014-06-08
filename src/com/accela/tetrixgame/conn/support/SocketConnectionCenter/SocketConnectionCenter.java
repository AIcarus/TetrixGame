package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.net.InetAddress;

import com.accela.tetrixgame.conn.ConnectionCenter;
import com.accela.tetrixgame.conn.ConnectionCenterInitializer;

/**
 * 
 * 使用Socket技术实现的ConnectionCenter
 * 
 */
public class SocketConnectionCenter extends ConnectionCenter {
	private boolean useHPObjectStreams = false;

	protected SocketConnectionCenter(ConnectionCenterInitializer initializer) {
		super(initializer);
	}

	/**
	 * 新建一个SocketConnectionCenter，其中传输数据使用的是 java自带的对象输入输出流
	 */
	public static SocketConnectionCenter createInstance() {
		SocketConnectionCenterInitializer initializer = new SocketConnectionCenterInitializer();
		SocketConnectionCenter connectionCenter = null;

		connectionCenter = new SocketConnectionCenter(initializer);

		return connectionCenter;
	}

	/**
	 * 新建一个SocketConnectionCenter，其中传输数据用的对象输入输出流，
	 * 即可以是java自带的对象输入输出流，也可以是HPObjectInputStream和
	 * HPObjectOutputStream，这通过useHPObjectStreams来决定。
	 * 
	 * @param useHPObjectStreams
	 *            是否使用HPObjectOutputStream和HPObjectInputStream， 作为传输数据时的对象输入输出流
	 */
	public static SocketConnectionCenter createInstance(
			boolean useHPObjectStreams) {
		if (!useHPObjectStreams) {
			return createInstance();
		} else {
			HPObjectStreamSocketConnectionCenterInitializer initializer = new HPObjectStreamSocketConnectionCenterInitializer();
			SocketConnectionCenter connectionCenter = null;

			connectionCenter = new SocketConnectionCenter(initializer);
			connectionCenter.useHPObjectStreams = true;

			return connectionCenter;
		}

	}

	/**
	 * 返回SocketConnectionCenter在哪一个端口监听连接。
	 * 
	 * 调用这个方法前，你需要先调用startToReceiveConnection()方法，
	 * 否则将抛出ConnectionReceivingFunctionStoppedException。
	 * 
	 */
	public int getConnectionReceivingPort() {
		return ((SocketConnectionReceiver) getConnectionReceiver())
				.getSocketServerPort();
	}

	public int getBroadcastPort() {
		if (!useHPObjectStreams) {
			return ((SocketBroadcaster) getBroadcaster()).getBroadcastPort();
		} else {
			return ((HPObjectStreamSocketBroadcaster) getBroadcaster())
					.getBroadcastPort();
		}

	}

	/**
	 * 设定广播功能所使用的端口。你应该在打开广播功能之前调用这个方法，
	 * 即在调用openBroadcastFunction()方法之前设定广播的端口。否则将
	 * 抛出BroadcastFunctionAlreadyOpenedException异常。
	 * 
	 * @param port
	 *            设定的端口号，应大于1024
	 */
	public void setBroadcastPort(int port) {
		ocs.lockMethod();
		try {
			if (!useHPObjectStreams) {
				((SocketBroadcaster) getBroadcaster()).setBroadcastPort(port);
			} else {
				((HPObjectStreamSocketBroadcaster) getBroadcaster())
						.setBroadcastPort(port);
			}
		} finally {
			ocs.unlockMethod();
		}
	}

	public InetAddress getBroadcastGroupAddress() {
		if (!useHPObjectStreams) {
			return ((SocketBroadcaster) getBroadcaster()).getGroupAddress();
		} else {
			return ((HPObjectStreamSocketBroadcaster) getBroadcaster())
					.getGroupAddress();
		}
	}

	public void setBroadcastGroupAddress(String groupAddress) {
		ocs.lockMethod();
		try {
			if (!useHPObjectStreams) {
				((SocketBroadcaster) getBroadcaster())
						.setGroupAddress(groupAddress);
			} else {
				((HPObjectStreamSocketBroadcaster) getBroadcaster())
						.setGroupAddress(groupAddress);
			}
		} finally {
			ocs.unlockMethod();
		}
	}

	/**
	 * @return 这个SocketConnectionCenter是否使用HPObjectStreams
	 */
	public boolean isUsingHPObjectStreams() {
		return useHPObjectStreams;
	}

}
