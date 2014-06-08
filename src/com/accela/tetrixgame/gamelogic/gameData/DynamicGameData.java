package com.accela.tetrixgame.gamelogic.gameData;

import java.util.Random;

import com.accela.tetrixgame.gamelogic.container.BlockContainer;
import com.accela.tetrixgame.gamelogic.control.Control;
import com.accela.tetrixgame.gamelogic.effects.EffectContainer;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.score.ScoreRecorder;
import com.accela.tetrixgame.gamelogic.state.GameState;
import com.accela.tetrixgame.gamelogic.state.State;

/**
 * 
 * 动态数据是指游戏过程中不断被改变的数据。
 * 
 */
public class DynamicGameData {
	/**
	 * 游戏的随机数，用来生成随机方块
	 */
	private Random random;
	/**
	 * 游戏中所有方块的容器
	 */
	private BlockContainer blockContainer;
	/**
	 * 游戏中所有效果的容器
	 */
	private EffectContainer effectContainer;
	/**
	 * 游戏状态的记录和管理
	 */
	private GameState gameState;
	/**
	 * 游戏的控制键状态的记录和管理
	 */
	private Control control;
	/**
	 * 记录游戏分数
	 */
	private ScoreRecorder scoreRecorder;

	public DynamicGameData(long randomSeed, int rowNum, int columnNum,
			int blockWidth, int blockHeight, long baseScore) {
		if (randomSeed <= 0) {
			throw new IllegalArgumentException("randomSeed should be positive");
		}
		if (rowNum <= 0) {
			throw new IllegalArgumentException("rowNum should be positive");
		}
		if (columnNum <= 0) {
			throw new IllegalArgumentException("columnNum should be positive");
		}
		if (blockWidth <= 0) {
			throw new IllegalArgumentException("blockWidth should be positive");
		}
		if (blockHeight <= 0) {
			throw new IllegalArgumentException("blockHeight should be positive");
		}
		if (baseScore <= 0) {
			throw new IllegalArgumentException("baseScore should be positive");
		}

		random = new Random(randomSeed);
		blockContainer = new BlockContainer(rowNum, columnNum, new Grid(
				blockWidth, blockHeight));
		effectContainer = new EffectContainer();
		gameState = new GameState(State.GENERATING);
		control = new Control();

		scoreRecorder = new ScoreRecorder(baseScore);
	}

	public Random getRandom() {
		return random;
	}

	public BlockContainer getBlockContainer() {
		return blockContainer;
	}

	public GameState getGameState() {
		return gameState;
	}

	public Control getControl() {
		return control;
	}

	public boolean isGameOver() {
		return gameState.isGameOver();
	}

	public ScoreRecorder getScoreRecorder() {
		return scoreRecorder;
	}

	public EffectContainer getEffectContainer() {
		return effectContainer;
	}
}
