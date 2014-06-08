package com.accela.tetrixgame.conn.support.ObjectStreams.support;

import com.accela.tetrixgame.conn.support.ReflectionSupport.FieldExtractor;

/**
 * 
 * 这个类是ObjectInputStreamSupport和ObjectOutputStream 的父类，为它们一共一些公用的服务，以及定义一些公用的量
 * 
 */
abstract class ObjectStreamSupport {
	/**
	 * 获得一个FieldExtractor对象。通过这个方法来获得FieldExtractor 对象才能够优化它的新建。
	 */
	protected FieldExtractor getFieldExtractor() {
		return FieldExtractor.getFieldExtractor();
	}

	/**
	 * 释放一个FieldExtractor对象。通过这个方法来释放FieldExtractor 对象才能够优化它的释放。
	 */
	protected void disposeFieldExtractor(FieldExtractor fieldExtractor) {
		if (null == fieldExtractor) {
			throw new NullPointerException("fieldExtractor should not be null");
		}

		FieldExtractor.disposeFieldExtractor(fieldExtractor);
	}

	/**
	 * 写入到流中的数据类型的标识。 这个代表写入的是null
	 */
	protected static final int NULL_OBJECT = 0;
	/**
	 * 写入到流中的数据类型的标识。 这个代表写入的是一个已经写如果的 对象。出现这种情况的常常是具有循 环引用的对象。
	 */
	protected static final int REUSE_OBJECT = 1;
	/**
	 * 写入到流中的数据类型的标识。 这个代表写入的是一个正常的对象，即非以上两 种的对象。
	 */
	protected static final int NORMAL_OBJECT = 2;

	/*
	 * protected static final int NORMAL_OBJECT_COMMON=3; protected static final
	 * int NORMAL_OBJECT_COLLECTION=4; protected static final int
	 * NORMAL_OBJECT_MAP=5;
	 */

}
