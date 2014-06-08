package com.accela.tetrixgame.ui;

import com.accela.tetrixgame.bot.BOT;
import com.accela.tetrixgame.bot.BOTManager;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketClientID;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyClosedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyOpenedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.net.basic.HostBroadcastReceiver;
import com.accela.tetrixgame.net.hostClient.FailedException;
import com.accela.tetrixgame.net.hostClient.NetClient;
import com.accela.tetrixgame.net.hostClient.NetHost;
import com.accela.tetrixgame.net.hostClient.RunningStation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * 这个游戏的与用户进行交互的UI。 通过执行各种命令来操纵游戏，建立主机、客户机等等。
 * 
 */
public class CommandLine {
	public static final String DEMO_FILE_SUFFIX = ".trx";

	private NetHost host;

	private NetClient client;

	private HostBroadcastReceiver broadcastReceiver;

	private BOTManager botManager = new BOTManager();

	private CommandProcessingThead commandThread;

	private boolean open = false;

	private BufferedReader in;

	private PrintWriter out;

	// 当用exit命令退出的时候，需要关闭录像文件，也就是要关闭所有正在运行的TetrixRunningStation
	private List<RunningStation> stationList = new LinkedList<RunningStation>();

	/**
	 * @param in
	 *            指定从什么地方输入命令
	 * @param out
	 *            指定向什么地方打印信息
	 */
	public CommandLine(InputStream in, OutputStream out) {
		if (null == in) {
			throw new IllegalArgumentException("in should not be null");
		}
		if (null == out) {
			throw new IllegalArgumentException("out should not be null");
		}

		broadcastReceiver = new HostBroadcastReceiver();

		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = new PrintWriter(out, true);
	}

	public void open() throws FailedToOpenException {
		if (open) {
			throw new AlreadyOpenedException();
		}

		try {
			open = true;

			broadcastReceiver.open();

			commandThread = new CommandProcessingThead();
			commandThread.start();
		} catch (FailedToOpenException ex) {
			open = false;
			throw ex;
		}
	}

	public void close() throws FailedToCloseException {
		if (!open) {
			throw new AlreadyClosedException();
		}

		try {
			open = false;

			try {
				broadcastReceiver.close();
			} finally {
				botManager.close();
			}

			if (host != null) {
				host.abort();
			}
			if (client != null) {
				client.abort();
			}

		} finally {
			commandThread.interrupt();
		}

	}

	public boolean isOpen() {
		return open;
	}

	public ClientID[] getHosts() {
		return broadcastReceiver.getHosts().toArray(new ClientID[0]);
	}

	public class CommandProcessingThead extends Thread {
		public CommandProcessingThead() {
			super("TetrixCommandLine - CommandProcessingThread");
		}

