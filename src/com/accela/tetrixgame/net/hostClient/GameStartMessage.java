package com.accela.tetrixgame.net.hostClient;

import com.accela.tetrixgame.gamelogic.GameAttributes;

/**
 * 
 * TetrixNetHost发送这个消息来告诉TetrixNetClient应当开始游戏了。
 * 
 */
public class GameStartMessage {
	private GameAttributes attr;

	@SuppressWarnings("unused")
	private GameStartMessage() {

	}

	public GameStartMessage(GameAttributes attr) {
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}

		this.attr = attr;
	}

	public GameAttributes getAttr() {
		return attr;
	}

}
