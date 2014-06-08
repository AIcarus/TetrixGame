package com.accela.tetrixgame.conn.shared;

import java.io.IOException;

/**
 * ClientID用来唯一标识连接到Connector的Client ClientID应该保证在连接重建后，只要Client不变， ClientID就不变。
 * 此外，ClientID必须是一个不可变类
 * 
 * ClientID被设计成独立于不同的网络技术。在具体的 网络技术下，应该用相应的ClientID的子类来作为ClientID
 * 
 * //Inheritance needed
 */
public abstract class ClientID implements Comparable<ClientID> {
	public abstract boolean equals(Object o);

	public abstract int hashCode();

	public abstract int compareTo(ClientID other);
	
	/* @return human readable address string*/
	public abstract String info();
	
	public abstract String serializeToString();
	public abstract void serializeFromString(String addr) throws IOException;
}
