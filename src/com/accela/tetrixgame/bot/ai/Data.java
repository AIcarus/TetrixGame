package com.accela.tetrixgame.bot.ai;

import java.awt.Point;

import com.accela.tetrixgame.gamelogic.basic.Bar;
import com.accela.tetrixgame.gamelogic.basic.Block;
import com.accela.tetrixgame.gamelogic.container.BlockContainer;

/**
 * 
 * TetrixAI从游戏中抽取出来的数据，作为判断
 * 
 */
public class Data {// TODO 这个类所使用的经过缓存处理的点、矩形等，是否有内存泄漏没有测试
	private boolean reset = true;

	private boolean firstIn = true;

	private int blockWidth;

	private int blockHeight;

	private int[][] matrix = null; // 不包括bar, -1 表示不存在，其它的用blockType表示

	private int[][] matrixCopy = null; // 充当getter和setter中返回给外界的值

	private Point[][] barIdx = null;

	private Point[][] barIdxCopy = null;

	private Point barOffsetInGrid = null;

	private int[][][] barTypes = null; // barTypes[0]表示当前bar的样子，barTypes[1]表示bar翻转一次后的样子，以此类推

	private int[][][] barTypesCopy = null;

	private boolean isHerizontalMoving = false;

	private double barXVel = 0;

	private double barYVel = 0;

	private boolean isScrolling = false;

	private long lastScrollElapse = -1; // 最近一次翻转所用的时间

	private long curScrollElapse = -1; // 如果当前正在翻转，那么翻转已经进行了多少时间

	/**
	 * 当游戏处于BarDropping状态中的时候，调用这个方法，刷新TetrixData中的数据
	 */
	protected void update(BlockContainer blockContainer, long interpolation) {
		if (null == blockContainer) {
			throw new NullPointerException("blockContainer should not be null");
		}
		if (null == blockContainer.getBar()) {
			throw new NullPointerException("bar should not be null");
		}
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should not be negtive");
		}

		// 处理重置
		if (reset) {
			resetImediately(blockContainer, interpolation);
			reset = false;
			firstIn = true;
		} else {
			firstIn = false;
		}

