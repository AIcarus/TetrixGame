package com.accela.tetrixgame.gamelogic.uisupport;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.gamelogic.gameData.GameData;

public abstract class Painter
{
	public void paint(Graphics g, Point orign, ImageObserver observer,
			GameData gameData, double scale)
	{
		if (null == g)
		{
			throw new NullPointerException("g should not be null");
		}
		if (null == orign)
		{
			throw new NullPointerException("orign should not be null");
		}
		if (null == observer)
		{
			throw new NullPointerException("observer should not be null");
		}
		if (null == gameData)
		{
			throw new NullPointerException("gameData should not be null");
		}
		if (scale <= 0)
		{
			throw new IllegalArgumentException("scale should be positive");
		}

		paintImpl(g, orign, observer, gameData, scale);
	}

	protected abstract void paintImpl(Graphics g, Point orign,
			ImageObserver observer, GameData gameData, double scale);

	private static Map<Color, Map<Integer, Color>> colorTable = new HashMap<Color, Map<Integer, Color>>();

	private static ReentrantLock clrTableLock = new ReentrantLock();

	protected static Color getColor(Color clr, int alpha)
	{
		if (null == clr)
		{
			throw new NullPointerException("clr should not be null");
		}
		if (clr.getAlpha() != 255)
		{
			throw new IllegalArgumentException("clr.getAlpha() should be 255");
		}
		if (alpha < 0 || alpha > 255)
		{
			throw new IllegalArgumentException("Illegal alpha: " + alpha);
		}

		clrTableLock.lock();
		try
		{
			Map<Integer, Color> alphaClrMap = colorTable.get(clr);
			if (null == alphaClrMap)
			{
				alphaClrMap = new HashMap<Integer, Color>();
				Color alphaClr = new Color(clr.getRed(), clr.getGreen(),
						clr.getBlue(), alpha);

				alphaClrMap.put(alpha, alphaClr);
				colorTable.put(clr, alphaClrMap);

				return alphaClr;
			} else
			{
				Color alphaClr = alphaClrMap.get(alpha);
				if (null == alphaClr)
				{
					alphaClr = new Color(clr.getRed(), clr.getGreen(),
							clr.getBlue(), alpha);
					alphaClrMap.put(alpha, alphaClr);

					return alphaClr;
				} else
				{
					return alphaClr;
				}

			}
		} finally
		{
			clrTableLock.unlock();
		}

	}
}
