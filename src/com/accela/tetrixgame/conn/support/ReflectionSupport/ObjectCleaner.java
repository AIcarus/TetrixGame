package com.accela.tetrixgame.conn.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * 这个类能够清空一个对象的所有引用类型的字段， 以及继续递归地清空该字段所引用的对象。
 * 
 * 你可以定制一个方法，规定一个对象在被清除之前做什么
 * 
 */
public class ObjectCleaner {
	private final ObjectCleanerObjectRecorder objectRecorder = new ObjectCleanerObjectRecorder();

	private final List<Object> cleanedObjects = new LinkedList<Object>();

	private void addCleanedObject(Object object) {
		assert (object != null);
		assert (!containsCleanedObject(object));
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		cleanedObjects.add(object);
	}

	private void clearCleanedObjects() {
		assert (cleanedObjects != null);
		cleanedObjects.clear();
	}

	// 这个方法消耗大量资源，只用在断言中
	private boolean containsCleanedObject(Object object) {
		assert (object != null);

		for (Object element : cleanedObjects) {
			assert (element != null);

			if (element == object) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 递归地清理对象的方法，会把对象的所有非static且非final的引用类型的成员变量置为null，并且会递归地把所有被这个对象引用的对象也以同
	 * 样的方式清理掉。能够对付循环引用。
	 * 
	 * 另外，这个方法会自动跳过枚举类型、包装类类型、String类型的对象，不会清理它们的内部，只是把它们放到返回的表中。
	 * 这个方法对java集合类的清理进行 了优化。
	 * 
	 * @return 返回所有被清理过的对象，包括传入的object。ObjectCleaner
	 *         每次返回的表都是同一个表，而且这个表存储在ObjectCleaner中，被其使
	 *         用。不要修改返回值，或者在下一次调用cleanObject(Object)方法的时候 仍保留返回值。
	 * 
	 */
	public synchronized List<Object> cleanObject(Object object) {
		if (null == object) {
			throw new NullPointerException("object should not be null");
		}

		clearCleanedObjects();
		cleanObjectImpl(object);
		objectRecorder.clearObjectRecord();

		assert (containsCleanedObject(object));
		assert (checkReturnListValid(cleanedObjects));
		return cleanedObjects;
	}

	private void cleanObjectImpl(Object object) {
		assert (object != null);

		if (object != null && !objectRecorder.hasRecordOf(object)) {
			onClean(object);

			objectRecorder.recordObject(object);
			addCleanedObject(object);
			cleanNormalObject(object);
		}

	}

	// normal object 指即不是null，也不是objectRecorder所记录过的已经清理过的对象
	private void cleanNormalObject(Object object) {
		assert (object != null);
		assert (objectRecorder.hasRecordOf(object));
		assert (containsCleanedObject(object));

		if (object.getClass().isArray()) {
			// 清理数组
			Class<?> componentType = object.getClass().getComponentType();
			assert (componentType != null);

			if (!componentType.isPrimitive()) {
				cleanObjectArray((Object[]) object);
			}

		} else if (object.getClass().isEnum()) {
			// do nothing
		} else if (object.getClass() == Integer.class) {
			// do nothing
		} else if (object.getClass() == Boolean.class) {
			// do nothing
		} else if (object.getClass() == Double.class) {
			// do nothing
		} else if (object.getClass() == Float.class) {
			// do nothing
		} else if (object.getClass() == Long.class) {
			// do nothing
		} else if (object.getClass() == Character.class) {
			// do nothing
		} else if (object.getClass() == Short.class) {
			// do nothing
		} else if (object.getClass() == Byte.class) {
			// do nothing
		} else if (object.getClass() == String.class) {
			// do nothing
		} else {
			if (object instanceof Collection) {
				// 以集合类的方式清理对象
				cleanCollection((Collection<?>) object);
			} else if (object instanceof Map) {
				// 以Map的方式清理对象
				cleanMap((Map<?, ?>) object);
			} else {
				// 依次清理对象的字段
				cleanFields(object);
			}

		}

	}

	private void cleanObjectArray(Object[] objects) {
		assert (objects != null);

		for (int i = 0; i < objects.length; i++) {
			objects[i] = null;
			cleanObjectImpl(objects[i]);
		}
	}

	private void cleanCollection(Collection<?> collection) {
		assert (collection != null);

		for (Object element : collection) {
			cleanObjectImpl(element);
		}

		collection.clear();
	}

	private void cleanMap(Map<?, ?> map) {
		assert (map != null);

		for (Entry<?, ?> element : map.entrySet()) {
			cleanObjectImpl(element.getKey());
			cleanObjectImpl(element.getValue());
		}

		map.clear();
	}

	private void cleanFields(Object object) {
		assert (object != null);
		assert (!object.getClass().isArray());
		assert (!object.getClass().isEnum());
		assert (!(object instanceof Collection));
		assert (!(object instanceof Map));

		FieldExtractor fieldExtractor = FieldExtractor.getFieldExtractor();
		List<Field[]> fieldList = fieldExtractor.getSortedFieldsList(object
				.getClass());

		for (Field[] fields : fieldList) {
			for (Field f : fields) {
				if (Modifier.isStatic(f.getModifiers())) {
					break;
				}
				if (Modifier.isFinal(f.getModifiers())) {
					continue;
				}

				cleanField(object, f);
			}
		}

		FieldExtractor.disposeFieldExtractor(fieldExtractor);
	}

	private void cleanField(Object object, Field field) {
		assert (object != null);
		assert (field != null);
		assert (!Modifier.isStatic(field.getModifiers()));
		assert (!Modifier.isFinal(field.getModifiers()));

		Class<?> fieldType = field.getType();
		field.setAccessible(true);
		if (fieldType.isPrimitive()) {
			return;
		}

		try {
			Object fieldVal = field.get(object);
			if (fieldVal != null) {
				field.set(object, null);
				cleanObjectImpl(fieldVal);
			}
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			assert (false);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			assert (false);
		}
	}

	// //////////////////////////////////////////////////////////////////

	private static boolean checkReturnListValid(List<Object> list) {
		if (null == list) {
			return false;
		}
		if (list.size() < 1) {
			return false;
		}

		int outerIdx = 0;
		for (Object outer : list) {
			if (null == outer) {
				return false;
			}

			int innerIdx = 0;
			for (Object inner : list) {
				if (innerIdx >= outerIdx) {
					break;
				}

				if (inner == outer) {
					return false;
				}

				innerIdx++;
			}

			if (!checkFields(outer)) {
				return false;
			}

			outerIdx++;
		}

		return true;
	}

	private static boolean checkFields(Object object) {
		assert (object != null);

		if (object.getClass().isArray()) {
			Class<?> componentType = object.getClass().getComponentType();
			assert (componentType != null);

			if (!componentType.isPrimitive()) {
				for (Object element : (Object[]) object) {
					if (element != null) {
						return false;
					}
				}
			}

		} else if (object.getClass().isEnum()) {
			// do nothing
		} else {
			if (object instanceof Collection) {
				if (((Collection<?>) object).size() != 0) {
					return false;
				}

			} else if (object instanceof Map) {
				if (((Map<?, ?>) object).size() != 0) {
					return false;
				}
			} else {
				FieldExtractor fieldExtractor = FieldExtractor
						.getFieldExtractor();
				List<Field[]> fieldList = fieldExtractor
						.getSortedFieldsList(object.getClass());

				for (Field[] fields : fieldList) {
					for (Field f : fields) {
						if (Modifier.isStatic(f.getModifiers())) {
							continue;
						}
						if (Modifier.isFinal(f.getModifiers())) {
							continue;
						}

						Class<?> fieldType = f.getType();
						f.setAccessible(true);
						if (fieldType.isPrimitive()) {
							continue;
						}

						try {
							Object fieldVal = f.get(object);
							if (fieldVal != null) {
								return false;
							}
						} catch (IllegalArgumentException ex) {
							ex.printStackTrace();
							return false;
						} catch (IllegalAccessException ex) {
							ex.printStackTrace();
							return false;
						}
					}
				}

				FieldExtractor.disposeFieldExtractor(fieldExtractor);
			}

		}

		return true;
	}

	/**
	 * 
	 * 这个方法用来继承，可以定制在对象在被清理 之前做什么
	 */
	protected void onClean(Object object) {

	}

}
