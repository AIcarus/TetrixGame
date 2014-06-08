package com.accela.tetrixgame.gamelogic.geometry;

/**
 * 
 * 表示一个向x轴正方向和y轴正方向无限延伸 的网格。网格的起点在（0，0）。每个网格 都有一个行数和列数，它们都是从零开始的。
 * 
 */
public class Grid {
	/**
	 * 每个网格的宽度
	 */
	private int gridWidth;
	/**
	 * 每个网格的高度
	 */
	private int gridHeight;

	/**
	 * 新建一个网格对象
	 * 
	 * @param gridWidth
	 *            网格宽度
	 * @param gridHeight
	 *            网格高度
	 */
	public Grid(int gridWidth, int gridHeight) {
		if (gridWidth <= 0) {
			throw new IllegalArgumentException("gridWidth shoud not be null");
		}
		if (gridHeight <= 0) {
			throw new IllegalArgumentException("gridHeight shoud not be null");
		}

		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	/**
	 * 用Point返回指定的点p在网格的哪一行哪一列上
	 */
	public void atWhichRowColumn(Point result, Point p) {
		if (null == p) {
			throw new NullPointerException("p should not be null");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.reset(p.y / gridHeight, p.x / gridWidth);
	}

	/**
	 * 用Point返回指定的矩形的左上角点在网格的哪一行哪一列上
	 */
	public void atWhichRowColumn(Point result, Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		Point leftUp = Point.create();
		rect.getLeftUpPoint(leftUp);

		atWhichRowColumn(result, leftUp);

		Point.dispose(leftUp);
	}

	/**
	 * 给定的点是否在网格的网格线上
	 * 
	 * @param x
	 *            给定的点的横坐标
	 * @param y
	 *            给定的点的纵坐标
	 * @return 给定的点是否在网格线上
	 */
	public boolean isOnGridLine(int x, int y) {
		if (x < 0) {
			throw new IllegalArgumentException("x should be nonnegtive");
		}
		if (y < 0) {
			throw new IllegalArgumentException("y should be nonnegtive");
		}

		return x % gridWidth == 0 || y % gridHeight == 0;
	}

	/**
	 * 给定的点是否在网格线上
	 * 
	 * @param point
	 *            给定的点
	 * @return 给定的点是否在网格线上
	 */
	public boolean isOnGridLine(Point point) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}

		return point.x % gridWidth == 0 || point.y % gridHeight == 0;
	}

	/**
	 * 给定的点（x,y）是否在网格线的交点上
	 */
	public boolean isOnGridCross(Point point) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}

		return point.x % gridWidth == 0 && point.y % gridHeight == 0;
	}

	/**
	 * 给定的矩形是否在网格线的交点上
	 */
	public boolean isOnGridCross(Rectangle rect) {
		if (null == rect) {
			throw new IllegalArgumentException("rect should not be null");
		}

		Point temp = Point.create();

		rect.getLeftUpPoint(temp);
		if (!isOnGridCross(temp)) {
			return false;
		}

		rect.getLeftDownPoint(temp);
		if (!isOnGridCross(temp)) {
			return false;
		}

		rect.getRightUpPoint(temp);
		if (!isOnGridCross(temp)) {
			return false;
		}

		rect.getRightDownPoint(temp);
		if (!isOnGridCross(temp)) {
			return false;
		}

		Point.dispose(temp);

		return true;
	}

	/**
	 * 检查给定的点是否在竖直的网格线上
	 */
	public boolean isOnVerticalGridLine(Point point) {
		if (null == point) {
			throw new NullPointerException("point should not be null");
		}

		return point.x % gridWidth == 0;
	}

	/**
	 * 检查给定的矩形的两条竖直边是否都在网格的竖直网格线上
	 */
	public boolean isOnVerticalGridLine(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		Point temp = Point.create();

		rect.getLeftUpPoint(temp);
		if (!isOnVerticalGridLine(temp)) {
			return false;
		}

		rect.getRightDownPoint(temp);
		if (!isOnVerticalGridLine(temp)) {
			return false;
		}

		Point.dispose(temp);

		return true;
	}

	/**
	 * 检查给定的点的两条水平边是否都在网格的水平网格线上
	 */
	public boolean isOnHerizontalGridLine(Point point) {
		if (null == point) {
			throw new NullPointerException("point should not be null");
		}

		return point.y % gridHeight == 0;
	}

	/**
	 * 检查给定的矩形的两条水平边是否都在网格的水平网格线上
	 */
	public boolean isOnHerizontalGridLine(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		Point temp = Point.create();

		rect.getLeftUpPoint(temp);
		if (!isOnHerizontalGridLine(temp)) {
			return false;
		}

		rect.getLeftDownPoint(temp);
		if (!isOnHerizontalGridLine(temp)) {
			return false;
		}

		Point.dispose(temp);

		return true;
	}

	/**
	 * 返回一个点在它所在的网格中，相对于该网格的左上角的横坐标
	 */
	public int xCoordinateInGrid(Point point) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}

		return point.x % gridWidth;
	}

	/**
	 * 返回一个点在它所在的网格中，相对于该网格的左上角的纵坐标
	 */
	public int yCoordinateInGrid(Point point) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}

		return point.y % gridHeight;
	}

	/**
	 * 检查给定的两个点是否在网格的同一行
	 */
	public boolean isAtSameRow(Point point1, Point point2) {
		if (null == point1) {
			throw new IllegalArgumentException("point1 should not be null");
		}
		if (null == point2) {
			throw new IllegalArgumentException("point2 should not be null");
		}

		return point1.y / gridHeight == point2.y / gridHeight;
	}

	/**
	 * 检查给定的两个点是否在网格的同一列
	 */
	public boolean isAtSameColumn(Point point1, Point point2) {
		if (null == point1) {
			throw new IllegalArgumentException("point1 should not be null");
		}
		if (null == point2) {
			throw new IllegalArgumentException("point2 should not be null");
		}

		return point1.x / gridWidth == point2.x / gridWidth;
	}

	/**
	 * 检查给定的两个点是否在网格的同一格
	 */
	public boolean isInSameGrid(Point point1, Point point2) {
		if (null == point1) {
			throw new IllegalArgumentException("point1 should not be null");
		}
		if (null == point2) {
			throw new IllegalArgumentException("point2 should not be null");
		}

		return isAtSameRow(point1, point2) && isAtSameColumn(point1, point2);
	}
}
