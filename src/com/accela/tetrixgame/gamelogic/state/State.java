package com.accela.tetrixgame.gamelogic.state;

/**
 * 
 * 游戏的状态
 * 
 */
// TODO 画一个状态转换示意图
public enum State
{
	/**
	 * 生成随机方块的状态，只占一个周期。下一个周期是BAR_DROPPING
	 */
	GENERATING,
	/**
	 * 玩家控制Bar下落的状态，落地后结束，转到COLLIDING状态
	 */
	BAR_DROPPING,
	/**
	 * 碰撞状态，只占一个周期。这个状态实际上只是计算哪些方块应该消去。
	 * 如果有就转移到REMOVING_AND_FALLING状态，没有就转移到GENERATING 状态。
	 */
	COLLIDING,
	/**
	 * 砖块的消去、效果释放以及下落。
	 */
	REMOVING_AND_FALLING,
	/**
	 * 游戏结束状态，此时还有删除方块的动画
	 */
	GAME_OVER_REMOVING,
	/**
	 * 真正的游戏结束状态，此时没有任何动作了
	 */
	GAME_OVER,
}
