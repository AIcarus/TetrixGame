package com.accela.tetrixgame.conn;

import com.accela.tetrixgame.conn.broadcaster.Broadcaster;
import com.accela.tetrixgame.conn.connectionLauncher.ConnectionLauncher;
import com.accela.tetrixgame.conn.connectionReceiver.ConnectionReceiver;
import com.accela.tetrixgame.conn.connectorCreator.ConnectorCreator;

/**
 * 
 * ConnectionCenter的初始化器，用来指定各个ConnectionCenter的组件
 * 
 */
public abstract class ConnectionCenterInitializer {
	/**
	 * 指定一个ConnectorCreator对象，它作为ConnectionCenter的基本功能组件
	 */
	public abstract ConnectorCreator specifyConnectorCreator();

	/**
	 * 同上
	 */
	public abstract ConnectionReceiver specifyConnectionReceiver();

	/**
	 * 同上
	 */
	public abstract ConnectionLauncher specifyConnectionLauncher();

	/**
	 * 同上
	 */
	public abstract Broadcaster specifyBroadcaster();

}
