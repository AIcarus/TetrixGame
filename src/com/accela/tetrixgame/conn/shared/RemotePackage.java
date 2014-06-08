package com.accela.tetrixgame.conn.shared;

import java.util.Stack;

/**
 * RemotePackage是网络上传送的信息的表示形式， 封装了clientID和message。
 * 
 * RemotePackage是不可变类
 * 
 * 
 */
public class RemotePackage {
	private ClientID clientID;
	private Object message;

	private RemotePackage() {
		// this constructor is reserved for HPObjectStreams
	}

	protected RemotePackage(ClientID clientID, Object message) {
		this();

		if (null == clientID) {
			throw new NullPointerException("clientID is null");
		}
		if (null == message) {
			throw new NullPointerException("message is null");
		}

		this.clientID = clientID;
		this.message = message;
	}

	public ClientID getClientID() {
		return clientID;
	}

	public Object getMessage() {
		return message;
	}

	public String toString() {
		String out = "";
		out += "RemotePackage[" + "clientID=" + clientID.toString()
				+ ", message=" + message.toString() + "]";
		return out;
	}

	// ///////////////////////////////////////////////////////////////////

	// TODO 使用对象池，以及类中的私有对象缓冲的时候，
	// 千万不要忘了，从池中去除的对象要初始化!!!!!!!!

	private static Stack<RemotePackage> buffer = new Stack<RemotePackage>();

	public synchronized static RemotePackage newObject(ClientID clientID,
			Object message) {
		if (null == clientID) {
			throw new NullPointerException("clientID is null");
		}
		if (null == message) {
			throw new NullPointerException("message is null");
		}

		if (buffer.isEmpty()) {
			return new RemotePackage(clientID, message);
		} else {
			RemotePackage pack = buffer.pop();
			assert (pack != null);

			pack.clientID = clientID;
			pack.message = message;

			return pack;
		}
	}

	public synchronized static void deleteObject(RemotePackage pack) {
		if (null == pack) {
			throw new NullPointerException("pack should not be null");
		}

		buffer.push(pack);
	}

}
