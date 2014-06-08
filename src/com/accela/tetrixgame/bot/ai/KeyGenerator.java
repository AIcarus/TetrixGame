package com.accela.tetrixgame.bot.ai;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import com.accela.tetrixgame.util.KeyList;

/**
 * 
 * 这个类的功能是将MovingStrategy转化为相应的按键， 以便把按键信息传给TetrixEngine。
 * 
 */
public class KeyGenerator {
	public static final Integer LEFT = KeyEvent.VK_LEFT; // TODO
															// 这里的控制键没有和gamelogic中的一致，而是使用冗余信息

	public static final Integer RIGHT = KeyEvent.VK_RIGHT;

	public static final Integer DOWN = KeyEvent.VK_DOWN;

	public static final Integer SCROLL = KeyEvent.VK_UP;

	private final Map<Object, Integer> keyMap = new HashMap<Object, Integer>();

	public KeyGenerator() {
		initKeyMap();
	}

	private void initKeyMap() {
		keyMap.put(MovingStrategy.HerizontalStrategy.LEFT, LEFT);
		keyMap.put(MovingStrategy.HerizontalStrategy.RIGHT, RIGHT);

		keyMap.put(MovingStrategy.VerticalStrategy.DOWN_ACC, DOWN);
		keyMap.put(MovingStrategy.ScrollStrategy.SCROLL, SCROLL);
	}

	public synchronized void generateKeys(KeyList result, MovingStrategy last,
			MovingStrategy cur) {
		if (null == result) {
			throw new NullPointerException("keyList should not be null");
		}
		if (null == last) {
			throw new NullPointerException("last should not be null");
		}
		if (null == cur) {
			throw new NullPointerException("cur should not be null");
		}
		if (last == cur) {
			throw new IllegalArgumentException(
					"last should not be identical with cur");
		}

		result.clear();

		last.sweepNull();
		cur.sweepNull();

		// 处理水平方向
		if (last.hs != cur.hs) {
			Integer releasedKey = keyMap.get(last.hs);
			Integer pressedKey = keyMap.get(cur.hs);

			assert (last.hs == MovingStrategy.HerizontalStrategy.NONE || releasedKey != null);
			assert (cur.hs == MovingStrategy.HerizontalStrategy.NONE || pressedKey != null);

			if (releasedKey != null) {
				result.keyReleased(releasedKey);
			}
			if (pressedKey != null) {
				result.keyPressed(pressedKey);
			}
		}

		// 处理竖直方向
		if (last.vs != cur.vs) {
			Integer releasedKey = keyMap.get(last.vs);
			Integer pressedKey = keyMap.get(cur.vs);

			assert (last.vs == MovingStrategy.VerticalStrategy.NONE || releasedKey != null);
			assert (cur.vs == MovingStrategy.VerticalStrategy.NONE || pressedKey != null);

			if (releasedKey != null) {
				result.keyReleased(releasedKey);
			}
			if (pressedKey != null) {
				result.keyPressed(pressedKey);
			}
		}

		// 处理翻转
		if (last.ss != cur.ss) {
			Integer releasedKey = keyMap.get(last.ss);
			Integer pressedKey = keyMap.get(cur.ss);

			assert (last.ss == MovingStrategy.ScrollStrategy.NONE || releasedKey != null);
			assert (cur.ss == MovingStrategy.ScrollStrategy.NONE || pressedKey != null);

			if (releasedKey != null) {
				result.keyReleased(releasedKey);
			}
			if (pressedKey != null) {
				result.keyPressed(pressedKey);
			}
		}

	}

}
