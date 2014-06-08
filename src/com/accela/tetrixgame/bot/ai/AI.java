package com.accela.tetrixgame.bot.ai;

import java.lang.reflect.Field;

import com.accela.tetrixgame.gamelogic.GameLogic;
import com.accela.tetrixgame.gamelogic.container.BlockContainer;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.state.GameState;
import com.accela.tetrixgame.gamelogic.state.State;
import com.accela.tetrixgame.util.IntervalTimer;
import com.accela.tetrixgame.util.KeyList;

/**
 * 
 * BOT的人工智能封装在这个类中。
 * 
 */
public abstract class AI {
	private Data tetrixData = new Data();

	private MovingStrategy lastStrategy = new MovingStrategy();

	private MovingStrategy curStrategy = new MovingStrategy();

	private static final KeyGenerator keyGenerator = new KeyGenerator();

	private IntervalTimer intervalTimer = new IntervalTimer();

	private boolean isInBarDroppingState = false;

	private GameLogic lastGame = null;

	private BlockContainer lastBC = null;

	private GameState lastGameState = null;

	/**
	 * 每次刷新游戏的时候调用。 传入game，人工智能经过运算后，得出应该按下和松开什么键，用过result返回
	 */
	public void update(KeyList result, GameLogic game) {
		if (null == result) {
			throw new NullPointerException("keyList should not be null");
		}
		if (null == game) {
			throw new NullPointerException("game should not be null");
		}

		// 将result清空
		result.clear();

		// 计算interpolation
		intervalTimer.update();
		long interpolation = intervalTimer.getInterval();
		if (-1 == interpolation) {
			return;
		}

		// 检查gamelogic当前的状态是否是initLock，如果是，则自动按一次下键来解除
		if (game.isInitLock()) {
			result.keyPressed(KeyGenerator.DOWN);
			result.keyReleased(KeyGenerator.DOWN);

			return;
		}

		// 得到gameState
		GameState curGameState = null;
		if (null == lastGame || lastGame != game) {
			try {
				lastGameState = extractGameState(game);
			} catch (SecurityException ex) {
				ex.printStackTrace();
				assert (false);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
				assert (false);
			} catch (NoSuchFieldException ex) {
				ex.printStackTrace();
				assert (false);
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
				assert (false);
			}
			assert (lastGameState != null);

		} else {
			assert (lastGameState != null);
		}
		curGameState = lastGameState;

		// 检测当前gamelogic所处的状态是否是BarDropping，只有在BarDropping状态，AI才能工作
		boolean curIsInBarDroppingState = curGameState.getCurrentState() == State.BAR_DROPPING;
		if (!isInBarDroppingState) {
			if (curIsInBarDroppingState) {
				// do nothing
			} else {
				// do nothing
			}
		} else {
			if (curIsInBarDroppingState) {
				// do nothing
			} else {
				tetrixData.reset();
			}
		}
		isInBarDroppingState = curIsInBarDroppingState;

		// 清零strategy
		curStrategy.clear();
		curStrategy.sweepNull();

		// 当处于BarDroppingState中的时候，应该产生控制键
		if (isInBarDroppingState) {
			// 得到BlockContainer
			BlockContainer curBC = null;
			if (null == lastGame || lastGame != game) {
				try {
					lastBC = extractBlockContainer(game);
				} catch (SecurityException ex) {
					ex.printStackTrace();
					assert (false);
				} catch (NoSuchFieldException ex) {
					ex.printStackTrace();
					assert (false);
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
					assert (false);
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
					assert (false);
				}
				assert (lastBC != null);

			} else {
				assert (lastBC != null);
			}
			curBC = lastBC;

			// 刷新TetrixData
			tetrixData.update(curBC, interpolation);

			// 调用AI方法
			decideStrategy(curStrategy, tetrixData);

			// 消除strategy中的null属性
			curStrategy.sweepNull();
		}

		// 得到按键信息strategy
		assert (result.isEmpty());
		keyGenerator.generateKeys(result, lastStrategy, curStrategy);

		// 更新lastStrategy和curStrategy
		MovingStrategy tempStrategy = lastStrategy;
		lastStrategy = curStrategy;
		curStrategy = tempStrategy;
		assert (curStrategy != lastStrategy);

		// 返回
		return;
	}

	private BlockContainer extractBlockContainer(GameLogic game)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		assert (game != null);

		Field field = game.getClass().getDeclaredField("gameData");
		field.setAccessible(true);

		GameData gameData = (GameData) field.get(game);
		BlockContainer bc = gameData.getBlockContainer();
		assert (bc != null);

		return bc;
	}

	private GameState extractGameState(GameLogic game)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		assert (game != null);

		Field field = game.getClass().getDeclaredField("gameData");
		field.setAccessible(true);

		GameData gameData = (GameData) field.get(game);
		GameState s = gameData.getGameState();
		assert (s != null);

		return s;
	}

	/**
	 * 子类覆盖，根据TetrixData中的内容，得出此时的移动策略strategy。
	 * 返回值实际上是通过传入的参数strategy来实现的，即把strategy改成 你需要返回的内容。
	 * 
	 * @param strategy
	 * @param data
	 */
	protected abstract void decideStrategy(MovingStrategy strategy, Data data);

}
