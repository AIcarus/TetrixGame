package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.basic.Block;
import com.accela.tetrixgame.gamelogic.container.BlockMatrix;
import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;

/**
 * 
 * 负责删除应该被消去的方块，释放效果，炸飞方块，以及处理被炸飞的方块 的下落和碰撞。
 * 
 * 实际上，如果Bar的列数大于2，当Bar碰撞后，如果有剩下的半截没有落地， 这个处理器也会负责使它们下落并落地。
 * 
 */
public class RemovingAndFallingStateHandler extends StateHandler
{
	/**
	 * 记录哪些砖块可以移动，哪些不可以。
	 */
	private boolean[][] moveMatrix = null;

	/**
	 * 记录这次处理是不是可以
	 */
	private long removeIntervalCounter = 0;

	private boolean shouldInit = true;

	public RemovingAndFallingStateHandler()
	{
		super(State.REMOVING_AND_FALLING);
	}

	@Override
	protected State handleStateImpl(HerizontalStatus hStatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation)
	{
		if (shouldInit)
		{
			init(gameData);
		}

		// 测试是否达到remove interval，已经等待了足够长的时间，可以消去下一个方块
		boolean allRemoved = false;
		if (removeIntervalCounter < gameData.getRemoveInterval())
		{
			removeIntervalCounter += interpolation;
			allRemoved = false;
		} else
		{
			removeIntervalCounter = 0;
			allRemoved = !selectBlockToRemove(gameData);
		}

		// 处理砖块的自由飞行，这里假设下面的砖块绝对不会撞上上面的砖块；
		// 并且下面的砖块落地后，上面的砖块才可能落地；
		// 并且先处理上面的砖块的运动，在处理下面的砖块的运动，一定不会出现
		// 处理的时候，下面撞上上面的情况
		boolean hasMovable = false;
		for (int i = moveMatrix.length - 1; i >= 0; i--)
		{
			for (int j = 0; j < moveMatrix[i].length; j++)
			{
				if (moveMatrix[i][j])
				{
					Block block = gameData.getBlockContainer().getMatrix().get(i,
							j);
					assert (block != null);

					hasMovable = true;

					boolean collided = block.update(HerizontalStatus.DRIFT,
							VerticalStatus.DOWN_ACC,
							gameData.getBlockContainer().getMatrix(),
							gameData.getBlockContainer().getGrid(),
							gameData.getColumnNum() * gameData.getBlockWidth(),
							gameData.getRowNum() * gameData.getBlockHeight(),
							interpolation);

					if (collided)
					{
						moveMatrix[i][j] = false;
						block.resetMovingProxy();

						// 添加音效
						if (gameData.isEnableSound())
						{
							SoundManager.playSound(SoundManager.BOX_HIT);
						}
					}

				}
			}
		}

		// 准备状态转移
		if (!hasMovable && allRemoved)
		{
			reset();
			return State.COLLIDING;
		} else
		{
			return State.REMOVING_AND_FALLING;
		}
	}

	private boolean selectBlockToRemove(GameData gameData)
	{
		assert (gameData != null);
		BlockMatrix matrix = gameData.getBlockContainer().getMatrix();
		assert (matrix != null);

		// 逐个清除被消去的方块
		for (int i = matrix.getRowNum() - 1; i >= 0; i--)
		{
			for (int j = 0; j < matrix.getColumnNum(); j++)
			{
				if (matrix.isToBeRemoved(i, j))
				{
					performBlockRemoval(i, j, gameData);
					matrix.setToBeRemoved(i, j, false);

					return true;
				}
			}
		}

		return false;
	}

	private void performBlockRemoval(final int i, final int j, GameData gameData)
	{
		BlockMatrix matrix = gameData.getBlockContainer().getMatrix();
		assert (matrix.isToBeRemoved(i, j));

		Rectangle removedRect = matrix.get(i, j);
		assert (removedRect != null);

		// 删除应该被删除的方块
		boolean result = matrix.remove(i, j);
		assert (result);

		// 将block设为不可移动
		moveMatrix[i][j] = false;

		// 将block上方的方块设为可移动
		int counter = 0;
		for (int idx_i = i - 1; idx_i >= 0; idx_i--)
		{
			if (matrix.get(idx_i, j) != null)
			{
				moveMatrix[idx_i][j] = true;
				matrix.get(idx_i, j).switchToGravityVerticalVel(-(gameData.getExplosionVel() + counter
						* gameData.getExplosionAddedVel()));

				counter++;
			}
		}

		// 添加Spark
		for (int idx = 0; idx < gameData.getSparkNumPerBlock(); idx++)
		{
			Point center = null;
			if (gameData.isEnableEffects())
			{
				center = Point.create(removedRect.x
						+ (int) ((removedRect.getWidth() + 1) * Math.random()),
						removedRect.y
								+ (int) ((removedRect.getHeight() + 1) * Math.random()));
			} else
			{

				center = Point.create(removedRect.x + removedRect.getWidth()
						/ 2, removedRect.y + removedRect.getHeight() / 2);
			}

			gameData.getEffectContainer().createSpark(center,
					idx * gameData.getSparkDelayInterval(),
					gameData.getSparkAttributes());

			Point.dispose(center);
		}

		// 添加FireLine
		Point center = Point.create(removedRect.x + removedRect.getWidth() / 2,
				removedRect.y + removedRect.getHeight() / 2);

		if (gameData.isEnableEffects())
		{
			gameData.getEffectContainer().createBallFirework(center,
					gameData.getFireworkAttributes(),
					gameData.getColumnNum() * gameData.getBlockWidth(),
					gameData.getRowNum() * gameData.getBlockHeight());
		}

		// 添加RunningCircle
		if (gameData.isEnableEffects())
		{
			gameData.getEffectContainer().createRunningCircle(center);
		}

		Point.dispose(center);

		// 添加振动shaker
		if (gameData.isEnableEffects())
		{
			gameData.getEffectContainer().setShaker(gameData.getShakeMaxOffset(),
					gameData.getShakePeriod(),
					gameData.getShakeNumPeriod());
		}

		// 添加音效
		if (gameData.isEnableSound())
		{
			SoundManager.playExplode();
		}

	}

	private void init(GameData gameData)
	{
		assert (gameData != null);
		assert (gameData.getBlockContainer().getMatrix().isAllAtRightIndex());

		removeIntervalCounter = gameData.getRemoveInterval();

		moveMatrix = new boolean[gameData.getRowNum()][gameData.getColumnNum()];
		for (int i = 0; i < moveMatrix.length; i++)
		{
			for (int j = 0; j < moveMatrix[i].length; j++)
			{
				moveMatrix[i][j] = false;
			}
		}

		checkFloatingBlocks(gameData.getBlockContainer().getMatrix());

		shouldInit = false;
	}

	// 检查悬空空中的方块，并把它们的moveMatrix相应位置设为true
	private void checkFloatingBlocks(BlockMatrix matrix)
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
						moveMatrix[i][j] = true;
					}
				}
			}
		}
	}

	private void reset()
	{
		moveMatrix = null;
		removeIntervalCounter = 0;
		shouldInit = true;
	}

}
