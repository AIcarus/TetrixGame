package com.accela.tetrixgame.conn.support.ClassIDAndInstanceID;

import java.lang.ref.WeakReference;

/**
 * 
 * InstanceID用于给一个对象实例建立一个唯一的标识符。用==验证相等的对象建立的
 * InstanceID总是equals，用==验证不相等对象建立的InstanceID总是不相等。
 * 
 * InstanceID支持多线程
 * 
 * Note: InstanceID 使用WeakReference引用目标对像，不会造成对象无法回收 Note：因为SUN JDK BUG
 * ID=6321873，InstanceID无法实现Comparable接口
 */
public class InstanceID {
	private WeakReference<?> aimInstance;
	/**
	 * 记录下对象的哈希码以备失效后使用
	 */
	private int oldHashCode = -1;
	/**
	 * 记录下对像的类以备InstanceID失效后仍能工作
	 */
	private Class<?> oldClass = null;

	private String oldString = null;

	/**
	 * 新建一个InstanceID
	 * 
	 * @param aimInstance
	 *            这个InstanceID所指向的目标对象，InstanceID 内部用弱引用指向它。
	 */
	protected <T> InstanceID(T aimInstance) {
		if (null == aimInstance) {
			throw new NullPointerException("aimInstance should not be null");
		}

		this.aimInstance = new WeakReference<T>(aimInstance);
		oldHashCode = hashCode();
		this.oldClass = aimInstance.getClass();
		oldString = aimInstance.toString();

		assert (oldHashCode >= 0);
		assert (oldClass != null);
		assert (oldString != null);
		assert (this.isIDOf(aimInstance));
		assert (isValid());

		// 如果在构造方法中就失效，则会导致麻烦的错误，所以这里要拦住
		testWeakRefValid();
	}

	/**
	 * @return 这个InstanceID所指向的目标对象是否是someInstance。
	 *         如果InstanceID内部用弱引用所存储的目标对象已经被垃圾回收， 那么一定返回false
	 */
	public boolean isIDOf(Object someInstance) {
		if (null == someInstance) {
			throw new NullPointerException("someInstance should not be null");
		}
		if (!isValid()) {
			return false;
		}

		return aimInstance.get() == someInstance;
	}

	/**
	 * @return 得到这个InstanceID所指向的目标对象的类ID 即使InstanceID内部用弱引用所存储的目标对象已经被垃圾回收，
	 *         这个方法仍能返回和目标对象生前相同的结果
	 */
	public ClassID getClassIDOfSameClass() {
		return IDDispatcher.createClassID(oldClass);
	}

	/**
	 * 判断传入的someClass所代表的类和这个InstanceID所指向的目标对象的类，是否是 同一个类。
	 * 
	 * 如果InstanceID内部用弱引用所存储的目标对象已经被垃圾回收，这个方法仍能返回 和对象生前相同的结果
	 */
	public boolean isOfSameClass(Class<?> someClass) {
		if (null == someClass) {
			throw new NullPointerException("someClass should not be null");
		}
		return someClass == oldClass;
	}

	/**
	 * 判断传入的classID所代表的类和这个InstanceID所指向的目标对象的类， 是否是同一个类
	 * 
	 * 如果InstanceID内部用弱引用所存储的目标对象已经被垃圾回收，这个方 法仍能返回和对象生前相同的结果
	 */
	public boolean isOfSameClass(ClassID classID) {
		if (null == classID) {
			throw new NullPointerException("classID should not be null");
		}
		return classID.isIDOf(oldClass);
	}

	/**
	 * @return 这个InstanceID所指向的对像的引用，但是 如果非常不凑巧，刚好在这个InstanceID检验过内部
	 *         存储的弱引用的有效性后，垃圾回收器回收了弱引用所 指的对象，则会返回null
	 * 
	 *         如果InstanceID内部用弱引用所存储的目标对象已经被 垃圾回收，这个方法将一定返回null。
	 */
	public Object getAimInstance() {
		if (!isValid()) {
			return null;
		}

		assert (aimInstance != null);
		assert (aimInstance.get() != null);
		return aimInstance.get();
	}

	/**
	 * 检验弱引用的有效性，如果已经失效，则抛出IllegalStateException异常。
	 * 
	 * @throws IllegalStateException
	 *             如果InstanceID内部 用弱引用所存储的目标对象已经被垃圾回收
	 */
	private void testWeakRefValid() {
		if (!isValid()) {
			throw new IllegalStateException(
					"the object to which this instanceID points to is already collected by gc!");
		}
	}

	/**
	 * 测试InstanceID所指向的对象是否已经被释放，即InstanceID已经无效
	 */
	public boolean isValid() {
		return aimInstance.get() != null;
	}

	/**
	 * 两个InstanceID相等，当且仅当其指向的目标对象用== 验证相等。
	 * 
	 * 如果两个相比较的InstanceID中的任何一个失效，则仅除 other==this的情况下，都会返回false
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InstanceID)) {
			return false;
		}

		InstanceID other = (InstanceID) obj;
		if (isValid() && other.isValid()) {
			boolean result = aimInstance.get() == other.aimInstance.get();
			assert (!result || this == other);
			return result;
		} else {
			if (this == other) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @return 返回System.identityHashCode(InstanceID所指向的目标对象)
	 * 
	 *         如果InstanceID已经失效，则这个方法将返回该对象生前的哈希码，以维持 哈希码不变。
	 */
	@Override
	public int hashCode() {
		if (!isValid()) {
			return oldHashCode;
		}

		// 这种取得hashCode的办法保证只要实例不同，
		// 哪怕它们equals相等，也会基本上返回不同的hashCode
		return System.identityHashCode(aimInstance.get());
	}

	/**
	 * 返回目标对象的toString()所返回的字符串，保证InstanceID失效后 仍能返回相同的结果。当然这里的相同指用equals()方法相同
	 */
	public String toString() {
		assert (oldString != null);
		return oldString;
	}

}
