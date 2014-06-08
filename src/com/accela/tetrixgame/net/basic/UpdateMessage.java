package com.accela.tetrixgame.net.basic;

import com.accela.tetrixgame.util.KeyList;
import com.accela.tetrixgame.util.ObjectPool;

/**
 * 
 * 用于记录游戏的记录信息。实际上记录信息由发送记录信息 的时候的游戏时刻和控制键信息组成。
 * 
 * 根据记录信息可以重现整个游戏。
 * 
 */
public class UpdateMessage implements Cloneable {
	private long curTimeCount = 0;

	private KeyList keyList = new KeyList();

	protected UpdateMessage() {

	}

	protected void reset(long curTimeCount, KeyList keyList) {
		if (curTimeCount < 0) {
			throw new IllegalArgumentException(
					"curTimeCount should not be negtive");
		}
		if (null == keyList) {
			throw new NullPointerException("keyQueue should not be null");
		}

		this.curTimeCount = curTimeCount;
		this.keyList.addAll(keyList);
	}

	public KeyList getKeyList() {
		return keyList;
	}

	public long getCurTimeCount() {
		return curTimeCount;
	}

	@Override
	public UpdateMessage clone() {
		UpdateMessage m = create(this.curTimeCount, this.keyList);

		assert (this.keyList != m.keyList);
		assert (this != m);
		return m;
	}

	// ////////////////////////////////////////////////////////////////////////////////

	private static UpdateMessage retrieve() {
		UpdateMessage m = ObjectPool.retrieve(UpdateMessage.class);
		if (null == m) {
			m = new UpdateMessage();
		}

		assert (m.keyList != null);
		m.keyList.clear();

		return m;
	}

	public static UpdateMessage create(long curTimeCount, KeyList keyList) {
		UpdateMessage m = retrieve();
		assert (m != null);
		assert (m.keyList != null);

		m.reset(curTimeCount, keyList);

		return m;
	}

	public static void dispose(UpdateMessage m) {
		if (null == m) {
			throw new NullPointerException("m should not be null");
		}

		ObjectPool.put(m);

	}

}
