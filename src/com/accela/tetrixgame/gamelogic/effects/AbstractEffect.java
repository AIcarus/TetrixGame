package com.accela.tetrixgame.gamelogic.effects;

import java.awt.Graphics;
import java.awt.Point;

/**
 * 
 * Various effects of the parent category, to provide them with some common
 * attributes and methods.
 * 
 * All of the effects of class time with millisecond units reunification;
 * coordinate system coordinate system for the swing; angle for the arc system,
 * the coordinate system in the swing from the beginning of x-axis being along
 * the clockwise rotation; the length of the use of pixel units.
 * 
 */
public abstract class AbstractEffect {
	/**
	 * Whether the results also effective. Generally speaking, one effect, such
	 * as the spark to continue for some time because of the disappeared, that
	 * is failure. At this time to identify this attribute.
	 */
	private boolean valid = true;

	/**
	 * Results in each clock cycle of processing a certain task to achieve
	 * through this method.
	 * 
	 * @param interpolation
	 *            The call and the last call time interval, millisecond time
	 */
	public void onTick(long interpolation) {
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should not be null");
		}

		onTickImpl(interpolation);
	}

	protected abstract void onTickImpl(long interpolation);

	public boolean isValid() {
		return valid;
	}

	protected void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Calling this method can be expressed by the type of graphics rendering by
	 * 
	 * @param g
	 * @param center
	 *            The positioning of the graphics, the general center of the
	 *            graphics, which is the start position of the graphics (such as
	 *            FlyingSpark).
	 */
	public void paint(final Graphics g, final Point center, double scale) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}
		if (null == center) {
			throw new NullPointerException("center should not be null");
		}
		if (scale <= 0) {
			throw new IllegalArgumentException("scale should be positive");
		}

		if (!isValid()) {
			return;
		}

		paintImpl(g, center, scale);
	}

	protected abstract void paintImpl(final Graphics g, final Point center,
			double scale);

}
