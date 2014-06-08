package com.accela.tetrixgame.gamelogic.container;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.accela.tetrixgame.gamelogic.basic.Block;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Point;

/**
 * 
 * 装载和记录方块，以及记录方块在网格中的位置的矩阵。
 * 
 */
public class BlockMatrix implements Collection<Block> {

	private Block[][] matrix;

	private Set<Block> blockSet = new HashSet<Block>(); // 用来记录matrix中所有的block，以优化迭代器

	private Block[][] backupMatrix; // 用来优化updateAll方法

	private boolean[][] removeMatrix;

	private Grid grid;

	private int size = 0;

	public BlockMatrix(int rowNum, int columnNum, Grid grid) {
		if (rowNum <= 0 || columnNum <= 0) {
			throw new IllegalArgumentException("illegal rowNum or colunmNum");
		}
		if (null == grid) {
			throw new NullPointerException("grid should not be null");
		}

		matrix = new Block[rowNum][columnNum];
		assert (checkEmpty(matrix));
		backupMatrix = new Block[rowNum][columnNum];
		assert (checkEmpty(backupMatrix));
		removeMatrix = new boolean[rowNum][columnNum];
		clearAllToBeRemoved();
		this.grid = grid;
	}

	public int getRowNum() {
		return matrix.length;
	}

	public int getColumnNum() {
		return matrix[0].length;
	}

	private void checkIdx(int i, int j) {
		if (!(i < matrix.length && j < matrix[i].length)) {
			throw new IllegalArgumentException("illegal index: i = " + i
					+ ", j = " + j);
		}
	}

