package com.accela.tetrixgame.conn.support.ObjectPool;

import java.util.*;

import com.accela.tetrixgame.conn.support.ClassIDAndInstanceID.IDDispatcher;
import com.accela.tetrixgame.conn.support.ClassIDAndInstanceID.InstanceID;
import com.accela.tetrixgame.conn.support.ReflectionSupport.ObjectCleaner;

/**
 * 
 * 对象池。 对象池是为了避免在高频率地大量新建和删除对象的时候，不断 和JVM交互造成性能损耗，以及使内存中出现大量碎片。
 * 
 * 对象池的主要思想是，新建的对象在不用的时候，可以放到对像池中， 而不是释放给垃圾会收器。当需要新建对象的时候，再从对象池中来取。
 * 
 * ObjectPool和可以和接口IObjectPoolSensitive配合使用，详见 IObjectPoolSensitive
 * 
 */
public class ObjectPool {
	/**
	 * 存放被释放的对象的容器
	 */
	private static final Map<Class<?>, Stack<Object>> objectHolder = new HashMap<Class<?>, Stack<Object>>();

	private static final Set<InstanceID> instanceIDSet = new HashSet<InstanceID>();

	private static final ObjectPoolObjectCleaner objectCleaner = new ObjectPoolObjectCleaner();

	private ObjectPool() {
		throw new IllegalStateException(
				"You should never create an instance of ObjectPool");
	}

	private static boolean hasRecordOf(Object object) {
		assert (object != null);

		return instanceIDSet.contains(IDDispatcher.createInstanceID(object));
	}

	private static void recordObject(Object object) {
		assert (object != null);
		assert (!hasRecordOf(object));

		boolean result = instanceIDSet.add(IDDispatcher
				.createInstanceID(object));
		assert (result);
	}

	private static void disrecordObject(Object object) {
		assert (object != null);
		assert (hasRecordOf(object));

		boolean result = instanceIDSet.remove(IDDispatcher
				.createInstanceID(object));
		assert (result);
	}

	/**
	 * 同put(object, false);
	 */
	public static void put(Object object) {
		put(object, false);
	}

	/**
	 * 将一个对象放入对象池，如果这个对象是数组、字符串、枚举或包装类型， 那么这个方法将跳过它们，因为对象池不允许放入这些类型的对象。
	 * 
	 * @param object
	 *            被放入对象池的对象
	 * @param fullyClean
	 *            是否清空这个对象的成员变量。如果是false， 则直接把对象放入对象池；否则会把传入的对象的非static且非final
	 *            的引用型成员变量全部置为null，并且把被这个对象所引用的对象也用 put(object, true)方法放入对象池。
	 * 
	 *            这个方法能够对付循环引用类型的变量
	 * 
	 */
	public static void put(Object object, boolean fullyClean) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}
		// 测试是否加入了重复对象
		if (hasRecordOf(object)) {
			throw new IllegalArgumentException(
					"You should never put one object into object pool twice");
		}

		putImpl(object, fullyClean);

	}

	// ////////////////////////////////////////////////////////////////////////

	/**
	 * 具体负责如何放入对象的方法
	 */
	private synchronized static boolean putImpl(Object object,
			boolean fullyClean) {
		assert (object != null);

		if (fullyClean) {
			List<Object> cleanedObjects = objectCleaner.cleanObject(object);
			assert (cleanedObjects != null);
			assert (cleanedObjects.contains(object));

			boolean succ = true;
			for (Object element : cleanedObjects) {
				assert (element != null);
				if (element != null) {
					if (!putAnObject(element)) {
						succ = false;
					}
				}
			}

			return succ;
		} else {
			if (!hasRecordOf(object)) {
				if (object instanceof IObjectPoolSensitive) {
					((IObjectPoolSensitive) object).onPut();
				}
			}

			return putAnObject(object);
		}
	}

	/**
	 * 负责放入一个对象
	 */
	private static boolean putAnObject(Object object) {
		assert (object != null);

		if (hasRecordOf(object)) {
			return false;
		}

		if (object.getClass().isArray() || object.getClass().isEnum()
				|| (object.getClass() == Integer.class)
				|| (object.getClass() == Boolean.class)
				|| (object.getClass() == Double.class)
				|| (object.getClass() == Float.class)
				|| (object.getClass() == Long.class)
				|| (object.getClass() == Character.class)
				|| (object.getClass() == Short.class)
				|| (object.getClass() == Byte.class)
				|| (object.getClass() == String.class)) {
			return true;
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

		// 记录对象已经装载过
		recordObject(object);

		return true;

	}

	private static class ObjectPoolObjectCleaner extends ObjectCleaner {
		@Override
		protected void onClean(Object object) {
			super.onClean(object);
			assert (object != null);

			if (ObjectPool.hasRecordOf(object)) {
				return;
			}

			if (object instanceof IObjectPoolSensitive) {
				((IObjectPoolSensitive) object).onPut();
			}

		}

	}

	// ////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public static <T> T retrieve(Class<T> objectClass) {
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

	private synchronized static Object retrieveImpl(Class<?> objectClass) {
		assert (objectClass != null);

		Stack<Object> list = objectHolder.get(objectClass);
		if (null == list) {
			return null;
		} else {
			if (!list.isEmpty()) {
				assert (list.size() > 0);

				Object result = list.pop();

				assert (result != null);
				assert (hasRecordOf(result));

				disrecordObject(result);

				if (result instanceof IObjectPoolSensitive) {
					((IObjectPoolSensitive) result).onRetrieve();
				}

				assert (!hasRecordOf(result));

				return result;
			} else {
				return null;
			}
		}

	}

}
