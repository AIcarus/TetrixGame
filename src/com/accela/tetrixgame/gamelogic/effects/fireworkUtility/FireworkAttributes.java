package com.accela.tetrixgame.gamelogic.effects.fireworkUtility;

import java.util.*;

/**
 * 
 * 烟花效果的属性，用来初始化烟花
 * 
 */
public class FireworkAttributes extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	// 各个属性的具体含义，见TetrixGameAttributes中的相应项。
	public static final String FIRE_LINE_VEL = "fireLineVel";
	public static final Double DEFAULT_FIRE_LINE_VEL = 1.6;

	public static final String FIRE_LINE_GRAVITY = "fireLineGravity";
	public static final Double DEFAULT_FIRE_LINE_GRAVITY = 0.003;

	public static final String FIRE_LINE_LENGTH = "fireLineLength";
	public static final Integer DEFAULT_FIRE_LINE_LENGTH = 15;

	public static final String FIRE_LINE_LAYER_NUM = "fireLineLayerNum";
	public static final Integer DEFAULT_FIRE_LINE_LAYER_NUM = 4;

	public static final String FIRE_LINE_NUM_PER_LAYER = "fireLineNumPerLayer";
	public static final Integer DEFAULT_FIRE_LINE_NUM_PER_LAYER = 11;

	public FireworkAttributes() {

		this.put(FIRE_LINE_VEL, String.valueOf(DEFAULT_FIRE_LINE_VEL));
		this.put(FIRE_LINE_GRAVITY, String.valueOf(DEFAULT_FIRE_LINE_GRAVITY));
		this.put(FIRE_LINE_LENGTH, String.valueOf(DEFAULT_FIRE_LINE_LENGTH));

		this.put(FIRE_LINE_LAYER_NUM,
				String.valueOf(DEFAULT_FIRE_LINE_LAYER_NUM));
		this.put(FIRE_LINE_NUM_PER_LAYER,
				String.valueOf(DEFAULT_FIRE_LINE_NUM_PER_LAYER));
	}

	public FireworkAttributes(double fireLineVel, double fireLineGravity,
			int fireLineLength, int fireLineLayerNum, int fireLineNumPerLayer) {
		this.put(FIRE_LINE_VEL, String.valueOf(fireLineVel));
		this.put(FIRE_LINE_GRAVITY, String.valueOf(fireLineGravity));
		this.put(FIRE_LINE_LENGTH, String.valueOf(fireLineLength));

		this.put(FIRE_LINE_LAYER_NUM, String.valueOf(fireLineLayerNum));
		this.put(FIRE_LINE_NUM_PER_LAYER, String.valueOf(fireLineNumPerLayer));
	}

}
