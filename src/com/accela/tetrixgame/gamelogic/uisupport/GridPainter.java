package com.accela.tetrixgame.gamelogic.uisupport;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;

import com.accela.tetrixgame.gamelogic.gameData.GameData;

/**
 * 
 * 绘制游戏区域的边界
 * 
 */
public class GridPainter extends Painter
{
	/**
	 * 绘制游戏区域的边界
	 */
	@Override
	protected void paintImpl(Graphics g, Point orign, ImageObserver observer,
			GameData gameData, double scale)
	{
		final int GRID_WIDTH = gameData.getGridWidth();

		g.setColor(Color.WHITE);

		g.drawRect(orign.x,
				orign.y,
				(int) ((gameData.getColumnNum() * gameData.getBlockWidth() + 2 * GRID_WIDTH) * scale),
				(int) ((gameData.getRowNum() * gameData.getBlockHeight() + 2 * GRID_WIDTH) * scale));
		g.drawRect(orign.x + (int) (GRID_WIDTH * scale),
				orign.y + (int) (GRID_WIDTH * scale),
				(int) (gameData.getColumnNum() * gameData.getBlockWidth() * scale),
				(int) (gameData.getRowNum() * gameData.getBlockHeight() * scale));

	}

}
