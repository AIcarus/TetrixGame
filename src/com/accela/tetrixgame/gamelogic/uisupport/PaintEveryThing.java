package com.accela.tetrixgame.gamelogic.uisupport;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

import com.accela.tetrixgame.gamelogic.gameData.GameData;

/**
 * 
 * 利用这个类，来方便地画出游戏中的所有组成部分
 * 
 */
public class PaintEveryThing {
	private BlockPainter blockPainter = new BlockPainter();

	private GridPainter gridPainter = new GridPainter();

	private ScorePainter scorePainter = new ScorePainter();

	private HintPainter hintPainter = new HintPainter();

	// scale 指的是，把原来的图像按着scale指定的比例缩放后再绘出；
	// 其他的参数都是针对原图像的
	public void paintAll(Graphics g, Point orign, ImageObserver observer,
			GameData gameData, double scale, String hint) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}
		if (null == orign) {
			throw new NullPointerException("orign should not be null");
		}
		if (null == observer) {
			throw new NullPointerException("observer should not be null");
		}
		if (null == gameData) {
			throw new NullPointerException("gameData should not be null");
		}
		if (scale <= 0) {
			throw new IllegalArgumentException("scale should be positive");
		}

		setRenderHint((Graphics2D) g);

		Point orignInGrid = new Point(orign.x
				+ (int) (gameData.getGridWidth() * scale), orign.y
				+ (int) (gameData.getGridWidth() * scale));

		blockPainter.paint(g, orignInGrid, observer, gameData, scale);
		gridPainter.paint(g, orign, observer, gameData, scale);
		gameData.getEffectContainer().paintAll(g, orignInGrid, scale);
		scorePainter.paint(g, orignInGrid, observer, gameData, scale);
		hintPainter.setHint(hint);
		hintPainter.paint(g, orignInGrid, observer, gameData, scale);
		restoreRenderHint((Graphics2D) g);

	}

	private Map<java.awt.RenderingHints.Key, Object> renderHintMap = new HashMap<java.awt.RenderingHints.Key, Object>();

	private void setRenderHint(Graphics2D g2) {
		assert (g2 != null);

		recordOldHint(g2);

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_SPEED);
	}

	private void restoreRenderHint(Graphics2D g2) {
		assert (g2 != null);

		Object value = renderHintMap.get(RenderingHints.KEY_ANTIALIASING);
		if (value != null) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, value);
		}

		value = renderHintMap.get(RenderingHints.KEY_RENDERING);
		if (value != null) {
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, value);
		}

		value = renderHintMap.get(RenderingHints.KEY_COLOR_RENDERING);
		if (value != null) {
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, value);
		}

	}

	private void recordOldHint(Graphics2D g2) {
		assert (g2 != null);

		renderHintMap.clear();

		renderHintMap.put(RenderingHints.KEY_ANTIALIASING,
				g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING));

		renderHintMap.put(RenderingHints.KEY_RENDERING,
				g2.getRenderingHint(RenderingHints.KEY_RENDERING));

		renderHintMap.put(RenderingHints.KEY_COLOR_RENDERING,
				g2.getRenderingHint(RenderingHints.KEY_COLOR_RENDERING));

	}

}
