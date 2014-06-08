package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.broadcaster.Broadcaster;
import com.accela.tetrixgame.conn.shared.RemotePackage;

public class SocketBroadcaster extends Broadcaster {
	/**
	 * 被广播的对象序列化之后，字节数的上限
	 */
	public static final int MESSAGE_MAX_BYTES = 512;

	/**
	 * 广播端口，所有的Broadcaster都工作在这一接口
	 */
	private int port = 1171;
	/**
	 * 组播地址
	 */
	private String groupAddress = "230.0.0.1";

	private MulticastSocket socket;

	/**
	 * 将一个传入对象序列化，并返回其字节数组。 这个方法不检查是否序列化后的字节数组长度大于MESSAGE_MAX_BYTES
	 * 
	 * @param message
	 *            传入对象
	 * @return 对象序列化后的字节数组
	 * @throws IOException
	 */
	private byte[] serializeObject(Object message) throws IOException {
		if (null == message) {
			throw new NullPointerException();
		}

		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		objectOut.writeObject(message);
		objectOut.flush();
		objectOut.close();
		byteOut.close();

		return byteOut.toByteArray();
	}

	/**
	 * 将对象反序列化，返回对应的对象。
	 * 
	 * @param message
	 *            被序列化的对象的字节数组
	 * @param length
	 *            字节数据的长度
	 * @return 反序列化后被还原的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Object unserializeObject(byte[] message, int length)
			throws IOException, ClassNotFoundException {
		if (null == message) {
			throw new NullPointerException("message == null");
		}
		if (length <= 0 || length > message.length) {
			throw new IllegalArgumentException("length is illegal");
		}

		ByteArrayInputStream byteIn = new ByteArrayInputStream(message, 0,
				length);
		ObjectInputStream objectIn = new ObjectInputStream(byteIn);
		Object ret = objectIn.readObject();
		objectIn.close();
		byteIn.close();

		return ret;
	}

	@Override
	protected void broadcastMessageImpl(Object message) throws Exception {
		byte[] data = serializeObject(message);
		if (data.length > MESSAGE_MAX_BYTES) {
			throw new MessageTooLargeException("message after serizalization("
					+ data.length + " bytes ) should be less than "
					+ MESSAGE_MAX_BYTES + " bytes");
		}

		Common.LOG.info("SocketBroadcaster: broadcastMessageImpl() invoked");
		socket.send(new DatagramPacket(data, data.length, InetAddress
				.getByName(groupAddress), port));
	}

	@Override
	protected void closeImpl() throws IOException {
		try {
			InetAddress group = InetAddress.getByName(groupAddress);
			socket.leaveGroup(group);
		} finally {
			socket.close();
			socket = null;
		}
	}

	@Override
	protected void openImpl() throws IOException {
		socket = new MulticastSocket(port);
		InetAddress group = InetAddress.getByName(groupAddress);
		socket.joinGroup(group);
	}

	@Override
	protected RemotePackage receiveMessageFromBroadcast() throws IOException,
			ClassNotFoundException {
		DatagramPacket dataPacket = new DatagramPacket(
				new byte[MESSAGE_MAX_BYTES], MESSAGE_MAX_BYTES);

		socket.receive(dataPacket);
		Common.LOG.info("received broadcast message");

		Object message = unserializeObject(dataPacket.getData(),
				dataPacket.getLength());

		SocketClientID clientID = new SocketClientID(dataPacket.getAddress(),
				dataPacket.getPort());

		return RemotePackage.newObject(clientID, message);
	}

	public int getBroadcastPort() {
		return port;
	}

	public void setBroadcastPort(int port) {
		if (port <= 1024) {
			throw new IllegalArgumentException(
					"port should be greater than 1024");
		}

		ocs.lockMethod();

		try {
			ocs.ensureClosed();

			this.port = port;
		} finally {
			ocs.unlockMethod();
		}
	}

	public InetAddress getGroupAddress() {
		try {
			return InetAddress.getByName(groupAddress);
		} catch (UnknownHostException ex) {
			assert (false);
			throw new IllegalStateException("group cast address is illegal!");
		}
	}

	public void setGroupAddress(String groupAddress) {
		if (null == groupAddress) {
			throw new NullPointerException("groupAddress should not be null");
		}
		InetAddress group = null;
		try {
			group = InetAddress.getByName(groupAddress);
			if (!group.isMulticastAddress()) {
				throw new IllegalArgumentException(
						"groupAddress is not legal broadcast address!");
			}
		} catch (UnknownHostException ex) {
			throw new IllegalArgumentException(
					"groupAddress is not legal broadcast address!");
		}
		assert (group != null);

		ocs.lockMethod();
		try {
			ocs.ensureClosed();

			this.groupAddress = groupAddress;
		} finally {
			ocs.unlockMethod();
		}
	}

	// QUESTION DatagramSocket可以同时发送和接收信息吗？

}
