package com.accela.tetrixgame.conn.shared;

/**
 * 当Accept一个连接的时候，或者发起新的连接的时候，记录这个连接的信息， 同时这个信息也是创建Connector时所需的必要信息。
 * 
 * NewConnectionInfo记录的是指向远程端的连接信息，继承的时候 不要弄反。
 * 
 * 不同的网络技术需要不同的NewConnectionInfo的实现， 因此这个类被设置成抽象类
 * 
 * //Inheritance needed
 */
public abstract class NewConnectionInfo {

}
