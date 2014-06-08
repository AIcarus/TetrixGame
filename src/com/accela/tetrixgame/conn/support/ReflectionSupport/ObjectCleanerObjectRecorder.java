package com.accela.tetrixgame.conn.support.ReflectionSupport;

import java.util.*;

import com.accela.tetrixgame.conn.support.ClassIDAndInstanceID.IDDispatcher;
import com.accela.tetrixgame.conn.support.ClassIDAndInstanceID.InstanceID;

/**
 * 
 * 这个类给ObjectCleaner使用，用来记录已经被清理过的对象
 * 
 */
class ObjectCleanerObjectRecorder {
	/**
	 * 记录已经清理过的对象，InstanceID代表一个对象，用来唯一地区分对象，
	 */
	private Set<InstanceID> objectSet = new HashSet<InstanceID>();

	/**
	 * 记录一个对象
	 */
	public void recordObject(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		InstanceID objectId = IDDispatcher.createInstanceID(object);
		assert (objectId != null);

		if (objectSet.contains(objectId)) {
			throw new IllegalArgumentException(
					"the object is already recorded, it should never be recorded more than once");
		}

		putObject(objectId);

	}

	/**
	 * 记录一个对象，将其InstanceID放入objectSet
	 */
	private void putObject(InstanceID objectId) {
		assert (objectId != null);
		assert (!objectSet.contains(objectId));

		objectSet.add(objectId);
	}

	/**
	 * @return 是否已经记录过指定的object
	 */
	public boolean hasRecordOf(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		InstanceID objectId = IDDispatcher.createInstanceID(object);
		assert (objectId != null);

		return objectSet.contains(objectId);
	}

	public void clearObjectRecord() {
		objectSet.clear();
	}

}
