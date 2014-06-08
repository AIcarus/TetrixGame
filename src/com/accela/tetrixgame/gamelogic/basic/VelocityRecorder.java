package com.accela.tetrixgame.gamelogic.basic;

import com.accela.tetrixgame.gamelogic.geometry.Point;

/**
 * 
 * 这个类用来记录和计算方块的移动速度。
 * 
 * 方块移动由移动代理完成，如果直接计算速度，会很复杂。 但是这个类绕过移动代理，直接通过记录方块的坐标来计算 其移动速度。
 * 
 */
public class VelocityRecorder {
	private Point lastPos;

	private double hVel = 0;

	private double vVel = 0;

	private static final int RECORD_INTERVAL = 10;

	private int recordIntervalCounter = RECORD_INTERVAL;

	public void updatePoint(Point curPos, long interpolation) {
		if (null == curPos) {
			throw new NullPointerException("curPos should not be null");
		}
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		recordIntervalCounter += interpolation;
		if (recordIntervalCounter < RECORD_INTERVAL) {
			return;
		} else {

			if (null == lastPos) {
				lastPos = Point.create();
				lastPos.x = curPos.x;
				lastPos.y = curPos.y;

				hVel = 0;
				vVel = 0;
			} else {
				if (0 == interpolation) {
					lastPos.x = curPos.x;
					lastPos.y = curPos.y;
				} else {
					hVel = (double) (curPos.x - lastPos.x)
							/ (double) recordIntervalCounter;
					vVel = (double) (curPos.y - lastPos.y)
							/ (double) recordIntervalCounter;

					lastPos.x = curPos.x;
					lastPos.y = curPos.y;
				}
			}

			recordIntervalCounter = 0;
		}

	}

	public double getHVel() {
		return hVel;
	}

	public double getVVel() {
		return vVel;
	}

	public void reset() {
		lastPos = null;
		hVel = 0;
		vVel = 0;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if (lastPos != null) {
			Point.dispose(lastPos);
		}
	}

}
