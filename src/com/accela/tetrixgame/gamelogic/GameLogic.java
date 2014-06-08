package com.accela.tetrixgame.gamelogic;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.ImageObserver;
import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.gamelogic.control.Control;
import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.state.BarDroppingStateHandler;
import com.accela.tetrixgame.gamelogic.state.CollidingStateHandler;
import com.accela.tetrixgame.gamelogic.state.GameOverRemovingStateHandler;
import com.accela.tetrixgame.gamelogic.state.GameOverStateHandler;
import com.accela.tetrixgame.gamelogic.state.GeneratingStateHandler;
import com.accela.tetrixgame.gamelogic.state.RemovingAndFallingStateHandler;
import com.accela.tetrixgame.gamelogic.state.State;
import com.accela.tetrixgame.gamelogic.state.StateHandlerBox;
import com.accela.tetrixgame.gamelogic.uisupport.PaintEveryThing;
import com.accela.tetrixgame.util.KeyList;

/**
 * 
 * 游戏的整个模块就装在这个类里，你只需要新建一个这个类的对象，定时刷新这个对象，并且 画出这个对象，就行了。 游戏的主体结构是：
 * 以GameData存储数据为中心，整个游戏分为有几个状态构成的有限状态机，用StateHandler 来操纵状态。
 * 
 * 游戏的方块存储在BlockContainer中，方块的移动由移动代理来负责，它们根据水平、竖直、
 * 翻转三种正交的移动动作分类，分别由HerizontalCollisionMovingProxy、
 * VerticalCollisionMovingProxy以及BarScrollProxy负责。
 * 
 * gamelogic支持线程同步
 * 
 */
public class GameLogic {
	/**
	 * 处理游戏状态的模块
	 */
	private StateHandlerBox stateBox = new StateHandlerBox();

	/**
	 * 游戏的数据
	 */
	private GameData gameData;

	/**
	 * 处理游戏的绘画
	 */
	private PaintEveryThing painter = new PaintEveryThing();

	public static final int PAUSE_KEY = KeyEvent.VK_P;
	/**
	 * 游戏是否暂停状态
	 */
	private boolean pause = false;
	/**
	 * 当游戏刚刚建立的时候，自动作为暂停状态，当有人按下一个键的时候， 才真正开始运行起来。
	 */
	private boolean initLock = true;

	/**
	 * 为了使update方法和paintAll方法各自以及互相同步执行
	 */
	private ReentrantLock synLock = new ReentrantLock();
	/**
	 * 在游戏中显示的提示，因为有很多人提出申请要出现提示，因此使用栈来管理
	 */
	private String hint = null;

	public GameLogic() {
		this(new GameAttributes());
	}

	public GameLogic(GameAttributes attr) {
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}