	private boolean checkEmpty(Object[][] matrix) {
		if (null == matrix) {
			assert (false);
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != null) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 方块移动和，其实际位置可能和记录的网格位置不符，因此使用这个方法， 重新计算。
	 */
	public void updateAll() {
		assert (checkNoOverlap());

		assert (checkEmpty(backupMatrix));

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != null) {
					Point truePos = Point.create();
					grid.atWhichRowColumn(truePos, matrix[i][j]);
					assert (backupMatrix[truePos.x][truePos.y] == null);
					backupMatrix[truePos.x][truePos.y] = matrix[i][j];
					matrix[i][j] = null;

					Point.dispose(truePos);
				}
			}
		}

		Block[][] temp = backupMatrix;
		backupMatrix = matrix;
		matrix = temp;

		assert (checkEmpty(backupMatrix));
		assert (isAllAtRightIndex());
		assert (checkSize());
	}

	/**
	 * 检查是否所有方块的实际位置都和它们的网格位置相符
	 */
	public boolean isAllAtRightIndex() {
		for (Block b : this) {
			assert (b != null);

			if (!isAtRightIndex(b)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 检查指定方块的实际位置都和它们的网格位置相符
	 */
	public boolean isAtRightIndex(Block b) {
		if (null == b) {
			throw new NullPointerException("b should not be null");
		}

		Point pos = Point.create();
		if (indexOf(pos, b)) {
			Point truePos = Point.create();
			grid.atWhichRowColumn(truePos, b);
			if (truePos.x == pos.x && truePos.y == pos.y) {
				Point.dispose(truePos);
				Point.dispose(pos);
				return true;
			} else {
				Point.dispose(truePos);
				Point.dispose(pos);
				return false;
			}
		} else {
			Point.dispose(pos);
			throw new IllegalArgumentException("b is not contained");
		}
	}

	/**
	 * 检查是否有两个方块重叠
	 */
	private boolean checkNoOverlap() {
		for (Block outer : this) {
			for (Block inner : this) {
				assert (outer != null);
				assert (inner != null);

				if (outer.equals(inner)) {
					continue;
				}

				if (outer.isOverlap(inner) || inner.isOverlap(outer)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * 检查BlockMatrix的size成员变量是否和实际的size相符
	 */
	private boolean checkSize() {
		int trueSize = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != null) {
					trueSize++;
				}
			}
		}

		return (size == trueSize) && (size == blockSet.size());
	}

	public Block get(int i, int j) {
		checkIdx(i, j);

		return matrix[i][j];
	}

	/**
	 * 返回(i, j)位置的砖块是否应该被消去
	 */
	public boolean isToBeRemoved(int i, int j) {
		checkIdx(i, j);

		return removeMatrix[i][j];
	}

	/**
	 * 将(i, j)位置的砖块设为应该被消去
	 */
	public void setToBeRemoved(int i, int j, boolean toBeRemoved) {
		checkIdx(i, j);

		removeMatrix[i][j] = toBeRemoved;
	}

	/**
	 * 将所有砖块都设为不应该被消去
	 */
	public void clearAllToBeRemoved() {
		assert (removeMatrix != null);

		for (int i = 0; i < removeMatrix.length; i++) {
			for (int j = 0; j < removeMatrix[i].length; j++) {
				removeMatrix[i][j] = false;
			}
		}

	}

	@Override
	public boolean add(Block b) {
		if (null == b) {
			throw new NullPointerException("b should not be null");
		}
		if (!b.isProportionalToGrid(grid)) {
			throw new IllegalArgumentException(
					"b should be propotional to the grid");
		}
		if (contains(b)) {
			throw new IllegalArgumentException("b is alread contained");
		}
		for (Block element : this) {
			assert (element != null);

			if (element.isOverlap(b)) {
				throw new IllegalArgumentException(
						"b is overlaped with an existing block");
			}
		}

		Point pos = Point.create();
		grid.atWhichRowColumn(pos, b);
		assert (pos != null);
		assert (null == matrix[pos.x][pos.y]);
		matrix[pos.x][pos.y] = b;
		blockSet.add(b);

		size++;
		assert (checkSize());
		Point.dispose(pos);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Block> c) {
		if (null == c) {
			throw new NullPointerException("c should not be null");
		}

		boolean result = false;
		for (Block b : c) {
			if (b != null) {
				if (add(b)) // 一定要调用add以调整size
				{
					result = true;
				}
			}
		}

		assert (checkSize());
		return result;
	}

	@Override
	public void clear() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				matrix[i][j] = null;
			}
		}

		blockSet.clear();
		size = 0;

		assert (checkSize());
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Block)) {
			return false;
		}

		Block b = (Block) o;

		for (Block element : this) {
			assert (element != null);

			if (element.equals(b)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (null == c) {
			throw new NullPointerException("c should not be null");
		}

		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isEmpty() {
		return 0 == size;
	}

	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {
			private final Iterator<Block> i = blockSet.iterator();

			public boolean hasNext() {
				return i.hasNext();
			}

			public Block next() {
				return i.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean remove(Object o) {
		if (!(o instanceof Block)) {
			return false;
		}

		boolean result = false;
		Block b = (Block) o;
		Point pos = Point.create();
		if (indexOf(pos, b)) {
			assert (matrix[pos.x][pos.y].equals(b));

			boolean hasRemoved = remove(pos.x, pos.y);
			assert (hasRemoved);

			Point.dispose(pos);
			result = true;
		}

		assert (checkSize());
		Point.dispose(pos);
		return result;
	}

	public boolean remove(int i, int j) {
		checkIdx(i, j);
		if (null == matrix[i][j]) {
			return false;
		} else {
			boolean result = blockSet.remove(matrix[i][j]);
			assert (result);

			matrix[i][j] = null; // remove block here
			size--;
			return true;
		}

	}

	public boolean indexOf(Point result, Block b) {
		if (null == b) {
			throw new NullPointerException("b should not be null");
		}
		if (null == result) {
			throw new NullPointerException("result should not be null");
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != null && matrix[i][j].equals(b)) {
					result.reset(i, j);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (null == c) {
			throw new NullPointerException("c should not be null");
		}

		boolean result = false;
		for (Object o : c) {
			if (remove(o)) // 一定要调用remove以调整size
			{
				result = true;
			}
		}

		assert (checkSize());
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (null == c) {
			throw new NullPointerException("c should not be null");
		}

		boolean result = false;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (null == matrix[i][j]) {
					continue;
				}

				if (!c.contains(matrix[i][j])) {
					boolean ret = blockSet.remove(matrix[i][j]);
					assert (ret);

					matrix[i][j] = null;
					size--;
				} else {
					result = true;
				}
			}
		}

		assert (checkSize());
		return result;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {
		List<Object> list = new LinkedList<Object>();
		for (Block b : this) {
			assert (b != null);
			list.add(b);
		}

		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (null == a) {
			throw new NullPointerException("a should not be null");
		}

		List<Object> list = new LinkedList<Object>();
		for (Block b : this) {
			assert (b != null);
			list.add(b);
		}

		return list.toArray(a);
	}

	public Grid getGrid() {
		return grid;
	}

	// /////////////////////////////////////////////////////////////////////

}
