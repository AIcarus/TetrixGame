package com.accela.tetrixgame.conn.support.ClassIDAndInstanceID;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import com.accela.tetrixgame.conn.support.ObjectPool.ObjectPool;

/**
 * 
 * ClassID和InstanceID应该通过IDDispatcher来创建。 IDDispatcher保证
 * 1、对每一个不同的类所创建的ClassID不超过一个。 2、对每一个不同的对象所创建的InstanceID不超过一个。这里的不同是指用==验证不同。
 * 用==验证不同，但是用equals方法验证相等的对象，可以建立不同的InstanceID，而且所 建立的InstanceID不同
 * 
 * IDDispatcher支持多线程
 */
public class IDDispatcher {
	/**
	 * 记录已经建立过的ClassID
	 */
	private static Map<Class<?>, ClassID> classIDMap = new HashMap<Class<?>, ClassID>();

	/**
	 * 记录已经建立过的InstanceID
	 */
	private static Map<Integer, List<InstanceID>> instanceIDMap = new HashMap<Integer, List<InstanceID>>();

	private static ReentrantLock classIDLock = new ReentrantLock();

	private static ReentrantLock instanceIDLock = new ReentrantLock();

	/**
	 * 创建ClassID
	 * 
	 * @param aimClass
	 *            新建的ClassID所指向的目标类
	 */
	public static ClassID createClassID(Class<?> aimClass) {
		if (null == aimClass) {
			throw new NullPointerException("aimClass should not be null");
		}

		classIDLock.lock();

		try {
			ClassID classID = findOldClassID(aimClass);
			if (null == classID) {
				classID = new ClassID(aimClass);
				recordClassID(aimClass, classID);
			}

			assert (classID != null);
			return classID;
		} finally {
			classIDLock.unlock();
		}
	}

	/**
	 * 创建ClassID
	 * 
	 * @param aimClass
	 *            新建的ClassID所指向的目标类的类名
	 * @throws ClassNotFoundException
	 *             如果类名所指定的类并不存在
	 */
	public static ClassID createClassID(String className)
			throws ClassNotFoundException {
		if (null == className) {
			throw new NullPointerException("className should not be null");
		}

		return createClassID(Class.forName(className));
	}

	/**
	 * 新建InstanceID
	 * 
	 * @param aimInstance
	 *            新建的InstanceID所指向的对象
	 */
	public static InstanceID createInstanceID(Object aimInstance) {
		if (null == aimInstance) {
			throw new NullPointerException("aimInstance should not be null");
		}

		instanceIDLock.lock();
		try {

			InstanceID instanceID = findOldInstanceID(aimInstance);
			if (null == instanceID) {
				instanceID = new InstanceID(aimInstance);
				recordInstanceID(aimInstance, instanceID);

				/*
				 * assert(instanceID.isValid());
				 * 
				 * 本来我想加上这条语句的，但是我发现当加上它的时候，运行 for (int i = 0; i <
				 * 100000000;i++) { IDDispatcher.createInstanceID(newObject());
				 * } 的时候就会出错，几经排查，也没查出准确原因，就好像是JVM 在还有强引用的时候就吃掉了instanceID指向的对象
				 */
			}

			assert (instanceID != null);
			return instanceID;
		} finally {
			instanceIDLock.unlock();
		}
	}

	private static ClassID findOldClassID(Class<?> aimClass) {
		assert (aimClass != null);

		ClassID classID = classIDMap.get(aimClass);
		assert (null == classID || classID.isIDOf(aimClass));

		return classID;
	}

	private static void recordClassID(Class<?> aimClass, ClassID classID) {
		assert (aimClass != null);
		assert (classID != null);
		assert (classID.isIDOf(aimClass));

		ClassID lastVal = classIDMap.put(aimClass, classID);
		assert (null == lastVal);
	}

	private static InstanceID findOldInstanceID(Object aimInstance) {
		assert (aimInstance != null);

		List<InstanceID> list = instanceIDMap.get(getInstanceCode(aimInstance));
		if (null == list) {
			return null;
		} else {
			assert (list.size() > 0);

			boolean meetInvalidID = false;
			InstanceID result = null;
			for (InstanceID id : list) {
				assert (list != null);

				if (id.isValid()) {
					if (id.isIDOf(aimInstance)) {
						result = id;
						break;
					}
				} else {
					meetInvalidID = true;
				}
			}

			// 清理instanceIDMap()
			if (meetInvalidID) {
				cleanInstanceIDMap();
			}

			return result;
		}
	}

	private static void recordInstanceID(Object aimInstance,
			InstanceID instanceID) {
		assert (aimInstance != null);
		assert (instanceID != null);
		assert (instanceID.isIDOf(aimInstance));

		List<InstanceID> list = instanceIDMap.get(getInstanceCode(aimInstance));
		if (null == list) {
			list = new LinkedList<InstanceID>();
			list.add(instanceID);
			instanceIDMap.put(getInstanceCode(aimInstance), list);
		} else {
			assert (list.size() > 0);
			assert (!list.contains(instanceID));
			list.add(instanceID);
		}
	}

	private static int getInstanceCode(Object instance) {
		assert (instance != null);
		return System.identityHashCode(instance);
	}

	/**
	 * 这个方法搜索整个instanceIDMap，清除所有无效的instanceID
	 */
	@SuppressWarnings("unchecked")
	private static void cleanInstanceIDMap() {
		List<Integer> keysToBeRemoved = (LinkedList<Integer>) ObjectPool
				.retrieve(LinkedList.class);
		if (null == keysToBeRemoved) {
			keysToBeRemoved = new LinkedList<Integer>();
		} else {
			keysToBeRemoved.clear();
		}

		for (Integer key : instanceIDMap.keySet()) {
			assert (key != null);
			List<InstanceID> list = instanceIDMap.get(key);

			assert (list != null);
			boolean meetInvalidID = false;
			do {
				meetInvalidID = false;

				int index = 0;
				for (InstanceID id : list) {
					assert (id != null);

					if (!id.isValid()) {
						InstanceID removedID = list.remove(index);
						assert (!removedID.isValid());
						meetInvalidID = true;
						break;
					}

					index++;
				}

			} while (meetInvalidID);

			if (list.size() <= 0) {
				keysToBeRemoved.add(key);
			}

		}

		for (Integer key : keysToBeRemoved) {
			assert (key != null);
			List<InstanceID> removedList = instanceIDMap.remove(key);
			assert (removedList != null);
			assert (removedList.size() == 0);
		}

		ObjectPool.put(keysToBeRemoved);

		// //////////////////////////////////////////////////////////

		// 正确性校验
		/*
		 * for(List<InstanceID> list : instanceIDMap.values()) {
		 * assert(list!=null); assert(list.size()>0); }
		 */

	}

}
