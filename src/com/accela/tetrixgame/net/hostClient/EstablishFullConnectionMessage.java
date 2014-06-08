package com.accela.tetrixgame.net.hostClient;

import java.util.List;

/**
 * 
 * TetrixNetHost发送这个消息来通知所有与其连接的TetrixNetClient， 应当开始建立全连接
 * 
 */
public class EstablishFullConnectionMessage {
	private ClientIdentifyMessage[] clientIdentifyMessages;

	@SuppressWarnings("unused")
	private EstablishFullConnectionMessage() {

	}

	public EstablishFullConnectionMessage(
			List<ClientIdentifyMessage> clientIdentifyMessages) {
		if (null == clientIdentifyMessages) {
			throw new NullPointerException(
					"clientIdentifyMessages should not be null");
		}

		this.clientIdentifyMessages = clientIdentifyMessages.toArray(new ClientIdentifyMessage[0]);
	}

	public ClientIdentifyMessage[] getClientIdentifyMessages() {
		return clientIdentifyMessages;
	}

}
