package com.accela.tetrixgame.conn.connectionLauncher;

import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;

/**
 * 
 * ConnectionLauncher用来向指定的Client发起连接。
 * 
 * 根据具体的网络技术，这个类将会被继承和实现。
 * 
 */
public abstract class ConnectionLauncher {
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * 新建一个连接，这个方法实际上会调用launchConnectionImpl(ClientID) 来完成连接的新建，这个方法只完成一些常规化的操作
	 * 
	 * @param clientID
	 *            远端的主机的ClientID，指明和谁新建连接
	 * @return 经过和Client交涉，得到的建立和Client的连接所必需的信息
	 * @throws FailedToLaunchConnectionException
	 */
	public NewConnectionInfo launchConnection(ClientID clientID)
			throws FailedToLaunchConnectionException {
		if (null == clientID) {
			throw new NullPointerException("clientID should not be null");
		}

		NewConnectionInfo info = null;
		lock.lock();

		try {
			info = launchConnectionImpl(clientID);
		} catch (Exception ex) {
			throw new FailedToLaunchConnectionException(ex);
		} finally {
			lock.unlock();
		}

		if (null == info) {
			assert (false);
			throw new NullPointerException(
					"launchConnectionImpl should not return null!");
		}

		return info;
	}

	/**
	 * 根据具体的网络技术而实现的新建连接的方法。
	 * 
	 * @param clientID
	 *            指明和谁新建连接
	 * @return 经过和Client交涉，得到的建立和Client的连接所必需的信息
	 * @throws Exception
	 */
	protected abstract NewConnectionInfo launchConnectionImpl(ClientID clientID)
			throws Exception;
}
