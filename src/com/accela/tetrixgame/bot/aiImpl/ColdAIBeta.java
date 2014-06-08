package com.accela.tetrixgame.bot.aiImpl;

import java.awt.Point;

import com.accela.tetrixgame.bot.ai.MovingStrategy;
import com.accela.tetrixgame.bot.ai.AI;
import com.accela.tetrixgame.bot.ai.Data;

/**
 * 
 * 一个简单的TetrixAI的实现。这个AI控制的BOT基本上就是 先找出一个能得分的地方，然后把Bar移动过去
 * 
 */
public class ColdAIBeta extends AI {
	private int[][] aimBarType = null;
	private Point[][] aimBarIdx = null;

	@Override
	protected void decideStrategy(MovingStrategy strategy, Data data) {
		if (data.isFirstIn()) {
			// 计算希望将bar移动到的位置，以及翻转后的状态。用aimBarType和aimBarIdx记录
			findAim(data);
		} else {
			// 将Bar移向过去计算出的目标
			assert (aimBarType != null);
			assert (aimBarIdx != null);

			boolean hAchieved = false;

			// 计算水平移动策略
			Point[][] barIdx = data.getBarIdx();
			if (barIdx[0][0].y == aimBarIdx[0][0].y
					&& ((data.getBarOffsetXInGrid() == 0) || (data
							.getBarHerizontalVel() < 0))) {
				strategy.hs = MovingStrategy.HerizontalStrategy.NONE;
			} else if (barIdx[0][0].y == aimBarIdx[0][0].y - 1
					&& data.getBarHerizontalVel() > 0) {
				strategy.hs = MovingStrategy.HerizontalStrategy.NONE;
			} else if (barIdx[0][0].y < aimBarIdx[0][0].y) {
				strategy.hs = MovingStrategy.HerizontalStrategy.RIGHT;
			} else if (barIdx[0][0].y > aimBarIdx[0][0].y) {
				strategy.hs = MovingStrategy.HerizontalStrategy.LEFT;
			} else {
				strategy.hs = MovingStrategy.HerizontalStrategy.LEFT;
			}

			if (barIdx[0][0].y == aimBarIdx[0][0].y
					&& data.getBarOffsetXInGrid() == 0) {
				hAchieved = true;
			}

			boolean scrollAchieved = true;
			// 计算翻转策略
			int[][] barType = data.getBarTypes()[0];
			assert (barType.length == aimBarType.length);
			assert (barType[0].length == aimBarType[0].length);

			for (int i = 0; i < barType.length; i++) {
				for (int j = 0; j < barType[i].length; j++) {
					if (barType[i][j] != aimBarType[i][j]) {
						scrollAchieved = false;
						break;
					}
				}
			}

			if (scrollAchieved) {
				strategy.ss = MovingStrategy.ScrollStrategy.NONE;
			} else {
				strategy.ss = MovingStrategy.ScrollStrategy.SCROLL;
			}

			// 计算竖直移动策略
			if (hAchieved && scrollAchieved) {
				strategy.vs = MovingStrategy.VerticalStrategy.DOWN_ACC;
			} else {
				strategy.vs = MovingStrategy.VerticalStrategy.NONE;
			}

		}

	}

	private Point[][] bufBarIdx = null;

	private Point[][] getBufBarIdx(Point[][] barIdx) {
		if (null == bufBarIdx || bufBarIdx.length != barIdx.length
				|| bufBarIdx[0].length != barIdx[0].length) {
			bufBarIdx = new Point[barIdx.length][barIdx[0].length];
			for (int i = 0; i < bufBarIdx.length; i++) {
				for (int j = 0; j < bufBarIdx[i].length; j++) {
					bufBarIdx[i][j] = new Point();
				}
			}
		}

		for (int i = 0; i < bufBarIdx.length; i++) {
			for (int j = 0; j < bufBarIdx[i].length; j++) {
				if (null == bufBarIdx[i][j]) {
					bufBarIdx[i][j] = new Point();
				}

				bufBarIdx[i][j].x = barIdx[i][j].x;
				bufBarIdx[i][j].y = barIdx[i][j].y;
			}
		}

		return bufBarIdx;
	}

