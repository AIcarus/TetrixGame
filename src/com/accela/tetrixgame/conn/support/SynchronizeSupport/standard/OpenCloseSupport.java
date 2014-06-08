package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.other.SwitchLock;

/**
 * 
 * ================================================================
 * 首先我提出我所定义的可开关组件的概念： 1、可开关组件拥有open方法和close方法，它们叫做开关方法。以及一些普通的方法
 * 2、开关方法之间只能串行执行，普通方法之间可以并行执行 3、当开关方法执行的时候，普通方法不能执行，即线程不能进入其中
 * 4、如果在有线程滞留在普通方法中的时候，调用开关方法，那么滞留在普通方法中
 * 的线程会被InterruptedException或其他异常赶出来。然后线程才能进入和执行 开关方法。
 * 5、当组件处于未打开或者关闭的状态中时，多数方法应该相应地禁止线程进入。并且， 此种状态下，组件中必须没有任何一个方法可能使线程阻塞。
 * 
 * 满足这5条的组件，可以相互拼接、聚合，构成一个更大的可开关组件。 第5条很重要，设想一个组件A，其类中有包含了A1、A2、A3三个组件。
 * 当A关闭的时候，会关闭A1、A2、A3，只有当A1、A2、A3都能够保证在关 闭的状态下，线程一定不会阻塞在里面的时候，A才能在不需要了解A1、
 * A2、A3的内部构造的情况下，保证它自己也能遵循第5条。
 * ================================================================
 * 
 * 这个类就是为了为可开关软件提供实现而设计的。具体使用参见 OpenCloseSupportExample。
 * 
 * 这个类实际上是使用SwitchLock编写的。
 * 
 * 使用时，要注意： 1、不要在开关方法中调用被lockMethod()...unlockMethod()保护起来的方法 否则会发生死锁。
 * 2、unlockMethod()方法一定要放在finally字句中，并保证线程无论抛出什么 异常，都会调用unlockMethod()来释放锁
 * 3、使用这个类来保证同步，因为比较复杂，因而效率并不高
 * 
 */
public class OpenCloseSupport implements IOpenClosable {
	private boolean open = false;

	private SwitchLock switchLock = new SwitchLock();

	private TargetMethod openImpl = null;

	private TargetMethod closeImpl = null;

	/**
	 * 新建一个实例，你需要提供打开方法和关闭方法具体执行的任务。
	 * 
	 * @param openImpl
	 *            使用者提供的打开方法的实现
	 * @param closeImpl
	 *            使用者提供的关闭方法的实现
	 */
	public OpenCloseSupport(TargetMethod openImpl, TargetMethod closeImpl) {
		if (null == openImpl) {
			throw new NullPointerException("openImpl should not be null");
		}
		if (null == closeImpl) {
			throw new NullPointerException("closeImpl should not be null");
		}

		this.openImpl = openImpl;
		this.closeImpl = closeImpl;
	}

	/**
	 * 测试是否打开。 可开关组件的状态实际上只有两个：打开的和关闭的。
	 */
	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void open() throws AlreadyOpenedException, FailedToOpenException {
		if (isOpen()) {
			throw new AlreadyOpenedException();
		}

		switchLock.lockSwitch();
		try {
			open = true;
			openImpl();
		} catch (Exception ex) {
			open = false;

			throw new FailedToOpenException(ex);
		} finally {
			switchLock.unlockSwitch();
		}
	}

	/**
	 * 注意这个只是这个类自己用的方法，和使用者提供的打开方法 没有关系
	 */
	private void openImpl() throws Exception {
		if (openImpl != null) {
			openImpl.run();
		} else {
			assert (false);
			System.out
					.println("WARNING: openImpl()'s MethodTarget not assigned");
		}
	}

	@Override
	public void close() throws AlreadyClosedException, FailedToCloseException {
		if (!isOpen()) {
			throw new AlreadyClosedException();
		}

		switchLock.lockSwitch();
		try {
			open = false;

			closeImpl();
		} catch (Exception ex) {
			throw new FailedToCloseException(ex);
		} finally {
			switchLock.unlockSwitch();
		}
	}

	/**
	 * 注意这个只是这个类自己用的方法，和使用者提供的关闭方法 没有关系
	 */
	private void closeImpl() throws Exception {
		if (closeImpl != null) {
			closeImpl.run();
		} else {
			assert (false);
			System.out
					.println("WARNING: closeImpl()'s MethodTarget not assigned");
		}
	}

	/**
	 * 确保组件的状态是打开的。 如果组件没有打开，则抛出AlreadyClosedException
	 * 
	 * @throws AlreadyClosedException
	 *             ，如果组件没有打开
	 */
	public void ensureOpen() {
		if (!isOpen()) {
			throw new AlreadyClosedException();
		}
	}

	/**
	 * 确保组件的状态是关闭的。 如果组件已经打开，则抛出AlreadyOpenedException
	 * 
	 * @throws AlreadyOpenedException
	 *             ，如果组件已经打开
	 */
	public void ensureClosed() {
		if (isOpen()) {
			throw new AlreadyOpenedException();
		}
	}

	/**
	 * 用来保护普通方法，加锁
	 */
	public void lockMethod() {
		switchLock.lockMethod();
	}

	/**
	 * 用来保护普通方法，解锁
	 */
	public void unlockMethod() {
		switchLock.unlockMethod();
	}

	public TargetMethod getOpenImpl() {
		return openImpl;
	}

	public TargetMethod getCloseImpl() {
		return closeImpl;
	}

}
