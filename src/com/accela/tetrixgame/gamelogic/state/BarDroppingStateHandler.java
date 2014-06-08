package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.basic.Bar;
import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;

/**
 * 
 * 处理Bar的下落阶段。负责Bar的下落、玩家控制的响应以及碰撞检测和结束下落阶段
 * 
 */
public class BarDroppingStateHandler extends StateHandler
{
	public BarDroppingStateHandler()
	{
		super(State.BAR_DROPPING);
	}

	@Override
	protected State handleStateImpl(HerizontalStatus hStatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation)
	{
		Bar bar = gameData.getBlockContainer().getBar();
		assert (bar != null);

		boolean collision = bar.update(hStatus,
				vStatus,
				sStatus,
				gameData.getBlockContainer().getMatrix(),
				gameData.getBlockContainer().getGrid(),
				gameData.getBlockWidth() * gameData.getColumnNum(),
				gameData.getBlockHeight() * gameData.getRowNum(),
				interpolation);

		if (!collision)
		{
			return State.BAR_DROPPING;
		} else
		{
			bar.actOnHitGround(gameData.getBlockContainer().getGrid());

			gameData.getBlockContainer().addBarToMatrix();

			assert (gameData.getBlockContainer().getMatrix().isAllAtRightIndex());

			// 添加音效
			if (gameData.isEnableSound())
			{
				SoundManager.playSound(SoundManager.BOX_HIT);
			}

			return State.COLLIDING;
		}

	}
}
