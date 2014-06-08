package com.accela.tetrixgame.util;

import java.util.*;

/**
 * 
 * 为了方便ColumnGame使用而实现的极其简单的对象池。这个对象池支持线程同步
 * 
 */
public class ObjectPool {
	private static final Map<Class<?>, Stack<Object>> objectHolder = new HashMap<Class<?>, Stack<Object>>();

	private ObjectPool() {
		throw new IllegalStateException(
				"You should never create an instance of ObjectPool");
	}

	public synchronized static void put(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		// 将对象装入池中
		Stack<Object> list = objectHolder.get(object.getClass());
		if (null == list) {
			list = new Stack<Object>();
			list.add(object);
			objectHolder.put(object.getClass(), list);
		} else {
			list.add(object);
		}
	}

	// ////////////////////////////////////////////////////////////////////////

	// TODO 对象从ObjectPool中取出后，千万不要忘了将对象内部清空成刚刚新建对象的样子！！
	@SuppressWarnings("unchecked")
	public synchronized static <T> T retrieve(Class<T> objectClass) {
		if (null == objectClass) {
			throw new NullPointerException("objectClass should not be null");
		}

		Object result = retrieveImpl((Class<?>) objectClass);

		T typedResult = null;

		if (result != null) {
			try {
				typedResult = (T) result;
			} catch (Exception ex) {
				ex.printStackTrace();
				assert (false);
			}
		} else {
			typedResult = null;
		}

		return typedResult;
	}

	private static Object retrieveImpl(Class<?> objectClass) {
		assert (objectClass != null);

		Stack<Object> list = objectHolder.get(objectClass);
		if (null == list) {
			return null;
		} else {
			if (!list.isEmpty()) {
				assert (list.size() > 0);
				Object result = list.pop();
				assert (result != null);
				return result;
			} else {
				return null;
			}
		}

	}

}
