package com.accela.tetrixgame.gamelogic.rectMover.scrolling;

import com.accela.tetrixgame.gamelogic.control.ScrollStatus;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;

/**
 * 
 * Bar专用的控制翻转运动的移动代理
 * 
 */
public class BarScrollProxy
{
	private ScrollProxy[] upScroll;

	private ScrollProxy[] upScrollReverse;

	private UniformSpeedDownMoveProxy[][] liftProxy;

	private UniformSpeedDownMoveProxy[][] liftProxyReverse;

	private boolean running = false;

	private int blockWidth;

	private int blockHeight;

	private boolean reverseScroll = false;

	@SuppressWarnings("unused")
	private BarScrollProxy()
	{

	}

	public BarScrollProxy(int barRowNum, int barColumnNum, int blockWidth,
			int blockHeight, double maxVel)
	{
		if (barRowNum <= 0 || barColumnNum <= 0)
		{
			throw new IllegalArgumentException(
					"illegal barRowNum or barColumnNum");
		}
		if (blockWidth <= 0 || blockHeight <= 0)
		{
			throw new IllegalArgumentException(
					"illegal blockWidth or blockHeight");
		}
		if (maxVel <= 0)
		{
			throw new IllegalArgumentException("maxVel should be positive");
		}

		this.blockWidth = blockWidth;
		this.blockHeight = blockHeight;
		initScrollProxyAndLiftProxy(barRowNum,
				barColumnNum,
				blockWidth,
				blockHeight,
				maxVel);
	}

	private void initScrollProxyAndLiftProxy(int barRowNum, int barColumnNum,
			int blockWidth, int blockHeight, double maxVel)
	{
		assert (barRowNum > 0);
		assert (barColumnNum > 0);
		assert (blockWidth > 0);
		assert (blockHeight > 0);
		assert (maxVel > 0);

		if (barRowNum < 2)
		{
			throw new IllegalArgumentException(
					"barRowNum should not be less than 2");
		}

		upScroll = new ScrollProxy[barColumnNum];
		upScrollReverse = new ScrollProxy[barColumnNum];
		liftProxy = new UniformSpeedDownMoveProxy[barRowNum - 1][barColumnNum];
		liftProxyReverse = new UniformSpeedDownMoveProxy[barRowNum - 1][barColumnNum];

		double factor = 1.0;

		for (int i = 0; i < upScroll.length; i++)
		{
			upScroll[i] = new ScrollProxy(i * blockWidth, upScroll.length
					* blockWidth, (upScroll.length - 1 - i) * blockWidth
					* factor, maxVel, Math.pow((double) blockWidth * blockWidth
					+ Math.pow((barRowNum - 1) * (double) blockHeight / 2, 2),
					0.5),
					-Math.atan(((barRowNum - 1) * (double) blockHeight / 2)
							/ blockWidth), (i + 1) * blockWidth, false);

			upScrollReverse[upScrollReverse.length - 1 - i] = new ScrollProxy(i
					* blockWidth, upScroll.length * blockWidth,
					(upScroll.length - 1 - i) * blockWidth * factor, maxVel,
					Math.pow((double) blockWidth
							* blockWidth
							+ Math.pow((barRowNum - 1) * (double) blockHeight
									/ 2, 2), 0.5), Math.atan(((barRowNum - 1)
							* (double) blockHeight / 2)
							/ blockWidth)
							- Math.PI, (i + 1) * blockWidth, true);

			double liftMaxVel = maxVel / blockWidth * blockHeight;
			for (int row = 0; row < liftProxy.length; row++)
			{
				liftProxy[row][i] = new UniformSpeedDownMoveProxy(0,
						blockHeight, (liftProxy.length + i) * blockHeight
								* factor, liftMaxVel, false);

				liftProxyReverse[row][liftProxyReverse[row].length - 1 - i] = new UniformSpeedDownMoveProxy(
						0, blockHeight, (liftProxy.length + i) * blockHeight
								* factor, liftMaxVel, false);

			}

		}

	}

	public void update(ScrollStatus status, Rectangle[][] bar, int barX,
			int barY, long interpolation)
	{
		if (null == status)
		{
			throw new NullPointerException("status should not be null");
		}
		if (null == bar)
		{
			throw new NullPointerException("bar should not be null");
		}
		if (bar.length <= 0)
		{
			throw new IllegalArgumentException("illegal bar: bar.length = "
					+ bar.length);
		}
		if (bar[0].length != upScroll.length)
		{
			throw new IllegalArgumentException(
					"bar does match this BarScrollProxy");
		}
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}
		for (int i = 0; i < bar.length; i++)
		{
			for (int j = 0; j < bar[i].length; j++)
			{
				if (null == bar[i][j])
				{
					throw new NullPointerException("bar[" + i + "][" + j
							+ "] is null. It's illegal");
				}
			}
		}

		// parameter check is over

