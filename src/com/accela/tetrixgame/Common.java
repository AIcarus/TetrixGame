package com.accela.tetrixgame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Common {
	public static Logger LOG = Logger.getLogger("tetrix");
	static {
		LOG.setLevel(Level.OFF); 
		ConsoleHandler consoleHandler =new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		LOG.addHandler(consoleHandler);
	}
	
	public static PrintWriter LESS_IMPORTANT_ERROR = null;
	static {
		try {
			LESS_IMPORTANT_ERROR = new PrintWriter("log0.log");
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * When a game starts to hosting, the ip:port address will be written 
	 * into this file. So that BOTs can find it, even without broadcasting.
	 */
	public static final File HOST_ADDRESS=new File("host_address.txt");
}
