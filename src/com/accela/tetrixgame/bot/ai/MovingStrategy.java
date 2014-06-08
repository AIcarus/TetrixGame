package com.accela.tetrixgame.bot.ai;

/**
 * 
 * AI经过运算后，决定的Bar的移动策略
 * 
 */
public class MovingStrategy {
	public static enum HerizontalStrategy {
		LEFT, RIGHT, NONE,
	}

	public HerizontalStrategy hs;

	public static enum VerticalStrategy {
		NONE, DOWN_ACC
	}

	public VerticalStrategy vs;

	public static enum ScrollStrategy {
		NONE, SCROLL
	}

	public ScrollStrategy ss;

	protected void clear() {
		hs = null;
		vs = null;
		ss = null;
	}

	protected void sweepNull() {
		if (null == hs) {
			hs = HerizontalStrategy.NONE;
		}
		if (null == vs) {
			vs = VerticalStrategy.NONE;
		}
		if (null == ss) {
			ss = ScrollStrategy.NONE;
		}
	}

}