		public void run() {
			while (isOpen()) {
				try {
					out.print("<Command> ");
					out.flush();

					String line = in.readLine();

					if (null == line) {
						// 输入流可能已经读取完毕
						out.println();

						Thread.sleep(3000);
						continue;
					}

					String command = null;
					if (null == line
							|| (command = line.trim().toLowerCase()).length() == 0) {
						continue;
					}

					handleCommand(command);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		}
	}

	/**
	 * 显示所有探测到的主机
	 */
	public static final String SHOW_HOSTS = "show hosts";
	/**
	 * 自己当主机
	 */
	public static final String HOST = "host";
	/**
	 * 显示所有连接到自己的主机的客户
	 */
	public static final String SHOW_CLIENTS = "show clients";
	/**
	 * 踢出一个客户
	 */
	public static final String KICK = "kick";
	/**
	 * 启动自己当主机的游戏
	 */
	public static final String START = "start";
	/**
	 * 加入别人的主机
	 */
	public static final String JOIN = "join";
	/**
	 * 取消当前任务
	 */
	public static final String ABORT = "abort";
	/**
	 * 播放录像
	 */
	public static final String DEMO = "demo";
	/**
	 * 显示已经加入其游戏的主机
	 */
	public static final String SHOW_JOINED = "show joined";

	public static final String ADD_BOT = "add bot";
	public static final String ADD_PUBLIC_BOT = "add public bot";
	public static final String ADD_PRIVATE_BOT = "add private bot";

	public static final String REMOVE_BOT = "remove bot";
	public static final String REMOVE_PUBLIC_BOT = "remove public bot";
	public static final String REMOVE_PRIVATE_BOT = "remove private bot";

	public static final String REMOVE_ALL_BOTS = "remove all bots";
	public static final String REMOVE_ALL_PUBLIC_BOTS = "remove all public bots";
	public static final String REMOVE_ALL_PRIVATE_BOTS = "remove all private bots";

	public static final String SHOW_BOTS = "show bots";

	/**
	 * 显示帮助
	 */
	public static final String HELP = "help";
	/**
	 * 退出
	 */
	public static final String EXIT = "exit";

	private boolean beginWith(String header, String str) {
		if (null == header) {
			throw new NullPointerException("header should not be null");
		}
		if (null == str) {
			throw new NullPointerException("str should not be null");
		}

		for (int i = 0; i < header.length(); i++) {
			if (i >= str.length()) {
				return false;
			}

			if (header.charAt(i) != str.charAt(i)) {
				return false;
			}
		}

		if (str.length() > header.length()
				&& str.charAt(header.length()) != ' ') {
			return false;
		}

		return true;
	}

	private void handleCommand(String command) {
		assert (command != null);
		assert (command.trim().toLowerCase().equals(command));

		if (beginWith(SHOW_HOSTS.toLowerCase(), command)) {
			handleShowHostCommand(command);
		} else if (beginWith(HOST.toLowerCase(), command)) {
			handleHostCommand(command);
		} else if (beginWith(SHOW_CLIENTS.toLowerCase(), command)) {
			handleShowClientCommand(command);
		} else if (beginWith(KICK.toLowerCase(), command)) {
			handleKickCommand(command);
		} else if (beginWith(START.toLowerCase(), command)) {
			handleStartCommand(command);
		} else if (beginWith(JOIN.toLowerCase(), command)) {
			handleJoinCommand(command);
		} else if (beginWith(ABORT.toLowerCase(), command)) {
			handleAbortCommand(command);
		} else if (beginWith(DEMO.toLowerCase(), command)) {
			handleDemoCommnand(command);
		} else if (beginWith(SHOW_JOINED.toLowerCase(), command)) {
			handleShowJoinedCommand(command);
		} else if (beginWith(ADD_BOT.toLowerCase(), command)) {
			handleAddBotCommand(command);
		} else if (beginWith(ADD_PUBLIC_BOT.toLowerCase(), command)) {
			handleAddPublicBotCommand(command);
		} else if (beginWith(ADD_PRIVATE_BOT.toLowerCase(), command)) {
			handleAddPrivateBotCommand(command);
		} else if (beginWith(REMOVE_BOT.toLowerCase(), command)) {
			handleRemoveBotCommand(command);
		} else if (beginWith(REMOVE_PUBLIC_BOT.toLowerCase(), command)) {
			handleRemovePublicBotCommand(command);
		} else if (beginWith(REMOVE_PRIVATE_BOT.toLowerCase(), command)) {
			handleRemovePrivateBotCommand(command);
		} else if (beginWith(REMOVE_ALL_BOTS.toLowerCase(), command)) {
			handleRemoveAllBotsCommand(command);
		} else if (beginWith(REMOVE_ALL_PUBLIC_BOTS.toLowerCase(), command)) {
			handleRemvoveAllPublicBotsCommand(command);
		} else if (beginWith(REMOVE_ALL_PRIVATE_BOTS.toLowerCase(), command)) {
			handleRemoveAllPrivateBotsCommand(command);
		} else if (beginWith(SHOW_BOTS.toLowerCase(), command)) {
			handleShowBotsCommand(command);
		} else if (beginWith(HELP.toLowerCase(), command)) {
			handleHelpCommand(command);
		} else if (beginWith(EXIT.toLowerCase(), command)) {
			handleExitCommand(command);
		} else {
			handleIllegalCommand(command);
		}
	}

	// 用来记录上一次通过show hosts命令得到的hosts列表，以方便输入
	private Map<Integer, ClientID> lastHostMap = new HashMap<Integer, ClientID>();

	private void handleShowHostCommand(String command) {
		out.println("Below are all detected LAN host:");

		ClientID[] hosts = getHosts();
		assert (hosts != null);

		lastHostMap.clear();

		int idx = 0;
		for (ClientID host : hosts) {
			assert (host instanceof SocketClientID);

			SocketClientID sid = (SocketClientID) host;

			out.print(idx);
			out.print('\t');
			out.print(sid.getAddress().getHostAddress());
			out.print('\t');
			out.print(sid.getPort());
			out.println();

			lastHostMap.put(idx, host);

			idx++;
		}
	}

	private void handleHostCommand(String command) {
		if (host != null) {
			commandFailure(
					"You are hosting a game now, you should abort it first",
					null);
			return;
		}
		if (client != null) {
			commandFailure(
					"You are joining a game now, you should abort it first",
					null);
			return;
		}

		int maxPlayerNum = 0;
		try {
			maxPlayerNum = Integer.parseInt(command.substring(command
					.lastIndexOf(' ') + 1));

			host = new NetHost();
			host.startToReceiveClients(maxPlayerNum);

		} catch (Exception ex) {
			try {
				host.abort();
			} catch (Exception ex_inner) {
				ex_inner.printStackTrace();
			} finally {
				host = null;
			}

			commandFailure("Usage: host <maxPlayerNum>", ex);
			return;
		}

		out.println("Successfully created a host game, max player num is: "
				+ maxPlayerNum+", "+host.getMyAddress().info());
	}

	// 用来记录上一次通过show clients命令得到的Client列表，以方便输入
	private Map<Integer, ClientID> lastClientMap = new HashMap<Integer, ClientID>();

	private void handleShowClientCommand(String command) {
		if (null == host) {
			commandFailure(
					"You should first create a host game(using command host <maxPlayerNum>), then use this command to see who has joined the game",
					null);
			return;
		}
		if (client != null) {
			commandFailure(
					"You are joining a game now, you should abort it first",
					null);
			return;
		}

		Set<ClientID> clients = host.getClients();
		assert (clients != null);

		out.println("Below are clients that has connected to your game: ");

		lastClientMap.clear();

		int idx = 0;
		for (ClientID client : clients) {
			assert (client instanceof SocketClientID);

			SocketClientID sid = (SocketClientID) client;

			out.print(idx);
			out.print('\t');
			out.print(sid.getAddress().getHostAddress());
			out.print('\t');
			out.print(sid.getPort());
			out.println();

			lastClientMap.put(idx, sid);

			idx++;
		}

	}

	private void handleKickCommand(String command) {
		if (null == host) {
			commandFailure(
					"You should first create a host game(using command host <maxPlayerNum>), then start the game",
					null);
			return;
		}
		if (client != null) {
			commandFailure(
					"You are joining a game now, you should abort it first",
					null);
			return;
		}

		StringTokenizer tokens = new StringTokenizer(command);
		tokens.nextToken();

		List<String> args = new LinkedList<String>();
		while (tokens.hasMoreTokens()) {
			args.add(tokens.nextToken());
		}

		// 分析指令
		ClientID client = null;

		try {
			if (args.size() == 1) {
				client = lastClientMap.get(Integer.parseInt(args.get(0)));
				if (null == client) {
					throw new IllegalArgumentException("illegal host id number");
				}
			} else if (args.size() == 2) {
				InetAddress address = InetAddress.getByName(args.get(0));
				int port = Integer.parseInt(args.get(1));

				client = new SocketClientID(address, port);
			} else {
				throw new IllegalArgumentException("illegal arguments");
			}
		} catch (Exception ex) {
			commandFailure("Usage: kick <IP address> <port> "
					+ "or kick <client id>", ex);

			return;
		}

		assert (client != null);

		// 踢出指定的Client
		host.kick(client);

		out.println("Successfully kicked client "
				+ ((SocketClientID) client).getAddress().getHostAddress() + " "
				+ ((SocketClientID) client).getPort());

	}

	private void handleStartCommand(String command) {
		if (null == host) {
			commandFailure(
					"You should first create a host game(using command host <maxPlayerNum>), then start the game",
					null);
			return;
		}
		if (client != null) {
			commandFailure(
					"You are joining a game now, you should abort it first",
					null);
			return;
		}

		out.println("Starting...");

		String demoFileName = null;
		GameAttributes attr = new GameAttributes();

		try {
			// 分离指定的录像文件
			int spliter = command.indexOf('>');
			if (spliter < 0) {
				demoFileName = null;
			} else {
				if (spliter >= command.length() - 1) {
					demoFileName = null;
				} else {
					demoFileName = command.substring(spliter + 1).trim();
				}
			}

			if (demoFileName != null
					&& (demoFileName.indexOf(' ') >= 0 || demoFileName.length() <= 0)) {
				throw new IllegalArgumentException("illegal file name");
			}

			if (spliter >= 0) {
				command = command.substring(0, spliter);
			}

			// 解析命令部分
			StringTokenizer tokens = new StringTokenizer(command);
			tokens.nextToken();

			List<Double> args = new LinkedList<Double>();
			while (tokens.hasMoreTokens()) {
				args.add(Double.parseDouble(tokens.nextToken()));
			}

			if (args.size() == 0) {
				// don't modify attr
			} else if (args.size() == 1) {
				// 读取<barVel>
				startCommandHandlerBarVelReader(attr, args);

			} else if (args.size() == 3) {
				// 读取<barVel>
				startCommandHandlerBarVelReader(attr, args);

				// 读取<barRowNum>和<barColumnNum>
				int barRowNum = 0;
				int barColumnNum = 0;

				barRowNum = (int) args.get(1).doubleValue();
				barColumnNum = (int) args.get(2).doubleValue();

				if (barRowNum <= 1 || barRowNum > 8) {
					throw new IllegalArgumentException(
							"bar row num is illegal: " + barRowNum);
				}
				if (barColumnNum <= 0 || barColumnNum >= 4) {
					throw new IllegalArgumentException(
							"bar column num is illegal: " + barColumnNum);
				}

				attr.put(GameAttributes.BAR_ROW_NUM,
						String.valueOf(barRowNum));
				attr.put(GameAttributes.BAR_COLUMN_NUM,
						String.valueOf(barColumnNum));
			} else {
				throw new IllegalArgumentException("illegal arguments");
			}
		} catch (Exception ex) {
			commandFailure(
					"Usage: start [>demo file name] "
							+ "or start <barVel> [>demo file name] "
							+ "or start <barVel> <barRowNum> <barColunmNum> [>demo file name]",
					ex);
			return;
		}

		try {
			assert (host != null);

			// 生成随机种子
			attr.put(GameAttributes.RANDOM_SEED,
					String.valueOf((int) (Integer.MAX_VALUE * Math.random())));

			RunningStation station = host.launchGame(attr);

			if (demoFileName != null) {
				try {
					station.setDemoOutputStream(new FileOutputStream(
							demoFileName + DEMO_FILE_SUFFIX));

				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
					assert (false);
				}
			}

			if (station != null) {
				popupTetrixRunningStationPanel(station, "Host");
			} else {
				host.abort();
			}

			host = null;
		} catch (Exception ex) {
			try {
				host.abort();
			} catch (Exception ex_inner) {
				ex_inner.printStackTrace();
			} finally {
				host = null;
			}

			commandFailure(
					"Usage: start [>demo file name] "
							+ "or start <barVel> [>demo file name] "
							+ "or start <barVel> <barRowNum> <barColunmNum> [>demo file name]",
					ex);
			return;
		}

		// 生成显示下落的bar的速度的字符串
		double barVel = Double.parseDouble(attr
				.get(GameAttributes.V_NORMAL_VEL))
				/ GameAttributes.DEFAULT_V_NORMAL_VEL;
		String barVelStr = null;
		if (Math.abs(barVel - Math.round(barVel)) < 0.01) {
			barVelStr = String.valueOf(Math.round(barVel));
		} else {
			barVelStr = String.valueOf(barVel);
			int dotIdx = barVelStr.lastIndexOf('.');
			assert (dotIdx >= 0);

			barVelStr = barVelStr.substring(0,
					Math.min(barVelStr.length(), dotIdx + 3));
		}

		out.println("Successfully started the game, " + "bar velocity is: "
				+ barVelStr + ", bar row num is: "
				+ attr.get(GameAttributes.BAR_ROW_NUM)
				+ ", bar column num is: "
				+ attr.get(GameAttributes.BAR_COLUMN_NUM));
	}

	// 这个方法专门给startCommandHandler方法是用，用来帮助他读取barVel
	private void startCommandHandlerBarVelReader(GameAttributes attr,
			List<Double> args) {
		// 读取<barVel>
		double vDefNormalVel = Double.parseDouble(attr
				.get(GameAttributes.V_NORMAL_VEL));
		double vDefMaxVel = Double.parseDouble(attr
				.get(GameAttributes.V_MAX_VEL));

		if (args.get(0) < 0.01) {
			throw new IllegalArgumentException("bar velocity too small");
		}

		double vNormalVel = Math.min(args.get(0) * vDefNormalVel, vDefMaxVel);

		attr.put(GameAttributes.V_NORMAL_VEL, String.valueOf(vNormalVel));

		double factor = Math.max(1, vNormalVel / vDefNormalVel / 2);
		// 将bar的翻转速度也按照下落速度做相应改动
		double defScrollVel = GameAttributes.DEFAULT_SCROLL_MAX_VEL;
		attr.put(GameAttributes.SCROLL_MAX_VEL,
				String.valueOf(defScrollVel * factor));

		// 将bar的水平移动的速度也改变
		double defHVel = GameAttributes.DEFAULT_H_MAX_VEL;
		attr.put(GameAttributes.H_MAX_VEL,
				String.valueOf(defHVel * factor));
	}

	private void handleJoinCommand(String command) {
		if (host != null) {
			commandFailure(
					"You are hosting a game now, you should abort it first",
					null);
			return;
		}
		if (client != null) {
			commandFailure(
					"You are joining a game now, you should abort it first",
					null);
			return;
		}

		try {
			// 分析指定的录像文件
			String demoFileName = null;

			int spliter = command.indexOf('>');
			if (spliter < 0) {
				demoFileName = null;
			} else {
				if (spliter >= command.length() - 1) {
					demoFileName = null;
				} else {
					demoFileName = command.substring(spliter + 1).trim();
				}
			}

			if (demoFileName != null
					&& (demoFileName.indexOf(' ') >= 0 || demoFileName.length() <= 0)) {
				throw new IllegalArgumentException("illegal file name");
			}

			if (spliter >= 0) {
				command = command.substring(0, spliter);
			}

			// 分析命令部分
			StringTokenizer tokens = new StringTokenizer(command);
			tokens.nextToken();

			List<String> args = new LinkedList<String>();
			while (tokens.hasMoreTokens()) {
				args.add(tokens.nextToken());
			}

			ClientID host = null;
			if (args.size() == 1) {
				host = lastHostMap.get(Integer.parseInt(args.get(0)));
				if (null == host) {
					throw new IllegalArgumentException("illegal host id number");
				}
			} else if (args.size() == 2) {
				InetAddress address = InetAddress.getByName(args.get(0));
				int port = Integer.parseInt(args.get(1));

				host = new SocketClientID(address, port);
			} else {
				throw new IllegalArgumentException("illegal arguments");
			}

			client = new NetClient();
			final ClientID finalHost = host;

			final String finalDemoFileName = demoFileName;

			new Thread(new Runnable() {
				public void run() {
					try {
						RunningStation station = client
								.launchGame(finalHost);

						if (finalDemoFileName != null) {
							try {
								station.setDemoOutputStream(new FileOutputStream(
										finalDemoFileName + DEMO_FILE_SUFFIX));
							} catch (FileNotFoundException ex) {
								ex.printStackTrace();
								assert (false);
							}
						}

						if (station != null) {
							popupTetrixRunningStationPanel(station, "Client");
						} else {
							try {
								client.abort();
							} catch (FailedToCloseException ex) {
								ex.printStackTrace();
							}
						}

						clientLock.lock();
						try {
							client = null;
						} finally {
							clientLock.unlock();
						}
					} catch (FailedException ex) {
						try {
							client.abort();
						} catch (FailedToCloseException ex_inner) {
							ex_inner.printStackTrace();
						} finally {
							clientLock.lock();

							try {
								client = null;
							} finally {
								clientLock.unlock();
							}
						}
					}

				}
			}).start();

			out.println("Successfully joined game "
					+ ((SocketClientID) host).getAddress().getHostAddress()
					+ " " + ((SocketClientID) host).getPort()
					+ ", you have to wait until the game started automatically");

		} catch (Exception ex) {
			try {
				client.abort();
			} catch (Exception ex_inner) {
				ex_inner.printStackTrace();
			} finally {
				client = null;
			}

			commandFailure("Usage: join <IP address> <port> [>demo file name] "
					+ "or join <host id> [>demo file name]", ex);
			return;
		}

	}

	private void handleAbortCommand(String command) {
		boolean hostAbort = false;
		boolean clientAbort = false;
		if (host != null) {
			hostAbort = true;

			try {
				host.abort();
			} catch (Exception ex) {
				System.err.println("host abort failure");
				ex.printStackTrace();
			}
			host = null;
		}
		if (client != null) {
			clientAbort = true;

			try {
				client.abort();
			} catch (Exception ex) {
				System.err.println("client abort failure");
				ex.printStackTrace();
			}
			client = null;
		}

		if (hostAbort && !clientAbort) {
			out.println("Host successfully aborted. ");
		} else if (!hostAbort && clientAbort) {
			out.println("Successfully aborted joining game. ");
		} else if (!hostAbort && !clientAbort) {
			out.println("Nothing to abort actually. ");
		} else {
			assert (false);
		}
	}

	private void handleDemoCommnand(String command) {
		StringTokenizer tokens = new StringTokenizer(command);
		tokens.nextToken();

		List<String> args = new LinkedList<String>();
		while (tokens.hasMoreTokens()) {
			args.add(tokens.nextToken().trim());
		}

		List<InputStream> ins = new LinkedList<InputStream>();

		if (args.size() == 0) {
			// 用FileBrownzer选择文件。
			out.println("Selecting Files...");

			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileNameExtensionFilter(DEMO_FILE_SUFFIX
					.replace('.', ' ').trim(), "Tetrix Game Demo File"));

			File[] selected = null;
			if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)) {
				selected = fc.getSelectedFiles();
			}

