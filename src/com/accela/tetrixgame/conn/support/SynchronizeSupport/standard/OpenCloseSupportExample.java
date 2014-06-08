package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

import com.accela.tetrixgame.Common;

/**
 * 
 * 这个类为了清楚地表示如何使用OpenCloseSupport而设计
 * 
 * 首先，设想OpenCloseSupportExample是一个可开关组件。 它使用OpenCloseSupport来满足其可开关组件的要求。
 */
public class OpenCloseSupportExample implements IOpenClosable {
	private OpenCloseSupport ocs;

	/**
	 * 构造方法中，你应该使用你自己的open方法、close方法的具体
	 * 实现来初始化OpenCloseSupport。这样，OpenCloseSupport在 打开和关闭的时候，就会执行你定制的打开和关闭方法了。
	 */
	public OpenCloseSupportExample() {
		ocs = new OpenCloseSupport(new OpenMethod(), new CloseMethod());
	}

	/**
	 * 这里只需简单地调用OpenCloseSupport提供的close方法， OpenCloseSupport帮你包办关闭方法
	 */
	@Override
	public void close() throws AlreadyClosedException, FailedToCloseException {
		ocs.close();
	}

	/**
	 * OpenCloseSupport帮你包办打开和关闭，因此询问打开和关闭的状态 的时候，也应该找它。
	 */
	@Override
	public boolean isOpen() {
		return ocs.isOpen();
	}

	/**
	 * 这里只需简单地调用OpenCloseSupport提供的open方法， OpenCloseSupport帮你包办打开方法
	 */
	@Override
	public void open() throws AlreadyOpenedException, FailedToOpenException {
		ocs.open();
	}

	/**
	 * 
	 * 你定制的打开组件时应该执行的方法。建议使用内部类，简便易行。 在初始化OpenCloseSupport的时候，会把这个类的对象传入，以便
	 * 调用OpenCloseSupport的open方法的时候，执行你定制的打开方法。
	 * 
	 */
	private class OpenMethod implements TargetMethod {

		/**
		 * 里面写上打开时要执行的代码
		 */
		@Override
		public void run() throws Exception {
			Common.LOG.info("Hey! This class is opening.");
		}

	}

	/**
	 * 
	 * 你定制的关闭组件时应该执行的方法。建议使用内部类，简便易行。 在初始化OpenCloseSupport的时候，会把这个类的对象传入，以便
	 * 调用OpenCloseSupport的close方法的时候，执行你定制的关闭方法。
	 * 
	 */
	private class CloseMethod implements TargetMethod {

		/**
		 * 里面写上关闭时要执行的代码
		 */
		@Override
		public void run() throws Exception {
			Common.LOG.info("Hey! This class is closing.");

		}

	}

	/**
	 * 这是一个普通方法，因为可能引起线程阻塞，故应该用 lockMethod()...unlockMethod()保护起来。
	 */
	public void method1() throws InterruptedException {
		ocs.lockMethod();
		try {
			/*
			 * 使用这个保证一定在组件打开的时候， 才可以进入这个方法。 这条语句可能抛出异常，因此一定要放 在try块中
			 */
			ocs.ensureOpen();

			Thread.sleep(1000);
		} finally {
			/*
			 * 这里请注意，将unlockMethod()方法放在finally字句中， 并用try将可能抛出异常的方法包起来，不仅是一种好的
			 * 变成习惯，而且对于OpenCloseSupport是必须的。
			 * 
			 * 因为，OpenCloseSupport.open()和OpenCloseSupport.close()
			 * 通过使进入普通方法的线程抛出异常来将其赶出方法。
			 */
			ocs.unlockMethod();
		}
	}

	/**
	 * 这同样是一个普通方法，但它不会造成线程阻塞。如果你认为它不遵守
	 * 可开关组件的规则也无大碍，当然可以不用lockMethod()...unlockMethod() 来保护它。
	 */
	public void method2() {
		ocs.lockMethod();
		try {
			/*
			 * 使用这个保证一定在组件关闭的时候， 才可以进入这个方法 这条语句可能抛出异常，因此一定要放 在try块中
			 */
			ocs.ensureClosed();

			Common.LOG.info("method2 invoked!");
		} finally {
			ocs.unlockMethod();
		}
	}

}
