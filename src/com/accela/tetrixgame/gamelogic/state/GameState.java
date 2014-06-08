package com.accela.tetrixgame.gamelogic.state;

/**
 * 
 * 存储、记录和管理当前的游戏状态
 * 
 */
public class GameState
{
	private State currentState;

	public GameState(State initState)
	{
		if (null == initState)
		{
			throw new NullPointerException("initState should not be null");
		}

		this.currentState = initState;
	}

	public State getCurrentState()
	{
		return currentState;
	}

	public void setCurrentState(State currentState)
	{
		if (null == currentState)
		{
			throw new NullPointerException("currentState should not be null");
		}

		this.currentState = currentState;
	}

	public boolean isGameOver()
	{
		return currentState.equals(State.GAME_OVER);
	}
}
