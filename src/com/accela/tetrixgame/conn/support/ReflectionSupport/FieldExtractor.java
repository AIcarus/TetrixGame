package com.accela.tetrixgame.conn.support.ReflectionSupport;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.*;

/**
 * 
 * 这个类用来从一个Class<?>对象中抽取其自身以及其所有父类的字段。 字段以固定方法排序，每个数组装有一个类的所有字段，并且其中static
 * 字段放在非static字段后头。
 * 
 * //TODO 未经详尽测试，只是通过HPObjectStream间接验证过
 * 
 */
public class FieldExtractor {
	private static final Map<Class<?>, List<Field[]>> fieldMap = new ConcurrentHashMap<Class<?>, List<Field[]>>();

	private static final FieldComparator fieldComparator = new FieldComparator();

	/**
	 * 
	 * 排序规则：静态字段比非静态字段大，静态字段和静态字段都相等， 非静态字段和非静态字段之间用名字来按字典序比较
	 * 
	 */
	private static class FieldComparator implements Comparator<Field> {
		@Override
		public int compare(Field left, Field right) {
			boolean isLeftStatic = Modifier.isStatic(left.getModifiers());
			boolean isRightStatic = Modifier.isStatic(right.getModifiers());

			if (!isLeftStatic && isRightStatic) {
				return -1;
			} else if (isLeftStatic && !isRightStatic) {
				return 1;
			} else if (isLeftStatic && isRightStatic) {
				return 0;
			} else {
				return left.getName().compareTo(right.getName());
			}

		}
	}

	/**
	 * 抽取一个类及其父类，以及父类的父类的所有字段。返回的链表中，每个数组 都装有一个类的所有字段。后一个数组是前一个数组的父类。在一个数组内，
	 * 所有静态字段都放在非静态字段后面，非静态字段按照其名字的字典序排列， 静态字段的顺序没有保证。
	 * 
	 * 另外，返回的链表以及其中的数组都是存储在这个类中的，被所有人共享。如果 更改，则会引起错误。
	 */
	public List<Field[]> getSortedFieldsList(final Class<?> objectClass) {
		if (null == objectClass) {
			throw new NullPointerException("objectClass should not be null");
		}

		List<Field[]> fieldList = fieldMap.get(objectClass);
		if (fieldList != null) {
			assert (checkReturnListValid(fieldList));
			return fieldList;
		} else {
			// 计算并得到fieldList
			fieldList = new LinkedList<Field[]>();
			Class<?> aimClass = objectClass;
			while (aimClass != null) {
				Field[] fields = aimClass.getDeclaredFields();
				Arrays.sort(fields, fieldComparator);
				fieldList.add(fields);

				aimClass = aimClass.getSuperclass();
			}

			// 得到不可改变的表视图
			fieldList = Collections.unmodifiableList(fieldList);
			assert (fieldList != null);

			// 将新得到的fieldList记录入fieldMap，如果使用多线程，有可能重复
			// 放入相同的fieldList，不过不会引起错误
			fieldMap.put(objectClass, fieldList);

			// 检查得到的fieldList的正确性
			assert (checkReturnListValid(fieldList));

			return fieldList;
		}

	}

	private static boolean checkReturnListValid(List<Field[]> fieldList) {
		if (null == fieldList) {
			return false;
		}

		for (Field[] fields : fieldList) {
			if (null == fields) {
				return false;
			}

			boolean hasMetStatic = false;
			for (int i = 0; i < fields.length; i++) {
				final Field f = fields[i];
				if (null == f) {
					return false;
				}

				if (hasMetStatic) {
					if (!Modifier.isStatic(f.getModifiers())) {
						return false;
					}
				} else {
					if (Modifier.isStatic(f.getModifiers())) {
						hasMetStatic = true;
					} else {
						if (i > 0) {
							final Field lastF = fields[i - 1];
							if (lastF == null) {
								return false;
							}
							if (Modifier.isStatic(lastF.getModifiers())) {
								return false;
							}
							if (lastF.getName().compareTo(f.getName()) >= 0) {
								return false;
							}
						} else {
							if (hasMetStatic) {
								return false;
							}
						}
					}
				}
			}
		}// outer for

		return true;
	}

	/**
	 * FieldExtractor的创建使用了缓冲区技术，因此应该通过静态方法创建
	 */
	protected FieldExtractor() {

	}

	/**
	 * 装载字段提取器FieldExtractor对象的栈。这是为了优化FieldExtractor对象
	 * 的使用，避免频繁创建和释放FieldExtractor对象而设计。
	 */
	private static Stack<FieldExtractor> fieldExtractorStack = new Stack<FieldExtractor>();

	/**
	 * 获得一个FieldExtractor对象。通过这个方法来获得FieldExtractor 对象才能够优化它的新建。
	 */
	public synchronized static FieldExtractor getFieldExtractor() {
		if (fieldExtractorStack.isEmpty()) {
			return new FieldExtractor();
		} else {
			return fieldExtractorStack.pop();
		}
	}

	/**
	 * 释放一个FieldExtractor对象。通过这个方法来释放FieldExtractor 对象才能够优化它的释放。
	 */
	public synchronized static void disposeFieldExtractor(
			FieldExtractor fieldExtractor) {
		if (null == fieldExtractor) {
			throw new NullPointerException("fieldExtractor should not be null");
		}
		fieldExtractorStack.push(fieldExtractor);
	}

}
