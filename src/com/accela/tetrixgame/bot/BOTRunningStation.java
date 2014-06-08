package com.accela.tetrixgame.bot;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.accela.tetrixgame.bot.ai.AI;
import com.accela.tetrixgame.conn.ConnectionCenter;
import com.accela.tetrixgame.conn.ConnectionDoesNotExistException;
import com.accela.tetrixgame.conn.connector.FailedToSendMessageException;
import com.accela.tetrixgame.conn.shared.ClientID;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;
import com.accela.tetrixgame.gamelogic.GameAttributes;
import com.accela.tetrixgame.gamelogic.GameLogic;
import com.accela.tetrixgame.net.basic.Engine;
import com.accela.tetrixgame.net.basic.EngineMirror;
import com.accela.tetrixgame.net.hostClient.EnforceDisconnectMessage;
import com.accela.tetrixgame.net.hostClient.RunningStation;
import com.accela.tetrixgame.util.IntervalTimer;
import com.accela.tetrixgame.util.KeyList;
import com.accela.tetrixgame.util.TimeCounter;

/**
 * 
 * BOTRunningStation建立的时候，需要传入一个TetrixRunningStation的实例，
 * 代表一场游戏，然后BOTRunningStation会作为TetrixRunningStation的包装，
 * 用TetrixAI来智能模拟玩家的按键操作，来进行游戏。
 * 简单来说，BOTRunningStation就是在TetrixRunningStation上加上了一层封装， 让BOT来控制游戏。
 * 
 */
public class BOTRunningStation {// TODO
								// 如果TetrixRunningStation被修改过了，则整个BOT包中的东西都需要重新检查
	private RunningStation station;

	private ConnectionCenter connectionCenter;

	private GameLogic game;

	private AI ai;

	private KeyList keyList = new KeyList();

	public static final long UPDATE_INTERVAL = RunningStation.UPDATE_INTERVAL;

	public BOTRunningStation(RunningStation station, AI ai) {
		if (null == station) {
			throw new NullPointerException("station should not be null");
		}
		if (station.isOpen()) {
			throw new IllegalArgumentException("station should not be open");
		}
		if (null == ai) {
			throw new NullPointerException("ai should not be null");
		}

		this.station = station;
		this.ai = ai;

		try {
			initStation();
		} catch (SecurityException ex) {
			ex.printStackTrace();
			assert (false);
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
			assert (false);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			assert (false);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			assert (false);
		}

	}

	@SuppressWarnings("unchecked")
	private void initStation() throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		assert (station != null);
		assert (!station.isOpen());

		Class<?> stationClass = station.getClass();

		// 用FalseMirror替换掉TetrixEngineMirror
		Field field = stationClass.getDeclaredField("mirrorMap");
		field.setAccessible(true);

		Map<ClientID, EngineMirror> mirrorMap = (Map<ClientID, EngineMirror>) field
				.get(station);
		assert (mirrorMap != null);

		field = mirrorMap.getClass().getDeclaredField("m");
		field.setAccessible(true);
		mirrorMap = (Map<ClientID, EngineMirror>) field.get(mirrorMap);

		for (ClientID client : mirrorMap.keySet()) {
			assert (client != null);

			EngineMirror mirror = mirrorMap.get(client);
			assert (mirror != null);

			Object ret = mirrorMap.put(
					client,
					new FalseMirror(mirror.getGameFieldWidth(), mirror
							.getGameFieldHeight()));
			assert (ret == mirror);
		}

		// 替换attr，关闭其效果和音效
		field = stationClass.getDeclaredField("attr");
		field.setAccessible(true);

		GameAttributes attr = (GameAttributes) field.get(station);
		assert (attr != null);

		attr.put(GameAttributes.ENABLE_EFFECTS, "false");
		attr.put(GameAttributes.ENABLE_SOUND, "false");

		// 用关闭音效和效果的TetrixEngine替换原来的
		field = stationClass.getDeclaredField("engine");
		field.setAccessible(true);

		Engine engine = (Engine) field.get(station);
		assert (engine != null);

		field.set(station, new Engine(attr));

		// 清除录像
		station.setDemoOutputStream(null);

		// 得到TetrixEngine中的gamelogic对象
		field = stationClass.getDeclaredField("engine");
		field.setAccessible(true);

		engine = (Engine) field.get(station);
		assert (engine != null);

		field = engine.getClass().getDeclaredField("game");
		field.setAccessible(true);

		GameLogic game = (GameLogic) field.get(engine);
		assert (game != null);

		this.game = game;

		// 抽取ConnectionCenter
		field = stationClass.getDeclaredField("connectionCenter");
		field.setAccessible(true);

		connectionCenter = (ConnectionCenter) field.get(station);
		assert (connectionCenter != null);

		// 将station的enforceDisconnectable属性设为true
		station.setEnforceDisconnectable(true);

	}

	public void close() throws FailedToCloseException {
		station.close();
	}

	public List<ClientID> getAllClients() {
		return station.getAllClients();
	}

	public int getGameFieldHeight() {
		return station.getGameFieldHeight();
	}

	public int getGameFieldWidth() {
		return station.getGameFieldWidth();
	}

	public boolean isAllGameOver() {
		return station.isAllGameOver();
	}

	public boolean isLocalGameOver() {
		return station.isLocalGameOver();
	}

	// 因为BOT用FalseMirror替换了所有TetrixEngineMirror,因此无法知道Client是否游戏结束
	public boolean isClientsAllGameOver() {
		List<ClientID> clients = station.getAllClients();
		for (ClientID client : clients) {
			assert (client != null);
			if (station.isClientValid(client)) {
				return false;
			}
		}

		return true;
	}

	public boolean isClientValid(ClientID client) {
		return station.isClientValid(client);
	}

	public boolean isOpen() {
		return station.isOpen();
	}

	public void open() throws FailedToOpenException {
		station.open();
	}

	public void setDemoOutputStream(OutputStream out) {
		station.setDemoOutputStream(out);
	}

	private static final EnforceDisconnectMessage botNotifyMessage = new EnforceDisconnectMessage();

	private TimeCounter timeCounter = new TimeCounter(2000);

	private IntervalTimer intervalTimer = new IntervalTimer();

	public void updateAndPaintEngine(Graphics g, Point orign,
			ImageObserver observer, double scale) {
		// 用TetrixAI模拟BOT操作游戏
		keyList.clear();
		assert (keyList.isEmpty());

		ai.update(keyList, game);

		synchronized (keyList) {
			for (KeyList.KeyEntry entry : keyList) {
				assert (entry != null);

				if (entry.isPressed()) {
					station.keyPressed(entry.getKeyCode());
				} else {
					station.keyReleased(entry.getKeyCode());
				}
			}
		}

		// 刷新游戏
		station.updateAndPaintEngine(g, orign, observer, scale);

		// 定时将EnforceDisconnectMessage送给各个Client，这样保证当一个网络
		// 游戏上只有BOT的时候，能够退出

		intervalTimer.update();
		long interpolation = intervalTimer.getInterval();
		if (interpolation >= 0) {
			timeCounter.update(interpolation);
		}
		if (timeCounter.isBeep()) {
			timeCounter.clearBeep();

			for (ClientID client : connectionCenter.getConnectedClientIDs()) {
				try {
					connectionCenter.sendMessage(client, botNotifyMessage);
				} catch (ConnectionDoesNotExistException ex) {
					ex.printStackTrace();
				} catch (FailedToSendMessageException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
