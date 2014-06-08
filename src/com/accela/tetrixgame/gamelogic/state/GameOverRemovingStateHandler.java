package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.container.BlockMatrix;
import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;

/**
 * 
 * 游戏结束的时候转到这个状态，负责将游戏区域中的所有方块 都引爆并移除
 * 
 */
public class GameOverRemovingStateHandler extends StateHandler
{
	private long curRemoveIntervalCounter = 0;

	private boolean firstIn = true;

	public GameOverRemovingStateHandler()
	{
		super(State.GAME_OVER_REMOVING);
	}

	@Override
	protected State handleStateImpl(HerizontalStatus hStatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation)
	{
		if (curRemoveIntervalCounter < gameData.getGameOverRemoveInterval())
		{
			curRemoveIntervalCounter += interpolation;
			return State.GAME_OVER_REMOVING;
		} else
		{
			curRemoveIntervalCounter = 0;
		}

		// 将bar加入到BlockContainer的BlockMatrix中
		if (gameData.getBlockContainer().getBar() != null)
		{
			gameData.getBlockContainer().addBarToMatrix();
		}

		BlockMatrix matrix = gameData.getBlockContainer().getMatrix();
		assert (matrix != null);

		// 逐个清除被消去的方块
		for (int i = matrix.getRowNum() - 1; i >= 0; i--)
		{
			for (int j = 0; j < matrix.getColumnNum(); j++)
			{
				if (matrix.get(i, j) != null)
				{
					performBlockRemoval(i, j, gameData);

					return State.GAME_OVER_REMOVING;
				}
			}
		}

		// 播放声音
		if (firstIn && gameData.isEnableSound())
		{
			SoundManager.playSound(SoundManager.GAME_OVER);
		}

		// 状态转移
		if (gameData.getEffectContainer().isEmpty())
		{
			firstIn = true;
			return State.GAME_OVER;
		} else
		{
			if (firstIn)
			{
				firstIn = false;
			}
			return State.GAME_OVER_REMOVING;
		}
	}

	private void performBlockRemoval(final int i, final int j, GameData gameData)
	{
		BlockMatrix matrix = gameData.getBlockContainer().getMatrix();

		Rectangle removedRect = matrix.get(i, j);
		assert (removedRect != null);

		// 删除应该被删除的方块
		boolean result = matrix.remove(i, j);
		assert (result);

		// 添加Spark
		for (int idx = 0; idx < gameData.getSparkNumPerBlock(); idx++)
		{
			Point center = null;
			if (gameData.isEnableEffects())
			{
				center = Point.create(removedRect.x
						+ (int) ((removedRect.getWidth() + 1) * Math.random()),
						removedRect.y
								+ (int) ((removedRect.getHeight() + 1) * Math.random()));
			} else
			{

				center = Point.create(removedRect.x + removedRect.getWidth()
						/ 2, removedRect.y + removedRect.getHeight() / 2);
			}

			gameData.getEffectContainer().createSpark(center,
					idx * gameData.getSparkDelayInterval(),
					gameData.getSparkAttributes());

			Point.dispose(center);
		}

		// 添加RunningCircle
		Point center = Point.create(removedRect.x + removedRect.getWidth() / 2,
				removedRect.y + removedRect.getHeight() / 2);
		gameData.getEffectContainer().createRunningCircle(center);

		Point.dispose(center);

		// 添加振动shaker
		gameData.getEffectContainer().setShaker(gameData.getShakeMaxOffset(),
				gameData.getShakePeriod(),
				gameData.getShakeNumPeriod());
		
	}

}
