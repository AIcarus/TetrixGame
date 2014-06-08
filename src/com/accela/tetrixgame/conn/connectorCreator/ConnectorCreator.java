package com.accela.tetrixgame.conn.connectorCreator;

import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.conn.connector.Connector;
import com.accela.tetrixgame.conn.shared.NewConnectionInfo;

/**
 * 
 * ConnectionCenter只会通过这个类来新建 Connector对象。
 * 
 * //Inheritance needed
 * 
 */
public abstract class ConnectorCreator {
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * 
	 * 利用NewConnectionInfo新建一个Connector实例
	 * 
	 * @param info
	 *            用来新建Connector的必要信息
	 * @return 一个新的Connector实例
	 * @throws FailedToCreateConnectorException
	 */
	public Connector createConnector(NewConnectionInfo info)
			throws FailedToCreateConnectorException {
		if (null == info) {
			throw new NullPointerException();
		}
		Connector connector = null;
		lock.lock();
		try {
			connector = createConnectorImpl(info);
		} catch (Exception ex) {
			throw new FailedToCreateConnectorException(ex);
		} finally {
			lock.unlock();
		}

		if (null == connector) {
			assert (false);
			throw new NullPointerException(
					"createConnectorImpl should not return null!");
		}

		return connector;
	}

	/**
	 * 根据具体网络技术实现的新建Connector的方法。
	 * 
	 * 注意：不要试图利用被释放的Connector，比如对象缓冲区，因为 Connector即使被关闭，缓冲区中仍然可能存储有正在信息，而且
	 * 这个信息有可能正在被使用。
	 */
	protected abstract Connector createConnectorImpl(NewConnectionInfo info)
			throws Exception;

}
