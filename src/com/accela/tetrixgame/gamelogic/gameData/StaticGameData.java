package com.accela.tetrixgame.gamelogic.gameData;

import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.basic.MovingAttributes;
import com.accela.tetrixgame.gamelogic.effects.fireworkUtility.FireworkAttributes;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.SparkAttributes;

/**
 * 
 * 静态数据在开始一场游戏以后就不再改变，使用相同的静态数据重新开始一场游戏， 能够得到完全相同的场景设定。这是利用控制键记录来实现游戏重放的基础。
 * 
 */
public class StaticGameData { // TODO
								// 如果新加入了游戏参数，则这个类的构造函数、gameData的构造函数、TetrixGameAttributs，以及MovingAttributs都要考虑是否需要更改

	// 关于各种游戏数据的含义，查阅TetrixGameAttributes中的对应项的注释
	private long randomSeed;
	private long hMaxAccTime;
	private double hMaxVel;
	private long vMaxAccTime;
	private double vNormalVel;
	private double vMaxVel;
	private int rowNum;
	private int columnNum;
	private int barRowNum;
	private int barColumnNum;
	private int blockTypeNum;
	private int blockWidth;
	private int blockHeight;
	private double gravity;
	private double scrollMaxVel;
	private long removeInterval;
	private double explosionVel;
	private double explosionAddedVel;

	private int outerSparkRadius;
	private double outerSparkAcc;
	private int innerSparkRadius;
	private double innerSparkAcc;
	private double outerSparkVel;
	private double innerSparkVel;
	private int sparkNumPerBlock;
	private double sparkVel;
	private double sparkGravity;
	private long sparkDelayInterval;

	private double fireLineVel;
	private double fireLineGravity;
	private int fireLineLength;
	private int fireLineLayerNum;
	private int fireLineNumPerLayer;

	private int shakeMaxOffset;
	private long shakePeriod;
	private int shakeNumPeriod;

	private long gameOverRemoveInterval;

	private int gridWidth;
	private int gameFieldWidth;
	private int gameFieldHeight;

	private boolean enableEffects;

	private boolean enableSound;

	private long baseScore;

