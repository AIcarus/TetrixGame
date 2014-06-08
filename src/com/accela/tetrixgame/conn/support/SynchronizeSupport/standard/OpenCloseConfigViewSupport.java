package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

/**
 * 
 * 如果你已经看过了OpenCloseSupport和ConfigViewSupport，那么
 * 你应该能很容易地理解这个类。这个类就是吧OpenCloseSupport和 ConfigViewSupport的功能结合到了一起。
 * 
 * 一些类，即使可开关类，有需要区分config方法和view方法。这里把 这种类中的方法分类为： 1、开关方法 2、普通方法 2.1、config方法
 * 2.2、view方法
 * 
 * open()和close()用来代办开关方法；lockConfigMethod()...unlockConfigMethod()
 * 用来保护普通方法中的config方法；lockViewMethod()...unlockViewMethod()方法。
 * 
 */
public class OpenCloseConfigViewSupport implements IOpenClosable {
	private OpenCloseSupport ocs;

	private ConfigViewSupport cvs;

	/**
	 * 与OpenCloseSupport的构造方法一样
	 * 
	 * @param openImpl
	 * @param closeImpl
	 */
	public OpenCloseConfigViewSupport(TargetMethod openImpl,
			TargetMethod closeImpl) {
		ocs = new OpenCloseSupport(openImpl, closeImpl);
		cvs = new ConfigViewSupport();
	}

	@Override
	public void close() throws AlreadyClosedException, FailedToCloseException {
		ocs.close();
	}

	public TargetMethod getCloseImpl() {
		return ocs.getCloseImpl();
	}

	public TargetMethod getOpenImpl() {
		return ocs.getOpenImpl();
	}

	/**
	 * 同OpenCloseSupport的isOpen()
	 */
	@Override
	public boolean isOpen() {
		return ocs.isOpen();
	}

	@Override
	public void open() throws AlreadyOpenedException, FailedToOpenException {
		ocs.open();
	}

	/**
	 * 同OpenCloseSupport的ensureClosed()
	 */
	public void ensureClosed() {
		ocs.ensureClosed();
	}

	/**
	 * 同OpenCloseSupport的ensureOpen()
	 */
	public void ensuredOpen() {
		ocs.ensureOpen();
	}

	/**
	 * 锁住普通方法中的config方法
	 */
	public void lockConfigMethod() {
		ocs.lockMethod();
		cvs.lockConfig();
	}

	/**
	 * 解锁普通方法中的config方法
	 */
	public void unlockConfigMethod() {
		cvs.unlockConfig();
		ocs.unlockMethod();
	}

	/**
	 * 锁住普通方法中的view方法
	 */
	public void lockViewMethod() {
		ocs.lockMethod();
		cvs.lockView();
	}

	/**
	 * 解锁普通方法中的view方法
	 */
	public void unlockViewMethod() {
		cvs.unlockView();
		ocs.unlockMethod();
	}
}
