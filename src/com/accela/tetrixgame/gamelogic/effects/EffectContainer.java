package com.accela.tetrixgame.gamelogic.effects;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.gamelogic.effects.fireworkUtility.FireLine;
import com.accela.tetrixgame.gamelogic.effects.fireworkUtility.FireworkAttributes;
import com.accela.tetrixgame.gamelogic.effects.fireworkUtility.Point3D;
import com.accela.tetrixgame.gamelogic.effects.fireworkUtility.Vector3D;
import com.accela.tetrixgame.gamelogic.effects.shake.Shaker;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.RunningCircle;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.Spark;
import com.accela.tetrixgame.gamelogic.effects.sparkUtilities.SparkAttributes;
import com.accela.tetrixgame.gamelogic.geometry.Point;
import com.accela.tetrixgame.util.ObjectPool;

/**
 * 所有效果的容器
 */
public class EffectContainer {
	private List<AbstractEffect> effects = new LinkedList<AbstractEffect>();

	public void createSpark(Point center, long sparkDelay, SparkAttributes attr) {
		if (null == center) {
			throw new NullPointerException("p should not be null");
		}
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}

		Spark spark = ObjectPool.retrieve(Spark.class);
		if (null == spark) {
			spark = new Spark(new java.awt.Point(center.x, center.y), attr);
		} else {
			spark.initialize(new java.awt.Point(center.x, center.y), attr);
		}
		spark.setTotalDelay(sparkDelay);
		effects.add(spark);

	}

	public void update(long interpolation) {
		if (interpolation < 0) {
			throw new IllegalArgumentException(
					"interpolation should be nonnegtive");
		}

		Iterator<AbstractEffect> itr = effects.iterator();
		AbstractEffect effect = null;
		while (itr.hasNext()) {
			effect = itr.next();
			assert (effect != null);

			effect.onTick(interpolation);
			if (!effect.isValid()) {
				itr.remove();
				ObjectPool.put(effect);
			}
		}

		hShaker.onTick(interpolation);
		vShaker.onTick(interpolation);

	}

	// //////////////////////////////////////////////////////////////////////////

	public void createRunningCircle(Point center) {
		if (null == center) {
			throw new NullPointerException("center should not be null");
		}

		RunningCircle circle = ObjectPool.retrieve(RunningCircle.class);
		if (null == circle) {
			circle = new RunningCircle(new java.awt.Point(center.x, center.y));
		} else {
			circle.initialize(new java.awt.Point(center.x, center.y));
		}

		effects.add(circle);
	}

	// ////////////////////////////////////////////////////////////////////////

	/**
	 * 火花效果是基于3D的，需要一个平面来投影生成2D图像
	 */
	public void createBallFirework(Point center, FireworkAttributes attr,
			int rightBound, int downBound) {
		if (null == center) {
			throw new NullPointerException("center should not be null");
		}
		if (null == attr) {
			throw new NullPointerException("attr should not be null");
		}

		double planeAngle = Math.PI / 6 - Math.PI / 3 * Math.random();
		double elevation = Math.PI / 2 * Math.random();
		Vector3D arrow = Vector3D.create(
				Math.cos(elevation) * Math.sin(planeAngle),
				-Math.sin(elevation),
				Math.cos(elevation) * Math.cos(planeAngle));

		double vel = Double.parseDouble(attr
				.get(FireworkAttributes.FIRE_LINE_VEL));
		Point3D center3D = Point3D.create(center.x, center.y, 0);

		createBallFireworkImpl(
				center3D,
				vel,
				Double.parseDouble(attr
						.get(FireworkAttributes.FIRE_LINE_GRAVITY)),
				Integer.parseInt(attr.get(FireworkAttributes.FIRE_LINE_LENGTH)),
				Integer.parseInt(attr
						.get(FireworkAttributes.FIRE_LINE_LAYER_NUM)),
				Integer.parseInt(attr
						.get(FireworkAttributes.FIRE_LINE_NUM_PER_LAYER)),
				arrow, rightBound, downBound);

		Point3D.dispose(center3D);
		Vector3D.dispose(arrow);

	}

	private void createBallFireworkImpl(Point3D center, double vel,
			double gravity, int length, int layerNum, int numPerLayer,
			Vector3D arrow, int rightBound, int downBound) {
		Vector3D temp = Vector3D.create();

		Vector3D I = Vector3D.create(-arrow.z, 0, arrow.x);

		Vector3D J = Vector3D.create();
		Vector3D.outerProduct(temp, arrow, I);
		temp.toUnitVector(J);
		Vector3D.dispose(temp);

		Vector3D K = Vector3D.create();
		arrow.toUnitVector(K);

		for (int layer = 1; layer < layerNum + 1; layer++) {
			double elevation = (layer * Math.PI / 2) / (double) (layerNum + 1);
			for (int num = 0; num < numPerLayer; num++) {
				double planeAngle = ((double) num / (double) numPerLayer)
						* Math.PI * 2;

				// 计算火花发射方向
				double randomVel = vel;

				double velK = randomVel * Math.sin(elevation);
				double velI = randomVel * Math.cos(elevation)
						* Math.cos(planeAngle);
				double velJ = randomVel * Math.cos(elevation)
						* Math.sin(planeAngle);

				Vector3D tempI = Vector3D.create();
				Vector3D.mutiply(tempI, I, velI);

				Vector3D tempJ = Vector3D.create();
				Vector3D.mutiply(tempJ, J, velJ);

				Vector3D tempK = Vector3D.create();
				Vector3D.mutiply(tempK, K, velK);

				Vector3D add = Vector3D.create();

				Vector3D.addition(add, tempI, tempJ);

				Vector3D vel3D = Vector3D.create();
				Vector3D.addition(vel3D, add, tempK);

				Vector3D.dispose(tempI);
				Vector3D.dispose(tempJ);
				Vector3D.dispose(tempK);
				Vector3D.dispose(add);

				// 生成火花
				FireLine fireLine = ObjectPool.retrieve(FireLine.class);
				if (fireLine != null) {
					fireLine.initialize(vel3D, center, gravity, length,
							Color.YELLOW, Color.WHITE, rightBound, downBound);
				} else {
					fireLine = new FireLine(vel3D, center, gravity, length,
							Color.YELLOW, Color.WHITE, rightBound, downBound);
				}

				Vector3D.dispose(vel3D);

				// 加入火花
				effects.add(fireLine);
			}
		}

		Vector3D.dispose(I);
		Vector3D.dispose(J);
		Vector3D.dispose(K);

	}

	// //////////////////////////////////////////////////////////////////////////

	private Shaker hShaker = new Shaker();

	private Shaker vShaker = new Shaker();

	public void setShaker(int maxOffset, long period, int numPeriod) {
		hShaker.initialize(maxOffset, period, numPeriod);
		vShaker.initialize(maxOffset / 2, period / 2, numPeriod);
	}

	public double getHShakeOffset() {
		return hShaker.getShakeOffset();
	}

	public double getVShakeOffset() {
		return vShaker.getShakeOffset();
	}

	public boolean isEmpty() {
		return effects.isEmpty();
	}

	// /////////////////////////////////////////////////////////////////////////

	private java.awt.Point newOrign = new java.awt.Point();

	public void paintAll(Graphics g, java.awt.Point orign, double scale) {
		if (null == g) {
			throw new NullPointerException("g should not be null");
		}
		if (null == orign) {
			throw new NullPointerException();
		}
		if (scale <= 0) {
			throw new IllegalArgumentException("scale should be positive");
		}

		newOrign.x = orign.x + (int) hShaker.getShakeOffset();
		newOrign.y = orign.y + (int) vShaker.getShakeOffset();

		for (AbstractEffect effect : effects) {
			assert (effect != null);

			effect.paint(g, newOrign, scale);
		}

	}
}
