package com.accela.tetrixgame.bot;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.accela.tetrixgame.net.basic.EngineMirror;
import com.accela.tetrixgame.net.basic.UpdateMessage;

/**
 * 
 * 这个类给BOTRunningStation使用。 BOT只需要控制自己的游戏，不需要看到其他玩家的游戏，因此
 * 用这个类的实例来替换TetrixRunningStation中原有的TetrixEngineMirror，
 * 既能够保证TetrixRunningStation正常工作，又能够是TetrixEngineMirror 失效，从而减少无用功。
 * 
 */
public class FalseMirror extends EngineMirror {
	private int gameFieldWidth;

	private int gameFieldHeight;

	public FalseMirror(int gameFieldWidth, int gameFieldHeight) {
		super();

		this.gameFieldWidth = gameFieldWidth;
		this.gameFieldHeight = gameFieldHeight;

		clearFatherFields();
	}

	private void clearFatherFields() {
		Field[] fatherFields = this.getClass().getSuperclass()
				.getDeclaredFields();
		for (Field field : fatherFields) {
			field.setAccessible(true);

			if (Modifier.isFinal(field.getModifiers())
					|| Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			if (field.getType().isPrimitive()) {
				continue;
			}

			try {
				field.set(this, null);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
				assert (false);
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
				assert (false);
			}
		}

	}

	@Override
	public void enforceGameOver() {
		// do nothing
	}

	@Override
	public int getGameFieldHeight() {
		return gameFieldHeight;
	}

	@Override
	public int getGameFieldWidth() {
		return gameFieldWidth;
	}

	@Override
	public int getMessageQueueSize() {
		return 0;
	}

	@Override
	public boolean isGameOver() {
		return true;
	}

	@Override
	public void putUpdateMessage(UpdateMessage m, int sizeLimit)
			throws InterruptedException {
		if (m != null) {
			UpdateMessage.dispose(m);
		}
	}

	@Override
	public void putUpdateMessage(UpdateMessage m) {
		if (m != null) {
			UpdateMessage.dispose(m);
		}
	}

	@Override
	public synchronized void updateAndPaint(Graphics g, Point orign,
			ImageObserver observer, double scale) {
		return;
	}

}