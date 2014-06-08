package com.accela.tetrixgame.gamelogic.basic;

import java.util.Collection;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;
import com.accela.tetrixgame.gamelogic.rectMover.scrolling.BarScrollProxy;

/**
 * 
 * 玩家可以控制的，正在下落的由几个方块所组成的方块条-Bar
 * 
 */
public class Bar extends MovableRect {
	private Block[][] bar;

	private Point lastPos;

	private BarScrollProxy scrollProxy;

	private Bar() {
		super(0, 0, 0, 0, new MovingAttributes());
	}

	public Bar(int x, int y, int blockWidth, int blockHeight, int rowNum,
			int columnNum, int[][] typeMatrix, double scrollMaxVel,
			MovingAttributes moveAttributes) {
		super(x, y, blockWidth * columnNum, blockHeight * rowNum,
				moveAttributes);

		if (rowNum <= 0 || columnNum <= 0) {
			throw new IllegalArgumentException("illegal rowNum of columnNum");
		}

		this.bar = new Block[rowNum][columnNum];
		fillBlock(typeMatrix, moveAttributes);
		this.lastPos = Point.create(x, y);

		this.scrollProxy = new BarScrollProxy(rowNum, columnNum, blockWidth,
				blockHeight, scrollMaxVel);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		Point.dispose(lastPos);
	}

	private void fillBlock(int[][] typeMatrix, MovingAttributes moveAttributes) {
		if (null == typeMatrix) {
			throw new NullPointerException("typeMatrix should not be null");
		}
		if (null == moveAttributes) {
			throw new NullPointerException("moveAttributes should not be null");
		}

		int blockWidth = getWidth() / bar[0].length;
		int blockHeight = getHeight() / bar.length;
		for (int i = 0; i < bar.length; i++) {
			for (int j = 0; j < bar[i].length; j++) {
				if (i < typeMatrix.length && j < typeMatrix[i].length) {
					bar[i][j] = new Block(x + j * blockWidth, y + i
							* blockHeight, blockWidth, blockHeight,
							typeMatrix[i][j], moveAttributes);
				} else {
					throw new IllegalArgumentException("typeMatrix too small");
				}
			}
		}
	}

	@Override
	public void resetMovingProxy() {
		super.resetMovingProxy();

		for (int i = 0; i < bar.length; i++) {
			for (int j = 0; j < bar[i].length; j++) {
				bar[i][j].resetMovingProxy();
			}
		}
	}

	@Override
	protected boolean update(HerizontalStatus status,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int rightBound, long interpolation) {
		boolean result = super.update(status, otherRects, grid, rightBound,
				interpolation);
		updateBarBlocks();

		return result;
	}

	@Override
	protected boolean update(VerticalStatus status,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int downBound, long interpolation) {
		boolean result = super.update(status, otherRects, grid, downBound,
				interpolation);
		updateBarBlocks();

		return result;
	}

	private void updateBarBlocks() {
		for (int i = 0; i < bar.length; i++) {
			for (int j = 0; j < bar[i].length; j++) {
				assert (bar[i][j] != null);

				bar[i][j].x += x - lastPos.x;
				bar[i][j].y += y - lastPos.y;
			}
		}

		updateLastPos();
	}

	private void updateLastPos() {
		lastPos.x = x;
		lastPos.y = y;
	}

	protected void update(ScrollStatus status, long interpolation) {
		scrollProxy.update(status, bar, x, y, interpolation);
	}

	public boolean update(HerizontalStatus hStatus, VerticalStatus vStatus,
			ScrollStatus sStatus, Collection<? extends Rectangle> otherRects,
			Grid grid, int rightBound, int downBound, long interpolation) {
		update(hStatus, otherRects, grid, rightBound, interpolation);
		update(sStatus, interpolation);
		boolean collided = update(vStatus, otherRects, grid, downBound,
				interpolation);

		this.refreshVelRecorder(interpolation);

		if (collided) {
			this.resetVelRecorder();
		}

		return collided;
	}

	public void actOnHitGround(Grid grid) {
		if (null == grid) {
			throw new NullPointerException("grid should not be null");
		}

		// 强制结束翻转过程
		scrollProxy.enforceFinish(bar, x, y);

		for (int i = 0; i < bar.length; i++) {
			for (int j = 0; j < bar[i].length; j++) {
				assert (bar[i][j].x == x + j * scrollProxy.getBlockWidth());
				assert (bar[i][j].y == y + i * scrollProxy.getBlockHeight());
			}
		}

		// 水平方向自动对齐网格
		Point lu = Point.create();
		getLeftUpPoint(lu);
		int xInGrid = grid.xCoordinateInGrid(lu);
		Point.dispose(lu);
		assert (xInGrid >= 0);

		if (xInGrid < grid.getGridWidth() / 2) {
			this.x -= xInGrid;
		} else {
			this.x += grid.getGridWidth() - xInGrid;
		}

		updateBarBlocks();
	}

	public boolean contains(Block block) {
		if (null == block) {
			throw new NullPointerException("block should not be null");
		}

		for (int i = 0; i < bar.length; i++) {
			for (int j = 0; j < bar[i].length; j++) {
				assert (bar[i][j] != null);

				if (bar[i][j].equals(block)) {
					return true;
				}
			}
		}

		return false;
	}

	public int getRowNum() {
		return bar.length;
	}

	public int getColumnNum() {
		return bar[0].length;
	}

	public Block getBlock(int row, int column) {
		if (row < 0 || row >= getRowNum()) {
			throw new IllegalArgumentException("illegal row: row = " + row);
		}
		if (column < 0 || column >= getColumnNum()) {
			throw new IllegalArgumentException("illegal column: column = "
					+ column);
		}

		return bar[row][column];
	}

}
