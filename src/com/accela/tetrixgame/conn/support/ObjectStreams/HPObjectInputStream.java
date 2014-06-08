package com.accela.tetrixgame.conn.support.ObjectStreams;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import com.accela.tetrixgame.conn.support.ObjectStreams.support.ObjectInputStreamSupport;

/**
 * 
 * 一、HP对象输出流(我起的名字)。这个对象输出流由反射写成。 可以读取或输出： 1、读取拥有可以通过反射利用构造函数来创建的对象。构造函数可以
 * 是私有的，最好是无参构造函数，但是有参数的构造函数也可以， 只是速度更慢。 2、数组类型，无论数组元素是基本类型还是对象类型。 null。 3、枚举变量
 * 4、能够识别声明为transient的对象类型的字段，在输出的时候会自动 写为null
 * 5、能够识别实现了Collection或Map接口的对象，用优化的方式输出。
 * 但是对于它们会忽略去中的transient字段，当作没有声明transient 的字段输出 6、能够输出和读取存在循环引用的对象
 * 
 * 二、与java.io.ObjectInputStream和java.io.ObjectOutputStream的区别：
 * 1、如果在文件中写入一个对象，用java.io.ObjectInputStream读取无论
 * 多少次，都只会生成一个实例。这在网络中发送对象的时候会导致发送对象 方发送一个对象后，更改该对象，再发送，而接收对象方只能接收一次，后
 * 续的接收返回的都是第一次接收的对象。 2、与ObjectPool配合，优化了新建对象时的内存分配。当发送同类型的对
 * 象的时候，如果每10ms克隆一副本并发送一次(如果不克隆，java.io.ObjectInputStream，
 * 就只能接收第一次发送的对象)，使用java.io.ObjectInputStream接收对象
 * 的时候会新建大量对象实例，即使释放也难以被立即回收。而使用HPObjectInputStream 则可以与对象池配合，利用释放掉的对象，避免重复新建实例。
 * 
 * 三、需要注意的地方：
 * 1、HPObjectOutputStream和HPObjectInputStream与java.io.ObjectInputStream
 * 和java.io.ObjectOutputStream并不能兼容。用一种输出流写的对象，另一种输入流 不能读取。
 * 2、HPObjectOutputStream和HPObjectInputStream使用反射写成，反射是其性能的瓶
 * 颈，尽管对象池可以大大地优化其效率。HPObjectInputStream使用反射构造函数来新
 * 建对系那个，如果没有无参构造函数，HPObjectInputStream就会用试探参数测试其他的
 * 有参构造函数，来试图新建对象实例。这回造成性能浪费，建议在不便使用有参构造函数 的情况下，给需要输出的类写一个私有的无参构造函数。
 * 3、HPObjectOutputStream和HPObjectInputStream使用反射来访问对象的构造函数和字
 * 段，但是如果对象是用了SecurityManager来禁止，哪怕使用setAccessible(true)方法，
 * 也无法访问的话，HPObjectOutputStream和HPObjectInputStream就会抛出异常。
 * 
 * 四、未解决的BUG 1、在使用中发现，当使用HPObjectOutputStream和HPObjectInputStream时，极少情况下，
 * 会随机的发生ConcurrentModificationException。原因不明，可能和对写集合类有关。
 * 
 * 
 */
public class HPObjectInputStream extends DataInputStream {
	// TODO HPObjectInputStream的硬伤就在生成对象的时候，需要搜索构造函数，如果构造函数不好用，就会很慢
	// TODO 这一点已经利用缓冲区技术改进，但是没有正二八经的测试过。
	private ObjectInputStreamSupport support = new ObjectInputStreamSupport();

	public HPObjectInputStream(InputStream in) {
		super(in);
	}

	public Object readObject() throws IOException, ClassNotFoundException,
			InvocationTargetException, InstantiationException {
		return support.readObject(this);
	}

}