		// 更新matrix
		assert (matrix.length == blockContainer.getMatrix().getRowNum());
		for (int i = 0; i < matrix.length; i++) {
			assert (matrix[i].length == blockContainer.getMatrix()
					.getColumnNum());

			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = -1;
			}
		}

		for (Block b : blockContainer.getMatrix()) {
			assert (b != null);
			assert (blockContainer.getMatrix().getGrid().isOnGridCross(b));

			com.accela.tetrixgame.gamelogic.geometry.Point p = com.accela.tetrixgame.gamelogic.geometry.Point
					.create();
			blockContainer.getMatrix().getGrid().atWhichRowColumn(p, b);

			matrix[p.x][p.y] = b.getType();

			com.accela.tetrixgame.gamelogic.geometry.Point.dispose(p);
		}

		// 更新barIdx
		Bar bar = blockContainer.getBar();

		com.accela.tetrixgame.gamelogic.geometry.Point p = com.accela.tetrixgame.gamelogic.geometry.Point
				.create();

		bar.getLeftUpPoint(p);
		com.accela.tetrixgame.gamelogic.geometry.Point idx = com.accela.tetrixgame.gamelogic.geometry.Point
				.create();
		blockContainer.getGrid().atWhichRowColumn(idx, p);

		barIdx[0][0].x = idx.x;
		barIdx[0][0].y = idx.y;

		com.accela.tetrixgame.gamelogic.geometry.Point.dispose(idx);
		com.accela.tetrixgame.gamelogic.geometry.Point.dispose(p);

		assert (barIdx.length == bar.getRowNum());
		for (int i = 0; i < barIdx.length; i++) {
			assert (barIdx[i].length == bar.getColumnNum());
			for (int j = 0; j < barIdx[i].length; j++) {
				barIdx[i][j].x = barIdx[0][0].x + i;
				barIdx[i][j].y = barIdx[0][0].y + j;
			}
		}

		// 更新barOffsetInGrid
		p = com.accela.tetrixgame.gamelogic.geometry.Point.create();

		bar.getLeftUpPoint(p);
		barOffsetInGrid.x = blockContainer.getMatrix().getGrid()
				.xCoordinateInGrid(p);
		barOffsetInGrid.y = blockContainer.getMatrix().getGrid()
				.yCoordinateInGrid(p);

		com.accela.tetrixgame.gamelogic.geometry.Point.dispose(p);

		// 更新barType
		assert (barTypes.length == bar.getRowNum());
		for (int scroll_idx = 0; scroll_idx < barTypes.length; scroll_idx++) {
			int[][] barType = barTypes[scroll_idx];
			if (0 == scroll_idx) {
				assert (barType.length == bar.getRowNum());
				for (int i = 0; i < barType.length; i++) {
					assert (barType[i].length == bar.getColumnNum());

					for (int j = 0; j < barType[i].length; j++) {
						Block b = bar.getBlock(i, j);
						assert (b != null);

						barType[i][j] = b.getType();

					}
				}
			} else {
				int[][] lastBarType = barTypes[scroll_idx - 1];
				for (int i = 0; i < barType.length - 1; i++) {
					for (int j = 0; j < barType[i].length; j++) {
						barType[i][j] = lastBarType[i + 1][j];
					}
				}
				for (int i = 0; i < barType[barType.length - 1].length; i++) {
					barType[barType.length - 1][i] = lastBarType[0][barType[barType.length - 1].length
							- 1 - i];
				}
			}
		}

		// 更新barXVel和barYVel
		barXVel = bar.getHerizontalVel();
		barYVel = bar.getVerticalVel();

		// 更新isHerizontalMoing
		isHerizontalMoving = Math.abs(barXVel) < 1e-4;

		// 更新isScrolling,lastScrollElapse和curScrollElapse
		boolean curIsScrolling = false;
		for (int i = 0; i < bar.getRowNum(); i++) {
			for (int j = 0; j < bar.getColumnNum(); j++) {
				Block b = bar.getBlock(i, j);
				assert (b != null);

				if (b.x != bar.x + j * b.getWidth()
						|| b.y != bar.y + i * b.getHeight()) {
					curIsScrolling = true;
					break;
				}
			}
		}

		if (isScrolling) {
			if (!curIsScrolling) {
				lastScrollElapse = curScrollElapse;
				curScrollElapse = -1;
			} else {
				curScrollElapse += interpolation;
			}
		} else {
			if (!curIsScrolling) {
				curScrollElapse = -1;
			} else {
				curScrollElapse = 0;
			}
		}

		isScrolling = curIsScrolling;

	}

	private void resetImediately(BlockContainer blockContainer,
			long interpolation) {
		assert (blockContainer != null);
		assert (blockContainer.getBar() != null);
		assert (interpolation >= 0);

		// 创建matrix
		if (null == matrix
				|| matrix.length != blockContainer.getMatrix().getRowNum()
				|| matrix[0].length != blockContainer.getMatrix()
						.getColumnNum()) {
			matrix = new int[blockContainer.getMatrix().getRowNum()][blockContainer
					.getMatrix().getColumnNum()];
		}

		assert (matrix.length > 0);

		// 创建barIdx
		if (null == barIdx
				|| barIdx.length != blockContainer.getBar().getRowNum()
				|| barIdx[0].length != blockContainer.getBar().getColumnNum()) {
			barIdx = new Point[blockContainer.getBar().getRowNum()][blockContainer
					.getBar().getColumnNum()];

			for (int i = 0; i < barIdx.length; i++) {
				for (int j = 0; j < barIdx[i].length; j++) {
					barIdx[i][j] = new Point();
				}
			}
		}

		assert (barIdx.length > 0);

		// 创建barOffestInGrid
		if (null == barOffsetInGrid) {
			barOffsetInGrid = new Point();
		}

		// 创建barType
		if (null == barTypes
				|| barTypes.length != blockContainer.getBar().getRowNum()
				|| barTypes[0].length != blockContainer.getBar().getRowNum()
				|| barTypes[0][0].length != blockContainer.getBar()
						.getColumnNum()) {
			barTypes = new int[blockContainer.getBar().getRowNum()][blockContainer
					.getBar().getRowNum()][blockContainer.getBar()
					.getColumnNum()];
		}

		assert (barTypes[0].length == barIdx.length);
		assert (barTypes[0][0].length == barIdx[0].length);

		// 重置变量
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = -1;
			}
		}

		for (int i = 0; i < barIdx.length; i++) {
			for (int j = 0; j < barIdx[i].length; j++) {
				barIdx[i][j].x = 0;
				barIdx[i][j].y = 0;
			}
		}

		barOffsetInGrid.x = 0;
		barOffsetInGrid.y = 0;

		for (int i = 0; i < barTypes.length; i++) {
			for (int j = 0; j < barTypes[i].length; j++) {
				for (int k = 0; k < barTypes[i][j].length; k++) {
					barTypes[i][j][k] = -1;
				}
			}
		}

		isHerizontalMoving = false;

		barXVel = 0;
		barYVel = 0;

		isScrolling = false;
		if (-1 == lastScrollElapse) {
			lastScrollElapse = curScrollElapse;
		}
		curScrollElapse = -1;

		// 更新blockWidth和blockHeight
		blockWidth = blockContainer.getGrid().getGridWidth();
		blockHeight = blockContainer.getGrid().getGridHeight();

	}

	/**
	 * 当游戏状态从BarDropping状态中离开的时候，应该 调用这个方法，已通知TetrixData。
	 */
	// 懒惰是reset
	protected void reset() {
		reset = true;
	}

	// ///////////////////////////////////////////////////////////////////////////////////

	private void testReset() {
		if (reset) {
			throw new IllegalStateException(
					"please update to perform reset before you invokde these methods. ");
		}
	}

	public int getMatrixRowNum() {
		testReset();

		return matrix.length;
	}

	public int getMatrixColumnNum() {
		testReset();

		return matrix[0].length;
	}

	private boolean checkMatrixIdx(int i, int j) {
		testReset();

		if (i >= 0 && i < matrix.length) {
			if (j >= 0 && j < matrix[0].length) {
				return true;
			}
		}

		return false;
	}

	public boolean hasMatrixBlock(int i, int j) {
		testReset();

		if (!checkMatrixIdx(i, j)) {
			throw new IllegalArgumentException("idx out of bound: i = " + i
					+ ", j = " + j);
		}

		return matrix[i][j] >= 0;
	}

	public int getMatrixBlockType(int i, int j) {
		testReset();

		if (!checkMatrixIdx(i, j)) {
			throw new IllegalArgumentException("idx out of bound: i = " + i
					+ ", j = " + j);
		}
		if (!hasMatrixBlock(i, j)) {
			throw new IllegalArgumentException("don't have a block here");
		}

		return matrix[i][j];
	}

	public int getBarRowNum() {
		testReset();

		return barIdx.length;
	}

	public int getBarColumnNum() {
		testReset();

		return barIdx[0].length;
	}

	private boolean checkBarIdx(int i, int j) {
		testReset();

		if (i >= 0 && i < barIdx.length) {
			if (j >= 0 && j < barIdx[0].length) {
				return true;
			}
		}

		return false;
	}

	public int getBarBlockInWhichRow(int i, int j) {
		testReset();

		if (!checkBarIdx(i, j)) {
			throw new IllegalArgumentException("idx out of bound: i = " + i
					+ ", j = " + j);
		}

		return barIdx[i][j].x;
	}

	public int getBarBlockInWhichColumn(int i, int j) {
		testReset();

		if (!checkBarIdx(i, j)) {
			throw new IllegalArgumentException("idx out of bound: i = " + i
					+ ", j = " + j);
		}

		return barIdx[i][j].y;
	}

	public int getBarOffsetXInGrid() {
		testReset();

		return barOffsetInGrid.x;
	}

	public int getBarOffsetYInGrid() {
		testReset();

		return barOffsetInGrid.y;
	}

	public int getBarBlockType(int scrollIdx, int i, int j) {
		testReset();

		if (scrollIdx >= getBarRowNum()) {
			throw new IllegalArgumentException(
					"scrollIdx should be less than bar row num");
		}
		if (!checkBarIdx(i, j)) {
			throw new IllegalArgumentException("idx out of bound: i = " + i
					+ ", j = " + j);
		}

		assert (barTypes[scrollIdx][i][j] >= 0);
		return barTypes[scrollIdx][i][j];
	}

	public boolean isMovingHerizontally() {
		testReset();

		return isHerizontalMoving;
	}

	public double getBarHerizontalVel() {
		testReset();

		return barXVel;
	}

	public double getBarVerticalVel() {
		testReset();

		return barYVel;
	}

	public boolean isScrolling() {
		testReset();

		if (isScrolling) {
			assert (curScrollElapse >= 0);
		} else {
			assert (-1 == curScrollElapse);
		}

		return isScrolling;
	}

	public boolean hasLastScrollElapse() {
		testReset();

		assert (-1 == lastScrollElapse || lastScrollElapse >= 0);

		return -1 == lastScrollElapse;
	}

	public long getLastScrollElapse() {
		testReset();

		if (!hasLastScrollElapse()) {
			assert (-1 == lastScrollElapse);
			throw new IllegalStateException(
					"have not scrolled for once, so can't get last scroll elapse. ");
		}

		return lastScrollElapse;
	}

	public long getCurScrollElapse() {
		testReset();

		if (!isScrolling()) {
			assert (curScrollElapse == -1);
			throw new IllegalStateException("the bar is not scrolling now.");
		}

		assert (curScrollElapse >= 0);
		return curScrollElapse;
	}

	public int getBlockWidth() {
		return blockWidth;
	}

	public int getBlockHeight() {
		return blockHeight;
	}

	public boolean isFirstIn() {
		return firstIn;
	}

	// ////////////////////////////////////////////////////////////

	public int[][] getMatrix() {
		testReset();

		if (null == matrixCopy || matrixCopy.length != matrix.length
				|| matrixCopy[0].length != matrix[0].length) {
			matrixCopy = new int[matrix.length][matrix[0].length];
		}

		for (int i = 0; i < matrixCopy.length; i++) {
			for (int j = 0; j < matrixCopy[i].length; j++) {
				matrixCopy[i][j] = matrix[i][j];
			}
		}

		return matrixCopy;
	}

	public Point[][] getBarIdx() {
		if (null == barIdxCopy || barIdxCopy.length != barIdx.length
				|| barIdxCopy[0].length != barIdx[0].length) {
			barIdxCopy = new Point[barIdx.length][barIdx[0].length];
			for (int i = 0; i < barIdxCopy.length; i++) {
				for (int j = 0; j < barIdxCopy[i].length; j++) {
					barIdxCopy[i][j] = new Point();
				}
			}
		}

		for (int i = 0; i < barIdxCopy.length; i++) {
			for (int j = 0; j < barIdxCopy[i].length; j++) {
				if (null == barIdxCopy[i][j]) {
					barIdxCopy[i][j] = new Point();
				}

				barIdxCopy[i][j].x = barIdx[i][j].x;
				barIdxCopy[i][j].y = barIdx[i][j].y;
			}
		}

		return barIdxCopy;
	}

	public int[][][] getBarTypes() {
		if (null == barTypesCopy || barTypesCopy.length != barTypes.length
				|| barTypesCopy[0].length != barTypes[0].length
				|| barTypesCopy[0][0].length != barTypes[0][0].length) {
			barTypesCopy = new int[barTypes.length][barTypes[0].length][barTypes[0][0].length];
		}

		for (int i = 0; i < barTypesCopy.length; i++) {
			for (int j = 0; j < barTypesCopy[i].length; j++) {
				for (int k = 0; k < barTypesCopy[i][j].length; k++) {
					barTypesCopy[i][j][k] = barTypes[i][j][k];
				}
			}
		}

		return barTypesCopy;
	}

	private int[][] bufMatrix = null;

	// 计算指定的bar落地后能够消去的砖块的组数
	public int calScore(int[][] matrix, Point[][] barIdx, int[][] barType) {
		testReset();

		if (null == matrix) {
			throw new NullPointerException("matrix should not be null");
		}
		if (null == barIdx) {
			throw new NullPointerException("barIdx should not be null");
		}
		if (null == barType) {
			throw new NullPointerException("barType should not be null");
		}
		if (matrix.length != this.matrix.length) {
			throw new IllegalArgumentException("matrix illegal scale");
		}
		if (matrix[0].length != this.matrix[0].length) {
			throw new IllegalArgumentException("matrix illegal scale");
		}
		if (barIdx.length != this.barIdx.length) {
			throw new IllegalArgumentException("barIdx illegal scale");
		}
		if (barIdx[0].length != this.barIdx[0].length) {
			throw new IllegalArgumentException("barIdx illegal scale");
		}
		if (barType.length != this.barTypes[0].length) {
			throw new IllegalArgumentException("barType illegal scale");
		}
		if (barType[0].length != this.barTypes[0][0].length) {
			throw new IllegalArgumentException("barType illegal scale");
		}

		// 初始化bufMatrix
		if (null == bufMatrix || bufMatrix.length != matrix.length
				|| bufMatrix[0].length != matrix[0].length) {
			bufMatrix = new int[matrix.length][matrix[0].length];
		}
		for (int i = 0; i < bufMatrix.length; i++) {
			for (int j = 0; j < bufMatrix[i].length; j++) {
				bufMatrix[i][j] = matrix[i][j];
			}
		}

		// 计算bar落下后的bufMatrix的状态
		for (int i = 0; i < barIdx[0].length; i++) {
			int column = barIdx[0][i].y;
			int barRow = barType.length - 1;
			for (int row = matrix.length - 1; row >= 0; row--) {
				if (matrix[row][column] >= 0) {
					continue;
				} else {
					bufMatrix[row][column] = barType[barRow][i];

					barRow--;
					if (barRow < 0) {
						break;
					}
				}
			}
		}

		// 计算能够消去的方块的组数
		int removeCount = 0;
		for (int i = 0; i < bufMatrix.length; i++) {
			for (int j = 0; j < bufMatrix[i].length; j++) {
				if (-1 == bufMatrix[i][j]) {
					continue;
				}

				removeCount += checkHerizontal(i, j, bufMatrix) ? 1 : 0;
				removeCount += checkVertical(i, j, bufMatrix) ? 1 : 0;
				removeCount += checkLeftUpToRightDownDiagonal(i, j, bufMatrix) ? 1
						: 0;
				removeCount += checkLeftDownToRightUpDiagonal(i, j, bufMatrix) ? 1
						: 0;
			}
		}

		return removeCount;

	}

	private boolean checkIndexValid(final int i, final int j,
			final int[][] matrix) {
		if (i >= 0 && i < matrix.length) {
			if (j >= 0 && j < matrix[0].length) {
				return true;
			}
		}

		return false;
	}

	private boolean checkHerizontal(final int i, final int j,
			final int[][] matrix) {
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i - 1, j, matrix)
				|| !checkIndexValid(i + 1, j, matrix)) {
			return false;
		}

		final int left = matrix[i - 1][j];
		final int mid = matrix[i][j];
		final int right = matrix[i + 1][j];

		if (left != -1 && mid != -1 && right != -1) {
			if (left == mid && mid == right) {
				return true;
			}
		}

		return false;

	}

	private boolean checkVertical(final int i, final int j, final int[][] matrix) {
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i, j - 1, matrix)
				|| !checkIndexValid(i, j + 1, matrix)) {
			return false;
		}

		final int up = matrix[i][j - 1];
		final int mid = matrix[i][j];
		final int down = matrix[i][j + 1];

		if (up != -1 && mid != -1 && down != -1) {
			if (up == mid && mid == down) {
				return true;
			}
		}

		return false;
	}

	private boolean checkLeftUpToRightDownDiagonal(final int i, final int j,
			final int[][] matrix) {
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i - 1, j - 1, matrix)
				|| !checkIndexValid(i + 1, j + 1, matrix)) {
			return false;
		}

		final int leftUp = matrix[i - 1][j - 1];
		final int rightDown = matrix[i + 1][j + 1];
		final int mid = matrix[i][j];

		if (leftUp != -1 && mid != -1 && rightDown != -1) {
			if (leftUp == mid && mid == rightDown) {
				return true;
			}
		}

		return false;
	}

	private boolean checkLeftDownToRightUpDiagonal(final int i, final int j,
			final int[][] matrix) {
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i + 1, j - 1, matrix)
				|| !checkIndexValid(i - 1, j + 1, matrix)) {
			return false;
		}

		final int rightUp = matrix[i + 1][j - 1];
		final int leftDown = matrix[i - 1][j + 1];
		final int mid = matrix[i][j];

		if (rightUp != -1 && mid != -1 && leftDown != -1) {
			if (rightUp == mid && mid == leftDown) {
				return true;
			}
		}

		return false;
	}
}
