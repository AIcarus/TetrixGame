package com.accela.tetrixgame.conn.support.ObjectStreams.support;

import java.util.*;

import com.accela.tetrixgame.conn.support.ClassIDAndInstanceID.*;

public class ObjectOutputStreamObjectRecorder {
	/**
	 * 记录已经写入的对象，InstanceID代表一个对象，用来唯一地区分对象，
	 * 这里称作objectIdentifier，而Integer表示给这个对象分配的id号
	 */
	private Map<InstanceID, Integer> objectMap = new HashMap<InstanceID, Integer>();

	/**
	 * 记录一个对象
	 */
	public void recordObject(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		InstanceID objectIdentifier = IDDispatcher.createInstanceID(object);
		assert (objectIdentifier != null);
		if (objectMap.containsKey(objectIdentifier)) {
			throw new IllegalArgumentException(
					"the object is already recorded, it should never be recorded more than once");
		}

		putObject(objectIdentifier);

	}

	/**
	 * 记录一个对象，将其InstanceID放入objectMap，使用此时objectMap 的大小作为其id号
	 */
	private void putObject(InstanceID objectIdentifier) {
		assert (objectIdentifier != null);
		assert (!objectMap.containsKey(objectIdentifier));

		objectMap.put(objectIdentifier, objectMap.size());
	}

	/**
	 * @return 是否已经记录过指定的object
	 */
	public boolean hasRecordOf(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		InstanceID objectIdentifier = IDDispatcher.createInstanceID(object);
		assert (objectIdentifier != null);

		return objectMap.containsKey(objectIdentifier);
	}

	/**
	 * 得到一个已经记录过的对象的id号
	 */
	public int getObjectId(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		InstanceID objectIdentifier = IDDispatcher.createInstanceID(object);
		assert (objectIdentifier != null);
		if (!objectMap.containsKey(objectIdentifier)) {
			throw new IllegalArgumentException(
					"the object have not been recorded yet!");
		}

		Integer id = objectMap.get(objectIdentifier);
		assert (id != null);

		return id;
	}

	public void clearObjectRecord() {
		objectMap.clear();
	}

}
