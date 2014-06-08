package com.accela.tetrixgame.net.basic;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.GameLogic;
import com.accela.tetrixgame.util.GlobalUpdater;
import com.accela.tetrixgame.util.IntervalTimer;
import com.accela.tetrixgame.util.KeyList;
import com.accela.tetrixgame.util.TimeCounter;

/**
 * 
 * 在网络中，玩家操纵自己所玩的一场游戏，同时可以观看到其他人的游戏。 玩家自己操纵的游戏用TetrixEngine来驱动，而观看的别人的游戏用
 * TetrixEngineMirror来驱动。
 * 
 * TetrixEngine包含了一场可以由玩家控制的，完整的游戏。你需要按照TetrixEngine
 * 规定的刷新周期不断刷新它，同时把玩家的按键也传递给它。用JPanel显示
 * TetrixEngine的游戏的时候，通过updateAndPaint方法就可以。这个方法实际上 是把刷新和绘制绑定到了一起。
 * 
 * TetrxEngine输出一场游戏的所有信息，放在messageQueue中，一个TetrixEngineMirror
 * 利用这些信息可以完整地再现一场游戏。
 * 
 * TetrixEngine是如何记录游戏信息的呢？
 * TetrixEngine首先会记录玩家的控制键，但这还不够，在网络上，还需要每隔MAX_MESSAGE_INTERVAL
 * 发送一个时间同步包给TetrixEngineMirror。因此，TetrixEngine每当有控制键，或者生成上一个
 * UpdateMessage时间到现在已经打到MAX_MESSAGE_INTERVAL时，就会生成一个UpdateMessage。
 * 这些UpdateMessage加在一起，就构成了对整个游戏的记录。同时，重现游戏还需要游戏启动时的设置信息，
 * 即初始化TetrixGameLogic所用的TetrixGameAttributes对象。
 * 
 * 在SharedPanel中，可以看到TetrixEngine和TetrixEngineMirror的工作模式的实例
 */
public class Engine {
	/**
	 * TetrixEngine将游戏的信息输出到这个队列。把游戏启动时的TetrixGameAttributes对象
	 * 和messageQueue中的所有信息传给TetrixEngineMirror，就可以重放一场游戏。
	 */
	private BlockingQueue<UpdateMessage> messageQueue = new LinkedBlockingQueue<UpdateMessage>();
	/**
	 * 记录一个刷新周期中按下和松开的键
	 */
	private KeyList keyList = new KeyList();
	/**
	 * TetrixEngine实际上是在gamelogic上加了一层封装
	 */
	private GameLogic game;
	/**
	 * 刷新周期
	 */
	public static final long UPDATE_INTERVAL = GlobalUpdater.UPDATE_INTERVAL;
	/**
	 * TetrixEngine产生记录游戏的信息的时候，设定的两个信息包之间的最大时间间隔。
	 */
	public static final long MAX_MESSAGE_INTERVAL = Math.max(1,
			(1000 / UPDATE_INTERVAL)) * UPDATE_INTERVAL; // 必须是UPDATE_INTERVAL的整数倍
	/**
	 * 用于记录游戏进行了多少时间
	 */
	private TimeCounter timeCounter = new TimeCounter(MAX_MESSAGE_INTERVAL);
	/**
	 * 用来获得这一次刷新与上一次刷新的系统时间的时间间隔
	 */
	private IntervalTimer intervalTimer = new IntervalTimer();

	public Engine() {
		game = new GameLogic();
	}

	public Engine(GameAttributes attr) {
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}

		game = new GameLogic(attr);
	}

	public synchronized void keyPressed(int keyCode) {
		keyList.keyPressed(keyCode);
	}

	public synchronized void keyReleased(int keyCode) {
		keyList.keyReleased(keyCode);
	}

	public int getGameFieldHeight() {
		return game.getGameFieldHeight();
	}

	public int getGameFieldWidth() {
		return game.getGameFieldWidth();
	}

	/**
	 * 
	 * 调用这个方法，会使游戏刷新一次，并且绘制一次。
	 * 
	 * 这个方法的方法做了一个改进：本来，应该每隔UPDATE_INTERVAL调用一次这个方法，而且
	 * 每次调用这个方法都会运行一次updateImpl。但是我发现，其实调用这个方法的时间间隔
	 * 并不一定是UPDATE_INTERVAL，有时甚至是它的几倍。因此，我把这个方法改为，每调用
	 * 一次这个方法，会调用多次updateImpl方法，因为调用一次updateImpl方法就好像使游戏
	 * 刷新了UPDATE_INTERVAL毫秒，因此调用updateImpl的次数由调用updateAndPaint方法的
	 * 时间间隔除以UPDATE_INTERVAL确定。TetrixEngineMirror的updateAndPaint使用的是相 同的办法。
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
		Common.LOG.info("Engine Interval: "+intervalTimer.getInterval()); // the log frequency is too high

		final boolean FRAME_JUMP = true;
		long updateCount = 0;
		if (FRAME_JUMP) {
			updateCount = Math.round((double) intervalTimer.getInterval()
					/ (double) UPDATE_INTERVAL);
		} else {
			updateCount = 1;
		}

		if (updateCount > 0) {
			for (long i = 0; i < updateCount; i++) {
				updateImpl();
			}
		} else {
			// do nothing
		}

		// 绘制
		if (g != null && orign != null && observer != null && scale > 0) {
			game.paintAll(g, orign, observer, scale);
		}

	}

	private void updateImpl() {
		// 刷新计时器
		timeCounter.update(UPDATE_INTERVAL);

		assert (UPDATE_INTERVAL == timeCounter.getInterval());

		// 刷新游戏
		if (timeCounter.getInterval() >= 0) {
			game.update(keyList, timeCounter.getInterval());
		}

		// 根据当前时间及控制键生成消息
		if (timeCounter.isBeep() || !keyList.isEmpty()) {
			timeCounter.clearBeep();

			assert (!timeCounter.isBeep() || timeCounter.getCurTimeCount()
					% MAX_MESSAGE_INTERVAL == 0);

			UpdateMessage m = UpdateMessage.create(
					timeCounter.getCurTimeCount(), keyList);
			assert (m != null);

			messageQueue.offer(m);
		}

		// 清理控制键
		keyList.clear();

	}

	public boolean hasMoreMessageToRetrieve() {
		return !messageQueue.isEmpty();
	}

	/**
	 * 取出一个messageQueue中的UpdateMessage
	 * 
	 * @throws InterruptedException
	 */
	public UpdateMessage retrieveUpdateMessage() throws InterruptedException {// TODO 别忘了dispose取出的Message, 别忘了RemotePackage的dispose
		return messageQueue.take();
	}

	public boolean isGameOver() {
		return game.isGameOver();
	}

}
