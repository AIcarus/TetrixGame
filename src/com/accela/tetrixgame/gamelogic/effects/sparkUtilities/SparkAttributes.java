package com.accela.tetrixgame.gamelogic.effects.sparkUtilities;

import java.util.*;

public class SparkAttributes extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public static final String OUTER_SPARK_RAIDUS = "outerSparkRadius";
	public static final Integer DEFAULT_OUTER_SPARK_RAIDUS = 32 / 4;

	public static final String OUTER_SPARK_ACC = "outerSparkAcc";
	public static final Double DEFAULT_OUTER_SPARK_ACC = 0.001;

	public static final String INNER_SPARK_RAIDUS = "innerSparkRadius";
	public static final Integer DEFAULT_INNER_SPARK_RAIDUS = 32 / 8;

	public static final String INNER_SPARK_ACC = "innerSparkAcc";
	public static final Double DEFAULT_INNER_SPARK_ACC = 0.0015;

	public static final String OUTER_SPARK_VEL = "outerSparkVel";
	public static final Double DEFAULT_OUTER_SPARK_VEL = 0.02;

	public static final String INNER_SPARK_VEL = "innerSparkVel";
	public static final Double DEFAULT_INNER_SPARK_VEL = 0.005;

	public static final String SPARK_NUM_PER_BLOCK = "sparkNumPerBlock";
	public static final Integer DEFAULT_SPARK_NUM_PER_BLOCK = 8;

	public static final String SPARK_VEL = "sparkVel";
	public static final Double DEFAULT_SPARK_VEL = 0.4;

	public static final String SPARK_GRAVITY = "sparkGravity";
	public static final Double DEFAULT_SPARK_GRAVITY = 0.0;

	public static final String SPARK_DELAY_INTERVAL = "sparkDelayInterval";
	public static final Long DEFAULT_SPARK_DELAY_INTERVAL = 10L;

	public SparkAttributes() {
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
	}

	public SparkAttributes(int outerSparkRadius, double outerSparkAcc,
			int innerSparkRadius, double innerSparkAcc, double outerSparkVel,
			double innerSparkVel, int sparkNumPerBlock, double sparkVel,
			double sparkGravity, long sparkDelayInterval) {
		this.put(OUTER_SPARK_RAIDUS, String.valueOf(outerSparkRadius));
		this.put(OUTER_SPARK_ACC, String.valueOf(outerSparkAcc));
		this.put(INNER_SPARK_RAIDUS, String.valueOf(innerSparkRadius));

		this.put(INNER_SPARK_ACC, String.valueOf(innerSparkAcc));
		this.put(OUTER_SPARK_VEL, String.valueOf(outerSparkVel));
		this.put(INNER_SPARK_VEL, String.valueOf(innerSparkVel));

		this.put(SPARK_NUM_PER_BLOCK, String.valueOf(sparkNumPerBlock));
		this.put(SPARK_VEL, String.valueOf(sparkVel));
		this.put(SPARK_GRAVITY, String.valueOf(sparkGravity));

		this.put(SPARK_DELAY_INTERVAL, String.valueOf(sparkDelayInterval));
	}

}
