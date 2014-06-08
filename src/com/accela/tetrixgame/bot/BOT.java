package com.accela.tetrixgame.bot;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;

import com.accela.tetrixgame.Common;
import com.accela.tetrixgame.bot.ai.AI;
import com.accela.tetrixgame.bot.aiImpl.ColdAIBeta;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.support.SocketConnectionCenter.SocketClientID;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyClosedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.AlreadyOpenedException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.net.basic.HostBroadcastReceiver;
import com.accela.tetrixgame.net.hostClient.FailedException;
import com.accela.tetrixgame.net.hostClient.NetClient;
import com.accela.tetrixgame.net.hostClient.RunningStation;
import com.accela.tetrixgame.util.GlobalUpdater;

/**
 * 
 * 实现电脑机器人的类。 你只需要建立一个BOT实例，这个BOT就会自动寻找可以加入 的主机，加入之并进行游戏。
 * 
 */
public class BOT {
	/**
	 * 被BOT用来监听网络上主机的广播，从而识别主机
	 */
	private static final HostBroadcastReceiver broadcastReceiver = new HostBroadcastReceiver();

	static {
		try {
			broadcastReceiver.open();

		} catch (FailedToOpenException ex) {
			ex.printStackTrace();
		}
	}

	public static final long BOT_WORK_INTERVAL = 1000;

	private boolean open = false;

	/**
	 * private表示这个BOT只能加入本地主机的游戏，而public表示BOT 会有限加入本地主机的游戏，也能加入其他IP上的游戏。
	 */
	private boolean isPrivate = true;
	/**
	 * 管理BOT的AI的对象
	 */
	private AI ai;
	/**
	 * 实现BOT的功能的线程。
	 */
	private BOTWorker worker;

	public static enum BOTState {
		FINDING, JOINING, PLAYING,
	}

	private BOTState botState = BOTState.FINDING;

	public BOT(boolean isPrivate) {
		this(null, isPrivate);
	}

	public BOT(AI ai, boolean isPrivate) {
		if (null == ai) {
			ai = new ColdAIBeta();
		}

		this.ai = ai;
		this.isPrivate = isPrivate;

	}

	public boolean isOpen() {
		return open;
	}

	public void open() throws FailedToOpenException {
		if (open) {
			throw new AlreadyOpenedException();
		}

		try {
			open = true;

			worker = new BOTWorker();
			worker.start();

		} catch (Exception ex) {
			open = false;

			throw new FailedToOpenException(ex);
		}
	}

	public void close() throws FailedToCloseException {
		if (!open) {
			throw new AlreadyClosedException();
		}

		try {
			open = false;

		} finally {
			worker.interrupt();
		}

	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public BOTState getBotState() {
		return botState;
	}

	/**
	 * 
	 * BOT的工作由这个线程来完成
	 * 
	 */
	private class BOTWorker extends Thread {
		private InetAddress localHost = null;

		public BOTWorker() {
			super("BOT - BOTWorker");
		}

		public void run() {
			try {
				localHost = InetAddress.getLocalHost();
			} catch (UnknownHostException ex) {
				System.err.println("BOT can't work because unknown local host");
				ex.printStackTrace();
				return;
			}

			while (open) {
				serveHosts();

				try {
					Thread.sleep(BOT_WORK_INTERVAL);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}

		/**
		 * 探测Host并加入之进行游戏。
		 */
		private void serveHosts() {
			Common.LOG.info("BOT serving...");
			// 选择一个Host
			botState = BOTState.FINDING;
			ClientID host = selectHost();
			if (null == host) {
				return;
			}

			// 加入这个Host
			botState = BOTState.JOINING;
			NetClient client = new NetClient();
			RunningStation station = null;
			try {
				Common.LOG.info("BOT launch game: "+host);
				station = client.launchGame(host);
			} catch (FailedException ex) {
				try {
					client.abort();
				} catch (FailedToCloseException ex_inner) {
					ex_inner.printStackTrace();
				}

				ex.printStackTrace();

				return;
			}

			if (null == station) {
				return;
			}

			// 建立BOTRunningStation来作为BOT参加游戏
			BOTRunningStation botStation = new BOTRunningStation(station, ai);

			// 打开BotRunningStation
			try {
				botStation.open();
			} catch (FailedToOpenException ex) {
				try {
					botStation.close();
				} catch (FailedToCloseException ex_inner) {
					ex_inner.printStackTrace();
				}

				ex.printStackTrace();

				return;
			}

			// 进行游戏直到所有玩家都已经退出，但是刷新游戏的任务交给另一个线程来做
			botState = BOTState.PLAYING;
			BOTUpdateRunnable runnable = new BOTUpdateRunnable(botStation);

			Common.LOG.info("bot " + this + " update task delivered");
			try {
				GlobalUpdater.submitTask(runnable);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			Common.LOG.info("bot " + this + " returned to work");

			try {
				botStation.close();
			} catch (FailedToCloseException ex) {
				ex.printStackTrace();
			}

		}

		/**
		 * 根据isPrivate属性选择一个host以待加入
		 */
		private ClientID selectHost() {
			assert (localHost != null);
			Set<ClientID> hostSet = broadcastReceiver.getHosts();
			assert (hostSet != null);

			// 优先查找和自己处于同个IP上的Host
			for (ClientID host : hostSet) {
				assert (host != null);
				assert (host instanceof SocketClientID);

				SocketClientID sHost = (SocketClientID) host;

				if (sHost.getAddress().equals(localHost)) {
					return sHost;
				}
			}

			// public bot can join any ip
			if (!isPrivate) {
				// 随机选择一个Host加入
				ClientID[] hosts = hostSet.toArray(new ClientID[0]);
				int randIdx = (int) (Math.random() * hosts.length);

				if (randIdx >= hosts.length) {
					return null;
				} else {
					return hosts[randIdx];
				}
			}
			
			// if no host found, use host address file 
			File hostAddr = Common.HOST_ADDRESS;
			Scanner scanner=null;
			try{
				scanner=new Scanner(hostAddr);
				String line=scanner.nextLine().trim();
				Common.LOG.info("BOT read from host_address.txt: "+line);
				SocketClientID host=new SocketClientID(line);
				Common.LOG.info("BOT read from host_address.txt and parse: "+host);
				return host;
			} catch (IOException ex) {
				// swallow it
				ex.printStackTrace();
			}finally{
				if(scanner!=null){
					scanner.close();
				}
			}
			
			return null;
		}
	}

	/**
	 * 
	 * 一个Bot，当进行游戏的时候，它需要不断刷新自己的游戏，这个刷新的任务 就由这个类来表示。调用其run方法一次，表示刷新一次。
	 * 
	 */
	private class BOTUpdateRunnable implements GlobalUpdater.UpdateRunnable {
		private BOTRunningStation botStation = null;

		public BOTUpdateRunnable(BOTRunningStation botStation) {
			if (null == botStation) {
				throw new NullPointerException("botStation should not be null");
			}

			this.botStation = botStation;
		}

		@Override
		public void run() {
			botStation.updateAndPaintEngine(null, null, null, 0);
		}

		public boolean isOver() {
			return !open || botStation.isLocalGameOver()
					|| botStation.isClientsAllGameOver();
		}
	}

}
