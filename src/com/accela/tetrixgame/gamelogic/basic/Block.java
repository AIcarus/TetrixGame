package com.accela.tetrixgame.gamelogic.basic;

import java.util.Collection;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * 游戏中的方块
 * 
 */
public class Block extends MovableRect {
	private int type;

	private Block() {
		super(0, 0, 0, 0, new MovingAttributes());
	}

	public Block(int x, int y, int width, int height, int type,
			MovingAttributes moveAttributes) {
		super(x, y, width, height, moveAttributes);

		if (type < 0) {
			throw new IllegalArgumentException("type is illegal: type = "
					+ type);
		}

		this.type = type;
	}

	public int getType() {
		return type;
	}

	public boolean isOfSameType(Block block) {
		if (null == block) {
			throw new NullPointerException("block should not be null");
		}

		return type == block.type;
	}

	public boolean update(HerizontalStatus hStatus, VerticalStatus vStatus,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int rightBound, int downBound, long interpolation) {
		update(hStatus, otherRects, grid, rightBound, interpolation);
		boolean collided = update(vStatus, otherRects, grid, downBound,
				interpolation);

		this.refreshVelRecorder(interpolation);

		if (collided) {
			this.resetVelRecorder();
		}

		return collided;
	}

}
