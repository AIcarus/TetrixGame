package com.accela.tetrixgame.net.hostClient;

import java.io.IOException;

import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketClientID;

/**
 * 
 * TetrixHostNet会不断广播这个信息，来告知其他人自己的存在， 以及如何连接上自己。
 * 
 */
public class HostBroadcastMessage {
	@Override
	public String toString() {
		return "HostBroadcastMessage [hostReceivingID=" + hostReceivingID + "]";
	}

	private String hostReceivingID;	// use string so that HPObjectStreamSocketConnector can serialize it

	@SuppressWarnings("unused")
	private HostBroadcastMessage() {

	}

	public HostBroadcastMessage(ClientID hostReceivingID) {
		if (null == hostReceivingID) {
			throw new NullPointerException("hostReceivingID should not be null");
		}
		assert(hostReceivingID instanceof SocketClientID);
		this.hostReceivingID = hostReceivingID.serializeToString();
	}

	public ClientID getHostReceivingID() {
		try {
			return new SocketClientID(hostReceivingID);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
