package com.accela.tetrixgame.gamelogic.geometry;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.util.ObjectPool;

/**
 * 
 * 矩形。唯一确定一个矩形的方法统一使用矩形的左上角的点， 和矩形的宽度和高度。 在这个类的所有方法中均使用这个方式。
 * 
 */
public class Rectangle {
	/**
	 * 矩形左上角的点的横坐标
	 */
	public int x;
	/**
	 * 矩形左上角的点的纵坐标
	 */
	public int y;
	/**
	 * 矩形的宽度
	 */
	private int width;
	/**
	 * 矩形的高度
	 */
	private int height;

	/**
	 * 使用指定的左上角的点的坐标和指定的宽度和高度构造矩形。
	 */
	protected Rectangle(int x, int y, int width, int height) {
		if (width < 0) {
			throw new IllegalArgumentException("width should be nonnegtive");
		}
		if (height < 0) {
			throw new IllegalArgumentException("height should be nonnegtive");
		}

		construct(x, y, width, height);
	}

	private void construct(int x, int y, int width, int height) {
		if (width < 0) {
			throw new IllegalArgumentException("width should be nonnegtive");
		}
		if (height < 0) {
			throw new IllegalArgumentException("height should be nonnegtive");
		}

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public void getPoint(Point result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.reset(x, y);
	}

	/**
	 * 返回矩形的左上角的点的坐标
	 */
	public void getLeftUpPoint(Point result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.reset(x, y);
	}

	/**
	 * 返回矩形的右上角的点的坐标
	 */
	public void getRightUpPoint(Point result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.reset(x + width, y);
	}

	/**
	 * 返回矩形的左下角的点的坐标
	 */
	public void getLeftDownPoint(Point result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.reset(x, y + height);
	}

	/**
	 * 返回矩形的右下角的点的坐标
	 */
	public void getRightDownPoint(Point result) {
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		result.reset(x + width, y + height);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * 测试如果把矩形移动到点(x,y)，矩形是否还在网格 grid的同一个格子中
	 */
	public boolean isInSameGridIfMoveTo(Point point, Grid grid) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}
		if (null == grid) {
			throw new NullPointerException("grid should not be null");
		}

		Point old = Point.create(x, y);
		boolean ret = grid.isInSameGrid(old, point);
		Point.dispose(old);

		return ret;
	}

	/**
	 * 测试如果把矩形移动偏移量(xOffset, yOffset)，矩形是否还在网格 grid的同一个格子中
	 */
	public boolean isInSameGridIfMoveBy(int xOffset, int yOffset, Grid grid) {
		if (null == grid) {
			throw new NullPointerException("grid should not be null");
		}

		Point newPos = Point.create(x + xOffset, y + yOffset);
		boolean ret = isInSameGridIfMoveTo(newPos, grid);
		Point.dispose(newPos);

		return ret;
	}

