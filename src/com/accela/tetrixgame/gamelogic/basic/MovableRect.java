package com.accela.tetrixgame.gamelogic.basic;

import java.util.Collection;

import com.accela.tetrixgame.gamelogic.control.HerizontalStatus;
import com.accela.tetrixgame.gamelogic.control.VerticalStatus;
import com.accela.tetrixgame.gamelogic.geometry.Grid;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.gamelogic.geometry.Rectangle;
import com.accela.tetrixgame.gamelogic.rectMover.herizontal.HerizontalCollisionMovingProxy;
import com.accela.tetrixgame.gamelogic.rectMover.vertical.VerticalCollisionMovingProxy;

/**
 * 
 * 可以移动的矩形。矩形的移动非常复杂，水平方向上是正弦运动， 还得处理网格吸附。竖直方向上加速的时候也是正弦加速。因此，
 * 方块的移动由外置的移动代理proxy来解决
 * 
 */
public class MovableRect extends Rectangle {
	private HerizontalCollisionMovingProxy hProxy;

	private VerticalCollisionMovingProxy vProxy;

	private VelocityRecorder velRecorder = new VelocityRecorder();

	// 子类决定在适当的时候刷新速度记录，从而使getHerizontalVel和getVerticalVel方法可以获得速度
	protected void refreshVelRecorder(long interpolation) {
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		Point pos = Point.create(x, y);
		velRecorder.updatePoint(pos, interpolation);
		Point.dispose(pos);
	}

	protected void resetVelRecorder() {
		velRecorder.reset();
	}

	public MovableRect(int x, int y, int width, int height,
			MovingAttributes moveAttributes) {
		super(x, y, width, height);

		init(moveAttributes);
	}

	private void init(MovingAttributes moveAttributes) {
		assert (moveAttributes != null);

		hProxy = new HerizontalCollisionMovingProxy(
				Long.parseLong(moveAttributes
						.get(MovingAttributes.H_MAX_ACC_TIME)),
				Double.parseDouble(moveAttributes
						.get(MovingAttributes.H_MAX_VEL)));
		vProxy = new VerticalCollisionMovingProxy(
				Long.parseLong(moveAttributes
						.get(MovingAttributes.V_MAX_ACC_TIME)),
				Double.parseDouble(moveAttributes
						.get(MovingAttributes.V_NORMAL_VEL)),
				Double.parseDouble(moveAttributes
						.get(MovingAttributes.V_MAX_VEL)),
				Double.parseDouble(moveAttributes.get(MovingAttributes.GRAVITY)));
	}

	public void resetMovingProxy() {
		hProxy.reset();
		vProxy.reset();
	}

	protected boolean update(HerizontalStatus status,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int rightBound, long interpolation) {
		boolean result = hProxy.update(status, this, otherRects, grid,
				rightBound, interpolation);
		if (result) {
			hProxy.reset();
		}

		return result;
	}

	protected boolean update(VerticalStatus status,
			Collection<? extends Rectangle> otherRects, Grid grid,
			int downBound, long interpolation) {
		return vProxy.update(status, this, otherRects, grid, downBound,
				interpolation);
	}

	public double getHerizontalVel() {
		return velRecorder.getHVel();
	}

	public double getVerticalVel() {
		return velRecorder.getVVel();
	}

	public void switchToGravityVerticalVel(double initVel) {
		vProxy.switchToGravity(initVel);
	}

	public void switchToNormalVerticalVel() {
		vProxy.switchToNormal();
	}

}