		if (!running)
		{
			if (ScrollStatus.SCROLLING == status)
			{
				running = true;
			}
		} else
		{
			for (int i = 0; i < upScroll.length; i++)
			{
				final Rectangle rect = bar[0][i];
				assert (rect != null);

				if (!reverseScroll)
				{
					upScroll[i].update(rect, interpolation);
				} else
				{
					upScrollReverse[i].update(rect, interpolation);
				}
			}

			for (int i = 0; i < liftProxy.length; i++)
			{
				for (int j = 0; j < liftProxy[i].length; j++)
				{
					final Rectangle rect = bar[i + 1][j];

					if (!reverseScroll)
					{
						liftProxy[i][j].update(rect, interpolation);
					} else
					{
						liftProxyReverse[i][j].update(rect, interpolation);
					}
				}
			}

			// 检查是否已经完成翻转
			boolean allFinished = true;
			for (int i = 0; i < upScroll.length; i++)
			{
				if (!reverseScroll)
				{
					if (!upScroll[i].hasFinished())
					{
						allFinished = false;
						break;
					}
				} else
				{
					if (!upScrollReverse[i].hasFinished())
					{
						allFinished = false;
						break;
					}
				}

				for (int row = 0; row < liftProxy.length; row++)
				{
					if (!reverseScroll)
					{
						if (!liftProxy[row][i].hasFinished())
						{
							allFinished = false;
							break;
						}
					} else
					{
						if (!liftProxyReverse[row][i].hasFinished())
						{
							allFinished = false;
							break;
						}
					}
				}
			}

			if (allFinished)
			{
				enforceFinish(bar, barX, barY);
			}
		}

	}

	private void switchBarBlocks(Rectangle[][] bar)
	{
		assert (bar != null);

		Rectangle[] temp = new Rectangle[bar[0].length];
		for (int i = 0; i < temp.length; i++)
		{
			temp[i] = bar[0][i];
		}

		for (int j = 0; j < bar[0].length; j++)
		{
			for (int i = 0; i < bar.length - 1; i++)
			{
				bar[i][j] = bar[i + 1][j];
			}
		}

		for (int i = 0; i < temp.length; i++)
		{
			bar[bar.length - 1][i] = temp[temp.length - 1 - i];
		}

	}

	private void gridAlignAll(Rectangle[][] bar, int barX, int barY)
	{
		assert (bar != null);

		for (int i = 0; i < bar.length; i++)
		{
			for (int j = 0; j < bar[i].length; j++)
			{
				gridAlign(bar, i, j, barX, barY);
			}
		}
	}

	private void gridAlign(Rectangle[][] bar, int i, int j, int barX, int barY)
	{
		assert (bar != null);
		assert (i >= 0 && i < bar.length);
		assert (j >= 0 && j < bar[i].length);

		bar[i][j].x = barX + j * blockWidth;
		bar[i][j].y = barY + i * blockHeight;

	}

	public void reset()
	{
		for (int i = 0; i < upScroll.length; i++)
		{
			if (!reverseScroll)
			{
				upScroll[i].reset();
			} else
			{
				upScrollReverse[i].reset();
			}

			if (!reverseScroll)
			{
				for (int row = 0; row < liftProxy.length; row++)
				{
					liftProxy[row][i].reset();
				}
			} else
			{
				for (int row = 0; row < liftProxyReverse.length; row++)
				{
					liftProxyReverse[row][i].reset();
				}
			}
		}

		running = false;
		reverseScroll = !reverseScroll;
	}

	public void enforceFinish(Rectangle[][] bar, int barX, int barY)
	{
		// parameter check
		if (null == bar)
		{
			throw new NullPointerException("bar should not be null");
		}
		if (bar.length <= 0)
		{
			throw new IllegalArgumentException("illegal bar: bar.length = "
					+ bar.length);
		}
		if (bar[0].length != upScroll.length)
		{
			throw new IllegalArgumentException(
					"bar does match this BarScrollProxy");
		}
		for (int i = 0; i < bar.length; i++)
		{
			for (int j = 0; j < bar[i].length; j++)
			{
				if (null == bar[i][j])
				{
					throw new NullPointerException("bar[" + i + "][" + j
							+ "] is null. It's illegal");
				}
			}
		}

		// start to process
		if (!reverseScroll)
		{
			if (upScroll[0].hasFinished() || upScroll[0].hasHalfFinished())
			{
				switchBarBlocks(bar);
			}
		} else
		{
			if (upScrollReverse[0].hasFinished()
					|| upScrollReverse[0].hasHalfFinished())
			{
				switchBarBlocks(bar);
			}
		}

		gridAlignAll(bar, barX, barY);

		reset();

		assert (!running);
	}

	public int getBlockWidth()
	{
		return blockWidth;
	}

	public int getBlockHeight()
	{
		return blockHeight;
	}

}
