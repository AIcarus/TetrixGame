package com.accela.tetrixgame.gamelogic.gameData;

import java.util.Random;

import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.basic.MovingAttributes;
import com.accela.tetrixgame.gamelogic.container.BlockContainer;
import com.accela.tetrixgame.gamelogic.control.Control;
import com.accela.tetrixgame.gamelogic.effects.EffectContainer;
import com.accela.tetrixgame.gamelogic.effects.fireworkUtility.FireworkAttributes;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.SparkAttributes;
import com.accela.tetrixgame.gamelogic.score.ScoreRecorder;
import com.accela.tetrixgame.gamelogic.state.GameState;

/**
 * 
 * 游戏中的诸多数据存储于此。 游戏数据分为两大类：静态数据和动态数据。 静态数据在开始一场游戏以后就不再改变，使用相同的静态数据重新开始一场游戏，
 * 能够得到完全相同的场景设定。这是利用控制键记录来实现游戏重放的基础。 动态数据是指游戏过程中不断被改变的数据。
 * 
 */
public class GameData {
	private StaticGameData staticData;

	private DynamicGameData dynamicData;

	public GameData(long randomSeed, long hMaxAccTime, double hMaxVel,
			long vMaxAccTime, double vNormalVel, double vMaxVel, int rowNum,
			int columnNum, int barRowNum, int barColumnNum, int blockTypeNum,
			int blockWidth, int blockHeight, double gravity,
			double scrollMaxVel, long removeInterval, double explosionVel,
			double explosionAddedVel, int outerSparkRadius,
			double outerSparkAcc, int innerSparkRadius, double innerSparkAcc,
			double outerSparkVel, double innerSparkVel, int sparkNumPerBlock,
			double sparkVel, double sparkGravity, long sparkDelayInterval,
			double fireLineVel, double fireLineGravity, int fireLineLength,
			int fireLineLayerNum, int fireLineNumPerLayer, int shakeMaxOffset,
			long shakePeriod, int shakeNumPeriod, long gameOverRemoveInterval,
			int gridWidth, boolean enableEffects, boolean enableSound,
			long baseScore) {
		staticData = new StaticGameData(randomSeed, hMaxAccTime, hMaxVel,
				vMaxAccTime, vNormalVel, vMaxVel, rowNum, columnNum, barRowNum,
				barColumnNum, blockTypeNum, blockWidth, blockHeight, gravity,
				scrollMaxVel, removeInterval, explosionVel, explosionAddedVel,
				outerSparkRadius, outerSparkAcc, innerSparkRadius,
				innerSparkAcc, outerSparkVel, innerSparkVel, sparkNumPerBlock,
				sparkVel, sparkGravity, sparkDelayInterval, fireLineVel,
				fireLineGravity, fireLineLength, fireLineLayerNum,
				fireLineNumPerLayer, shakeMaxOffset, shakePeriod,
				shakeNumPeriod, gameOverRemoveInterval, gridWidth,
				enableEffects, enableSound, baseScore);

		dynamicData = new DynamicGameData(randomSeed, rowNum, columnNum,
				blockWidth, blockHeight, baseScore);

	}

	public int getGameFieldHeight() {
		return staticData.getGameFieldHeight();
	}

	public int getGameFieldWidth() {
		return staticData.getGameFieldWidth();
	}

	public int getGridWidth() {
		return staticData.getGridWidth();
	}

	public BlockContainer getBlockContainer() {
		return dynamicData.getBlockContainer();
	}

	public Control getControl() {
		return dynamicData.getControl();
	}

	public GameState getGameState() {
		return dynamicData.getGameState();
	}

	public Random getRandom() {
		return dynamicData.getRandom();
	}

	public long getHMaxAccTime() {
		return staticData.getHMaxAccTime();
	}

	public double getHMaxVel() {
		return staticData.getHMaxVel();
	}

	public long getRandomSeed() {
		return staticData.getRandomSeed();
	}

