package com.accela.tetrixgame.conn.support.SynchronizeSupport.standard;

/**
 * 
 * 实现这个接口表示该类是一个可以关闭和打开的组件。
 * 
 */
public interface IOpenClosable {
	/**
	 * @return 组件当前是打开的还是已经关闭的
	 */
	public boolean isOpen();

	/**
	 * 打开组件
	 * 
	 * @throws AlreadyOpenedException
	 *             如果你连续两次打开组件的话
	 * @throws FailedToOpenException
	 *             如果打开过程中遇到异常
	 */
	public void open() throws AlreadyOpenedException, FailedToOpenException;

	/**
	 * 关闭组件
	 * 
	 * @throws AlreadyOpenedException
	 *             如果你连续两次打开组件的话
	 * @throws FailedToOpenException
	 *             如果打开过程中遇到异常
	 */
	public void close() throws AlreadyClosedException, FailedToCloseException;

}
