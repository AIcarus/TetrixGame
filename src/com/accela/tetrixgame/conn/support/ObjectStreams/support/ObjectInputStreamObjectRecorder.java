package com.accela.tetrixgame.conn.support.ObjectStreams.support;

import java.util.*;

/**
 * 
 * 这个类给ObjectInputStreamSupport使用，是为了方便 ObjectInputStreamSupport记录读取的对象，以及处理
 * 循环引用类型的对象读写。
 * 
 */
class ObjectInputStreamObjectRecorder {
	/**
	 * 用来记录已经读入的对象。键是一个分配给对象的id号
	 */
	private final Map<Integer, Object> objectMap = new HashMap<Integer, Object>();

	/**
	 * 记录一个已经读出的对象。
	 * 
	 * @param objectId
	 *            给该对象分配的id号
	 * @param object
	 *            被记录的对象
	 */
	public void recordObject(int objectId, Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}
		if (objectId < 0) {
			throw new IllegalArgumentException("objectId should be non negtive");
		}
		if (containsObjectId(objectId)) {
			throw new IllegalArgumentException(
					"the object is already recorded, it should never be recorded more than once");
		}

		objectMap.put(objectId, object);
	}

	/**
	 * 得到一个已经被记录的对象的引用
	 * 
	 * @param objectId
	 *            该对象被分配的id号
	 * @return 对应的对象。如果找不到则抛出异常，而不是返回null
	 */
	public Object getRecordedObject(int objectId) {
		Object result = objectMap.get(objectId);
		if (null == result) {
			throw new IllegalArgumentException(
					"the object specified by objectId is not recorded");
		}

		return result;
	}

	/**
	 * 清除所有已经记录的对象
	 */
	public void clearObjectRecord() {
		objectMap.clear();
	}

	/**
	 * 测试是否记录有id号为objectId的对象
	 */
	public boolean containsObjectId(int objectId) {
		if (objectId < 0) {
			throw new IllegalArgumentException("objectId should be non negtive");
		}
		return objectMap.containsKey(objectId);
	}

}
