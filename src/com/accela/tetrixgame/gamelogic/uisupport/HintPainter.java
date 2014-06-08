package com.accela.tetrixgame.gamelogic.uisupport;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;

import com.accela.tetrixgame.util.IntervalTimer;
import com.accela.tetrixgame.gamelogic.gameData.GameData;

/**
 * 
 * 用来在游戏区域中心绘制一个透明度渐变的单行文本
 * 
 */
public class HintPainter extends Painter
{
	public static final int DEFAULT_FONT_SIZE = 12;

	public static Color FONT_COLOR = Color.YELLOW;

	public static final int INSET = 10;

	private String hint = null;

	public String getHint()
	{
		return hint;
	}

	public void setHint(String hint)
	{
		this.hint = hint;
	}

	private FontMetrics fm = null; // 用来提高效率的缓冲数据

	private double lastScale = 1.0; // 用来提高效率的缓冲数据

	private Font lastFont = new Font(null, Font.PLAIN, DEFAULT_FONT_SIZE); // 用来提高效率的缓冲数据

	@Override
	protected void paintImpl(Graphics g, Point orign, ImageObserver observer,
			GameData gameData, double scale)
	{
		if (null == hint)
		{
			return;
		}

		// 初始化关于字体绘制的参数
		Font curFont = null;
		if (lastScale == scale)
		{
			curFont = lastFont;
		} else
		{
			lastScale = scale;
			lastFont = new Font(null, Font.PLAIN, Math.max(1,
					(int) (DEFAULT_FONT_SIZE * scale)));

			curFont = lastFont;
		}

		if (null == fm)
		{
			fm = g.getFontMetrics(curFont);
		}

		// 得到要绘制的文字的大小
		int strWidth = fm.stringWidth(hint);
		int strHeight = fm.getAscent();

		// 得到要绘制的文字的颜色
		final int BASE_ALPHA = 0;
		Color clr = getScoreColor(BASE_ALPHA
				+ (int) ((255 - BASE_ALPHA) * getHotDegree()));

		// 绘制分数
		double leftupX = orign.x + gameData.getColumnNum()
				* gameData.getBlockWidth() / 2 * scale - strWidth / 2;
		double leftupY = orign.y + gameData.getRowNum()
				* gameData.getBlockHeight() / 2 * scale - strHeight / 2;

		g.setFont(curFont);

		g.setColor(clr);
		g.drawString(hint, (int) leftupX, (int) leftupY + strHeight);

		// 刷新hotDegree相关参数
		update();

	}

	private Color getScoreColor(int alpha)
	{
		if (alpha < 0 || alpha > 255)
		{
			throw new IllegalArgumentException("Illegal alpha: " + alpha);
		}

		Color clr = Painter.getColor(FONT_COLOR, alpha);
		assert (clr != null);

		return clr;
	}

	private static final long HOT_PERIOD = 2 * 1000;

	private long curHotCounter = 0;

	private IntervalTimer intervalTimer = new IntervalTimer();

	private void update()
	{
		intervalTimer.update();
		long interpolation = intervalTimer.getInterval();
		if (interpolation < 0)
		{
			return;
		}

		curHotCounter += interpolation;
		curHotCounter %= HOT_PERIOD;
	}

	private double getHotDegree()
	{
		return Math.sin((double) curHotCounter / (double) (2 * HOT_PERIOD)
				* Math.PI * 2);
	}

}
