package com.accela.tetrixgame.conn.support.ObjectPool;

/**
 * 
 * 实现了这个接口的对象，当放入ObjectPool中的时候， ObjectPool会调用onPutIntoObjectPool()方法，当
 * 从ObjectPool中取出的时候，ObjectPool会调用 onRetrievedFromObjectPool()方法
 * 
 */
public interface IObjectPoolSensitive {
	/**
	 * 在从对象池中取回对象的时候执行
	 */
	public void onPut();

	/**
	 * 在向对象池中放入对象的时候执行
	 */
	public void onRetrieve();
}
