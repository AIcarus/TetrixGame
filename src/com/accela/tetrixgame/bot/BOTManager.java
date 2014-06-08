package com.accela.tetrixgame.bot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToCloseException;
import com.accela.tetrixgame.conn.support.SynchronizeSupport.standard.FailedToOpenException;

/**
 * 
 * 游戏中常常会新建一大堆BOT，因此BOT的添加、删除等操作由这个类负责
 * 
 */
public class BOTManager {
	private List<BOT> publicBotList = new LinkedList<BOT>();
	private List<BOT> privateBotList = new LinkedList<BOT>();

	public List<BOT> getPublicBotList() {
		return Collections.unmodifiableList(publicBotList);
	}

	public List<BOT> getPrivateBotList() {
		return Collections.unmodifiableList(privateBotList);
	}

	/**
	 * 批量添加
	 */
	public void addBots(boolean isPrivate, int num)
			throws FailedToOpenException {
		if (num <= 0) {
			throw new IllegalArgumentException("num should be positive");
		}

		boolean hasException = false;
		FailedToOpenException excep = null;

		for (int i = 0; i < num; i++) {
			try {
				addBot(isPrivate);
			} catch (FailedToOpenException ex) {
				hasException = true;
				excep = ex;
			}
		}

		if (hasException) {
			assert (excep != null);
			throw excep;
		}
	}

	public void addBot(boolean isPrivate) throws FailedToOpenException {
		BOT bot = new BOT(isPrivate);

		try {
			bot.open();
		} catch (FailedToOpenException ex) {
			throw ex;
		}

		if (isPrivate) {
			privateBotList.add(bot);
		} else {
			publicBotList.add(bot);
		}
	}

	/**
	 * 批量删除
	 */
	public void removeBots(boolean isPrivate, int num)
			throws FailedToCloseException {
		if (num <= 0) {
			throw new IllegalArgumentException("num should be positive");
		}

		boolean hasException = false;
		FailedToCloseException excep = null;

		for (int i = 0; i < num; i++) {
			try {
				removeBot(isPrivate);
			} catch (FailedToCloseException ex) {
				hasException = true;
				excep = ex;
			}
		}

		if (hasException) {
			assert (excep != null);
			throw excep;
		}
	}

	public void removeBot(boolean isPrivate) throws FailedToCloseException {
		if (isPrivate) {
			if (!privateBotList.isEmpty()) {
				BOT bot = privateBotList.remove(0);
				assert (bot != null);
				assert (bot.isOpen());
				assert (bot.isPrivate());

				try {
					bot.close();
				} catch (FailedToCloseException ex) {
					throw ex;
				}
			}
		} else {
			if (!publicBotList.isEmpty()) {
				BOT bot = publicBotList.remove(0);
				assert (bot != null);
				assert (bot.isOpen());
				assert (!bot.isPrivate());

				try {
					bot.close();
				} catch (FailedToCloseException ex) {
					throw ex;
				}
			}
		}
	}

	public void removeAllBots(boolean isPrivate) throws FailedToCloseException {
		boolean hasException = false;
		FailedToCloseException excep = null;

		if (isPrivate) {
			while (!privateBotList.isEmpty()) {
				try {
					removeBot(true);
				} catch (FailedToCloseException ex) {
					excep = ex;
					hasException = true;
				}
			}
		} else {
			while (!publicBotList.isEmpty()) {
				try {
					removeBot(false);
				} catch (FailedToCloseException ex) {
					excep = ex;
					hasException = true;
				}
			}

		}

		if (hasException) {
			assert (excep != null);
			throw excep;
		}
	}

	/**
	 * 删除所有BOT，无论private还是public
	 */
	public void removeAllBots() throws FailedToCloseException {
		boolean hasException = false;
		FailedToCloseException excep = null;

		try {
			removeAllBots(true);
		} catch (FailedToCloseException ex) {
			hasException = true;
			excep = ex;
		}

		try {
			removeAllBots(false);
		} catch (FailedToCloseException ex) {
			hasException = true;
			excep = ex;
		}

		if (hasException) {
			assert (excep != null);
			throw excep;
		}
	}

	public void close() throws FailedToCloseException {
		removeAllBots();
	}

}
