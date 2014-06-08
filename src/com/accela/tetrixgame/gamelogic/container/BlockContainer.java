package com.accela.tetrixgame.gamelogic.container;

import com.accela.tetrixgame.gamelogic.basic.Bar;
import com.accela.tetrixgame.gamelogic.geometry.Grid;

/**
 * 
 * 游戏中装载所有方块的容器
 * 
 */
public class BlockContainer {
	private BlockMatrix matrix;

	private Bar bar;

	public BlockContainer(int rowNum, int columnNum, Grid grid) {
		if (rowNum <= 0 || columnNum <= 0) {
			throw new IllegalArgumentException("illegal rowNum or colunmNum");
		}
		if (null == grid) {
			throw new NullPointerException("grid should not be null");
		}

		matrix = new BlockMatrix(rowNum, columnNum, grid);
		bar = null;
	}

	public BlockMatrix getMatrix() {
		return matrix;
	}

	public Bar getBar() {
		return bar;
	}

	public Grid getGrid() {
		return matrix.getGrid();
	}

	public void setBar(Bar bar) {
		this.bar = bar;
	}

	public void addBarToMatrix() {
		if (null == bar) {
			throw new IllegalStateException("bar is null now");
		}

		for (int i = 0; i < bar.getRowNum(); i++) {
			for (int j = 0; j < bar.getColumnNum(); j++) {
				boolean result = matrix.add(bar.getBlock(i, j));
				assert (result);
			}
		}

		matrix.updateAll();
		this.bar = null;
	}

}
