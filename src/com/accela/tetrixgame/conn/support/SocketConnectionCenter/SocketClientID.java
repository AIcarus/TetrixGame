package com.accela.tetrixgame.conn.support.SocketConnectionCenter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.accela.tetrixgame.conn.shared.ClientID;

/**
 * 
 * 在套接字具体网络技术下的ClientID
 * 
 */
public class SocketClientID extends ClientID {
	private InetAddress address;
	private int port;

	private SocketClientID() {
		// this constructor is reserved for HPObjectStreams
	}
	
	public SocketClientID(String addr) throws IOException{
		this.serializeFromString(addr);
	}

	public SocketClientID(InetAddress address, int port) {
		this();

		if (null == address) {
			throw new NullPointerException("address is null");
		}
		if (port < 0) {
			throw new IllegalArgumentException("port should not be negative");
		}

		this.address = address;
		this.port = port;
	}

	public boolean equals(Object o) {
		assert (address != null);
		assert (port >= 0);

		if (!(o instanceof SocketClientID)) {
			return false;
		}

		SocketClientID c = (SocketClientID) o;

		assert (c.address != null);
		assert (c.port >= 0);

		/*we only compare byte ip, hostname string (which can be empty) is not considered*/
		byte[] myBytes = address.getAddress();
		byte[] cBytes = c.address.getAddress();
		if(myBytes.length!=cBytes.length){
			return false;
		}
		for(int i=0;i<myBytes.length;i++){
			if(myBytes[i]!=cBytes[i]){
				return false;
			}
		}
		
		return port == c.getPort();
	}

	public int hashCode() {
		assert (address != null);
		assert (port >= 0);
	
		int addrHash=0;
		for(byte b : address.getAddress()){
			addrHash<<=8;
			addrHash+=b;
		}
		return addrHash + port;
	}

	public int compareTo(ClientID c) {
		assert (address != null);
		assert (port >= 0);

		if (!(c instanceof SocketClientID)) {
			return 1; // TODO 如果同时使用多种ClientID,则这个判断就过于武断了
		}

		SocketClientID other = (SocketClientID) c;
		assert (other.getAddress() != null);
		assert (other.getPort() >= 0);

		byte[] selfIP = address.getAddress();
		byte[] otherIP = address.getAddress();

		boolean findDifferent = false;
		boolean selfBiggerThanOther = false;
		for (int i = 0; i < Math.max(selfIP.length, otherIP.length); i++) {
			int selfIPByte = (i < selfIP.length) ? selfIP[i] : 0;
			int otherIPByte = (i < otherIP.length) ? otherIP[i] : 0;

			if (selfIPByte != otherIPByte) {
				findDifferent = true;
				selfBiggerThanOther = selfIPByte > otherIPByte;
				break;
			}
		}

		if (findDifferent) {
			if (selfBiggerThanOther) {
				return 1;
			} else {
				return -1;
			}
		} else {
			return port - other.getPort();
		}

	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String toString() {
		String out = "";
		out += "SocketClientID[address=" + address.toString() + ", port="
				+ port + "]";
		return out;
	}
	
	public String info(){
		return "IP:port is: "+this.address+":"+this.port;
	}

	@Override
	public String serializeToString() {
		return byteToIpStr(this.address.getAddress())+":"+this.port;
	}
	
	private static String byteToIpStr(byte[] bytes){
		StringBuffer buf=new StringBuffer();
		
		boolean hasDot=false;
		for(int b : bytes){
			buf.append((b&0xFF)+".");
			hasDot=true;
		}
		
		String result=buf.toString();
		if(hasDot){
			result=result.substring(0, buf.length()-1);
		}
		return result;
	}

	@Override
	public void serializeFromString(String addr) throws IOException {
		String[] tokens=addr.split(":");
		if(tokens.length!=2){
			throw new IOException("addr is in illegal format: "+addr);
		}
		try{
			this.address=InetAddress.getByName(tokens[0]);
			this.port=Integer.parseInt(tokens[1]);
		}catch(UnknownHostException ex){
			throw new IOException("addr is in illegal format: "+addr, ex);
		}catch(NumberFormatException ex){
			throw new IOException("addr is in illegal format: "+addr, ex);
		}
	}

}
