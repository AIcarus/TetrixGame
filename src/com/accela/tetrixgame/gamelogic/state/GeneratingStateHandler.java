package com.accela.tetrixgame.gamelogic.state;

import com.accela.tetrixgame.gamelogic.basic.Bar;
import com.accela.tetrixgame.gamelogic.basic.Block;
import com.accela.tetrixgame.gamelogic.basic.MovingAttributes;
import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.gameData.GameData;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.gamelogic.uisupport.SoundManager;

/**
 * 
 * 游戏每一回合开始的时候，生成方块的时候，使用这个状态处理器。 这个状态处理器生成随机方块。
 * 
 */
public class GeneratingStateHandler extends StateHandler
{
	private boolean firstTime = true;

	public GeneratingStateHandler()
	{
		super(State.GENERATING);
	}

	@Override
	protected State handleStateImpl(HerizontalStatus hStatus,
			VerticalStatus vStatus, ScrollStatus sStatus, GameData gameData,
			long interpolation)
	{
		assert (gameData.getBlockContainer().getMatrix().isAllAtRightIndex());
		assert (gameData.getBlockContainer().getBar() == null);

		// 生成block的type矩阵，用来决定block的颜色
		int[][] types = new int[gameData.getBarRowNum()][gameData.getBarColumnNum()];
		for (int i = 0; i < types.length; i++)
		{
			for (int j = 0; j < types[i].length; j++)
			{
				types[i][j] = gameData.getRandom().nextInt(gameData.getBlockTypeNum());
			}
		}

		// 计算bar的初始位置
		Point pos = Point.create(gameData.getColumnNum() / 2
				* gameData.getBlockWidth(), 0);

		// 生成移动参数
		MovingAttributes attr = gameData.getMovingAttributes();

		// 生成bar
		Bar bar = new Bar(pos.x, pos.y, gameData.getBlockWidth(),
				gameData.getBlockHeight(), gameData.getBarRowNum(),
				gameData.getBarColumnNum(), types, gameData.getScrollMaxVel(),
				attr);

		Point.dispose(pos);

		// 检查bar是否和BlockContainer中已有的block重合
		boolean overlap = false;
		for (Block b : gameData.getBlockContainer().getMatrix())
		{
			assert (b != null);

			for (int i_inner = 0; i_inner < bar.getColumnNum(); i_inner++)
			{
				if (bar.getBlock(bar.getRowNum() - 1, i_inner).isOverlap(b))
				{
					overlap = true;
					break;
				}
			}
		}

		if (firstTime)
		{
			if (gameData.isEnableSound())
			{
				SoundManager.playSound(SoundManager.START);
			}

			firstTime = false;
		}

		// 结束并转移状态
		if (overlap)
		{
			return State.GAME_OVER_REMOVING;// removes block here
		} else
		{
			gameData.getBlockContainer().setBar(bar);

			return State.BAR_DROPPING;

		}

	}

}
