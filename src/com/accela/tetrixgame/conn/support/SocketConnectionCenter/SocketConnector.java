package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.connector.ClientDisconnectException;
import com.accela.tetrixgame.conn.connector.Connector;
import com.accela.tetrixgame.conn.shared.ClientID;

public class SocketConnector extends Connector {
	private Socket client;
	private DelayedObjectInputStream inputStream;
	private ObjectOutputStream outputStream;

	public SocketConnector(Socket client) {
		if (null == client) {
			throw new NullPointerException();
		}

		this.client = client;
	}

	@Override
	protected void closeImpl() throws IOException {
		try {
			inputStream.close();
			inputStream = null;
			outputStream.close();
			outputStream = null;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException ex) {
				throw ex;
			}

		}
	}

	@Override
	protected ClientID getClientIDImpl() {
		if (null == client) {
			throw new NullPointerException(
					"SocketConnector's property client is null.");
		}
		SocketClientID clientID = new SocketClientID(client.getInetAddress(),
				client.getPort());
		return clientID;
	}

	@Override
	protected void openImpl() {
		if (!SocketConnectionCenterUtilities.checkOpened(client)) {
			throw new IllegalStateException(
					"the socket used in SocketConnector is not opened");
		}

		try {
			inputStream = new DelayedObjectInputStream(new BufferedInputStream(
					client.getInputStream()));
			outputStream = new ObjectOutputStream(new BufferedOutputStream(
					client.getOutputStream()));
		} catch (IOException ex) {
			throw new IllegalStateException(
					"failed to create input or output stream");
		}

	}

	@Override
	protected Object receiveMessageFromClient() throws IOException,
			ClassNotFoundException, ClientDisconnectException {
		try {
			Object message = inputStream.readObject();
			Common.LOG.info("SocketConnector received message: "+message);
			return message;
		} catch (EOFException ex) {
			throw new ClientDisconnectException();
		}
	}

	@Override
	protected void sendMessageToClient(Object message) throws IOException {
		outputStream.writeObject(message);
		outputStream.flush();
	}

}
