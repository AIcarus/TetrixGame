package com.accela.tetrixgame.net.basic;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;

import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.GameLogic;
import com.accela.tetrixgame.util.BufferQueue;
import com.accela.tetrixgame.util.IntervalTimer;
import com.accela.tetrixgame.util.TimeCounter;

/**
 * 
 * TetrixEngineMirror的作用和TetrixEngine相对应。它用来播放
 * TetrixEngine产生的UpdateMessage，即重现一场游戏。在网络上，
 * 一端的玩家控制TetrixEngine，产生的游戏记录信息产送给另一端
 * 的TetrixEngineMirror，TetrixEngineMirror播放这些信息，就 可以是这一端的玩家观看到对方的游戏。
 * 
 */
public class EngineMirror {
	/**
	 * 设定缓冲队列的缓冲长度
	 */
	public static final int BUFFER_LENGTH = 1;
	/**
	 * 用来存放外进发送过来的游戏记录信息UpdateMessage的队列。这个队列起到缓冲作用。
	 */
	private BufferQueue<UpdateMessage> messageQueue = new BufferQueue<UpdateMessage>(
			BUFFER_LENGTH);

	private GameLogic game;
	/**
	 * TetrixEngineMirror的刷新周期与TetrixEngine相同。
	 */
	public static final long UPDATE_INTERVAL = Engine.UPDATE_INTERVAL;
	/**
	 * 用来记录游戏当前进行到的时间
	 */
	private TimeCounter timeCounter = new TimeCounter();
	/**
	 * 用来获得这一次刷新与上一次刷新的系统时间的时间间隔
	 */
	private IntervalTimer intervalTimer = new IntervalTimer();
	/**
	 * 是否强制游戏结束了，强制游戏结束后，可以不再根据messageQueue来运行。
	 */
	private boolean enforcedGameOver = false;

	public EngineMirror() {
		this(null, true, true);
	}

	public EngineMirror(GameAttributes attr) {
		this(attr, true, true);
	}

	public EngineMirror(GameAttributes attr, boolean lowEffect,
			boolean noSound) {
		if (null == attr) {
			attr = new GameAttributes();
		} else {
			attr = attr.clone();
		}

		if (lowEffect) {
			attr.put(GameAttributes.ENABLE_EFFECTS, "false");
		} else {
			attr.put(GameAttributes.ENABLE_EFFECTS, "true");
		}

		if (noSound) {
			attr.put(GameAttributes.ENABLE_SOUND, "false");
		} else {
			attr.put(GameAttributes.ENABLE_SOUND, "true");
		}

		game = new GameLogic(attr);

	}

	public int getGameFieldHeight() {
		return game.getGameFieldHeight();
	}

	public int getGameFieldWidth() {
		return game.getGameFieldWidth();
	}

	/**
	 * 刷新并绘制游戏。如果当前messageQueue中的信息已经用完，则直接返回， 不会造成阻塞
	 * 
	 * @param g
	 * @param orign
	 *            游戏绘制时的原点
	 * @param observer
	 * @param scale
	 *            游戏绘制出来的图像的缩放比例
	 */
	public synchronized void updateAndPaint(Graphics g, Point orign,
			ImageObserver observer, double scale) {
		intervalTimer.update();
		final boolean FRAME_JUMP = true;
		long updateCount = 0;
		if (FRAME_JUMP) {
			updateCount = Math.round((double) intervalTimer.getInterval()
					/ (double) UPDATE_INTERVAL);
		} else {
			updateCount = 1;
		}

		for (long i = 0; i < updateCount; i++) {
			if (enforcedGameOver && messageQueue.size() <= 0) {
				enforcedGameOverUpdateImpl();
			} else {
				updateImpl();
			}
		}

		// 绘制
		if (g != null && orign != null && observer != null && scale > 0) {
			game.paintAll(g, orign, observer, scale);
		}

	}