	public ScoreRecorder getScoreRecorder() {
		return dynamicData.getScoreRecorder();
	}

	public double getVMaxVel() {
		return staticData.getVMaxVel();
	}

	public long getVMaxAccTime() {
		return staticData.getVMaxAccTime();
	}

	public double getVNormalVel() {
		return staticData.getVNormalVel();
	}

	public int getColumnNum() {
		return staticData.getColumnNum();
	}

	public int getRowNum() {
		return staticData.getRowNum();
	}

	public int getBarColumnNum() {
		return staticData.getBarColumnNum();
	}

	public int getBarRowNum() {
		return staticData.getBarRowNum();
	}

	public int getBlockTypeNum() {
		return staticData.getBlockTypeNum();
	}

	public int getBlockHeight() {
		return staticData.getBlockHeight();
	}

	public int getBlockWidth() {
		return staticData.getBlockWidth();
	}

	public MovingAttributes getMovingAttributes() {
		return staticData.getMovingAttributes();
	}

	public double getScrollMaxVel() {
		return staticData.getScrollMaxVel();
	}

	public long getRemoveInterval() {
		return staticData.getRemoveInterval();
	}

	public double getExplosionAddedVel() {
		return staticData.getExplosionAddedVel();
	}

	public double getExplosionVel() {
		return staticData.getExplosionVel();
	}

	public boolean equals(Object obj) {
		return dynamicData.equals(obj);
	}

	public int hashCode() {
		return dynamicData.hashCode();
	}

	public String toString() {
		return dynamicData.toString();
	}

	public double getInnerSparkAcc() {
		return staticData.getInnerSparkAcc();
	}

	public int getInnerSparkRadius() {
		return staticData.getInnerSparkRadius();
	}

	public double getInnerSparkVel() {
		return staticData.getInnerSparkVel();
	}

	public double getOuterSparkAcc() {
		return staticData.getOuterSparkAcc();
	}

	public int getOuterSparkRadius() {
		return staticData.getOuterSparkRadius();
	}

	public double getOuterSparkVel() {
		return staticData.getOuterSparkVel();
	}

	public long getSparkDelayInterval() {
		return staticData.getSparkDelayInterval();
	}

	public double getSparkGravity() {
		return staticData.getSparkGravity();
	}

	public int getSparkNumPerBlock() {
		return staticData.getSparkNumPerBlock();
	}

	public double getSparkVel() {
		return staticData.getSparkVel();
	}

	public SparkAttributes getSparkAttributes() {
		return staticData.getSparkAttributes();
	}

	public EffectContainer getEffectContainer() {
		return dynamicData.getEffectContainer();
	}

	public double getFireLineGravity() {
		return staticData.getFireLineGravity();
	}

	public int getFireLineLayerNum() {
		return staticData.getFireLineLayerNum();
	}

	public int getFireLineLength() {
		return staticData.getFireLineLength();
	}

	public int getFireLineNumPerLayer() {
		return staticData.getFireLineNumPerLayer();
	}

	public double getFireLineVel() {
		return staticData.getFireLineVel();
	}

	public FireworkAttributes getFireworkAttributes() {
		return staticData.getFireworkAttributes();
	}

	public int getShakeMaxOffset() {
		return staticData.getShakeMaxOffset();
	}

	public int getShakeNumPeriod() {
		return staticData.getShakeNumPeriod();
	}

	public long getShakePeriod() {
		return staticData.getShakePeriod();
	}

	public long getGameOverRemoveInterval() {
		return staticData.getGameOverRemoveInterval();
	}

	public boolean isGameOver() {
		return dynamicData.isGameOver();
	}

	public boolean isEnableEffects() {
		return staticData.isEnableEffects();
	}

	public GameAttributes getTetrixGameAttributes() {
		return staticData.getTetrixGameAttributes();
	}

	public boolean isEnableSound() {
		return staticData.isEnableSound();
	}

	public long getBaseScore() {
		return staticData.getBaseScore();
	}

}
