package com.accela.tetrixgame.gamelogic.score;

/**
 * 
 * 这个类用来记录游戏分数。游戏分数增加的时候，数字不是一下子就增加的， 而是逐个数字地快速增长，一直长到实际的分数。而且当分数保持不变的时候，
 * UI中绘制的分数文本的透明度会逐渐下降，而当加分时，透明度又会增加。
 * 
 */
public class ScoreRecorder
{
	/**
	 * 记录当前显示出来的分数
	 */
	private long curScore;
	/**
	 * 记录实际的分数
	 */
	private long actualScore;
	/**
	 * 当加分的时候，hosDegree立即变成1.0。 如果一段时间内分数不再增加，则hotDegree
	 * 逐渐衰减直到0。衰减速度是HOT_DECAY_VEL
	 */
	private double hotDegree = 0; // 反映是否刚刚增加过分数，绘制的时候，会根据这个值来决定透明度
	/**
	 * hotDegree的衰减速度
	 */
	public static final double HOT_DECAY_VEL = 0.0005;
	/**
	 * 加分时的分数基数
	 */
	private long baseScore = -1;

	public ScoreRecorder(long baseScore)
	{
		if (baseScore <= 0)
		{
			throw new IllegalArgumentException("baseScore should be positive");
		}

		this.baseScore = baseScore;

		this.curScore = 0;
		this.actualScore = 0;
	}

	/**
	 * 根据blockNum，按照等差数列加分
	 */
	public void addScore(int blockNum)
	{
		if (blockNum < 0)
		{
			throw new IllegalArgumentException("blockNum should be nonnegtive");
		}

		long scoreAdder = baseScore;
		boolean added = false;
		for (int i = 0; i < blockNum; i++)
		{
			this.actualScore += scoreAdder;
			added = true;

			scoreAdder += baseScore;
		}

		if (added)
		{
			hotDegree = 1.0;
		}
	}

	/**
	 * 返回当前应该显示出来的分数。这个分数不是准确的分数。 准确的分数是actualScore。curScore是为了使分数显示
	 * 有动态的效果，而设计的。
	 */
	public long getCurScore()
	{
		return curScore;
	}

	public double getHotDegree()
	{
		return hotDegree;
	}

	public void update(long interpolation)
	{
		if (interpolation < 0)
		{
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		// 分数动态追加
		curScore += Math.min(actualScore - curScore,
				Math.max(1, baseScore / 10));

		// hotDegree的衰减
		if (curScore == actualScore)
		{
			hotDegree -= Math.min(hotDegree, HOT_DECAY_VEL * interpolation);
		}
	}

}
