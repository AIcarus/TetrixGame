package com.accela.tetrixgame.gamelogic.uisupport;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.accela.tetrixgame.gamelogic.basic.Bar;
import com.accela.tetrixgame.gamelogic.basic.Block;
import com.accela.tetrixgame.gamelogic.basic.MovableRect;
import com.accela.tetrixgame.gamelogic.gameData.GameData;

/**
 * 
 * 用来绘制游戏中的所有方块。方块被绘制成在移动的时候能够 拖长影的。
 * 
 */
public class BlockPainter extends Painter {
	private static Map<Integer, BufferedImage[]> imgMap = new ConcurrentHashMap<Integer, BufferedImage[]>();

	public static String IMAGE_SUFFIX = ".png";
	public static String IMAGE_PATH = "com/accela/tetrixgame/pic/blocks/";

	/**
	 * 水平方向的影子最远能够延伸的距离与砖块的宽的比
	 */
	public final int SHADOW_H_MAX_OFFSET_COUNT = 1;
	/**
	 * 竖直方向的影子最远能够延伸的距离与砖块的高的比
	 */
	public final int SHADOW_V_MAX_OFFSET_COUNT = 2;
	/**
	 * 影子的数量，这个数量必须和提供的图片的数量相匹配
	 */
	public static final int SHADOW_NUM = 1;

	/**
	 * 绘制出所有砖块
	 * 
	 * @param g
	 * @param orign
	 *            绘制的原点
	 * @param observer
	 *            ImageObserver
	 * @param gameData
	 *            游戏数据
	 */
	// 这里画影子的时候，把砖块从下往上画，来防止下面的方块的影子盖住上面的
	@Override
	protected void paintImpl(Graphics g, Point orign, ImageObserver observer,
			GameData gameData, double scale) {
		Point shakedOrign = new java.awt.Point(orign.x
				+ (int) gameData.getEffectContainer().getHShakeOffset(),
				orign.y + (int) gameData.getEffectContainer().getVShakeOffset());

		for (int i = gameData.getBlockContainer().getMatrix().getRowNum() - 1; i >= 0; i--) {
			for (int j = 0; j < gameData.getBlockContainer().getMatrix()
					.getColumnNum(); j++) {
				Block b = gameData.getBlockContainer().getMatrix().get(i, j);
				if (null == b) {
					continue;
				}

				paintBlock(g, shakedOrign, observer, gameData
						.getBlockContainer().getMatrix().get(i, j), gameData,
						scale);
			}
		}

		Bar bar = gameData.getBlockContainer().getBar();
		if (bar != null) {
			paintBar(g, shakedOrign, observer, bar, gameData, scale);
		}

	}

	/**
	 * 绘制Bar时，要求下面的Block的影子不会重叠上面的Block，因此使用特别的绘制方法
	 */
	private void paintBar(Graphics g, Point orign, ImageObserver observer,
			Bar bar, GameData gameData, double scale) {
		assert (g != null);
		assert (orign != null);
		assert (observer != null);
		assert (bar != null);
		assert (gameData != null);
		assert (scale > 0);

		if (bar.getHerizontalVel() > 0) {
			for (int i = bar.getRowNum() - 1; i >= 0; i--) {
				for (int j = bar.getColumnNum() - 1; j >= 0; j--) {
					Block b = bar.getBlock(i, j);
					assert (b != null);

					paintBlock(g, orign, observer, b, gameData, scale);
				}
			}
		} else {
			for (int i = bar.getRowNum() - 1; i >= 0; i--) {
				for (int j = 0; j < bar.getColumnNum(); j++) {
					Block b = bar.getBlock(i, j);
					assert (b != null);

					paintBlock(g, orign, observer, b, gameData, scale);
				}
			}
		}

	}

