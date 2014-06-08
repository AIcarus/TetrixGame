package com.accela.tetrixgame.conn.support.ReflectionSupport;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.conn.support.ObjectPool.ObjectPool;

/**
 * 
 * 这个类使用ParameterGenerator来完成使用反射测试一个类的构造函数， 并新建对象的工作。
 * 
 * ObjectCreator会自动记住构造函数列表、能够成功新建对像的构造函数，以及 能够成功新建对象的参数列表 //TODO
 * 新加上的缓冲区功能都没有经过详尽的测试 //TODO 未经详尽测试，只是通过HPObjectStreams间接验证过
 */
public class ObjectCreator {
	/**
	 * 用来抽取构造函数
	 */
	private static final ConstructorExtractor constructorExtractor = new ConstructorExtractor();
	/**
	 * 用来记录以前找到的，能够成功新建对象的构造函数。 下次再需要新建用样的对象的时候，就可以直接使用了。
	 */
	private static final Map<Class<?>, Constructor<?>> hitConstructorMap = new ConcurrentHashMap<Class<?>, Constructor<?>>();
	/**
	 * 记录一组可以成功使构造函数新建出对像的参数。 下次再遇到同样的构造函数的时候，就可以直接使用了。
	 */
	private static final Map<Constructor<?>, Object[]> hitParamMap = new ConcurrentHashMap<Constructor<?>, Object[]>();

	private static final CaseObjectCreator caseCreator = new CaseObjectCreator();

	/**
	 * 尽力测试objectClass的构造函数，试图找到一个可以用的构造函数并新建对像。 如果实在找不到，则返回null。
	 * 
	 * //TODO 有一个BUG，如果构造函数中的参数类型存在循环，就会使得ObjectCreator卡死 //TODO
	 * 但是这种情况应该很少见，毕竟此时应该有其他构造函数可以使用，它们应该被优先选中
	 */
	public Object createObject(Class<?> objectClass) {
		if (null == objectClass) {
			throw new NullPointerException("objectClass should not be null");
		}

		// 尝试使用CaseObjectCreator创建
		Object caseRet = caseCreator.createInstance(objectClass);
		if (caseRet != null) {
			assert (caseRet.getClass() == objectClass);
			return caseRet;
		}

		// 尝试从对象池中寻找
		Object result = ObjectPool.retrieve(objectClass);
		if (result != null) {
			assert (result.getClass() == objectClass);
			return result;
		}

		// 尝试旧的存储的构造函数
		Constructor<?> oldConstructor = hitConstructorMap.get(objectClass);
		if (oldConstructor != null) {
			Object newInstance = tryConstructor(oldConstructor);

			if (newInstance != null) {
				return newInstance;
			} else {
				// 这种情况不应该出现，因此用assert(false)。但是如果关掉异常，
				// 这里通过后就可以重新用遍历构造函数的方法来尝试，增加健壮性。
				assert (false);
			}
		}

		// 遍历构造函数，依次尝试
		Constructor<?>[] constructors = constructorExtractor
				.getConstructors(objectClass);

		Object newInstance = null;
		for (int i = 0; i < constructors.length; i++) {
			try {
				newInstance = tryConstructor(constructors[i]);
				if (newInstance != null) {
					assert (newInstance.getClass() == objectClass);

					// 记录成功试出结果的构造函数
					hitConstructorMap.put(objectClass, constructors[i]);

					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return newInstance;
	}

	/**
	 * 用各种参数试探一个构造函数，试图新建对象，如果所有尝试 都失败了，则返回null
	 * 
	 * @param constructor
	 * @return
	 */
	private Object tryConstructor(final Constructor<?> constructor) {
		Object result = null;
		constructor.setAccessible(true);

		// 从旧有的记录中尝试能否成功
		Object[] oldParams = hitParamMap.get(constructor);
		if (oldParams != null) {
			try {
				result = constructor.newInstance(oldParams);
				assert (result != null);

				if (result != null) {
					return result;
				}
			} catch (Exception ex) {
				System.err.println("constructor is : " + constructor);
				ex.printStackTrace();
				assert (false);
			}
		}

		// 尝试遍历构造函数的参数的各种可能，来创建新的对象

		Class<?>[] parameterTypes = constructor.getParameterTypes();
		ParameterGenerator[] parameterGenerators = new ParameterGenerator[parameterTypes.length];
		for (int i = 0; i < parameterGenerators.length; i++) {
			parameterGenerators[i] = getParameterGenerator();
		}

		Object[] parameters = new Object[parameterTypes.length];

		int fillingIdx = 0;
		while (fillingIdx >= 0) {
			if (fillingIdx < parameters.length) {
				if (parameterGenerators[fillingIdx].hasNext()) {
					parameters[fillingIdx] = parameterGenerators[fillingIdx]
							.createParameter(parameterTypes[fillingIdx], this);
					fillingIdx++;
				} else {
					parameterGenerators[fillingIdx].reset();
					fillingIdx--;
				}
			} else {
				try {
					result = constructor.newInstance(parameters);
					assert (result != null);

					// 记录成功试出结果的参数
					hitParamMap.put(constructor, parameters);

					break;
				} catch (Exception ex) {
					fillingIdx--;
				}
			}
		}

		for (ParameterGenerator ppg : parameterGenerators) {
			disposeParameterGenerator(ppg);
		}

		return result;
	}

	/**
	 * 与ppgStack配合，取得一个ParameterGenerator。使用这个
	 * 方法来取ParameterGenerator对象才能完成ParameterGenerator 的使用优化。
	 * 
	 * @return 一个ParameterStack对象
	 */
	private ParameterGenerator getParameterGenerator() {
		return ParameterGenerator.getParameterGenerator();
	}

	/**
	 * 当用完了一个ParameterGenerator对象后，应该通过这个方法
	 * 来释放，这样才能使ParameterGenererator对象被重复利用。
	 * 
	 * @param ppg
	 *            要释放的ParameterGenerator对象
	 */
	private void disposeParameterGenerator(ParameterGenerator ppg) {
		if (null == ppg) {
			throw new NullPointerException("ppg should not be null");
		}

		ParameterGenerator.disposeParameterGenerator(ppg);
	}

}
