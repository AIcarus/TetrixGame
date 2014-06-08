package com.accela.tetrixgame.gamelogic.basic;

import java.util.HashMap;

/**
 * 
 * 初始化MovableRect，要用到的属性很多，因此用一个映射图来存储属性。
 * 
 */
public class MovingAttributes extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	// 属性个是什么意思，见TetrixGameAttributes相应项的注释

	public static final String GRAVITY = "gravity";
	public static final Double DEFAULT_GRAVITY = 0.005;

	public static final String V_MAX_VEL = "vMaxVel";
	public static final Double DEFAULT_V_MAX_VEL = 0.8;

	public static final String V_NORMAL_VEL = "vNormalVel";
	public static final Double DEFAULT_V_NORMAL_VEL = 0.1;

	public static final String V_MAX_ACC_TIME = "vMaxAccTime";
	public static final Long DEFAULT_V_MAX_ACC_TIME = 100L;

	public static final String H_MAX_VEL = "hMaxVel";
	public static final Double DEFAULT_H_MAX_VEL = 0.8;

	public static final String H_MAX_ACC_TIME = "hMaxAccTime";
	public static final Long DEFAULT_H_MAX_ACC_TIME = 300L;

	public MovingAttributes() {
		this.put(GRAVITY, String.valueOf(DEFAULT_GRAVITY));
		this.put(V_MAX_VEL, String.valueOf(DEFAULT_V_MAX_VEL));

		this.put(V_NORMAL_VEL, String.valueOf(DEFAULT_V_NORMAL_VEL));
		this.put(V_MAX_ACC_TIME, String.valueOf(DEFAULT_V_MAX_ACC_TIME));

		this.put(H_MAX_VEL, String.valueOf(DEFAULT_H_MAX_VEL));
		this.put(H_MAX_ACC_TIME, String.valueOf(DEFAULT_H_MAX_ACC_TIME));
	}

}
