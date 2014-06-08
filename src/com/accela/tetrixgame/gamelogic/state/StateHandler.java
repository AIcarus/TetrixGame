package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;

/**
 * 
 * 游戏状态处理器。一个状态处理器专门针对一种状态进行处理。 整个游戏的处理由处理器不断处理和推进，组成一个有限状态机。
 * 
 */
public abstract class StateHandler
{
	private State aimState;

	public StateHandler(State aimState)
	{
		if (null == aimState)
		{
			throw new NullPointerException("state should not be null");
		}

		this.aimState = aimState;
	}

	public void handleState(GameData gameData, long interpolation)
	{
		if (null == gameData)
		{
			throw new NullPointerException("gameData should not be null");
		}
		if (gameData.getGameState().getCurrentState() != aimState)
		{
			throw new IllegalArgumentException(
					"this handler does handle the current state");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		assert (gameData.getControl().getHerizontalStatus() != null);
		assert (gameData.getControl().getVerticalStatus() != null);
		assert (gameData.getControl().getScrollStatus() != null);

		State nextState = handleStateImpl(gameData.getControl().getHerizontalStatus(),
				gameData.getControl().getVerticalStatus(),
				gameData.getControl().getScrollStatus(),
				gameData,
				interpolation);

		if (null == nextState)
		{
			assert (false);
			throw new NullPointerException(
					"handleStateImpl should not return null");
		}

		gameData.getGameState().setCurrentState(nextState);
	}

	protected abstract State handleStateImpl(HerizontalStatus hStatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation);

	public State getAimState()
	{
		return aimState;
	}

}