	private void findAim(Data data) {
		int[][] matrix = data.getMatrix();
		int[][][] barTypes = data.getBarTypes();

		boolean find = false;
		int aimScrollIdx = 0;
		int aimOffset = 0;

		int maxScore = 0;

		// ==搜索得分点==
		for (int scroll_idx = 0; scroll_idx < barTypes.length; scroll_idx++) {
			boolean leftSearchFirst = Math.random() >= 0.5;

			// 先向一边搜索
			int offset = 0;
			while (true) {
				Point[][] bufBarIdx = getBufBarIdx(data.getBarIdx());
				shiftBarIdx(bufBarIdx, offset);

				if (hasBarCollision(matrix, bufBarIdx)) {
					break;
				}

				int score = data.calScore(matrix, bufBarIdx,
						barTypes[scroll_idx]);

				if (score > maxScore) {
					find = true;
					aimScrollIdx = scroll_idx;
					aimOffset = offset;

					maxScore = score;

					break;
				}

				if (leftSearchFirst) {
					offset--;
				} else {
					offset++;
				}
			}

			// 检查是否找到得分点
			if (find) {
				break;
			}

			// 再向另一边搜索
			offset = 0;
			while (true) {
				Point[][] bufBarIdx = getBufBarIdx(data.getBarIdx());
				shiftBarIdx(bufBarIdx, offset);

				if (hasBarCollision(matrix, bufBarIdx)) {
					break;
				}

				int score = data.calScore(matrix, bufBarIdx,
						barTypes[scroll_idx]);

				if (score > maxScore) {
					find = true;
					aimScrollIdx = scroll_idx;
					aimOffset = offset;

					maxScore = score;

					break;
				}

				if (!leftSearchFirst) {
					offset--;
				} else {
					offset++;
				}
			}

			// 检查是否找到得分点
			if (find) {
				break;
			}

		}

		// 计算搜索到的得分点的砖块堆了多高
		Point[][] barIdx = data.getBarIdx();
		int searchPointHeight = 0;
		if (find) {
			for (int i = 0; i < barIdx[0].length; i++) {
				int column = barIdx[0][0].y + aimOffset + i;
				for (int row = matrix.length - 1; row >= 0; row--) {
					if (matrix[row][column] >= 0) {
						searchPointHeight++;
					} else {
						break;
					}
				}
			}
		}

		// 寻找matrix砖块堆的最低的地方
		int minHeightAimColumn = 0;
		int minTotalHeight = barIdx[0].length * matrix.length;
		if (Math.random() >= 0.5) {
			// 从左向右搜索
			for (int column = 0; column < matrix[0].length - barIdx[0].length
					+ 1; column++) {
				int totalHeight = 0;
				for (int i = 0; i < barIdx[0].length; i++) {
					int trueColumn = column + i;
					for (int row = matrix.length - 1; row >= 0; row--) {
						if (matrix[row][trueColumn] >= 0) {
							totalHeight++;
						} else {
							break;
						}
					}
				}
				if (totalHeight < minTotalHeight) {
					minHeightAimColumn = column;
					minTotalHeight = totalHeight;
				}
			}
		} else {
			// 从右向左搜索
			for (int column = matrix[0].length - barIdx[0].length; column >= 0; column--) {
				int totalHeight = 0;
				for (int i = 0; i < barIdx[0].length; i++) {
					int trueColumn = column + i;
					for (int row = matrix.length - 1; row >= 0; row--) {
						if (matrix[row][trueColumn] >= 0) {
							totalHeight++;
						} else {
							break;
						}
					}
				}
				if (totalHeight < minTotalHeight) {
					minHeightAimColumn = column;
					minTotalHeight = totalHeight;
				}
			}
		}

		// 修正结果
		if (find) {
			if (((searchPointHeight - minTotalHeight) * 1.0
					/ (barIdx[0].length * barIdx.length) * 1.0) >= maxScore) {
				find = false;
			}
		}

		// 处理搜索结果
		if (find) {
			recordAimBarType(barTypes[aimScrollIdx]);
			recordAimBarIdx(barIdx, aimOffset);
		} else {
			recordAimBarType(barTypes[0]);

			// 计算aimOffset
			aimOffset = minHeightAimColumn - barIdx[0][0].y;
			recordAimBarIdx(barIdx, aimOffset);
		}
	}

	private void shiftBarIdx(Point[][] barIdx, int offset) {
		for (int i = 0; i < barIdx.length; i++) {
			for (int j = 0; j < barIdx[i].length; j++) {
				barIdx[i][j].y += offset;
			}
		}
	}

	private boolean hasBarCollision(int[][] matrix, Point[][] barIdx) {
		for (int i = 0; i < barIdx.length; i++) {
			for (int j = 0; j < barIdx[i].length; j++) {
				Point pos = barIdx[i][j];
				if (pos.x < 0 || pos.y >= matrix.length || pos.y < 0
						|| pos.y >= matrix[0].length) {
					return true;
				}

				if (matrix[pos.x][pos.y] >= 0) {
					return true;
				}
			}
		}

		return false;
	}

	private void recordAimBarType(int[][] aim) {
		if (null == aimBarType || aimBarType.length != aim.length
				|| aimBarType[0].length != aim[0].length) {
			aimBarType = new int[aim.length][aim[0].length];
		}

		for (int i = 0; i < aimBarType.length; i++) {
			for (int j = 0; j < aimBarType[i].length; j++) {
				aimBarType[i][j] = aim[i][j];
			}
		}
	}

	private void recordAimBarIdx(Point[][] barIdx, int aimOffset) {
		if (null == aimBarIdx || aimBarIdx.length != barIdx.length
				|| aimBarIdx[0].length != barIdx[0].length) {
			aimBarIdx = new Point[barIdx.length][barIdx[0].length];
			for (int i = 0; i < aimBarIdx.length; i++) {
				for (int j = 0; j < aimBarIdx[i].length; j++) {
					aimBarIdx[i][j] = new Point();
				}
			}
		}

		for (int i = 0; i < aimBarIdx.length; i++) {
			for (int j = 0; j < aimBarIdx[i].length; j++) {
				if (null == aimBarIdx[i][j]) {
					aimBarIdx[i][j] = new Point();
				}

				aimBarIdx[i][j].x = barIdx[i][j].x;
				aimBarIdx[i][j].y = barIdx[i][j].y + aimOffset;
			}
		}
	}

}