	/**
	 * 绘制一个砖块
	 */
	private void paintBlock(Graphics g, Point orign, ImageObserver observer,
			Block block, GameData gameData, double scale) {
		assert (g != null);
		assert (orign != null);
		assert (observer != null);
		assert (block != null);
		assert (gameData != null);
		assert (scale > 0);

		final int SHADOW_H_MAX_OFFSET = gameData.getBlockWidth()
				* SHADOW_H_MAX_OFFSET_COUNT;

		final int SHADOW_V_MAX_OFFSET = gameData.getBlockHeight()
				* SHADOW_V_MAX_OFFSET_COUNT;

		// 获取水平速度与竖直速度和最大速度的比率，比率可以是负数
		double hRate = 0;
		double vRate = 0;
		if (gameData.getBlockContainer().getBar() != null
				&& gameData.getBlockContainer().getBar().contains(block)) {
			hRate = getHerizontalVelRate(gameData.getBlockContainer().getBar(),
					gameData);
			vRate = getVerticalVelRate(gameData.getBlockContainer().getBar(),
					gameData);
		} else {
			hRate = getHerizontalVelRate(block, gameData);
			vRate = getVerticalVelRate(block, gameData);
		}

		Image[] imgs = getBlockImage(block.getType());
		assert (imgs != null);
		for (int i = 0; i < imgs.length; i++) {
			assert (imgs[i] != null);
		}
		assert (imgs.length == SHADOW_NUM + 1);

		if (Math.abs(hRate) < 0.1 && Math.abs(vRate) < 0.1) {
			// 不画影子
			drawImage(g, imgs[0], orign.x + (int) (block.x * scale), orign.y
					+ (int) (block.y * scale),
					(int) (block.getWidth() * scale),
					(int) (block.getHeight() * scale), observer);
		} else {
			// 画出影子
			for (int i = imgs.length - 1; i >= 0; i--) {
				int hOffset = (int) (SHADOW_H_MAX_OFFSET * hRate * i
						/ (imgs.length - 1) * (-1));
				int vOffset = (int) (SHADOW_V_MAX_OFFSET * vRate * i
						/ (imgs.length - 1) * (-1));

				drawImage(g, imgs[i], orign.x
						+ (int) ((block.x + hOffset) * scale), orign.y
						+ (int) ((block.y + vOffset) * scale),
						(int) (block.getWidth() * scale),
						(int) (block.getHeight() * scale), observer);
			}
		}

	}
	
	private void drawImage(Graphics g, Image img, int x, int y, int width, int height, ImageObserver observer){
		if(width==img.getWidth(null)&&height==img.getHeight(null)){
			// I believe without scaling, image painting can be faster
			g.drawImage(img, x, y, observer);
		}else{
			g.drawImage(img, x, y, width, height, observer);
		}
	}

	/**
	 * 根据blockType获取或者加载砖块
	 * 
	 * @param blockType
	 * @return
	 */
	private BufferedImage[] getBlockImage(int blockType) {
		if (blockType < 0) {
			throw new IllegalArgumentException("blockType should be nonnegtive");
		}

		BufferedImage[] imgs = imgMap.get(blockType);

		if (null == imgs) {
			imgs = new BufferedImage[SHADOW_NUM + 1];

			for (int i = 0; i < imgs.length; i++) {
				boolean hasException = false;
				do {
					hasException = false;
					try {
						imgs[i] = ImageIO.read(ResourceLocator
								.getURL(IMAGE_PATH + blockType + "_" + i
										+ IMAGE_SUFFIX));

						if (i > 0) {
							// imgs[i] = processImage(imgs[i]); TODO
							// 有的jre不支持processImage中的操作
						}

					} catch (IOException ex) {
						hasException = true;
						ex.printStackTrace();

						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex2) {
							hasException = true;
							ex2.printStackTrace();
						}
					}
				} while (hasException);

				assert (imgs[i] != null);
			}

			imgMap.put(blockType, imgs);
		} else {
			for (int i = 0; i < imgs.length; i++) {
				assert (imgs[i] != null);
			}
		}

		return imgs;
	}

	/*
	 * private BufferedImage processImage(BufferedImage srcImg) { assert (srcImg
	 * != null);
	 * 
	 * BufferedImage destImg = new BufferedImage(srcImg.getWidth(),
	 * srcImg.getHeight(), srcImg.getType());
	 * 
	 * final int LENGTH = 1; float[] data = new float[LENGTH * LENGTH]; for (int
	 * i = 0; i < data.length; i++) { data[i] = 1.0f / (data.length * 1.0f); }
	 * Kernel kernel = new Kernel(LENGTH, LENGTH, data);
	 * 
	 * BufferedImageOp op = new ConvolveOp(kernel); // TODO 这里模糊效果不管用啊
	 * op.filter(srcImg, destImg); return destImg; }
	 */

	/**
	 * 根据速度，决定当前水平方向的延伸距离和最大延伸距离的比率
	 */
	private double getHerizontalVelRate(MovableRect rect, GameData gameData) {
		assert (rect != null);
		assert (gameData != null);

		return rect.getHerizontalVel() / gameData.getHMaxVel();
	}

	/**
	 * 根据速度，决定当前竖直方向的延伸距离和最大延伸距离的比率
	 */
	private double getVerticalVelRate(MovableRect rect, GameData gameData) {
		assert (rect != null);
		assert (gameData != null);

		return Math.pow(rect.getVerticalVel(), 3)
				/ Math.pow(gameData.getVMaxVel(), 3);
	}

}
