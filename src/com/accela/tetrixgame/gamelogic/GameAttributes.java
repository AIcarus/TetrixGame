package com.accela.tetrixgame.gamelogic;

import java.util.HashMap;

/**
 * 
 * TetrixGame包含大量的属性，这些属性在构造方法中需要传入，但是这很麻烦。因此使用 一个哈希映射表来装载这些属性。同时所有属性的默认值也定义在其中。
 * 
 */
public class GameAttributes extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	/**
	 * 随机数种子，用来随机地产生砖块
	 */
	public static final String RANDOM_SEED = "randomSeed";
	public static final Integer DEFAULT_RANDOM_SEED = 44;

	/**
	 * 游戏区域的网格总行数
	 */
	public static final String ROW_NUM = "rowNum";
	public static final Integer DEFAULT_ROW_NUM = 16;

	/**
	 * 游戏区域的网格总列数
	 */
	public static final String COLUMN_NUM = "columnNum";
	public static final Integer DEFAULT_COLUMN_NUM = 8;

	/**
	 * 游戏中玩家控制的下落的方块条Bar的行数
	 */
	public static final String BAR_ROW_NUM = "barRowNum";
	public static final Integer DEFAULT_BAR_ROW_NUM = 3;

	/**
	 * 游戏中玩家控制的下落的方块条Bar的列数
	 */
	public static final String BAR_COLUMN_NUM = "barColumnNum";
	public static final Integer DEFAULT_BAR_COLUMN_NUM = 1;

	/**
	 * 游戏中砖块的种类的总数
	 */
	public static final String BLOCK_TYPE_NUM = "blockTypeNum";
	public static final Integer DEFAULT_BLOCK_TYPE_NUM = 5;

	/**
	 * 游戏中砖块的宽度
	 */
	public static final String BLOCK_WIDTH = "blockWidth";
	public static final Integer DEFAULT_BLOCK_WIDTH = 32;

	/**
	 * 游戏中砖块的高度
	 */
	public static final String BLOCK_HEIGHT = "blockHeight";
	public static final Integer DEFAULT_BLOCK_HEIGHT = 32;

	/**
	 * 砖块水平方向的移动的最大加速时间，见水平移动代理
	 */
	public static final String H_MAX_ACC_TIME = "hMaxAccTime";
	public static final Long DEFAULT_H_MAX_ACC_TIME = 100L;

	/**
	 * 砖块水平方向移动的最大速度，见水平移动代理
	 */
	public static final String H_MAX_VEL = "hMaxVel";
	public static final Double DEFAULT_H_MAX_VEL = 0.3;

	/**
	 * 砖块竖直方向的移动的最大加速时间，见竖直移动代理
	 */
	public static final String V_MAX_ACC_TIME = "vMaxAccTime";
	public static final Long DEFAULT_V_MAX_ACC_TIME = 100L;

	/**
	 * 砖块竖直方向移动的正常速度，见竖直移动代理
	 */
	public static final String V_NORMAL_VEL = "vNormalVel";
	public static final Double DEFAULT_V_NORMAL_VEL = 0.1;

	/**
	 * 砖块竖直方向移动的最大速度，见竖直移动代理
	 */
	public static final String V_MAX_VEL = "vMaxVel";
	public static final Double DEFAULT_V_MAX_VEL = 0.8;

	/**
	 * Bar翻转的时候的最大速度，见Bar翻转代理
	 */
	public static final String SCROLL_MAX_VEL = "scrollMaxVel";
	public static final Double DEFAULT_SCROLL_MAX_VEL = 0.5;

	/**
	 * 消除方块的时候，消除了一个方块，然后再消除下一个方块，之间的时间间隔
	 */
	public static final String REMOVE_INTERVAL = "removeInterval";
	public static final Long DEFAULT_REMOVE_INTERVAL = 30L;

	/**
	 * 方块下落的时候所受的重力加速度
	 */
	public static final String GRAVITY = "gravity";
	public static final Double DEFAULT_GRAVITY = 0.003;

	/**
	 * 砖块消除时，爆炸使得其他方块飞起来的基本速度
	 */
	public static final String EXPLOSION_VEL = "explosionVel";
	public static final Double DEFAULT_EXPLOSION_VEL = 0.8;

	/**
	 * 砖块消除时，爆炸使得其他方块飞起来的递增速度
	 */
	public static final String EXPLOSION_ADDED_VEL = "explosionAddedVel";
	public static final Double DEFAULT_EXPLOSION_ADDED_VEL = 0.18;

	// ////////////////////////////////////////////////////////////////////////

	/**
	 * 火花效果中外层火花的半径
	 */
	public static final String OUTER_SPARK_RAIDUS = "outerSparkRadius";
	public static final Integer DEFAULT_OUTER_SPARK_RAIDUS = (DEFAULT_BLOCK_WIDTH + DEFAULT_BLOCK_HEIGHT) / 2 / 4;

	/**
	 * 火花效果中外层火花的半径扩张速度
	 */
	public static final String OUTER_SPARK_VEL = "outerSparkVel";
	public static final Double DEFAULT_OUTER_SPARK_VEL = 0.02;

	/**
	 * 火花效果中外层火花的半径扩张加速度
	 */
	public static final String OUTER_SPARK_ACC = "outerSparkAcc";
	public static final Double DEFAULT_OUTER_SPARK_ACC = 0.0003;

	/**
	 * 火花效果中内层火花的半径
	 */
	public static final String INNER_SPARK_RAIDUS = "innerSparkRadius";
	public static final Integer DEFAULT_INNER_SPARK_RAIDUS = (DEFAULT_BLOCK_WIDTH + DEFAULT_BLOCK_HEIGHT) / 2 / 8;

	/**
	 * 火花效果中内层火花的半径扩张速度
	 */
	public static final String INNER_SPARK_VEL = "innerSparkVel";
	public static final Double DEFAULT_INNER_SPARK_VEL = 0.005;

	/**
	 * 火花效果中内层火花的半径扩张加速度
	 */
	public static final String INNER_SPARK_ACC = "innerSparkAcc";
	public static final Double DEFAULT_INNER_SPARK_ACC = 0.0008;

	/**
	 * 每个砖块爆炸的时候，产生的火花效果的数量
	 */
	public static final String SPARK_NUM_PER_BLOCK = "sparkNumPerBlock";
	public static final Integer DEFAULT_SPARK_NUM_PER_BLOCK = 16;

	/**
	 * 火花飞出的初速度
	 */
	public static final String SPARK_VEL = "sparkVel";
	public static final Double DEFAULT_SPARK_VEL = 0.05;

	/**
	 * 火花所受重力
	 */
	public static final String SPARK_GRAVITY = "sparkGravity";
	public static final Double DEFAULT_SPARK_GRAVITY = 0.0;

	/**
	 * 产生个个火花时，每个火花产生的时间间隔
	 */
	public static final String SPARK_DELAY_INTERVAL = "sparkDelayInterval";
	public static final Long DEFAULT_SPARK_DELAY_INTERVAL = 10L;

	// //////////////////////////////////////////////////////////////////////

	/**
	 * 烟花效果中，烟花线的初速度
	 */
	public static final String FIRE_LINE_VEL = "fireLineVel";
	public static final Double DEFAULT_FIRE_LINE_VEL = 0.9;

	/**
	 * 烟花线所受的重力
	 */
	public static final String FIRE_LINE_GRAVITY = "fireLineGravity";
	public static final Double DEFAULT_FIRE_LINE_GRAVITY = 0.001;

	/**
	 * 烟花线的长度
	 */
	public static final String FIRE_LINE_LENGTH = "fireLineLength";
	public static final Integer DEFAULT_FIRE_LINE_LENGTH = 15;

	/**
	 * 一个烟花效果中，烟花线的层数
	 */
	public static final String FIRE_LINE_LAYER_NUM = "fireLineLayerNum";
	public static final Integer DEFAULT_FIRE_LINE_LAYER_NUM = 4;

	/**
	 * 一个烟花效果中，每层烟花线中烟花的数量
	 */
	public static final String FIRE_LINE_NUM_PER_LAYER = "fireLineNumPerLayer";
	public static final Integer DEFAULT_FIRE_LINE_NUM_PER_LAYER = 20;

	// ////////////////////////////////////////////////////////////////////////

	/**
	 * 地震效果中，地震的最大偏移量
	 */
	public static final String SHAKE_MAX_OFFSET = "shakeMaxOffset";
	public static final Integer DEFAULT_SHAKE_MAX_OFFSET = 5;

	/**
	 * 地震效果中，振动的周期
	 */
	public static final String SHAKE_PERIOD = "shakePeriod";
	public static final Long DEFAULT_SHAKE_PERIOD = 50L;

	/**
	 * 地震效果中，振动的持续周期数
	 */
	public static final String SHAKE_NUM_PERIOD = "shakeNumPeriod";
	public static final Integer DEFAULT_SHAKE_NUM_PERIOD = 10;

	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * 游戏结束的时候，砖块一次爆炸的时间间隔
	 */
	public static final String GAME_OVER_REMOVE_INTERVAL = "gameOverRemoveInterval";
	public static final Long DEFAULT_GAME_OVER_REMOVE_INTERVAL = 30L;

	/**
	 * 游戏区域的边界线的宽度
	 */
	public static final String GRID_WIDTH = "gridWidth";
	public static final Integer DEFAULT_GRID_WIDTH = 10;

	/**
	 * 是否打开华丽的效果，关闭效果可以提高性能
	 */
	public static final String ENABLE_EFFECTS = "enableEffects";
	public static final Boolean DEFAULT_ENABLE_EFFECTS = true;

	/**
	 * 是否打开音效
	 */
	public static final String ENABLE_SOUND = "enableSound";
	public static final Boolean DEFAULT_ENABLE_SOUND = true;

	public static final String BASE_SCORE = "baseScore";
	public static final Long DEFAULT_BASE_SCORE = 10L;
	
	public GameAttributes() {
		this.put(RANDOM_SEED, String.valueOf(DEFAULT_RANDOM_SEED));
		this.put(H_MAX_ACC_TIME, String.valueOf(DEFAULT_H_MAX_ACC_TIME));
		this.put(H_MAX_VEL, String.valueOf(DEFAULT_H_MAX_VEL));

		this.put(V_MAX_ACC_TIME, String.valueOf(DEFAULT_V_MAX_ACC_TIME));
		this.put(V_MAX_VEL, String.valueOf(DEFAULT_V_MAX_VEL));
		this.put(V_NORMAL_VEL, String.valueOf(DEFAULT_V_NORMAL_VEL));

		this.put(GRAVITY, String.valueOf(DEFAULT_GRAVITY));
		this.put(ROW_NUM, String.valueOf(DEFAULT_ROW_NUM));
		this.put(COLUMN_NUM, String.valueOf(DEFAULT_COLUMN_NUM));

		this.put(BAR_ROW_NUM, String.valueOf(DEFAULT_BAR_ROW_NUM));
		this.put(BAR_COLUMN_NUM, String.valueOf(DEFAULT_BAR_COLUMN_NUM));
		this.put(BLOCK_TYPE_NUM, String.valueOf(DEFAULT_BLOCK_TYPE_NUM));

		this.put(BLOCK_WIDTH, String.valueOf(DEFAULT_BLOCK_WIDTH));
		this.put(BLOCK_HEIGHT, String.valueOf(DEFAULT_BLOCK_HEIGHT));
		this.put(SCROLL_MAX_VEL, String.valueOf(DEFAULT_SCROLL_MAX_VEL));

		this.put(REMOVE_INTERVAL, String.valueOf(DEFAULT_REMOVE_INTERVAL));
		this.put(EXPLOSION_VEL, String.valueOf(DEFAULT_EXPLOSION_VEL));
		this.put(EXPLOSION_ADDED_VEL,
				String.valueOf(DEFAULT_EXPLOSION_ADDED_VEL));

		// ============

		this.put(OUTER_SPARK_RAIDUS, String.valueOf(DEFAULT_OUTER_SPARK_RAIDUS));
		this.put(OUTER_SPARK_ACC, String.valueOf(DEFAULT_OUTER_SPARK_ACC));
		this.put(INNER_SPARK_RAIDUS, String.valueOf(DEFAULT_INNER_SPARK_RAIDUS));

		this.put(INNER_SPARK_ACC, String.valueOf(DEFAULT_INNER_SPARK_ACC));
		this.put(OUTER_SPARK_VEL, String.valueOf(DEFAULT_OUTER_SPARK_VEL));
		this.put(INNER_SPARK_VEL, String.valueOf(DEFAULT_INNER_SPARK_VEL));

		this.put(SPARK_NUM_PER_BLOCK,
				String.valueOf(DEFAULT_SPARK_NUM_PER_BLOCK));
		this.put(SPARK_VEL, String.valueOf(DEFAULT_SPARK_VEL));
		this.put(SPARK_GRAVITY, String.valueOf(DEFAULT_SPARK_GRAVITY));

		this.put(SPARK_DELAY_INTERVAL,
				String.valueOf(DEFAULT_SPARK_DELAY_INTERVAL));

		// =============

		this.put(FIRE_LINE_VEL, String.valueOf(DEFAULT_FIRE_LINE_VEL));
		this.put(FIRE_LINE_GRAVITY, String.valueOf(DEFAULT_FIRE_LINE_GRAVITY));
		this.put(FIRE_LINE_LENGTH, String.valueOf(DEFAULT_FIRE_LINE_LENGTH));

		this.put(FIRE_LINE_LAYER_NUM,
				String.valueOf(DEFAULT_FIRE_LINE_LAYER_NUM));
		this.put(FIRE_LINE_NUM_PER_LAYER,
				String.valueOf(DEFAULT_FIRE_LINE_NUM_PER_LAYER));

		// ==============

		this.put(SHAKE_MAX_OFFSET, String.valueOf(DEFAULT_SHAKE_MAX_OFFSET));
		this.put(SHAKE_PERIOD, String.valueOf(DEFAULT_SHAKE_PERIOD));
		this.put(SHAKE_NUM_PERIOD, String.valueOf(DEFAULT_SHAKE_NUM_PERIOD));

		// ==============

		this.put(GAME_OVER_REMOVE_INTERVAL,
				String.valueOf(DEFAULT_GAME_OVER_REMOVE_INTERVAL));

		this.put(GRID_WIDTH, String.valueOf(DEFAULT_GRID_WIDTH));
		this.put(ENABLE_EFFECTS, String.valueOf(DEFAULT_ENABLE_EFFECTS));

		// ==============
		this.put(ENABLE_SOUND, String.valueOf(DEFAULT_ENABLE_SOUND));

		this.put(BASE_SCORE, String.valueOf(DEFAULT_BASE_SCORE));

	}

	@SuppressWarnings("unchecked")
	public GameAttributes clone() {
		GameAttributes attr = new GameAttributes();
		attr.putAll((HashMap<String, String>) super.clone());

		return attr;
	}

}