			// 接收文件并打印文件名
			if (selected != null) {
				for (int i = 0; i < selected.length; i++) {
					File file = selected[i];
					if (null == file) {
						continue;
					}
					if (file.isDirectory()) {
						continue;
					}

					FileInputStream fileIn = null;
					try {
						fileIn = new FileInputStream(file);
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}

					if (fileIn != null) {
						ins.add(fileIn);

						out.println(file.getName());
					}

				}

			} else {
				out.println("User Cancelled.");
				return;
			}
		} else {
			out.println("Files selected are: ");

			// 从命令中读取文件名
			for (String fileStr : args) {
				if (null == fileStr) {
					continue;
				}

				File file = new File(fileStr + DEMO_FILE_SUFFIX);
				if (file.isDirectory()) {
					continue;
				}
				if (!file.exists()) {
					continue;
				}

				FileInputStream fileIn = null;
				try {
					fileIn = new FileInputStream(file);
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				}

				if (fileIn != null) {
					ins.add(fileIn);

					out.println(file.getName());
				}

			}
		}

		if (ins.size() == 0) {
			out.println("Failed: file not found or not available.");
			return;
		}
		if (ins.size() > DemoPanel.MAX_DEMO_NUM) {
			out.println("Failed: too much demo files. ");
			return;
		}

