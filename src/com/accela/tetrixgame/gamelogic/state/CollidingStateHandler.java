package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.basic.Block;
import com.accela.tetrixgame.gamelogic.container.BlockMatrix;
import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;

/**
 * 
 * 负责计算哪些方块需要移除
 * 
 */
public class CollidingStateHandler extends StateHandler
{
	public CollidingStateHandler()
	{
		super(State.COLLIDING);
	}

	@Override
	protected State handleStateImpl(HerizontalStatus hstatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation)
	{
		// 检查toBeRemoved状态和是否所有block都落在网格上
		BlockMatrix matrix = gameData.getBlockContainer().getMatrix();
		assert (matrix != null);

		for (int i = 0; i < matrix.getRowNum(); i++)
		{
			for (int j = 0; j < matrix.getColumnNum(); j++)
			{
				assert (!matrix.isToBeRemoved(i, j));

				if (matrix.get(i, j) != null)
				{
					assert (gameData.getBlockContainer().getGrid().isOnGridCross(matrix.get(i,
							j)));
				}
			}
		}

		// 重排BockMatrix中所有的block
		matrix.updateAll();

		// 开始计算哪些方块应该消去
		boolean hasBlockToBeRemoved = false;
		for (int i = 0; i < matrix.getRowNum(); i++)
		{
			for (int j = 0; j < matrix.getColumnNum(); j++)
			{
				if (null == matrix.get(i, j))
				{
					continue;
				}

				checkHerizontal(i, j, matrix);
				checkVertical(i, j, matrix);
				checkLeftUpToRightDownDiagonal(i, j, matrix);
				checkLeftDownToRightUpDiagonal(i, j, matrix);

				if (matrix.isToBeRemoved(i, j))
				{
					hasBlockToBeRemoved = true;
				}

			}
		}

		// 刷新分数
		int removedNum = 0;
		for (int i = 0; i < matrix.getRowNum(); i++)
		{
			for (int j = 0; j < matrix.getColumnNum(); j++)
			{
				if (null == matrix.get(i, j))
				{
					continue;
				}

				if (matrix.isToBeRemoved(i, j))
				{
					removedNum++;
				}

			}
		}
		gameData.getScoreRecorder().addScore(removedNum);
		if (removedNum > 3 && gameData.isEnableSound())
		{
			// 播放称赞的语音
			SoundManager.playApplause();
		}

		// 准备返回的状态
		if (hasBlockToBeRemoved || hasFloatingBlocks(matrix))
		{
			return State.REMOVING_AND_FALLING;
		} else
		{
			return State.GENERATING;
		}

	}

	private boolean hasFloatingBlocks(BlockMatrix matrix)
	{
		assert (matrix != null);

		for (int j = 0; j < matrix.getColumnNum(); j++)
		{
			boolean findEmptyGrid = false;
			for (int i = matrix.getRowNum() - 1; i >= 0; i--)
			{
				if (!findEmptyGrid)
				{
					if (matrix.get(i, j) == null)
					{
						findEmptyGrid = true;
					}
				} else
				{
					if (matrix.get(i, j) != null)
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean checkIndexValid(final int i, final int j,
			final BlockMatrix matrix)
	{
		if (i >= 0 && i < matrix.getRowNum())
		{
			if (j >= 0 && j < matrix.getColumnNum())
			{
				return true;
			}
		}

		return false;
	}

	private void checkHerizontal(final int i, final int j,
			final BlockMatrix matrix)
	{
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i - 1, j, matrix)
				|| !checkIndexValid(i + 1, j, matrix))
		{
			return;
		}

		final Block left = matrix.get(i - 1, j);
		final Block mid = matrix.get(i, j);
		final Block right = matrix.get(i + 1, j);

		if (left != null && mid != null && right != null)
		{
			if (left.isOfSameType(mid) && mid.isOfSameType(right))
			{
				matrix.setToBeRemoved(i - 1, j, true);
				matrix.setToBeRemoved(i, j, true);
				matrix.setToBeRemoved(i + 1, j, true);
			}
		}

	}

	private void checkVertical(final int i, final int j,
			final BlockMatrix matrix)
	{
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i, j - 1, matrix)
				|| !checkIndexValid(i, j + 1, matrix))
		{
			return;
		}

		final Block up = matrix.get(i, j - 1);
		final Block mid = matrix.get(i, j);
		final Block down = matrix.get(i, j + 1);

		if (up != null && mid != null && down != null)
		{
			if (up.isOfSameType(mid) && mid.isOfSameType(down))
			{
				matrix.setToBeRemoved(i, j - 1, true);
				matrix.setToBeRemoved(i, j, true);
				matrix.setToBeRemoved(i, j + 1, true);
			}
		}
	}

	private void checkLeftUpToRightDownDiagonal(final int i, final int j,
			final BlockMatrix matrix)
	{
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i - 1, j - 1, matrix)
				|| !checkIndexValid(i + 1, j + 1, matrix))
		{
			return;
		}

		final Block leftUp = matrix.get(i - 1, j - 1);
		final Block rightDown = matrix.get(i + 1, j + 1);
		final Block mid = matrix.get(i, j);

		if (leftUp != null && mid != null && rightDown != null)
		{
			if (leftUp.isOfSameType(mid) && mid.isOfSameType(rightDown))
			{
				matrix.setToBeRemoved(i - 1, j - 1, true);
				matrix.setToBeRemoved(i, j, true);
				matrix.setToBeRemoved(i + 1, j + 1, true);
			}
		}
	}

	private void checkLeftDownToRightUpDiagonal(final int i, final int j,
			final BlockMatrix matrix)
	{
		if (!checkIndexValid(i, j, matrix)
				|| !checkIndexValid(i + 1, j - 1, matrix)
				|| !checkIndexValid(i - 1, j + 1, matrix))
		{
			return;
		}

		final Block rightUp = matrix.get(i + 1, j - 1);
		final Block leftDown = matrix.get(i - 1, j + 1);
		final Block mid = matrix.get(i, j);

		if (rightUp != null && mid != null && leftDown != null)
		{
			if (rightUp.isOfSameType(mid) && mid.isOfSameType(leftDown))
			{
				matrix.setToBeRemoved(i + 1, j - 1, true);
				matrix.setToBeRemoved(i, j, true);
				matrix.setToBeRemoved(i - 1, j + 1, true);
			}
		}
	}

}
