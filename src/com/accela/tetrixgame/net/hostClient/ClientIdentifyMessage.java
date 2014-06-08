package com.accela.tetrixgame.net.hostClient;

import java.io.IOException;

import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketClientID;

/**
 * 
 * 当一个TetrixNetClient连接到一个TetrixNetHost后，会把自己的身份信息
 * 发送给TetrixNetHost，这个类表示TetrixNetClient的身份信息。
 * 
 */
public class ClientIdentifyMessage {
	// use String so that I can serialize it using HPObjectStreamSocketConnector
	private String clientReceivingID; // 指如何连接到client的clientID。假如此时你与该client已经建立了连接，该连接中的clientID和这个ClientID未必相等

	@SuppressWarnings("unused")
	private ClientIdentifyMessage() {

	}

	public ClientIdentifyMessage(ClientID clientReceivingID) {
		if (null == clientReceivingID) {
			throw new NullPointerException(
					"clientReceivingID should not be null");
		}
		assert(clientReceivingID instanceof SocketClientID);

		this.clientReceivingID = clientReceivingID.serializeToString();
	}

	public ClientID getClientReceivingID() {
		try {
			return new SocketClientID(clientReceivingID);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