	private void updateImpl() {
		// 刷新游戏
		UpdateMessage peekedMessage = messageQueue.peek();
		if (peekedMessage != null) {
			// 更新计时器
			timeCounter.update(UPDATE_INTERVAL);

			if (timeCounter.getCurTimeCount() >= peekedMessage
					.getCurTimeCount()) {
				assert (timeCounter.getCurTimeCount() == peekedMessage
						.getCurTimeCount());

				UpdateMessage m = messageQueue.dequeue();
				assert (m != null);
				assert (m == peekedMessage);

				assert (timeCounter.getInterval() >= 0);

				game.update(m.getKeyList(), timeCounter.getInterval());

				// 释放UpdateMessage
				UpdateMessage.dispose(m);
			} else {
				if (timeCounter.getInterval() >= 0) {
					game.update(null, timeCounter.getInterval());
				}
			}
		}
	}

	// 记录是否第一次进入enforceGameOverUpdateImpl方法
	private boolean firstInEnforceGameOverUpdateImpl = true;

	// 记录强制游戏结束前的等待时间
	private long enforcedGameOverUpdateImplWaitTime = 4 * 1000;

	private void enforcedGameOverUpdateImpl() {
		// 强制游戏结束前会等待一段时间
		if (enforcedGameOverUpdateImplWaitTime > 0) {
			enforcedGameOverUpdateImplWaitTime -= UPDATE_INTERVAL;
			return;
		}

		// 使game强制游戏结束
		if (firstInEnforceGameOverUpdateImpl) {
			game.enforceGameOver();
			firstInEnforceGameOverUpdateImpl = false;
		}

		// 刷新计时器
		timeCounter.update(UPDATE_INTERVAL);

		assert (UPDATE_INTERVAL == timeCounter.getInterval());

		// 刷新游戏
		if (timeCounter.getInterval() >= 0) {
			game.update(null, timeCounter.getInterval());
		}

		// 清理UpdateMessage，尽管理论上此时messageQueue应该已经被用光
		assert (messageQueue.size() == 0);
		while (messageQueue.size() > 0) {
			UpdateMessage.dispose(messageQueue.dequeue());
		}
	}

	/**
	 * 外界放入TetrixEngine发送过来的游戏记录信息
	 */
	public void putUpdateMessage(UpdateMessage m) {
		if (null == m) {
			throw new NullPointerException("m should not be null");
		}

		if (enforcedGameOver) {
			UpdateMessage.dispose(m);
		}

		// TODO 注意不要把同一个UpdaeMessage放入多个Mirror中，这会导致这个
		// UpdateMessage多次被dispose

		messageQueue.enqueue(m);

	}

	/**
	 * 外界放入TetrixEngine发送过来的游戏记录信息。 同时规定缓冲存储的最大信息对象个数，如果超过
	 * 这个个数，就会阻塞，信息缓冲区空出足够的地方。
	 * 
	 * @throws InterruptedException
	 */
	public void putUpdateMessage(UpdateMessage m, int sizeLimit)
			throws InterruptedException {
		if (null == m) {
			throw new NullPointerException("m should not be null");
		}
		if (sizeLimit <= 0) {
			throw new IllegalArgumentException("sizeLimit should be positive");
		}

		if (enforcedGameOver) {
			UpdateMessage.dispose(m);
		}

		messageQueue.put(m, sizeLimit);

	}

	public boolean isGameOver() {
		return game.isGameOver();
	}

	/**
	 * TetrixEngineMirror的强制游戏结束是这样的: 先正常进行游戏直到messageQueue用完，然后再
	 * 等待几秒钟不动，最后利用gamelogic的 强制游戏结束来结束游戏。此后会无视messageQueue 中是否还有消息。
	 */
	public void enforceGameOver() {
		enforcedGameOver = true;
	}

	public int getMessageQueueSize() {
		return messageQueue.size();
	}

}
