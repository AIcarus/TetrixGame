package com.accela.tetrixgame.conn.support.ReflectionSupport;

import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;

/**
 * 
 * 这个类用来提取一个类的构造函数，提取出来的构造函数会按照 一定的顺序排列。但不能保证每次的顺序完全相同。
 * 
 */
public class ConstructorExtractor {
	/**
	 * 给构造函数排序，含有对象类型的参数越少，就会认为越小。如果所含对象类型 的个数一样多，则参数越少的构造函数排列越小。
	 */
	private static final Comparator<Constructor<?>> constructorComparator = new Comparator<Constructor<?>>() {
		@Override
		public int compare(Constructor<?> left, Constructor<?> right) {
			int leftCount = 0;
			int rightCount = 0;
			Class<?>[] leftTypes = left.getParameterTypes();
			Class<?>[] rightTypes = right.getParameterTypes();

			// 统计非基本类型的参数的个数
			for (int i = 0; i < leftTypes.length; i++) {
				if (!leftTypes[i].isPrimitive()) {
					leftCount++;
				}
			}
			for (int i = 0; i < rightTypes.length; i++) {
				if (!rightTypes[i].isPrimitive()) {
					rightCount++;
				}
			}

			// 第一次比较，找出含非基本类型的参数更少的构造函数
			if (leftCount != rightCount) {
				return leftCount - rightCount;
			}

			// 第二次比较，找出含参数个数更少的构造函数
			return leftTypes.length - rightTypes.length;
		}

	};

	private static final Map<Class<?>, Constructor<?>[]> constructorMap = new ConcurrentHashMap<Class<?>, Constructor<?>[]>();

	/**
	 * 利用过去的得到的构造函数数组，得到传入参数objectClass的构造函数列表。 构造函数的排列顺序，
	 * 第一参考：含有对象类型的参数越少，就会排列得越靠前 第二参考：参数的个数越少，就会排列得越靠前 注意这不能保证每次的顺序完全相同。
	 * 
	 * 另外，不要更改返回的数组，这个数组存储在这个类中，被所有人共享。如果更改， 则可能引起错误。
	 */
	public Constructor<?>[] getConstructors(Class<?> objectClass) {
		assert (objectClass != null);

		Constructor<?>[] constructors = constructorMap.get(objectClass);
		if (constructors != null) {
			assert (checkReturnConstructorsValid(constructors));
			return constructors;
		} else {
			constructors = objectClass.getDeclaredConstructors();

			Arrays.sort(constructors, constructorComparator);
			assert (checkReturnConstructorsValid(constructors));

			constructorMap.put(objectClass, constructors);

			return constructors;
		}

	}

	/**
	 * 此方法用来检查得到的构造函数列表的正确性。
	 */
	private boolean checkReturnConstructorsValid(Constructor<?>[] constructors) {
		if (null == constructors) {
			return false;
		}

		// 检查是否有null元素或者两个相等的元素
		for (Constructor<?> c : constructors) {
			if (null == c) {
				return false;
			}

			for (Constructor<?> inner_c : constructors) {
				if (inner_c != c && inner_c.equals(c)) {
					return false;
				}
			}
		}

		// 检查排列顺序
		int outerIdx = 0;
		for (Constructor<?> outer : constructors) {
			// 找出outer的传入参数中基本类型参数的个数
			int outerPrimitiveCount = 0;
			for (Class<?> param : outer.getParameterTypes()) {
				if (param.isPrimitive()) {
					outerPrimitiveCount++;
				}
			}

			int innerIdx = 0;
			for (Constructor<?> inner : constructors) {
				if (innerIdx <= outerIdx) {
					continue;
				}

				// 找出inner的传入参数中基本类型参数的个数
				int innerPrimitiveCount = 0;
				for (Class<?> param : inner.getParameterTypes()) {
					if (param.isPrimitive()) {
						innerPrimitiveCount++;
					}
				}

				if (outer.getParameterTypes().length - outerPrimitiveCount > inner
						.getParameterTypes().length - innerPrimitiveCount) {
					return false;
				} else if (outer.getParameterTypes().length
						- outerPrimitiveCount < inner.getParameterTypes().length
						- innerPrimitiveCount) {
					// pass
				} else {
					if (outer.getParameterTypes().length > inner
							.getParameterTypes().length) {
						return false;
					} else {
						// pass
					}
				}

				innerIdx++;
			}

			outerIdx++;
		}

		// 返回
		return true;

	}

}
