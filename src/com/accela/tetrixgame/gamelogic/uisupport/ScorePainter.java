package com.accela.tetrixgame.gamelogic.uisupport;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;

import com.accela.tetrixgame.gamelogic.gameData.GameData;

public class ScorePainter extends Painter
{
	public static final Font FONT = new Font(null, Font.PLAIN, 12);

	public static Color FONT_COLOR = Color.YELLOW;

	public static final int INSET = 10;

	private FontMetrics fm = null; // 用来提高效率的缓冲数据

	private long lastScore = 0; // 用来提高效率的缓冲数据

	private String lastScoreStr = "0"; // 用来提高效率的缓冲数据

	@Override
	protected void paintImpl(Graphics g, Point orign, ImageObserver observer,
			GameData gameData, double scale)
	{
		// 初始化关于字体绘制的参数
		String curScoreStr = null;
		if (gameData.getScoreRecorder().getCurScore() == lastScore)
		{
			curScoreStr = lastScoreStr;
		} else
		{
			lastScore = gameData.getScoreRecorder().getCurScore();
			lastScoreStr = String.valueOf(lastScore);

			curScoreStr = lastScoreStr;

		}
		if (null == fm)
		{
			fm = g.getFontMetrics(FONT);
		}

		// 得到要绘制的文字的大小
		int strWidth = fm.stringWidth(curScoreStr);
		int strHeight = fm.getAscent();

		// 得到要绘制的文字的颜色
		final int BASE_ALPHA = 160;
		Color clr = getScoreColor(BASE_ALPHA
				+ (int) ((255 - BASE_ALPHA) * gameData.getScoreRecorder().getHotDegree()));

		// 绘制分数
		double leftupX = orign.x
				+ (gameData.getColumnNum() * gameData.getBlockWidth() - INSET)
				* scale - strWidth;
		double leftupY = orign.y + INSET * scale;

		g.setFont(FONT);

		g.setColor(clr);
		g.drawString(curScoreStr, (int) leftupX, (int) (leftupY) + strHeight);
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

}