	/**
	 * 测试点point是否包含在矩形中
	 */
	public boolean contains(Point point) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}

		if (point.x > x && point.x < x + width) {
			if (point.y > y && point.y < y + height) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 测试这个矩形与指定的矩形是否重合
	 */
	public boolean isOverlap(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		if (rect.x == x && rect.y == y) {
			return true;
		}

		Point temp = Point.create();

		rect.getLeftUpPoint(temp);
		if (contains(temp)) {
			return true;
		}

		rect.getLeftDownPoint(temp);
		if (contains(temp)) {
			return true;
		}

		rect.getRightUpPoint(temp);
		if (contains(temp)) {
			return true;
		}

		rect.getRightDownPoint(temp);
		if (contains(temp)) {
			return true;
		}

		this.getLeftUpPoint(temp);
		if (rect.contains(temp)) {
			return true;
		}

		this.getLeftDownPoint(temp);
		if (rect.contains(temp)) {
			return true;
		}

		this.getRightUpPoint(temp);
		if (rect.contains(temp)) {
			return true;
		}

		this.getRightDownPoint(temp);
		if (rect.contains(temp)) {
			return true;
		}

		Point.dispose(temp);
		return false;
	}

	/**
	 * 将rectB的宽度向左右无线延伸，测试rectA是否和此时的B相交
	 */
	public static boolean isAOverlapWithHerizontalExtensionOfB(Rectangle rectA,
			Rectangle rectB) {
		if (null == rectA) {
			throw new NullPointerException("rectA should not be null");
		}
		if (null == rectB) {
			throw new NullPointerException("rectA should not be null");
		}

		final int upBoundA = rectA.y;
		final int downBoundA = rectA.y + rectA.height;

		final int upBoundB = rectB.y;
		final int downBoundB = rectB.y + rectB.height;

		if (downBoundA <= upBoundB || upBoundA >= downBoundB) {
			return false;
		}

		return true;
	}

	/**
	 * 将rectB的高度向上下无线延伸，测试rectA是否和此时的B相交
	 */
	public static boolean isAOverlapWithVerticalExtensionOfB(Rectangle rectA,
			Rectangle rectB) {
		if (null == rectA) {
			throw new NullPointerException("rectA should not be null");
		}
		if (null == rectB) {
			throw new NullPointerException("rectA should not be null");
		}

		final int leftBoundA = rectA.x;
		final int rightBoundA = rectA.x + rectA.width;

		final int leftBoundB = rectB.x;
		final int rightBoundB = rectB.x + rectB.width;

		if (rightBoundA <= leftBoundB || leftBoundA >= rightBoundB) {
			return false;
		}

		return true;
	}

	/**
	 * 测试这个矩形是否在给定的矩形的上方
	 */
	public boolean isAbove(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		return y + height <= rect.y;
	}

	/**
	 * 计算这个方块在给定的方块的上方多少距离
	 */
	public int aboveByHowMuch(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		return rect.y - (y + height);
	}

	/**
	 * 将这个方块移动到给定点
	 */
	public void moveTo(Point point) {
		if (null == point) {
			throw new NullPointerException("point should not be null");
		}

		this.x = point.x;
		this.y = point.y;
	}

	/**
	 * 将这个方块移动给定的偏移量
	 */
	public void moveBy(int xOffset, int yOffset) {
		this.x += xOffset;
		this.y += yOffset;
	}

	/**
	 * 测试这个矩形的宽度和高度是否分别是给定的网格 的宽度和高度的整数倍。
	 */
	public boolean isProportionalToGrid(Grid grid) {
		if (null == grid) {
			throw new NullPointerException("grid should not be null");
		}

		if (width % grid.getGridWidth() == 0
				&& height % grid.getGridHeight() == 0 && width > 0
				&& height > 0) {
			return true;
		} else {
			return false;
		}

	}

	public boolean isLeftOf(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		return x + width <= rect.x;
	}

	public boolean isRightOf(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		return x >= rect.x + rect.width;
	}

	// ////////////////////////////////////////////////////////////////////////

	private static Rectangle retrieve() {
		Rectangle r = ObjectPool.retrieve(Rectangle.class);
		if (null == r) {
			r = new Rectangle(0, 0, 1, 1);
		}

		return r;
	}

	/**
	 * 使用指定的左上角的点的坐标和指定的宽度和高度构造矩形。
	 */
	public static Rectangle create(int x, int y, int width, int height) {
		if (width < 0) {
			throw new IllegalArgumentException("width should be nonnegtive");
		}
		if (height < 0) {
			throw new IllegalArgumentException("height should be nonnegtive");
		}

		Rectangle r = retrieve();

		r.construct(x, y, width, height);

		return r;
	}

	/**
	 * 使用指定的左上角的点的坐标和指定的宽度和高度构造矩形。
	 */
	public static Rectangle create(Point point, int width, int height) {
		if (null == point) {
			throw new IllegalArgumentException("point should not be null");
		}
		if (width < 0) {
			throw new IllegalArgumentException("width should be nonnegtive");
		}
		if (height < 0) {
			throw new IllegalArgumentException("height should be nonnegtive");
		}

		return create(point.x, point.y, width, height);
	}

	/**
	 * 复制一个矩形
	 */
	public static Rectangle create(Rectangle rect) {
		if (null == rect) {
			throw new NullPointerException("rect should not be null");
		}

		return create(rect.x, rect.y, rect.width, rect.height);
	}

	public static void dispose(Rectangle r) {
		ObjectPool.put(r);
	}
}