		gameData = new GameData(
				Integer.parseInt(attr.get(GameAttributes.RANDOM_SEED)),
				Long.parseLong(attr.get(GameAttributes.H_MAX_ACC_TIME)),
				Double.parseDouble(attr.get(GameAttributes.H_MAX_VEL)),
				Long.parseLong(attr.get(GameAttributes.V_MAX_ACC_TIME)),
				Double.parseDouble(attr.get(GameAttributes.V_NORMAL_VEL)),
				Double.parseDouble(attr.get(GameAttributes.V_MAX_VEL)),
				Integer.parseInt(attr.get(GameAttributes.ROW_NUM)),
				Integer.parseInt(attr.get(GameAttributes.COLUMN_NUM)),
				Integer.parseInt(attr.get(GameAttributes.BAR_ROW_NUM)),
				Integer.parseInt(attr.get(GameAttributes.BAR_COLUMN_NUM)),
				Integer.parseInt(attr.get(GameAttributes.BLOCK_TYPE_NUM)),
				Integer.parseInt(attr.get(GameAttributes.BLOCK_WIDTH)),
				Integer.parseInt(attr.get(GameAttributes.BLOCK_HEIGHT)),
				Double.parseDouble(attr.get(GameAttributes.GRAVITY)),
				Double.parseDouble(attr
						.get(GameAttributes.SCROLL_MAX_VEL)),
				Long.parseLong(attr.get(GameAttributes.REMOVE_INTERVAL)),
				Double.parseDouble(attr.get(GameAttributes.EXPLOSION_VEL)),
				Double.parseDouble(attr
						.get(GameAttributes.EXPLOSION_ADDED_VEL)),

				Integer.parseInt(attr
						.get(GameAttributes.OUTER_SPARK_RAIDUS)),
				Double.parseDouble(attr
						.get(GameAttributes.OUTER_SPARK_ACC)),
				Integer.parseInt(attr
						.get(GameAttributes.INNER_SPARK_RAIDUS)),
				Double.parseDouble(attr
						.get(GameAttributes.INNER_SPARK_ACC)),
				Double.parseDouble(attr
						.get(GameAttributes.OUTER_SPARK_VEL)),
				Double.parseDouble(attr
						.get(GameAttributes.INNER_SPARK_VEL)),
				Integer.parseInt(attr
						.get(GameAttributes.SPARK_NUM_PER_BLOCK)),
				Double.parseDouble(attr.get(GameAttributes.SPARK_VEL)),
				Double.parseDouble(attr.get(GameAttributes.SPARK_GRAVITY)),
				Long.parseLong(attr
						.get(GameAttributes.SPARK_DELAY_INTERVAL)),

				Double.parseDouble(attr.get(GameAttributes.FIRE_LINE_VEL)),
				Double.parseDouble(attr
						.get(GameAttributes.FIRE_LINE_GRAVITY)), Integer
						.parseInt(attr
								.get(GameAttributes.FIRE_LINE_LENGTH)),
				Integer.parseInt(attr
						.get(GameAttributes.FIRE_LINE_LAYER_NUM)),
				Integer.parseInt(attr
						.get(GameAttributes.FIRE_LINE_NUM_PER_LAYER)),

				Integer.parseInt(attr
						.get(GameAttributes.SHAKE_MAX_OFFSET)),
				Long.parseLong(attr.get(GameAttributes.SHAKE_PERIOD)),
				Integer.parseInt(attr
						.get(GameAttributes.SHAKE_NUM_PERIOD)),
				Long.parseLong(attr
						.get(GameAttributes.GAME_OVER_REMOVE_INTERVAL)),
				Integer.parseInt(attr.get(GameAttributes.GRID_WIDTH)),
				Boolean.parseBoolean(attr
						.get(GameAttributes.ENABLE_EFFECTS)), Boolean
						.parseBoolean(attr
								.get(GameAttributes.ENABLE_SOUND)), Long
						.parseLong(attr.get(GameAttributes.BASE_SCORE)));

