package com.accela.tetrixgame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.ui.CommandLine;

public class Main {
	
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void redirectSysErr(){
		boolean hasException = false;
		do {
			hasException = false;
			try {
				System.setErr(new PrintStream(new FileOutputStream("log0.log")));
			} catch (FileNotFoundException ex) {
				hasException = true;
				ex.printStackTrace();
			}
		} while (hasException);
	}
	
	/**
	 * 命令行的第一个参数可以用来指定输入文件
	 */
	public static void main(String[] args) {
		redirectSysErr();
		clearMyAddress();

		// 利用命令行参数设定系统输入流
		if (args != null && args.length == 1) {
			try {
				System.setIn(new FileInputStream(args[0]));
			} catch (FileNotFoundException ex) {
				ex.printStackTrace();
			}
		}

		CommandLine commandLine = new CommandLine(System.in,
				System.out);

		try {
			commandLine.open();
		} catch (FailedToOpenException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void clearMyAddress(){
		File hostAddr=Common.HOST_ADDRESS;
		PrintWriter out=null;
		try{
			out=new PrintWriter(hostAddr);
			out.println();
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			if(out!=null){
				out.close();
			}
		}
	}
}