	public StaticGameData(long randomSeed, long hMaxAccTime, double hMaxVel,
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
		if (randomSeed <= 0) {
			throw new IllegalArgumentException("randomSeed should be positive");
		}
		if (hMaxAccTime <= 0) {
			throw new IllegalArgumentException("hMaxAccTime should be positive");
		}
		if (hMaxVel <= 0) {
			throw new IllegalArgumentException("hMaxVel should be positive");
		}
		if (vMaxAccTime <= 0) {
			throw new IllegalArgumentException("vMaxAccTime should be positive");
		}
		if (vNormalVel <= 0) {
			throw new IllegalArgumentException("vNormalVel should be positive");
		}
		if (vMaxVel <= 0) {
			throw new IllegalArgumentException("vMaxVel should be positive");
		}
		if (rowNum <= 0) {
			throw new IllegalArgumentException("rowNum should be positive");
		}
		if (columnNum <= 0) {
			throw new IllegalArgumentException("columnNum should be positive");
		}
		if (barRowNum <= 0) {
			throw new IllegalArgumentException("barRowNum should be positive");
		}
		if (barColumnNum <= 0) {
			throw new IllegalArgumentException(
					"barColumnNum should be positive");
		}
		if (blockTypeNum <= 0) {
			throw new IllegalArgumentException(
					"blockTypeNum should be positive");
		}
		if (blockWidth <= 0) {
			throw new IllegalArgumentException("blockWidth should be positive");
		}
		if (blockHeight <= 0) {
			throw new IllegalArgumentException("blockHeight should be positive");
		}
		if (gravity < 0) {
			throw new IllegalArgumentException("gravity should be nonnegtive");
		}
		if (scrollMaxVel <= 0) {
			throw new IllegalArgumentException(
					"scrollMaxVel should be positive");
		}
		if (removeInterval < 0) {
			throw new IllegalArgumentException(
					"removeInterval should be nonnegtive");
		}
		if (explosionVel < 0) {
			throw new IllegalArgumentException(
					"explosionVel should be nonnegtive");
		}
		if (explosionAddedVel < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (outerSparkRadius <= 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be positive");
		}
		if (outerSparkAcc < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (innerSparkRadius <= 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be positive");
		}
		if (innerSparkAcc < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (outerSparkVel < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (innerSparkVel < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (sparkNumPerBlock < 1) {
			throw new IllegalArgumentException(
					"explosionAddedVel should not be less than 1");
		}
		if (sparkVel < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (sparkGravity < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (sparkDelayInterval < 0) {
			throw new IllegalArgumentException(
					"explosionAddedVel should be nonnegtive");
		}
		if (fireLineVel < 0) {
			throw new IllegalArgumentException(
					"fireLineVel should be nonnegtive");
		}
		if (fireLineGravity < 0) {
			throw new IllegalArgumentException(
					"fireLineGravity should be nonnegtive");
		}
		if (fireLineLength <= 0) {
			throw new IllegalArgumentException(
					"fireLineLength should be positive");
		}
		if (fireLineLayerNum <= 0) {
			throw new IllegalArgumentException(
					"fireLineLayerNum should be positive");
		}
		if (fireLineNumPerLayer <= 0) {
			throw new IllegalArgumentException(
					"fireLineNumPerLayer should be positive");
		}
		if (shakeMaxOffset < 0) {
			throw new IllegalArgumentException("maxOffset should be nonnegtive");
		}
		if (shakePeriod <= 0) {
			throw new IllegalArgumentException("maxOffset should be positive");
		}
		if (shakeNumPeriod < 0) {
			throw new IllegalArgumentException("maxOffset should be nonnegtive");
		}
		if (gameOverRemoveInterval <= 0) {
			throw new IllegalArgumentException(
					"gameOverRemoveInterval should be positive");
		}
		if (gridWidth <= 0) {
			throw new IllegalArgumentException("gridWidth should be positive");
		}
		if (baseScore <= 0) {
			throw new IllegalArgumentException("baseScore should be positive");
		}

		this.randomSeed = randomSeed;
		this.hMaxAccTime = hMaxAccTime;
		this.hMaxVel = hMaxVel;
		this.vMaxAccTime = vMaxAccTime;
		this.vNormalVel = vNormalVel;
		this.vMaxVel = vMaxVel;
		this.rowNum = rowNum;
		this.columnNum = columnNum;
		this.barRowNum = barRowNum;
		this.barColumnNum = barColumnNum;
		this.blockTypeNum = blockTypeNum;
		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
		this.gravity = gravity;
		this.scrollMaxVel = scrollMaxVel;
		this.removeInterval = removeInterval;
		this.explosionVel = explosionVel;
		this.explosionAddedVel = explosionAddedVel;

		this.outerSparkRadius = outerSparkRadius;
		this.outerSparkAcc = outerSparkAcc;
		this.innerSparkRadius = innerSparkRadius;
		this.innerSparkAcc = innerSparkAcc;
		this.outerSparkVel = outerSparkVel;
		this.innerSparkVel = innerSparkVel;
		this.sparkNumPerBlock = sparkNumPerBlock;
		this.sparkVel = sparkVel;
		this.sparkGravity = sparkGravity;
		this.sparkDelayInterval = sparkDelayInterval;

		this.fireLineVel = fireLineVel;
		this.fireLineGravity = fireLineGravity;
		this.fireLineLength = fireLineLength;
		this.fireLineLayerNum = fireLineLayerNum;
		this.fireLineNumPerLayer = fireLineNumPerLayer;

		this.shakeMaxOffset = shakeMaxOffset;
		this.shakePeriod = shakePeriod;
		this.shakeNumPeriod = shakeNumPeriod;

		this.gameOverRemoveInterval = gameOverRemoveInterval;

		this.gridWidth = gridWidth;
		this.gameFieldWidth = 2 * this.gridWidth + this.columnNum
				* this.blockWidth;
		this.gameFieldHeight = 2 * this.gridWidth + this.rowNum
				* this.blockHeight;

		this.enableEffects = enableEffects;
		// 当enableEffects==false时，强制改变spark的属性
		if (!enableEffects) {
			this.outerSparkVel *= 2;
			this.sparkNumPerBlock = 1;
			this.sparkVel = 0;
			this.sparkGravity = 0;
			this.sparkDelayInterval = 0;
		}

		this.enableSound = enableSound;
		this.baseScore = baseScore;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public int getGameFieldWidth() {
		return gameFieldWidth;
	}

	public int getGameFieldHeight() {
		return gameFieldHeight;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public long getHMaxAccTime() {
		return hMaxAccTime;
	}

	public double getHMaxVel() {
		return hMaxVel;
	}

	public long getVMaxAccTime() {
		return vMaxAccTime;
	}

	public double getVNormalVel() {
		return vNormalVel;
	}

	public double getVMaxVel() {
		return vMaxVel;
	}

	public int getRowNum() {
		return rowNum;
	}

	public int getColumnNum() {
		return columnNum;
	}

	public int getBarRowNum() {
		return barRowNum;
	}

	public int getBarColumnNum() {
		return barColumnNum;
	}

	public int getBlockTypeNum() {
		return blockTypeNum;
	}

	public int getBlockWidth() {
		return blockWidth;
	}

	public int getBlockHeight() {
		return blockHeight;
	}

	private MovingAttributes movingAttr = null;

	public MovingAttributes getMovingAttributes() {
		if (null == movingAttr) {
			movingAttr = new MovingAttributes();

			movingAttr.put(MovingAttributes.GRAVITY, String.valueOf(gravity));
			movingAttr.put(MovingAttributes.V_MAX_VEL, String.valueOf(vMaxVel));

			movingAttr.put(MovingAttributes.V_NORMAL_VEL,
					String.valueOf(vNormalVel));
			movingAttr.put(MovingAttributes.V_MAX_ACC_TIME,
					String.valueOf(vMaxAccTime));

			movingAttr.put(MovingAttributes.H_MAX_VEL, String.valueOf(hMaxVel));
			movingAttr.put(MovingAttributes.H_MAX_ACC_TIME,
					String.valueOf(hMaxAccTime));
		}

		return movingAttr;
	}

	public double getScrollMaxVel() {
		return scrollMaxVel;
	}

	public long getRemoveInterval() {
		return removeInterval;
	}

	public double getExplosionVel() {
		return explosionVel;
	}

	public double getExplosionAddedVel() {
		return explosionAddedVel;
	}

	public int getOuterSparkRadius() {
		return outerSparkRadius;
	}

	public double getOuterSparkAcc() {
		return outerSparkAcc;
	}

	public int getInnerSparkRadius() {
		return innerSparkRadius;
	}

	public double getInnerSparkAcc() {
		return innerSparkAcc;
	}

	public double getOuterSparkVel() {
		return outerSparkVel;
	}

	public double getInnerSparkVel() {
		return innerSparkVel;
	}

	public int getSparkNumPerBlock() {
		return sparkNumPerBlock;
	}

	public double getSparkVel() {
		return sparkVel;
	}

	public double getSparkGravity() {
		return sparkGravity;
	}

	public long getSparkDelayInterval() {
		return sparkDelayInterval;
	}

	private SparkAttributes sparkAttr = null; // TODO

	// 如果允许动态修改staticGameData的内容，这里就不能用这种方式来优化效率了

	public SparkAttributes getSparkAttributes() {
		if (null == sparkAttr) {
			sparkAttr = new SparkAttributes(outerSparkRadius, outerSparkAcc,
					innerSparkRadius, innerSparkAcc, outerSparkVel,
					innerSparkVel, sparkNumPerBlock, sparkVel, sparkGravity,
					sparkDelayInterval);
		}

		return sparkAttr;
	}

	public double getFireLineVel() {
		return fireLineVel;
	}

	public double getFireLineGravity() {
		return fireLineGravity;
	}

	public int getFireLineLength() {
		return fireLineLength;
	}

	public int getFireLineLayerNum() {
		return fireLineLayerNum;
	}

	public int getFireLineNumPerLayer() {
		return fireLineNumPerLayer;
	}

	private FireworkAttributes fireworkAttr = null; // TODO

	// 如果允许动态修改staticGameData的内容，这里就不能用这种方式来优化效率了
	// TODO 另外，不能允许程序在外部更改fireworkAttr等属性表

	public FireworkAttributes getFireworkAttributes() {
		if (null == fireworkAttr) {
			fireworkAttr = new FireworkAttributes(fireLineVel, fireLineGravity,
					fireLineLength, fireLineLayerNum, fireLineNumPerLayer);
		}

		return fireworkAttr;
	}

	public int getShakeMaxOffset() {
		return shakeMaxOffset;
	}

	public long getShakePeriod() {
		return shakePeriod;
	}

	public int getShakeNumPeriod() {
		return shakeNumPeriod;
	}

	public long getGameOverRemoveInterval() {
		return gameOverRemoveInterval;
	}

	public boolean isEnableEffects() {
		return enableEffects;
	}

	private GameAttributes tetrixGameAttr = null;// TODO

	// 如果允许动态修改staticGameData的内容，这里就不能用这种方式来优化效率了

	public GameAttributes getTetrixGameAttributes() {
		if (null == tetrixGameAttr) {
			tetrixGameAttr = new GameAttributes();

			tetrixGameAttr.put(GameAttributes.RANDOM_SEED,
					String.valueOf(randomSeed));
			tetrixGameAttr.put(GameAttributes.H_MAX_ACC_TIME,
					String.valueOf(hMaxAccTime));
			tetrixGameAttr.put(GameAttributes.H_MAX_VEL,
					String.valueOf(hMaxVel));

			tetrixGameAttr.put(GameAttributes.V_MAX_ACC_TIME,
					String.valueOf(vMaxAccTime));
			tetrixGameAttr.put(GameAttributes.V_MAX_VEL,
					String.valueOf(vMaxVel));
			tetrixGameAttr.put(GameAttributes.V_NORMAL_VEL,
					String.valueOf(vNormalVel));

			tetrixGameAttr.put(GameAttributes.GRAVITY,
					String.valueOf(gravity));
			tetrixGameAttr.put(GameAttributes.ROW_NUM,
					String.valueOf(rowNum));
			tetrixGameAttr.put(GameAttributes.COLUMN_NUM,
					String.valueOf(columnNum));

			tetrixGameAttr.put(GameAttributes.BAR_ROW_NUM,
					String.valueOf(barRowNum));
			tetrixGameAttr.put(GameAttributes.BAR_COLUMN_NUM,
					String.valueOf(barColumnNum));
			tetrixGameAttr.put(GameAttributes.BLOCK_TYPE_NUM,
					String.valueOf(blockTypeNum));

			tetrixGameAttr.put(GameAttributes.BLOCK_WIDTH,
					String.valueOf(blockWidth));
			tetrixGameAttr.put(GameAttributes.BLOCK_HEIGHT,
					String.valueOf(blockHeight));
			tetrixGameAttr.put(GameAttributes.SCROLL_MAX_VEL,
					String.valueOf(scrollMaxVel));

			tetrixGameAttr.put(GameAttributes.REMOVE_INTERVAL,
					String.valueOf(removeInterval));
			tetrixGameAttr.put(GameAttributes.EXPLOSION_VEL,
					String.valueOf(explosionVel));
			tetrixGameAttr.put(GameAttributes.EXPLOSION_ADDED_VEL,
					String.valueOf(explosionAddedVel));

			// ============

			tetrixGameAttr.put(GameAttributes.OUTER_SPARK_RAIDUS,
					String.valueOf(outerSparkRadius));
			tetrixGameAttr.put(GameAttributes.OUTER_SPARK_ACC,
					String.valueOf(outerSparkAcc));
			tetrixGameAttr.put(GameAttributes.INNER_SPARK_RAIDUS,
					String.valueOf(innerSparkRadius));

			tetrixGameAttr.put(GameAttributes.INNER_SPARK_ACC,
					String.valueOf(innerSparkAcc));
			tetrixGameAttr.put(GameAttributes.OUTER_SPARK_VEL,
					String.valueOf(outerSparkVel));
			tetrixGameAttr.put(GameAttributes.INNER_SPARK_VEL,
					String.valueOf(innerSparkVel));

			tetrixGameAttr.put(GameAttributes.SPARK_NUM_PER_BLOCK,
					String.valueOf(sparkNumPerBlock));
			tetrixGameAttr.put(GameAttributes.SPARK_VEL,
					String.valueOf(sparkVel));
			tetrixGameAttr.put(GameAttributes.SPARK_GRAVITY,
					String.valueOf(sparkGravity));

			tetrixGameAttr.put(GameAttributes.SPARK_DELAY_INTERVAL,
					String.valueOf(sparkDelayInterval));

			// =============

			tetrixGameAttr.put(GameAttributes.FIRE_LINE_VEL,
					String.valueOf(fireLineVel));
			tetrixGameAttr.put(GameAttributes.FIRE_LINE_GRAVITY,
					String.valueOf(fireLineGravity));
			tetrixGameAttr.put(GameAttributes.FIRE_LINE_LENGTH,
					String.valueOf(fireLineLength));

			tetrixGameAttr.put(GameAttributes.FIRE_LINE_LAYER_NUM,
					String.valueOf(fireLineLayerNum));
			tetrixGameAttr.put(GameAttributes.FIRE_LINE_NUM_PER_LAYER,
					String.valueOf(fireLineNumPerLayer));

			// ==============

			tetrixGameAttr.put(GameAttributes.SHAKE_MAX_OFFSET,
					String.valueOf(shakeMaxOffset));
			tetrixGameAttr.put(GameAttributes.SHAKE_PERIOD,
					String.valueOf(shakePeriod));
			tetrixGameAttr.put(GameAttributes.SHAKE_NUM_PERIOD,
					String.valueOf(shakeNumPeriod));

			// ==============

			tetrixGameAttr.put(GameAttributes.GAME_OVER_REMOVE_INTERVAL,
					String.valueOf(gameOverRemoveInterval));

			tetrixGameAttr.put(GameAttributes.GRID_WIDTH,
					String.valueOf(gridWidth));
			tetrixGameAttr.put(GameAttributes.ENABLE_EFFECTS,
					String.valueOf(enableEffects));

		}

		return tetrixGameAttr;
	}

	public boolean isEnableSound() {
		return enableSound;
	}

	public long getBaseScore() {
		return baseScore;
	}

}