		// 打印提示
		out.println("Note that demos are dislayed in the same order with the files printed.");

		// 启动窗口
		out.println("Demo window launching...");
		try {
			new DemoFrame(ins.toArray(new FileInputStream[0])).setVisible(true);
		} catch (Exception ex) {
			commandFailure("Failed to demo. ", ex);
		}
		out.println("Demo window is launched!");

	}

	private ReentrantLock clientLock = new ReentrantLock();

	private void handleShowJoinedCommand(String command) {
		clientLock.lock();

		try {
			if (client != null && client.getHostID() != null) // TODO
			// 这里可能导致同步问题，client被多个线程访问
			{
				assert (client.getHostID() instanceof SocketClientID);

				out.println("Host joined: ");
				SocketClientID hostId = (SocketClientID) client.getHostID();
				out.println("0\t" + hostId.getAddress().getHostAddress() + "\t"
						+ hostId.getPort());
			} else {
				out.println("You have not joined any host yet.");
			}
		} finally {
			clientLock.unlock();
		}

	}

	/**
	 * 这个方法从command中提取第一个数字，如果找不到数字，抛出NumberFormatException
	 * 
	 * @param command
	 * @return
	 * @throws NumberFormatException
	 */
	private int extractNum(String command) throws NumberFormatException {
		if (null == command) {
			throw new NullPointerException("command should not be null");
		}

		boolean hasFound = false;
		int value = 0;

		StringTokenizer tokens = new StringTokenizer(command);
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			try {
				value = Integer.parseInt(token);
				hasFound = true;
			} catch (NumberFormatException ex) {

			}

			if (hasFound) {
				return value;
			}
		}

		throw new NumberFormatException(command);
	}

	private void handleAddBotCommand(String command) {
		int num = 0;
		try {
			num = extractNum(command);
		} catch (NumberFormatException ex) {
			num = 1;
		}

		if (0 == num) {
			commandFailure("illegal num: " + num, null);
			return;
		}

		boolean isPrivate = true;

		try {
			botManager.addBots(isPrivate, num);
		} catch (FailedToOpenException ex) {
			commandFailure("Failed to add bots. ", ex);
			return;
		}

		out.println("Successfully added " + num + " "
				+ (isPrivate ? "private" : "public") + " bots. ");

	}

	private void handleAddPublicBotCommand(String command) {
		int num = 0;
		try {
			num = extractNum(command);
		} catch (NumberFormatException ex) {
			num = 1;
		}

		if (0 == num) {
			commandFailure("illegal num: " + num, null);
			return;
		}

		boolean isPrivate = false;

		try {
			botManager.addBots(isPrivate, num);
		} catch (FailedToOpenException ex) {
			commandFailure("Failed to add bots. ", ex);
			return;
		}

		out.println("Successfully added " + num + " "
				+ (isPrivate ? "private" : "public") + " bots. ");
	}

	private void handleAddPrivateBotCommand(String command) {
		int num = 0;
		try {
			num = extractNum(command);
		} catch (NumberFormatException ex) {
			num = 1;
		}

		if (0 == num) {
			commandFailure("illegal num: " + num, null);
			return;
		}

		boolean isPrivate = true;

		try {
			botManager.addBots(isPrivate, num);
		} catch (FailedToOpenException ex) {
			commandFailure("Failed to add bots. ", ex);
			return;
		}

		out.println("Successfully added " + num + " "
				+ (isPrivate ? "private" : "public") + " bots. ");
	}

	private void handleRemoveBotCommand(String command) {
		int num = 0;
		try {
			num = extractNum(command);
		} catch (NumberFormatException ex) {
			num = 1;
		}

		if (0 == num) {
			commandFailure("illegal num: " + num, null);
			return;
		}

		boolean isPrivate = true;

		try {
			botManager.removeBots(isPrivate, num);
		} catch (FailedToCloseException ex) {
			commandFailure("Failed to close bots. ", ex);
			return;
		}

		out.println("Successfully removed " + num + " "
				+ (isPrivate ? "private" : "public") + " bots. ");
	}

	private void handleRemovePublicBotCommand(String command) {
		int num = 0;
		try {
			num = extractNum(command);
		} catch (NumberFormatException ex) {
			num = 1;
		}

		if (0 == num) {
			commandFailure("illegal num: " + num, null);
			return;
		}

		boolean isPrivate = false;

		try {
			botManager.removeBots(isPrivate, num);
		} catch (FailedToCloseException ex) {
			commandFailure("Failed to close bots. ", ex);
			return;
		}

		out.println("Successfully removed " + num + " "
				+ (isPrivate ? "private" : "public") + " bots. ");

	}

	private void handleRemovePrivateBotCommand(String command) {
		int num = 0;
		try {
			num = extractNum(command);
		} catch (NumberFormatException ex) {
			num = 1;
		}

		if (0 == num) {
			commandFailure("illegal num: " + num, null);
			return;
		}

		boolean isPrivate = true;

		try {
			botManager.removeBots(isPrivate, num);
		} catch (FailedToCloseException ex) {
			commandFailure("Failed to close bots. ", ex);
			return;
		}

		out.println("Successfully removed " + num + " "
				+ (isPrivate ? "private" : "public") + " bots. ");

	}

	private void handleRemoveAllBotsCommand(String command) {
		try {
			botManager.removeAllBots();
		} catch (FailedToCloseException ex) {
			commandFailure("Failed to remove all bots. ", ex);
		}

		out.println("Successfully removed all bots, both private and public. ");
	}

	private void handleRemvoveAllPublicBotsCommand(String command) {
		try {
			botManager.removeAllBots(false);
		} catch (FailedToCloseException ex) {
			commandFailure("Failed to remove all public bots. ", ex);
		}

		out.println("Successfully removed all public bots. ");
	}

	private void handleRemoveAllPrivateBotsCommand(String command) {
		try {
			botManager.removeAllBots(true);
		} catch (FailedToCloseException ex) {
			commandFailure("Failed to remove all private bots. ", ex);
		}

		out.println("Successfully removed all private bots. ");
	}

	private void handleShowBotsCommand(String command) {
		out.println("Private bots: ");
		List<BOT> privateBotList = botManager.getPrivateBotList();
		int idx = 0;
		for (BOT bot : privateBotList) {
			assert (bot != null);

			out.print(idx);
			out.print("\t");
			if (bot.isPrivate()) {
				out.print("private");
			} else {
				out.print("public");
			}
			out.print("\t");
			out.print("cold");
			out.print("\t");
			out.print(bot.getBotState().name().toLowerCase());
			out.print("\t");
			if (bot.isOpen()) {
				out.print("active");
			} else {
				out.print("disabled");
			}

			out.println();

			idx++;
		}

		out.println("Public bots: ");
		List<BOT> publicBotList = botManager.getPublicBotList();
		idx = 0;
		for (BOT bot : publicBotList) {
			assert (bot != null);

			out.print(idx);
			out.print("\t");
			if (bot.isPrivate()) {
				out.print("private");
			} else {
				out.print("public");
			}
			out.print("\t");
			out.print("cold");
			out.print("\t");
			out.print(bot.getBotState().name().toLowerCase());
			out.print("\t");
			if (bot.isOpen()) {
				out.print("active");
			} else {
				out.print("disabled");
			}

			out.println();

			idx++;
		}

		out.println("Private bot total num: " + privateBotList.size());
		out.println("Public bot total num: " + publicBotList.size());
		out.println("Bot total num: "
				+ (privateBotList.size() + publicBotList.size()));
	}

	private void handleHelpCommand(String command) {
		out.println("================Command List================");
		out.println();

		// =======

		out.println(" * show hosts -\n Display all the detected LAN hosts. ");
		out.println();

		out.println(" * host <maxPlayerNum> -\n Create a game hosted by you and then wait for players to join in. ");
		out.println();

		out.println(" * show clients -\n Display who have connected to your game. ");
		out.println();

		out.println(" * kick (<IP address> <port>)|(client id) -\n Kick on of the clients that have joined your game. ");

		out.println(" * start <barVel> [<barRowNum> <barColumnNum>] [>demo file name] -\n Launch the game you are hosting. ");
		out.println();

		out.println(" * join (<IP address> <port>)|(host id) [>demo file name] -\n Join one of the games hosted by others. ");
		out.println();

		out.println(" * abort -\n Cancel current task. ");
		out.println();

		out.println(" * demo [file1 file2 file3 ...] -\n Watch demos. ");
		out.println();

		out.println(" * show joined -\n Show which host you have currently joined. ");
		out.println();

		out.println(" * add bot [num] -\n Same with command add private bot [num]. ");
		out.println();

		out.println(" * add public bot [num] -\n Add public bots with quantity specified by num. ");
		out.println();

		out.println(" * add private bot [num] -\n Add private bots with quantity specified by num. ");
		out.println();

		out.println(" * remove bot [num] -\n Same with command remove private bot [num]. ");
		out.println();

		out.println(" * remove public bot [num] -\n Remove public bots with quantity specified by num. ");
		out.println();

		out.println(" * remove private bot [num] -\n Remove private bots with quantity specified by num. ");
		out.println();

		out.println(" * remove all bots -\n To remove all public bots and all private bots. ");
		out.println();

		out.println(" * remove all public bots -\n To remove all public bots. ");
		out.println();

		out.println(" * remove all private bots -\n To rmove all private bots. ");
		out.println();

		out.println(" * show bots -\n Show bots info. ");
		out.println();

		out.println(" * help -\n Show help context. ");
		out.println();

		out.println(" * exit -\n Shut down the program. ");

		// =======

		out.println();
		out.println("============================================");

		out.println("========Detailed Command Help context========");

		out.println();

		out.print(" * show hosts - ");
		out.println("Display all the detected LAN hosts. "
				+ "You can see their IP addresses and ports, "
				+ "by which you can join their games. ");

		out.println();

		out.print(" * host <maxPlayerNum> - ");
		out.println("Create a game and you are the host. "
				+ "Then you have to wait for other players to join you. "
				+ "You can use command show clients to see who has been joining your game. "
				+ "If you want to cancel the host game, you can use command abort. "
				+ "When you think you have waited long enough, you can use command "
				+ "start to launch the game, which means you can play now. "
				+ "<maxPlayerNum> means how many people your game is able to involve.");

		out.println();

		out.print(" * show clients - ");
		out.println("When you have created a host game using command host <maxPlayerNum>, "
				+ "you can use this command to see who has been joining your game. ");

		out.println();

		out.print(" * kick <IP address> <port> " + "or kick <client id> - ");
		out.println("When you have created a host game using command host <maxPlayerNum>, "
				+ "you can use this command to enforce a client to leave. you can use his ip address"
				+ " and port number as the arguments, or use the client id number you see when you"
				+ "use command show clients. ");

		out.println();

		out.print(" * start [>demo file name] "
				+ "or start <barVel> [>demo file name] "
				+ "or start <barVel> <barRowNum> <barColumnNum> [>demo file name] - ");
		out.println("When you have created a host game using command host <maxPlayerNum>, "
				+ "you can then launch the game using this command. "
				+ "Launching the game means you can play then. After launching the game, others "
				+ "players can not join you any more. "
				+ "<barVel> decides the dropping velocity of the bar. You can use 0.5, 1, 2, 3, ... . "
				+ "<barRowNum> decides the row number of the bar. "
				+ "<barColumnNum> decides the column number of the bar. "
				+ "[>demo file name] you can append \" > <demo file name> \" after this command to record "
				+ "the game you have played to the specified file. ");

		out.println();

		out.print(" * join <IP address> <port> [>demo file name] "
				+ "or join <host id> [>demo file name] - ");
		out.println("This command is used to join the game created by other hosts. "
				+ "You can view net hosts via command show hosts, "
				+ "from which you can get the IP addresses and port numbers to join their games, "
				+ "if you use command join<IP address> <port>. You can also use join <host id>. "
				+ "<host id> is the id number for each host in the table printed by command show hosts. "
				+ "After you have joined a game using this command, you have to wait for a while. "
				+ "That's because only when the host of the game has launched the game using command start, "
				+ "can you start to play. "
				+ "If you don't want to wait any more, you can use abort to drop out of the game you have joined. "
				+ "[>demo file name] you can append \" > <demo file name> \" after this command to record "
				+ "the game you have played to the specified file. ");

		out.println();

		out.print(" * abort - ");
		out.println("This command is used to exit. "
				+ "That means if you have been hosting a game using command host <maxPlayerNum>( and not launched the game yet), "
				+ "You can use this command to caccel it. "
				+ "If you have been joining other hosts's games, you can use the command to drop out of the game, too. "
				+ "Note that when you are playing, i.e. when you can manipulate the dropping blocks in the popped out window, "
				+ "you can abort simple by close the window. There is no need to use this command in that condition. ");

		out.println();

		out.print(" * demo " + "or demo file1 file2 file3 ... -");
		out.println("This command is used to watch demos. When you use this command without arguments, "
				+ "there will pop up file brownzer. You can choose one or more demo files. "
				+ "You can also use this command with arguments to specify one or more demo files. "
				+ "You can watch them in the same time. "
				+ "The command line will print the files you have selected. "
				+ "Note that demos are dislayed in the same order(left to right, up to down) with the files printed.");

		out.println();

		out.print(" * show joined - ");
		out.println("Show which host you have currently joined. ");

		out.println();

		out.print(" * add bot " + "or add bot <num> - ");
		out.println("Same with command add private bot. ");
		out.println();

		out.print(" * add public bot " + "or add public bot <num> - ");
		out.println("Add public bots with quantity specified by num. Default num is 1. "
				+ "A public bot is a bot than will try to join any host's game that it has detected."
				+ "While a private bot is a bot that will only join the local host's game. ");
		out.println();

		out.print(" * add private bot " + "or add private bot <num> - ");
		out.println("Add private bots with quantity specified by num. Default num is 1. ");
		out.println();

		out.print(" * remove bot " + "or remove bot <num> - ");
		out.println("Same with command remove private bot. ");
		out.println();

		out.print(" * remove public bot" + "or remove public bot <num> - ");
		out.println("Remove public bots with quantity specified by num. Default num is 1. ");
		out.println();

		out.print(" * remove private bot " + "or remove private bot <num> - ");
		out.println("Remove private bots with quantity specified by num. Default num is 1. ");
		out.println();

		out.print(" * remove all bots - ");
		out.println("To remove all public bots and all private bots. ");
		out.println();

		out.print(" * remove all public bots - ");
		out.println("To remove all public bots. ");
		out.println();

		out.print(" * remove all private bots - ");
		out.println("To rmove all private bots. ");
		out.println();

		out.print(" * show bots - ");
		out.println("Show bots info. ");
		out.println();

		out.print(" * help - ");
		out.println("You can use this command to get help text. Actually that's you are reading now. ");

		out.println();

		out.print(" * exit - ");
		out.println("Shut down the program. It is different from command abort since abort only cancel what you are doing, "
				+ "while this command directly shut down the whole program. ");

		out.println();

		out.println("========================================");
	}

	private void handleExitCommand(String command) {
		out.println("Exiting...");

		try {
			this.close();

			for (RunningStation s : stationList) {
				assert (s != null);

				if (s.isOpen()) {
					s.close();
				}
			}

			out.close();
			in.close();

		} catch (FailedToCloseException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		System.exit(0);
	}

	private void handleIllegalCommand(String command) {
		out.println("IllegalCommand: " + command
				+ ". \nInput help to get command usage.");
	}

	private void commandFailure(String reason, Exception ex) {
		out.println("Sorry, we have failed to perform this command");

		out.println("Reason: " + ((null == reason) ? "none" : reason));
		out.println("Exception: " + ((null == ex) ? "none" : ex.getMessage()));
	}

	// 这个方法用来在得到一个TetrixRunningStation的情况下，将它显示出来
	private void popupTetrixRunningStationPanel(RunningStation station,
			String title) {
		if (null == station) {
			throw new NullPointerException("station should not be null");
		}

		// 清除无效的TetrixRunningStation登记记录
		Iterator<RunningStation> itr = stationList.iterator();
		while (itr.hasNext()) {
			RunningStation s = itr.next();

			assert (s != null);

			if (!s.isOpen()) {
				itr.remove();
			}
		}

		// 登记这个TetrixRunningStation
		stationList.add(station);

		// 显示
		final JFrame frame=new RunningStationFrame(station, title);
		frame.setVisible(true);
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	frame.toFront();
		    	frame.repaint();
		    }
		});
	}

}
