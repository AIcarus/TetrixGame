package com.accela.tetrixgame.conn.support.ClassIDAndInstanceID;

/**
 * 
 * ClassID用来给类建立唯一的标识符，不同的类所建立的ClassID用equals验证一定不等，
 * 相同的类所建立的ClassID用equals验证全都相等
 * 
 * 
 * ClassID支持多线程
 */
public class ClassID implements Comparable<ClassID> {
	private Class<?> aimClass;

	/**
	 * 建立一个ClassID对像
	 * 
	 * @param aimClass
	 *            这个ClassID对象所指向的目标类
	 */
	protected ClassID(Class<?> aimClass) {
		if (null == aimClass) {
			throw new NullPointerException("aimClass should not be null");
		}

		this.aimClass = aimClass;
		assert (isIDOf(aimClass));

	}

	/**
	 * 判断这个ClassID所指向的目标类是否是someClass
	 */
	public boolean isIDOf(Class<?> someClass) {
		if (null == someClass) {
			throw new NullPointerException("someClass should not be null");
		}

		return aimClass == someClass;
	}

	/**
	 * 判断这个ClassID所指向的目标类是否是someInstance的类
	 */
	public boolean isOfSameClass(Object someInstance) {
		if (null == someInstance) {
			throw new NullPointerException("someInstance should not be null");
		}

		return someInstance.getClass() == aimClass;
	}

	/**
	 * 判断这个ClassID所指向的目标类是否是instanceID的 所指向的对象的类
	 * 
	 * @throws IllegalStateException
	 *             如果instanceID内部 用弱引用所存储的目标对象已经被垃圾回收
	 */
	public boolean isOfSameClass(InstanceID instanceID) {
		if (null == instanceID) {
			throw new NullPointerException("instanceID should not be null");
		}

		return instanceID.isOfSameClass(this);
	}

	/**
	 * 返回这个ClassID所指向的目标类
	 */
	public Class<?> getAimClass() {
		assert (aimClass != null);
		return aimClass;
	}

	/**
	 * 当且仅当两个ClassID所指向的目标类用 ==验证相等，这两个ClassID才相等
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassID)) {
			return false;
		}

		ClassID other = (ClassID) obj;

		return compareTo(other) == 0;
	}

	/**
	 * @return 这个ClassID所指向的目标类的hashCode()方法
	 */
	@Override
	public int hashCode() {
		return aimClass.hashCode();
	}

	/**
	 * 比较这个ClassID所指向的目标类的类名和 other所指向的目标类的类名
	 */
	@Override
	public int compareTo(ClassID other) {
		if (null == other) {
			return 1;
		}

		int result = aimClass.getName().compareTo(other.aimClass.getName());
		assert (result != 0 || this.aimClass == other.aimClass);
		assert (result != 0 || this == other); // IDDispatcher的分配机制应该保证，相等的ClassID的实例一定只有一个
		return result;
	}

	/**
	 * 返回目标类的Class<?>对象的toString()方法返回的字符串
	 */
	public String toString() {
		assert (aimClass != null);
		return aimClass.toString();
	}

}