		initStateHandlers();
		initDefaultControls();

	}

	private void initStateHandlers() {
		stateBox.addHandler(new GeneratingStateHandler());
		stateBox.addHandler(new BarDroppingStateHandler());
		stateBox.addHandler(new CollidingStateHandler());
		stateBox.addHandler(new RemovingAndFallingStateHandler());
		stateBox.addHandler(new GameOverRemovingStateHandler());
		stateBox.addHandler(new GameOverStateHandler());
	}

	private void initDefaultControls() {
		// 第一套按键
		getControl().setHerizontalKey(KeyEvent.VK_LEFT, HerizontalStatus.LEFT);
		getControl()
				.setHerizontalKey(KeyEvent.VK_RIGHT, HerizontalStatus.RIGHT);

		getControl().setVerticalKey(KeyEvent.VK_DOWN, VerticalStatus.DOWN_ACC);

		getControl().setScrollKey(KeyEvent.VK_UP, ScrollStatus.SCROLLING);

		// 第二套按键
		getControl().setHerizontalKey(KeyEvent.VK_A, HerizontalStatus.LEFT);
		getControl().setHerizontalKey(KeyEvent.VK_D, HerizontalStatus.RIGHT);

		getControl().setVerticalKey(KeyEvent.VK_S, VerticalStatus.DOWN_ACC);

		getControl().setScrollKey(KeyEvent.VK_W, ScrollStatus.SCROLLING);

		// 第三套按键
		getControl().setHerizontalKey(KeyEvent.VK_J, HerizontalStatus.LEFT);
		getControl().setHerizontalKey(KeyEvent.VK_L, HerizontalStatus.RIGHT);

		getControl().setVerticalKey(KeyEvent.VK_K, VerticalStatus.DOWN_ACC);

		getControl().setScrollKey(KeyEvent.VK_I, ScrollStatus.SCROLLING);

		// 第四套按键
		getControl().setHerizontalKey(KeyEvent.VK_NUMPAD4,
				HerizontalStatus.LEFT);
		getControl().setHerizontalKey(KeyEvent.VK_NUMPAD6,
				HerizontalStatus.RIGHT);

		getControl().setVerticalKey(KeyEvent.VK_NUMPAD5,
				VerticalStatus.DOWN_ACC);

		getControl().setVerticalKey(KeyEvent.VK_NUMPAD2,
				VerticalStatus.DOWN_ACC);

		getControl().setScrollKey(KeyEvent.VK_NUMPAD8, ScrollStatus.SCROLLING);

	}

	private Control getControl() {
		return gameData.getControl();
	}

	/**
	 * 游戏的运行需要每个时钟周期的不断刷新。刷新的时候调用这个方法， 游戏就会不断改变自身的状态并运行起来。
	 * 
	 * @param pressedKeys
	 *            刷新的时候，应该传入这一时钟周期中，玩家按下的键， 注意是一个时钟周期中按下的键，而不是指刷新的时刻按下的键
	 * @param releasedKeys
	 *            刷新的时候，传入这一时钟周期玩家松开的键
	 * @param interpolation
	 *            这次刷新与上次刷新的时间间隔，单位是毫秒。这个值 控制着游戏的一次刷新时的步长
	 */
	public void update(KeyList keyList, long interpolation) {
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should not be negtive");
		}

		synLock.lock();
		try {
			// 处理控制键，处理控制键一定要放在刷新状态之前
			handleKeys(keyList);

			// 处理"初始锁定"状态
			if (initLock) {
				if (gameData.getGameState().getCurrentState()
						.equals(State.BAR_DROPPING)) {
					hint = "Ready? Press DOWN key to continue.";
					return;
				}
			}

			// 处理暂停状态
			if (pause) {
				hint = "Paused";
			}

			// 用StateHandler处理
			if (!pause) {
				boolean hasHandler = stateBox.handleState(gameData
						.getGameState().getCurrentState(), gameData,
						interpolation);
				assert (hasHandler);
			}

			// 刷新游戏效果
			gameData.getEffectContainer().update(interpolation);

			// 刷新记分器
			gameData.getScoreRecorder().update(interpolation);

			// 游戏结束的提示
			if (isGameOver()) {
				hint = "GAME OVER";
			}

		} catch (Throwable ex) {
			// 为了防止超级"健壮"的swing私吞异常，我这里还是自己把异常打印出来的好
			ex.printStackTrace();
			assert (false);
			throw new RuntimeException(ex);
		} finally {
			synLock.unlock();
		}

	}

	private void handleKeys(KeyList keyList) {
		if (null == keyList) {
			return;
		}

		synchronized (keyList) {
			for (KeyList.KeyEntry entry : keyList) {
				assert (entry != null);

				int keyCode = entry.getKeyCode();
				boolean pressed = entry.isPressed();

				// 处理initLock
				if (initLock) {
					if (!pressed) {
						VerticalStatus vs = getControl()
								.getVerticalKey(keyCode);
						if (vs != null && vs.equals(VerticalStatus.DOWN_ACC)) {
							initLock = false;
						}
					}
				}

				// 处理暂停
				if (!pressed && keyCode == PAUSE_KEY) {
					pause = !pause;
				}

				// 处理游戏控制键
				if (pressed) {
					getControl().keyPressed(keyCode);
				} else {
					getControl().keyReleased(keyCode);
				}

			}
		}

	}

	/**
	 * 将游戏全部会画出来
	 * 
	 * @param g
	 * @param orign
	 *            游戏绘图时的原点
	 * @param observer
	 *            当图像更新的时候，应该通知谁
	 */
	public void paintAll(Graphics g, Point orign, ImageObserver observer,
			double scale) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}
		if (null == orign) {
			throw new NullPointerException("orign should not be null");
		}
		if (null == observer) {
			throw new NullPointerException("observer should not be null");
		}
		if (scale <= 0) {
			throw new IllegalArgumentException("scale should be positive");
		}

		synLock.lock();
		try {
			painter.paintAll(g, orign, observer, gameData, scale, hint);

			// 清除hint
			hint = null;

		} catch (Throwable ex) {
			// 为了防止超级"健壮"的swing私吞异常，我这里还是自己把异常打印出来的好
			ex.printStackTrace();
			assert (false);
			throw new RuntimeException(ex);
		} finally {
			synLock.unlock();
		}

	}

	/**
	 * 调用这个方法将强制游戏结束。 TODO 这个方法的实现以来了StateHandler的内部细节。如果以后更改了结构， 很可能是这个方法不能正常工作
	 */
	public void enforceGameOver() {
		synLock.lock();
		try {
			if (gameData.getGameState().getCurrentState() != State.GAME_OVER_REMOVING
					&& gameData.getGameState().getCurrentState() != State.GAME_OVER) {
				initLock = false;
				hint = null;

				gameData.getGameState().setCurrentState(
						State.GAME_OVER_REMOVING);
			}
		} finally {
			synLock.unlock();
		}
	}

	public int getGameFieldWidth() {
		return gameData.getGameFieldWidth();
	}

	public int getGameFieldHeight() {
		return gameData.getGameFieldHeight();
	}

	public boolean isGameOver() {
		return gameData.isGameOver();
	}

	/**
	 * @see initLock
	 */
	public boolean isInitLock() {
		return initLock;
	}

}
