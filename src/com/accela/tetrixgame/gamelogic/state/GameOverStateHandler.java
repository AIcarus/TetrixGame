package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;

public class GameOverStateHandler extends StateHandler
{
	public GameOverStateHandler()
	{
		super(State.GAME_OVER);
	}

	@Override
	protected State handleStateImpl(HerizontalStatus hStatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation)
	{
		assert (gameData.getBlockContainer().getBar() == null);
		assert (gameData.getBlockContainer().getMatrix().isEmpty());
		assert (gameData.getEffectContainer().isEmpty());

		// TODO GameOverState定义为游戏结束并且所有效果均已经放完的状态，注意别搞错

		return State.GAME_OVER;
	}

}
