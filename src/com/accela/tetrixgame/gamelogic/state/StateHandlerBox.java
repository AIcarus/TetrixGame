package com.accela.tetrixgame.gamelogic.state;

import java.util.*;

import com.accela.tetrixgame.gamelogic.gameData.GameData;

/**
 * 
 * 游戏有多种状态以多个状态处理器，这个类负责装载状态处理器， 以及当传递过来一个状态的时候，查找匹配的状态处理器处理之。
 * 
 */
public class StateHandlerBox
{
	private Map<State, StateHandler> handlers = new HashMap<State, StateHandler>();

	public StateHandler addHandler(StateHandler handler)
	{
		if (null == handler)
		{
			throw new NullPointerException("handler should not be null");
		}

		return handlers.put(handler.getAimState(), handler);
	}

	public StateHandler removeHandler(State state)
	{
		if (null == state)
		{
			throw new NullPointerException("state should not be null");
		}

		return handlers.remove(state);
	}

	public boolean handleState(State state, GameData gameData,
			long interpolation)
	{
		if (null == state)
		{
			throw new NullPointerException("state should not be null");
		}
		if (null == gameData)
		{
			throw new NullPointerException("gameData should not be null");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		StateHandler handler = handlers.get(state);
		if (null == handler)
		{
			return false;
		} else
		{
			handler.handleState(gameData, interpolation);
			return true;
		}
	}

}
