package com.accela.tetrixgame.bot;

import com.accela.tetrixgame.net.basic.UpdateMessage;
import com.accela.tetrixgame.util.KeyList;
import com.accela.tetrixgame.util.ObjectPool;

/**
 * 
 * BOT之间传递的UpdateMessage与普通的TetrixRunningStation
 * 之间所传播的UpdateMessage不同。它们传递的是BOTUpdateMessage。 这有利于BOT之间识别出自己的成员。
 * 
 */
public class BOTUpdateMessage extends UpdateMessage {
	private BOTUpdateMessage() {

	}

	// ////////////////////////////////////////////////////////////////////////////////

	private static BOTUpdateMessage retrieve() {
		BOTUpdateMessage m = ObjectPool.retrieve(BOTUpdateMessage.class);
		if (null == m) {
			m = new BOTUpdateMessage();
		}

		assert (m.getKeyList() != null);
		m.getKeyList().clear();

		return m;
	}

	public static BOTUpdateMessage create(long curTimeCount, KeyList keyList) {
		BOTUpdateMessage m = retrieve();
		assert (m != null);
		assert (m.getKeyList() != null);

		m.reset(curTimeCount, keyList);

		return m;
	}

	public static void dispose(BOTUpdateMessage m) {
		if (null == m) {
			throw new NullPointerException("m should not be null");
		}

		ObjectPool.put(m);
	}

}
